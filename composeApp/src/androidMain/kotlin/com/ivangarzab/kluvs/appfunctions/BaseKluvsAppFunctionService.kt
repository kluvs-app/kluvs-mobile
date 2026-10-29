package com.ivangarzab.kluvs.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.ivangarzab.kluvs.appfunctions.clubs.MyClub
import com.ivangarzab.kluvs.appfunctions.clubs.listMyClubs
import com.ivangarzab.kluvs.appfunctions.discussions.DiscussionDetails
import com.ivangarzab.kluvs.appfunctions.discussions.DiscussionNoteResult
import com.ivangarzab.kluvs.appfunctions.discussions.DiscussionSummary
import com.ivangarzab.kluvs.appfunctions.discussions.RsvpResult
import com.ivangarzab.kluvs.appfunctions.discussions.createDiscussion
import com.ivangarzab.kluvs.appfunctions.discussions.getDiscussionDetails
import com.ivangarzab.kluvs.appfunctions.discussions.listUpcomingDiscussions
import com.ivangarzab.kluvs.appfunctions.discussions.rsvpToDiscussion
import com.ivangarzab.kluvs.appfunctions.discussions.saveDiscussionNote
import com.ivangarzab.kluvs.appfunctions.progress.ReadingProgressResult
import com.ivangarzab.kluvs.appfunctions.progress.updateReadingProgress
import org.koin.android.ext.android.get
import java.time.LocalDateTime

/**
 * The app's single AppFunctions entry point — Android allows only one service per app to
 * handle AppFunctions, so every `@AppFunctionDeclaration` lives here as a thin delegate into
 * its own file under this package. Wrap each body in [appFunction].
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "KluvsAppFunctionService",
    appFunctionXmlFileName = "kluvs_app_function_service",
)
abstract class BaseKluvsAppFunctionService : AppFunctionService() {

    internal val session: AppFunctionSession by lazy { AppFunctionSession(get(), get()) }

    /**
     * Lists the book clubs the signed-in user belongs to, with each club's ID and the user's role in it.
     *
     * @return The user's clubs; empty if they haven't joined any.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listMyClubs(): List<MyClub> = appFunction("listMyClubs") { listMyClubs(session) }

    /**
     * Lists discussions scheduled from today onward for the books the user's clubs are currently reading,
     * soonest first.
     *
     * @param clubId The ID of one club to limit the list to; null lists discussions for all of the user's clubs.
     * @return The upcoming discussions; empty if none are scheduled.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listUpcomingDiscussions(clubId: String? = null): List<DiscussionSummary> =
        appFunction("listUpcomingDiscussions") { listUpcomingDiscussions(session, get(), clubId) }

    /**
     * Gets a discussion's details: when and where it meets, the club and book, who has RSVPed, and the
     * user's own RSVP and private note.
     *
     * @param discussionId The ID of the discussion.
     * @return The discussion's details.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getDiscussionDetails(discussionId: String): DiscussionDetails =
        appFunction("getDiscussionDetails") { getDiscussionDetails(session, get(), get(), get(), discussionId) }

    /**
     * Sets the user's RSVP to a discussion, replacing any earlier RSVP.
     *
     * @param discussionId The ID of the discussion.
     * @param response "going", "maybe", or "not_going"; "none" removes the user's RSVP.
     * @return The user's RSVP and the discussion's attendance counts after the change.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun rsvpToDiscussion(discussionId: String, response: String): RsvpResult =
        appFunction("rsvpToDiscussion") {
            rsvpToDiscussion(session, get(), get(), get(), get(), discussionId, response)
        }

    /**
     * Updates the user's reading progress on the book their club is currently reading.
     *
     * @param clubId The ID of the club whose current book the user is reading.
     * @param currentPage The page the user has reached. Give this or percentComplete, not both.
     * @param percentComplete How much of the book the user has read, from 0 to 100. Give this or
     * currentPage, not both.
     * @param markFinished True if the user has finished the book; then currentPage and percentComplete
     * may be omitted.
     * @return The user's progress after the update.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun updateReadingProgress(
        clubId: String,
        currentPage: Int? = null,
        percentComplete: Float? = null,
        markFinished: Boolean = false,
    ): ReadingProgressResult = appFunction("updateReadingProgress") {
        updateReadingProgress(session, get(), get(), get(), clubId, currentPage, percentComplete, markFinished)
    }

    /**
     * Schedules a new discussion for the book a club is currently reading. Only the club's owner or
     * admins can schedule discussions.
     *
     * @param clubId The ID of the club.
     * @param title A short title for the discussion, such as "Chapters 1–5".
     * @param location Where the club will meet, such as an address or "Online".
     * @param dateTime When the discussion starts, in the device's local time. Must be in the future.
     * @return The scheduled discussion.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun createDiscussion(
        clubId: String,
        title: String,
        location: String,
        dateTime: LocalDateTime,
    ): DiscussionSummary = appFunction("createDiscussion") {
        createDiscussion(session, get(), get(), clubId, title, location, dateTime)
    }

    /**
     * Saves the user's private note for a discussion. Creates the note if the user has none yet;
     * otherwise replaces the existing note's text entirely with the given content.
     *
     * @param discussionId The ID of the discussion.
     * @param content The full text of the note.
     * @return The saved note.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun saveDiscussionNote(discussionId: String, content: String): DiscussionNoteResult =
        appFunction("saveDiscussionNote") {
            saveDiscussionNote(session, get(), get(), get(), get(), discussionId, content)
        }
}
