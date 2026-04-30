package com.example.physmath.ui.lessons

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.model.Lesson
import com.example.physmath.data.repository.LessonRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LessonsViewModel(private val repository: LessonRepository) : ViewModel() {
    
    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> = _lessons
    
    private val _downloadedLessons = MutableLiveData<List<Lesson>>()
    val downloadedLessons: LiveData<List<Lesson>> = _downloadedLessons
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadLessons() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllLessons().collectLatest { lessonList ->
                    _lessons.value = lessonList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun loadDownloadedLessons() {
        viewModelScope.launch {
            repository.getDownloadedLessons().collectLatest { lessonList ->
                _downloadedLessons.value = lessonList
            }
        }
    }
    
    fun fetchLessonsFromNetwork(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchLessonsFromNetwork()
            if (result.isSuccess) {
                loadLessons()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to fetch lessons"
            }
            _isLoading.value = false
        }
    }
    
    fun downloadLesson(context: Context, lesson: Lesson) {
        viewModelScope.launch {
            try {
                val questionsResult = repository.fetchQuestionsFromNetwork(lesson.id)
                if (questionsResult.isSuccess) {
                    repository.downloadLesson(lesson, questionsResult.getOrNull()!!)
                    loadLessons()
                    loadDownloadedLessons()
                } else {
                    _error.value = "Failed to download lesson content"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
