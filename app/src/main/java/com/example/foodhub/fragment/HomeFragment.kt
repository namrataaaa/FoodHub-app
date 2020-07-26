package com.example.foodhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodhub.R
import com.example.foodhub.adapter.HomeRecyclerAdapter
import com.example.foodhub.database_fav_res.RestaurantEntity
import com.example.foodhub.model.Restaurant
import com.example.foodhub.util.ConnectionManager
import com.example.foodhub.util.DrawerLocker
import org.json.JSONException
import java.util.*
import java.util.Locale.filter
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    lateinit var recyclerHome: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HomeRecyclerAdapter

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    val restaurantInfoList = arrayListOf<Restaurant>()

    private val ratingComparator = Comparator<Restaurant>{ res1, res2 ->
        if(res1.restaurantRating.compareTo(res2.restaurantRating, true) == 0) {
            res1.restaurantName.compareTo(res2.restaurantName, true)
        } else {
            res1.restaurantRating.compareTo(res2.restaurantRating, true)
        }
    }

    private val costComparator = Comparator<Restaurant> { res1, res2 ->
        if(res1.restaurantPrice.compareTo(res2.restaurantPrice, true) == 0) {
            res1.restaurantName.compareTo(res2.restaurantName, true)
        } else {
            res1.restaurantPrice.compareTo(res2.restaurantPrice, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        (activity as DrawerLocker).setDrawerEnabled(true)
        recyclerHome = view.findViewById(R.id.recyclerHome)
        layoutManager = LinearLayoutManager(activity)

        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)

        setHasOptionsMenu(true)

        progressLayout.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            if (activity != null) {
                val jsonObject =
                    object : JsonObjectRequest(Method.GET, url, null, Response.Listener {
                        try {
                            progressLayout.visibility = View.GONE
                            val data = it.getJSONObject("data")
                            val success = data.getBoolean("success")
                            if (success) {
                                val response = data.getJSONArray("data")
                                for (i in 0 until response.length()) {
                                    val resJsonObject = response.getJSONObject(i)
                                    val resObject = Restaurant(
                                        resJsonObject.getString("id"),
                                        resJsonObject.getString("name"),
                                        resJsonObject.getString("rating"),
                                        resJsonObject.getString("cost_for_one"),
                                        resJsonObject.getString("image_url")
                                    )
                                    restaurantInfoList.add(resObject)
                                    recyclerAdapter =
                                        HomeRecyclerAdapter(activity as Context, restaurantInfoList)
                                    recyclerHome.adapter = recyclerAdapter
                                    recyclerHome.layoutManager = layoutManager
                                }
                            } else {
                                //if not success
                                val error = data.getString("errorMessage")
                                Toast.makeText(
                                    activity as Context,
                                    error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(
                                activity as Context,
                                "A json Exception $e occurred",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }, Response.ErrorListener {
                        if (activity != null)
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
                queue.add(jsonObject)
            }
        } else {
            // No internet
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error ")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.action_sort) {
            val builder = AlertDialog.Builder(context)
            val alertView = LayoutInflater.from(context).inflate(R.layout.sort_alert_dialog, null)
            val btnCostLH = alertView.findViewById<RadioButton>(R.id.radioBtn1)
            val btnCostHL = alertView.findViewById<RadioButton>(R.id.radioBtn2)
            val btnRating = alertView.findViewById<RadioButton>(R.id.radioBtn3)
            val btnCancel = alertView.findViewById<Button>(R.id.btnCancel)
            val btnOk = alertView.findViewById<Button>(R.id.btnOk)
            builder.setView(alertView)
            builder.setCancelable(true)
            val dialog = builder.create()
            dialog.setTitle("Choose the sort option")

            btnCostLH.setOnClickListener {
                Collections.sort(restaurantInfoList, costComparator)
            }
            btnCostHL.setOnClickListener {
                Collections.sort(restaurantInfoList, costComparator)
                restaurantInfoList.reverse()
            }
            btnRating.setOnClickListener {
                Collections.sort(restaurantInfoList, ratingComparator)
                restaurantInfoList.reverse()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            btnOk.setOnClickListener {
                recyclerAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }

            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            val wmlp = dialog.window!!.attributes
            wmlp.gravity = Gravity.CENTER
            dialog.show()
        }
        return super.onOptionsItemSelected(item)
    }
}