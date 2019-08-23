package com.potados.geomms.feature.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.util.Log
import android.view.*
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
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import androidx.recyclerview.widget.RecyclerView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.potados.geomms.common.navigation.Navigator
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : NavigationFragment(),
    OnMapReadyCallback,
    ConnectionsAdapter.ConnectionClickListener, // TODO: remove
    RequestsAdapter.RequestClickListener // TODO: remove
{

    override val optionMenuId: Int? = R.menu.map
    override val navigationItemId: Int = R.id.menu_item_navigation_map
    override val titleId: Int = R.string.title_friends

    private val navigator: Navigator by inject()

    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewDataBinding: MapFragmentBinding

    private val connectionsAdapter = ConnectionsAdapter(this)
    private val requestsAdapter = RequestsAdapter(this)

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapViewModel = getViewModel()
        setAddressSelectReceiver()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return MapFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = mapViewModel }
            .apply { viewDataBinding = this }
            .apply { initializeView(root, savedInstanceState) }
            .root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.invite -> {
                navigator.showInvite()
            }
        }

        return true
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

    private fun setAddressSelectReceiver() {
        // TODO unregister extension
        context?.registerReceiver(ACTION_SET_ADDRESS) { intent ->
            intent?.getStringExtra("address")?.let { address ->
                mapViewModel.request(address)
                Notify(context).short("New request to $address")
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
            val behavior = params.behavior
            if (behavior != null && behavior is CustomBottomSheetBehavior<*>)
                behavior.setNestedScrollingChildRef(recyclerView)
        }
    }

    companion object {
        const val ACTION_SET_ADDRESS = "com.potados.geomms.SET_ADDR"
    }
}
