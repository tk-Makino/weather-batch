package com.example.pdfbatch.config

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * OkHttp クライアント設定
 */
@Configuration
class OkHttpConfig {

    @Bean
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .followRedirects(true)
            .build()
    }
}

