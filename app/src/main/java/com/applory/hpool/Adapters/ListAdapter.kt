package com.applory.hpool.Adapters

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.Log
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.applory.hpool.Models.Message
import com.applory.hpool.R
import com.applory.hpool.Utilities.SharedPrefs
import com.google.firebase.auth.FirebaseAuth

class ListAdapter(val context: Context, val messages: ArrayList<Message>): BaseAdapter() {
    val prefs = SharedPrefs(context)
    val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val listView: View
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            listView = from(context).inflate(R.layout.chat_custom_listview, null)
            holder.name = listView.findViewById(R.id.nameTextView)
            holder.content = listView.findViewById(R.id.contentTextView)
            holder.profileImage = listView.findViewById(R.id.profileImageView)
            listView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            listView = convertView
        }

        val message = messages[position]


        holder.name?.text = message.name
        holder.content?.text = message.content

        if (prefs.roomId == message.userId) {
            val resourceId = context.resources.getIdentifier("crown", "drawable", context.packageName)
            Log.d("roomId in listview: ", resourceId.toString())
            holder.profileImage?.setImageResource(resourceId)
        } else {
            val resourceId = context.resources.getIdentifier("profile_default", "drawable", context.packageName)
            holder.profileImage?.setImageResource(resourceId)
        }


        return listView
    }

    override fun getItem(position: Int): Any {
        return messages[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return messages.count()
    }

    private class ViewHolder {
        var name: TextView? = null
        var content: TextView? = null
        var profileImage: ImageView? = null
    }
}