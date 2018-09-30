package com.applory.hpool.Controllers

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.applory.hpool.Adapters.ListAdapter
import com.applory.hpool.Controllers.App.Companion.prefs
import com.applory.hpool.Models.HPOOLRequest
import com.applory.hpool.Models.Message
import com.applory.hpool.R
import com.applory.hpool.Utilities.EXTRA_REQUEST_INFO
import com.applory.hpool.Utilities.SharedPrefs
import kotlinx.android.synthetic.main.activity_room.*

class RoomActivity : AppCompatActivity() {

    lateinit var adapter: ListAdapter
    var messages =  ArrayList<Message>()

    lateinit var request: HPOOLRequest
    lateinit var prefs: SharedPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        //Request Activity에서 온 정보 받기
        if (intent.getParcelableArrayExtra(EXTRA_REQUEST_INFO) != null) {
            request = intent.getParcelableExtra(EXTRA_REQUEST_INFO)
            placeTextView.text = "${request.departure} - ${request.destination}"
        }


        prefs = SharedPrefs(this@RoomActivity)



        adapter = ListAdapter(this@RoomActivity, messages)
        val newMessage = Message("최창원", "야이 장원이들아.")
        messages.add(newMessage)
        messages.add(newMessage)
        listView.adapter = adapter

        quitButton.setOnClickListener {
            quitRoom()
        }
    }

    private fun quitRoom() {
        val noticeAlertDialog = AlertDialog.Builder(this@RoomActivity)
        val noticeDialogView = layoutInflater.inflate(R.layout.layout_getoffroom, null)
        noticeAlertDialog.setView(noticeDialogView)
        val dialog = noticeAlertDialog.create()
        val outButton : Button = noticeDialogView.findViewById(R.id.outButton)
        val cancleButton : Button = noticeDialogView.findViewById(R.id.cancleButton)

        outButton.setOnClickListener {
            prefs.isJoined = false
            dialog.dismiss()
            finish()
        }

        cancleButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onBackPressed() {
        quitRoom()
    }
}
