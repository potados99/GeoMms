package com.potados.geomms.feature.location

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.databinding.MapFragmentBinding
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Recipient
import com.potados.geomms.util.Notify
import io.realm.Realm
import io.realm.annotations.PrimaryKey
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.map_fragment.view.*
import kotlinx.android.synthetic.main.map_fragment.view.map_view
import kotlinx.android.synthetic.main.map_fragment.view.toolbar
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import timber.log.Timber

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : NavigationFragment(),
    OnMapReadyCallback,
    ConnectionsAdapter.ConnectionClickListener, // TODO: remove
    RequestsAdapter.RequestClickListener // TODO: remove
{

    override fun optionMenuId(): Int? = R.menu.map
    override fun navigationItemId(): Int = R.id.menu_item_navigation_map

    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewDataBinding: MapFragmentBinding

    private val connectionsAdapter = ConnectionsAdapter(this)
    private val requestsAdapter = RequestsAdapter(this)

    private var map: GoogleMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapViewModel = getViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return MapFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = mapViewModel }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = false) }
            .apply { initializeView(root, savedInstanceState) }
            .root
    }

    override fun onShow() {
        super.onShow()
        Timber.i("MapFragment is shown")
    }
    override fun onHide() {
        super.onHide()
        Timber.i("MapFragment is hidden")
    }
    override fun onResume() {
        super.onResume()
        viewDataBinding.mapView.onResume()

        Realm.getDefaultInstance().executeTransaction {
            for (i in (0..15)) {
                it.insertOrUpdate(
                    Connection(
                        id = 12345 + i.toLong(),
                        recipient = Recipient(123, "01029222661", null),
                        duration = 1800000,
                        date = System.currentTimeMillis()- 3000,
                        lastUpdate = System.currentTimeMillis()
                    )
                )
            }

            for (i in (0..10)) {
                it.insertOrUpdate(
                    ConnectionRequest(
                        connectionId = 13579 + i.toLong(),
                        recipient = Recipient(12345, "01043732663", null),
                        duration = 1800000,
                        date = System.currentTimeMillis()- 5000,
                        isInbound = true
                    )
                )
            }
        }
    }
    override fun onPause() {
        super.onPause()
        viewDataBinding.mapView.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewDataBinding.mapView.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        viewDataBinding.mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map?.apply {
            val seoul = LatLng(37.56, 126.97)

            MapsInitializer.initialize(context)

            moveCamera(CameraUpdateFactory.newLatLng(seoul))
            animateCamera(CameraUpdateFactory.zoomTo(10.0f))

            try {
                isMyLocationEnabled = true

                uiSettings.isCompassEnabled = true
                uiSettings.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                throw RuntimeException("THIS IS IMPOSSIBLE. CHECK PERMISSION.")
            }

            setOnCameraMoveStartedListener {
                if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    with(viewDataBinding.sheet) {
                        if (sheetState == BottomSheetBehavior.STATE_HIDDEN) {
                            //
                        }
                        else {
                            collapseSheet()
                        }
                    }
                }
            }

            setOnCameraMoveListener {

            }
        }

        Timber.i( "map is ready!")
    }

    private fun initializeView(view: View, savedInstanceState: Bundle?) {
        with(view.map_view) {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment) // onMapReady called after this done
        }

        with(view.connections) {
            connectionsAdapter.emptyView = view.empty_view
            adapter = connectionsAdapter

            addOnItemTouchListener(onItemTouchListener)
        }

        with(view.incoming_requests) {
            requestsAdapter.companionView = view.incoming_requests_layout
            adapter = requestsAdapter

            addOnItemTouchListener(onItemTouchListener)
        }

        with(view.sheet) {
            collapseSheet()

            bottomSheetBehavior().setBottomSheetCallback(object: CustomBottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        CustomBottomSheetBehavior.STATE_EXPANDED -> {
                            view.grip.alpha = 0f
                        }
                        else -> {
                            view.grip.alpha = 1f
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val param = empty_view.layoutParams as ConstraintLayout.LayoutParams
                    param.verticalBias = slideOffset / 2 + 0.05f
                    empty_view.layoutParams = param
                }
            })

            setOnClickListener {
                toggleSheet()
            }
        }
    }


    override fun onConnectionClick(connection: Connection) {
        Notify(context).short("Hello!")
    }

    override fun onRequestClick(request: ConnectionRequest) {
        Notify(context).short("Ya!!")
    }


    /**
     * Pass touch events of Bottom Sheet to Recycler View.
     */
    private val onItemTouchListener: RecyclerView.OnItemTouchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            setScrollable(viewDataBinding.sheet, rv)
            return false
        }
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    private fun setScrollable(bottomSheet: View, recyclerView: RecyclerView) {
        val params = bottomSheet.layoutParams
        if (params is CoordinatorLayout.LayoutParams) {
            val coordinatorLayoutParams = params as CoordinatorLayout.LayoutParams
            val behavior = coordinatorLayoutParams.getBehavior()
            if (behavior != null && behavior is CustomBottomSheetBehavior<*>)
                behavior.setNestedScrollingChildRef(recyclerView)
        }
    }
}
