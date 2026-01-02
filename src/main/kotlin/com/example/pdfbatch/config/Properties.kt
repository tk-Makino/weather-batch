package com.example.pdfbatch.config

/**
 * ストレージ設定プロパティ
 */
data class StorageProperties(
    val type: String = "s3",
    val directory: String = "",
    val s3: S3Properties = S3Properties()
)

/**
 * S3設定プロパティ
 */
data class S3Properties(
    val bucketName: String? = null,
    val region: String? = null,
    val prefix: String? = null,
)

/**
 * PDF取得プロパティ
 *
 * @property pdfUrls00
 * @property pdfUrls12
 */
data class PdfFetchProperties(
    val pdfUrls00: List<String> = listOf(),
    val pdfUrls12: List<String> = listOf(),
)

/**
 * 画像取得プロパティ
 *
 * @property imageUrls
 */
data class ImageFetchProperties(
    val imageUrls: List<String> = listOf(),
)

