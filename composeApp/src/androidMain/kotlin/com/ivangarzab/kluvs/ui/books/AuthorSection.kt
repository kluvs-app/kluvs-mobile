package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.model.Author

/**
 * "About the Author" body: photo + name/description row, then bio below full-width (mirrors
 * web's vertical stack, not a side-by-side avatar/bio layout). The description is a one-line
 * eyebrow under the name, as on web. Silently omitted entirely if [author] has no name,
 * description or bio — same graceful-degradation semantics as web's
 * `BooksPage.tsx`. The section eyebrow header and surrounding divider are owned by the
 * caller, matching the Details/More-by sections. Loading is handled by the screen-level
 * skeleton, not this section — see [BookDetailScreenContent].
 */
@Composable
fun AuthorSection(
    modifier: Modifier = Modifier,
    author: Author?
) {
    if (author == null || (author.name == null && author.description == null && author.bio == null)) return

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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val name = author.name
                if (name != null) {
                    Text(
                        text = name,
                        style = KluvsTheme.typography.title.medium,
                        color = KluvsTheme.colors.content
                    )
                }
                val description = author.description
                if (description != null) {
                    Text(
                        text = description.uppercase(),
                        style = KluvsTheme.typography.eyebrow,
                        color = KluvsTheme.colors.contentMuted
                    )
                }
            }
        }
        val bio = author.bio
        if (bio != null) {
            Text(
                text = bio,
                style = KluvsTheme.typography.body.medium,
                color = KluvsTheme.colors.content
            )
        }
    }
}
