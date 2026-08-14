package com.ridecast.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ridecast.domain.model.FavoritePlace
import com.ridecast.domain.repository.FavoritePlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreFavoritePlacesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : FavoritePlacesRepository {

    companion object {
        private const val MAX_FAVORITES = 15
        val KEY_FAVORITE_PLACES = stringPreferencesKey("favorite_places")
    }

    private val gson = Gson()
    private val listType = object : TypeToken<List<FavoritePlace>>() {}.type

    override val favorites: Flow<List<FavoritePlace>> = dataStore.data
        .catch { e ->
            Timber.e(e, "Error reading favorite places")
            emit(emptyPreferences())
        }
        .map { prefs -> parseFavorites(prefs[KEY_FAVORITE_PLACES]) }

    override suspend fun addFavorite(place: FavoritePlace): Boolean {
        var added = false
        dataStore.edit { prefs ->
            val current = parseFavorites(prefs[KEY_FAVORITE_PLACES]).toMutableList()
            if (current.any { it.placeId == place.placeId }) return@edit
            if (current.size >= MAX_FAVORITES) return@edit
            current.add(place)
            prefs[KEY_FAVORITE_PLACES] = gson.toJson(current)
            added = true
        }
        return added
    }

    override suspend fun removeFavorite(placeId: String) {
        dataStore.edit { prefs ->
            val current = parseFavorites(prefs[KEY_FAVORITE_PLACES])
                .filterNot { it.placeId == placeId }
            prefs[KEY_FAVORITE_PLACES] = gson.toJson(current)
        }
    }

    override suspend fun isFavorite(placeId: String): Boolean =
        favorites.first().any { it.placeId == placeId }

    private fun parseFavorites(json: String?): List<FavoritePlace> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson<List<FavoritePlace>>(json, listType) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse favorite places JSON")
            emptyList()
        }
    }
}
