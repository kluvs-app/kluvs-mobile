package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

@Composable
fun TextDivider(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Gray,
    thickness: Dp = 1.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = color,
            thickness = thickness
        )

        Text(
            text = text,
            color = color,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = color,
            thickness = thickness
        )
    }
}

@PreviewLightDark
@Composable
fun Preview_TextDivider() = KluvsTheme {
    TextDivider(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp),
        text = "test divider"
    )
}