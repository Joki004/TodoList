package com.example.todolist.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.todolist.R
import com.google.android.material.navigation.NavigationView

class HeaderManager(
    private val drawerLayout: DrawerLayout,
    private val navView: NavigationView,
    private val navHostFragment: NavHostFragment,
    private val hamburgerIcon: ImageView,
    private val headerTitle: TextView
) {

    fun initialize() {
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.editTaskFragment, R.id.allTasksFragment, R.id.DailyTasksFragment), drawerLayout
        )

        navView.setupWithNavController(navController)

        hamburgerIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            headerTitle.text = when (destination.id) {
                R.id.homeFragment -> "Home"
                R.id.editTaskFragment -> "Edit Task"
                R.id.allTasksFragment -> "All Tasks"
                R.id.DailyTasksFragment -> "Daily Tasks"
                else -> "Your Things"
            }
        }
    }
}
