package com.example.pdfbatch.adapters.storage

import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists

/**
 * ファイルシステムを使用したストレージ実装
 */
@Component
class FileSystemStorage(
    @Value("\${pdf.storage.directory:./data/pdfs}") private val storageDirectory: String
) : Storage {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val basePath: Path = Path.of(storageDirectory)

    init {
        // ディレクトリが存在しない場合は作成
        if (!basePath.exists()) {
            Files.createDirectories(basePath)
            logger.info("Created storage directory: $storageDirectory")
        }
    }

    override fun save(filename: String, data: ByteArray): Boolean {
        return try {
            val filePath = basePath.resolve(filename)
            Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            logger.info("Saved file: $filename (${data.size} bytes)")
            true
        } catch (e: Exception) {
            logger.error("Error saving file: $filename", e)
            false
        }
    }

    override fun exists(filename: String): Boolean {
        val filePath = basePath.resolve(filename)
        return filePath.exists()
    }
}

