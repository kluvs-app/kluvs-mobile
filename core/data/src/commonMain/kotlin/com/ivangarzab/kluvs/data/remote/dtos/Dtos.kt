package com.ivangarzab.kluvs.data.remote.dtos

import com.ivangarzab.kluvs.network.serializers.IntListToStringListSerializer
import com.ivangarzab.kluvs.network.serializers.IntToStringSerializer
import com.ivangarzab.kluvs.network.serializers.NullableIntListToStringListSerializer
import com.ivangarzab.kluvs.network.serializers.NullableIntToStringSerializer
import kotlinx.serialization.Serializable

// ========================================
// CORE REUSABLE DTOs
// ========================================

@Serializable
data class BookDto(
    @Serializable(with = IntToStringSerializer::class)
    val id: String,
    val title: String,
    val author: String,
    val edition: String? = null,
    val year: Int? = null,
    val isbn: String? = null,
    val page_count: Int? = null,
    val image_url: String? = null,
    val external_google_id: String? = null
)

@Serializable
data class DiscussionDto(
    val id: String,
    val session_id: String? = null,
    val title: String,
    val date: String,
    val location: String? = null
)

@Serializable
data class MemberDto(
    @Serializable(with = IntToStringSerializer::class)
    val id: String,
    val name: String? = null,  // Nullable to handle incomplete API responses
    val handle: String? = null,
    val avatar_path: String? = null,
    val books_read: Int = 0,
    val user_id: String? = null,
    val role: String? = null,
    val created_at: String? = null,
    val clubs: List<String> = emptyList()
)

@Serializable
data class ClubMemberDto(
    @Serializable(with = IntToStringSerializer::class)
    val id: String,
    val name: String? = null,
    val handle: String? = null,
    val avatar_path: String? = null,
    val books_read: Int = 0,
    val user_id: String? = null,
    val role: String,  // Required: member's role in this specific club
    val created_at: String? = null,
    val clubs: List<String> = emptyList()
)

@Serializable
data class ClubDto(
    val id: String,
    val name: String,
    val discord_channel: String? = null,
    val server_id: String? = null,
    val founded_date: String? = null,
    val role: String? = null  // Optional: populated when club is in member's clubs list
)

@Serializable
data class SessionDto(
    val id: String,
    val club_id: String? = null,
    val book: BookDto? = null,
    val due_date: String? = null,
    val discussions: List<DiscussionDto> = emptyList()
)

@Serializable
data class ServerDto(
    val id: String,
    val name: String
)

// ========================================
// COMMON RESPONSE PATTERNS
// ========================================

@Suppress("unused")
@Serializable
data class ErrorResponseDto(
    val error: String,
    val partial_success: Boolean? = null,
    val message: String? = null
)

@Serializable
data class DeleteResponseDto(
    val success: Boolean,
    val message: String,
    val warning: String? = null
)

// ========================================
// CLUB SPECIFIC RESPONSES
// ========================================

@Serializable
data class ClubResponseDto(
    val id: String,
    val name: String,
    val discord_channel: String?,
    val server_id: String?,
    val founded_date: String? = null,
    val members: List<ClubMemberDto>,
    val active_session: SessionDto?,
    val past_sessions: List<SessionDto>,
    @Serializable(with = IntListToStringListSerializer::class)
    val shame_list: List<String> // Member IDs
)

@Serializable
data class CreateClubRequestDto(
    val id: String? = null,
    val name: String,
    val discord_channel: String? = null,
    val server_id: String? = null,
    val members: List<MemberDto>? = null,
    val active_session: SessionDto? = null,
    @Serializable(with = NullableIntListToStringListSerializer::class)
    val shame_list: List<String>? = null
)

@Serializable
data class UpdateClubRequestDto(
    val id: String,
    val server_id: String?,
    val name: String? = null,
    val discord_channel: String? = null,
    @Serializable(with = NullableIntListToStringListSerializer::class)
    val shame_list: List<String>? = null
)

@Serializable
data class ClubSuccessResponseDto(
    val success: Boolean,
    val message: String,
    val club: ClubDto,
    val club_updated: Boolean? = null,
    val shame_list_updated: Boolean? = null
)

// ========================================
// MEMBER SPECIFIC RESPONSES
// ========================================

@Serializable
data class MemberResponseDto(
    @Serializable(with = IntToStringSerializer::class)
    val id: String,
    val name: String,
    val handle: String? = null,
    val avatar_path: String? = null,
    val books_read: Int,
    val user_id: String?,
    val created_at: String? = null,
    val clubs: List<ClubDto>,
    val shame_clubs: List<ClubDto>
)

@Serializable
data class CreateMemberRequestDto(
    @Serializable(with = NullableIntToStringSerializer::class)
    val id: String? = null,
    val name: String,
    val books_read: Int = 0,
    val user_id: String? = null,
    val role: String? = null,
    val clubs: List<String>? = null // Club IDs
)

@Serializable
data class UpdateMemberRequestDto(
    @Serializable(with = IntToStringSerializer::class)
    val id: String,
    val name: String? = null,
    val handle: String? = null,
    val avatar_path: String? = null,
    val books_read: Int? = null,
    val user_id: String? = null,
    val role: String? = null,
    val clubs: List<String>? = null,
    val club_roles: Map<String, String>? = null
)

@Serializable
data class MemberSuccessResponseDto(
    val success: Boolean,
    val message: String,
    val member: MemberDto,
    val clubs_updated: Boolean? = null
)

// ========================================
// SESSION SPECIFIC RESPONSES
// ========================================

@Serializable
data class SessionResponseDto(
    val id: String,
    val club: ClubDto,
    val book: BookDto,
    val due_date: String?,
    val discussions: List<DiscussionDto>,
    val shame_list: List<MemberDto> // Full member objects for shame list
)

@Serializable
data class CreateSessionRequestDto(
    val id: String? = null,
    val club_id: String,
    val book_id: String? = null,
    val book: BookDto? = null,
    val due_date: String? = null,
    val discussions: List<DiscussionDto>? = null
)

@Serializable
data class UpdateSessionRequestDto(
    val id: String,
    val club_id: String? = null,
    val book: BookDto? = null,
    val due_date: String? = null,
    val discussions: List<DiscussionDto>? = null,
    val discussion_ids_to_delete: List<String>? = null
)

@Serializable
data class SessionSuccessResponseDto(
    val success: Boolean? = null,  // Nullable to handle "No changes to apply" response
    val message: String,
    val session: SessionDto? = null,
    val updates: SessionUpdatesDto? = null
)

@Serializable
data class SessionUpdatesDto(
    val book: Boolean,
    val session: Boolean,
    val discussions: Boolean
)

// ========================================
// SERVER SPECIFIC RESPONSES
// ========================================

@Serializable
data class ServerResponseDto(
    val id: String,
    val name: String,
    val clubs: List<ServerClubDto>
)

@Serializable
data class ServersResponseDto(
    val servers: List<ServerResponseDto>
)

@Serializable
data class ServerClubDto(
    val id: String,
    val name: String,

    val discord_channel: String?,
    val founded_date: String? = null,
    val member_count: Int? = null,
    val latest_session: SessionDto? = null
)

@Serializable
data class CreateServerRequestDto(
    val id: String? = null,
    val name: String
)

@Serializable
data class UpdateServerRequestDto(
    val id: String,
    val name: String? = null
)

@Serializable
data class ServerSuccessResponseDto(
    val success: Boolean,
    val message: String,
    val server: ServerDto
)

// ========================================
// BOOK SPECIFIC REQUESTS / RESPONSES
// ========================================

@Serializable
data class CreateBookRequestDto(
    val title: String,
    val author: String,
    val year: Int? = null,
    val isbn: String? = null,
    val page_count: Int? = null,
    val image_url: String? = null,
    val external_google_id: String? = null
)

@Serializable
data class BookRegistrationResponseDto(
    val success: Boolean,
    val book: BookDto,
    val created: Boolean,
    val message: String? = null
)

@Serializable
data class BookSearchResponseDto(
    val success: Boolean,
    val books: List<BookDto>,
    val total: Int? = null
)

@Serializable
data class BookLookupResponseDto(
    val success: Boolean,
    val book: BookDto
)
