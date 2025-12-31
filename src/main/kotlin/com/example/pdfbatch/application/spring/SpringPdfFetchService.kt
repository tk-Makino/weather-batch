package com.example.pdfbatch.application.spring

import com.example.pdfbatch.application.PdfFetchService
import com.example.pdfbatch.ports.PdfDownloader
import com.example.pdfbatch.ports.Storage
import org.springframework.stereotype.Service

/**
 * PdfFetchService の Spring Boot 用アダプター
 */
@Service
class SpringPdfFetchService(
    pdfDownloader: PdfDownloader,
    storage: Storage
) : PdfFetchService(pdfDownloader, storage)
