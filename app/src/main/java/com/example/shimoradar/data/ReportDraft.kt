package com.example.shimoradar.data

import android.location.Location

/** Holds the details captured on the Scan screen until the form is submitted. */
object ReportDraft {
    var severity: String = "Medium"
    var latitude: Double? = null
    var longitude: Double? = null
    var address: String? = null

    fun set(severity: String, location: Location?, address: String?) {
        this.severity = severity
        latitude = location?.latitude
        longitude = location?.longitude
        this.address = address
    }

    fun locationText(): String {
        address?.let { return it }
        val lat = latitude
        val lng = longitude
        return if (lat != null && lng != null) "%.5f, %.5f".format(lat, lng) else "Location unavailable"
    }

    fun clear() {
        severity = "Medium"
        latitude = null
        longitude = null
        address = null
    }
}