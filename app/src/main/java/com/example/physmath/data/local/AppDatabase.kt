package com.example.physmath.data.local

import androidx.room.*
import com.example.physmath.data.model.Lesson
import com.example.physmath.data.model.Question
import com.example.physmath.data.model.TestResult
import com.example.physmath.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<Lesson>>
    
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: Int): Lesson?
    
    @Query("SELECT * FROM lessons WHERE isDownloaded = 1")
    fun getDownloadedLessons(): Flow<List<Lesson>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)
    
    @Update
    suspend fun updateLesson(lesson: Lesson)
    
    @Query("DELETE FROM lessons")
    suspend fun deleteAllLessons()
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE lessonId = :lessonId")
    fun getQuestionsByLessonId(lessonId: Int): Flow<List<Question>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)
    
    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM testresults WHERE lessonId = :lessonId ORDER BY timestamp DESC")
    fun getTestResultsByLessonId(lessonId: Int): Flow<List<TestResult>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(testResult: TestResult)
    
    @Query("DELETE FROM testresults")
    suspend fun deleteAllTestResults()
}

@Database(
    entities = [Lesson::class, Question::class, TestResult::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun questionDao(): QuestionDao
    abstract fun testResultDao(): TestResultDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "physmath_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
