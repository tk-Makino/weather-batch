package com.example.pdfbatch.application

import com.example.pdfbatch.domain.Metadata
import com.example.pdfbatch.domain.MetadataCollection
import com.example.pdfbatch.ports.MetadataRepository
import com.example.pdfbatch.ports.PdfDownloader
import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * PDF取得アプリケーションサービス（ユースケース）
 */
@Service
class PdfFetchService(
    private val pdfDownloader: PdfDownloader,
    private val storage: Storage,
    private val metadataRepository: MetadataRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 指定URLからPDFを取得し、差分があれば保存
     */
    fun fetchAndSaveIfChanged(url: String) {
        logger.info("Fetching PDF from: $url")

        // 1. PDFをダウンロード
        val pdfData = pdfDownloader.download(url)
        if (pdfData == null) {
            logger.warn("Failed to download PDF from: $url")
            return
        }

        logger.info("Downloaded ${pdfData.size} bytes from: $url")

        // 2. ハッシュ値を計算
        val hash = calculateHash(pdfData)

        // 3. 既存のメタデータをチェック
        val existingMetadata = metadataRepository.findByUrl(url)

        if (existingMetadata != null && existingMetadata.hash == hash) {
            logger.info("No changes detected for: $url (hash: $hash)")
            return
        }

        // 4. 差分があるため保存
        val timestamp = LocalDateTime.now()
        val filename = generateFilename(url, timestamp)

        val saved = storage.save(filename, pdfData)
        if (!saved) {
            logger.error("Failed to save PDF: $filename")
            return
        }

        logger.info("Saved PDF as: $filename")

        // 5. メタデータを更新
        val metadata = Metadata(
            url = url,
            filename = filename,
            hash = hash,
            downloadedAt = timestamp,
            size = pdfData.size.toLong()
        )

        metadataRepository.save(metadata)
        logger.info("Updated metadata for: $url")
    }

    /**
     * 複数URLからPDFを取得
     */
    fun fetchMultiple(urls: List<String>) {
        logger.info("Starting batch fetch for ${urls.size} URLs")
        urls.forEach { url ->
            try {
                fetchAndSaveIfChanged(url)
            } catch (e: Exception) {
                logger.error("Error fetching PDF from $url", e)
            }
        }
        logger.info("Batch fetch completed")
    }

    /**
     * SHA-256ハッシュを計算
     */
    private fun calculateHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * ファイル名を生成（タイムスタンプ付き）
     */
    private fun generateFilename(url: String, timestamp: LocalDateTime): String {
        val dateStr = timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val urlHash = url.hashCode().toString(16).takeLast(6)
        return "pdf_${dateStr}_${urlHash}.pdf"
    }
}

