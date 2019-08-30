package com.potados.geomms.feature.license

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.model.License
import kotlinx.android.synthetic.main.license_list_item.view.*


class LicenseAdapter(private val context: Context) : BaseAdapter<License>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.license_list_item, parent, false)

        return BaseViewHolder(view)
    }
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.containerView) {
            package_name.text = item.packageName
            license_url.text = item.licenseUrl

            setOnClickListener {
                Intent(Intent.ACTION_VIEW).run {
                    data = Uri.parse(item.licenseUrl)
                    context.startActivity(this)
                }
            }
        }
    }
}