package com.ivangarzab.kluvs.model

/**
 * Domain model representing a member in the context of a specific club.
 *
 * This composition model ensures type safety by guaranteeing that role information
 * is always available when working with club members. Use this when displaying
 * club member lists where roles need to be shown.
 *
 * @property role The member's role in the club
 * @property member The underlying member entity
 */
data class ClubMember(
    val role: Role,
    val member: Member
)