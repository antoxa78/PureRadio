package com.toxa.pureradio.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object RadioBrowserDiscovery {
    private const val DISCOVERY_URL = "all.api.radio-browser.info"
    private const val FALLBACK_URL = "https://de1.api.radio-browser.info/"
    @Volatile
    private var currentBaseUrl: String? = null

    suspend fun getBaseUrl(): String = withContext(Dispatchers.IO) {
        currentBaseUrl?.let { return@withContext it }

        val baseUrl = try {
            val addresses = InetAddress.getAllByName(DISCOVERY_URL)
            if (addresses.isNotEmpty()) {
                val randomAddress = addresses.random()
                // PTR reverse-lookup gives us the real hostname for SSL compatibility.
                // If PTR isn't set, canonicalHostName returns the raw IP — fall back rather
                // than sending an SSL request to a numeric address (cert validation fails).
                val hostname = randomAddress.canonicalHostName
                val isRawIp = hostname == randomAddress.hostAddress
                if (isRawIp) {
                    FALLBACK_URL
                } else {
                    "https://$hostname/"
                }
            } else {
                FALLBACK_URL
            }
        } catch (e: Exception) {
            FALLBACK_URL
        }

        currentBaseUrl = baseUrl
        baseUrl
    }

    fun reset() {
        currentBaseUrl = null
    }
}
