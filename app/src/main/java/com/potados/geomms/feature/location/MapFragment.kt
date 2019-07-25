package com.potados.geomms.feature.location

import android.content.IntentFilter
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.core.exception.Failure
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

        Log.i("MapFragment: onCreate", "created.")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeView(view)
        getMapReady(view.map_view, savedInstanceState)
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map

        val seoul = LatLng(37.56, 126.97)

        map?.addMarker(MarkerOptions().apply {
            position(seoul)
            title("Seoul")
            snippet("Capital of korea")
        })

        activity?.let {
            MapsInitializer.initialize(it)
        }

        map?.moveCamera(CameraUpdateFactory.newLatLng(seoul))
        map?.animateCamera(CameraUpdateFactory.zoomTo(10.0f))

        Log.d("MapFragment: onMapReady", "map is ready!")
    }

    private fun renderConnections(connections: List<LocationSupportConnection>?) {
        adapter.collection = connections.orEmpty()
    }

    private fun handleIncomingRequest(requests: List<LocationSupportRequest>?) {
        if (requests.isNullOrEmpty()) return

        val req = requests.first()

        notifyWithAction("Have new request from ${req.person.address} for ${Duration(req.lifeSpan).toShortenString()}.", "Accept") {
            viewModel.acceptRequest(req)
        }
    }

    private fun initializeView(view: View) {
        with(view) {

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

            friends_list_add_button.setOnClickListener {
                // TODO: 연결 추가 과정 구현
                viewModel.requestNewConnection("1234", 1800000)
            }

        }
    }

    private fun getMapReady(map: MapView, savedInstanceState: Bundle?) {
        with(map) {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment) /* 준비 끝나면 onMapReady 호출됨. */
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
    override fun onFriendClicked() {
        Notify(context!!).short("clicked")
    }

    override fun onFriendCallClicked() {
        Notify(context!!).short("call clicked")
    }

    override fun onFriendRequestUpdateClicked() {
        Notify(context!!).short("update clicked")
    }







}
