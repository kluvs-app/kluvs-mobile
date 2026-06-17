package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.settings.presentation.EditableProfile

/**
 * UseCase that loads the current member's profile for editing in SettingsScreen.
 *
 * Maps the domain [com.ivangarzab.kluvs.model.Member] to an [EditableProfile], deriving
 * a handle from the member's name if none is stored.
 */
class GetEditableProfileUseCase(
    private val memberRepository: MemberRepository
) {
    /**
     * Fetches the editable profile for the given user.
     *
     * @param userId The Discord user ID of the current user
     * @return Result containing [EditableProfile] if successful, or an error
     */
    suspend operator fun invoke(userId: String): Result<EditableProfile> {
        Bark.d("Fetching editable profile (User ID: $userId)")
        return memberRepository.getMemberByUserId(userId).map { member ->
            val handle = (member.handle ?: generateHandleFromName(member.name)).removePrefix("@")
            EditableProfile(
                memberId = member.id,
                name = member.name,
                handle = handle
            ).also {
                Bark.i("Loaded editable profile (Name: ${member.name}, Handle: $handle)")
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch editable profile (User ID: $userId).", error)
        }
    }

    /**
     * Derives a handle from a member's display name.
     * Converts "John Doe" → "johndoe" (lowercase, spaces stripped).
     */
    private fun generateHandleFromName(name: String): String =
        name.lowercase().replace(" ", "")
}
