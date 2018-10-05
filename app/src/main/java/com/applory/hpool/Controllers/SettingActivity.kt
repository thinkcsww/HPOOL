package com.applory.hpool.Controllers

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.applory.hpool.R
import com.applory.hpool.Utilities.SharedPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    var roomId: String? = null
    val TAG = SettingActivity::class.java.simpleName
    val requestDB = FirebaseFirestore.getInstance()
    val requestReference = requestDB.collection("Request")

    lateinit var prefs: SharedPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)


        prefs = SharedPrefs(this@SettingActivity)
        roomId = prefs.roomId

        welcomeTextView.text = "즐거운 카풀되세요 ${prefs.nickname}님!"

        termOfUseButton.setOnClickListener {
            val intent = Intent(this, TermOfUseActivity::class.java)
            startActivity(intent)

        }

        termOfPersonalInfoButton.setOnClickListener {
            val intent = Intent(this, TermOfPersonalInfoActivity::class.java)
            startActivity(intent)
        }

        personalInfoBackButton.setOnClickListener {
            super.onBackPressed()
        }

        logoutButton.setOnClickListener {
            val logoutAlertDialog = AlertDialog.Builder(this@SettingActivity)
            val logoutDialogView = layoutInflater.inflate(R.layout.layout_logout, null)
            logoutAlertDialog.setView(logoutDialogView)
            val dialog = logoutAlertDialog.create()
            val outButton : Button = logoutDialogView.findViewById(R.id.outButton)
            val cancleButton : Button = logoutDialogView.findViewById(R.id.cancleButton)

            outButton.setOnClickListener {



                if(prefs.isJoined) {
                    //방장이 방을 나갈때
                    if (userId == roomId) {
                        requestDB.collection("Request").document(roomId!!).delete().addOnSuccessListener {
                            dialog.dismiss()
                            resetState()
                            finishListAcitivty()
                            finish()
                            return@addOnSuccessListener
                        }.addOnFailureListener { e ->
                            Log.d(TAG + "Master room delete:", e.localizedMessage)
                            Toast.makeText(this@SettingActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                            return@addOnFailureListener
                        }
                        //카풀 참여자가 방을 나갈때
                    } else {
                        requestReference.document(roomId!!).get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                val number = task.result.data!!["number"].toString().toInt()
                                val newNumber = number - 1

                                requestDB.collection("Request").document(roomId!!).update("number", newNumber).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        dialog.dismiss()
                                        resetState()
                                        finishListAcitivty()
                                        finish()
                                    }
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(this@SettingActivity, "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                                    Log.d(TAG + ": Room Quit Error : ", exception.localizedMessage)

                                }
                            }
                        }
                    }
                } else {
                    resetState()
                    finishListAcitivty()
                    finish()

                }


            }

            cancleButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun finishListAcitivty() {

        val intent = Intent("finish_activity")
        sendBroadcast(intent)
    }

    private fun resetState() {
        prefs.isJoined = false
        prefs.nickname = ""
        prefs.roomId = ""

        auth.signOut()

        val intent = Intent(this@SettingActivity, MainActivity::class.java)
        startActivity(intent)
    }

}
