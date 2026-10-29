package com.ivangarzab.kluvs.appfunctions.clubs

import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession

/** A book club the signed-in user belongs to. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class MyClub(
    /** The club's ID. */
    val clubId: String,
    /** The club's name. */
    val name: String,
    /** The user's role in the club: "owner", "admin", or "member". */
    val role: String?,
)

internal suspend fun listMyClubs(session: AppFunctionSession): List<MyClub> =
    session.requireMember(forceRefresh = true).clubs.orEmpty().map { club ->
        MyClub(clubId = club.id, name = club.name, role = club.role?.name?.lowercase())
    }
