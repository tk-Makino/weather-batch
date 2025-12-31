package com.example.pdfbatch.adapters.storage.spring

import com.example.pdfbatch.adapters.storage.S3Storage
import com.example.pdfbatch.config.StorageProperties
import com.example.pdfbatch.ports.Storage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * S3Storage の Spring Boot 用アダプター
 * StorageProperties から設定を読み込み、S3Storage を委譲する
 */
@Component
@ConditionalOnProperty(name = ["pdf.storage.type"], havingValue = "s3")
class SpringS3Storage(
    properties: StorageProperties
) : Storage {

    private val delegate = S3Storage(
        bucketName = properties.s3.bucketName ?: "",
        region = properties.s3.region ?: "ap-northeast-1",
        prefix = properties.s3.prefix ?: ""
    )

    override fun saveFileToS3(filename: String, data: ByteArray): Boolean {
        return delegate.saveFileToS3(filename, data)
    }

    override fun exists(filename: String): Boolean {
        return delegate.exists(filename)
    }

    override fun createDirectory(directory: String): Boolean {
        return delegate.createDirectory(directory)
    }

    override fun existDirectory(directory: String): Boolean {
        return delegate.existDirectory(directory)
    }
}
