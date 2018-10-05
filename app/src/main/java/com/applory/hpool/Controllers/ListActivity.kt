package com.applory.hpool.Controllers

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.applory.hpool.Adapters.GridAdapter
import com.applory.hpool.Models.HPOOLRequest
import com.applory.hpool.R
import com.applory.hpool.Utilities.EXTRA_REQUEST_INFO
import com.applory.hpool.Utilities.SharedPrefs
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_list.*
import java.util.*

class ListActivity : AppCompatActivity() {

    val TAG = ListActivity::class.java.simpleName

    val calendar = Calendar.getInstance()
    var currentDate: String? = null
    var nextDate: String? = null

    lateinit var prefs: SharedPrefs

    lateinit var gridAdapter: GridAdapter
    var hpoolRequests =  ArrayList<HPOOLRequest>()

    val requestDB = FirebaseFirestore.getInstance()
    val requestReference = requestDB.collection("Request")

    //To finish this activity when logged out
    lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        prefs = SharedPrefs(this@ListActivity)


        gridAdapter = GridAdapter(this@ListActivity, hpoolRequests)

        gridView.adapter = gridAdapter

        broadcastReceiver = object : BroadcastReceiver() {

            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish_activity") {
                    finish()
                    // DO WHATEVER YOU WANT.
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish_activity"))

        floatingButton.setOnClickListener {
            val intent = Intent(this@ListActivity, RequestActivity::class.java)
            startActivity(intent)
        }

        gridView.setOnItemClickListener { parent, view, position, id ->

            val enterRoomDialog = AlertDialog.Builder(this@ListActivity)
            val enterRoomDialogView = layoutInflater.inflate(R.layout.layout_enter_room, null)
            enterRoomDialog.setView(enterRoomDialogView)
            val dialog = enterRoomDialog.create()
            val enterButton : Button = enterRoomDialogView.findViewById(R.id.enterButton)
            val cancelButton : Button = enterRoomDialogView.findViewById(R.id.enterRoomCancelButton)

            enterButton.setOnClickListener {
                //Update joined person number
                showProgressbar(progressBar)
                updateJoinedPersonNumber(position, dialog)

            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            if (hpoolRequests[position].number < 4) {
                dialog.show()
            } else {
                Toast.makeText(this@ListActivity, "정원이 가득찼습니다.", Toast.LENGTH_LONG).show()
            }

        }

        settingButton.setOnClickListener {
            val intent = Intent(this@ListActivity, SettingActivity::class.java)
            startActivity(intent)
        }


    }

    /*
    *** Function: Update the number of person joined in database
     */

    private fun updateJoinedPersonNumber(position: Int, dialog: AlertDialog) {
        val newNumber = hpoolRequests[position].number + 1
        val roomId = hpoolRequests[position].id
        requestDB.collection("Request").document(roomId).update("number", newNumber).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                prefs.isJoined = true
                prefs.roomId = roomId
                val intent = Intent(this@ListActivity, RoomActivity::class.java)
                intent.putExtra(EXTRA_REQUEST_INFO, hpoolRequests[position].id)
                hideProgressbar(progressBar)
                startActivity(intent)
                dialog.dismiss()
                return@addOnCompleteListener
                gridAdapter.notifyDataSetChanged()
            }
        }.addOnFailureListener { e ->
            Log.d(TAG, e.localizedMessage)
            Toast.makeText(this@ListActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
        }
    }


    /*
    *** Function : Retreive carpool list from firestore
     */
    private fun retreiveRequest() {

        getDate()

        Log.d(TAG + "curDate : ", currentDate.toString() )
        Log.d(TAG + "nextDate : ", nextDate.toString() )

        requestReference.whereEqualTo("date", currentDate).addSnapshotListener(EventListener { requests, exception ->
            if (exception != null) {
                Log.e(TAG, exception.localizedMessage)
                return@EventListener
            }
            hpoolRequests.clear()
            if (requests != null) {
                for (request in requests) {
                    Log.d(TAG, request.data.toString())
                    val departure = request.data["departure"].toString()
                    val destination = request.data["destination"].toString()
                    val number = request.data["number"].toString().toInt()
                    val fullTime = request.data["date"].toString()
                    val time = request.data["time"].toString()
                    val pickUpLocation = request.data["pickUpLocation"].toString()
                    val id = request.id.toString()
                    val hpoolRequest = HPOOLRequest(id, departure, destination, fullTime, time, pickUpLocation, number)
                    hpoolRequests.add(hpoolRequest)
                    gridAdapter.notifyDataSetChanged()
                }
            }

        })

        requestReference.whereEqualTo("date", nextDate).addSnapshotListener(EventListener { requests, exception ->
            if (exception != null) {
                Log.e(TAG, exception.localizedMessage)
                return@EventListener
            }
            if (requests != null) {
                for (request in requests) {
                    Log.d(TAG, request.data.toString())
                    val departure = request.data["departure"].toString()
                    val destination = request.data["destination"].toString()
                    val number = request.data["number"].toString().toInt()
                    val fullTime = request.data["date"].toString()
                    val time = request.data["time"].toString()
                    val pickUpLocation = request.data["pickUpLocation"].toString()
                    val id = request.id.toString()
                    val hpoolRequest = HPOOLRequest(id, departure, destination, fullTime, time, pickUpLocation, number)
                    hpoolRequests.add(hpoolRequest)
                    gridAdapter.notifyDataSetChanged()
                }
            }

        })


    }

    /*
    ** Fun -> Calcuate today and tomorrow's date
     */
    private fun getDate() {
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val nextMonth = calendar.get(Calendar.MONTH) + 1
        val nextDay = calendar.get(Calendar.DAY_OF_MONTH)
        currentDate = "${month}월 ${day}일"
        nextDate = "${nextMonth}월 ${nextDay}일"
    }

    fun showProgressbar(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
    }
    fun hideProgressbar(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        retreiveRequest()
    }

    override fun onRestart() {
        super.onRestart()
        finish()
        startActivity(getIntent());
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
