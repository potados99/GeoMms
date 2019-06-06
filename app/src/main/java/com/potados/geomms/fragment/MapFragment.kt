package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import kotlinx.android.synthetic.main.fragment_map.*


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


        MapsInitializer.initialize(activity)

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
