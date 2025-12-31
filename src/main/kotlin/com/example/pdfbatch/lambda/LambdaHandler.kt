package com.example.pdfbatch.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.example.pdfbatch.di.DependencyContainer
import org.slf4j.LoggerFactory

/**
 * AWS Lambda ハンドラー
 * EventBridge (CloudWatch Events) からのスケジュール実行を処理
 */
class LambdaHandler : RequestHandler<ScheduledEvent, String> {
    
    private val logger = LoggerFactory.getLogger(LambdaHandler::class.java)
    private val pdfFetchService = DependencyContainer.pdfFetchService
    
    override fun handleRequest(event: ScheduledEvent, context: Context): String {
        logger.info("Lambda function invoked. Request ID: ${context.awsRequestId}")
        logger.info("Event: ${event.id}, Time: ${event.time}")
        
        return try {
            val config = com.example.pdfbatch.config.AppConfig.fromEnvironment()
            config.pdfUrls.forEach { url ->
                try {
                    pdfFetchService.fetchMultiple(listOf(url))
                } catch (e: Exception) {
                    logger.error("Error fetching PDF from $url", e)
                }
            }
            logger.info("PDF fetch completed successfully")
            "Success: PDF fetch completed"
        } catch (e: Exception) {
            logger.error("Error during PDF fetch", e)
            "Error: ${e.message}"
        }
    }
}
