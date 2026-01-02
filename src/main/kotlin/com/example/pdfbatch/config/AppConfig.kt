package com.example.pdfbatch.config

import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * アプリケーション設定
 * 環境変数から設定を読み取る
 */
data class AppConfig(
    val pdfUrls: PdfFetchProperties,
    val imageUrls: ImageFetchProperties,
    val s3BucketName: String,
    val awsRegion: String,
    val s3Prefix: String,
    val s3MetadataKey: String
) {
    companion object {
        private const val DEFAULT_PDF_URL_00 = "https://www.jma.go.jp/bosai/numericmap/data/nwpmap/fupa252_00.pdf"
        private const val DEFAULT_PDF_URL_12 = "https://www.jma.go.jp/bosai/numericmap/data/nwpmap/fupa252_12.pdf"

        /**
         * 環境変数から設定を読み取る
         */
        fun fromEnvironment(): AppConfig {
            val pdfUrls00Str = System.getenv("PDF_URLS_00") ?: DEFAULT_PDF_URL_00
            val pdfUrls12Str = System.getenv("PDF_URLS_12") ?: DEFAULT_PDF_URL_12
            val imageUrlsTemplate = System.getenv("IMAGE_URLS") ?: ""

            return AppConfig(
                pdfUrls = PdfFetchProperties(
                    pdfUrls00 = pdfUrls00Str.split(",").map { it.trim() },
                    pdfUrls12 = pdfUrls12Str.split(",").map { it.trim() }
                ),
                imageUrls = ImageFetchProperties(
                    imageUrls = generateImageUrls(imageUrlsTemplate),
                ),
                s3BucketName = System.getenv("S3_BUCKET_NAME")
                    ?: throw IllegalStateException("S3_BUCKET_NAME environment variable is required"),
                awsRegion = System.getenv("AWS_REGION") ?: "ap-northeast-1",
                s3Prefix = System.getenv("S3_PREFIX") ?: "pdfs/",
                s3MetadataKey = System.getenv("S3_METADATA_KEY") ?: "pdfs/metadata.json"
            )
        }

        /**
         * 画像URLを生成
         * テンプレート文字列 {yyyyMM}, {yyyyMMdd} を UTC時刻-1日で置換
         *
         * @param urlTemplates カンマ区切りのURLテンプレート文字列
         * @return 生成された画像URLリスト
         */
        private fun generateImageUrls(urlTemplates: String): List<String> {
            val yesterday = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1)
            val yyyyMM = yesterday.format(DateTimeFormatter.ofPattern("yyyyMM"))
            val yyyyMMdd = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

            return urlTemplates.split(",").map { template ->
                template.trim()
                    .replace("{yyyyMM}", yyyyMM)
                    .replace("{yyyyMMdd}", yyyyMMdd)
            }
        }
    }

    /**
     * timeSlotに応じたURLリストを取得
     *
     * @param timeSlot 時間帯
     * @return URLリスト
     */
    fun getUrlsForTimeSlot(timeSlot: String): List<String> {
        return when (timeSlot) {
            "00" -> pdfUrls.pdfUrls00
            "12" -> pdfUrls.pdfUrls12
            else -> throw IllegalArgumentException("Invalid timeSlot: $timeSlot. Must be '00' or '12'")
        }
    }

    /**
     * timeSlotに応じた画像URLリストを取得
     *
     * @param timeSlot 時間帯
     * @return 画像URLリスト
     */
    fun getImageUrlsForTimeSlot(
        timeSlot: String
    ): List<String> {
        return when (timeSlot) {
            "00" -> imageUrls.imageUrls
            else -> emptyList()
        }
    }
}
