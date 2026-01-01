package com.example.pdfbatch.application

import com.example.pdfbatch.ports.PdfDownloader
import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * PDF取得アプリケーションサービス（ユースケース）
 */
class PdfFetchService(
    private val pdfDownloader: PdfDownloader,
    private val storage: Storage,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 複数URLからPDFを取得
     */
    fun fetchMultiple(urls: List<String>) {
        logger.info("Starting batch fetch for ${'$'}{urls.size} URLs")
        urls.forEach { url ->
            try {
                fetchAndSaveWeatherMap(url)
            } catch (e: Exception) {
                logger.error("Error fetching PDF from $url", e)
            }
        }
        logger.info("Batch fetch completed")
    }

    /**
     * 指定URLからPDFを取得し、差分があれば保存
     */
    private fun fetchAndSaveWeatherMap(url: String) {
        logger.info("Fetching PDF from: $url")

        // 1. PDFをダウンロード
        val pdfData = pdfDownloader.download(url)
        if (pdfData == null) {
            logger.warn("Failed to download PDF from: $url")
            return
        }
        logger.info("Downloaded ${'$'}{pdfData.size} bytes from: $url")

        // 2. ファイルの保存
        val timestamp = LocalDateTime.now(ZoneOffset.UTC)
        val filename = generateFilename(url)
        val directoryPath = generateDirectoryPath(timestamp)
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
     * @return "YYYY/MM/DD"形式の相対パス
     */
    private fun generateDirectoryPath(
        timestamp: LocalDateTime
    ): String {
        val format = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return timestamp.format(format)
        // TODO hh (00/12)を追加する必要ある
    }
}
