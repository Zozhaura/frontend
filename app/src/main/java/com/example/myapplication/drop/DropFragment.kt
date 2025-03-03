package com.example.myapplication.drop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentDropBinding

class DropFragment : Fragment() {
    private var _binding: FragmentDropBinding? = null
    private val binding get() = _binding!!
    private var currentWater = 0
    private val maxWater = 2000
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDropBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun updateWaterUI() {
            val progress = (currentWater.toFloat() / maxWater) * 100
            binding.progressWater.progress = progress
            binding.textWater.text = "$currentWater мл"
            binding.textWaterLeft.text = "${maxWater - currentWater} мл осталось"
        }
        val buttons = listOf(
            binding.button50ml,
            binding.button100ml,
            binding.button150ml,
            binding.button200ml,
            binding.button250ml,
            binding.button300ml
        )
        buttons.forEach { button ->
            button.setOnClickListener {
                val volume = button.text.toString().replace(" мл", "").toInt()
                currentWater += volume
                if (currentWater > maxWater) currentWater = maxWater
                updateWaterUI()
            }
        }
        updateWaterUI()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
