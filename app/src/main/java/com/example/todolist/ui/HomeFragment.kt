package com.example.todolist.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
    companion object {
        var isDaily = false
    }

    private lateinit var headerManager: HeaderManager
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var headerDate: TextView
    private lateinit var searchBar: EditText
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        @SuppressLint("SimpleDateFormat")
        override fun run() {
            val sdf = SimpleDateFormat("dd MMM ")
            Log.d("HomeFragment", "Updating date to: ${sdf.format(Date())}")
            headerDate.text = sdf.format(Date())
            handler.postDelayed(this, 60000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val fab: FloatingActionButton = rootView.findViewById(R.id.fab)
        fab.setOnClickListener {
            TaskCreateTask.newInstance().show(parentFragmentManager, "create_task")
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
            headerDate = view.findViewById(R.id.header_date)
            val headerProgress: ProgressBar = view.findViewById(R.id.progressBar)
            searchBar = view.findViewById(R.id.search_view)

            headerManager = HeaderManager(
                drawerLayout, navView, navHostFragment, hamburgerIcon,
                headerTitle
            )

            taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

            taskViewModel.taskCompletionPercentage.observe(
                viewLifecycleOwner,
                Observer { percentage ->
                    headerProgress.progress = percentage
                })

            searchBar.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    Log.d("AllTasksFragment", "Search action triggered")

                    val imm =
                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(v.windowToken, 0)
                    Log.d("AllTasksFragment", "Keyboard hidden")

                    val query = searchBar.text.toString()
                    Log.d("AllTasksFragment", "Search query: $query")
                    val allTasksFragment = AllTasksFragment()
                    val dailyTasksFragment = DailyTasksFragment()
                    val bundle = Bundle()
                    bundle.putString("search_query", query)

                    if (isDaily) {
                        dailyTasksFragment.arguments = bundle
                        replaceFragment(dailyTasksFragment)
                    } else {
                        allTasksFragment.arguments = bundle
                        replaceFragment(allTasksFragment)
                    }
                    true
                } else {
                    false
                }
            }

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

                    R.id.nav_settings -> {
                        Toast.makeText(context, "Attempting to open settings", Toast.LENGTH_SHORT)
                            .show()
                        val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                        Log.d("Navigation", "Action: $action")
                        NavHostFragment.findNavController(requireParentFragment()).navigate(action)
                    }

                }
                drawerLayout.closeDrawers()
                true
            }

            replaceFragment(AllTasksFragment())
            handler.post(updateTimeRunnable)

        } catch (e: Exception) {
            Log.e("HomeFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.child_fragment_container, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }
}
