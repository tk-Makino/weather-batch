package com.example.pdfbatch.domain

import java.time.LocalDateTime

/**
 * PDFメタデータのドメインモデル
 */
data class Metadata(
    val url: String,
    val filename: String,
    val hash: String,
    val downloadedAt: LocalDateTime,
    val size: Long
)

/**
 * メタデータコレクション
 */
data class MetadataCollection(
    val items: List<Metadata> = emptyList()
)

