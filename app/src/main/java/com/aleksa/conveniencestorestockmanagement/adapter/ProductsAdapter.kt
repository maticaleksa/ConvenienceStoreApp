package com.aleksa.conveniencestorestockmanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.domain.model.Product

class ProductsAdapter(
    private val onItemClick: (Product) -> Unit = {}
) : ListAdapter<Product, ProductsAdapter.ProductViewHolder>(ProductDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.product_name)
        private val categoryView: TextView = itemView.findViewById(R.id.product_category)
        private val stockView: TextView = itemView.findViewById(R.id.product_stock)
        private val priceView: TextView = itemView.findViewById(R.id.product_price)

        fun bind(product: Product, onItemClick: (Product) -> Unit) {
            nameView.text = product.name
            categoryView.text = product.category.name
            stockView.text = itemView.context.resources.getQuantityString(
                R.plurals.product_stock_format,
                product.currentStockLevel,
                product.currentStockLevel
            )
            priceView.text = itemView.context.getString(
                R.string.product_price_format,
                product.price.toDecimalString()
            )
            itemView.setOnClickListener { onItemClick(product) }
        }
    }

    private object ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
