package com.example.todolist.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.TaskAdapter
import com.example.todolist.database.entities.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var headerManager: HeaderManager
    private lateinit var taskAdapter: TaskAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter()
        recyclerView.adapter = taskAdapter

        // Add some sample data
        val sampleTasks = listOf(
            Task(title = "Sample Task 1", description = "This is a sample task description 1", creationTime = System.currentTimeMillis(), completionTime = System.currentTimeMillis() + 100000, isCompleted = false, notificationEnabled = false, categoryId = null, hasAttachments = false),
            Task(title = "Sample Task 2", description = "This is a sample task description 2", creationTime = System.currentTimeMillis(), completionTime = System.currentTimeMillis() + 200000, isCompleted = false, notificationEnabled = false, categoryId = null, hasAttachments = false),
            Task(title = "Sample Task 3", description = "This is a sample task description 3", creationTime = System.currentTimeMillis(), completionTime = System.currentTimeMillis() + 300000, isCompleted = false, notificationEnabled = false, categoryId = null, hasAttachments = false)
        )
        taskAdapter.submitList(sampleTasks)

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
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val hamburgerIcon: ImageView = view.findViewById(R.id.hamburger_icon)

        headerManager = HeaderManager(drawerLayout, navView, navHostFragment, hamburgerIcon)
        headerManager.initialize()
    }
}
