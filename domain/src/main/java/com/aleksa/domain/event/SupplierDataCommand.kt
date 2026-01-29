package com.aleksa.domain.event

import com.aleksa.core.arch.event.DataCommand

sealed interface SupplierDataCommand : DataCommand {
    data object RefreshAll : SupplierDataCommand
}
