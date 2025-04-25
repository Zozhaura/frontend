package com.example.myapplication.food

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.utils.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Модель данных для краткой информации о рецепте.
 *
 * @param id Идентификатор рецепта.
 * @param name Название рецепта.
 * @param nutrition Пищевая ценность рецепта.
 */
@Serializable
data class RecipeShortDTO(
    val id: Int,
    val name: String,
    val nutrition: NutritionDTO
)

/**
 * Модель данных для пищевой ценности.
 *
 * @param calories Калории.
 * @param proteins Белки.
 * @param fats Жиры.
 * @param carbohydrates Углеводы.
 * @param dietaryFiber Пищевые волокна (опционально).
 * @param water Вода (опционально).
 */
@Serializable
data class NutritionDTO(
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbohydrates: Double,
    val dietaryFiber: Double? = null,
    val water: Double? = null
)

/**
 * Модель данных для полной информации о рецепте.
 *
 * @param id Идентификатор рецепта.
 * @param name Название рецепта.
 * @param preparationMethod Способ приготовления.
 * @param category Категория рецепта (опционально).
 * @param nutrition Пищевая ценность рецепта (опционально).
 * @param ingredients Список ингредиентов.
 */
@Serializable
data class FullRecipeDTO(
    val id: Int,
    val name: String,
    val preparationMethod: String,
    val category: String?,
    val nutrition: NutritionDTO?,
    val ingredients: List<IngredientDTO>
)

/**
 * Модель данных для ингредиента.
 *
 * @param name Название ингредиента.
 * @param quantity Количество ингредиента.
 */
@Serializable
data class IngredientDTO(val name: String, val quantity: String)

/**
 * Модель данных для записи в дневник.
 *
 * @param recipeId Идентификатор рецепта (опционально).
 * @param name Название блюда.
 * @param weight Вес порции (опционально).
 * @param mealType Тип приёма пищи (опционально).
 * @param nutrition Пищевая ценность.
 */
@Serializable
data class DiaryEntryDTO(
    val recipeId: Int? = null,
    val name: String,
    val weight: Double? = null,
    val mealType: String? = null,
    val nutrition: NutritionDTO
)

/**
 * Модель ответа от прокси для списка рецептов.
 *
 * @param message Сообщение от сервера.
 * @param proxyPath Путь прокси.
 * @param foodResponse Список рецептов.
 */
@Serializable
data class ProxyFoodResponse(
    val message: String,
    val proxyPath: String,
    val foodResponse: List<RecipeShortDTO>
)

/**
 * Модель ответа от прокси для одного рецепта.
 *
 * @param message Сообщение от сервера.
 * @param proxyPath Путь прокси.
 * @param foodResponse Полная информация о рецепте.
 */
@Serializable
data class ProxyRecipeResponse(
    val message: String,
    val proxyPath: String,
    val foodResponse: FullRecipeDTO
)

/**
 * Модель для обработки ошибок от прокси или сервиса.
 *
 * @param error Описание ошибки (опционально).
 * @param message Сообщение об ошибке (опционально).
 * @param details Детали ошибки (опционально).
 */
@Serializable
data class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val details: String? = null
)

/**
 * ViewModel для управления данными экрана выбора блюд.
 *
 * Отвечает за загрузку рецептов, поиск, добавление блюд в дневник и управление состоянием.
 */
class FoodViewModel : ViewModel() {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val _recommendedRecipes: MutableState<List<RecipeShortDTO>> = mutableStateOf(emptyList())
    val recommendedRecipes: List<RecipeShortDTO> get() = _recommendedRecipes.value

    private val _selectedRecipe: MutableState<FullRecipeDTO?> = mutableStateOf(null)
    val selectedRecipe: FullRecipeDTO? get() = _selectedRecipe.value

    private val _errorMessage: MutableState<String?> = mutableStateOf(null)
    val errorMessage: String? get() = _errorMessage.value

    private val _selectedDishes: MutableState<Map<RecipeShortDTO, Double>> = mutableStateOf(emptyMap())
    val selectedDishes: Map<RecipeShortDTO, Double> get() = _selectedDishes.value

    private val _isLoading: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading.value

    /**
     * Загружает рекомендованные рецепты.
     *
     * @param context Контекст приложения.
     */
    fun fetchRecommendedRecipes(context: Context) {
        val token = TokenManager.getToken(context)
        if (token == null) {
            _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val httpResponse = client.get("http://10.0.2.2:8080/food/recipes_recommendation") {
                    header("Authorization", "Bearer $token")
                    val url = this.url.buildString()
                    Log.d("FoodViewModel", "Запрос на рекомендации: $url")
                }
                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.d("FoodViewModel", "Полученный JSON (рекомендации): $responseText")
                        val proxyResponse = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString(ProxyFoodResponse.serializer(), responseText)
                        _recommendedRecipes.value = proxyResponse.foodResponse
                        _errorMessage.value = null
                    }
                    else -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $responseText")
                        try {
                            val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), responseText)
                            val errorMessage = errorResponse.message ?: errorResponse.error ?: errorResponse.details ?: "Неизвестная ошибка"
                            _errorMessage.value = "Ошибка загрузки рецептов: $errorMessage"
                        } catch (e: Exception) {
                            _errorMessage.value = "Ошибка загрузки рецептов: ${httpResponse.status} - $responseText"
                        }
                        if (httpResponse.status == HttpStatusCode.Unauthorized) {
                            TokenManager.clearToken(context)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки рецептов: ${e.message}"
                Log.e("FoodViewModel", "Исключение: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Выполняет поиск рецептов по заданным параметрам.
     *
     * @param context Контекст приложения.
     * @param query Поисковый запрос (опционально).
     * @param includeIngredients Список обязательных ингредиентов (опционально).
     * @param excludeIngredients Список исключаемых ингредиентов (опционально).
     * @param category Категория рецепта (опционально).
     */
    fun searchRecipes(
        context: Context,
        query: String?,
        includeIngredients: List<String>?,
        excludeIngredients: List<String>?,
        category: String?
    ) {
        val token = TokenManager.getToken(context)
        if (token == null) {
            _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val httpResponse = client.get("http://10.0.2.2:8080/food/recipes_search") {
                    header("Authorization", "Bearer $token")
                    url {
                        query?.let { parameters.append("name", it) }
                        includeIngredients?.map { it.lowercase() }?.forEach {
                            parameters.append("includeIngredients", it)
                        }
                        excludeIngredients?.map { it.lowercase() }?.forEach {
                            parameters.append("excludeIngredients", it)
                        }
                        category?.let { parameters.append("category", it) }
                    }
                    Log.d("FoodViewModel", "🔍 URL запроса поиска: ${this.url.buildString()}")
                }

                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.d("FoodViewModel", "Полученный JSON (поиск): $responseText")
                        val proxyResponse = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString(ProxyFoodResponse.serializer(), responseText)
                        _recommendedRecipes.value = proxyResponse.foodResponse
                        _errorMessage.value = null
                    }
                    else -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $responseText")
                        try {
                            val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), responseText)
                            val errorMessage = errorResponse.message ?: errorResponse.error ?: errorResponse.details ?: "Неизвестная ошибка"
                            _errorMessage.value = "Ошибка поиска рецептов: $errorMessage"
                        } catch (e: Exception) {
                            _errorMessage.value = "Ошибка поиска рецептов: ${httpResponse.status} - $responseText"
                        }
                        if (httpResponse.status == HttpStatusCode.Unauthorized) {
                            TokenManager.clearToken(context)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка поиска рецептов: ${e.message}"
                Log.e("FoodViewModel", "Исключение: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Загружает детальную информацию о рецепте.
     *
     * @param context Контекст приложения.
     * @param recipeId Идентификатор рецепта.
     */
    fun fetchRecipeDetails(context: Context, recipeId: Int) {
        val token = TokenManager.getToken(context)
        if (token == null) {
            _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val httpResponse = client.get("http://10.0.2.2:8080/food/recipe") {
                    header("Authorization", "Bearer $token")
                    parameter("id", recipeId)
                    val url = this.url.buildString()
                    Log.d("FoodViewModel", "Запрос на детали рецепта: $url")
                }

                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.d("FoodViewModel", "Полученный JSON (детали): $responseText")
                        val proxyResponse = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString(ProxyRecipeResponse.serializer(), responseText)
                        _selectedRecipe.value = proxyResponse.foodResponse
                        _errorMessage.value = null
                        Log.d("FoodViewModel", "Детали рецепта загружены: ${proxyResponse.foodResponse}")
                    }
                    else -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $responseText")
                        try {
                            val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), responseText)
                            val errorMessage = errorResponse.message ?: errorResponse.error ?: errorResponse.details ?: "Неизвестная ошибка"
                            _errorMessage.value = "Ошибка загрузки деталей рецепта: $errorMessage"
                        } catch (e: Exception) {
                            _errorMessage.value = "Ошибка загрузки деталей рецепта: ${httpResponse.status} - $responseText"
                        }
                        if (httpResponse.status == HttpStatusCode.Unauthorized) {
                            TokenManager.clearToken(context)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки деталей рецепта: ${e.message}"
                Log.e("FoodViewModel", "Исключение: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Добавляет или обновляет выбранное блюдо с указанным весом.
     *
     * @param recipe Рецепт.
     * @param weight Вес порции.
     */
    fun addOrUpdateSelectedDish(recipe: RecipeShortDTO, weight: Double) {
        val currentDishes = _selectedDishes.value.toMutableMap()
        if (weight > 0) {
            currentDishes[recipe] = weight
        } else {
            currentDishes.remove(recipe)
        }
        _selectedDishes.value = currentDishes
    }

    /**
     * Очищает список выбранных блюд.
     */
    fun clearSelectedDishes() {
        _selectedDishes.value = emptyMap()
    }

    /**
     * Очищает информацию о выбранном рецепте.
     */
    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }

    /**
     * Добавляет выбранные и пользовательские блюда в дневник.
     *
     * @param context Контекст приложения.
     * @param selectedDishes Список выбранных рецептов с весом.
     * @param customDishes Список пользовательских блюд.
     * @param onComplete Callback, вызываемый по завершении операции (true, если успешно).
     */
    fun addToDiary(
        context: Context,
        selectedDishes: Map<RecipeShortDTO, Double>,
        customDishes: List<CustomDish>,
        onComplete: (Boolean) -> Unit
    ) {
        val token = TokenManager.getToken(context)
        if (token == null) {
            _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
            onComplete(false)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val diaryEntries = mutableListOf<DiaryEntryDTO>()
                selectedDishes.forEach { (recipe, weight) ->
                    val factor = weight / 100.0
                    diaryEntries.add(
                        DiaryEntryDTO(
                            recipeId = recipe.id,
                            name = recipe.name,
                            weight = weight,
                            mealType = null,
                            nutrition = NutritionDTO(
                                calories = recipe.nutrition.calories * factor,
                                proteins = recipe.nutrition.proteins * factor,
                                fats = recipe.nutrition.fats * factor,
                                carbohydrates = recipe.nutrition.carbohydrates * factor,
                                dietaryFiber = recipe.nutrition.dietaryFiber?.let { it * factor },
                                water = recipe.nutrition.water?.let { it * factor }
                            )
                        )
                    )
                }
                customDishes.forEachIndexed { index, dish ->
                    diaryEntries.add(
                        DiaryEntryDTO(
                            recipeId = null,
                            name = "Блюдо ${index + 1}",
                            weight = null,
                            mealType = null,
                            nutrition = NutritionDTO(
                                calories = 0.0,
                                proteins = dish.proteins.toDouble(),
                                fats = dish.fats.toDouble(),
                                carbohydrates = dish.carbs.toDouble()
                            )
                        )
                    )
                }
                val httpResponse = client.post("http://10.0.2.2:8080/food/diary") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(diaryEntries)
                    val url = this.url.buildString()
                    Log.d("FoodViewModel", "Запрос на добавление в дневник: $url")
                }
                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        _errorMessage.value = null
                        Log.d("FoodViewModel", "Записи успешно добавлены в дневник")
                        clearSelections()
                        onComplete(true)
                    }
                    else -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $responseText")
                        try {
                            val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), responseText)
                            val errorMessage = errorResponse.message ?: errorResponse.error ?: errorResponse.details ?: "Неизвестная ошибка"
                            _errorMessage.value = "Ошибка добавления в дневник: $errorMessage"
                        } catch (e: Exception) {
                            _errorMessage.value = "Ошибка добавления в дневник: ${httpResponse.status} - $responseText"
                        }
                        if (httpResponse.status == HttpStatusCode.Unauthorized) {
                            TokenManager.clearToken(context)
                        }
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка добавления в дневник: ${e.message}"
                Log.e("FoodViewModel", "Исключение: ${e.message}", e)
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Очищает сообщение об ошибке.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    private fun clearSelections() {
        _selectedDishes.value = emptyMap()
        _selectedRecipe.value = null
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}