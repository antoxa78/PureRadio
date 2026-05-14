package com.toxa.pureradio.data.repository

import com.toxa.pureradio.data.model.Station
import com.toxa.pureradio.network.Country
import com.toxa.pureradio.network.RadioBrowserService
import com.toxa.pureradio.network.Tag
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RadioRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://de1.api.radio-browser.info/") // Using one of the mirrors
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(RadioBrowserService::class.java)

    suspend fun getTopStations(hideBroken: Boolean = false): List<Station> {
        return try {
            service.getTopStations(hideBroken = hideBroken)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchStations(
        query: String? = null,
        tag: String? = null,
        country: String? = null,
        limit: Int = 100,
        hideBroken: Boolean = false
    ): List<Station> {
        return try {
            val byName = service.searchStations(
                name = query,
                tag = tag,
                country = country,
                limit = limit,
                hideBroken = hideBroken
            )
            if (query != null && byName.size < 10 && tag == null) {
                val byTag = service.searchStations(
                    tag = query,
                    country = country,
                    limit = limit,
                    hideBroken = hideBroken
                )
                (byName + byTag).distinctBy { it.stationUuid }
            } else {
                byName
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStats(): com.toxa.pureradio.network.ServerStats? {
        return try {
            service.getStats()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTags(): List<Tag> {
        return try {
            service.getTags()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCountries(): List<Country> {
        return try {
            service.getCountries()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
