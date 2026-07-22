package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import com.ivangarzab.kluvs.model.DiscussionNote

/**
 * UseCase for updating the signed-in member's note on a discussion.
 *
 * Self-serve, ownership-scoped write — no role gating is needed.
 *
 * @param discussionNoteRepository Repository for discussion note data
 */
class UpdateDiscussionNoteUseCase(
    private val discussionNoteRepository: DiscussionNoteRepository
) {
    data class Params(
        val noteId: String,
        val content: String
    )

    suspend operator fun invoke(params: Params): Result<DiscussionNote> {
        Bark.d("Updating discussion note (ID: ${params.noteId})")
        return discussionNoteRepository.updateNote(params.noteId, params.content)
            .onSuccess { Bark.i("Discussion note updated (ID: ${it.id})") }
            .onFailure { Bark.e("Failed to update discussion note (ID: ${params.noteId}). Retry.", it) }
    }
}
