package com.example.todolist.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.todolist.R

class SettingsFragment: Fragment(R.layout.settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notificationTimeInput: EditText = view.findViewById(R.id.notification_time_input)
        val saveButton: Button = view.findViewById(R.id.save_settings_button)

        // Load saved preferences
        val sharedPreferences = requireActivity().getSharedPreferences("TaskSettings", Context.MODE_PRIVATE)
        val savedTime = sharedPreferences.getInt("notification_time", 10)
        notificationTimeInput.setText(savedTime.toString())

        saveButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            val notificationTime = notificationTimeInput.text.toString().toIntOrNull() ?: 10
            editor.putInt("notification_time", notificationTime)
            editor.apply()
        }
    }
}