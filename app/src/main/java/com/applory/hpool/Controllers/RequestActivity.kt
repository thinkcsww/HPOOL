package com.applory.hpool.Controllers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.applory.hpool.Controllers.App.Companion.prefs
import com.applory.hpool.Models.HPOOLRequest
import com.applory.hpool.R
import com.applory.hpool.R.id.*
import com.applory.hpool.Utilities.EXTRA_REQUEST_INFO
import com.applory.hpool.Utilities.SharedPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_request.*
import java.util.*

class RequestActivity : AppCompatActivity() {

    val TAG = RequestActivity::class.java.simpleName
    lateinit var prefs: SharedPrefs
    //Date
    lateinit var calendar: Calendar
    var year = 0; var month = 0; var day = 0 ; var hour = 0; var minute = 0

    // Database
    val requestDB = FirebaseFirestore.getInstance()


    val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)


        prefs = SharedPrefs(this@RequestActivity)

        Log.d(TAG, prefs.isJoined.toString())

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH)



        dateTextView.setText("${this.month + 1}월 ${this.day}일")
        timeTextView.setText("00시 00분")

        dateTextView.setOnClickListener {

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this@RequestActivity, R.style.DialogTheme, DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                this.year = calendar.get(Calendar.YEAR).toInt()
                this.month = calendar.get(Calendar.MONTH).toInt()
                this.day = calendar.get(Calendar.DAY_OF_MONTH).toInt()
                dateTextView.setText("${this.month + 1}월 ${this.day}일")
//                reserveTime = ""
//                reserveTime = "$year 년 ${month + 1} 월 $day 일 "
            }, year, month, day)
            datePickerDialog.show()
        }

        timeTextView.setOnClickListener {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this@RequestActivity, R.style.DialogTheme, TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                timeTextView.setText("${hour.toString()}시 ${minute}분")
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                this.hour = calendar.get(Calendar.HOUR_OF_DAY).toInt()
                this.minute = calendar.get(Calendar.MINUTE).toInt()
                timeTextView.setText("${this.hour}시 ${this.hour}분")
//                reserveTime = "${this.year} 년 ${this.month + 1} 월 ${this.day} 일 "
//                reserveTime += "$hour 시 $minute 분 "
            }, hour, minute, true)
            timePickerDialog.show()
        }

        okButton.setOnClickListener {

            prefs.isJoined = true


            val destination = destinationEditText.text.toString()
            val departure = departureEditText.text.toString()
            val pickup = pickupEditText.text.toString()
            val date = dateTextView.text.toString()
            val time = timeTextView.text.toString()

            if(!TextUtils.isEmpty(destination) && !TextUtils.isEmpty(departure)
            && !TextUtils.isEmpty(pickup) && !TextUtils.isEmpty(date) && !TextUtils.isEmpty(date)) {

                //빈칸이 없으면 newPoolRequest에 담는다.
                val newPoolRequest = HPOOLRequest(departure, destination, "${date} ${time}", time, pickup)
                //프로그래스바 보여주기
                progressBar.visibility = View.VISIBLE

                //DB에 담는다.
                requestDB.collection("Request").document(userId).set(newPoolRequest).addOnCompleteListener {
                    Toast.makeText(this@RequestActivity, "요청되었습니다.", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    val intent = Intent(this@RequestActivity, RoomActivity::class.java)
                    intent.putExtra(EXTRA_REQUEST_INFO, newPoolRequest)
                    startActivity(intent)
                    finish()
                    return@addOnCompleteListener
                }.addOnFailureListener {
                    Toast.makeText(this@RequestActivity, "요청이 실패하였습니다.", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    Log.d(TAG, it.localizedMessage)
                }
            } else {
                //빈칸이 있을 때 토스트를 보여준다.
                Toast.makeText(this@RequestActivity, "빈칸을 입력해주세요.", Toast.LENGTH_LONG).show()
            }


        }

        cancleButton.setOnClickListener {
            finish()

        }

    }
}
