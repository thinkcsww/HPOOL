package com.applory.hpool.Controllers

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.applory.hpool.Adapters.ListAdapter
import com.applory.hpool.Models.Message
import com.applory.hpool.R
import com.applory.hpool.Utilities.EXTRA_REQUEST_INFO
import com.applory.hpool.Utilities.SharedPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_room.*
import java.sql.Timestamp

class RoomActivity : AppCompatActivity() {


    lateinit var adapter: ListAdapter
    var messages =  ArrayList<Message>()
    lateinit var prefs: SharedPrefs

    lateinit var roomId: String
    val userId = FirebaseAuth.getInstance().currentUser!!.uid

    val TAG = RoomActivity::class.java.simpleName
    val requestDB = FirebaseFirestore.getInstance()
    val requestReference = requestDB.collection("Request")

    val messageDB = FirebaseFirestore.getInstance()
    val messageReference = messageDB.collection("Request")
    lateinit var nickname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)


        updateUi()

        prefs = SharedPrefs(this@RoomActivity)
        nickname = prefs.nickname

        adapter = ListAdapter(this@RoomActivity, messages)
        listView.adapter = adapter

        retrieveMessage()

        sendButton.setOnClickListener {

            if(!TextUtils.isEmpty(contentEditText.text.toString())) {
                val content = contentEditText.text.toString()
                val newMessage = Message(nickname, content, userId, "empty")
                sendMessagesToDB(newMessage)
                contentEditText.text.clear()
            }
        }

        quitButton.setOnClickListener {
            quitRoom()
        }
    }

    private fun retrieveMessage() {
        messageReference.document(roomId).collection("Message").addSnapshotListener { messages, exception ->
            if (exception != null) {
                Log.e(TAG + "receive message error :", exception.localizedMessage)
                return@addSnapshotListener
            }
            this.messages.clear()

            if (messages != null) {
                for (message in messages) {
                    Log.d(TAG  + "message : ", message.data.toString())
                    val name = message.data["name"].toString()
                    val content = message.data["content"].toString()
                    val id = message.data["userId"].toString()
                    val messageId = message.id
                    val newMessage = Message(name, content, id, messageId)
                    this.messages.add(newMessage)
                    adapter.notifyDataSetChanged()
                    listView.smoothScrollToPosition(adapter.count)
                }
            }
        }
    }

    private fun sendMessagesToDB(newMessage: Message) {
        val timestamp = Timestamp(System.currentTimeMillis())
        messageDB.collection("Request")
                .document(roomId)
                .collection("Message")
                .document(timestamp.time.toString())
                .set(newMessage)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this@RoomActivity, "메시지 전송이 실패하였습니다.", Toast.LENGTH_LONG).show()
                    Log.d(TAG +  "Sending message error: ", exception.localizedMessage)
                }
    }


    private fun updateUi() {

        //이전 액티비티, Request || List Activity에서 받은 Room_id를 키로 사용해서 디비에서 받아와 UI 업데이트

        if (intent.getStringExtra(EXTRA_REQUEST_INFO) != null) {
            roomId = intent.getStringExtra(EXTRA_REQUEST_INFO)
            Log.d(TAG, roomId)
            requestReference.document(roomId).addSnapshotListener(EventListener { roomInfo, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.d(TAG, firebaseFirestoreException.localizedMessage)
                    return@EventListener
                }
                if (roomInfo != null) {
                    Log.d(TAG + "Room", roomInfo.data.toString())
                    try {
                        val departure = roomInfo.data!!["departure"].toString()
                        val destination = roomInfo.data!!["destination"].toString()
                        val number = roomInfo.data!!["number"].toString()
                        placeTextView.text = "${departure} - ${destination}"
                        numberTextView.text = "${number}"
                    } catch (e: KotlinNullPointerException) {
                        Toast.makeText(this@RoomActivity, "카풀 모집이 취소되었습니다.", Toast.LENGTH_LONG).show()
                        prefs.isJoined = false
                        finish()
                        return@EventListener
                    }


                }
            })

        }
    }



    private fun quitRoom() {
        var flag = false
        val noticeAlertDialog = AlertDialog.Builder(this@RoomActivity)
        val noticeDialogView = layoutInflater.inflate(R.layout.layout_getoffroom, null)
        noticeAlertDialog.setView(noticeDialogView)
        val dialog = noticeAlertDialog.create()
        val outButton : Button = noticeDialogView.findViewById(R.id.outButton)
        val cancleButton : Button = noticeDialogView.findViewById(R.id.cancleButton)

        outButton.setOnClickListener {

            showProgressbar(progressBar)

            //방장이 방을 나갈때
            if (userId == roomId) {


                for (message in messages) {
                    requestDB.collection("Request").document(roomId).collection("Message").document(message.messageId).delete().addOnSuccessListener {
                        flag = true
                    }.addOnFailureListener {
                        flag = false
                        Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }

                }
                if (flag) {
                    requestDB.collection("Request").document(roomId).delete().addOnSuccessListener {
                        resetState()
                        hideProgressbar(progressBar)
                        finish()
                        return@addOnSuccessListener
                    }.addOnFailureListener { e ->
                        Log.d(TAG + "Master room delete:", e.localizedMessage)
                        Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }
                }

            //카풀 참여자가 방을 나갈때
            } else {
                requestReference.document(roomId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        resetState()
                        val number = task.result.data!!["number"].toString().toInt()
                        val newNumber = number - 1

                        requestDB.collection("Request").document(roomId).update("number", newNumber).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                hideProgressbar(progressBar)
                                dialog.dismiss()
                                finish()
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                            Log.d(TAG + ": Room Quit Error : ", exception.localizedMessage)

                        }
                    }
                }
            }

        }

        cancleButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun resetState() {

        //앱을 껐다가 켰을 때 Mainacitivity에서 바로 이동하기 위해서 SharedPref에 roomid를 저장함
        prefs.isJoined = false
        prefs.roomId = ""
    }

    override fun onBackPressed() {
        quitRoom()
    }

    fun showProgressbar(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
    }
    fun hideProgressbar(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }
}
