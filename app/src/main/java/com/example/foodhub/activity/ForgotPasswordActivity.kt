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

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etEmail: EditText
    lateinit var btnNext: Button
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmail = findViewById(R.id.etEmail)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBarForgot)
        progressBar.visibility = View.GONE

        btnNext.setOnClickListener {
            val mobileNumber = etMobileNumber.text.toString()
            val email = etEmail.text.toString()
            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

            if (ConnectionManager().checkConnectivity(this)) {
                if (mobileNumber.length == 10 && email.contains('@')) {
                    progressBar.visibility = View.VISIBLE
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", mobileNumber)
                    jsonParams.put("email", email)
                    val jsonObjectRequest =
                        object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                            try {
                                progressBar.visibility = View.GONE
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    val firstTry = data.getBoolean("first_try")
                                    if (firstTry) {
                                        Toast.makeText(
                                            this,
                                            "Please check your registered email for the OTP",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                        val intent = Intent(
                                            this@ForgotPasswordActivity,
                                            ForgotPasswordActivity2::class.java
                                        )
                                        intent.putExtra("user_mobile", mobileNumber)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        //if not first try
                                        Toast.makeText(
                                            this,
                                            "Please refer to your email for the OTP",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                        val intent = Intent(
                                            this@ForgotPasswordActivity,
                                            ForgotPasswordActivity2::class.java
                                        )
                                        intent.putExtra("user_mobile", mobileNumber)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    // Success is false
                                    val error = data.getString("errorMessage")
                                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: JSONException) {
                                Toast.makeText(
                                    this,
                                    "A json exception $e occurred!",
                                    Toast.LENGTH_LONG
                                ).show()
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
            } else {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Error ")
                dialog.setMessage("Internet Connection Not Found")
                dialog.setPositiveButton("Open Settings") { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit") { _, _ ->
                    ActivityCompat.finishAffinity(this)
                }
                dialog.create()
                dialog.show()
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
        finish()
    }
}