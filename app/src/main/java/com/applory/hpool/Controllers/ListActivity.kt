package com.applory.hpool.Controllers

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.EventLog
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

class ListActivity : AppCompatActivity() {

    val TAG = ListActivity::class.java.simpleName

    lateinit var prefs: SharedPrefs

    lateinit var gridAdapter: GridAdapter
    var hpoolRequests =  ArrayList<HPOOLRequest>()
    val requestDB = FirebaseFirestore.getInstance()
    val requestReference = requestDB.collection("Request")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        prefs = SharedPrefs(this@ListActivity)




        gridAdapter = GridAdapter(this@ListActivity, hpoolRequests)

        gridView.adapter = gridAdapter

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

        requestReference.addSnapshotListener(EventListener { requests, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.e(TAG, firebaseFirestoreException.localizedMessage)
                return@EventListener
            }
            hpoolRequests.clear()
            if (requests != null) {
                for (request in requests) {
                    Log.d(TAG, request.data.toString())
                    val departure = request.data["departure"].toString()
                    val destination = request.data["destination"].toString()
                    val number = request.data["number"].toString().toInt()
                    val fullTime = request.data["fullTime"].toString()
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
}
