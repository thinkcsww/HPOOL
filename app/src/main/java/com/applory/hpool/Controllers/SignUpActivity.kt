package com.applory.hpool.Controllers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import com.applory.hpool.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.layout_agree_term.*

class SignUpActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    val TAG = SignUpActivity::class.java.simpleName
    val userDB = FirebaseFirestore.getInstance().collection("User")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        signUpButton.setOnClickListener { view ->
            if (!TextUtils.isEmpty(signUpIdEditText.text) && !TextUtils.isEmpty(signUpPasswordEditText.text)) {
                view.hideKeyboard()
                val email = signUpIdEditText.text.toString()
                val password = signUpPasswordEditText.text.toString()

                val agreeAlertDialog = AlertDialog.Builder(this)
                val agreeDialogView = layoutInflater.inflate(R.layout.layout_agree_term, null)
                agreeAlertDialog.setView(agreeDialogView)
                val agreeCompleteButton: Button = agreeDialogView.findViewById(R.id.agreeCompleteButton)
                val termOfUseCheckBox: CheckBox = agreeDialogView.findViewById(R.id.termOfUseCheckBox)
                val personalInfoCheckBox:CheckBox = agreeDialogView.findViewById(R.id.personalInfoCheckBox)
                val dialog = agreeAlertDialog.create()
                agreeCompleteButton.setOnClickListener {
                    if (termOfUseCheckBox.isChecked && personalInfoCheckBox.isChecked) {
                        showProgressbar(signUpProgressBar)
                        dialog.dismiss()
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                                val agree = HashMap<String, Any>()
                                agree.put("agreeTerm", true)

                                userDB.document(userId).set(agree).addOnSuccessListener {
                                    hideProgressbar(signUpProgressBar)
                                    Toast.makeText(this@SignUpActivity, "회원가입 성공", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@SignUpActivity, NickNameActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(this@SignUpActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                                    Log.d(TAG, exception.localizedMessage)
                                }

                            } else {
                                hideProgressbar(signUpProgressBar)
                                Toast.makeText(this@SignUpActivity, "회원가입 실패: 잘못된 이메일 형식입니다.", Toast.LENGTH_LONG).show()
                                signUpIdEditText.requestFocus()
                                view.showKeyboard()
                                dialog.dismiss()
                            }
                        })
                    } else {
                        Toast.makeText(this, "약관을 동의해야 서비스를 사용할 수 있습니다.", Toast.LENGTH_LONG).show()
                    }

                }
                dialog.show()



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
