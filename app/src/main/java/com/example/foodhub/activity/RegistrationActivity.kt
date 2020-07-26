package com.example.foodhub.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodhub.util.ConnectionManager
import com.example.foodhub.R
import kotlinx.android.synthetic.main.activity_registration.view.*
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.typeOf

class RegistrationActivity : AppCompatActivity() {
    lateinit var etName: EditText
    lateinit var etEmail: EditText
    lateinit var etAddress: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var progressBar: ProgressBar
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBarRegistration)
        progressBar.visibility = View.GONE
        sharedPreferences =
            getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)

        btnRegister.setOnClickListener {
            val name: String? = etName.text.toString()
            val email: String? = etEmail.text.toString()
            val address: String? = etAddress.text.toString()
            val mobileNumber: String? = etMobileNumber.text.toString()
            val password: String? = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (confirmPassword != password) {
                Toast.makeText(
                    this,
                    "The passwords do not match. Please enter password again",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val queue = Volley.newRequestQueue(this)
                val url = "http://13.235.250.119/v2/register/fetch_result"

                if (ConnectionManager().checkConnectivity(this)) {
                    progressBar.visibility = View.VISIBLE
                    val jsonParams = JSONObject()
                    jsonParams.put("name", name)
                    jsonParams.put("email", email)
                    jsonParams.put("address", address)
                    jsonParams.put("mobile_number", mobileNumber)
                    jsonParams.put("password", password)
                    val jsonRequest =
                        object : JsonObjectRequest(Request.Method.POST,
                            url,
                            jsonParams,
                            Response.Listener {
                                try {
                                    progressBar.visibility = View.GONE
                                    val data = it.getJSONObject("data")
                                    val success = data.getBoolean("success");
                                    if (success) {
                                        Toast.makeText(
                                            this,
                                            "You have successfully registered!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val response = data.getJSONObject("data")
                                        sharedPreferences.edit()
                                            .putString("user_id", response.getString("user_id")).apply()
                                        sharedPreferences.edit()
                                            .putString("user_name", response.getString("name")).apply()
                                        sharedPreferences.edit()
                                            .putString(
                                                "user_mobile_number",
                                                response.getString("mobile_number")
                                            )
                                            .apply()
                                        sharedPreferences.edit()
                                            .putString("user_address", response.getString("address"))
                                            .apply()
                                        sharedPreferences.edit()
                                            .putString("user_email", response.getString("email")).apply()
                                        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            data.getString("errorMessage"),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: JSONException) {
                                    Toast.makeText(
                                        this,
                                        "A json Exception $e error occurred!",
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

                    queue.add(jsonRequest)
                } else {
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
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
        finish()
    }
}