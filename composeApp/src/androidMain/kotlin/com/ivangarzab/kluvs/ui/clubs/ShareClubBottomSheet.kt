package com.ivangarzab.kluvs.ui.clubs

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/** Web app domain used to build shareable invite links — mobile does not yet handle this URL via deep link. */
private const val WEB_APP_DOMAIN = "https://kluvs.com"

/**
 * Bottom sheet for managing a club's invite link (mirrors web's `ShareClubModal`).
 *
 * Toggling the join policy and rotating the invite token are owner-only ([canManage]);
 * an admin sees the current link read-only with just the copy/share actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareClubBottomSheet(
    joinPolicy: JoinPolicy?,
    inviteToken: String?,
    canManage: Boolean,
    isOperationInProgress: Boolean,
    onTogglePolicy: (JoinPolicy) -> Unit,
    onRotate: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val isInviteActive = joinPolicy == JoinPolicy.INVITE_LINK && inviteToken != null
    val inviteUrl = inviteToken?.let { "$WEB_APP_DOMAIN/join/$it" }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Share Club",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Allow join via link",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = joinPolicy == JoinPolicy.INVITE_LINK,
                    onCheckedChange = { checked ->
                        onTogglePolicy(if (checked) JoinPolicy.INVITE_LINK else JoinPolicy.PRIVATE)
                    },
                    enabled = canManage && !isOperationInProgress
                )
            }

            if (isOperationInProgress) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (isInviteActive && inviteUrl != null) {
                Text(
                    text = inviteUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { clipboardManager.setText(AnnotatedString(inviteUrl)) }
                    ) {
                        Text("Copy")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, inviteUrl)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share invite link"))
                        }
                    ) {
                        Text(
                            text = "Share",
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }

                if (canManage) {
                    TextButton(
                        onClick = onRotate,
                        enabled = !isOperationInProgress
                    ) {
                        Text("Rotate link")
                    }
                }
            } else if (!canManage) {
                Text(
                    text = "Invite link sharing is currently off for this club.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_ShareClubBottomSheet_Owner() = KluvsTheme {
    ShareClubBottomSheet(
        joinPolicy = JoinPolicy.INVITE_LINK,
        inviteToken = "abc123",
        canManage = true,
        isOperationInProgress = false,
        onTogglePolicy = {},
        onRotate = {},
        onDismiss = {}
    )
}

@PreviewLightDark
@Composable
fun Preview_ShareClubBottomSheet_Admin() = KluvsTheme {
    ShareClubBottomSheet(
        joinPolicy = JoinPolicy.INVITE_LINK,
        inviteToken = "abc123",
        canManage = false,
        isOperationInProgress = false,
        onTogglePolicy = {},
        onRotate = {},
        onDismiss = {}
    )
}
