package com.raincast.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class GeocodeHelper(private val context: Context) {

    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            cont.resume(formatAddress(addresses.firstOrNull()))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    formatAddress(addresses?.firstOrNull())
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun searchLocations(query: String): List<Address> {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocationName(query, 5) { addresses ->
                            cont.resume(addresses)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 5) ?: emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun formatAddress(address: Address?): String? {
        if (address == null) return null
        return listOfNotNull(
            address.locality ?: address.subLocality,
            address.adminArea
        ).joinToString(", ").ifEmpty {
            address.getAddressLine(0)
        }
    }
}
