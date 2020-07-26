package com.example.foodhub.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodhub.R
import com.example.foodhub.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity2: AppCompatActivity() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmNewPassword: EditText
    lateinit var btnSubmit: Button
    private lateinit var mobileNumber: String
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_2)

        etOTP = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBarReset)
        progressBar.visibility = View.GONE

        if(intent != null) {
            mobileNumber = intent.getStringExtra("user_mobile") as String
        }

        btnSubmit.setOnClickListener {
            val otp = etOTP.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmNewPassword = etConfirmNewPassword.text.toString()

            if(otp.length == 4) {
                if(newPassword == confirmNewPassword) {
                    val queue = Volley.newRequestQueue(this)
                    val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                    if(ConnectionManager().checkConnectivity(this)) {
                        progressBar.visibility = View.VISIBLE
                        val jsonParams = JSONObject()
                        jsonParams.put("mobile_number", mobileNumber)
                        jsonParams.put("password", newPassword)
                        jsonParams.put("otp", otp)

                        val jsonObjectRequest = object: JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                            try {
                                progressBar.visibility = View.GONE
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    Toast.makeText(this, "Your password has been successfully changed!. Logging In...",
                                        Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this@ForgotPasswordActivity2, LoginActivity::class.java))
                                    finish()
                                }
                                else {
                                    val error = data.getString("errorMessage")
                                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                                }
                            } catch (e:JSONException) {
                                Toast.makeText(this, "The json Exception is $e", Toast.LENGTH_LONG).show()
                            }
                        }, Response.ErrorListener {
                            Toast.makeText(
                                this,
                                "A Volley error $it occurred!",
                                Toast.LENGTH_LONG
                            ).show()
                        }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-type"] = "application/json"
                                headers["token"] = "deb6a2b542c8d1"
                                return headers
                            }
                        }
                        queue.add(jsonObjectRequest)
                    }
                    else {
                        val dialog = AlertDialog.Builder(this)
                        dialog.setTitle("Error ")
                        dialog.setMessage("Internet Connection Not Found")
                        dialog.setPositiveButton("Open Settings") { text, listener ->
                            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingsIntent)
                            finish()
                        }
                        dialog.setNegativeButton("Exit") { text, listener ->
                            ActivityCompat.finishAffinity(this)
                        }
                        dialog.create()
                        dialog.show()
                    }
                } else {
                    Toast.makeText(this, "The passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
            }

        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this@ForgotPasswordActivity2, ForgotPasswordActivity::class.java))
        finish()
    }
}