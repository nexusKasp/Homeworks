package com.example.hw3.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat

class LocationHelper(private val context: Context) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    /** Получает текущие координаты. В onResult придёт null, если определить их не удалось. */
    fun requestCurrentLocation(onResult: (Location?) -> Unit) {
        val providers = PROVIDERS.filter { locationManager.isProviderEnabled(it) }
        request(providers, onResult)
    }

    @SuppressLint("MissingPermission")
    private fun request(providers: List<String>, onResult: (Location?) -> Unit) {
        val provider = providers.firstOrNull()
        if (provider == null || !hasPermission()) {
            onResult(null)
            return
        }

        val onLocation: (Location?) -> Unit = { location ->
            if (location != null) {
                onResult(location)
            } else {
                request(providers.drop(1), onResult)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                provider,
                null,
                ContextCompat.getMainExecutor(context),
            ) { location -> onLocation(location) }
        } else {
            requestSingleUpdateLegacy(provider, onLocation)
        }
    }

    /** Для Android ниже 11, где нет getCurrentLocation. */
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun requestSingleUpdateLegacy(provider: String, onLocation: (Location?) -> Unit) {
        locationManager.requestSingleUpdate(
            provider,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocation(location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) = Unit
            },
            Looper.getMainLooper(),
        )
    }

    private companion object {
        val PROVIDERS = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
        )
    }
}