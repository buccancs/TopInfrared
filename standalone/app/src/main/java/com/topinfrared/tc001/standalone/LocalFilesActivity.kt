package com.topinfrared.tc001.standalone

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.topinfrared.tc001.standalone.databinding.ActivityLocalFilesBinding
import com.topinfrared.tc001.common.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

class LocalFilesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLocalFilesBinding
    private lateinit var filesAdapter: LocalFilesAdapter
    private var isGridView = false
    private var searchQuery = ""
    
    companion object {
        private const val TAG = "LocalFilesActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupActionBar()
        setupUI()
        loadLocalFiles()
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            title = "Local Files"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_local_files, menu)
        
        // Setup search
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchFiles(query ?: "")
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                searchFiles(newText ?: "")
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_view_toggle -> {
                toggleViewMode()
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_select_all -> {
                filesAdapter.selectAll()
                updateSelectionUI()
                true
            }
            R.id.action_delete_selected -> {
                deleteSelectedFiles()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Setup RecyclerView with enhanced adapter
            filesAdapter = LocalFilesAdapter(
                onFileClick = { file -> openFile(file) },
                onFileLongClick = { file -> 
                    startSelectionMode(file)
                    true
                },
                onFileSelected = { file, selected -> 
                    updateSelectionUI()
                }
            )
            
            recyclerViewFiles.apply {
                layoutManager = LinearLayoutManager(this@LocalFilesActivity)
                adapter = filesAdapter
            }
            
            // Setup toolbar buttons
            btnRefresh.setOnClickListener {
                loadLocalFiles()
            }
            
            btnClearAll.setOnClickListener {
                showClearAllDialog()
            }
            
            // Selection mode buttons
            btnCancelSelection.setOnClickListener {
                exitSelectionMode()
            }
            
            btnDeleteSelected.setOnClickListener {
                deleteSelectedFiles()
            }
            
            btnShareSelected.setOnClickListener {
                shareSelectedFiles()
            }
            
            // Filter chips
            chipAll.setOnClickListener {
                filterFiles(LocalFilesAdapter.FileFilter.ALL)
                updateFilterChips(LocalFilesAdapter.FileFilter.ALL)
            }
            
            chipImages.setOnClickListener {
                filterFiles(LocalFilesAdapter.FileFilter.IMAGES)
                updateFilterChips(LocalFilesAdapter.FileFilter.IMAGES)
            }
            
            chipVideos.setOnClickListener {
                filterFiles(LocalFilesAdapter.FileFilter.VIDEOS)
                updateFilterChips(LocalFilesAdapter.FileFilter.VIDEOS)
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
            if (file.name.lowercase().run { endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") }) {
                // Open image file with system viewer
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "image/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(Intent.createChooser(intent, "Open Image"))
                
            } else if (file.name.lowercase().endsWith(".mp4")) {
                // Open video file with system player
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "video/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(Intent.createChooser(intent, "Play Video"))
                
            } else {
                // Show file details for other files
                showFileDetailsDialog(file)
            }
            
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to open file: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showFileDetailsDialog(file: File) {
        val details = """
            Name: ${file.name}
            Size: ${FileUtils.formatFileSize(file.length())}
            Modified: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified()))}
            Path: ${file.absolutePath}
            Type: ${file.extension.uppercase()}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("File Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .setNeutralButton("Share") { _, _ -> shareFile(file) }
            .setNegativeButton("Delete") { _, _ -> deleteFile(file) }
            .show()
    }
    
    private fun shareFile(file: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when {
                    file.name.lowercase().run { endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png") } -> "image/*"
                    file.name.lowercase().endsWith(".mp4") -> "video/*"
                    else -> "*/*"
                }
                putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                putExtra(Intent.EXTRA_SUBJECT, "Thermal capture from TC001")
                putExtra(Intent.EXTRA_TEXT, "Sharing thermal capture: ${file.name}")
            }
            startActivity(Intent.createChooser(intent, "Share File"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to share file", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteFile(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete ${file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (file.delete()) {
                    Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
                    loadLocalFiles() // Refresh list
                } else {
                    Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showClearAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Files")
            .setMessage("Are you sure you want to delete all thermal images and videos? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllFiles()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
    
    // Enhanced UI methods
    private fun searchFiles(query: String) {
        searchQuery = query
        filesAdapter.searchFiles(query)
        updateFileCount()
    }
    
    private fun filterFiles(filter: LocalFilesAdapter.FileFilter) {
        filesAdapter.setFilter(filter)
        updateFileCount()
    }
    
    private fun updateFilterChips(activeFilter: LocalFilesAdapter.FileFilter) {
        binding.apply {
            chipAll.isChecked = (activeFilter == LocalFilesAdapter.FileFilter.ALL)
            chipImages.isChecked = (activeFilter == LocalFilesAdapter.FileFilter.IMAGES)
            chipVideos.isChecked = (activeFilter == LocalFilesAdapter.FileFilter.VIDEOS)
        }
    }
    
    private fun toggleViewMode() {
        isGridView = !isGridView
        binding.recyclerViewFiles.layoutManager = if (isGridView) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        invalidateOptionsMenu() // Update menu icon
    }
    
    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Name (A-Z)", "Name (Z-A)", 
            "Date (Newest)", "Date (Oldest)",
            "Size (Largest)", "Size (Smallest)",
            "Type (A-Z)", "Type (Z-A)"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Sort Files")
            .setItems(sortOptions) { _, which ->
                val sortType = when (which) {
                    0 -> LocalFilesAdapter.SortType.NAME_ASC
                    1 -> LocalFilesAdapter.SortType.NAME_DESC
                    2 -> LocalFilesAdapter.SortType.DATE_DESC
                    3 -> LocalFilesAdapter.SortType.DATE_ASC
                    4 -> LocalFilesAdapter.SortType.SIZE_DESC
                    5 -> LocalFilesAdapter.SortType.SIZE_ASC
                    6 -> LocalFilesAdapter.SortType.TYPE_ASC
                    7 -> LocalFilesAdapter.SortType.TYPE_DESC
                    else -> LocalFilesAdapter.SortType.DATE_DESC
                }
                filesAdapter.setSortType(sortType)
                updateFileCount()
            }
            .show()
    }
    
    private fun showFilterDialog() {
        val filterOptions = arrayOf("All Files", "Images Only", "Videos Only")
        
        AlertDialog.Builder(this)
            .setTitle("Filter Files")
            .setItems(filterOptions) { _, which ->
                val filter = when (which) {
                    0 -> LocalFilesAdapter.FileFilter.ALL
                    1 -> LocalFilesAdapter.FileFilter.IMAGES
                    2 -> LocalFilesAdapter.FileFilter.VIDEOS
                    else -> LocalFilesAdapter.FileFilter.ALL
                }
                filterFiles(filter)
                updateFilterChips(filter)
            }
            .show()
    }
    
    private fun startSelectionMode(file: File) {
        filesAdapter.toggleSelectionMode()
        updateSelectionUI()
    }
    
    private fun exitSelectionMode() {
        filesAdapter.clearSelection()
        filesAdapter.toggleSelectionMode()
        updateSelectionUI()
    }
    
    private fun updateSelectionUI() {
        val selectedCount = filesAdapter.getSelectedCount()
        val isSelectionMode = selectedCount > 0
        
        binding.apply {
            if (isSelectionMode) {
                selectionToolbar.visibility = View.VISIBLE
                tvSelectionCount.text = "$selectedCount selected"
            } else {
                selectionToolbar.visibility = View.GONE
            }
        }
        
        supportActionBar?.title = if (isSelectionMode) {
            "$selectedCount selected"
        } else {
            "Local Files"
        }
    }
    
    private fun deleteSelectedFiles() {
        val selectedFiles = filesAdapter.getSelectedFiles()
        if (selectedFiles.isEmpty()) return
        
        AlertDialog.Builder(this)
            .setTitle("Delete Files")
            .setMessage("Delete ${selectedFiles.size} selected files?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    var deletedCount = 0
                    selectedFiles.forEach { file ->
                        if (file.delete()) deletedCount++
                    }
                    
                    Toast.makeText(
                        this@LocalFilesActivity,
                        "$deletedCount files deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    exitSelectionMode()
                    loadLocalFiles()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun shareSelectedFiles() {
        val selectedFiles = filesAdapter.getSelectedFiles()
        if (selectedFiles.isEmpty()) return
        
        try {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                val uris = selectedFiles.map { Uri.fromFile(it) }
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                putExtra(Intent.EXTRA_SUBJECT, "Thermal captures from TC001")
                putExtra(Intent.EXTRA_TEXT, "Sharing ${selectedFiles.size} thermal captures")
            }
            startActivity(Intent.createChooser(intent, "Share Files"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to share files", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateFileCount() {
        // This will be called after adapter updates to show current file count
        binding.apply {
            // Update UI based on current adapter state if needed
        }
    }
}