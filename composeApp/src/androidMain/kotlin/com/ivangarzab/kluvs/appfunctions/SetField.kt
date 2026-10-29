package com.ivangarzab.kluvs.appfunctions

import androidx.appfunctions.AppFunctionSerializable

/**
 * A new value for one field of an existing item. Omit it to leave the field unchanged;
 * pass it holding null to clear the field.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SetField<T>(
    /** The value to set the field to. */
    val value: T,
)
