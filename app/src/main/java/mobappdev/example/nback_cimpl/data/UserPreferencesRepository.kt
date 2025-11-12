package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository (
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val NROFTURNS = intPreferencesKey("nrofturns")
        val TIMEINTERVAL = intPreferencesKey("timeinterval")
        val NBACK = intPreferencesKey("nback")
        val GRIDSIZE = intPreferencesKey("gridsize")
        val NROFLETTERS = intPreferencesKey("nrofletters")

        const val TAG = "UserPreferencesRepo"
    }

    val highscore: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[HIGHSCORE] ?: 0
        }
    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }

    val nrofturns: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[NROFTURNS] ?: 10
        }
    suspend fun saveNrOfTurns(nrOfTurns: Int) {
        dataStore.edit { preferences ->
            preferences[NROFTURNS] = nrOfTurns
        }
        NROFTURNS
    }

    val timeinterval: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[TIMEINTERVAL] ?: 2
        }
    suspend fun saveTimeInterval(timeInterval: Int) {
        dataStore.edit { preferences ->
            preferences[TIMEINTERVAL] = timeInterval
        }
    }

    val nback: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[NBACK] ?: 2
        }
    suspend fun saveNBack(nBack: Int) {
        dataStore.edit { preferences ->
            preferences[NBACK] = nBack
        }
    }

    val gridsize: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[GRIDSIZE] ?: 3
        }
    suspend fun saveGridSize(gridSize: Int) {
        dataStore.edit { preferences ->
            preferences[GRIDSIZE] = gridSize
        }
    }

    val nrofletters: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[NROFLETTERS] ?: 9
        }
    suspend fun saveNrOfLetters(nrOfLetters: Int) {
        dataStore.edit { preferences ->
            preferences[NROFLETTERS] = nrOfLetters
        }
    }
}