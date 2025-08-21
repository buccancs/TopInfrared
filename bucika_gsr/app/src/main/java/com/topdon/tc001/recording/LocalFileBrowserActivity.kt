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

/**
 * Local File Browser Activity for bucika_gsr
 * Browse and manage recorded video files and GSR data
 */
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
        // No additional data initialization needed
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
            File(FileConfig.lineGalleryDir), // Main gallery directory
            File(FileConfig.lineGalleryDir, "enhanced_recordings"), // Enhanced recordings
            File(FileConfig.lineGalleryDir, "gsr_data") // GSR data files
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
        
        // Sort by modification date (newest first)
        allFiles.sortByDescending { it.file.lastModified() }
        
        fileAdapter.submitList(allFiles)
        
        // Update UI based on file count
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
                ?: "*/*"
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, "Open with...")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            } else {
                android.widget.Toast.makeText(
                    this,
                    "No app found to open ${file.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                "Error opening file: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    enum class FileType {
        VIDEO, DATA, JSON, OTHER
    }

    data class FileItem(
        val file: File,
        val type: FileType
    )

    private inner class FileAdapter(
        private val onFileClick: (File) -> Unit
    ) : RecyclerView.Adapter<FileViewHolder>() {
        
        private var files = listOf<FileItem>()
        
        fun submitList(newFiles: List<FileItem>) {
            files = newFiles
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FileViewHolder {
            val view = layoutInflater.inflate(R.layout.item_file, parent, false)
            return FileViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            holder.bind(files[position])
        }
        
        override fun getItemCount(): Int = files.size
    }

    private inner class FileViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        
        private val tvFileName = itemView.findViewById<android.widget.TextView>(R.id.tv_file_name)
        private val tvFileSize = itemView.findViewById<android.widget.TextView>(R.id.tv_file_size)
        private val tvFileDate = itemView.findViewById<android.widget.TextView>(R.id.tv_file_date)
        private val ivFileType = itemView.findViewById<android.widget.ImageView>(R.id.iv_file_type)
        
        fun bind(fileItem: FileItem) {
            val file = fileItem.file
            
            tvFileName.text = file.name
            tvFileSize.text = formatFileSize(file.length())
            tvFileDate.text = dateFormat.format(Date(file.lastModified()))
            
            // Set file type icon
            val iconRes = when (fileItem.type) {
                FileType.VIDEO -> R.drawable.ic_video_white_svg
                FileType.DATA -> android.R.drawable.ic_menu_agenda
                FileType.JSON -> android.R.drawable.ic_menu_info_details
                FileType.OTHER -> android.R.drawable.ic_menu_gallery
            }
            ivFileType.setImageResource(iconRes)
            
            itemView.setOnClickListener {
                onFileClick(file)
            }
            
            // Add long click for file operations
            itemView.setOnLongClickListener {
                showFileOptions(file)
                true
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> "%.2f GB".format(gb)
            mb >= 1 -> "%.2f MB".format(mb)
            kb >= 1 -> "%.2f KB".format(kb)
            else -> "$bytes B"
        }
    }

    private fun showFileOptions(file: File) {
        val options = arrayOf("Open", "Share", "Delete", "Properties")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(file.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openFile(file) // Open
                    1 -> shareFile(file) // Share
                    2 -> deleteFile(file) // Delete
                    3 -> showFileProperties(file) // Properties
                }
            }
            .show()
    }

    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
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
                    loadFiles() // Refresh the list
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