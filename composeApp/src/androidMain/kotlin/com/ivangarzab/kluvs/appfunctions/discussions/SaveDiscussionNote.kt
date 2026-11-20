package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.appfunctions.resolveDiscussion
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.GetDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionNoteUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository

/** The user's private note for a discussion, as saved. */
@RequiresApi(36)
@AppFunctionSerializable(isDescribedByKDoc = true)
data class DiscussionNoteResult(
    /** The discussion the note belongs to. */
    val discussion: DiscussionSummary,
    /** The full saved text of the note. */
    val content: String,
)

@RequiresApi(36)
internal suspend fun saveDiscussionNote(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    getDiscussionNote: GetDiscussionNoteUseCase,
    createDiscussionNote: CreateDiscussionNoteUseCase,
    updateDiscussionNote: UpdateDiscussionNoteUseCase,
    content: String,
    discussion: String?,
    club: String?,
): DiscussionNoteResult {
    if (content.isBlank()) throw AppFunctionInvalidArgumentException("The note can't be blank.")
    val context = resolveDiscussion(session, clubRepository, discussion, club)
    val discussionId = context.discussion.id
    val existing = getDiscussionNote(discussionId).getOrThrowAppFunction()
    val saved = if (existing == null) {
        createDiscussionNote(CreateDiscussionNoteUseCase.Params(discussionId, content))
    } else {
        updateDiscussionNote(UpdateDiscussionNoteUseCase.Params(existing.id, content))
    }.getOrThrowAppFunction()
    return DiscussionNoteResult(discussion = context.toSummary(), content = saved.content)
}
