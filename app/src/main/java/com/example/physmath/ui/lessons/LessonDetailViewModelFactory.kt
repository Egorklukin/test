package com.example.physmath.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.physmath.data.repository.LessonRepository

class LessonDetailViewModelFactory(
    private val repository: LessonRepository,
    private val lessonId: Int
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LessonDetailViewModel::class.java)) {
            return LessonDetailViewModel(repository, lessonId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
