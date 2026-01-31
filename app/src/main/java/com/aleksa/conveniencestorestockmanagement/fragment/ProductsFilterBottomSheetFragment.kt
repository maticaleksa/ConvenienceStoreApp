package com.aleksa.conveniencestorestockmanagement.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.aleksa.conveniencestorestockmanagement.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class ProductsFilterBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val RESULT_KEY = "products_filter_result"
        const val RESULT_SELECTED_IDS = "products_filter_selected_ids"
        private const val ARG_CATEGORY_IDS = "arg_category_ids"
        private const val ARG_CATEGORY_NAMES = "arg_category_names"
        private const val ARG_SELECTED_IDS = "arg_selected_ids"

        fun newInstance(
            categoryIds: List<String>,
            categoryNames: List<String>,
            selectedIds: Set<String>,
        ): ProductsFilterBottomSheetFragment {
            return ProductsFilterBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_CATEGORY_IDS, ArrayList(categoryIds))
                    putStringArrayList(ARG_CATEGORY_NAMES, ArrayList(categoryNames))
                    putStringArrayList(ARG_SELECTED_IDS, ArrayList(selectedIds))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.dialog_products_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ids = arguments?.getStringArrayList(ARG_CATEGORY_IDS).orEmpty()
        val names = arguments?.getStringArrayList(ARG_CATEGORY_NAMES).orEmpty()
        val selected = arguments?.getStringArrayList(ARG_SELECTED_IDS)?.toSet().orEmpty()

        val listView = view.findViewById<ListView>(R.id.products_filter_list)
        val applyButton = view.findViewById<MaterialButton>(R.id.products_filter_apply)
        val clearButton = view.findViewById<MaterialButton>(R.id.products_filter_clear)

        listView.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, names)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        ids.forEachIndexed { index, id ->
            listView.setItemChecked(index, id in selected)
        }

        applyButton.setOnClickListener {
            val selectedIds = mutableListOf<String>()
            ids.forEachIndexed { index, id ->
                if (listView.isItemChecked(index)) {
                    selectedIds.add(id)
                }
            }
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                Bundle().apply {
                    putStringArrayList(RESULT_SELECTED_IDS, ArrayList(selectedIds))
                },
            )
            dismiss()
        }

        clearButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                Bundle().apply {
                    putStringArrayList(RESULT_SELECTED_IDS, arrayListOf())
                },
            )
            dismiss()
        }
    }
}
