
package com.potados.geomms.feature.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber
import kotlinx.android.synthetic.main.contact_list_item.view.*

class PhoneNumberAdapter(
    private val onNumberClick: (Contact, Int) -> Unit
) : BaseAdapter<PhoneNumber>() {

    lateinit var contact: Contact

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_number_list_item, parent, false)
        return BaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val number = getItem(position) ?: return
        val view = holder.containerView

        // Setting this in onCreateViewHolder causes a crash sometimes. [contact] returns the
        // contact from a different row, I'm not sure why
        view.setOnClickListener { onNumberClick(contact, position) }

        view.address.text = number.address
        view.type.text = number.type
    }

}