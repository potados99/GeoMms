package com.potados.geomms.feature.location

import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import com.potados.geomms.databinding.MapFragmentBinding
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.util.Notify
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.map_fragment.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : NavigationFragment(),
    OnMapReadyCallback,
    ConnectionsAdapter.ConnectionClickListener,
    RequestsAdapter.RequestClickListener
{

    override val optionMenuId: Int? = R.menu.map
    override val navigationItemId: Int = R.id.menu_item_navigation_map
    override val titleId: Int = R.string.title_friends

    private val navigator: Navigator by inject()
    private val locationRepo: LocationRepository by inject()

    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewDataBinding: MapFragmentBinding

    private val connectionsAdapter = ConnectionsAdapter(this)
    private val requestsAdapter = RequestsAdapter(this)

    /**
     * Invoked when ACTION_SET_ADDRESS intent received.
     */
    private val receiver = createBroadcastReceiver {
        it?.getStringExtra(EXTRA_ADDRESS)?.let { address ->
            mapViewModel.request(address)
            Notify(context).short("New request to $address")
        }
    }

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(context)

        mapViewModel = getViewModel { start() }
        context?.registerReceiver(receiver, IntentFilter(ACTION_SET_ADDRESS))
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
    }
    override fun onPause() {
        super.onPause()
        viewDataBinding.mapView.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewDataBinding.mapView.onDestroy()
        context?.unregisterReceiver(receiver)
    }
    override fun onLowMemory() {
        super.onLowMemory()
        viewDataBinding.mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map?.apply {

            // Set map UI.
            try {
                isMyLocationEnabled = true

                uiSettings.isCompassEnabled = true
                uiSettings.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                throw RuntimeException("THIS IS IMPOSSIBLE. CHECK PERMISSION.")
            }

            // Move to current location.
            locationRepo.getCurrentLocation()?.let {
                moveTo(it.latitude, it.longitude, 10f)
            }

            // Hide bottom sheet when map moving.
            setOnCameraMoveStartedListener {
                if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    viewDataBinding.sheet.collapseSheet()
                }
            }

            // Draw marker when connection refreshed.
            observe(mapViewModel.markers) { markers ->
                clear() // map
                markers?.forEach { marker ->
                    addMarker(marker) // map

                    Timber.i("Marker drawn.")
                }
            }
        }

        Timber.i( "Map is ready!")
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
        withNonNull(map) {
            if (connection.lastUpdate != 0L) {
                moveTo(connection.latitude, connection.longitude, 15f)
            }
        }
    }

    override fun onConnectionLongClick(connection: Connection) {
        mapViewModel.delete(connection)

        Notify(context).short("Deleted connection.")
    }

    override fun onRequestClick(request: ConnectionRequest) {
        mapViewModel.accept(request)

        Notify(context).short("Accept.")
    }

    override fun onRequestLongClick(request: ConnectionRequest) {
        mapViewModel.refuse(request)

        Notify(context).short("Refuse.")
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
        const val EXTRA_ADDRESS = "address"
    }
}
