package com.ivangarzab.kluvs.designsystem.components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.ivangarzab.kluvs.designsystem.R

/**
 * Single entry point for every icon in the app. Callers pick an [IconType] instead of
 * reaching for `R.drawable.ic_*` or `Icons.*` directly, so custom vs. Material-backed
 * icons are an implementation detail here, not something every call site has to know.
 *
 * Named to shadow `androidx.compose.material3.Icon` deliberately — callers only ever
 * need this one. The wrapped M3 composable is called fully-qualified below to avoid
 * this declaration shadowing itself.
 */
@Composable
fun Icon(
    type: IconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val vector = type.toImageVectorOrNull()
    if (vector != null) {
        androidx.compose.material3.Icon(imageVector = vector, contentDescription = contentDescription, modifier = modifier, tint = tint)
    } else {
        androidx.compose.material3.Icon(
            painter = painterResource(type.toDrawableRes()),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}

private fun IconType.toImageVectorOrNull(): ImageVector? = when (this) {
    IconType.ArrowBack -> Icons.AutoMirrored.Filled.ArrowBack
    IconType.Add -> Icons.Filled.Add
    IconType.Search -> Icons.Filled.Search
    IconType.MoreVert -> Icons.Filled.MoreVert
    IconType.ChevronRight -> Icons.AutoMirrored.Filled.KeyboardArrowRight
    IconType.ChevronDown -> Icons.Filled.KeyboardArrowDown
    IconType.Check -> Icons.Filled.Check
    IconType.Favorite -> Icons.Filled.Favorite
    IconType.FavoriteOutline -> Icons.Outlined.FavoriteBorder
    else -> null
}

internal fun IconType.toDrawableRes(): Int = when (this) {
    IconType.Back -> R.drawable.ic_back
    IconType.Book -> R.drawable.ic_book
    IconType.Checkmark -> R.drawable.ic_checkmark
    IconType.Club -> R.drawable.ic_club
    IconType.Clubs -> R.drawable.ic_clubs
    IconType.Discord -> R.drawable.ic_discord
    IconType.Edit -> R.drawable.ic_edit
    IconType.Email -> R.drawable.ic_email
    IconType.Google -> R.drawable.ic_google
    IconType.Help -> R.drawable.ic_help
    IconType.Location -> R.drawable.ic_location
    IconType.Password -> R.drawable.ic_password
    IconType.Settings -> R.drawable.ic_settings
    IconType.SignOut -> R.drawable.ic_signout
    IconType.User -> R.drawable.ic_user
    else -> error("$this is Material-backed; has no drawable resource")
}
