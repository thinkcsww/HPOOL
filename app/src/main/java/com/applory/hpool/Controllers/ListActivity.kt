package com.applory.hpool.Controllers

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.EventLog
import android.util.Log
import com.applory.hpool.Adapters.GridAdapter
import com.applory.hpool.Models.HPOOLRequest
import com.applory.hpool.R
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {

    val TAG = ListActivity::class.java.simpleName

    lateinit var gridAdapter: GridAdapter
    var hpoolRequests =  ArrayList<HPOOLRequest>()
    val requestDB = FirebaseFirestore.getInstance()
    val requestReference = requestDB.collection("Request")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        retrieveRequest()


        gridAdapter = GridAdapter(this@ListActivity, hpoolRequests)

        gridView.adapter = gridAdapter

        floatingButton.setOnClickListener {
            val intent = Intent(this@ListActivity, RequestActivity::class.java)
            startActivity(intent)
        }

        gridView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@ListActivity, RoomActivity::class.java)
            startActivity(intent)
        }


    }

    private fun retrieveRequest() {
        requestReference.addSnapshotListener(EventListener { documentSnapshots, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.e(TAG, firebaseFirestoreException.localizedMessage)
                return@EventListener
            }
            hpoolRequests.clear()
            if (documentSnapshots != null) {
                for (request in documentSnapshots) {
                    val departure = request.data["departure"].toString()
                    val destination = request.data["destination"].toString()
                    val number = request.data["number"].toString().toInt()
                    val fullTime = request.data["fullTime"].toString()
                    val time = request.data["time"].toString()
                    val pickUpLocation = request.data["pickUpLocation"].toString()

                    val hpoolRequest = HPOOLRequest(departure, destination, fullTime, time, pickUpLocation, number)
                    hpoolRequests.add(hpoolRequest)
                    gridAdapter.notifyDataSetChanged()
                }
            }

        })
    }
}
