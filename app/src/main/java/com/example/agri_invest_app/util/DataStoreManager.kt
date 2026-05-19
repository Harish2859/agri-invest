package com.example.agri_invest_app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val VERIFIED_KEY = booleanPreferencesKey("is_verified")
    }

    suspend fun saveAuthData(token: String, role: String, verified: Boolean) {
        context.dataStore.edit { pref ->
            pref[TOKEN_KEY] = token
            pref[ROLE_KEY] = role
            pref[VERIFIED_KEY] = verified
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val roleFlow: Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }
    val verifiedFlow: Flow<Boolean> = context.dataStore.data.map { it[VERIFIED_KEY] ?: false }

    suspend fun getToken(): String? = context.dataStore.data.map { it[TOKEN_KEY] }.first()
    suspend fun getRole(): String? = context.dataStore.data.map { it[ROLE_KEY] }.first()
    suspend fun isVerified(): Boolean = context.dataStore.data.map { it[VERIFIED_KEY] ?: false }.first()
    suspend fun hasToken(): Boolean = getToken() != null

    suspend fun clearAuthData() {
        context.dataStore.edit { pref ->
            pref.remove(TOKEN_KEY)
            pref.remove(ROLE_KEY)
            pref.remove(VERIFIED_KEY)
        }
    }
}
