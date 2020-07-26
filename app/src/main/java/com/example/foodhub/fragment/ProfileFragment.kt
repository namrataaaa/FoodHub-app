package com.example.foodhub.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.foodhub.R

class ProfileFragment : Fragment() {

    private lateinit var txtUserName: TextView
    private lateinit var txtUserPhone: TextView
    private lateinit var txtUserAddress: TextView
    private lateinit var txtUserEmail: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPreferences = (activity as FragmentActivity)
            .getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)

        txtUserName = view.findViewById(R.id.txtUserName)
        txtUserPhone = view.findViewById(R.id.txtUserPhone)
        txtUserAddress = view.findViewById(R.id.txtUserAddress)
        txtUserEmail = view.findViewById(R.id.txtUserEmail)

        txtUserName.text = sharedPreferences.getString("user_name", null)
        val number = "+91 ${sharedPreferences.getString("user_mobile_number", null)}"
        txtUserPhone.text = number
        txtUserAddress.text = sharedPreferences.getString("user_address", null)
        txtUserEmail.text = sharedPreferences.getString("user_email", null)

        return view
    }
}