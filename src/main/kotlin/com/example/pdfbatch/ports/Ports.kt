package com.example.pdfbatch.ports

import com.example.pdfbatch.domain.Metadata
import com.example.pdfbatch.domain.MetadataCollection

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

/**
 * ストレージポート（出力ポート）
 */
interface Storage {
    /**
     * PDFファイルを保存
     * @return 保存に成功した場合true
     */
    fun save(filename: String, data: ByteArray): Boolean

    /**
     * ファイルが既に存在するかチェック
     */
    fun exists(filename: String): Boolean
}

/**
 * メタデータリポジトリポート（出力ポート）
 */
interface MetadataRepository {
    /**
     * すべてのメタデータを取得
     */
    fun findAll(): MetadataCollection

    /**
     * 特定URLのメタデータを取得
     */
    fun findByUrl(url: String): Metadata?

    /**
     * メタデータを保存
     */
    fun save(metadata: Metadata)

    /**
     * すべてのメタデータを保存
     */
    fun saveAll(collection: MetadataCollection)
}

