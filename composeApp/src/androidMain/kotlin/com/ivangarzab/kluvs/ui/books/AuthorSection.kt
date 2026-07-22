package com.ivangarzab.kluvs.ui.books

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.model.Author
import com.ivangarzab.kluvs.theme.ebGaramond

/**
 * "About the Author" body: photo + name row, then bio below full-width (mirrors web's
 * vertical stack, not a side-by-side avatar/bio layout). Shows a shimmer while loading,
 * and is silently omitted entirely if [author] is null once loading finishes — same
 * graceful-degradation semantics as web's `BooksPage.tsx`. The section eyebrow header
 * and surrounding divider are owned by the caller, matching the Details/More-by sections.
 */
@Composable
fun AuthorSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    author: Author?
) {
    when {
        isLoading -> AuthorSectionShimmer(modifier = modifier)
        author != null && (author.name != null || author.bio != null) -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (author.imageUrl != null) {
                        SubcomposeAsyncImage(
                            model = author.imageUrl,
                            contentDescription = author.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    val name = author.name
                    if (name != null) {
                        Text(
                            text = name,
                            fontFamily = ebGaramond,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                val bio = author.bio
                if (bio != null) {
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        // No author data available — omit the section entirely, no error/empty text.
    }
}

@Composable
private fun AuthorSectionShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "AuthorShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuthorShimmerAlpha"
    )
    val shimmerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(shimmerColor)
            ) {}
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerColor)
            ) {}
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        ) {}
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        ) {}
    }
}
