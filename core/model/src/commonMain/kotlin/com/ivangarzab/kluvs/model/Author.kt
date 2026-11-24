package com.ivangarzab.kluvs.model

/**
 * Domain model for author enrichment data (description, bio, photo).
 */
data class Author(

    val name: String?,

    /** One-line descriptor, e.g. "American novelist". */
    val description: String? = null,

    val imageUrl: String? = null,

    val bio: String? = null
)
