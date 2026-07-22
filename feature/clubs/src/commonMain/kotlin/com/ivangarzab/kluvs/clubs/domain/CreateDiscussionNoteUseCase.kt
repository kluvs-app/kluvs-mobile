package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import com.ivangarzab.kluvs.model.DiscussionNote

/**
 * UseCase for creating the signed-in member's note on a discussion.
 *
 * Self-serve, ownership-scoped write — no role gating is needed.
 *
 * @param discussionNoteRepository Repository for discussion note data
 */
class CreateDiscussionNoteUseCase(
    private val discussionNoteRepository: DiscussionNoteRepository
) {
    data class Params(
        val discussionId: String,
        val content: String
    )

    suspend operator fun invoke(params: Params): Result<DiscussionNote> {
        Bark.d("Creating discussion note (Discussion ID: ${params.discussionId})")
        return discussionNoteRepository.createNote(params.discussionId, params.content)
            .onSuccess { Bark.i("Discussion note created (ID: ${it.id})") }
            .onFailure { Bark.e("Failed to create discussion note (Discussion ID: ${params.discussionId}). Retry.", it) }
    }
}
