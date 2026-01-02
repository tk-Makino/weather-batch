package com.example.pdfbatch.adapters.storage

import com.example.pdfbatch.config.StorageProperties
import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*

/**
 * S3を使用したストレージ実装
 */
class S3Storage(
    properties: StorageProperties,
) : Storage {

    /**
     * S3の設定
     */
    private val bucketName: String = properties.s3.bucketName ?: ""
    private val regionName: String = properties.s3.region ?: ""
    private val prefix: String = properties.s3.prefix ?: ""
    private val s3Client: S3Client = S3Client.builder()
        .region(Region.of(regionName))
        .build()

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Initialized S3Storage with bucket: $bucketName, region: $regionName, prefix: $prefix")
    }

    override fun saveFileToS3(
        filename: String,
        data: ByteArray
    ): Boolean {
        return uploadToS3(
            filename,
            data,
            "application/pdf",
            "file"
        )
    }

    override fun saveImageToS3(
        filename: String,
        data: ByteArray
    ): Boolean {
        return uploadToS3(
            filename,
            data,
            "image/png",
            "image"
        )
    }

    /**
     * S3にファイルをアップロードする共通処理
     *
     * @param filename ファイル名
     * @param data ファイルデータ
     * @param contentType コンテンツタイプ
     * @param fileType ログ出力用のファイルタイプ（"file" or "image"）
     * @return アップロード成功時true、失敗時false
     */
    private fun uploadToS3(
        filename: String,
        data: ByteArray,
        contentType: String,
        fileType: String
    ): Boolean {
        return try {
            val key = buildKey(filename)
            val putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build()

            val response = s3Client.putObject(putRequest, RequestBody.fromBytes(data))

            if (response.eTag() != null) {
                logger.info("Saved $fileType to S3: $key (${data.size} bytes)")
                true
            } else {
                logger.error("S3 upload completed but no ETag returned for: $filename")
                false
            }
        } catch (e: S3Exception) {
            logger.error("S3 error saving $fileType: $filename", e)
            false
        } catch (e: Exception) {
            logger.error("Error saving $fileType to S3: $filename", e)
            false
        }
    }
    /**
     * S3のキーを構築（プレフィックス + ファイル名）
     */
    private fun buildKey(filename: String): String {
        val normalizedPrefix = if (prefix.endsWith("/")) prefix else "$prefix/"
        // 複数の先頭スラッシュを除去し、空文字列をチェック
        val normalizedFilename = filename.trimStart('/')
        if (normalizedFilename.isEmpty()) {
            throw IllegalArgumentException("Filename cannot be empty or contain only slashes")
        }
        return "$normalizedPrefix$normalizedFilename"
    }
}
