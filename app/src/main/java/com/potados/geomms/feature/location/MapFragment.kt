package com.potados.geomms.feature.location

import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.extension.*
import com.potados.geomms.core.platform.NavigationBasedFragment
import com.potados.geomms.core.util.DateTime
import com.potados.geomms.core.util.Duration
import com.potados.geomms.core.util.Notify
import com.potados.geomms.core.util.Popup
import com.potados.geomms.feature.common.SettingsActivity
import com.potados.geomms.feature.common.SettingsFragment
import com.potados.geomms.feature.common.SmsReceiver
import com.potados.geomms.feature.location.data.LocationSupportConnection
import com.potados.geomms.feature.location.data.LocationSupportRequest
import kotlinx.android.synthetic.main.bottom_sheet_content.*
import kotlinx.android.synthetic.main.bottom_sheet_content.friends_list_recyclerview
import kotlinx.android.synthetic.main.bottom_sheet_content.no_connection_textview
import kotlinx.android.synthetic.main.bottom_sheet_content.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map.view.map_view
import kotlinx.android.synthetic.main.fragment_map_bottom_sheet.view.*

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : NavigationBasedFragment(),
    OnMapReadyCallback,
    LocationSupportConnectionRecyclerViewAdapter.FriendClickListener
{

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
        map_view?.onResume()
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
                    with(fragment_map_bottom_sheet_view) {
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

    private fun renderConnections(connections: List<LocationSupportConnection>?) {
        if (connections.isNullOrEmpty()) {
            no_connection_textview.visibility = View.VISIBLE
            friends_list_recyclerview.visibility = View.GONE
        }
        else {
            no_connection_textview.visibility = View.GONE
            friends_list_recyclerview.visibility = View.VISIBLE

            adapter.collection = connections.orEmpty()

            viewModel.markers.map { it.remove() }
            viewModel.markers.clear()

            connections.forEach { connection ->

                connection.lastReceivedPacket?.let { packet ->
                    map?.addMarker(
                        MarkerOptions().apply {
                            position(LatLng(packet.latitude, packet.longitude))
                            title(connection.person.name)
                            snippet("현재 위치")
                        }
                    )?.let {
                        viewModel.markers.add(it.apply { tag = connection.person.address })
                    }
                }
            }
        }
    }

    private fun handleIncomingRequest(requests: List<LocationSupportRequest>?) {
        if (requests.isNullOrEmpty()) return

        val req = requests.first()

        notifyWithAction("Have new request from ${req.person.name} for ${Duration(req.lifeSpan).toShortenString()}.", "Accept") {
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
            with(bottom_sheet_root_layout) {
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
                    view.fragment_map_bottom_sheet_view.toggleSheet()
                }
            }


            /**
             * 바텀 시트
             */
            with(fragment_map_bottom_sheet_view) {
                collapseSheet()

                bottomSheetBehavior().setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                view.friends_content_grip.alpha = 0f
                            }
                            else -> {
                                view.friends_content_grip.alpha = 1f
                            }
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        val param = no_connection_textview.layoutParams as ConstraintLayout.LayoutParams
                        param.verticalBias = slideOffset / 2 + 0.05f
                        no_connection_textview.layoutParams = param
                    }
                })

                bottom_sheet_root_layout.setOnClickListener {
                    toggleSheet()
                }
            }

            /**
             * 연결 추가 버튼
             */
            with(fragment_map_add_button) {
                setOnClickListener {
                    val input = EditText(context).apply {
                        inputType = InputType.TYPE_CLASS_PHONE
                    }

                    Popup(context)
                        .withTitle("새 연결!")
                        .withView(input)
                        .withPositiveButton("오케이") { _, _ ->
                            val address = input.text.toString()
                            val lifeSpan = PreferenceManager
                                .getDefaultSharedPreferences(context)
                                .getString("location_connection_lifespan", "1600000")
                                ?.toLong() ?: 1700000

                            viewModel.requestNewConnection(address, lifeSpan)
                            Notify(context).long("${viewModel.getName(address)}에다가 ${Duration(lifeSpan).toShortenString()}짜리 연결 요청 날립니다")
                        }
                        .show()
                }
            }
        }
    }

    private fun handleFailure(failure: Failure?) {
        failure?.let {
            notify(it::class.java.name)
        }
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

            fragment_map_bottom_sheet_view.collapseSheet()
        }
    }

    override fun onFriendCallClicked() {
        Notify(context!!).short("call clicked")
    }

    override fun onFriendRequestUpdateClicked() {
        Notify(context!!).short("update clicked")
    }


}
