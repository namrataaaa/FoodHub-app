package com.example.foodhub.database_fav_res

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.foodhub.database_cart.OrderDao
import com.example.foodhub.database_cart.OrderEntity

@Database(entities = [RestaurantEntity::class, OrderEntity::class], version = 1)
abstract class RestaurantDatabase: RoomDatabase() {
    abstract fun restaurantDao():RestaurantDao
    abstract fun orderDao(): OrderDao
}