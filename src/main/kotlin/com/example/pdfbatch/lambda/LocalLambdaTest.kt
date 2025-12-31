package com.example.pdfbatch.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent

/**
 * ローカルでLambdaハンドラーをテストするための実行ファイル
 * SAMやServerless Frameworkなしで動作確認できます
 */
fun main() {
    println("=".repeat(60))
    println("Weather Batch Lambda - Local Test")
    println("=".repeat(60))
    
    // 環境変数を設定（実際の値に変更してください）
    val testConfig = mapOf(
        "PDF_URLS" to "https://www.jma.go.jp/bosai/numericmap/data/nwpmap/fupa252_00.pdf",
        "S3_BUCKET_NAME" to (System.getenv("S3_BUCKET_NAME") ?: "your-test-bucket-name"),
        "AWS_REGION" to "ap-northeast-1",
        "S3_PREFIX" to "pdfs/",
        "S3_METADATA_KEY" to "pdfs/metadata.json"
    )
    
    testConfig.forEach { (key, value) ->
        System.setProperty(key, value)
        println("$key = $value")
    }
    println("=".repeat(60))
    
    // AWS認証情報の確認
    val awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID")
    val awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY")
    
    if (awsAccessKey.isNullOrBlank() || awsSecretKey.isNullOrBlank()) {
        println("⚠️  WARNING: AWS credentials not found in environment variables")
        println("   Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY")
        println("   Or configure AWS CLI: aws configure")
    } else {
        println("✅ AWS credentials found")
    }
    println("=".repeat(60))
    
    // モックのScheduledEventを作成
    val event = ScheduledEvent().apply {
        id = "local-test-event-${System.currentTimeMillis()}"
        time = java.time.Instant.now().toString()
        region = "ap-northeast-1"
        source = "local.test"
        detailType = "Scheduled Event"
    }
    
    // モックのContextを作成
    val context = MockLambdaContext()
    
    println("\n🚀 Starting Lambda handler execution...\n")
    
    try {
        // Lambdaハンドラーを実行
        val handler = LambdaHandler()
        val result = handler.handleRequest(event, context)
        
        println("\n" + "=".repeat(60))
        println("✅ Execution completed successfully!")
        println("Result: $result")
        println("=".repeat(60))
    } catch (e: Exception) {
        println("\n" + "=".repeat(60))
        println("❌ Execution failed with error:")
        println("=".repeat(60))
        e.printStackTrace()
    }
}

/**
 * テスト用のモックContext
 */
class MockLambdaContext : Context {
    override fun getAwsRequestId() = "local-test-request-${System.currentTimeMillis()}"
    override fun getLogGroupName() = "/aws/lambda/weather-batch-local"
    override fun getLogStreamName() = "local-test-stream"
    override fun getFunctionName() = "weather-batch-local-test"
    override fun getFunctionVersion() = "local"
    override fun getInvokedFunctionArn() = "arn:aws:lambda:local:000000000000:function:weather-batch-local"
    override fun getIdentity() = null
    override fun getClientContext() = null
    override fun getRemainingTimeInMillis() = 300000
    override fun getMemoryLimitInMB() = 512
    override fun getLogger() = LambdaLogger { message -> 
        println("[Lambda] $message") 
    }
}
