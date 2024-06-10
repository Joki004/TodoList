package com.example.todolist.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.TaskAdapter
import com.example.todolist.helpers.observeAllTasks
import com.example.todolist.helpers.observeCompletedTasks
import com.example.todolist.helpers.observePendingTasks
import com.example.todolist.helpers.observeUrgentTasks
import com.example.todolist.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var headerManager: HeaderManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)


        // Initialize FloatingActionButton
        val fab: FloatingActionButton = rootView.findViewById(R.id.fab)
        fab.setOnClickListener {
            // Handle the click event to add new tasks
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val drawerLayout: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        val navView: NavigationView = requireActivity().findViewById(R.id.nav_view)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val hamburgerIcon: ImageView = view.findViewById(R.id.hamburger_icon)

        headerManager = HeaderManager(drawerLayout, navView, navHostFragment, hamburgerIcon)
        headerManager.initialize()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_all_tasks -> replaceFragment(AllTasksFragment())
                R.id.nav_daily_tasks -> replaceFragment(DailyTasksFragment())
            }
            drawerLayout.closeDrawers()
            true
        }

        // Set the initial fragment
        replaceFragment(AllTasksFragment())

    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.child_fragment_container, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

}
