package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.model.Supplier
import kotlinx.coroutines.flow.Flow

class RecordingSupplierRepository : FakeSupplierRepository() {
    val queries = mutableListOf<String>()

    override fun observeSearch(query: String): Flow<List<Supplier>> {
        queries.add(query)
        return super.observeSearch(query)
    }
}
