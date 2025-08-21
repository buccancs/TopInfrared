package com.topinfrared.tc001.standalone

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topinfrared.tc001.standalone.databinding.ItemLocalFileBinding
import com.topinfrared.tc001.common.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LocalFilesAdapter(
    private val onFileClick: (File) -> Unit,
    private val onFileLongClick: (File) -> Boolean = { false },
    private val onFileSelected: (File, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<LocalFilesAdapter.FileViewHolder>() {
    
    private var files = listOf<File>()
    private var filteredFiles = listOf<File>()
    private val selectedFiles = mutableSetOf<File>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    private var isSelectionMode = false
    private var currentSortBy = SortType.DATE_DESC
    private var currentFilter = FileFilter.ALL
    
    enum class SortType {
        NAME_ASC, NAME_DESC, 
        DATE_ASC, DATE_DESC, 
        SIZE_ASC, SIZE_DESC,
        TYPE_ASC, TYPE_DESC
    }
    
    enum class FileFilter {
        ALL, IMAGES, VIDEOS
    }
    
    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        applyFilterAndSort()
    }
    
    fun setSortType(sortType: SortType) {
        currentSortBy = sortType
        applyFilterAndSort()
    }
    
    fun setFilter(filter: FileFilter) {
        currentFilter = filter
        applyFilterAndSort()
    }
    
    fun searchFiles(query: String) {
        filteredFiles = if (query.isEmpty()) {
            files
        } else {
            files.filter { it.name.lowercase().contains(query.lowercase()) }
        }
        applySorting()
    }
    
    private fun applyFilterAndSort() {
        // Apply filter first
        filteredFiles = when (currentFilter) {
            FileFilter.ALL -> files
            FileFilter.IMAGES -> files.filter { isImageFile(it) }
            FileFilter.VIDEOS -> files.filter { isVideoFile(it) }
        }
        
        applySorting()
    }
    
    private fun applySorting() {
        filteredFiles = when (currentSortBy) {
            SortType.NAME_ASC -> filteredFiles.sortedBy { it.name.lowercase() }
            SortType.NAME_DESC -> filteredFiles.sortedByDescending { it.name.lowercase() }
            SortType.DATE_ASC -> filteredFiles.sortedBy { it.lastModified() }
            SortType.DATE_DESC -> filteredFiles.sortedByDescending { it.lastModified() }
            SortType.SIZE_ASC -> filteredFiles.sortedBy { it.length() }
            SortType.SIZE_DESC -> filteredFiles.sortedByDescending { it.length() }
            SortType.TYPE_ASC -> filteredFiles.sortedBy { it.extension.lowercase() }
            SortType.TYPE_DESC -> filteredFiles.sortedByDescending { it.extension.lowercase() }
        }
        
        notifyDataSetChanged()
    }
    
    private fun isImageFile(file: File): Boolean {
        return file.name.lowercase().run { 
            endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") || endsWith(".bmp")
        }
    }
    
    private fun isVideoFile(file: File): Boolean {
        return file.name.lowercase().run { 
            endsWith(".mp4") || endsWith(".avi") || endsWith(".mov") || endsWith(".3gp")
        }
    }
    
    fun toggleSelectionMode(): Boolean {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) {
            selectedFiles.clear()
        }
        notifyDataSetChanged()
        return isSelectionMode
    }
    
    fun selectAll() {
        selectedFiles.clear()
        selectedFiles.addAll(filteredFiles)
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        selectedFiles.clear()
        notifyDataSetChanged()
    }
    
    fun getSelectedFiles(): Set<File> = selectedFiles.toSet()
    
    fun getSelectedCount(): Int = selectedFiles.size
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemLocalFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(filteredFiles[position])
    }
    
    override fun getItemCount(): Int = filteredFiles.size
    
    inner class FileViewHolder(
        private val binding: ItemLocalFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(file: File) {
            binding.apply {
                tvFileName.text = file.name
                tvFileSize.text = FileUtils.formatFileSize(file.length())
                tvFileDate.text = dateFormat.format(Date(file.lastModified()))
                
                // Enhanced file type detection and icons
                val isVideo = isVideoFile(file)
                val isImage = isImageFile(file)
                
                when {
                    isVideo -> {
                        ivFileType.setImageResource(android.R.drawable.ic_media_play)
                        tvFileType.text = "Video"
                        // Load video thumbnail if possible
                        loadVideoThumbnail(file)
                    }
                    isImage -> {
                        ivFileType.setImageResource(android.R.drawable.ic_menu_gallery)
                        tvFileType.text = "Image"
                        // Load image thumbnail
                        loadImageThumbnail(file)
                    }
                    else -> {
                        ivFileType.setImageResource(android.R.drawable.ic_menu_info_details)
                        tvFileType.text = "File"
                        ivThumbnail.visibility = View.GONE
                    }
                }
                
                // Selection mode handling
                if (isSelectionMode) {
                    cbFileSelect.visibility = View.VISIBLE
                    cbFileSelect.isChecked = selectedFiles.contains(file)
                } else {
                    cbFileSelect.visibility = View.GONE
                }
                
                // Set selection background
                root.isSelected = selectedFiles.contains(file)
                
                // Click handlers
                root.setOnClickListener {
                    if (isSelectionMode) {
                        toggleFileSelection(file)
                    } else {
                        onFileClick(file)
                    }
                }
                
                root.setOnLongClickListener {
                    if (!isSelectionMode) {
                        isSelectionMode = true
                        toggleFileSelection(file)
                        notifyDataSetChanged()
                    }
                    onFileLongClick(file)
                }
                
                cbFileSelect.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedFiles.add(file)
                    } else {
                        selectedFiles.remove(file)
                    }
                    onFileSelected(file, isChecked)
                    root.isSelected = isChecked
                }
            }
        }
        
        private fun toggleFileSelection(file: File) {
            if (selectedFiles.contains(file)) {
                selectedFiles.remove(file)
            } else {
                selectedFiles.add(file)
            }
            onFileSelected(file, selectedFiles.contains(file))
            notifyItemChanged(adapterPosition)
        }
        
        private fun loadImageThumbnail(file: File) {
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4 // Scale down for thumbnail
                    inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                if (bitmap != null) {
                    binding.ivThumbnail.setImageBitmap(bitmap)
                    binding.ivThumbnail.visibility = View.VISIBLE
                } else {
                    binding.ivThumbnail.visibility = View.GONE
                }
            } catch (e: Exception) {
                binding.ivThumbnail.visibility = View.GONE
            }
        }
        
        private fun loadVideoThumbnail(file: File) {
            try {
                // For video thumbnails, we could use MediaMetadataRetriever
                // For now, show a generic video icon
                binding.ivThumbnail.setImageResource(android.R.drawable.ic_media_play)
                binding.ivThumbnail.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.ivThumbnail.visibility = View.GONE
            }
        }
    }
}