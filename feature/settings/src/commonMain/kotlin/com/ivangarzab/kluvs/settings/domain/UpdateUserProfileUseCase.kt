package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository

/**
 * UseCase that validates and persists profile edits (name and handle) from SettingsScreen.
 *
 * Validation rules:
 * - Name must not be blank
 * - Handle must not be blank
 * - Handle must be 2–30 characters: lowercase letters, digits, and single hyphens between
 *   segments only (no leading/trailing hyphen, no "@", no spaces) — matches the backend's
 *   handle format exactly, so a handle the backend would reject is also rejected here before
 *   any network call.
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
        if (handle.length !in 2..30 || !HANDLE_REGEX.matches(handle)) {
            Bark.w("Update rejected: handle has invalid format ($handle)")
            return Result.failure(
                IllegalArgumentException(
                    "Handle must be 2–30 characters: lowercase letters, numbers, and hyphens only"
                )
            )
        }

        // Stored bare, without "@" — matches the backend's convention (see seed.sql) and every
        // display site, which prepends its own "@". Saving it pre-prefixed here used to double
        // up as "@@handle" wherever it was shown.
        Bark.d("Updating user profile (Member ID: $memberId, Name: $name, Handle: $handle)")
        return memberRepository.updateMember(
            memberId = memberId,
            name = name,
            handle = handle
        ).map { }.onFailure { error ->
            Bark.e("Failed to update profile (Member ID: $memberId).", error)
        }
    }

    companion object {
        private val HANDLE_REGEX = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")
    }
}
