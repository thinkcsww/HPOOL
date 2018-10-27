package com.applory.hpool.Controllers

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.applory.hpool.Adapters.ListAdapter
import com.applory.hpool.Models.HPOOLRecord
import com.applory.hpool.Models.Message
import com.applory.hpool.Models.NotificationMessage
import com.applory.hpool.R
import com.applory.hpool.R.drawable.document
import com.applory.hpool.Utilities.EXTRA_REQUEST_INFO
import com.applory.hpool.Utilities.SharedPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_room.*
import okhttp3.*
import java.io.IOException
import java.sql.Timestamp

class RoomActivity : AppCompatActivity() {


    lateinit var adapter: ListAdapter
    var messages =  ArrayList<Message>()
    var tokens = ArrayList<String>()
    lateinit var prefs: SharedPrefs

    lateinit var roomId: String
    var destination: String? = null
    var departure: String? = null
    var number: String? = null
    var date: String? = null
    var time: String? = null

    val userId = FirebaseAuth.getInstance().currentUser!!.uid

    val TAG = RoomActivity::class.java.simpleName
    val requestDB = FirebaseFirestore.getInstance()
    val requestReference = requestDB.collection("Request")

    val messageDB = FirebaseFirestore.getInstance()
    val messageReference = messageDB.collection("Request")

    val recordDB = FirebaseFirestore.getInstance().collection("Record")
    val tokenDB = FirebaseFirestore.getInstance().collection("Request")
    lateinit var nickname: String

    var token: String? = null
    var retrievedTokens: List<Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        updateUi()

        prefs = SharedPrefs(this@RoomActivity)
        nickname = prefs.nickname

        adapter = ListAdapter(this@RoomActivity, messages)
        listView.adapter = adapter

        retrieveMessage()


        getToken()

        saveTokenToDB()


        retrieveTokens()


        sendButton.setOnClickListener {


            if(!TextUtils.isEmpty(contentEditText.text.toString())) {
                val content = contentEditText.text.toString()
                val newMessage = Message(nickname, content, userId, "empty")
                val newMessageMap: Map<String, String> = mapOf(
                        "content" to newMessage.content,
                        "messageId" to newMessage.messageId,
                        "name" to newMessage.name,
                        "userId" to userId
                )
                sendMessagesToDB(newMessageMap)
                sendGcm()
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

    private fun retrieveTokens() {
        messageReference.document(roomId).collection("Token").addSnapshotListener { tokens, exception ->
            if (exception != null) {
                Log.e(TAG + "recieve token error:", exception.localizedMessage)
                return@addSnapshotListener
            }
            this.tokens.clear()

            if(tokens != null) {
                for (token in tokens) {
//                    Log.d(TAG + "value", token.data.values.toString())
                    retrievedTokens = token.data.values.toList()
//                    Log.d(TAG + "value", retrievedTokens!!.toList().get(1).toString())
                }
            }


        }
    }

    private fun sendMessagesToDB(newMessage: Map<String, String>) {
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
    private fun saveTokenToDB() {
        if (roomId == userId) {
            messageDB.collection("Request")
                    .document(roomId)
                    .collection("Token")
                    .document("Token")
                    .set(mapOf(userId to token))
        } else {
            messageDB.collection("Request")
                    .document(roomId)
                    .collection("Token")
                    .document("Token")
                    .update(userId, token)
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
                        departure = roomInfo.data!!["departure"].toString()
                        destination = roomInfo.data!!["destination"].toString()
                        number = roomInfo.data!!["number"].toString()
                        date = roomInfo.data!!["date"].toString()
                        time = roomInfo.data!!["time"].toString()


                        placeTextView.text = "${departure} - ${destination}"
                        numberTextView.text = "${number}"
                    } catch (e: KotlinNullPointerException) {
                        if (roomId != userId) {
                            Toast.makeText(this, "카풀 모집이 취소되었습니다.", Toast.LENGTH_LONG).show()
                        }
                        prefs.isJoined = false
                        finish()
                        return@EventListener
                    }


                }
            })

        }
    }



    private fun quitRoom() {

        val noticeAlertDialog = AlertDialog.Builder(this@RoomActivity)
        val noticeDialogView = layoutInflater.inflate(R.layout.layout_getoffroom, null)
        noticeAlertDialog.setView(noticeDialogView)
        val dialog = noticeAlertDialog.create()
        val outButton : Button = noticeDialogView.findViewById(R.id.outButton)
        val cancleButton : Button = noticeDialogView.findViewById(R.id.cancleButton)
        val completeButton : Button = noticeDialogView.findViewById(R.id.completeButton)
        val orTextView: TextView = noticeDialogView.findViewById(R.id.orTextView)

        if (userId == roomId) {
            completeButton.visibility = View.VISIBLE
            orTextView.visibility = View.VISIBLE
            completeButton.setOnClickListener {
                masterLeaveRoomForComplete()
                dialog.dismiss()
            }
        }

        outButton.setOnClickListener {

            dialog.dismiss()
            showProgressbar(progressBar)

            //방장이 방을 나갈때
            if (userId == roomId) {
                masterLeaveRoom()
            //카풀 참여자가 방을 나갈때
            } else {
                participantLeaveRoom()
            }

        }

        cancleButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun writeRecord() {

        val timestamp = Timestamp(System.currentTimeMillis()).toString()
        val hpoolRecord = HPOOLRecord(roomId, departure!!, destination!!, date!!, time!!, number!!)
        recordDB.document(timestamp).set(hpoolRecord).addOnSuccessListener {
            Toast.makeText(this@RoomActivity, "카풀이 정상 종료되었습니다.", Toast.LENGTH_LONG).show()
            hideProgressbar(progressBar)
            return@addOnSuccessListener
        }.addOnFailureListener { exception ->
            Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            Log.d(TAG + ": Room Quit Error : ", exception.localizedMessage)
        }
    }

    /*
    ** Fun -> When parcitipant leave the room
     */
    private fun participantLeaveRoom() {
        requestReference.document(roomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                resetState()
                val number = task.result.data!!["number"].toString().toInt()
                val newNumber = number - 1

                tokenDB.document(roomId).collection("Token").document("Token").update(userId,FieldValue.delete()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }.addOnFailureListener { exception ->
                    Log.d(TAG + "delete token error : ", exception.localizedMessage)
                }

                requestDB.collection("Request").document(roomId).update("number", newNumber).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@RoomActivity, "카풀 모집이 취소되었습니다.", Toast.LENGTH_LONG).show()
                        hideProgressbar(progressBar)
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                    Log.d(TAG + ": Room Quit Error : ", exception.localizedMessage)

                }
            }
        }
    }

    /*
    ** Fun -> When master leave the room
     */
    private fun masterLeaveRoom() {
        var flag = true
        for (message in messages) {
            requestDB.collection("Request").document(roomId).collection("Message").document(message.messageId).delete()
                    .addOnSuccessListener {
                        flag = true
                    }.addOnFailureListener {
                        flag = false
                        Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }

        }
        if (flag) {
            requestDB.collection("Request").document(roomId).delete().addOnSuccessListener {
                Toast.makeText(this@RoomActivity, "카풀 모집이 취소되었습니다.", Toast.LENGTH_LONG).show()
                resetState()
                hideProgressbar(progressBar)
                return@addOnSuccessListener
            }.addOnFailureListener { e ->
                Log.d(TAG + "Master room delete:", e.localizedMessage)
                Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                return@addOnFailureListener
            }

            requestDB.collection("Request")
                    .document(roomId)
                    .collection("Token")
                    .document("Token")
                    .delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG + "token delete :", "success")
                        }


                    }.addOnFailureListener { exception ->
                        Log.d(TAG + "token delete error : ", exception.localizedMessage)
                    }

        }
    }
    private fun masterLeaveRoomForComplete() {
        var flag = true
        for (message in messages) {
            requestDB.collection("Request").document(roomId).collection("Message").document(message.messageId).delete()
                    .addOnSuccessListener {
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
                writeRecord()
            }.addOnFailureListener { e ->
                Log.d(TAG + "Master room delete:", e.localizedMessage)
                Toast.makeText(this@RoomActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                return@addOnFailureListener
            }
            requestDB.collection("Request")
                    .document(roomId)
                    .collection("Token")
                    .document("Token")
                    .delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG + "token delete :", "success")
                        }


                    }.addOnFailureListener { exception ->
                        Log.d(TAG + "token delete error : ", exception.localizedMessage)
                    }
        }
    }

    /*
    ** Fun -> getToken for messaging
     */
    private fun getToken() {
        token = FirebaseInstanceId.getInstance().getToken()
    }

    fun sendGcm() {
        val gson = Gson()
        val notificationMessage = NotificationMessage()

        for (i in 0..retrievedTokens!!.size - 1) {
            notificationMessage.to = retrievedTokens!!.get(i).toString()
            notificationMessage.notification.title = prefs.nickname
            notificationMessage.notification.text = contentEditText.text.toString()
//            notificationMessage.data.title = prefs.nickname
//            notificationMessage.data.text = contentEditText.text.toString()

            val requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf8"),
                    gson.toJson(notificationMessage)
            )

            val request = Request.Builder()
                    .header("Content-Type", "application/json")
                    .addHeader("Authorization", "key=AIzaSyAOUEKh3P5TvRUDXsqYZbOTJdEM77eXr-w")
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(requestBody)
                    .build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {

                }

                override fun onResponse(call: Call?, response: Response?) {

                }

            })
        }
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
