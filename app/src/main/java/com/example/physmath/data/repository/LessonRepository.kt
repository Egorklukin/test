package com.example.physmath.data.repository

import android.content.Context
import com.example.physmath.data.local.AppDatabase
import com.example.physmath.data.local.PreferencesManager
import com.example.physmath.data.model.Lesson
import com.example.physmath.data.model.Question
import com.example.physmath.data.model.TestResult
import com.example.physmath.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class LessonRepository(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val lessonDao = database.lessonDao()
    private val questionDao = database.questionDao()
    private val testResultDao = database.testResultDao()
    private val preferencesManager = PreferencesManager(context)
    private val apiService = RetrofitClient.lessonApiService
    
    fun getAllLessons(): Flow<List<Lesson>> {
        return lessonDao.getAllLessons()
    }
    
    fun getDownloadedLessons(): Flow<List<Lesson>> {
        return lessonDao.getDownloadedLessons()
    }
    
    suspend fun getLessonById(lessonId: Int): Lesson? {
        return withContext(Dispatchers.IO) {
            lessonDao.getLessonById(lessonId)
        }
    }
    
    fun getQuestionsByLessonId(lessonId: Int): Flow<List<Question>> {
        return questionDao.getQuestionsByLessonId(lessonId)
    }
    
    suspend fun fetchLessonsFromNetwork(): Result<List<Lesson>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllLessons()
                if (response.isSuccessful && response.body() != null) {
                    val lessons = response.body()!!
                    lessonDao.insertLessons(lessons)
                    Result.success(lessons)
                } else {
                    Result.failure(IOException("Failed to fetch lessons: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun fetchQuestionsFromNetwork(lessonId: Int): Result<List<Question>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getQuestionsByLessonId(lessonId)
                if (response.isSuccessful && response.body() != null) {
                    val questions = response.body()!!
                    questionDao.insertQuestions(questions)
                    Result.success(questions)
                } else {
                    Result.failure(IOException("Failed to fetch questions: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun saveTestResult(testResult: TestResult) {
        withContext(Dispatchers.IO) {
            testResultDao.insertTestResult(testResult)
        }
    }
    
    fun getTestResultsByLessonId(lessonId: Int): Flow<List<TestResult>> {
        return testResultDao.getTestResultsByLessonId(lessonId)
    }
    
    fun isLessonDownloaded(lessonId: Int): Boolean {
        return preferencesManager.isLessonDownloaded(lessonId)
    }
    
    suspend fun downloadLesson(lesson: Lesson, questions: List<Question>) {
        withContext(Dispatchers.IO) {
            val updatedLesson = lesson.copy(isDownloaded = true)
            lessonDao.insertLesson(updatedLesson)
            questionDao.insertQuestions(questions)
            preferencesManager.setLessonDownloaded(lesson.id, true)
        }
    }
    
    suspend fun deleteLesson(lessonId: Int) {
        withContext(Dispatchers.IO) {
            preferencesManager.setLessonDownloaded(lessonId, false)
            // Note: We don't delete the lesson content, just mark as not downloaded
            val lesson = lessonDao.getLessonById(lessonId)
            lesson?.let {
                lessonDao.insertLesson(it.copy(isDownloaded = false))
            }
        }
    }
}
