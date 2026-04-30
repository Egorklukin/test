package com.example.physmath.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.physmath.data.model.UserProgress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "physmath_prefs", 
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    fun saveUserProgress(progress: UserProgress) {
        val json = gson.toJson(progress)
        prefs.edit().putString("user_progress", json).apply()
    }
    
    fun getUserProgress(): UserProgress {
        val json = prefs.getString("user_progress", null)
        return if (json != null) {
            val type = object : TypeToken<UserProgress>() {}.type
            gson.fromJson(json, type)
        } else {
            UserProgress()
        }
    }
    
    fun saveExportData(): String {
        val progress = getUserProgress()
        return gson.toJson(progress)
    }
    
    fun importData(json: String): Result<Unit> {
        return try {
            val type = object : TypeToken<UserProgress>() {}.type
            val progress: UserProgress = gson.fromJson(json, type)
            saveUserProgress(progress)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Sorry, data from this file can't be imported"))
        }
    }
    
    fun isLessonDownloaded(lessonId: Int): Boolean {
        val downloadedLessons = prefs.getStringSet("downloaded_lessons", emptySet()) ?: emptySet()
        return downloadedLessons.contains(lessonId.toString())
    }
    
    fun setLessonDownloaded(lessonId: Int, isDownloaded: Boolean) {
        val downloadedLessons = prefs.getStringSet("downloaded_lessons", emptySet()) ?: emptySet()
        val mutableSet = downloadedLessons.toMutableSet()
        if (isDownloaded) {
            mutableSet.add(lessonId.toString())
        } else {
            mutableSet.remove(lessonId.toString())
        }
        prefs.edit().putStringSet("downloaded_lessons", mutableSet).apply()
    }
}
