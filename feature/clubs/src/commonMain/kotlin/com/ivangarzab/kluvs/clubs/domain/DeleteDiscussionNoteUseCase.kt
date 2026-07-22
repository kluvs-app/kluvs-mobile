package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository

/**
 * UseCase for deleting the signed-in member's note on a discussion.
 *
 * Self-serve, ownership-scoped write — no role gating is needed.
 *
 * @param discussionNoteRepository Repository for discussion note data
 */
class DeleteDiscussionNoteUseCase(
    private val discussionNoteRepository: DiscussionNoteRepository
) {
    data class Params(
        val noteId: String
    )

    suspend operator fun invoke(params: Params): Result<Unit> {
        Bark.d("Deleting discussion note (ID: ${params.noteId})")
        return discussionNoteRepository.deleteNote(params.noteId)
            .onSuccess { Bark.i("Discussion note deleted (ID: ${params.noteId})") }
            .onFailure { Bark.e("Failed to delete discussion note (ID: ${params.noteId}). Retry.", it) }
    }
}
