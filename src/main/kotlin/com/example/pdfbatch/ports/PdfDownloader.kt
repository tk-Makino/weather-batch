package com.example.pdfbatch.ports

/**
 * PDF ダウンロードポート（出力ポート）
 */
interface PdfDownloader {
    /**
     * 指定URLからPDFをダウンロード
     * @return PDFのバイト配列、またはnull（ダウンロード失敗時）
     */
    fun download(url: String): ByteArray?
}


