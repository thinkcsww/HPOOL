package com.applory.hpool.Controllers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.applory.hpool.Models.HPOOLRequest
import com.applory.hpool.R
import kotlinx.android.synthetic.main.activity_request.*
import java.util.*

class RequestActivity : AppCompatActivity() {

    lateinit var calendar: Calendar
    var year = 0; var month = 0; var day = 0 ; var hour = 0; var minute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)
        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH)

        dateTextView.setText("${this.month + 1}월 ${this.day}일")
        timeTextView.setText("0시 0분")

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

            val destination = destinationEditText.text.toString()
            val departure = departureEditText.text.toString()
            val pickup = pickupEditText.text.toString()
            val date = dateTextView.text.toString()
            val time = timeTextView.text.toString()

            val newPoolRequest = HPOOLRequest(departure, destination, "${date} ${time}", pickup)


        }

        cancleButton.setOnClickListener {
            finish()

        }

    }
}
