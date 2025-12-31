package com.example.pdfbatch.adapters.storage

import com.example.pdfbatch.ports.Storage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists

/**
 * ファイルシステムを使用したストレージ実装
 */
@Component
@ConditionalOnProperty(name = ["pdf.storage.type"], havingValue = "filesystem", matchIfMissing = true)
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
            // 親ディレクトリがなければ作成
            filePath.parent?.let { parent ->
                if (!parent.exists()) {
                    Files.createDirectories(parent)
                }
            }
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

    override fun createDirectory(directory: String): Boolean {
        return try {
            val dirPath = basePath.resolve(directory)
            if (!dirPath.exists()) {
                Files.createDirectories(dirPath)
                logger.info("Created directory: $dirPath")
            }
            true
        } catch (e: Exception) {
            logger.error("Error creating directory: $directory", e)
            false
        }
    }

    override fun existDirectory(directory: String): Boolean {
        val dirPath = basePath.resolve(directory)
        return dirPath.exists() && Files.isDirectory(dirPath)
    }
}
