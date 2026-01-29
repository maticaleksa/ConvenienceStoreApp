package com.aleksa.conveniencestorestockmanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TransactionsAdapter :
    ListAdapter<Transaction, TransactionsAdapter.TransactionViewHolder>(TransactionDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeView: TextView = itemView.findViewById(R.id.transaction_type)
        private val productView: TextView = itemView.findViewById(R.id.transaction_product)
        private val quantityView: TextView = itemView.findViewById(R.id.transaction_quantity)
        private val dateView: TextView = itemView.findViewById(R.id.transaction_date)
        private val notesView: TextView = itemView.findViewById(R.id.transaction_notes)

        fun bind(transaction: Transaction) {
            typeView.text = when (transaction.type) {
                TransactionType.RESTOCK -> itemView.context.getString(R.string.transaction_type_restock)
                TransactionType.SALE -> itemView.context.getString(R.string.transaction_type_sale)
            }
            productView.text = itemView.context.getString(
                R.string.transaction_product_format,
                transaction.productId
            )
            quantityView.text = itemView.context.getString(
                R.string.transaction_quantity_format,
                transaction.quantity
            )
            val localDateTime = transaction.date.toLocalDateTime(TimeZone.currentSystemDefault())
            dateView.text = itemView.context.getString(
                R.string.transaction_date_format,
                "%04d-%02d-%02d %02d:%02d".format(
                    localDateTime.year,
                    localDateTime.monthNumber,
                    localDateTime.dayOfMonth,
                    localDateTime.hour,
                    localDateTime.minute
                )
            )
            notesView.text = itemView.context.getString(
                R.string.transaction_notes_format,
                transaction.notes ?: "-"
            )
        }
    }

    private object TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
