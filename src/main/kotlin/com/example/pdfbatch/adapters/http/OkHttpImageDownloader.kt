package com.example.pdfbatch.adapters.http

import com.example.pdfbatch.ports.ImageDownloader
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

/**
 * OkHttpを使用した画像ダウンローダー実装
 */
class OkHttpImageDownloader(
    private val okHttpClient: OkHttpClient
) : ImageDownloader {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private val SUPPORTED_IMAGE_TYPES = setOf(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/gif",
            "image/webp",
            "image/bmp"
        )
    }

    override fun download(url: String): ByteArray? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Image-Batch-Downloader/1.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.warn("HTTP request failed with code ${response.code} for URL: $url")
                    return null
                }

                val contentType = response.header("Content-Type")
                if (contentType != null && !isImageContentType(contentType)) {
                    logger.warn("Content-Type is not a supported image format: $contentType for URL: $url")
                }

                response.body?.bytes()
            }
        } catch (e: Exception) {
            logger.error("Error downloading image from: $url", e)
            null
        }
    }

    /**
     * Content-Typeが画像形式かチェック
     */
    private fun isImageContentType(contentType: String): Boolean {
        return SUPPORTED_IMAGE_TYPES.any { contentType.contains(it, ignoreCase = true) }
    }
}

