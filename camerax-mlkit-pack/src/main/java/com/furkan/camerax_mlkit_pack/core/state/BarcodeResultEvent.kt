package com.furkan.camerax_mlkit_pack.core.state

sealed class BarcodeResultEvent {
    data class SuccessEvent(val barcodeResult: String) : BarcodeResultEvent()
}