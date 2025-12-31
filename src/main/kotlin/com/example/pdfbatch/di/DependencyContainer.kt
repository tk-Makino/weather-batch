package com.example.pdfbatch.di

import com.example.pdfbatch.adapters.http.OkHttpPdfDownloader
import com.example.pdfbatch.adapters.storage.S3Storage
import com.example.pdfbatch.application.PdfFetchService
import com.example.pdfbatch.config.AppConfig
import com.example.pdfbatch.config.S3Properties
import com.example.pdfbatch.config.StorageProperties
import okhttp3.OkHttpClient
import java.time.Duration

/**
 * 手動DIコンテナ
 * 全ての依存関係を管理する
 */
object DependencyContainer {
    
    private val config: AppConfig by lazy {
        AppConfig.fromEnvironment()
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .build()
    }
    
    private val downloader by lazy {
        OkHttpPdfDownloader(okHttpClient)
    }
    
    private val storageProperties by lazy {
        StorageProperties(
            type = "s3",
            directory = "",
            s3 = S3Properties(
                bucketName = config.s3BucketName,
                region = config.awsRegion,
                prefix = config.s3Prefix
            )
        )
    }
    
    private val storage by lazy {
        S3Storage(storageProperties)
    }
    
    val pdfFetchService: PdfFetchService by lazy {
        PdfFetchService(
            pdfDownloader = downloader,
            storage = storage
        )
    }
}
