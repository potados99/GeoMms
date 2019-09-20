package com.potados.geomms.feature.location

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import com.potados.geomms.databinding.ConnectionsFragmentBinding
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.connections_fragment.*
import kotlinx.android.synthetic.main.connections_fragment.view.*

class ConnectionsFragment : BaseFragment() {

    private lateinit var connectionsViewModel: ConnectionsViewModel
    private lateinit var viewDataBinding: ConnectionsFragmentBinding

    private var connectionsAdapter = ConnectionsAdapter()
    private val requestsAdapter = RequestsAdapter()

    init {
        failables += this
        failables += connectionsAdapter
        failables += requestsAdapter
    }

    /**
     * Propagate actions to outside
     * Place this here, not in the sheetView model.
     */
    var onShowConnectionOnMap: (connection: Connection) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionsViewModel = getViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ConnectionsFragmentBinding.inflate(inflater)
            .apply { vm = connectionsViewModel }
            .apply { lifecycleOwner = this@ConnectionsFragment }
            .apply { viewDataBinding = this }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {

        with(view.root_layout) {
            animateLayoutChanges = true
            layoutTransition = LayoutTransition().apply {
                setAnimateParentHierarchy(false)
            }
        }

        with(view.connections) {
            adapter = connectionsAdapter.apply {
                emptyView = view.empty_view

                onConnectionClick = {
                    bottomSheetManager?.push(ConnectionDetailFragment())
                    // connectionsViewModel.showConnectionInfo(it)

                    // Running the outside code should be happened here, I think.
                    onShowConnectionOnMap(it)
                }
                onConnectionLongClick = {
                    connectionsViewModel.askDeleteConnection(activity, it)
                }
            }

            // For scroll in bottom sheet.
            // addOnItemTouchListener(onItemTouchListener)
        }

        with(view.incoming_requests) {
            adapter = requestsAdapter.apply {
                companionView = view.incoming_requests_layout

                onRequestClick = {
                    connectionsViewModel.showRequestInfo(it)
                }
                onRequestLongClick = {
                    connectionsViewModel.askDeleteRequest(activity, it)
                }
            }

            // For scroll in bottom sheet.
            // addOnItemTouchListener(onItemTouchListener)
        }
    }


}