package com.example.shimoradar.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint("MissingPermission")
fun fetchLocation(context: Context, onResult: (Location?) -> Unit) {
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { onResult(it) }
        .addOnFailureListener { onResult(null) }
}

@Suppress("DEPRECATION")
suspend fun lookupAddress(context: Context, latitude: Double, longitude: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            val address = Geocoder(context, Locale.getDefault())
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?: return@withContext null
            listOfNotNull(
                address.thoroughfare ?: address.featureName,
                address.subLocality ?: address.locality
            )
                .distinct()
                .joinToString(", ")
                .ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }