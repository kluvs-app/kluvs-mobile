package com.ivangarzab.kluvs.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.modals.BottomSheet
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * "Check your inbox" prompt shown after email sign up succeeds but requires email confirmation
 * before a session can be created. Unlike [ForgotPasswordScreen], there is no form phase — by the
 * time this shows, sign up has already completed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSignUpConfirmationSheet(
    email: String,
    onDismiss: () -> Unit,
) {
    BottomSheet(
        header = stringResource(R.string.check_your_inbox),
        onDismiss = onDismiss,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = KluvsTheme.colors.cardAlt
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.signup_confirmation_sent_to),
                    color = KluvsTheme.colors.contentMuted,
                    style = KluvsTheme.typography.label,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    color = KluvsTheme.colors.content,
                    fontWeight = FontWeight.Medium,
                    style = KluvsTheme.typography.body.large,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.signup_confirmation_body),
                    color = KluvsTheme.colors.contentMuted,
                    style = KluvsTheme.typography.body.medium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview_AuthSignUpConfirmationSheet() = KluvsTheme {
    AuthSignUpConfirmationSheet(
        email = "test@example.com",
        onDismiss = {},
    )
}
