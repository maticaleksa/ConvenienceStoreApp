package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.aleksa.conveniencestorestockmanagement.R
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.TransactionDateFilter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class TransactionsFilterBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val RESULT_KEY = "transactions_filter_result"
        const val RESULT_SELECTED_TYPES = "transactions_filter_selected_types"
        const val RESULT_DATE_FILTER = "transactions_filter_date"
        private const val ARG_SELECTED_TYPES = "arg_selected_types"
        private const val ARG_DATE_FILTER = "arg_date_filter"

        fun newInstance(
            selectedTypes: Set<TransactionType>,
            dateFilter: TransactionDateFilter,
        ): TransactionsFilterBottomSheetFragment {
            return TransactionsFilterBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(
                        ARG_SELECTED_TYPES,
                        ArrayList(selectedTypes.map { it.name }),
                    )
                    putString(ARG_DATE_FILTER, dateFilter.name)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.dialog_transactions_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedTypes = arguments?.getStringArrayList(ARG_SELECTED_TYPES).orEmpty().toSet()
        val dateFilterName = arguments?.getString(ARG_DATE_FILTER) ?: TransactionDateFilter.ALL.name

        val types = listOf(TransactionType.RESTOCK, TransactionType.SALE)
        val typeLabels = types.map {
            if (it == TransactionType.RESTOCK) {
                getString(R.string.transaction_type_restock)
            } else {
                getString(R.string.transaction_type_sale)
            }
        }.toTypedArray()
        val dateOptions = listOf(
            TransactionDateFilter.ALL,
            TransactionDateFilter.LAST_7_DAYS,
            TransactionDateFilter.LAST_30_DAYS,
        )
        val dateLabels = arrayOf(
            getString(R.string.transactions_filter_date_all),
            getString(R.string.transactions_filter_date_7),
            getString(R.string.transactions_filter_date_30),
        )

        val typeList = view.findViewById<ListView>(R.id.transaction_filter_types)
        val dateList = view.findViewById<ListView>(R.id.transaction_filter_dates)
        val applyButton = view.findViewById<MaterialButton>(R.id.transaction_filter_apply)
        val clearButton = view.findViewById<MaterialButton>(R.id.transaction_filter_clear)

        typeList.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, typeLabels)
        typeList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        types.forEachIndexed { index, type ->
            typeList.setItemChecked(index, type.name in selectedTypes)
        }

        dateList.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, dateLabels)
        dateList.choiceMode = ListView.CHOICE_MODE_SINGLE
        val initialDate = dateOptions.indexOfFirst { it.name == dateFilterName }.coerceAtLeast(0)
        dateList.setItemChecked(initialDate, true)

        applyButton.setOnClickListener {
            val selected = mutableListOf<String>()
            types.forEachIndexed { index, type ->
                if (typeList.isItemChecked(index)) {
                    selected.add(type.name)
                }
            }
            val selectedDateIndex = dateList.checkedItemPosition.coerceAtLeast(0)
            val selectedDate = dateOptions[selectedDateIndex].name
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                Bundle().apply {
                    putStringArrayList(RESULT_SELECTED_TYPES, ArrayList(selected))
                    putString(RESULT_DATE_FILTER, selectedDate)
                },
            )
            dismiss()
        }

        clearButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                Bundle().apply {
                    putStringArrayList(RESULT_SELECTED_TYPES, arrayListOf())
                    putString(RESULT_DATE_FILTER, TransactionDateFilter.ALL.name)
                },
            )
            dismiss()
        }
    }
}
