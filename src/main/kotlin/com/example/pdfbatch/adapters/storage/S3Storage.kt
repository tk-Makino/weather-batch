package com.example.pdfbatch.adapters.storage

import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*

/**
 * S3を使用したストレージ実装
 */
@Component
@ConditionalOnProperty(name = ["pdf.storage.type"], havingValue = "s3")
class S3Storage(
    @Value("\${pdf.storage.s3.bucket-name}") private val bucketName: String,
    @Value("\${pdf.storage.s3.region:ap-northeast-1}") private val regionName: String,
    @Value("\${pdf.storage.s3.prefix:pdfs/}") private val prefix: String
) : Storage {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val s3Client: S3Client = S3Client.builder()
        .region(Region.of(regionName))
        .build()

    init {
        logger.info("Initialized S3Storage with bucket: $bucketName, region: $regionName, prefix: $prefix")
    }

    override fun save(filename: String, data: ByteArray): Boolean {
        return try {
            val key = buildKey(filename)
            val putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .build()

            s3Client.putObject(putRequest, RequestBody.fromBytes(data))
            logger.info("Saved file to S3: $key (${data.size} bytes)")
            true
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
            val key = buildKey(directory)
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
        val normalizedFilename = if (filename.startsWith("/")) filename.substring(1) else filename
        return "$normalizedPrefix$normalizedFilename"
    }
}
