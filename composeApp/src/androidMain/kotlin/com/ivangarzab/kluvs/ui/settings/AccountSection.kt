package com.ivangarzab.kluvs.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

@Composable
fun AccountSection(
    modifier: Modifier = Modifier,
    onChangePasswordClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = stringResource(R.string.account_title).uppercase(),
            style = KluvsTheme.typography.eyebrow,
            color = KluvsTheme.colors.contentMuted
        )

        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        AccountRow(
            label = stringResource(R.string.change_password),
            onClick = onChangePasswordClick
        )

        HorizontalDivider(color = KluvsTheme.colors.divider)

        AccountRow(
            label = stringResource(R.string.delete_account),
            onClick = onDeleteAccountClick,
            textColor = KluvsTheme.colors.danger
        )

        HorizontalDivider(color = KluvsTheme.colors.divider)
    }
}

@Composable
private fun AccountRow(
    label: String,
    onClick: () -> Unit,
    textColor: Color = KluvsTheme.colors.accent,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = KluvsTheme.typography.body.large,
            color = textColor,
        )
    }
}

@PreviewLightDark
@Composable
fun Preview_AccountSection() = KluvsTheme {
    AccountSection()
}
