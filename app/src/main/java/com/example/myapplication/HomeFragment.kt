package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val editTextName: EditText = view.findViewById(R.id.editTextName)
        val editTextAge: EditText = view.findViewById(R.id.editTextHeight)
        val editTextWeight: EditText = view.findViewById(R.id.editTextWeight)

        val buttonSubmit: Button = view.findViewById(R.id.buttonSubmit)

        val textViewName: TextView = view.findViewById(R.id.textViewName)
        val textViewHeight: TextView = view.findViewById(R.id.textViewHeight)
        val textViewWeight: TextView = view.findViewById(R.id.textViewWeight)

        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString()
            val age = editTextAge.text.toString()
            val weight = editTextWeight.text.toString()

            textViewName.text = name
            textViewHeight.text = "Рост: $age см"
            textViewWeight.text = "Вес: $weight кг"
        }

        return view
    }
}

