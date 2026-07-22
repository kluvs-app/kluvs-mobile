package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import com.ivangarzab.kluvs.model.DiscussionNote

/**
 * UseCase for fetching the signed-in member's note on a discussion.
 *
 * Self-serve, ownership-scoped read — the backend resolves the caller from
 * their auth token and only ever returns their own note, so no role gating
 * is needed.
 *
 * @param discussionNoteRepository Repository for discussion note data
 */
class GetDiscussionNoteUseCase(
    private val discussionNoteRepository: DiscussionNoteRepository
) {
    suspend operator fun invoke(discussionId: String): Result<DiscussionNote?> {
        Bark.d("Fetching discussion note (Discussion ID: $discussionId)")
        return discussionNoteRepository.getNote(discussionId)
            .onSuccess { Bark.i("Loaded discussion note (Discussion ID: $discussionId, Found: ${it != null})") }
            .onFailure { Bark.e("Failed to fetch discussion note (Discussion ID: $discussionId).", it) }
    }
}
