package com.bangkit.alpaca.data.model

data class Story(
    val id: Int,
    val title: String,
    val body: String,
    val coverPath: String?,
    val authorName: String?,
    val createdAt: Long
)