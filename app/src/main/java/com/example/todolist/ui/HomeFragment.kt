package com.example.todolist.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.todolist.R
import com.example.todolist.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var headerManager: HeaderManager
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var headerDate: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        @SuppressLint("SimpleDateFormat")
        override fun run() {
            val sdf = SimpleDateFormat("dd MMM a")
            Log.d("HomeFragment", "Updating date to: ${sdf.format(Date())}")
            headerDate.text = sdf.format(Date())
            handler.postDelayed(this, 60000) // Update every minute
        }
    }

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

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val drawerLayout: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
            val navView: NavigationView = requireActivity().findViewById(R.id.nav_view)
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val hamburgerIcon: ImageView = view.findViewById(R.id.hamburger_icon)
            val headerTitle: TextView = view.findViewById(R.id.header_title)
            headerDate = view.findViewById(R.id.header_date) // Initialize headerDate TextView
            val headerProgress: ProgressBar = view.findViewById(R.id.progressBar)

            headerManager = HeaderManager(
                drawerLayout, navView, navHostFragment, hamburgerIcon,
                headerTitle
            )

            taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

            taskViewModel.taskCompletionPercentage.observe(viewLifecycleOwner, Observer { percentage ->
                headerProgress.progress = percentage
            })

            headerManager.initialize()

            navView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_all_tasks -> {
                        headerTitle.text = "All Tasks"
                        replaceFragment(AllTasksFragment())
                    }

                    R.id.nav_daily_tasks -> {
                        headerTitle.text = "Daily Tasks"
                        replaceFragment(DailyTasksFragment())
                    }
                    // Add other menu items if needed
                }
                drawerLayout.closeDrawers()
                true
            }

            // Set the initial fragment
            replaceFragment(AllTasksFragment())

            // Start updating the time
            handler.post(updateTimeRunnable)

        } catch (e: Exception) {
            Log.e("HomeFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop the time updates when the view is destroyed
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.child_fragment_container, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }
}
