package com.example.foodhub.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodhub.R
import com.example.foodhub.adapter.OrderHistoryAdapter
import com.example.foodhub.model.OrderDetails
import com.example.foodhub.util.ConnectionManager
import org.json.JSONException

class OrderHistoryFragment: Fragment() {

    lateinit var recyclerOrder: RecyclerView
    lateinit var progressLayout: RelativeLayout
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var orderHistoryAdapter: OrderHistoryAdapter
    lateinit var sharedPreferences: SharedPreferences

    lateinit var txtOrderHistory: TextView

    var orderHistoryList = arrayListOf<OrderDetails>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        sharedPreferences = (activity as FragmentActivity).getSharedPreferences(getString(R.string.preferences_file_name),
            Context.MODE_PRIVATE)
        recyclerOrder = view.findViewById(R.id.recyclerOrderHistory)
        progressLayout = view.findViewById(R.id.progressLayout)
        txtOrderHistory = view.findViewById(R.id.txtOrderHistory)
        txtOrderHistory.visibility = View.GONE

        val userId = sharedPreferences.getString("user_id", "")
        layoutManager = LinearLayoutManager(activity as Context)

        progressLayout.visibility = View.VISIBLE

        sendServerRequest(userId)

        return view
    }

    private fun sendServerRequest(userId: String?) {
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"

        if(ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object: JsonObjectRequest(Method.GET, url, null,
            Response.Listener {
                progressLayout.visibility = View.GONE
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if(success) {
                        val resArray = data.getJSONArray("data")
                        if(resArray.length() == 0){
                            // user has not ordered anything yet
                            txtOrderHistory.visibility = View.VISIBLE
                        }
                        else {
                            for(i in 0 until resArray.length()) {
                                val orderObject = resArray.getJSONObject(i)
                                val foodItems = orderObject.getJSONArray("food_items")
                                val orderDetails = OrderDetails(
                                    orderObject.getInt("order_id"),
                                    orderObject.getString("restaurant_name"),
                                    orderObject.getString("order_placed_at"),
                                    foodItems)
                                orderHistoryList.add(orderDetails)
                                if(orderHistoryList.isEmpty()) {
                                    txtOrderHistory.visibility = View.VISIBLE
                                } else {
                                    if(activity != null) {
                                        orderHistoryAdapter = OrderHistoryAdapter(activity as Context, orderHistoryList)
                                        val mLayoutManager = LinearLayoutManager(activity as Context)
                                        recyclerOrder.layoutManager = mLayoutManager
                                        recyclerOrder.itemAnimator = DefaultItemAnimator()
                                        recyclerOrder.adapter = orderHistoryAdapter
                                    }
                                    else {
                                        queue.cancelAll(this::class.java.simpleName)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    Toast.makeText(activity as Context, "Some error $it occurred!", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                    Toast.makeText(activity as Context, "A Volley error $it occurred!", Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "deb6a2b542c8d1"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("Error ")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(context as FragmentActivity)
            }
            dialog.create()
            dialog.show()
        }
    }
}











