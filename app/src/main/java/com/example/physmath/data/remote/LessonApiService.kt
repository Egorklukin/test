package com.example.physmath.data.remote

import com.example.physmath.data.model.Lesson
import com.example.physmath.data.model.Question
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LessonApiService {
    
    @GET("lessons")
    suspend fun getAllLessons(): Response<List<Lesson>>
    
    @GET("lessons/{lessonId}")
    suspend fun getLessonById(@Path("lessonId") lessonId: Int): Response<Lesson>
    
    @GET("lessons/{lessonId}/questions")
    suspend fun getQuestionsByLessonId(@Path("lessonId") lessonId: Int): Response<List<Question>>
}
