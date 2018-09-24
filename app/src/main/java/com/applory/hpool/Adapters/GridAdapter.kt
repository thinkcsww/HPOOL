package com.applory.hpool.Adapters

import android.content.Context
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.applory.hpool.Models.HPOOLRequest
import com.applory.hpool.R

class GridAdapter(val context: Context, val hpoolRequests: ArrayList<HPOOLRequest>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val gridView: View
        val holder: ViewHolder
        var resourceId: Int? = null

        if (convertView == null) {
            holder = ViewHolder()
            gridView = from(context).inflate(R.layout.gridview_custom, null)
            holder.depToDes = gridView.findViewById(R.id.depToDesTextView)
            holder.time = gridView.findViewById(R.id.dateTextView)
            holder.pickupLocation = gridView.findViewById(R.id.pickupLocationTextView)
            holder.num = gridView.findViewById(R.id.numTextView)
            holder.numImage = gridView.findViewById(R.id.numImageView)
            gridView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            gridView = convertView
        }

        val hpoolRequest = hpoolRequests[position]
        holder.pickupLocation?.text = hpoolRequest.pickUpLocation
        holder.depToDes?.text = "${hpoolRequest.departure} - ${hpoolRequest.destination}"
        holder.time?.text = hpoolRequest.time
        holder.num?.text = "${hpoolRequest.number.toString()}/4"

        when (hpoolRequest.number) {

            1 -> resourceId = context.resources.getIdentifier("num1", "drawable", context.packageName)
            2 -> resourceId = context.resources.getIdentifier("num2", "drawable", context.packageName)
            3 -> resourceId = context.resources.getIdentifier("num3", "drawable", context.packageName)
            4 -> resourceId = context.resources.getIdentifier("num4", "drawable", context.packageName)

        }

        holder.numImage?.setImageResource(resourceId!!)

        return gridView
    }

    override fun getItem(position: Int): Any {
        return hpoolRequests[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return hpoolRequests.count()
    }

    private class ViewHolder {
        var depToDes: TextView? = null
        var pickupLocation: TextView? = null
        var time: TextView? = null
        var numImage: ImageView? = null
        var num: TextView? = null
    }
}