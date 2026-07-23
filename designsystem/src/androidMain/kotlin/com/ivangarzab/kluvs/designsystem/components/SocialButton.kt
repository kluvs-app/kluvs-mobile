package com.ivangarzab.kluvs.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

@Composable
fun SocialButton(
    text: String,
    height: Dp = 40.dp,
    icon: IconType,
    iconSize: Dp = 20.dp,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(icon.toDrawableRes()),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            style = KluvsTheme.typography.label
        )
    }
}