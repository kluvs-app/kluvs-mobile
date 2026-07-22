package com.ivangarzab.kluvs.model

/**
 * Domain model for author enrichment data (bio, photo).
 */
data class Author(

    val name: String?,

    val imageUrl: String? = null,

    val bio: String? = null
)
