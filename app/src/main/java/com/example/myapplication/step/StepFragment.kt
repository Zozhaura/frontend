package com.example.myapplication.step

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentStepsBinding

class StepFragment : Fragment() {
    private var _binding: FragmentStepsBinding? = null
    private val binding get() = _binding!!
    private var currentSteps = 1000
    private var maxSteps = 10000
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun updateStepsUI() {
            val progress = (currentSteps.toFloat() / maxSteps) * 100
            binding.progressSteps.progress = progress
            binding.textSteps.text = "$currentSteps шагов"
            binding.textStepsLeft.text = "${maxSteps - currentSteps} осталось"
            binding.textCaloriesBurned.text = "Калорий потрачено: ${currentSteps / 45}"
        }
        val buttons = listOf(
            binding.button4000,
            binding.button6000,
            binding.button8000,
            binding.button10000,
            binding.button12000,
            binding.button15000
        )
        buttons.forEach { button ->
            button.setOnClickListener {
                maxSteps = button.text.toString().toInt()
                updateStepsUI()
            }
        }
        updateStepsUI()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
