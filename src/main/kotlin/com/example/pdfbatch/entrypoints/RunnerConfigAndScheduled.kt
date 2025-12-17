package com.example.pdfbatch.entrypoints

import com.example.pdfbatch.application.PdfFetchService
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
     * URLリストの取得
     * application.ymlの pdf.urls からカンマ区切りで取得
     * 空白や空のエントリは除外される
     */
    private val urls: List<String> by lazy {
        val urlsString = env.getProperty("pdf.urls", "")
        if (urlsString.isBlank()) {
            logger.warn("No PDF URLs configured in application.yml (pdf.urls)")
            emptyList()
        } else {
            urlsString.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .also {
                    logger.info("Configured ${it.size} PDF URL(s): ${it.joinToString(", ")}")
                }
        }
    }

    /**
     * アプリケーション起動時の実行
     */
    override fun run(args: ApplicationArguments) {
        val runOnStartup = env.getProperty("pdf.fetch.run-on-startup", Boolean::class.java, false)
        if (runOnStartup) {
            logger.info("Running PDF fetch on startup")
            executeFetch()
        } else {
            logger.info("Skipping startup fetch (run-on-startup is disabled)")
        }
    }

    /**
     * 設定ファイルで設定された時間に定期実行
     */
    @Scheduled(cron = "\${pdf.fetch.cron}")
    fun scheduledFetch() {
        logger.info("Running scheduled PDF fetch")
        executeFetch()
    }

    /**
     * PDF取得の共通処理
     */
    private fun executeFetch() {
        if (urls.isEmpty()) {
            logger.warn("No URLs configured for PDF fetching")
            return
        }

        logger.info("Fetching PDFs from ${urls.size} URL(s)")
        pdfFetchService.fetchMultiple(urls)
        logger.info("PDF fetch completed")
    }
}

