package com.applory.hpool.Controllers

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.applory.hpool.R
import com.applory.hpool.Utilities.SharedPrefs
import kotlinx.android.synthetic.main.activity_nick_name.*

class NickNameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nick_name)

        val prefs = SharedPrefs(this@NickNameActivity)

        nicknameOkButton.setOnClickListener {
            prefs.nickname = nicknameEditText.text.toString()
            val intent = Intent(this@NickNameActivity, ListActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
