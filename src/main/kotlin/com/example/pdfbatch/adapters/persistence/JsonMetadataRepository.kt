package com.example.pdfbatch.adapters.persistence

import com.example.pdfbatch.domain.Metadata
import com.example.pdfbatch.domain.MetadataCollection
import com.example.pdfbatch.ports.MetadataRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists

/**
 * JSONファイルを使用したメタデータリポジトリ実装
 */
@Component
class JsonMetadataRepository(
    @Value("\${pdf.storage.directory:./data/pdfs}") private val storageDirectory: String,
    @Value("\${pdf.metadata.filename:metadata.json}") private val metadataFilename: String
) : MetadataRepository {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val basePath: Path = Path.of(storageDirectory)
    private val metadataPath: Path = basePath.resolve(metadataFilename)

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        enable(SerializationFeature.INDENT_OUTPUT)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    init {
        // ディレクトリが存在しない場合は作成
        if (!basePath.exists()) {
            Files.createDirectories(basePath)
            logger.info("Created storage directory: $storageDirectory")
        }

        // メタデータファイルが存在しない場合は空のコレクションを作成
        if (!metadataPath.exists()) {
            saveAll(MetadataCollection())
            logger.info("Created metadata file: $metadataFilename")
        }
    }

    override fun findAll(): MetadataCollection {
        return try {
            if (!metadataPath.exists()) {
                return MetadataCollection()
            }

            val json = Files.readString(metadataPath)
            objectMapper.readValue(json, MetadataCollection::class.java)
        } catch (e: Exception) {
            logger.error("Error reading metadata file", e)
            MetadataCollection()
        }
    }

    override fun findByUrl(url: String): Metadata? {
        val collection = findAll()
        return collection.items.findLast { it.url == url }
    }

    override fun save(metadata: Metadata) {
        val collection = findAll()
        val updatedItems = collection.items + metadata
        saveAll(MetadataCollection(updatedItems))
    }

    override fun saveAll(collection: MetadataCollection) {
        try {
            val json = objectMapper.writeValueAsString(collection)
            Files.writeString(
                metadataPath,
                json,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            logger.debug("Saved metadata: ${collection.items.size} items")
        } catch (e: Exception) {
            logger.error("Error writing metadata file", e)
        }
    }
}

