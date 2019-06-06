package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.adapter.FriendsRecyclerViewAdapter
import com.potados.geomms.dummy.DummyContent
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.friends_list.*
import kotlinx.android.synthetic.main.friends_list.view.*


class MapFragment : Fragment(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val map: MapView = view.findViewById(R.id.map_view)
        map.onCreate(savedInstanceState)
        map.getMapAsync(this)

        val friendsRecyclerView: RecyclerView = view.findViewById(R.id.friends_recyclerview)
        with(friendsRecyclerView) {
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = FriendsRecyclerViewAdapter(DummyContent.ITEMS, null)

            ViewCompat.setNestedScrollingEnabled(this, true)
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

    companion object {

    }
}
