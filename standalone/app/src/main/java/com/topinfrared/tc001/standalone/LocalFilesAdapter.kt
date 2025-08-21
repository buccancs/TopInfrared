package com.topinfrared.tc001.standalone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topinfrared.tc001.standalone.databinding.ItemLocalFileBinding
import com.topinfrared.tc001.common.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LocalFilesAdapter(
    private val onFileClick: (File) -> Unit
) : RecyclerView.Adapter<LocalFilesAdapter.FileViewHolder>() {
    
    private var files = listOf<File>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemLocalFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }
    
    override fun getItemCount(): Int = files.size
    
    inner class FileViewHolder(
        private val binding: ItemLocalFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(file: File) {
            binding.apply {
                tvFileName.text = file.name
                tvFileSize.text = FileUtils.formatFileSize(file.length())
                tvFileDate.text = dateFormat.format(Date(file.lastModified()))
                
                // Set file type icon
                val isVideo = file.name.lowercase().endsWith(".mp4")
                if (isVideo) {
                    ivFileType.setImageResource(android.R.drawable.ic_media_play)
                    tvFileType.text = "Video"
                } else {
                    ivFileType.setImageResource(android.R.drawable.ic_menu_gallery)
                    tvFileType.text = "Image"
                }
                
                root.setOnClickListener {
                    onFileClick(file)
                }
            }
        }
    }
}