package com.ivangarzab.kluvs.model

/**
 * Domain model bundling all enrichment data for a [Book]'s detail screen:
 * volume info, primary author info, and other books by that author.
 */
data class BookEnrichment(

    val volumeInfo: BookVolumeInfo?,

    val author: Author?,

    val authorBooks: List<Book> = emptyList()
)
