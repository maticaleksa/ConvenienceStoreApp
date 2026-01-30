package com.aleksa.domain.event

import com.aleksa.core.arch.event.DataCommand

sealed interface TransactionDataCommand : DataCommand {
    data object RefreshAll : TransactionDataCommand
}
