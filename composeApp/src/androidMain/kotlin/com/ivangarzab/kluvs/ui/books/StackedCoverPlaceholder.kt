package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.lightDivider
import com.ivangarzab.kluvs.designsystem.theme.warmDarkCard2

private data class TiltedCover(val tiltDegrees: Float, val offsetXFraction: Float)

// tilts per design-system/tokens.json component.empty-state.stacked-covers (lg): left, center, right
private val COVER_TILTS = listOf(
    TiltedCover(-7f, -0.32f),
    TiltedCover(4f, 0f),
    TiltedCover(10f, 0.32f)
)

/**
 * "Nothing here" illustration for empty book lists/shelves: three tilted, diagonally-striped
 * placeholder covers fanned out. See design-system tokens component.empty-state.stacked-covers.
 */
@Composable
fun StackedCoverPlaceholder(modifier: Modifier = Modifier) {
    val stripeColor = if (isSystemInDarkTheme()) warmDarkCard2 else lightDivider

    Canvas(modifier = modifier.size(220.dp, 200.dp)) {
        val coverSize = Size(112.dp.toPx(), 160.dp.toPx())
        val stripeWidth = 5.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)

        COVER_TILTS.forEach { cover ->
            rotate(degrees = cover.tiltDegrees, pivot = center) {
                val topLeft = Offset(
                    x = center.x - coverSize.width / 2f + cover.offsetXFraction * size.width,
                    y = center.y - coverSize.height / 2f
                )
                drawDiagonalStripedRect(topLeft, coverSize, stripeColor, stripeWidth)
            }
        }
    }
}

private fun DrawScope.drawDiagonalStripedRect(
    topLeft: Offset,
    rectSize: Size,
    stripeColor: Color,
    stripeWidth: Float
) {
    clipRect(topLeft.x, topLeft.y, topLeft.x + rectSize.width, topLeft.y + rectSize.height) {
        val diagonal = rectSize.width + rectSize.height
        var x = -diagonal
        while (x < diagonal) {
            drawLine(
                color = stripeColor,
                start = Offset(topLeft.x + x, topLeft.y - diagonal / 2f),
                end = Offset(topLeft.x + x + diagonal, topLeft.y + diagonal / 2f + rectSize.height),
                strokeWidth = stripeWidth
            )
            x += stripeWidth * 2
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_StackedCoverPlaceholder() = KluvsTheme {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        StackedCoverPlaceholder()
    }
}
