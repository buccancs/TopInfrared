package com.topdon.transfer

import android.media.MediaScannerConnection
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.UriUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseActivity
import kotlinx.android.synthetic.main.activity_transfer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class TransferActivity : BaseActivity() {

    private lateinit var transferDialog: TransferDialog

    override fun initContentView(): Int = R.layout.activity_transfer

    override fun initView() {
        iv_back.setOnClickListener {
            finish()
        }

        requestPermission()
    }

    override fun initData() {
    }

    private fun requestPermission() {
        XXPermissions.with(this)
            .permission(if (applicationInfo.targetSdkVersion < 33) Permission.READ_EXTERNAL_STORAGE else Permission.READ_MEDIA_IMAGES)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        startTransfer()
                    } else {
                        ToastUtils.showShort(R.string.scan_ble_tip_authorize)
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        TipDialog.Builder(this@TransferActivity)
                            .setTitleMessage(getString(R.string.app_tip))
                            .setMessage(getString(R.string.app_album_content))
                            .setPositiveListener(R.string.app_open) {
                                AppUtils.launchAppDetailsSettings()
                            }
                            .setCancelListener(R.string.app_cancel) {
                            }
                            .setCanceled(true)
                            .create().show()
                    }
                }
            })
    }

    private fun startTransfer() {
        val oldGalleryList: Array<File>? = File(FileConfig.oldTc001GalleryDir).listFiles()

        transferDialog = TransferDialog(this)
        transferDialog.max = oldGalleryList?.size ?: 0
        transferDialog.show()

        lifecycleScope.launch {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            transferIrFiles()
            transferImgFile()
            MediaScannerConnection.scanFile(this@TransferActivity, arrayOf(FileConfig.lineGalleryDir), null, null)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            transferDialog.dismiss()
            cl_success.isVisible = true
        }
    }

    private suspend fun transferIrFiles() {
        withContext(Dispatchers.IO) {
            val irZipFile: File = UriUtils.uri2File(intent.data) ?: return@withContext
            val zipFile = ZipFile(irZipFile)
            val buffer = ByteArray(200 * 1024)

            launch(Dispatchers.Main) {
                transferDialog.max += zipFile.size()
            }

            val enumeration: Enumeration<out ZipEntry> = zipFile.entries()
            while (enumeration.hasMoreElements()) {
                try {
                    val zipEntry: ZipEntry = enumeration.nextElement()
                    if (!zipEntry.isDirectory) {
                        val targetParentFile = File(FileConfig.lineIrGalleryDir)
                        val targetFile = File(targetParentFile, zipEntry.name)
                        if (targetFile.canonicalPath.startsWith(targetParentFile.canonicalPath + File.separator)) {
                            val inputStream: InputStream = zipFile.getInputStream(zipEntry)
                            val fileOutputStream = FileOutputStream(targetFile)
                            var readSize = inputStream.read(buffer)
                            while (readSize != -1) {
                                fileOutputStream.write(buffer, 0, readSize)
                                readSize = inputStream.read(buffer)
                            }
                            inputStream.close()
                            fileOutputStream.close()
                        }
                    }
                } catch (_: Exception) {

                }
                launch(Dispatchers.Main) {
                    transferDialog.progress += 1
                }
            }
        }
    }

    private suspend fun transferImgFile() {
        withContext(Dispatchers.IO) {
            val oldGalleryList: Array<File> = File(FileConfig.oldTc001GalleryDir).listFiles() ?: return@withContext
            oldGalleryList.forEach {
                val newFile = File(FileConfig.lineGalleryDir, it.name)
                if (newFile.exists()) {
                    val oldMd5 = EncryptUtils.encryptMD5File2String(it)
                    val newMd5 = EncryptUtils.encryptMD5File2String(newFile)
                    if (oldMd5 != newMd5) {
                        FileUtils.copy(it, newFile)
                    }
                } else {
                    FileUtils.copy(it, newFile)
                }
                launch(Dispatchers.Main) {
                    transferDialog.progress += 1
                }
            }
        }
    }
}