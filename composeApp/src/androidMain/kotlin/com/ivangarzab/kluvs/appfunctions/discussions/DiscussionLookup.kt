package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.DiscussionContext
import com.ivangarzab.kluvs.model.AttendanceStatus
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDateTime

/** A scheduled meeting where a book club discusses its current book. */
@RequiresApi(36)
@AppFunctionSerializable(isDescribedByKDoc = true)
data class DiscussionSummary(
    /** The discussion's ID. */
    val discussionId: String,
    /** The discussion's title. */
    val title: String,
    /** When the discussion meets, in the device's local time. */
    val dateTime: LocalDateTime,
    /** Where the discussion meets, such as an address or "Online"; null if not set. */
    val location: String?,
    /** The ID of the club holding the discussion. */
    val clubId: String,
    /** The name of the club holding the discussion. */
    val clubName: String,
    /** The title of the book the club is reading. */
    val bookTitle: String,
)

@RequiresApi(36)
internal fun DiscussionContext.toSummary() = DiscussionSummary(
    discussionId = discussion.id,
    title = discussion.title,
    dateTime = discussion.date.toJavaLocalDateTime(),
    location = discussion.location?.takeIf { it.isNotBlank() },
    clubId = club.id,
    clubName = club.name,
    bookTitle = session.book.title,
)

internal fun AttendanceStatus.toRsvp(): String = when (this) {
    AttendanceStatus.YES -> RSVP_GOING
    AttendanceStatus.MAYBE -> RSVP_MAYBE
    AttendanceStatus.NO -> RSVP_NOT_GOING
}

internal const val RSVP_GOING = "going"
internal const val RSVP_MAYBE = "maybe"
internal const val RSVP_NOT_GOING = "not_going"
internal const val RSVP_NONE = "none"
