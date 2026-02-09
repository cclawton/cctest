package com.raincast.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient? = try {
        LocationServices.getFusedLocationProviderClient(context)
    } catch (e: Exception) {
        null
    }

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        return try {
            suspendCancellableCoroutine { cont ->
                try {
                    fusedClient?.lastLocation
                        ?.addOnSuccessListener { location ->
                            cont.resume(location)
                        }
                        ?.addOnFailureListener {
                            // Try fallback
                            val fallback = getLocationManagerFallback()
                            cont.resume(fallback)
                        } ?: cont.resume(getLocationManagerFallback())
                } catch (e: SecurityException) {
                    cont.resume(null)
                }
            }
        } catch (e: Exception) {
            getLocationManagerFallback()
        }
    }

    @Suppress("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 300000): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        try {
            fusedClient?.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            close()
        }

        awaitClose {
            fusedClient?.removeLocationUpdates(callback)
        }
    }

    @Suppress("MissingPermission")
    private fun getLocationManagerFallback(): Location? {
        if (!hasLocationPermission()) return null
        return try {
            locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }
    }
}
