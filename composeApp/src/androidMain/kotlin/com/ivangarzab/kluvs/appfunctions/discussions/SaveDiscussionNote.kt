package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.GetDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionNoteUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository

/** The user's private note for a discussion, as saved. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class DiscussionNoteResult(
    /** The discussion's ID. */
    val discussionId: String,
    /** The discussion's title. */
    val discussionTitle: String,
    /** The full saved text of the note. */
    val content: String,
)

internal suspend fun saveDiscussionNote(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    getDiscussionNote: GetDiscussionNoteUseCase,
    createDiscussionNote: CreateDiscussionNoteUseCase,
    updateDiscussionNote: UpdateDiscussionNoteUseCase,
    discussionId: String,
    content: String,
): DiscussionNoteResult {
    if (content.isBlank()) throw AppFunctionInvalidArgumentException("The note can't be blank.")
    val context = findDiscussion(session, clubRepository, discussionId)
    val existing = getDiscussionNote(discussionId).getOrThrowAppFunction()
    val saved = if (existing == null) {
        createDiscussionNote(CreateDiscussionNoteUseCase.Params(discussionId, content))
    } else {
        updateDiscussionNote(UpdateDiscussionNoteUseCase.Params(existing.id, content))
    }.getOrThrowAppFunction()
    return DiscussionNoteResult(
        discussionId = discussionId,
        discussionTitle = context.discussion.title,
        content = saved.content,
    )
}
