package com.example.foodhub.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.foodhub.R
import com.example.foodhub.adapter.HomeRecyclerAdapter
import com.example.foodhub.database_fav_res.RestaurantDatabase
import com.example.foodhub.database_fav_res.RestaurantEntity
import com.example.foodhub.model.Restaurant

class FavouritesFragment : Fragment() {
    private lateinit var recyclerFavourite: RecyclerView

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var txtFav: TextView

    private var dbRestaurantList = arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        recyclerFavourite = view.findViewById(R.id.recyclerFavourite)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)
        txtFav = view.findViewById(R.id.txtFav)

        txtFav.visibility = View.VISIBLE

        setUpRecyclerView(view)

        return view
    }

    private fun setUpRecyclerView(view: View) {
        recyclerFavourite = view.findViewById(R.id.recyclerFavourite)
        val backgroundList = FavouritesAsync(activity as Context).execute().get()
        if(backgroundList.isEmpty()) {
            progressLayout.visibility = View.GONE
            txtFav.visibility = View.VISIBLE
        }
        else {
            txtFav.visibility = View.GONE
            progressLayout.visibility = View.GONE
            for(i in backgroundList) {
                dbRestaurantList.add(
                    Restaurant(i.restaurant_id.toString(),
                        i.restaurantName,
                        i.restaurantRating,
                        i.restaurantPrice,
                        i.restaurantImage)
                )
            }
            val homeRecyclerAdapter = HomeRecyclerAdapter(activity as Context, dbRestaurantList)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerFavourite.layoutManager = mLayoutManager
            recyclerFavourite.adapter = homeRecyclerAdapter
            recyclerFavourite.setHasFixedSize(true)
        }
    }

    class FavouritesAsync(context: Context): AsyncTask<Void, Void, List<RestaurantEntity>>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants_db").build()
        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {
            return db.restaurantDao().getAllRestaurants()
        }
    }

}