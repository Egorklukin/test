package com.example.physmath.ui.lessons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.physmath.data.model.Lesson
import com.example.physmath.databinding.ItemLessonBinding

class LessonAdapter(
    private val onItemClick: (Lesson) -> Unit,
    private val onDownloadClick: (Lesson) -> Unit
) : ListAdapter<Lesson, LessonAdapter.LessonViewHolder>(LessonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LessonViewHolder(
        private val binding: ItemLessonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.lessonTitle.text = lesson.title
            binding.lessonSubject.text = when (lesson.subject) {
                com.example.physmath.data.model.Subject.PHYSICS -> "Physics"
                com.example.physmath.data.model.Subject.MATHEMATICS -> "Mathematics"
            }

            if (lesson.isDownloaded) {
                binding.downloadIndicator.visibility = ViewGroup.VISIBLE
                binding.actionButton.text = "Open"
            } else {
                binding.downloadIndicator.visibility = ViewGroup.GONE
                binding.actionButton.text = "Download"
            }

            binding.root.setOnClickListener {
                onItemClick(lesson)
            }

            binding.actionButton.setOnClickListener {
                onDownloadClick(lesson)
            }
        }
    }

    class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem == newItem
        }
    }
}
