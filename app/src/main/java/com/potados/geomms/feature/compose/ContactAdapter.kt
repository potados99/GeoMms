package com.potados.geomms.feature.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.model.Contact
import kotlinx.android.synthetic.main.contact_list_item.view.*
import kotlin.math.min

class ContactAdapter : BaseAdapter<Contact>() {

    private val numbersViewPool = RecyclerView.RecycledViewPool()

    var onContactClick: (Contact) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.contact_list_item, parent, false)

        view.addresses.setRecycledViewPool(numbersViewPool)

        return BaseViewHolder(view).apply {
            view.primary.setOnClickListener {
                val contact = getItem(adapterPosition) ?: return@setOnClickListener
                onContactClick(copyContact(contact, 0))
            }

            view.addresses.adapter = PhoneNumberAdapter { contact, index ->
                onContactClick(copyContact(contact, index + 1))
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val contact = getItem(position) ?: return
        val view = holder.containerView

        view.avatar.setContact(contact)
        view.name.text = contact.name
        view.name.setVisible(view.name.text.isNotEmpty())
        view.address.text = contact.numbers.firstOrNull()?.address ?: ""
        view.type.text = contact.numbers.firstOrNull()?.type ?: ""

        val adapter = view.addresses.adapter as PhoneNumberAdapter
        adapter.contact = contact
        adapter.data = contact.numbers.drop(min(contact.numbers.size, 1))
    }

    /**
     * Creates a copy of the contact with only one phone number, so that the chips
     * sheetView can still display the packageName/photo, and not get confused about which phone number to use
     */
    private fun copyContact(contact: Contact, numberIndex: Int) = Contact().apply {
        lookupKey = contact.lookupKey
        name = contact.name
        numbers.add(contact.numbers[numberIndex])
    }

    override fun areItemsTheSame(old: Contact, new: Contact): Boolean {
        return old.lookupKey == new.lookupKey
    }
}