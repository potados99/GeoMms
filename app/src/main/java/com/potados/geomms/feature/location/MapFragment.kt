package com.potados.geomms.feature.location

import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.extension.baseActivity
import com.potados.geomms.core.extension.failure
import com.potados.geomms.core.extension.getViewModel
import com.potados.geomms.core.extension.observe
import com.potados.geomms.core.platform.NavigationBasedFragment
import com.potados.geomms.core.util.Duration
import com.potados.geomms.core.util.Notify
import com.potados.geomms.core.util.Popup
import com.potados.geomms.feature.common.SmsReceiver
import com.potados.geomms.feature.location.data.LocationSupportConnection
import com.potados.geomms.feature.location.data.LocationSupportRequest
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map.view.map_view
import kotlinx.android.synthetic.main.fragment_map_friends_list.*
import kotlinx.android.synthetic.main.fragment_map_friends_list.view.*
import kotlinx.android.synthetic.main.fragment_map_friends_list.view.friends_list_add_button

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : NavigationBasedFragment(),
    OnMapReadyCallback,
    LocationSupportConnectionRecyclerViewAdapter.FriendClickListener {

    private lateinit var viewModel: MapViewModel
    private val adapter = LocationSupportConnectionRecyclerViewAdapter(this)
    private var map: GoogleMap? = null

    /**
     * NavigationBasedFragment 설정들.
     */
    override fun layoutId(): Int = R.layout.fragment_map
    override fun toolbarId(): Int? = null
    override fun toolbarMenuId(): Int? = null
    override fun menuItemId(): Int = R.id.menu_item_navigation_map
    override fun smsReceivedBehavior() = { address: String, body: String, date: Long ->
        viewModel.onMessageReceived(address, body)
    }
    override fun intentFilter(): IntentFilter? = IntentFilter(SmsReceiver.SMS_DELIVER_ACTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getViewModel {
            observe(connections, ::renderConnections)
            observe(incomingRequests, ::handleIncomingRequest)
            failure(failure, ::handleFailure)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_view.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view.onLowMemory()
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
        }

        Log.d("MapFragment: onMapReady", "map is ready!")
    }

    private fun renderConnections(connections: List<LocationSupportConnection>?) {
        if (connections.isNullOrEmpty()) {
            showNoFriends()
        }
        else {
            adapter.collection = connections.orEmpty()

            viewModel.markers.map { it.remove() }
            viewModel.markers.clear()

            connections.forEach { connection ->

                connection.lastReceivedPacket?.let { packet ->
                    map?.addMarker(
                        MarkerOptions().apply {
                            position(LatLng(packet.latitude, packet.longitude))
                            title(connection.person.address)
                            snippet("Last seen location")
                        }
                    )?.let {
                        viewModel.markers.add(it.apply { tag = connection.person.address })
                    }
                }
            }

            showConnections()
        }
    }

    private fun handleIncomingRequest(requests: List<LocationSupportRequest>?) {
        if (requests.isNullOrEmpty()) return

        val req = requests.first()

        notifyWithAction("Have new request from ${req.person.address} for ${Duration(req.lifeSpan).toShortenString()}.", "Accept") {
            viewModel.acceptRequest(req)
        }
    }

    private fun initializeView(view: View, savedInstanceState: Bundle?) {
        with(view) {

            /**
             * 지도 설정
             */
            with(map_view) {
                onCreate(savedInstanceState)
                getMapAsync(this@MapFragment) /* 준비 끝나면 onMapReady 호출됨. */
            }

            /**
             * 친구 목록 리사이클러뷰 외관 설정.
             */
            with(friends_list_recyclerview) {
                /**
                 * 아이템 사이에 선을 그어줍니다.
                 */
                addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

                layoutManager = LinearLayoutManager(context)

                adapter = this@MapFragment.adapter

                /**
                 * 스크롤 이벤트가 리사이클러뷰에게 도착할 수 있게 해줍니다.
                 */
                ViewCompat.setNestedScrollingEnabled(this, true)
            }

            /**
             * 친구 목록 레이아웃에 대한 클릭 리스너 설정.
             * Bottom Sheet 위쪽을 누르면 토글되도록 해줍니다.
             */
            with(friends_list_root_layout) {
                /**
                 * friendsListRoot는 친구 목록을 보여주는 Bottom Sheet의 루트 레이아웃입니다.
                 * 리스트 이외의 영역을 터치하면 Sheet가 올라가거나 내려가도록 리스너를 등록해줍니다.
                 */
                setOnClickListener {

                    /**
                     * 현재 최대 확장 상태일 때를 제외하면 사용자가 친구 목록을 자세히 보려는 의도로 해석합니다.
                     * 따라서 STATE_EXPANDED 상태일 때에는 STATE_COLLAPSED 상태로 바꾸고,
                     * 그 이외에는 STATE_EXPANDED로 설정합니다.
                     */
                    toggleBottomSheet(view.fragment_map_bottom_sheet_view)
                }
            }

            /**
             * 친구 추가 버튼 리스너 설정.
             */
            with(friends_list_add_button) {
                setOnClickListener {
                    // TODO: 연결 추가 과정 구현
                    viewModel.requestNewConnection("1234", 1800000)
                }
            }

        }
    }

    private fun handleFailure(failure: Failure?) {
        failure?.let {
            notify(it::class.java.name)
        }
    }

    private fun toggleBottomSheet(sheet: View) {
        BottomSheetBehavior.from(sheet).apply {
            state = when (state) {
                BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                else -> BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun openBottomSheet(sheet: View) {
        BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun closeBottomSheet(sheet: View) {
        BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_COLLAPSED
    }


    /**
     * 친구 목록중 하나가 클릭되었을 때에 반응합니다.
     */
    override fun onFriendClicked(connection: LocationSupportConnection) {
        val marker = viewModel.markers.find {
            (it.tag as String) == connection.person.address
        } ?: return

        map?.apply {
            moveCamera(CameraUpdateFactory.newLatLng(marker.position))
            animateCamera(CameraUpdateFactory.zoomTo(15.0f))

            marker.showInfoWindow()

            closeBottomSheet(fragment_map_bottom_sheet_view)
        }
    }

    override fun onFriendCallClicked() {
        Notify(context!!).short("call clicked")
    }

    override fun onFriendRequestUpdateClicked() {
        Notify(context!!).short("update clicked")
    }

    private fun showNoFriends() {
        friends_list_no_one_textview.visibility = View.VISIBLE
        friends_list_recyclerview.visibility = View.GONE
    }

    private fun showConnections() {
        friends_list_no_one_textview.visibility = View.GONE
        friends_list_recyclerview.visibility = View.VISIBLE
    }






}
