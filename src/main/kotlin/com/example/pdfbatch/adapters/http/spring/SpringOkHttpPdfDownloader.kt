package com.example.pdfbatch.adapters.http.spring

import com.example.pdfbatch.adapters.http.OkHttpPdfDownloader
import okhttp3.OkHttpClient
import org.springframework.stereotype.Component

/**
 * OkHttpPdfDownloader の Spring Boot 用アダプター
 */
@Component
class SpringOkHttpPdfDownloader(
    okHttpClient: OkHttpClient
) : OkHttpPdfDownloader(okHttpClient)
