package com.project.datamule.model

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pi(
    var name: String,
    var device: BluetoothDevice
) : Parcelable