package com.example.pdfbatch.config

/**
 * アプリケーション設定
 * 環境変数から設定を読み取る
 */
data class AppConfig(
    val pdfUrls: List<String>,
    val s3BucketName: String,
    val awsRegion: String,
    val s3Prefix: String,
    val s3MetadataKey: String
) {
    companion object {
        private const val DEFAULT_PDF_URL = "https://www.jma.go.jp/bosai/numericmap/data/nwpmap/fupa252_00.pdf"
        
        /**
         * 環境変数から設定を読み取る
         */
        fun fromEnvironment(): AppConfig {
            val pdfUrlsStr = System.getenv("PDF_URLS") ?: DEFAULT_PDF_URL
            
            return AppConfig(
                pdfUrls = pdfUrlsStr.split(",").map { it.trim() },
                s3BucketName = System.getenv("S3_BUCKET_NAME") 
                    ?: throw IllegalStateException("S3_BUCKET_NAME environment variable is required"),
                awsRegion = System.getenv("AWS_REGION") ?: "ap-northeast-1",
                s3Prefix = System.getenv("S3_PREFIX") ?: "pdfs/",
                s3MetadataKey = System.getenv("S3_METADATA_KEY") ?: "pdfs/metadata.json"
            )
        }
    }
}
