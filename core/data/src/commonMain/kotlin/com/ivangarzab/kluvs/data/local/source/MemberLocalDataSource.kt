package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.database.entities.ClubMemberCrossRef
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role

/**
 * Local data source for Member entities.
 * Handles CRUD operations with the local Room database.
 */
interface MemberLocalDataSource {
    suspend fun getMember(memberId: String): Member?
    suspend fun getMemberByUserId(userId: String): Member?
    suspend fun getMembersForClub(clubId: String): List<Member>
    suspend fun insertMember(member: Member)
    suspend fun insertMembers(members: List<Member>)
    suspend fun insertClubMemberRelationship(clubId: String, memberId: String, role: String = "member")
    suspend fun deleteClubMemberRelationship(clubId: String, memberId: String)
    suspend fun deleteMember(memberId: String)
    suspend fun getLastFetchedAt(memberId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [MemberLocalDataSource] using Room database.
 */
class MemberLocalDataSourceImpl(
    private val database: KluvsDatabase
) : MemberLocalDataSource {

    private val memberDao = database.memberDao()
    private val clubDao = database.clubDao()

    override suspend fun getMember(memberId: String): Member? {
        val memberEntity = memberDao.getMember(memberId) ?: return null
        val clubEntities = memberDao.getClubsForMember(memberId)
        val clubs = if (clubEntities.isNotEmpty()) {
            val crossRefs = memberDao.getClubMemberCrossRefsForMember(memberId)
            val roleMap = crossRefs.associate { it.clubId to it.role }
            clubEntities.map { clubEntity ->
                clubEntity.toDomain().copy(role = roleMap[clubEntity.id]?.let { Role.fromString(it) })
            }
        } else null
        return memberEntity.toDomain().copy(clubs = clubs)
    }

    override suspend fun getMemberByUserId(userId: String): Member? {
        val memberEntity = memberDao.getMemberByUserId(userId) ?: return null
        val clubEntities = memberDao.getClubsForMember(memberEntity.id)
        val clubs = if (clubEntities.isNotEmpty()) {
            val crossRefs = memberDao.getClubMemberCrossRefsForMember(memberEntity.id)
            val roleMap = crossRefs.associate { it.clubId to it.role }
            clubEntities.map { clubEntity ->
                clubEntity.toDomain().copy(role = roleMap[clubEntity.id]?.let { Role.fromString(it) })
            }
        } else null
        return memberEntity.toDomain().copy(clubs = clubs)
    }

    override suspend fun getMembersForClub(clubId: String): List<Member> {
        return memberDao.getMembersForClub(clubId).map { it.toDomain() }
    }

    override suspend fun insertMember(member: Member) {
        Bark.v("Inserting member (ID: ${member.id}) into database")
        // Insert the member entity
        try {
            memberDao.insertMember(member.toEntity())

            // Insert club-member relationships, but DON'T cache club entities here.
            // Club entities should only be cached by ClubRepository with complete data.
            // We'll insert relationships only if the club already exists (ignore foreign key errors).
            member.clubs?.let { clubs ->
                Bark.v("Processing ${clubs.size} club relationships for member (ID: ${member.id})")
                var successCount = 0
                clubs.forEach { club ->
                    try {
                        memberDao.insertClubMemberCrossRef(
                            ClubMemberCrossRef(
                                clubId = club.id,
                                memberId = member.id,
                                role = club.role?.name?.lowercase() ?: "member" // Convert Role enum to string or default to "member"
                            )
                        )
                        successCount++
                    } catch (e: Exception) {
                        // Ignore foreign key violations - club will be cached later by ClubRepository
                        Bark.v("Skipping relationship for club (ID: ${club.id}) - not cached yet")
                    }
                }
                Bark.v("Inserted $successCount/${clubs.size} relationships for member (ID: ${member.id})")
            }
        } catch (e: Exception) {
            Bark.e("Failed to insert member (ID: ${member.id}) into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun insertMembers(members: List<Member>) {
        Bark.d("Inserting ${members.size} members into database")
        try {
            memberDao.insertMembers(members.map { it.toEntity() })
            Bark.d("Successfully inserted ${members.size} members into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert ${members.size} members into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun insertClubMemberRelationship(clubId: String, memberId: String, role: String) {
        Bark.d("Adding member (ID: $memberId) to club (ID: $clubId) with role: $role")
        try {
            memberDao.insertClubMemberCrossRef(
                ClubMemberCrossRef(clubId = clubId, memberId = memberId, role = role)
            )
            Bark.d("Successfully added member (ID: $memberId) to club (ID: $clubId)")
        } catch (e: Exception) {
            Bark.e("Failed to add member (ID: $memberId) to club (ID: $clubId). Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun deleteClubMemberRelationship(clubId: String, memberId: String) {
        Bark.d("Removing member (ID: $memberId) from club (ID: $clubId)")
        try {
            memberDao.deleteClubMemberCrossRef(clubId, memberId)
            Bark.d("Successfully removed member (ID: $memberId) from club (ID: $clubId)")
        } catch (e: Exception) {
            Bark.e("Failed to remove member (ID: $memberId) from club (ID: $clubId). Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun deleteMember(memberId: String) {
        val entity = memberDao.getMember(memberId)
        if (entity != null) {
            Bark.d("Deleting member (ID: $memberId) from database")
            try {
                memberDao.deleteMember(entity)
                Bark.d("Successfully deleted member (ID: $memberId) from database")
            } catch (e: Exception) {
                Bark.e("Failed to delete member (ID: $memberId) from database. Retry on next sync.", e)
                throw e
            }
        }
    }

    override suspend fun getLastFetchedAt(memberId: String): Long? {
        return memberDao.getLastFetchedAt(memberId)
    }

    override suspend fun deleteAll() {
        Bark.d("Clearing all members from database")
        try {
            memberDao.deleteAll()
            memberDao.deleteAllCrossRefs()
            Bark.d("Successfully cleared all members from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all members from database. Retry on next sync.", e)
            throw e
        }
    }
}
