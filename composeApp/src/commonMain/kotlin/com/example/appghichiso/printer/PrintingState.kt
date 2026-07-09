package com.example.appghichiso.printer

/**
 * Trạng thái của luồng in (quan sát bởi [com.example.appghichiso.presentation.printer.PrintHost]).
 */
sealed interface PrintingState {
    data object Idle : PrintingState
    data object NeedPermission : PrintingState
    data object Connecting : PrintingState
    data object Printing : PrintingState
    data object Success : PrintingState
    data class Error(val message: String) : PrintingState
}
