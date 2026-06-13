package com.toxa.pureradio.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object RadioBrowserDiscovery {
    private const val DISCOVERY_URL = "all.api.radio-browser.info"
    private var currentBaseUrl: String? = null

    suspend fun getBaseUrl(): String = withContext(Dispatchers.IO) {
        currentBaseUrl?.let { return@withContext it }

        val baseUrl = try {
            val addresses = InetAddress.getAllByName(DISCOVERY_URL)
            if (addresses.isNotEmpty()) {
                val randomAddress = addresses.random()
                // The official approach recommends a reverse DNS lookup to get the hostname
                // for SSL/TLS compatibility.
                val hostname = randomAddress.canonicalHostName
                "https://$hostname/"
            } else {
                "https://de1.api.radio-browser.info/" // Fallback
            }
        } catch (e: Exception) {
            "https://de1.api.radio-browser.info/" // Fallback
        }

        currentBaseUrl = baseUrl
        baseUrl
    }

    fun reset() {
        currentBaseUrl = null
    }
}
