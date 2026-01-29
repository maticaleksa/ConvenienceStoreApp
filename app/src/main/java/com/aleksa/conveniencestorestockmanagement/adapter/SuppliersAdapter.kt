package com.aleksa.conveniencestorestockmanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.domain.model.Supplier

class SuppliersAdapter :
    ListAdapter<Supplier, SuppliersAdapter.SupplierViewHolder>(SupplierDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_supplier, parent, false)
        return SupplierViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SupplierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.supplier_name)
        private val contactView: TextView = itemView.findViewById(R.id.supplier_contact)
        private val phoneView: TextView = itemView.findViewById(R.id.supplier_phone)
        private val emailView: TextView = itemView.findViewById(R.id.supplier_email)
        private val addressView: TextView = itemView.findViewById(R.id.supplier_address)

        fun bind(supplier: Supplier) {
            nameView.text = supplier.name
            contactView.text = supplier.contactPerson
            phoneView.text = supplier.phone
            emailView.text = supplier.email
            addressView.text = supplier.address
        }
    }

    private object SupplierDiffCallback : DiffUtil.ItemCallback<Supplier>() {
        override fun areItemsTheSame(oldItem: Supplier, newItem: Supplier): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Supplier, newItem: Supplier): Boolean {
            return oldItem == newItem
        }
    }
}
