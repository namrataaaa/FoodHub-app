package com.example.foodhub.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.foodhub.R
import com.example.foodhub.database_fav_res.RestaurantDatabase
import com.example.foodhub.database_fav_res.RestaurantEntity
import com.example.foodhub.fragment.RestaurantFragment
import com.example.foodhub.model.Restaurant
import com.squareup.picasso.Picasso


class HomeRecyclerAdapter(val context: Context, private val itemList: ArrayList<Restaurant>): RecyclerView.Adapter<HomeRecyclerAdapter.HomeRecyclerViewHolder>() {

    class HomeRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val restaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val restaurantPrice: TextView = view.findViewById(R.id.txtRestaurantPrice)
        val restaurantRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val restaurantImage: ImageView =  view.findViewById(R.id.imgRestaurantImage)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
        val imgFavButton: ImageView = view.findViewById(R.id.imgFavButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row, parent, false)
        return HomeRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        val restaurant = itemList[position]
        holder.restaurantName.text = restaurant.restaurantName
        holder.restaurantPrice.text = restaurant.restaurantPrice
        holder.restaurantRating.text = restaurant.restaurantRating
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.restaurant_default).into(holder.restaurantImage)

        holder.llContent.setOnClickListener {
            //open list of food items of restaurant
            val fragment = RestaurantFragment()
            val args = Bundle()
            args.putInt("resId", restaurant.restaurantId.toInt())
            args.putString("resName", restaurant.restaurantName)
            fragment.arguments = args
            val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, fragment)
            transaction.commit()
            (context as AppCompatActivity).supportActionBar?.title = holder.restaurantName.text.toString()
//            context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        val listOfFav = GetAllFavAsyncTask(context).execute().get()
        if(listOfFav.isNotEmpty() && listOfFav.contains(restaurant.restaurantId)){
            holder.imgFavButton.setImageResource(R.drawable.ic_favourite_checked)
        }
        else {
            holder.imgFavButton.setImageResource(R.drawable.ic_favourite_border)
        }
        holder.imgFavButton.setOnClickListener {
            //save the restaurant to favourites
            val restaurantEntity = RestaurantEntity(
                restaurant.restaurantId.toInt(),
                restaurant.restaurantName,
                restaurant.restaurantPrice,
                restaurant.restaurantRating,
                restaurant.restaurantImage)

            if (!DBAsyncTask( context, restaurantEntity, 1 ).execute().get()) {
                val async = DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(
                        this.context,
                        "Restaurant has been added to favourites",
                        Toast.LENGTH_SHORT
                    ).show()
                    holder.imgFavButton.setImageResource(R.drawable.ic_favourite_checked)
                }
            }
                else {
                    val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                    val result = async.get()
                    if(result) {
                        Toast.makeText(
                            this.context,
                            "Restaurant has been removed from favourites",
                            Toast.LENGTH_SHORT
                        ).show()
                        holder.imgFavButton.setImageResource(R.drawable.ic_favourite_border)
                    }
                }
        }
    }

    class DBAsyncTask(val context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int): AsyncTask<Void, Void, Boolean>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants_db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            when(mode) {
                1 -> {
                    // check if fav or not
                    val restaurant:RestaurantEntity?
                            = db.restaurantDao().getRestaurantById(restaurantEntity.restaurant_id.toString())
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    // add to fav
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    // delete from fav
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
    class GetAllFavAsyncTask(context: Context): AsyncTask<Void, Void, List<String>>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants_db").build()
        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.restaurantDao().getAllRestaurants()
            val listOfIds = arrayListOf<String>()
            for(i in list) {
                listOfIds.add(i.restaurant_id.toString())
            }
            db.close()
            return listOfIds
        }
    }
}