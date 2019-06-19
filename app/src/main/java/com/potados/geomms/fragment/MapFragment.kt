package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
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
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.adapter.FriendsRecyclerViewAdapter
import com.potados.geomms.dummy.DummyContent
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /**
         * 레이아웃 씌워주기.
         */
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        /**
         * 사용할 객체 레퍼런스 가져와놓기.
         *
         * kotlin의 synthetic 기능을 이용해 map_view 인스턴스에 바로 접근하면
         * 널참조가 일어나는 문제를 목격하여 findViewById로 접근함.
         *
         * 만약 가져온 인스턴스가 널이라면 로그 출력하고 반환함.
         */
        val map: MapView = view.findViewById(R.id.map_view) ?: let {
            Log.e("MapFragment:onCreateView()",  "R.id.map_view is null.")
            return view
        }
        val friendsListRecyclerView: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.friends_list_recyclerview) ?: let {
            Log.e("MapFragment:onCreateView()",  "R.id.friends_list_recyclerview is null.")
            return view
        }
        val friendsListRoot: ConstraintLayout = view.findViewById(R.id.friends_list_root_layout) ?: let {
            Log.e("MapFragment:onCreateView()",  "R.id.friends_list_root_layout is null.")
            return view
        }
        val bottomSheet: FrameLayout = view.findViewById(R.id.fragment_map_bottom_sheet_view) ?: let {
            Log.e("MapFragment:onCreateView()",  "R.id.fragment_map_bottom_sheet_view is null.")
            return view
        }

        /**
         * 지도 설정.
         */
        with(map) {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment) /* 준비 끝나면 onMapReady 호출됨. */
        }

        /**
         * 친구 목록 리사이클러뷰 설정.
         */
        with(friendsListRecyclerView) {
            /**
             * 아이템 사이에 선을 그어줍니다.
             */
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    activity,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )

            /**
             * 어댑터 설정해줍니다.
             */
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = FriendsRecyclerViewAdapter(DummyContent.ITEMS, null)

            /**
             * 스크롤 이벤트가 리사이클러뷰에게 도착할 수 있게 해줍니다.
             */
            ViewCompat.setNestedScrollingEnabled(this, true)
        }

        /**
         * 친구 목록 레이아웃에 대한 클릭 리스너 설정.
         * Bottom Sheet 위쪽을 누르면 토글되도록 해줌.
         */
        with(friendsListRoot) {
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
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = when (state) {
                        BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                        else -> BottomSheetBehavior.STATE_EXPANDED
                    }
                }

            }
        }

        return view
    }

    override fun onMapReady(map: GoogleMap?) {
        val seoul = LatLng(37.56, 126.97)

        Log.d("MapFragment: onMapReady", "map is ready!")

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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }
}
