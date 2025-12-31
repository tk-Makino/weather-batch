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
     * ファイルが既に存在するかチェック
     */
    fun exists(filename: String): Boolean

    /**
     * basePathからの相対パスでディレクトリを作成する
     * @return 作成に成功または既に存在する場合true
     */
    fun createDirectory(directory: String): Boolean

    /**
     * ディレクトリが存在するかチェック
     *
     * @param directory　ディレクトリの相対パス
     * @return　存在する場合true
     */
    fun existDirectory(directory: String): Boolean
}
