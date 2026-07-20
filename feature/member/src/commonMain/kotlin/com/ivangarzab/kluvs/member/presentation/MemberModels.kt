package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.presentation.progress.OwnProgressInfo

/**
 * UI model for current user's profile displayed in MeScreen header.
 */
data class UserProfile(
    val memberId: String,
    val name: String,
    val handle: String?,
    val joinDate: String,
    val avatarUrl: String?
)

/**
 * UI model for user statistics displayed in MeScreen StatisticsSection.
 *
 * Aggregates user metrics across all clubs.
 */
data class UserStatistics(
    val clubsCount: Int,
    val booksRead: Int
)

/**
 * UI model for a row in MeScreen's "On Your Shelf" section: the active-session
 * book for one of the member's clubs, with their own reading progress.
 *
 * Mirrors web's ProfilePage `ShelfRow`.
 */
data class ShelfItem(
    val sessionId: String,
    val bookId: String,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCoverUrl: String?,
    val bookPageCount: Int?,
    val clubId: String,
    val clubName: String,
    val nextDiscussionDate: String?,
    val ownProgress: OwnProgressInfo?
)
