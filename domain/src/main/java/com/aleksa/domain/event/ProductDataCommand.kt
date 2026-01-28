package com.aleksa.domain.event

import com.aleksa.core.arch.event.DataCommand

sealed interface ProductDataCommand : DataCommand {
    data object RefreshAll : ProductDataCommand
}
