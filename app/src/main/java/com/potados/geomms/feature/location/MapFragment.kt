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
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import com.potados.geomms.databinding.MapFragmentBinding
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.map_fragment.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import android.animation.LayoutTransition
import com.potados.geomms.R

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
    private val dateFormatter: DateFormatter by inject()

    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewDataBinding: MapFragmentBinding

    private lateinit var connectionsAdapter: ConnectionsAdapter
    private val requestsAdapter = RequestsAdapter(this)

    /**
     * Invoked when ACTION_SET_ADDRESS intent received.
     */
    private val receiver = createBroadcastReceiver {
        it?.getStringExtra(EXTRA_ADDRESS)?.let { address ->
            mapViewModel.request(address)
        }
    }

    private var map: GoogleMap? = null

    init {
        failables += this
        failables += navigator
        failables += locationRepo
        failables += requestsAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(context)

        mapViewModel = getViewModel { start() }
        connectionsAdapter = ConnectionsAdapter(context!!, this)

        failables += mapViewModel.failables
        failables += connectionsAdapter

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

        with(view.bottom_sheet_root_layout) {
            animateLayoutChanges = true
            layoutTransition = LayoutTransition().apply {
                setAnimateParentHierarchy(false)
            }
        }
    }

    override fun onConnectionClick(connection: Connection) {
        if (connection.isTemporal) {
            // Sent request not accepted yet
            Notify(context).short(R.string.notify_request_not_yet_accepted)
            return
        }

        withNonNull(map) {
            if (connection.lastUpdate != 0L) {
                moveTo(connection.latitude, connection.longitude, 15f)
            } else {
                Notify(context).short(R.string.notify_no_location_data)
            }
        }
    }

    override fun onConnectionLongClick(connection: Connection) {
        if (connection.isTemporal) {
            Popup(baseActivity)
                .withTitle(R.string.title_cancel_request)
                .withMessage(R.string.dialog_ask_cancel_request, connection.recipient?.getDisplayName())
                .withPositiveButton(R.string.button_yes) { mapViewModel.cancel(connection) }
                .withNegativeButton(R.string.button_no)
                .show()

        } else {
            Popup(baseActivity)
                .withTitle(R.string.title_disconnect)
                .withMessage(R.string.dialog_ask_disconnect, connection.recipient?.getDisplayName())
                .withPositiveButton(R.string.button_confirm) { mapViewModel.delete(connection) }
                .withNegativeButton(R.string.button_cancel)
                .show()
        }
    }

    override fun onInfoClick(connection: Connection) {
        val popup = Popup(context).withTitle(connection.recipient?.getDisplayName())

        if (connection.isTemporal) {
            popup
                .withMessage(R.string.dialog_invitation_sent)
                .withNewLine()
                .withNewLine()
                .withMoreMessage(R.string.dialog_sent_at, dateFormatter.getMessageTimestamp(connection.date))
                .withNewLine()
                .withMoreMessage(R.string.dialog_connection_id, connection.id)
                .withNewLine()
                .withMoreMessage(R.string.dialog_duration, dateFormatter.getDuration(connection.duration))
        } else {
            popup
                .withMessage(R.string.dialog_sharing_location)
                .withNewLine()
                .withNewLine()
                .withMoreMessage(R.string.dialog_connection_id, connection.id)
                .withNewLine()
                .withMoreMessage(R.string.dialog_from, dateFormatter.getMessageTimestamp(connection.date))
                .withNewLine()
                .withMoreMessage(R.string.dialog_until, dateFormatter.getMessageTimestamp(connection.due))
        }

        popup
            .withPositiveButton(R.string.button_ok)
            .show()
    }

    override fun onRequestClick(request: ConnectionRequest) {
        Popup(baseActivity)
            .withTitle(R.string.dialog_request_from, request.recipient?.getDisplayName())
            .withMessage(R.string.dialog_ask_accept_request, request.recipient?.getDisplayName(), dateFormatter.getDuration(request.duration))
            .withPositiveButton(R.string.button_accept) { mapViewModel.accept(request) }
            .withNegativeButton(R.string.button_later)
            .show()
    }

    override fun onRequestLongClick(request: ConnectionRequest) {
        Popup(baseActivity)
            .withTitle(R.string.dialog_refuse_request)
            .withMessage(R.string.dialog_ask_refuse, request.recipient?.getDisplayName())
            .withPositiveButton(R.string.button_refuse) { mapViewModel.refuse(request)  }
            .withNegativeButton(R.string.button_cancel)
            .show()
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
