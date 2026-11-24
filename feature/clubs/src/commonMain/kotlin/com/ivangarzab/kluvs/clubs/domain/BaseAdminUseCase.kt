package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.model.Role


/**
 * Base class for UseCases that require role-based authorization.
 *
 * Subclasses define [requiredRoles] and implement [execute].
 * The public [invoke] operator checks authorization before delegating.
 */
abstract class BaseAdminUseCase<Params, T> {

    /** Roles that are allowed to perform this operation. */
    abstract val requiredRoles: Set<Role>

    /** The actual operation logic — only called after authorization passes. */
    protected abstract suspend fun execute(params: Params): Result<T>

    suspend operator fun invoke(params: Params, userRole: Role): Result<T> {
        if (userRole !in requiredRoles) {
            return Result.failure(
                UnauthorizedException("You don't have permission to do that.")
            )
        }
        return execute(params)
    }

    internal companion object {
        /** Roles that are allowed to perform owner-only operations. */
        internal val OWNER_ONLY = setOf(Role.OWNER)

        /** Roles that are allowed to perform admin-and-above operations. */
        internal val ADMIN_AND_ABOVE = setOf(Role.OWNER, Role.ADMIN)

        /** Roles that are allowed to perform operations reserved for non-owners (e.g. leaving a club). */
        internal val NOT_OWNER = setOf(Role.ADMIN, Role.MEMBER)
    }
}
