package com.example.foodhub.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodhub.R
import com.example.foodhub.activity.CartActivity
import com.example.foodhub.adapter.RestaurantMenuAdapter
import com.example.foodhub.database_cart.OrderEntity
import com.example.foodhub.database_fav_res.RestaurantDatabase
import com.example.foodhub.model.FoodItem
import com.example.foodhub.util.ConnectionManager
import com.example.foodhub.util.DrawerLocker
import com.google.gson.Gson
import org.json.JSONException

class RestaurantFragment: Fragment() {
    lateinit var recyclerMenu: RecyclerView
    lateinit var restaurantMenuAdapter: RestaurantMenuAdapter
    private var menuList = arrayListOf<FoodItem>()
    lateinit var progressLayout: RelativeLayout
    private var orderList = arrayListOf<FoodItem>()

    lateinit var sharedPreferences: SharedPreferences

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var goToCart: Button
        var resId: Int? = 0
        var resName: String? = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant, container, false)
        sharedPreferences = activity?.getSharedPreferences(getString(R.string.preferences_file_name),
            Context.MODE_PRIVATE) as SharedPreferences
        progressLayout = view.findViewById(R.id.progressLayout) as RelativeLayout
        progressLayout.visibility = View.VISIBLE

        resId = arguments?.getInt("resId", 0)
        resName = arguments?.getString("resName", "")
        (activity as DrawerLocker).setDrawerEnabled(false)
        setHasOptionsMenu(true)
        goToCart = view.findViewById(R.id.btnGoToCart) as Button
        goToCart.visibility = View.GONE

        goToCart.setOnClickListener {
            goToCart()
        }
        setUpRestaurantMenu(view)
        return view
    }

    private fun setUpRestaurantMenu(view: View) {
        recyclerMenu = view.findViewById(R.id.recyclerMenuItems)
        val recyclerMenuLayoutParams = RelativeLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)

        if(ConnectionManager().checkConnectivity(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

            val jsonObjectRequest = object: JsonObjectRequest(Method.GET,
                url + resId, null, Response.Listener {
                try {
                    progressLayout.visibility = View.GONE
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if(success) {
                        val resArray = data.getJSONArray("data")
                        for(i in 0 until resArray.length()) {
                            val menuObject = resArray.getJSONObject(i)
                            val foodItem = FoodItem(
                                menuObject.getString("id"),
                                menuObject.getString("name"),
                                menuObject.getString("cost_for_one").toInt()
                            )
                            menuList.add(foodItem)
                            restaurantMenuAdapter = RestaurantMenuAdapter(
                                activity as Context,
                                menuList,
                                object : RestaurantMenuAdapter.OnItemClickListener {
                                    override fun onAddItemClick(foodItem: FoodItem) {
                                        orderList.add(foodItem)
                                        Log.i("size", "The size of orderlist is ${orderList.size}")
                                        if (orderList.size > 0) {
                                            recyclerMenuLayoutParams.setMargins(0, 140, 0, 140)
                                            recyclerMenu.layoutParams = recyclerMenuLayoutParams
                                            goToCart.visibility = View.VISIBLE
                                            RestaurantMenuAdapter.isCartEmpty = false
                                        }
                                    }

                                    override fun onRemoveItemClick(foodItem: FoodItem) {
                                        orderList.remove(foodItem)
                                        if (orderList.isEmpty()) {
                                            recyclerMenuLayoutParams.setMargins(0, 140, 0, 0)
                                            recyclerMenu.layoutParams = recyclerMenuLayoutParams
                                            goToCart.visibility = View.GONE
                                            RestaurantMenuAdapter.isCartEmpty = true
                                        }
                                    }
                                })

                            val mLayoutManager = LinearLayoutManager(activity)
                            recyclerMenu.layoutManager = mLayoutManager
                            recyclerMenu.itemAnimator = DefaultItemAnimator()
                            recyclerMenu.adapter = restaurantMenuAdapter
                        }
                    }
                } catch (e:JSONException) {
                    Toast.makeText(context, "A Json Exception $e Occurred!", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                    Toast.makeText(
                    activity as Context,
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

    private fun goToCart() {
        val gson = Gson()
        val foodItems = gson.toJson(orderList)
        val async = ItemsOfCart(activity as Context, resId.toString(), foodItems, 1).execute()
        val result = async.get()
        if(result)  {
            val data = Bundle()
            data.putInt("resId", resId as Int)
            data.putString("resName", resName)
            val intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("data", data)
            startActivity(intent)
        }
        else {
            Toast.makeText(activity as Context, "Some unexpected Error occurred!", Toast.LENGTH_SHORT).show()
        }
    }

    class ItemsOfCart(context: Context,
                      private val restaurantId: String,
                      private val foodItems: String,
                      private val mode: Int):
        AsyncTask<Void, Void, Boolean>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }
                2 -> {
                    db.orderDao().deleteOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}