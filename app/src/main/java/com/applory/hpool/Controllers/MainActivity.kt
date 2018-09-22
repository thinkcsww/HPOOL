package com.applory.hpool.Controllers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import com.applory.hpool.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
class MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()



        goToSignUpButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener { view ->

            if (!TextUtils.isEmpty(loginIdEditText.text) && !TextUtils.isEmpty(loginPasswordEditText.text)) {
                view.hideKeyboard()
                showProgressbar(loginProgressBar)

                val email = loginIdEditText.text.toString()
                val password = loginPasswordEditText.text.toString()
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressbar(loginProgressBar)
                        Toast.makeText(this@MainActivity, "로그인 완료", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@MainActivity, ListActivity::class.java)
                        startActivity(intent)
                        finish()
                        return@OnCompleteListener

                    } else {
                        hideProgressbar(loginProgressBar)
                        Toast.makeText(this@MainActivity, "잘못된 아이디 혹은 비밀번호", Toast.LENGTH_LONG).show()
                        loginIdEditText.requestFocus()
                        view.showKeyboard()
                        return@OnCompleteListener
                    }
                })
            } else {
                Toast.makeText(this@MainActivity, "아이디 혹은 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show()
                hideProgressbar(loginProgressBar)
                loginIdEditText.requestFocus()
                view.showKeyboard()
            }
        }

    } // Oncreate finish

    fun showProgressbar(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
    }
    fun hideProgressbar(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(loginIdEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@MainActivity, ListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


