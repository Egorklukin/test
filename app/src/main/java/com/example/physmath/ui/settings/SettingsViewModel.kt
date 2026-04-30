package com.example.physmath.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physmath.data.local.PreferencesManager
import com.example.physmath.data.model.UserProgress
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.BufferedReader
import java.io.FileReader

class SettingsViewModel(private val context: Context) : ViewModel() {
    
    private val preferencesManager = PreferencesManager(context)
    
    private val _exportStatus = MutableLiveData<String?>()
    val exportStatus: LiveData<String?> = _exportStatus
    
    private val _importStatus = MutableLiveData<String?>()
    val importStatus: LiveData<String?> = _importStatus
    
    private val _userProgress = MutableLiveData<UserProgress>()
    val userProgress: LiveData<UserProgress> = _userProgress
    
    init {
        loadUserProgress()
    }
    
    fun loadUserProgress() {
        _userProgress.value = preferencesManager.getUserProgress()
    }
    
    fun exportData(): File? {
        return try {
            val jsonData = preferencesManager.saveExportData()
            val exportFile = File(context.getExternalFilesDir(null), "physmath_export_${System.currentTimeMillis()}.json")
            
            FileWriter(exportFile).use { writer ->
                writer.write(jsonData)
            }
            
            _exportStatus.value = "Export successful"
            exportFile
        } catch (e: Exception) {
            _exportStatus.value = "Export failed: ${e.message}"
            null
        }
    }
    
    fun importData(file: File) {
        viewModelScope.launch {
            try {
                if (!file.exists()) {
                    _importStatus.value = "File not found"
                    return@launch
                }
                
                val jsonData = BufferedReader(FileReader(file)).use { reader ->
                    reader.readText()
                }
                
                val result = preferencesManager.importData(jsonData)
                
                if (result.isSuccess) {
                    _importStatus.value = "Импорт завершен"
                    loadUserProgress()
                } else {
                    _importStatus.value = "Sorry, data from this file can't be imported"
                }
            } catch (e: Exception) {
                _importStatus.value = "Sorry, data from this file can't be imported"
            }
        }
    }
    
    fun clearStatus() {
        _exportStatus.value = null
        _importStatus.value = null
    }
}
