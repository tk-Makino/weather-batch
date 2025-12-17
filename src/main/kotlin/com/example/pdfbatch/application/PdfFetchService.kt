package com.example.pdfbatch.application

import com.example.pdfbatch.domain.UTC
import com.example.pdfbatch.ports.PdfDownloader
import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * PDF取得アプリケーションサービス（ユースケース）
 */
@Service
class PdfFetchService(
    private val pdfDownloader: PdfDownloader,
    private val storage: Storage,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 複数URLからPDFを取得
     *
     * @param targetUrls PDFのURLリスト
     * @param targetUtc 取得対象のUTC時間
     *
     */
    fun fetchMultiple(
        targetUrls: List<String>,
        targetUtc: UTC,
        ) {
        logger.info("Starting batch fetch for ${'$'}{urls.size} URLs")
        targetUrls.forEach { url ->
            try {
                fetchAndSaveWeatherMap(url,targetUtc)
            } catch (e: Exception) {
                logger.error("Error fetching PDF from $url", e)
            }
        }
        logger.info("Batch fetch completed")
    }

    /**
     * 指定URLからPDFを取得し、差分があれば保存
     *
     * @param url PDFのURL
     * @param targetUtc 取得対象のUTC時間
     */
    private fun fetchAndSaveWeatherMap(
        url: String,
        targetUtc: UTC,
        ) {
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
        val directoryPath = generateDirectoryPath(
            timestamp,
            targetUtc,
        )
        val relativePath = "$directoryPath/$filename"
        if(!storage.existDirectory(directoryPath)) {
            storage.createDirectory(directoryPath)
            logger.info("Directory does not exist. Creating: $directoryPath")
        }
        if (!storage.save(relativePath, pdfData)) {
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
     * @param targetUtc 取得対象のUTC時間
     *
     * @return "YYYY/MM/DD/hh"形式の相対パス
     */
    private fun generateDirectoryPath(
        timestamp: LocalDateTime,
        targetUtc: UTC,
    ): String {
        val format = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return timestamp.format(format) + targetUtc.hour
    }
}
