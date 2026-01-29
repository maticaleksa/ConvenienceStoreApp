package com.aleksa.conveniencestorestockmanagement.uistate

data class SupplierEditUiState(
    val supplierId: String,
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
)
