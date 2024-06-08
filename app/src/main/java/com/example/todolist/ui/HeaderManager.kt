package com.example.todolist.ui

import android.view.View
import android.widget.ImageView
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
    private val hamburgerIcon: ImageView
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

        // Add other initialization code for header layout here
    }
}
