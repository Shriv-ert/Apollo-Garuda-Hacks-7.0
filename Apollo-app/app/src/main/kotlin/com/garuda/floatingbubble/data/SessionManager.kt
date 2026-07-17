package com.garuda.floatingbubble.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_FULL_NAME = "user_full_name"
    private const val KEY_ROLE = "user_role"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(
        context: Context,
        token: String,
        userId: Int,
        email: String,
        fullName: String,
        role: String
    ) {
        prefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_ROLE, role)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun clearSession(context: Context) {
        prefs(context).edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_FULL_NAME)
            .remove(KEY_ROLE)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean("overlay_asked", false)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun getToken(context: Context): String? =
        prefs(context).getString(KEY_TOKEN, null)

    fun getUserId(context: Context): Int =
        prefs(context).getInt(KEY_USER_ID, -1)

    fun getEmail(context: Context): String =
        prefs(context).getString(KEY_EMAIL, "") ?: ""

    fun getFullName(context: Context): String =
        prefs(context).getString(KEY_FULL_NAME, "") ?: ""

    fun getRole(context: Context): String =
        prefs(context).getString(KEY_ROLE, "user") ?: "user"

    fun isAdmin(context: Context): Boolean = getRole(context) == "admin"

    fun bearerToken(context: Context): String = "Bearer ${getToken(context)}"
}
