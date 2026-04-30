package com.example.physmath.data.model

data class Lesson(
    val id: Int,
    val title: String,
    val subject: Subject,
    val content: String,
    val isDownloaded: Boolean = false,
    val lastOpened: Long? = null
)

enum class Subject {
    PHYSICS,
    MATHEMATICS
}

data class Question(
    val id: Int,
    val lessonId: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class TestResult(
    val lessonId: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timestamp: Long,
    val wrongAnswers: List<WrongAnswer>
)

data class WrongAnswer(
    val questionId: Int,
    val selectedOptionIndex: Int,
    val explanation: String
)

data class UserProgress(
    val userId: String = "default_user",
    val completedLessons: List<Int> = emptyList(),
    val testResults: List<TestResult> = emptyList(),
    val downloadedLessons: List<Int> = emptyList()
)
