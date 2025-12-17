package com.example.pdfbatch.domain

/**
 * UTC時間を表す列挙型
 */
enum class UTC(
    val hour: String,
) {
    UTC_00("00"),
    UTC_12("12"),
}