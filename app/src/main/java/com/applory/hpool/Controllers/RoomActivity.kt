package com.applory.hpool.Controllers

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.applory.hpool.Adapters.ListAdapter
import com.applory.hpool.Models.Message
import com.applory.hpool.R
import kotlinx.android.synthetic.main.activity_room.*

class RoomActivity : AppCompatActivity() {

    lateinit var adapter: ListAdapter
    var messages =  ArrayList<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        adapter = ListAdapter(this@RoomActivity, messages)
        val newMessage = Message("최창원", "수경아 보고 싶다.")
        messages.add(newMessage)
        messages.add(newMessage)
        listView.adapter = adapter

        quitButton.setOnClickListener {
            val noticeAlertDialog = AlertDialog.Builder(this@RoomActivity)
            val noticeDialogView = layoutInflater.inflate(R.layout.layout_getoffroom, null)
            noticeAlertDialog.setView(noticeDialogView)
            val dialog = noticeAlertDialog.create()
            val outButton : Button = noticeDialogView.findViewById(R.id.outButton)
            val cancleButton : Button = noticeDialogView.findViewById(R.id.cancleButton)

            outButton.setOnClickListener {
                finish()
            }

            cancleButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
