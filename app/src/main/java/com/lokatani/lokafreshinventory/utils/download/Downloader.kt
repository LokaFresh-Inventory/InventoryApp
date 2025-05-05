package com.lokatani.lokafreshinventory.utils.download

interface Downloader {
    fun downloadFile(url: String): Long
}