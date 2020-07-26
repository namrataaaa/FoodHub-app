package com.example.foodhub.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodhub.R
import com.example.foodhub.adapter.CartItemAdapter
import com.example.foodhub.adapter.RestaurantMenuAdapter
import com.example.foodhub.database_cart.OrderEntity
import com.example.foodhub.database_fav_res.RestaurantDatabase
import com.example.foodhub.fragment.RestaurantFragment
import com.example.foodhub.model.FoodItem
import com.example.foodhub.util.ConnectionManager
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CartActivity: AppCompatActivity() {

    lateinit var progressLayout: RelativeLayout
    lateinit var rlCart: RelativeLayout
    private lateinit var txtResName: TextView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private var orderList = arrayListOf<FoodItem>()
    private lateinit var cartItemAdapter: CartItemAdapter
    private lateinit var recyclerCart: RecyclerView
    var resId: Int = 0
    private var resName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        init()
        setUpToolBar()
        setUpCartList()
        placeOrder()
    }

    private fun init() {
        progressLayout = findViewById(R.id.progressLayout)
        rlCart = findViewById(R.id.rlCart)
        txtResName = findViewById(R.id.txtRestaurantName)
        txtResName.text = RestaurantFragment.resName
        val bundle = intent?.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = bundle.getString("resName", "") as String
    }

    private fun setUpToolBar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpCartList() {
        recyclerCart = findViewById(R.id.recyclerCart)
        val dbList = GetItemsFromDBASync(applicationContext).execute().get()
        for(element in dbList) {
            orderList.addAll(Gson().fromJson(element.foodItems, Array<FoodItem>::class.java).asList())
        }
        if(orderList.isEmpty()) {
            rlCart.visibility = View.GONE
            progressLayout.visibility = View.VISIBLE
        } else {
            rlCart.visibility = View.VISIBLE
            progressLayout.visibility = View.GONE
        }

        cartItemAdapter = CartItemAdapter(orderList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this)
        recyclerCart.layoutManager = mLayoutManager
        recyclerCart.itemAnimator = DefaultItemAnimator()
        recyclerCart.adapter = cartItemAdapter
    }

    private fun placeOrder() {
        val btnPlaceOrder: Button = findViewById(R.id.btnPlaceOrder)

        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost_for_one
        }
        val total = "Place Order (Total: Rs. $sum)"
        btnPlaceOrder.text = total

        btnPlaceOrder.setOnClickListener {
            rlCart.visibility = View.GONE
            progressLayout.visibility = View.VISIBLE
            sendServerRequest()
        }
    }

    private fun sendServerRequest() {
        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/place_order/fetch_result/"
        val userId = this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
            .getString("user_id", null) as String
        val jsonParams = JSONObject()
        jsonParams.put("user_id", userId)
        jsonParams.put("restaurant_id", resId.toString())
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost_for_one
        }
        jsonParams.put("total_cost", sum.toString())
        val foodArray = JSONArray()
        for(i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].id)
            foodArray.put(i, foodId)
        }
        jsonParams.put("food", foodArray)
        if(ConnectionManager().checkConnectivity(this)){
            progressLayout.visibility = View.VISIBLE
            val jsonObjectRequest = object: JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if(success) {
                        progressLayout.visibility = View.GONE
                        val clearCart = ClearDBAsync(applicationContext, resId.toString()).execute().get()
                        val clearAll = ClearAllDBAsync(applicationContext).execute().get()
                        RestaurantMenuAdapter.isCartEmpty = true
                        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                        dialog.setContentView(R.layout.order_placed_dialog)
                        dialog.show()
                        dialog.setCancelable(false)
                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                        btnOk.setOnClickListener {
                            dialog.dismiss()
                            startActivity(Intent(this, MainActivity::class.java))
                            ActivityCompat.finishAffinity(this)
                        }
                    }
                    else {
                        //if not success
                        rlCart.visibility = View.VISIBLE
                        Toast.makeText(this, "Some Error Occurred!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e:JSONException) {
                    rlCart.visibility = View.VISIBLE
                    Toast.makeText(this, "Some Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                val clearAll = ClearAllDBAsync(applicationContext).execute().get()
                Toast.makeText(this, "Oops! A Volley Error Occurred!", Toast.LENGTH_LONG).show()
            }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "deb6a2b542c8d1"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
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

    class GetItemsFromDBASync(context: Context): AsyncTask<Void, Void, List<OrderEntity>>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            val orders = db.orderDao().getAllOrders()
            db.close()
            return orders
        }
    }

    class ClearDBAsync(context: Context, private val resId: String): AsyncTask<Void, Void, Boolean>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }
    }

    class ClearAllDBAsync(context: Context): AsyncTask<Void, Void, Boolean>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteAllOrders()
            db.close()
            return true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val clearCart = ClearDBAsync(applicationContext, resId.toString()).execute().get()
        onBackPressed()
        val clearAll = ClearAllDBAsync(applicationContext).execute().get()
        RestaurantMenuAdapter.isCartEmpty = true
        return true
    }
}









