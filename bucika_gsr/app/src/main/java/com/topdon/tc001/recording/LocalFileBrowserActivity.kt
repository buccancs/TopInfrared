package com.topdon.tc001.recording

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.config.FileConfig
import com.topdon.tc001.R
import kotlinx.android.synthetic.main.activity_local_file_browser.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LocalFileBrowserActivity : BaseActivity() {

    private lateinit var fileAdapter: FileAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    override fun initContentView(): Int = R.layout.activity_local_file_browser

    override fun initView() {
        title_view.setTitle("Local File Browser")
        title_view.setLeftClickListener { finish() }
        
        setupRecyclerView()
        loadFiles()
    }

    override fun initData() {
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter { file ->
            openFile(file)
        }
        
        recycler_files.apply {
            layoutManager = LinearLayoutManager(this@LocalFileBrowserActivity)
            adapter = fileAdapter
        }
    }

    private fun loadFiles() {
        val recordingDirs = listOf(
            File(FileConfig.lineGalleryDir),
            File(FileConfig.lineGalleryDir, "enhanced_recordings"),
            File(FileConfig.lineGalleryDir, "gsr_data")
        )
        
        val allFiles = mutableListOf<FileItem>()
        
        recordingDirs.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && (file.extension.lowercase() in listOf("mp4", "avi", "csv", "json", "txt"))) {
                        allFiles.add(FileItem(file, getFileType(file)))
                    }
                }
            }
        }
        
        allFiles.sortByDescending { it.file.lastModified() }
        
        fileAdapter.submitList(allFiles)
        
        if (allFiles.isEmpty()) {
            tv_empty_state.visibility = android.view.View.VISIBLE
            recycler_files.visibility = android.view.View.GONE
        } else {
            tv_empty_state.visibility = android.view.View.GONE
            recycler_files.visibility = android.view.View.VISIBLE
        }
        
        tv_file_count.text = "Found ${allFiles.size} files"
    }

    private fun getFileType(file: File): FileType {
        return when (file.extension.lowercase()) {
            "mp4", "avi", "mov" -> FileType.VIDEO
            "csv", "txt" -> FileType.DATA
            "json" -> FileType.JSON
            else -> FileType.OTHER
        }
    }

    private fun openFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                ?: "**"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                "Error sharing file: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteFile(file: File) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete ${file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (file.delete()) {
                    android.widget.Toast.makeText(this, "File deleted", android.widget.Toast.LENGTH_SHORT).show()
                    loadFiles()
                } else {
                    android.widget.Toast.makeText(this, "Failed to delete file", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFileProperties(file: File) {
        val properties = """
            Name: ${file.name}
            Size: ${formatFileSize(file.length())}
            Created: ${dateFormat.format(Date(file.lastModified()))}
            Path: ${file.absolutePath}
            Type: ${file.extension.uppercase()}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("File Properties")
            .setMessage(properties)
            .setPositiveButton("OK", null)
            .show()
    }
}