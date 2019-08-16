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

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : NavigationFragment(),
    OnMapReadyCallback,
    ConnectionsAdapter.ConnectionClickListener,
    RequestsAdapter.RequestClickListener
{

    override fun menuId(): Int = R.id.menu_item_navigation_map

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        map_view?.onResume()

        Realm.getDefaultInstance().executeTransaction {
            it.insertOrUpdate(
                Connection(
                    id = 12345,
                    recipient = Recipient(123, "01029222661", null),
                    duration = 1800000,
                    date = System.currentTimeMillis()- 3000,
                    lastUpdate = System.currentTimeMillis()
                )
            )
            it.insertOrUpdate(
                ConnectionRequest(
                    connectionId = 12345,
                    recipient = Recipient(1234, "01043732663", null),
                    duration = 1800000,
                    date = System.currentTimeMillis()- 5000,
                    isInbound = true
                )
            )
        }


    }
    override fun onPause() {
        super.onPause()
        map_view?.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        map_view?.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        map_view?.onLowMemory()
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
                    with(bottom_sheet_layout) {
                        if (sheetState() == BottomSheetBehavior.STATE_HIDDEN) {
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

        Log.d("MapFragment: onMapReady", "map is ready!")
    }

    private fun initializeView(view: View, savedInstanceState: Bundle?) {
        with(view.map_view) {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment) /* 준비 끝나면 onMapReady 호출됨. */
        }

        with(view.connections) {

            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

            connectionsAdapter.emptyView = view.empty_view
            adapter = connectionsAdapter

            /**
             * 스크롤 이벤트가 리사이클러뷰에게 도착할 수 있게 해줍니다.
             */
            ViewCompat.setNestedScrollingEnabled(this, true)
        }

        with(view.incoming_requests) {
            requestsAdapter.companionView = view.incoming_requests_layout
            adapter = requestsAdapter
        }

        with(view.bottom_sheet_layout) {
            collapseSheet()

            bottomSheetBehavior().setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
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
}
