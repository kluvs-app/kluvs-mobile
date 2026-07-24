package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.brandPrimary

/** Ribbon size, matching the [book-cover sizes][design-system/docs/book-cover.md] it's overlaid on. */
enum class ReadRibbonSize(val width: Dp, val height: Dp) {
    SM(12.dp, 21.dp),
    MD(16.dp, 28.dp),
    LG(24.dp, 42.dp)
}

/**
 * Corner "bookmark" marking a book as shelved through a Kluvs reading session
 * (as opposed to shelved manually). Mirrors the web `.kluvs-read-ribbon` notched-banner
 * shape (design-system/docs/book-cover.md, component.read-ribbon).
 */
@Composable
fun ReadRibbon(
    modifier: Modifier = Modifier,
    size: ReadRibbonSize = ReadRibbonSize.LG,
    contentDescription: String
) {
    val fillColor = brandPrimary
    Canvas(
        modifier = modifier
            .size(size.width, size.height)
            .semantics { this.contentDescription = contentDescription }
    ) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.18f, 0f)
            lineTo(w * 0.82f, 0f)
            lineTo(w * 0.82f, h * 0.50f)
            lineTo(w, h * 0.75f)
            lineTo(w * 0.82f, h)
            lineTo(w * 0.18f, h)
            lineTo(0f, h * 0.75f)
            lineTo(w * 0.18f, h * 0.50f)
            close()
        }
        drawPath(path, color = fillColor)
    }
}

@PreviewLightDark
@Composable
fun Preview_ReadRibbon() = KluvsTheme {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        ReadRibbon(contentDescription = "Read with Kluvs")
    }
}
