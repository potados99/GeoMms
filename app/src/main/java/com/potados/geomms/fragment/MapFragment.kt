package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.adapter.FriendsRecyclerViewAdapter
import com.potados.geomms.core.extension.observe
import com.potados.geomms.core.extension.viewModel
import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.data.entity.LocationSupportPerson
import com.potados.geomms.protocol.LocationSupportManager
import com.potados.geomms.protocol.LocationSupportProtocol
import com.potados.geomms.core.util.Notify
import com.potados.geomms.viewmodel.MapViewModel
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map_friends_list.view.*
import org.koin.android.ext.android.inject

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : Fragment(),
    OnMapReadyCallback,
    FriendsRecyclerViewAdapter.FriendClickListener {

    /**
     * LocationSupport 매니저
     */
    private val locationSupportManager: LocationSupportManager by inject()


    /**
     * 뷰모델
     */
    private lateinit var viewModel: MapViewModel

    private val handler = Handler()

    private val onEverySeconds = object: Runnable {
        override fun run() {
            locationSupportManager.onEverySecondUpdate()
            handler.postDelayed(this, 1000)
        }
    }


    /**
     * 프래그먼트가 액티비티에 붙을 때에 실행됩니다.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("MapFragment: onAttach", "attached.")
    }

    /**
     * 프래그먼트가 생성될 때에 실행됩니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = viewModel {
            // observe()
        }

        Log.i("MapFragment: onCreate", "created.")
    }

    /**
     * 프래그먼트에 뷰가 붙을 무렵에 실행됩니다.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).also {
            bindUi(it)
            setUpUi(it)

            getMapReady(it.map_view, savedInstanceState)

            Log.i("MapFragment: onCreateView", "view created.")
        }
    }

    /**
     * 프래그먼트가 재개될 때에 실행됩니다.
     */
    override fun onResume() {
        super.onResume()

        locationSupportManager.requestNewConnection(LocationSupportPerson("지은이", "1234"))
        locationSupportManager.requestNewConnection(LocationSupportPerson("하늘이", "4321"))

        Log.i("MapFragment: onResume", "resumed.")
    }

    /**
     * 프래그먼트가 멈출 때에 실행됩니다.
     */
    override fun onPause() {
        super.onPause()
        Log.i("MapFragment: onPause", "paused.")
    }

    /**
     * 프래그먼트가 종료될 무렵에 실행됩니다.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.i("MapFragment: onDestroy", "destroyed.")
    }

    /**
     * 프래그먼트가 액티비티에서 떨어질 때에 실행됩니다.
     */
    override fun onDetach() {
        super.onDetach()
        Log.i("MapFragment: onDetach", "detached.")
    }

    /**
     * 지도가 준비되었을 때에 실행됩니다.
     */
    override fun onMapReady(map: GoogleMap?) {
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

    /**
     * 지도를 준비시킵니다.
     * 준비 끝나면 onMapReady 호출됩니다.
     */
    private fun getMapReady(map: MapView, savedInstanceState: Bundle?) {

        /**
         * 지도 설정.
         */
        with(map) {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment) /* 준비 끝나면 onMapReady 호출됨. */
        }

    }

    /**
     * UI를 뷰모델과 이어줍니다.
     */
    private fun bindUi(view: View) {

        locationSupportManager.getConnections().observe(this, object: Observer<List<LocationSupportConnection>> {
            override fun onChanged(t: List<LocationSupportConnection>?) {
                if (t == null) return

                view.friends_list_recyclerview.adapter = FriendsRecyclerViewAdapter(t, this@MapFragment)
            }
        })

        this@MapFragment.handler.post(onEverySeconds)


        /*
        with(view.friends_list_recyclerview) {
            adapter = FriendsRecyclerViewAdapter(DummyContent.getLocationSupportConnectionDummy(), this@MapFragment)
        }
        */
    }

    /**
     * UI를 설정해줍니다.
     */
    private fun setUpUi(view: View) {
        /**
         * 친구 목록 리사이클러뷰 외관 설정.
         */
        with(view.friends_list_recyclerview) {
            /**
             * 아이템 사이에 선을 그어줍니다.
             */
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

            layoutManager = LinearLayoutManager(context)

            /**
             * 스크롤 이벤트가 리사이클러뷰에게 도착할 수 있게 해줍니다.
             */
            ViewCompat.setNestedScrollingEnabled(this, true)
        }

        /**
         * 친구 목록 레이아웃에 대한 클릭 리스너 설정.
         * Bottom Sheet 위쪽을 누르면 토글되도록 해줍니다.
         */
        with(view.friends_list_root_layout) {
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
    }

    /**
     * Bottom sheet가 완전히 열려있으면 닫고,
     * 그렇지 않으면 열어줍니다.
     */
    private fun toggleBottomSheet(sheet: View) {
        BottomSheetBehavior.from(sheet).apply {
            state = when (state) {
                BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                else -> BottomSheetBehavior.STATE_EXPANDED
            }
        }

        locationSupportManager.onPacketReceived(LocationSupportProtocol.createDataPacket(1, 127.12345, 37.12345), "1234")
        locationSupportManager.onPacketReceived(LocationSupportProtocol.createDataPacket(2, 128.12345, 37.12345), "4321")

    }

    /**
     * Bottom sheet를 열어줍니다.
     */
    private fun openBottomSheet(sheet: View) {
        BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * Bottom sheet를 닫습니다.
     */
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
