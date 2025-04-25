package com.example.myapplication.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Объект для управления токеном авторизации и состоянием первого запуска.
 *
 * Использует [EncryptedSharedPreferences] для безопасного хранения данных.
 */
object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    /**
     * Сохраняет токен авторизации в зашифрованное хранилище.
     *
     * @param context Контекст приложения.
     * @param token Токен авторизации.
     */
    fun saveToken(context: Context, token: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    /**
     * Получает токен авторизации из зашифрованного хранилища.
     *
     * @param context Контекст приложения.
     * @return Токен авторизации или `null`, если токен отсутствует.
     */
    fun getToken(context: Context): String? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    /**
     * Удаляет токен авторизации из зашифрованного хранилища.
     *
     * @param context Контекст приложения.
     */
    fun clearToken(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
    }

    /**
     * Проверяет, является ли запуск приложения первым.
     *
     * Если это первый запуск, флаг сбрасывается после проверки.
     *
     * @param context Контекст приложения.
     * @return `true`, если это первый запуск, иначе `false`.
     */
    fun isFirstLaunch(context: Context): Boolean {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val isFirstLaunch = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
        return isFirstLaunch
    }
}