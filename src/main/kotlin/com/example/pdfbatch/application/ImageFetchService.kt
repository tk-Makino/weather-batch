package com.example.pdfbatch.application

import com.example.pdfbatch.ports.ImageDownloader
import com.example.pdfbatch.ports.PdfDownloader
import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * 画像取得アプリケーションサービス(ユースケース)
 */
class ImageFetchService(
    private val imageDownloader: ImageDownloader,
    private val storage: Storage,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val prefix = "surface-weather-map"

    /**
     * 複数URL(地上気象天気図)から画像を取得
     *
     * @param urls 取得するPDFのURLリスト
     */
    fun fetchMultipleSurfaceWeatherMapImages(urls: List<String>) {
        logger.info("Starting batch fetch for ${urls.size} URLs")
        urls.forEach { url ->
            try {
                fetchAndSaveWeatherMapImage(url)
            } catch (e: Exception) {
                logger.error("Error fetching Image from $url", e)
            }
        }
        logger.info("Batch fetch completed.")
    }

    /**
     * 地上気象天気図を取得（画像）
     *
     * @param url 取得する画像のURL
     */
    private fun fetchAndSaveWeatherMapImage(
        url: String,
        ) {
        // 1. 画像をダウンロード
        val imageData = imageDownloader.download(url)
        if (imageData == null) {
            logger.warn("Failed to download image from: $url")
            return
        }
        // 2. ファイルの保存
        val timestamp = LocalDateTime.now(ZoneOffset.UTC)
        val filename = generateFilename(url)
        val directoryPath = generateDirectoryPath(timestamp.minusDays(1)) // 前日の天気図を保存
        val relativePath = "$directoryPath/$filename"
        if (!storage.saveImageToS3(relativePath, imageData)) {
            logger.error("Failed to save Image: $relativePath")
            return
        }
    }

    /**
     * ファイル名を生成
     *
     * @param url PDFのURL
     * @return URLの最後の文字列
     */
    private fun generateFilename(url: String): String {
        return url.substringAfterLast('/')
    }

    /**
     * ディレクトリパスを生成
     *
     * @param timestamp タイムスタンプ
     * @return "YYYY/MM/DD"形式の相対パス
     */
    private fun generateDirectoryPath(
        timestamp: LocalDateTime,
    ): String {
        val format = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return "$prefix/${timestamp.format(format)}"
    }
}
