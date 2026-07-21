package com.ivangarzab.kluvs.model

/**
 * Domain model for Google Books volume enrichment data that complements [Book].
 */
data class BookVolumeInfo(

    val subtitle: String? = null,

    val publisher: String? = null,

    val description: String? = null,

    val categories: List<String> = emptyList(),

    val language: String? = null,

    val isbn13: String? = null,

    val isbn10: String? = null
)
