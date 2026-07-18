package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.bark.Bark

/**
 * Sign out the current user and clear local cached data.
 */
class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val database: KluvsDatabase
) {
    suspend operator fun invoke(): Result<Unit> {
        // Sign out from auth first
        val result = authRepository.signOut()

        // Clear local database on successful sign-out
        if (result.isSuccess) {
            clearLocalData()
        }
        return result
    }

    private suspend fun clearLocalData() {
        Bark.v("Clearing local database on sign out")
        database.clubDao().deleteAll()
        database.serverDao().deleteAll()
        database.memberDao().deleteAll()
        database.memberDao().deleteAllCrossRefs()
        database.sessionDao().deleteAll()
        database.bookDao().deleteAll()
        database.discussionDao().deleteAll()
        database.shelfDao().deleteAll()
        database.likeDao().deleteAll()
        database.progressDao().deleteAll()
        database.discussionNoteDao().deleteAll()
        database.discussionAttendanceDao().deleteAll()
        Bark.d("Local database cleared")
    }
}