package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.lightBar
import com.ivangarzab.kluvs.theme.warmDarkBar
import kotlin.math.sqrt

/**
 * The design system's "no cover available" fallback: a tessellating hexagon
 * hive grid on the [warmDarkBar]/[lightBar] surface. See design-system/docs/book-cover.md.
 *
 * Pointy-top hexagon tiling where [hexWidth] is the vertex-to-vertex column spacing
 * (matches the web token's 28px tile width at book-cover--md scale).
 */
@Composable
fun BookCoverPlaceholder(
    modifier: Modifier = Modifier,
    hexWidth: Dp = 14.dp
) {
    val strokeColor = MaterialTheme.colorScheme.outline
    val backgroundColor = if (isSystemInDarkTheme()) warmDarkBar else lightBar

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val hexWidthPx = hexWidth.toPx()
        val radius = hexWidthPx / sqrt(3f)
        val hexHeight = radius * 2f
        val rowSpacing = hexHeight * 0.75f

        val path = Path()
        var row = 0
        var y = -hexHeight
        while (y < size.height + hexHeight) {
            val xOffset = if (row % 2 == 1) hexWidthPx / 2f else 0f
            var x = -hexWidthPx + xOffset
            while (x < size.width + hexWidthPx) {
                addHexagon(path, Offset(x, y), radius)
                x += hexWidthPx
            }
            y += rowSpacing
            row++
        }

        drawPath(path, color = strokeColor, style = Stroke(width = 1.5.dp.toPx()))
    }
}

/** Appends a pointy-top hexagon outline centered at [center] with circumradius [radius]. */
private fun addHexagon(path: Path, center: Offset, radius: Float) {
    for (i in 0..5) {
        val angleDeg = -90.0 + 60.0 * i
        val angleRad = Math.toRadians(angleDeg)
        val point = Offset(
            x = center.x + (radius * kotlin.math.cos(angleRad)).toFloat(),
            y = center.y + (radius * kotlin.math.sin(angleRad)).toFloat()
        )
        if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
}

@PreviewLightDark
@Composable
fun Preview_BookCoverPlaceholder() = KluvsTheme {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        BookCoverPlaceholder(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(2f / 3f)
        )
    }
}
