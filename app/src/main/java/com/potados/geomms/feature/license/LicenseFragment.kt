package com.potados.geomms.feature.license

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.setSupportActionBar
import com.potados.geomms.common.extension.setTitle
import com.potados.geomms.model.License
import com.potados.geomms.util.RawReader
import kotlinx.android.synthetic.main.license_fragment.*
import kotlinx.android.synthetic.main.license_fragment.view.*

class LicenseFragment : BaseFragment() {
    private lateinit var adapter: LicenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (context == null) return

        adapter = LicenseAdapter(context!!)

        val result = mutableListOf<License>()

        val metadata = RawReader(context!!, R.raw.third_party_license_metadata).toStringList().map { it.substringAfter(' ') }
        val license = RawReader(context!!, R.raw.third_party_licenses).toStringList()

        if (metadata.size != license.size) return

        for (i in metadata.indices) {
            result.add(License(metadata[i], license[i]))
        }

        adapter.data = result
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.license_fragment, container, false).apply {
            licenses.adapter = adapter
            setSupportActionBar(toolbar, title = false, upButton = true)
        }
    }
}