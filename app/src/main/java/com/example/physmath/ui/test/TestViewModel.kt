package com.example.physmath.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.model.Question
import com.example.physmath.data.model.TestResult
import com.example.physmath.data.model.WrongAnswer
import com.example.physmath.data.repository.LessonRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TestViewModel(
    private val repository: LessonRepository,
    private val lessonId: Int
) : ViewModel() {
    
    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions
    
    private val _currentQuestionIndex = MutableLiveData<Int>()
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex
    
    private val _selectedAnswer = MutableLiveData<Int?>()
    val selectedAnswer: LiveData<Int?> = _selectedAnswer
    
    private val _userAnswers = MutableLiveData<MutableMap<Int, Int>>()
    val userAnswers: LiveData<MutableMap<Int, Int>> = _userAnswers
    
    private val _testResult = MutableLiveData<TestResult?>()
    val testResult: LiveData<TestResult?> = _testResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _showExplanation = MutableLiveData<Boolean>()
    val showExplanation: LiveData<Boolean> = _showExplanation
    
    init {
        _currentQuestionIndex.value = 0
        _userAnswers.value = mutableMapOf()
        _selectedAnswer.value = null
        _showExplanation.value = false
    }
    
    fun loadQuestions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getQuestionsByLessonId(lessonId).collectLatest { questionList ->
                    if (questionList.isEmpty()) {
                        _error.value = "No questions available. Please download the lesson first."
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
    
    fun selectAnswer(answerIndex: Int) {
        _selectedAnswer.value = answerIndex
    }
    
    fun submitAnswer() {
        val currentIndex = _currentQuestionIndex.value ?: return
        val selectedAnswerIndex = _selectedAnswer.value ?: return
        val questionList = _questions.value ?: return
        
        if (currentIndex >= questionList.size) return
        
        val currentQuestion = questionList[currentIndex]
        val answers = _userAnswers.value ?: mutableMapOf()
        answers[currentQuestion.id] = selectedAnswerIndex
        _userAnswers.value = answers
        
        // Check if answer is correct
        if (selectedAnswerIndex != currentQuestion.correctAnswerIndex) {
            _showExplanation.value = true
        } else {
            moveToNextQuestion()
        }
    }
    
    fun moveToNextQuestion() {
        _showExplanation.value = false
        _selectedAnswer.value = null
        val currentIndex = _currentQuestionIndex.value ?: 0
        val questionList = _questions.value ?: return
        
        if (currentIndex < questionList.size - 1) {
            _currentQuestionIndex.value = currentIndex + 1
        } else {
            calculateResults()
        }
    }
    
    private fun calculateResults() {
        val questionList = _questions.value ?: return
        val answers = _userAnswers.value ?: return
        
        var correctCount = 0
        val wrongAnswers = mutableListOf<WrongAnswer>()
        
        questionList.forEach { question ->
            val userAnswer = answers[question.id]
            if (userAnswer == question.correctAnswerIndex) {
                correctCount++
            } else {
                wrongAnswers.add(
                    WrongAnswer(
                        questionId = question.id,
                        selectedOptionIndex = userAnswer ?: -1,
                        explanation = question.explanation
                    )
                )
            }
        }
        
        val result = TestResult(
            lessonId = lessonId,
            totalQuestions = questionList.size,
            correctAnswers = correctCount,
            timestamp = System.currentTimeMillis(),
            wrongAnswers = wrongAnswers
        )
        
        saveTestResult(result)
        _testResult.value = result
    }
    
    private fun saveTestResult(result: TestResult) {
        viewModelScope.launch {
            repository.saveTestResult(result)
        }
    }
    
    fun retryQuestion() {
        _showExplanation.value = false
        _selectedAnswer.value = null
    }
}
