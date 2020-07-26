package com.example.foodhub.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.foodhub.R
import com.example.foodhub.model.FoodItem

class RestaurantMenuAdapter(val context: Context,
                            var menuList: ArrayList<FoodItem>,
                            var listener: OnItemClickListener):
    RecyclerView.Adapter<RestaurantMenuAdapter.MenuViewHolder>() {

    companion object {
        var isCartEmpty = true
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_menu_single_row, parent, false)
        return MenuViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    interface OnItemClickListener {
        fun onAddItemClick(foodItem: FoodItem)
        fun onRemoveItemClick(foodItem: FoodItem)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuObject = menuList[position]
        holder.foodItemName.text = menuObject.name
        val cost = "Rs. ${menuObject.cost_for_one}"
        holder.foodItemCost.text = cost
        holder.sno.text = (position + 1).toString()
        holder.removeFromCart.visibility = View.GONE

        // to resolve the problem caused by scrolling of recycleView
        if(itemCount>12) {
            if(position <= (itemCount-13) && holder.addToCart.visibility == View.GONE) {
                holder.removeFromCart.visibility = View.VISIBLE
            }
        }
        if(position>9 && holder.addToCart.visibility == View.GONE) {
            holder.removeFromCart.visibility = View.VISIBLE
        }

        holder.addToCart.setOnClickListener {
            holder.addToCart.visibility = View.GONE
            holder.removeFromCart.visibility = View.VISIBLE
            listener.onAddItemClick(menuObject)
        }
        holder.removeFromCart.setOnClickListener {
            holder.addToCart.visibility = View.VISIBLE
            holder.removeFromCart.visibility = View.GONE
            listener.onRemoveItemClick(menuObject)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MenuViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val foodItemName: TextView = view.findViewById(R.id.txtItemName)
        val foodItemCost: TextView = view.findViewById(R.id.txtItemCost)
        val sno: TextView = view.findViewById(R.id.txtItemSno)
        val addToCart: Button = view.findViewById(R.id.btnAddToCart)
        val removeFromCart: Button = view.findViewById(R.id.btnRemoveFromCart)
    }
}