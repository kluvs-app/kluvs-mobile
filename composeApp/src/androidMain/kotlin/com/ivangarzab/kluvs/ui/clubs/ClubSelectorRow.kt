package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.theme.KluvsTheme

@Composable
fun ClubSelectorRow(
    clubName: String,
    hasMultipleClubs: Boolean,
    onSelectorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hasMultipleClubs) Modifier.clickable(onClick = onSelectorClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (hasMultipleClubs) {
            Icon(
                painter = painterResource(R.drawable.ic_unfold),
                contentDescription = "Switch club",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        AnimatedContent(
            targetState = clubName,
            transitionSpec = {
                slideInVertically { -it } togetherWith slideOutVertically { it }
            },
            label = "club_name_animation",
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        /* TODO: Impl once we have club/ create feature w/uiux
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add club",
            tint = MaterialTheme.colorScheme.onSurface
        )*/
    }
}

@PreviewLightDark
@Composable
fun Preview_ClubSelectorRow_MultipleClubs() = KluvsTheme {
    ClubSelectorRow(
        clubName = "My Book Club",
        hasMultipleClubs = true,
        onSelectorClick = {}
    )
}

@PreviewLightDark
@Composable
fun Preview_ClubSelectorRow_SingleClub() = KluvsTheme {
    ClubSelectorRow(
        clubName = "My Book Club",
        hasMultipleClubs = false,
        onSelectorClick = {}
    )
}
