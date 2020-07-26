package com.example.foodhub.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.foodhub.R

class SplashActivity : AppCompatActivity() {

    val permissionString =
        arrayOf(Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET)
    private lateinit var sharedPreferences: SharedPreferences
    private val splashTimeOut: Long = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_background)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)

//        Handler().postDelayed({
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }, splashTimeOut)

        if (!hasPermissions(this, permissionString)) {
            ActivityCompat.requestPermissions(this, permissionString, 101)
        } else {

            /*The handler delays the opening of the new activity thus displaying the logo for 1000 milliseconds i.e. 1 seconds*/
            Handler().postDelayed({
                openNewActivity()
            }, 1000)
        }

    }

    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        var hasAllPermissions = true
        for (permission in permissions) {
            val res = context.checkCallingOrSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
            }
        }
        return hasAllPermissions
    }

    fun openNewActivity() {
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Handler().postDelayed({
                        openNewActivity()
                    }, 1000)
                } else {
                    Toast.makeText(
                        this,
                        "Please grant all permissions to continue",
                        Toast.LENGTH_SHORT
                    ).show()
                    this.finish()
                }
                return
            }
            else -> {
                Toast.makeText(this@SplashActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
                this.finish()
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}