package com.project.datamule

class Constants {
    companion object {
        val REQUEST_ENABLE_BT = 9001
        val REQUEST_PHONE_CALL = 9002
        val REQUEST_ACCESS_LOCATION = 9003

        // Notifications
        val CHANNEL_ID = "DataMule"
        val CHANNEL_NAME = "data transport"

        val DATAMULE_PHONE_NUMBER = "+31612345678"
        val DATAMULE_EMAIL = "datamule@atos.com"

        // Title
        val ONE_NEARBY_PI = 1

        // Bluetooth connecting
        val DEVICE_CONNECTED = 1
        val DEVICE_COULD_NOT_CONNECT = -1
        val DEVICE_STILL_CONNECTING = 0

        val PI_PREFIX_NAME = "dean"

    }
}