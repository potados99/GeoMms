package com.potados.geomms.feature.location

import android.animation.LayoutTransition
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.snackbar.Snackbar
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import com.potados.geomms.databinding.MapFragmentBinding
import com.potados.geomms.util.Notify
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.map_fragment.view.*
import timber.log.Timber

class MapFragment : NavigationFragment(), OnMapReadyCallback {

    override val optionMenuId: Int? = R.menu.map
    override val navigationItemId: Int = R.id.menu_item_navigation_map
    override val titleId: Int = R.string.title_friends

    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewDataBinding: MapFragmentBinding

    private var connectionsAdapter = ConnectionsAdapter()
    private val requestsAdapter = RequestsAdapter()

    private var map: GoogleMap? = null

    /**
     * Invoked when ACTION_SET_ADDRESS intent received.
     */
    private val addressSetReceiver = newBroadcastReceiver {
        it?.getStringExtra(EXTRA_ADDRESS)?.let { address -> mapViewModel.request(activity, address) }
    }

    init {
        failables += this
        failables += requestsAdapter
        failables += connectionsAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(context)

        mapViewModel = getViewModel { start() }
        failables += mapViewModel.failables

        context?.registerReceiver(addressSetReceiver, IntentFilter(ACTION_SET_ADDRESS))
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
                mapViewModel.invite()
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
        context?.unregisterReceiver(addressSetReceiver)
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
                fail(R.string.fail_exception, e.message, show = false)
            }

            // Move to current location.
            mapViewModel.getLocation {
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
            adapter = connectionsAdapter.apply {
                emptyView = view.empty_view

                onConnectionClick = { mapViewModel.showConnectionInfo(activity, map, it) }
                onConnectionLongClick = { mapViewModel.showConnectionDeletionConfirmation(activity!!, it) }
            }

            // For scroll in bottom sheet.
            addOnItemTouchListener(onItemTouchListener)
        }

        with(view.incoming_requests) {
            adapter = requestsAdapter.apply {
                companionView = view.incoming_requests_layout

                onRequestClick = { mapViewModel.showRequestInfo(activity, it) }
                onRequestLongClick = { mapViewModel.showRequestDeletionConfirmation(activity, it) }
            }

            // For scroll in bottom sheet.
            addOnItemTouchListener(onItemTouchListener)
        }

        with(view.sheet) {
            onSlideBottomSheet(view, 0f)

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
                    onSlideBottomSheet(view, slideOffset)
                }
            })

            setOnClickListener {
                toggleSheet()
            }
        }

        with(view.bottom_sheet_root_layout) {
            animateLayoutChanges = true
            layoutTransition = LayoutTransition().apply {
                setAnimateParentHierarchy(false)
            }
        }
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

    private fun onSlideBottomSheet(view: View, offset: Float) {
        with(view) {
            empty_view.setVerticalBiasByOffset(offset)
            grip.setAlphaByOffset(offset)
            sheet.setBackgroundRadiusByOffset(offset)
        }
    }

    companion object {
        const val ACTION_SET_ADDRESS = "com.potados.geomms.SET_ADDR"
        const val EXTRA_ADDRESS = "address"
    }
}
