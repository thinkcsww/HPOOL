package com.applory.hpool.Controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.applory.hpool.R
import kotlinx.android.synthetic.main.activity_term_of_use.*

class TermOfUseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_of_use)
        termOfUseBackButton.setOnClickListener {
            finish()
        }

    }
}
