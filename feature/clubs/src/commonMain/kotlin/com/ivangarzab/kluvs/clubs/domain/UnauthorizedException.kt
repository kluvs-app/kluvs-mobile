package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.model.UserFacingException

/**
 * Represents an exception indicating that a user is not authorized to perform an operation.
 *
 */
class UnauthorizedException(message: String) : UserFacingException(message)
