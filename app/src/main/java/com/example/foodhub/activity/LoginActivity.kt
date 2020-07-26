package com.example.foodhub.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodhub.R
import com.example.foodhub.util.ConnectionManager
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParserException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException

class LoginActivity : AppCompatActivity() {
    private lateinit var etMobileNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogIn: Button
    private lateinit var txtForgotPassword: TextView
    private lateinit var txtRegister: TextView
    lateinit var progressBar: ProgressBar

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
        setContentView(R.layout.activity_login)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etpassword)
        btnLogIn = findViewById(R.id.btnLogIn)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtRegister = findViewById(R.id.txtRegister)
        progressBar = findViewById(R.id.progressBarLogin)
        progressBar.visibility = View.GONE

        btnLogIn.setOnClickListener {
            val mobileNumber = etMobileNumber.text.toString()
            val password = etPassword.text.toString()
            val intent = Intent(this, MainActivity::class.java)

            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/login/fetch_result/"

            if (ConnectionManager().checkConnectivity(this)) {
                if (mobileNumber.length == 10 && password.length >= 4) {
                    progressBar.visibility = View.VISIBLE
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", mobileNumber)
                    jsonParams.put("password", password)

                    val jsonObjectRequest =
                        object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                            try {
                                progressBar.visibility = View.GONE
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    val response = data.getJSONObject("data")

                                    sharedPreferences.edit()
                                        .putString("user_id", response.getString("user_id"))
                                        .apply()
                                    sharedPreferences.edit().putString(
                                        "user_mobile_number",
                                        response.getString("mobile_number")
                                    )
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("user_name", response.getString("name"))
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("user_email", response.getString("email"))
                                        .apply()
                                    sharedPreferences.edit()
                                        .putString("user_address", response.getString("address"))
                                        .apply()
                                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Incorrect Credentials",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                Toast.makeText(
                                    this,
                                    "A json Exception $e occurred",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, Response.ErrorListener {
                            val error: VolleyError = it
                            var errorMsg = ""
                            if (error is NoConnectionError) {
                                val cm =
                                    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                var activeNetwork: NetworkInfo? = null
                                activeNetwork = cm.activeNetworkInfo
                                errorMsg =
                                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
                                        "Server is not connected to the internet. Please try again"
                                    } else {
                                        "Your device is not connected to internet.please try again with active internet connection"
                                    }
                            } else if (error is NetworkError || error.cause is ConnectException) {
                                errorMsg =
                                    "Your device is not connected to internet.please try again with active internet connection"
                            } else if (error.cause is MalformedURLException) {
                                errorMsg = "That was a bad request please try again…"
                            } else if (error is ParseError || error.cause is IllegalStateException || error.cause is JSONException || error.cause is XmlPullParserException) {
                                errorMsg = "There was an error parsing data…"
                            } else if (error.cause is OutOfMemoryError) {
                                errorMsg = "Device out of memory"
                            } else if (error is AuthFailureError) {
                                errorMsg =
                                    "Failed to authenticate user at the server, please contact support"
                            } else if (error is ServerError || error.cause is ServerError) {
                                errorMsg = "Internal server error occurred please try again...."
                            } else if (error is TimeoutError || error.cause is SocketTimeoutException || error.cause is ConnectTimeoutException || error.cause is SocketException || (error.cause!!.message != null && error.cause!!.message!!.contains(
                                    "Your connection has timed out, please try again"
                                ))
                            ) {
                                errorMsg = "Your connection has timed out, please try again"
                            } else {
                                errorMsg =
                                    "An unknown error occurred during the operation, please try again"
                            }
                            Toast.makeText(
                                this,
                                errorMsg,
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

        txtRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        txtForgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}