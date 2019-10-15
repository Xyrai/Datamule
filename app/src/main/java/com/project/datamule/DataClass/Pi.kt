package com.project.datamule.DataClass

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pi(
    var name: String,
    var device: BluetoothDevice
) : Parcelable {
    companion object {
        val PI_S = arrayOf(
            "Raspberry Pi Alfa",
            "Raspberry Pi Bravo",
            "Raspberry Pi Charlie"
        )
    }
}