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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class RecipeShortDTO(
    val id: Int,
    val name: String,
    val nutrition: NutritionDTO
)

@Serializable
data class NutritionDTO(
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbohydrates: Double,
    val dietaryFiber: Double? = null,
    val water: Double? = null
)

@Serializable
data class FullRecipeDTO(
    val id: Int,
    val name: String,
    val preparationMethod: String,
    val category: String?,
    val nutrition: NutritionDTO?,
    val ingredients: List<IngredientDTO>
)

@Serializable
data class IngredientDTO(val name: String, val quantity: String)
@Serializable
data class DiaryEntryDTO(
    val recipeId: Int? = null,
    val name: String,
    val weight: Double? = null,
    val mealType: String? = null,
    val nutrition: NutritionDTO
)

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

    private val _selectedWeights: MutableState<Map<Int, Double>> = mutableStateOf(emptyMap())
    val selectedWeights: Map<Int, Double> get() = _selectedWeights.value

    private val _selectedMealTypes: MutableState<Map<Int, String>> = mutableStateOf(emptyMap())
    val selectedMealTypes: Map<Int, String> get() = _selectedMealTypes.value

    private val _isLoading: MutableState<Boolean> = mutableStateOf(false)

    fun fetchRecommendedRecipes(context: Context, excludeIngredients: List<String>? = null) {
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
                    if (!excludeIngredients.isNullOrEmpty()) {
                        parameter("excludeIngredients", excludeIngredients.joinToString(","))
                    }
                }
                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.d("FoodViewModel", "Полученный JSON: $responseText")
                        val recipes = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString(ListSerializer(RecipeShortDTO.serializer()), responseText)
                        _recommendedRecipes.value = recipes
                        _errorMessage.value = null
                        Log.d("FoodViewModel", "Рекомендованные рецепты загружены: $recipes")
                    }
                    HttpStatusCode.Unauthorized -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка авторизации: $errorText"
                        Log.e("FoodViewModel", "Ошибка авторизации: $errorText")
                        TokenManager.clearToken(context)
                    }
                    else -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка загрузки рецептов: ${httpResponse.status} - $errorText"
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $errorText")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки рецептов: ${e.message}"
                Log.e("FoodViewModel", "Ошибка: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun searchRecipes(context: Context, query: String, excludeIngredients: List<String>? = null) {
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
                    parameter("name", query)
                    if (!excludeIngredients.isNullOrEmpty()) {
                        parameter("excludeIngredients", excludeIngredients.joinToString(","))
                    }
                }
                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.d("FoodViewModel", "Полученный JSON (поиск): $responseText")
                        val recipes = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString(ListSerializer(RecipeShortDTO.serializer()), responseText)
                        _recommendedRecipes.value = recipes
                        _errorMessage.value = null
                        Log.d("FoodViewModel", "Рецепты по запросу '$query' загружены: $recipes")
                    }
                    HttpStatusCode.Unauthorized -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка авторизации: $errorText"
                        Log.e("FoodViewModel", "Ошибка авторизации: $errorText")
                        TokenManager.clearToken(context)
                    }
                    else -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка поиска рецептов: ${httpResponse.status} - $errorText"
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $errorText")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка поиска рецептов: ${e.message}"
                Log.e("FoodViewModel", "Ошибка: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                }

                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        val responseText = httpResponse.bodyAsText()
                        Log.d("FoodViewModel", "Полученный JSON (детали): $responseText")
                        val recipe = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString(FullRecipeDTO.serializer(), responseText)
                        _selectedRecipe.value = recipe
                        _errorMessage.value = null
                        Log.d("FoodViewModel", "Детали рецепта загружены: $recipe")
                    }
                    HttpStatusCode.Unauthorized -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка авторизации: $errorText"
                        Log.e("FoodViewModel", "Ошибка авторизации: $errorText")
                        TokenManager.clearToken(context)
                    }
                    HttpStatusCode.NotFound -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Рецепт не найден: $errorText"
                        Log.e("FoodViewModel", "Рецепт не найден: $errorText")
                    }
                    else -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка загрузки деталей рецепта: ${httpResponse.status} - $errorText"
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $errorText")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки деталей рецепта: ${e.message}"
                Log.e("FoodViewModel", "Ошибка: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateWeight(recipeId: Int, weight: Double) {
        _selectedWeights.value = _selectedWeights.value.toMutableMap().apply {
            if (weight > 0) put(recipeId, weight) else remove(recipeId)
        }
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }
    fun addToDiary(
        context: Context,
        selectedRecipes: List<RecipeShortDTO>,
        selectedWeights: Map<Int, Double>,
        selectedMealTypes: Map<Int, String>,
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
                selectedRecipes.forEach { recipe ->
                    val weight = selectedWeights[recipe.id] ?: 0.0
                    val factor = weight / 100.0
                    val mealType = selectedMealTypes[recipe.id]
                    diaryEntries.add(
                        DiaryEntryDTO(
                            recipeId = recipe.id,
                            name = recipe.name,
                            weight = weight,
                            mealType = mealType,
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
                }
                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        _errorMessage.value = null
                        Log.d("FoodViewModel", "Записи успешно добавлены в дневник")
                        clearSelections()
                        onComplete(true)
                    }
                    HttpStatusCode.Unauthorized -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка авторизации: $errorText"
                        Log.e("FoodViewModel", "Ошибка авторизации: $errorText")
                        TokenManager.clearToken(context)
                        onComplete(false)
                    }
                    else -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка добавления в дневник: ${httpResponse.status} - $errorText"
                        Log.e("FoodViewModel", "Ошибка: ${httpResponse.status} - $errorText")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка добавления в дневник: ${e.message}"
                Log.e("FoodViewModel", "Ошибка: ${e.message}", e)
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    private fun clearSelections() {
        _selectedWeights.value = emptyMap()
        _selectedMealTypes.value = emptyMap()
        _selectedRecipe.value = null
    }
    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}