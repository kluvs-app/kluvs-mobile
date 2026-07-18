package com.ivangarzab.kluvs.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for the member's personal note on a discussion.
 */
data class DiscussionNote(

    val id: String,

    /** Discussion ID that this note belongs to. **/
    val discussionId: String,

    val memberId: String,

    val content: String,

    val visibility: NoteVisibility = NoteVisibility.PRIVATE,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null
)

/**
 * Who can see a discussion note.
 */
enum class NoteVisibility {
    PRIVATE,
    PUBLIC
}
