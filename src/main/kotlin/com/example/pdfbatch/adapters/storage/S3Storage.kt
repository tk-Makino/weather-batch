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

    override fun saveFileToS3(filename: String, data: ByteArray): Boolean {
        return try {
            val key = buildKey(filename)
            val putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .build()

            val response = s3Client.putObject(putRequest, RequestBody.fromBytes(data))
            // Verify the ETag is present to confirm successful upload
            if (response.eTag() != null) {
                logger.info("Saved file to S3: $key (${data.size} bytes)")
                true
            } else {
                logger.error("S3 upload completed but no ETag returned for: $filename")
                false
            }
        } catch (e: S3Exception) {
            logger.error("S3 error saving file: $filename", e)
            false
        } catch (e: Exception) {
            logger.error("Error saving file to S3: $filename", e)
            false
        }
    }

    override fun exists(filename: String): Boolean {
        return try {
            val key = buildKey(filename)
            val headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            s3Client.headObject(headRequest)
            true
        } catch (e: NoSuchKeyException) {
            false
        } catch (e: S3Exception) {
            logger.error("S3 error checking file existence: $filename", e)
            false
        } catch (e: Exception) {
            logger.error("Error checking file existence in S3: $filename", e)
            false
        }
    }

    override fun createDirectory(directory: String): Boolean {
        // S3はディレクトリの概念がないため、常にtrueを返す
        // プレフィックスとしてディレクトリ構造を管理
        logger.debug("S3 does not require directory creation: $directory")
        return true
    }

    override fun existDirectory(directory: String): Boolean {
        // S3ではディレクトリの存在確認は不要
        // プレフィックスに該当するオブジェクトがあるかチェック
        return try {
            // ディレクトリにはトレーリングスラッシュを追加
            val directoryKey = if (directory.endsWith("/")) directory else "$directory/"
            val key = buildKey(directoryKey)
            val listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(key)
                .maxKeys(1)
                .build()

            val response = s3Client.listObjectsV2(listRequest)
            response.contents().isNotEmpty()
        } catch (e: S3Exception) {
            logger.error("S3 error checking directory existence: $directory", e)
            false
        } catch (e: Exception) {
            logger.error("Error checking directory existence in S3: $directory", e)
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
