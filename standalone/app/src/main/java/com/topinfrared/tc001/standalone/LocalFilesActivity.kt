package com.topinfrared.tc001.standalone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.topinfrared.tc001.standalone.databinding.ActivityLocalFilesBinding
import com.topinfrared.tc001.common.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

class LocalFilesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLocalFilesBinding
    private lateinit var filesAdapter: LocalFilesAdapter
    
    companion object {
        private const val TAG = "LocalFilesActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        loadLocalFiles()
    }
    
    private fun setupUI() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                finish()
            }
            
            // Setup RecyclerView
            filesAdapter = LocalFilesAdapter { file ->
                openFile(file)
            }
            
            recyclerViewFiles.apply {
                layoutManager = LinearLayoutManager(this@LocalFilesActivity)
                adapter = filesAdapter
            }
            
            // Refresh button
            btnRefresh.setOnClickListener {
                loadLocalFiles()
            }
            
            // Clear all button
            btnClearAll.setOnClickListener {
                clearAllFiles()
            }
        }
    }
    
    private fun loadLocalFiles() {
        lifecycleScope.launch {
            try {
                binding.progressLoading.visibility = View.VISIBLE
                
                val images = FileUtils.getAllThermalImages(this@LocalFilesActivity)
                val videos = FileUtils.getAllThermalVideos(this@LocalFilesActivity)
                
                val allFiles = (images + videos).sortedByDescending { it.lastModified() }
                
                filesAdapter.updateFiles(allFiles)
                
                binding.apply {
                    tvFilesCount.text = "Files: ${allFiles.size} (${images.size} images, ${videos.size} videos)"
                    
                    val totalSize = FileUtils.getTotalStorageUsed(this@LocalFilesActivity)
                    tvStorageUsed.text = "Storage: ${FileUtils.formatFileSize(totalSize)}"
                    
                    progressLoading.visibility = View.GONE
                    
                    if (allFiles.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                        recyclerViewFiles.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.GONE
                        recyclerViewFiles.visibility = View.VISIBLE
                    }
                }
                
            } catch (e: Exception) {
                binding.progressLoading.visibility = View.GONE
                Toast.makeText(
                    this@LocalFilesActivity,
                    "Failed to load files: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun openFile(file: File) {
        try {
            // TODO: Implement file viewer for thermal images/videos
            // For now, just show file info
            Toast.makeText(
                this,
                "File: ${file.name}\nSize: ${FileUtils.formatFileSize(file.length())}",
                Toast.LENGTH_LONG
            ).show()
            
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to open file: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun clearAllFiles() {
        lifecycleScope.launch {
            try {
                val thermalDir = FileUtils.getThermalDirectory(this@LocalFilesActivity)
                val deleted = thermalDir.deleteRecursively() && thermalDir.mkdirs()
                
                if (deleted) {
                    Toast.makeText(
                        this@LocalFilesActivity,
                        "All files cleared",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadLocalFiles()
                } else {
                    Toast.makeText(
                        this@LocalFilesActivity,
                        "Failed to clear files",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@LocalFilesActivity,
                    "Error clearing files: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}