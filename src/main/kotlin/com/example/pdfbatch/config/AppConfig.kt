package com.example.pdfbatch.config

/**
 * アプリケーション設定
 * 環境変数から設定を読み取る
 */
data class AppConfig(
    val pdfUrls00: List<String>,
    val pdfUrls12: List<String>,
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

            return AppConfig(
                pdfUrls00 = pdfUrls00Str.split(",").map { it.trim() },
                pdfUrls12 = pdfUrls12Str.split(",").map { it.trim() },
                s3BucketName = System.getenv("S3_BUCKET_NAME")
                    ?: throw IllegalStateException("S3_BUCKET_NAME environment variable is required"),
                awsRegion = System.getenv("AWS_REGION") ?: "ap-northeast-1",
                s3Prefix = System.getenv("S3_PREFIX") ?: "pdfs/",
                s3MetadataKey = System.getenv("S3_METADATA_KEY") ?: "pdfs/metadata.json"
            )
        }
    }

    /**
     * timeSlotに応じたURLリストを取得
     */
    fun getUrlsForTimeSlot(timeSlot: String): List<String> {
        return when (timeSlot) {
            "00" -> pdfUrls00
            "12" -> pdfUrls12
            else -> throw IllegalArgumentException("Invalid timeSlot: $timeSlot. Must be '00' or '12'")
        }
    }
}

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
