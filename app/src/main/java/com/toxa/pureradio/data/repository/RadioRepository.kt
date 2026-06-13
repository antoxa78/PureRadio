package com.toxa.pureradio.data.repository

import com.toxa.pureradio.data.model.Station
import com.toxa.pureradio.network.Country
import com.toxa.pureradio.network.RadioBrowserDiscovery
import com.toxa.pureradio.network.RadioBrowserService
import com.toxa.pureradio.network.ServerStats
import com.toxa.pureradio.network.Tag
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RadioRepository {
    private var _service: RadioBrowserService? = null
    private val mutex = Mutex()

    private suspend fun getService(): RadioBrowserService {
        val currentService = _service
        if (currentService != null) return currentService
        
        return mutex.withLock {
            _service ?: run {
                val baseUrl = RadioBrowserDiscovery.getBaseUrl()
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val newService = retrofit.create(RadioBrowserService::class.java)
                _service = newService
                newService
            }
        }
    }

    suspend fun getTopStations(limit: Int = 100, hideBroken: Boolean = false): List<Station> {
        return try {
            getService().getTopStations(limit = limit, hideBroken = hideBroken)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchStations(
        query: String? = null,
        tag: String? = null,
        country: String? = null,
        limit: Int = 100,
        offset: Int = 0,
        hideBroken: Boolean = false
    ): List<Station> {
        return try {
            val service = getService()
            val results = service.searchStations(
                name = query,
                tag = tag,
                country = country,
                limit = limit,
                offset = offset,
                hideBroken = hideBroken
            )
            
            val combined = if (query != null && tag == null) {
                val byTag = service.searchStations(
                    tag = query,
                    country = country,
                    limit = limit,
                    offset = offset,
                    hideBroken = hideBroken
                )
                (results + byTag).distinctBy { it.stationUuid }
            } else if (tag != null && query == null) {
                val byName = service.searchStations(
                    name = tag,
                    country = country,
                    limit = limit,
                    offset = offset,
                    hideBroken = hideBroken
                )
                (results + byName).distinctBy { it.stationUuid }
            } else {
                results
            }
            
            combined
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStats(): ServerStats? {
        return try {
            getService().getStats()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTags(limit: Int = 500): List<Tag> {
        return try {
            getService().getTags(limit = limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCountries(): List<Country> {
        return try {
            getService().getCountries()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStationsByUuid(uuids: String): List<Station> {
        return try {
            getService().getStationsByUuid(uuids)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
