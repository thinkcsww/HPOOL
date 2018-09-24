package com.applory.hpool.Controllers

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.applory.hpool.Adapters.GridAdapter
import com.applory.hpool.Models.HPOOLRequest
import com.applory.hpool.R
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {

    lateinit var gridAdapter: GridAdapter
    var hpoolRequests =  ArrayList<HPOOLRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val newHPOOLREquest = HPOOLRequest("학교", "양덕", "13:00", "택시승강장")
        for (i in 1..10) {
            hpoolRequests.add(newHPOOLREquest)
        }
        hpoolRequests.add(newHPOOLREquest)

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
}
