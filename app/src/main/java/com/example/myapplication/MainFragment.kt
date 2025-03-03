package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.myapplication.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var proteinValue: String = "0/90 г"
    private var fatValue: String = "24/48 г"
    private var carbsValue: String = "62/125 г"
    private var caloriesValue: String = "1300 ккал"
    private var caloriesLeft: String = "800 ккал осталось"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        setMacroData(proteinValue, fatValue, carbsValue, caloriesValue, caloriesLeft)
        setupIconClickListeners()
    }

    private fun setMacroData(protein: String, fat: String, carbs: String, calories: String, caloriesLeft: String) {
        binding.textProteins.text = protein
        binding.textFats.text = fat
        binding.textCarbs.text = carbs
        binding.textCalories.text = calories
        binding.textCaloriesLeft.text = caloriesLeft

        val proteinProgress = calculateProgress(protein)
        val fatProgress = calculateProgress(fat)
        val carbsProgress = calculateProgress(carbs)
        val caloriesProgress = calculateCaloriesProgress(calories)

        binding.progressProteins.progress = proteinProgress
        binding.progressFats.progress = fatProgress
        binding.progressCarbs.progress = carbsProgress
        binding.progressCalories.progress = caloriesProgress
    }

    private fun calculateProgress(value: String): Float {
        val values = value.replace(Regex("[^\\d/]"), "").split("/")
        val currentValue = values[0].toFloatOrNull() ?: 0f
        val totalValue = values[1].toFloatOrNull() ?: 1f
        return (currentValue / totalValue) * 100f
    }
    private fun calculateCaloriesProgress(value: String): Float {
        val numericValue = value.replace(Regex("[^\\d]"), "").toFloatOrNull() ?: 0f
        val totalCalories = 2000f
        return (numericValue / totalCalories) * 100f
    }
    private fun setupIconClickListeners() {
        binding.iconDrop.setOnClickListener {
            navController.navigate(R.id.nav_drop)
        }
        binding.iconRun.setOnClickListener {
            navController.navigate(R.id.nav_step)
        }
        binding.iconAddFood.setOnClickListener {
            navController.navigate(R.id.nav_food)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
