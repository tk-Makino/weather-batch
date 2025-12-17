package com.example.pdfbatch.entrypoints

import com.example.pdfbatch.application.PdfFetchService
import com.example.pdfbatch.domain.UTC
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * アプリケーション起動時の実行とスケジュール実行を管理
 */
@Component
class RunnerConfigAndScheduled(
    private val pdfFetchService: PdfFetchService,
    private val env: Environment,
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 00UTCで取得するURLリスト
     */
    private val urls00UTC: List<String> by lazy {
        getUrlsFromProperty("pdf.urls.00utc")
    }

    /**
     * 12UTCで取得するURLリスト
     */
    private val urls12UTC: List<String> by lazy {
        getUrlsFromProperty("pdf.urls.12utc")
    }

    /**
     * アプリケーション起動時の実行
     */
    override fun run(args: ApplicationArguments) {
        val runOnStartup = env.getProperty("pdf.fetch.run-on-startup", Boolean::class.java, false)
        if (runOnStartup) {
            logger.info("Running PDF fetch on startup")
            // executeFetch(urls00UTC + urls12UTC)
        } else {
            logger.info("Skipping startup fetch (run-on-startup is disabled)")
        }
    }

    /**
     * 設定ファイルで設定された時間に定期実行(00UTC)
     */
    @Scheduled(cron = "\${pdf.fetch.cron.00utc}")
    fun scheduledFetch00UTC() {
        logger.info("00UTC: Running scheduled PDF fetch")
        executeFetch(
            urls = urls00UTC,
            targetUtc = UTC.UTC_00)
    }

    /**
     * 設定ファイルで設定された時間に定期実行(12UTC)
     */
    @Scheduled(cron = "\${pdf.fetch.cron.12utc}")
    fun scheduledFetch12UTC() {
        logger.info("12UTC: Running scheduled PDF fetch")
        executeFetch(urls12UTC,UTC.UTC_12)
    }

    /**
     * PDF取得の共通処理
     */
    private fun executeFetch(
        urls: List<String>,
        targetUtc: UTC,
    ) {
        if (urls.isEmpty()) {
            logger.warn("No URLs configured for PDF fetching")
            return
        }

        logger.info("Fetching PDFs from ${urls.size} URL(s)")
        pdfFetchService.fetchMultiple(
            targetUrls = urls,
            targetUtc = targetUtc,
            )
        logger.info("PDF fetch completed")
    }


    /**
     * 指定されたプロパティキーからURLリストを取得
     */
    private fun getUrlsFromProperty(propertyKey: String): List<String> {
        val urlsString = env.getProperty(propertyKey, "")
        if (urlsString.isBlank()) {
            logger.warn("No PDF URLs configured in application.yml ($propertyKey)")
            return emptyList()
        }
        return urlsString.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .also {
                logger.info("Configured ${it.size} PDF URL(s) for $propertyKey: ${it.joinToString(", ")}")
            }
    }
}

