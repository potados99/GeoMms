package com.potados.geomms.feature.location.presentation

import android.os.Bundle
import android.text.InputType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.util.Duration
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import com.potados.geomms.databinding.MapFragmentBinding
import com.potados.geomms.feature.location.data.LSConnection
import kotlinx.android.synthetic.main.bottom_sheet_content.view.*
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.map_fragment.view.*
import kotlinx.android.synthetic.main.map_fragment.view.map_view
import kotlinx.android.synthetic.main.bottom_sheet.view.*

/**
 * 지도와 함께 연결된 친구 목록을 보여주는 프래그먼트입니다.
 */
class MapFragment : NavigationFragment(),
    OnMapReadyCallback,
    LocationSupportConnectionRecyclerViewAdapter.FriendClickListener
{

    override fun menuId(): Int = R.id.menu_item_navigation_map

    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewDataBinding: MapFragmentBinding

    private val adapter = LocationSupportConnectionRecyclerViewAdapter(this)
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapViewModel = getViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return MapFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = mapViewModel }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(root.toolbar) }
            .apply { initializeView(root, savedInstanceState) }
            .root

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


    private fun initializeView(view: View, savedInstanceState: Bundle?) {
        /**
         * 지도 설정
         */
        with(view.map_view) {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment) /* 준비 끝나면 onMapReady 호출됨. */
        }

        /**
         * 친구 목록 리사이클러뷰 외관 설정.
         */
        with(view.friends_list_recyclerview) {
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
        with(view.bottom_sheet_root_layout) {
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
        with(view.fragment_map_bottom_sheet_view) {
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
        with(view.fragment_map_add_button) {
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

                        mapViewModel.requestNewConnection(address, lifeSpan)
                        Notify(context).long("$address 에다가 ${Duration(lifeSpan).toShortenString()}짜리 연결 요청 날립니다")
                    }
                    .show()
            }
        }
    }

    /**
     * 친구 목록중 하나가 클릭되었을 때에 반응합니다.
     */
    override fun onFriendClicked(connection: LSConnection) {
        val marker = mapViewModel.markers.find {
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
