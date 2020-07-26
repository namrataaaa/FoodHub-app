package com.example.foodhub.model

import com.google.gson.JsonArray
import org.json.JSONArray

data class OrderDetails(val orderId: Int, val resName: String, val orderDate: String, val foodItem: JSONArray) {
}