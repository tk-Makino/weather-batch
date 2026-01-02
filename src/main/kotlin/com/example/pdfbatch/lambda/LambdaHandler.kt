package com.example.pdfbatch.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.example.pdfbatch.config.AppConfig
import com.example.pdfbatch.di.DependencyContainer
import org.slf4j.LoggerFactory

/**
 * AWS Lambda ハンドラー
 * EventBridge (CloudWatch Events) からのスケジュール実行を処理
 */
class LambdaHandler : RequestHandler<Map<String, Any>, String> {

    private val logger = LoggerFactory.getLogger(LambdaHandler::class.java)
    private val pdfFetchService = DependencyContainer.pdfFetchService
    
    override fun handleRequest(event: Map<String, Any>, context: Context): String {
        logger.info("Lambda function invoked. Request ID: ${context.awsRequestId}")
        logger.info("Event: $event")

        return try {
            // EventBridgeから渡されるtimeSlotを取得 (デフォルト: "00")
            val timeSlot = event["timeSlot"] as? String ?: "00"
            val config = AppConfig.fromEnvironment()
            val urls = config.getUrlsForTimeSlot(timeSlot)

            logger.info("Fetching ${urls.size} PDFs for timeSlot: $timeSlot")
            pdfFetchService.fetchMultiple(urls, timeSlot)

            logger.info("PDF fetch completed successfully for timeSlot: $timeSlot")
            "Success: PDF fetch completed for timeSlot $timeSlot"
        } catch (e: Exception) {
            logger.error("Error during PDF fetch", e)
            "Error: ${e.message}"
        }
    }
}
