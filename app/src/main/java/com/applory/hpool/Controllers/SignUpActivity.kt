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
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        signUpButton.setOnClickListener { view ->
            if (!TextUtils.isEmpty(signUpIdEditText.text) && !TextUtils.isEmpty(signUpPasswordEditText.text)) {
                showProgressbar(signUpProgressBar)
                view.hideKeyboard()
                val email = signUpIdEditText.text.toString()
                val password = signUpPasswordEditText.text.toString()

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressbar(signUpProgressBar)
                        Toast.makeText(this@SignUpActivity, "회원가입 성공", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@SignUpActivity, ListActivity::class.java)
                        startActivity(intent)
                        finish()
                        return@OnCompleteListener
                    } else {
                        hideProgressbar(signUpProgressBar)
                        Toast.makeText(this@SignUpActivity, "회원가입 실패: 잘못된 형식입니다.", Toast.LENGTH_LONG).show()
                        signUpIdEditText.requestFocus()
                        view.showKeyboard()

                    }
                })
            } else {
                hideProgressbar(signUpProgressBar)
                Toast.makeText(this@SignUpActivity, "아이디 혹은 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show()
                signUpIdEditText.requestFocus()
                view.showKeyboard()
            }


        }


    }
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
        imm.showSoftInput(signUpIdEditText, InputMethodManager.SHOW_IMPLICIT)
    }
}
