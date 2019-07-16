package com.potados.geomms.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.adapter.FriendsRecyclerViewAdapter
import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.dummy.DummyContent
import com.potados.geomms.util.Notify
import com.potados.geomms.viewmodel.MapViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map_friends_list.view.*

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : Fragment(),
    OnMapReadyCallback,
    FriendsRecyclerViewAdapter.FriendClickListener {

    /**
     * 뷰모델
     */
    private lateinit var viewModel: MapViewModel

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

        viewModel = ViewModelProviders
            .of(activity ?: throw RuntimeException("activity is null."))
            .get(MapViewModel::class.java)

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
        with(view.friends_list_recyclerview) {
            adapter = FriendsRecyclerViewAdapter(DummyContent.ITEMS, this@MapFragment)
        }
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
                BottomSheetBehavior.from(view.fragment_map_bottom_sheet_view).apply {
                    state = when (state) {
                        BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                        else -> BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
        }
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
