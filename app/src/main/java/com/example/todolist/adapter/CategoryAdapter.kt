package com.example.todolist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.database.entities.Category
import com.example.todolist.helpers.Categories.categoryIndex

class CategoryAdapter(private val isDaily: Boolean = false, private val onCategoryLongClick: (Category) -> Unit) : ListAdapter<Category, CategoryAdapter.CategoryHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem == newItem
            }
        }
        var categoryList: MutableList<Category>? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val layout = if (isDaily) R.layout.fragment_task_item_timeline else R.layout.fragment_task_item
        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return CategoryHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val currentCategory = getItem(position)
        holder.textViewName.text = (categoryList?.get(categoryIndex) ?: currentCategory.name).toString()
        holder.bind(currentCategory, onCategoryLongClick)
    }

    class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: Category, onCategoryLongClick: (Category) -> Unit) {
            itemView.setOnLongClickListener {
                onCategoryLongClick(category)
                true
            }
        }
    }
}