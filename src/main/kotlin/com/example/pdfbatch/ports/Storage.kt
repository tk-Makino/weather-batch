package com.example.pdfbatch.ports

/**
 * ストレージポート（出力ポート）
 */
interface Storage {
    /**
     * PDFファイルをS3に保存する
     *
     * @return 保存に成功した場合true
     */
    fun saveFileToS3(filename: String, data: ByteArray): Boolean

    /**
     * 画像ファイルをS3に保存する
     *
     * @param filename ファイル名
     * @param data 画像データのバイト配列
     * @return 保存に成功した場合true
     */
    fun saveImageToS3(filename: String, data: ByteArray): Boolean
}
