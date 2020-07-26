package com.example.foodhub.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodhub.R
import com.example.foodhub.model.FoodItem

class CartItemAdapter(private val cartList: ArrayList<FoodItem>, val context: Context):
    RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_cart_single_row, parent, false)
        return CartViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartObject = cartList[position]
        holder.itemName.text = cartObject.name
        val cost = "Rs. ${cartObject.cost_for_one}"
        holder.itemCost.text = cost
    }

    class CartViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.txtItemName)
        val itemCost: TextView = view.findViewById(R.id.txtItemCost)
    }
}