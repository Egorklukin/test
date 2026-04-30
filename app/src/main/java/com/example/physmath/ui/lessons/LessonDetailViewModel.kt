package com.example.physmath.ui.lessons

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.model.Lesson
import com.example.physmath.data.model.Question
import com.example.physmath.data.repository.LessonRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LessonDetailViewModel(
    private val repository: LessonRepository,
    private val lessonId: Int
) : ViewModel() {
    
    private val _lesson = MutableLiveData<Lesson?>()
    val lesson: LiveData<Lesson?> = _lesson
    
    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadLesson() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _lesson.value = repository.getLessonById(lessonId)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun loadQuestions(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getQuestionsByLessonId(lessonId).collectLatest { questionList ->
                    if (questionList.isEmpty()) {
                        // Try to fetch from network if not in database
                        val result = repository.fetchQuestionsFromNetwork(lessonId)
                        if (result.isSuccess) {
                            _questions.value = result.getOrNull() ?: emptyList()
                        } else {
                            _error.value = "No internet connection. Please download the lesson first."
                        }
                    } else {
                        _questions.value = questionList
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
}
