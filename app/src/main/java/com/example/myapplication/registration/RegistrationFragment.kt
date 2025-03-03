package com.example.myapplication.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentRegBinding

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerGender: Spinner = binding.spinnerGender
        val buttonSubmit: Button = binding.buttonSubmit
        val genderOptions = listOf(getString(R.string.select_gender)) +
                resources.getStringArray(R.array.gender_options).toList()
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner,
            genderOptions
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter
        spinnerGender.setSelection(0)
        buttonSubmit.setOnClickListener {
            Toast.makeText(requireContext(), "клик", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
