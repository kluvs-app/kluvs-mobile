package com.ivangarzab.kluvs.clubs.domain

/**
 * Represents an exception indicating that a user is not authorized to perform an operation.
 *
 */
class UnauthorizedException(message: String) : Exception(message)
