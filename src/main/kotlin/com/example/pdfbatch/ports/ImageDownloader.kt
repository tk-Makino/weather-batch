package com.example.pdfbatch.ports

/**
 * 画像ダウンロードポート（出力ポート）
 */
interface ImageDownloader {
    /**
     * 指定URLから画像をダウンロード
     * @param url ダウンロード対象のURL
     * @return 画像のバイト配列、またはnull（ダウンロード失敗時）
     */
    fun download(url: String): ByteArray?
}

