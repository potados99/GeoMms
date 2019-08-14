package com.potados.geomms.feature.conversations

import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter

import com.potados.geomms.model.Conversation
import com.potados.geomms.util.DateTime
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

import org.koin.core.KoinComponent
import org.koin.core.inject

class ConversationsAdapter : RealmRecyclerViewAdapter<Conversation, BaseViewHolder>(null, true),
    KoinComponent
{
    private val dateFormatter: DateFormatter by inject()
    private val navigator: Navigator by inject()

    override fun updateData(data: OrderedRealmCollection<Conversation>?) {
        if (getData() === data) return

        super.updateData(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.conversation_item, parent, false)

        if (viewType == VIEW_TYPE_READ) {
            with(view) {

            }

        }

        return BaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
       // getItem(position)?.let(holder::bind)
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)?.read) {
            true -> VIEW_TYPE_READ
            else -> VIEW_TYPE_UNREAD
        }

    interface ConversationClickListener {
        fun onConversationClicked(conversation: Conversation)
    }

    companion object {
        private val VIEW_TYPE_READ = 1
        private val VIEW_TYPE_UNREAD = 0
    }
}
