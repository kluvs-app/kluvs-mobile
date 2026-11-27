package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.app.InviteLink
import com.ivangarzab.kluvs.designsystem.components.buttons.SecondaryButton
import com.ivangarzab.kluvs.designsystem.components.controls.ToggleControl
import com.ivangarzab.kluvs.designsystem.components.loading.LoadingSpinner
import com.ivangarzab.kluvs.designsystem.components.modals.BottomSheet
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.model.JoinPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Bottom sheet for managing a club's invite link. Checked kluvs-frontend's ShareClubModal.tsx as
 * source of truth: replaces the previous plain on/off Switch with [ToggleControl] (Private/Invite
 * Link segmented pill row), matching web's exact "Who can join?" control, plus the "Copied!"
 * button-label state web has after tapping Copy (reverts after 2s).
 *
 * `onRotate`/"Rotate link" is a mobile-only addition beyond web's modal — web has no regenerate
 * action — kept as-is since it's pre-existing, already-wired functionality.
 *
 * Toggling the join policy and rotating the invite token are owner-only ([canManage]); an admin
 * sees the current link read-only with just the copy/share actions.
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
    onCopy: (inviteUrl: String) -> Unit,
    onShare: (inviteUrl: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val isInviteActive = joinPolicy == JoinPolicy.INVITE_LINK && inviteToken != null
    val inviteUrl = inviteToken?.let { InviteLink.build(it) }
    var copied by remember { mutableStateOf(false) }

    BottomSheet(
        header = "Share Club",
        onDismiss = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WHO CAN JOIN?",
                    style = KluvsTheme.typography.eyebrow,
                    color = KluvsTheme.colors.contentMuted,
                )
                ToggleControl(
                    options = listOf(JoinPolicy.PRIVATE, JoinPolicy.INVITE_LINK),
                    selected = joinPolicy ?: JoinPolicy.PRIVATE,
                    onSelect = onTogglePolicy,
                    label = { if (it == JoinPolicy.INVITE_LINK) "Invite Link" else "Private" },
                )
            }

            if (isOperationInProgress) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LoadingSpinner()
                }
            } else if (isInviteActive && inviteUrl != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KluvsTheme.colors.cardAlt, RoundedCornerShape(8.dp))
                            .border(1.dp, KluvsTheme.colors.divider, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = inviteUrl,
                            style = KluvsTheme.typography.mono,
                            color = KluvsTheme.colors.contentMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (copied) "Copied!" else "Copy",
                            style = KluvsTheme.typography.label,
                            color = if (copied) KluvsTheme.colors.success else KluvsTheme.colors.accent,
                            modifier = Modifier.clickable {
                                onCopy(inviteUrl)
                                copied = true
                                coroutineScope.launch {
                                    delay(2000)
                                    copied = false
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "Share",
                            onClick = { onShare(inviteUrl) }
                        )
                        if (canManage) {
                            SecondaryButton(text = "Rotate link", onClick = onRotate, enabled = !isOperationInProgress)
                        }
                    }
                }
            } else if (!canManage) {
                Text(
                    text = "Invite link sharing is currently off for this club.",
                    style = KluvsTheme.typography.body.medium,
                    color = KluvsTheme.colors.contentMuted,
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
        onCopy = { Bark.d("Preview: Copy tapped") },
        onShare = { Bark.d("Preview: Share tapped") },
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
        onCopy = { Bark.d("Preview: Copy tapped") },
        onShare = { Bark.d("Preview: Share tapped") },
        onDismiss = {}
    )
}
