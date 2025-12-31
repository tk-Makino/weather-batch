package com.example.pdfbatch.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * ストレージ設定
 * 
 * ストレージタイプ（filesystem/s3）に応じて、
 * 適切なStorageアダプターがSpringによって自動的にDIされます。
 * 
 * - FileSystemStorage: @ConditionalOnProperty(name = ["pdf.storage.type"], havingValue = "filesystem", matchIfMissing = true)
 * - S3Storage: @ConditionalOnProperty(name = ["pdf.storage.type"], havingValue = "s3")
 */
@Configuration
@EnableConfigurationProperties(StorageProperties::class)
class StorageConfig {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Storage configuration initialized")
    }
}

/**
 * ストレージ設定プロパティ
 */
@ConfigurationProperties(prefix = "pdf.storage")
data class StorageProperties(
    val type: String = "filesystem",
    val directory: String = "./data/pdfs",
    val s3: S3Properties = S3Properties()
)

/**
 * S3設定プロパティ
 */
@ConfigurationProperties(prefix = "pdf.storage.s3")
data class S3Properties(
    val bucketName: String? = null,
    val region: String? = null,
    val prefix: String? = null,
)
