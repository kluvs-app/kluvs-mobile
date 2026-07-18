package com.ivangarzab.kluvs.model

/**
 * Lightweight book information returned nested inside other entities
 * (reading progress, reading log) where the full [Book] is not available.
 */
data class BookSummary(

    val id: String,

    val title: String,

    val author: String? = null,

    val pageCount: Int? = null,

    val imageUrl: String? = null
)
