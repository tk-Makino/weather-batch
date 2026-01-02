package com.example.pdfbatch.application

import com.example.pdfbatch.ports.PdfDownloader
import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * PDF取得アプリケーションサービス(ユースケース)
 */
class PdfFetchService(
    private val pdfDownloader: PdfDownloader,
    private val storage: Storage,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 複数URLからPDFを取得
     *
     * @param urls 取得するPDFのURLリスト
     * @param timeSlot 時間帯 (00 or 12)
     */
    fun fetchMultiple(urls: List<String>, timeSlot: String) {
        logger.info("Starting batch fetch for ${urls.size} URLs (timeSlot: $timeSlot)")
        urls.forEach { url ->
            try {
                fetchAndSaveWeatherMap(url, timeSlot)
            } catch (e: Exception) {
                logger.error("Error fetching PDF from $url", e)
            }
        }
        logger.info("Batch fetch completed for timeSlot: $timeSlot")
    }

    /**
     * 指定URLからPDFを取得し、差分があれば保存
     *
     * @param url 取得するPDFのURL
     * @param timeSlot 時間帯 (00 or 12)
     */
    private fun fetchAndSaveWeatherMap(
        url: String,
        timeSlot: String,
        ) {
        // 1. PDFをダウンロード
        val pdfData = pdfDownloader.download(url)
        if (pdfData == null) {
            logger.warn("Failed to download PDF from: $url")
            return
        }
        // 2. ファイルの保存
        val timestamp = LocalDateTime.now(ZoneOffset.UTC)
        val filename = generateFilename(url)
        val directoryPath = generateDirectoryPath(timestamp, timeSlot)
        val relativePath = "$directoryPath/$filename"
        if (!storage.saveFileToS3(relativePath, pdfData)) {
            logger.error("Failed to save PDF: $relativePath")
            return
        }
        logger.info("Saved PDF as: $relativePath")
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
     * @param timeSlot 時間帯 (00 or 12)
     * @return "YYYY/MM/DD/HH"形式の相対パス
     */
    private fun generateDirectoryPath(
        timestamp: LocalDateTime,
        timeSlot: String
    ): String {
        val format = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return "${timestamp.format(format)}/$timeSlot"
    }
}
