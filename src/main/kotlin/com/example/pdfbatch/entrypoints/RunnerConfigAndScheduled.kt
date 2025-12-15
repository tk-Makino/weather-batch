package com.example.pdfbatch.entrypoints

import com.example.pdfbatch.application.PdfFetchService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * アプリケーション起動時の実行とスケジュール実行を管理
 */
@Component
class RunnerConfigAndScheduled(
    private val pdfFetchService: PdfFetchService,
    @Value("\${pdf.urls}") private val urlsString: String,
    @Value("\${pdf.fetch.run-on-startup:true}") private val runOnStartup: Boolean
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val urls: List<String> by lazy {
        urlsString.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * アプリケーション起動時に実行
     */
    override fun run(args: ApplicationArguments) {
        if (runOnStartup) {
            logger.info("Running PDF fetch on startup")
            executeFetch()
        } else {
            logger.info("Skipping startup fetch (run-on-startup is disabled)")
        }
    }

    /**
     * スケジュール実行（デフォルト: 1時間ごと）
     * cronの設定は application.yml で変更可能
     */
    @Scheduled(cron = "\${pdf.fetch.cron:0 0 * * * *}")
    fun scheduledFetch() {
        logger.info("Starting scheduled PDF fetch")
        executeFetch()
    }

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

