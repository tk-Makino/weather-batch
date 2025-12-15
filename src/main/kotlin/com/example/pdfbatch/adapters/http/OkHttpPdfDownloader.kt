package com.example.pdfbatch.adapters.http

import com.example.pdfbatch.ports.PdfDownloader
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * OkHttpを使用したPDFダウンローダー実装
 */
@Component
class OkHttpPdfDownloader(
    private val okHttpClient: OkHttpClient
) : PdfDownloader {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun download(url: String): ByteArray? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "PDF-Batch-Downloader/1.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.warn("HTTP request failed with code ${response.code} for URL: $url")
                    return null
                }

                val contentType = response.header("Content-Type")
                if (contentType != null && !contentType.contains("pdf", ignoreCase = true)) {
                    logger.warn("Content-Type is not PDF: $contentType for URL: $url")
                }

                response.body?.bytes()
            }
        } catch (e: Exception) {
            logger.error("Error downloading PDF from: $url", e)
            null
        }
    }
}

