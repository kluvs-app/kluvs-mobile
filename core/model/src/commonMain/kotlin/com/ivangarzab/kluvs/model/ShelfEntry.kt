package com.ivangarzab.kluvs.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for a single entry on the member's book shelf.
 */
data class ShelfEntry(

    val shelf: ShelfStatus,

    val source: ShelfSource = ShelfSource.MANUAL,

    val updatedAt: LocalDateTime? = null,

    val book: Book
)

/**
 * How a book ended up on the member's shelf.
 */
enum class ShelfSource {

    /** The member shelved the book manually. */
    MANUAL,

    /** The book was shelved automatically through a reading session. */
    SESSION
}
