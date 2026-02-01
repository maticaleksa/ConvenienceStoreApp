package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.CategoryRepository
import com.aleksa.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCategoryRepository : CategoryRepository {
    val categories = MutableStateFlow<List<Category>>(emptyList())

    override fun observeAll(): Flow<List<Category>> = categories
}
