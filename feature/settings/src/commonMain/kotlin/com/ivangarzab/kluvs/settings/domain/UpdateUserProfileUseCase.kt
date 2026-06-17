package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository

/**
 * UseCase that validates and persists profile edits (name and handle) from SettingsScreen.
 *
 * Validation rules:
 * - Name must not be blank
 * - Handle must not be blank
 * - Handle must be 2–30 characters: letters, digits, or underscores only (no "@", no spaces)
 */
class UpdateUserProfileUseCase(
    private val memberRepository: MemberRepository
) {
    /**
     * Validates and saves the profile changes.
     *
     * @param memberId The member's ID
     * @param name The new display name
     * @param handle The new handle (without "@" prefix)
     * @return Result.success(Unit) on success, or Result.failure with a descriptive error
     */
    suspend operator fun invoke(memberId: String, name: String, handle: String): Result<Unit> {
        if (name.isBlank()) {
            Bark.w("Update rejected: name is blank")
            return Result.failure(IllegalArgumentException("Name must not be blank"))
        }
        if (handle.isBlank()) {
            Bark.w("Update rejected: handle is blank")
            return Result.failure(IllegalArgumentException("Handle must not be blank"))
        }
        if (!HANDLE_REGEX.matches(handle)) {
            Bark.w("Update rejected: handle has invalid format ($handle)")
            return Result.failure(
                IllegalArgumentException("Handle must be 2–30 characters: letters, numbers, or underscores only")
            )
        }

        val fullHandle = "@$handle"
        Bark.d("Updating user profile (Member ID: $memberId, Name: $name, Handle: $fullHandle)")
        return memberRepository.updateMember(
            memberId = memberId,
            name = name,
            handle = fullHandle
        ).map { }.onFailure { error ->
            Bark.e("Failed to update profile (Member ID: $memberId).", error)
        }
    }

    companion object {
        private val HANDLE_REGEX = Regex("^[a-zA-Z0-9_]{2,30}$")
    }
}
