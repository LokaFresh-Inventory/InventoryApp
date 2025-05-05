package com.lokatani.lokafreshinventory.utils.download

import android.app.DownloadManager
import android.app.DownloadManager.Request.NETWORK_MOBILE
import android.app.DownloadManager.Request.NETWORK_WIFI
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

class AndroidDownloader(
    context: Context
) : Downloader {
    private var downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("xlxs/xlx/csv")
            .setAllowedNetworkTypes(NETWORK_WIFI or NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("Exporting Data")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "exported_data.csv")
        return downloadManager.enqueue(request)
    }
}