package com.example.myapplication.diary

import android.content.Context
import com.example.myapplication.utils.TokenManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Модель данных для записи дневника.
 *
 * @param date Дата записи.
 * @param name Название блюда.
 * @param calories Калории.
 * @param proteins Белки.
 * @param fats Жиры.
 * @param carbs Углеводы.
 * @param mealType Тип приёма пищи (например, "Завтрак").
 * @param isCustom Является ли блюдо пользовательским.
 * @param recipeId ID рецепта (опционально).
 */
@Serializable
data class DiaryEntry(
    val date: String,
    val name: String,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
    val mealType: String,
    val isCustom: Boolean,
    val recipeId: Int? = null
)

/**
 * Модель данных для ежедневной статистики КБЖУ.
 *
 * @param date Дата.
 * @param totalCalories Общие калории.
 * @param totalProteins Общие белки.
 * @param totalFats Общие жиры.
 * @param totalCarbs Общие углеводы.
 */
@Serializable
data class DailyStats(
    val date: String,
    val totalCalories: Double,
    val totalProteins: Double,
    val totalFats: Double,
    val totalCarbs: Double
)

/**
 * Объект для управления хранением записей дневника и статистики.
 *
 * Предоставляет методы для сохранения, загрузки и управления записями и статистикой КБЖУ.
 */
object DiaryStorage {
    private const val ENTRIES_FILE_BASE_NAME = "diary_entries"
    private const val STATS_FILE_BASE_NAME = "daily_stats"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Генерирует имя файла на основе токена пользователя.
     *
     * @param baseName Базовое имя файла.
     * @param context Контекст приложения.
     * @return Имя файла с добавленным токеном.
     */
    private fun getUserFileName(baseName: String, context: Context): String {
        val token = TokenManager.getToken(context) ?: "default_user"
        val sanitizedToken = token.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return "${baseName}_${sanitizedToken}.json"
    }

    /**
     * Возвращает файл для хранения записей дневника.
     *
     * @param context Контекст приложения.
     * @return Файл для записей.
     */
    private fun getEntriesFile(context: Context): File {
        val fileName = getUserFileName(ENTRIES_FILE_BASE_NAME, context)
        return File(context.filesDir, fileName)
    }

    /**
     * Возвращает файл для хранения статистики.
     *
     * @param context Контекст приложения.
     * @return Файл для статистики.
     */
    private fun getStatsFile(context: Context): File {
        val fileName = getUserFileName(STATS_FILE_BASE_NAME, context)
        return File(context.filesDir, fileName)
    }

    /**
     * Сохраняет список записей дневника в JSON-файл.
     *
     * @param context Контекст приложения.
     * @param entries Список записей для сохранения.
     */
    fun saveEntries(context: Context, entries: List<DiaryEntry>) {
        val file = getEntriesFile(context)
        try {
            val jsonString = json.encodeToString(entries)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Загружает список записей дневника из JSON-файла.
     *
     * @param context Контекст приложения.
     * @return Список записей.
     */
    fun loadEntries(context: Context): List<DiaryEntry> {
        val file = getEntriesFile(context)
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            json.decodeFromString<List<DiaryEntry>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Сохраняет статистику КБЖУ в JSON-файл.
     *
     * @param context Контекст приложения.
     * @param stats Список статистики для сохранения.
     */
    private fun saveDailyStats(context: Context, stats: List<DailyStats>) {
        val file = getStatsFile(context)
        try {
            val jsonString = json.encodeToString(stats)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Загружает статистику КБЖУ из JSON-файла.
     *
     * @param context Контекст приложения.
     * @return Список статистики.
     */
    private fun loadDailyStats(context: Context): List<DailyStats> {
        val file = getStatsFile(context)
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            json.decodeFromString<List<DailyStats>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Рассчитывает статистику КБЖУ за указанную дату.
     *
     * @param entries Список записей дневника.
     * @param date Дата для расчёта.
     * @return Объект DailyStats с рассчитанной статистикой.
     */
    private fun calculateStatsForDate(entries: List<DiaryEntry>, date: String): DailyStats {
        val entriesForDate = entries.filter { it.date == date }
        val totalCalories = entriesForDate.sumOf { it.calories }
        val totalProteins = entriesForDate.sumOf { it.proteins }
        val totalFats = entriesForDate.sumOf { it.fats }
        val totalCarbs = entriesForDate.sumOf { it.carbs }
        return DailyStats(
            date = date,
            totalCalories = totalCalories,
            totalProteins = totalProteins,
            totalFats = totalFats,
            totalCarbs = totalCarbs
        )
    }

    /**
     * Обновляет статистику КБЖУ после изменения записей.
     *
     * @param context Контекст приложения.
     * @param entries Текущий список записей.
     */
    private fun updateDailyStats(context: Context, entries: List<DiaryEntry>) {
        val dates = entries.map { it.date }.distinct()
        val stats = dates.map { date ->
            calculateStatsForDate(entries, date)
        }
        saveDailyStats(context, stats)
    }

    /**
     * Добавляет новую запись в дневник и обновляет статистику.
     *
     * @param context Контекст приложения.
     * @param entry Новая запись для добавления.
     */
    fun addEntry(context: Context, entry: DiaryEntry) {
        val currentEntries = loadEntries(context).toMutableList()
        currentEntries.add(entry)
        saveEntries(context, currentEntries)
        updateDailyStats(context, currentEntries)
    }

    /**
     * Получает записи дневника за указанную дату.
     *
     * @param context Контекст приложения.
     * @param date Дата для фильтрации записей.
     * @return Список записей за указанную дату.
     */
    fun getEntriesForDate(context: Context, date: String): List<DiaryEntry> {
        return loadEntries(context).filter { it.date == date }
    }

    /**
     * Получает статистику КБЖУ за указанную дату.
     *
     * @param context Контекст приложения.
     * @param date Дата для получения статистики.
     * @return Объект DailyStats или null, если данных нет.
     */
    fun getStatsForDate(context: Context, date: String): DailyStats? {
        return loadDailyStats(context).find { it.date == date }
    }

    /**
     * Получает всю статистику КБЖУ.
     *
     * @param context Контекст приложения.
     * @return Список всей статистики.
     */
    fun getAllStats(context: Context): List<DailyStats> {
        return loadDailyStats(context)
    }
}