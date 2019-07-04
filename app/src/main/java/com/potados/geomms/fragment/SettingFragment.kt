package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.adapter.SettingRecyclerViewAdapter

import com.potados.geomms.dummy.DummyContent
import com.potados.geomms.dummy.DummyContent.DummyItem

/**
 * 기타 설정들이 들어가는 프래그먼트입니다.
 */
class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = SettingRecyclerViewAdapter(DummyContent.ITEMS)
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
    }

}
