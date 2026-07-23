package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.MemberListItemInfo
import com.ivangarzab.kluvs.clubs.presentation.SessionParticipantInfo
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.Avatar
import com.ivangarzab.kluvs.designsystem.components.buttons.OutlinedButton
import com.ivangarzab.kluvs.designsystem.components.IconType
import com.ivangarzab.kluvs.designsystem.components.Icon
import com.ivangarzab.kluvs.designsystem.components.NoTabData
import com.ivangarzab.kluvs.ui.components.RoleEyebrow

@Composable
fun MembersTab(
    modifier: Modifier = Modifier,
    members: List<MemberListItemInfo>,
    participants: List<SessionParticipantInfo> = emptyList(),
    currentUserId: String? = null,
    userRole: Role? = null,
    onChangeRole: (memberId: String) -> Unit = {},
    onRemoveMember: (memberId: String) -> Unit = {},
    onInviteMember: () -> Unit = {},
) {
    // Session participation lookup for the reading/skipping indicator
    val readingByMemberId = participants.associate { it.memberId to it.isReading }
    if (members.isEmpty()) {
        NoTabData(
            modifier = modifier,
            text = R.string.no_members_in_club
        )
        return
    }

    val isAdminOrAbove = userRole == Role.OWNER || userRole == Role.ADMIN
    val isOwner = userRole == Role.OWNER

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.x_members, members.size),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic
                )
            )
            if (isAdminOrAbove) {
                OutlinedButton(
                    text = stringResource(R.string.invite),
                    onClick = onInviteMember
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn {
            itemsIndexed(members) { index, member ->
                //TODO: Consider creating an ext function: Member.isMe(Member or Member.id)
                val isSelf = member.userId != null && member.userId == currentUserId
                MemberListItem(
                    memberId = member.memberId,
                    name = member.name,
                    handle = member.handle,
                    avatarUrl = member.avatarUrl,
                    role = member.role,
                    isSelf = isSelf,
                    isReading = readingByMemberId[member.memberId],
                    showAdminActions = isAdminOrAbove && (!isSelf || isOwner),
                    showRemove = isOwner && !isSelf && member.role != Role.OWNER,
                    onChangeRole = { onChangeRole(member.memberId) },
                    onRemove = { onRemoveMember(member.memberId) }
                )
                if (index < members.size - 1) {
                    MemberDivider()
                }
            }
        }

        if (members.size <= 1 && isAdminOrAbove) {
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.invite_others_cta),
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onInviteMember) {
                    Text(stringResource(R.string.invite_members))
                }
            }
        }
    }
}

@Composable
private fun MemberListItem(
    modifier: Modifier = Modifier,
    memberId: String,
    name: String,
    handle: String,
    avatarUrl: String? = null,
    role: Role,
    isSelf: Boolean = false,
    isReading: Boolean? = null,
    showAdminActions: Boolean = false,
    showRemove: Boolean = false,
    onChangeRole: () -> Unit = {},
    onRemove: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Avatar(
            name = name,
            memberId = memberId,
            avatarUrl = avatarUrl,
            size = 40.dp,
            isOwn = isSelf,
            contentDescription = stringResource(R.string.avatar_of_x, name)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (isSelf) {
                    Text(
                        text = stringResource(R.string.you).uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = handle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleEyebrow(role = role)
                if (showAdminActions || showRemove) {
                    MemberOverflowMenu(
                        showChangeRole = showAdminActions,
                        showRemove = showRemove,
                        onChangeRole = onChangeRole,
                        onRemove = onRemove
                    )
                }
            }

            // Session participation indicator (mirrors the web members list)
            isReading?.let { reading ->
                Text(
                    text = if (reading) "Reading" else "Skipping",
                    color = if (reading) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun MemberOverflowMenu(
    showChangeRole: Boolean,
    showRemove: Boolean,
    onChangeRole: () -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                type = IconType.MoreVert,
                contentDescription = "Member options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (showChangeRole) {
                DropdownMenuItem(
                    text = { Text("Change Role") },
                    onClick = {
                        expanded = false
                        onChangeRole()
                    }
                )
            }
            if (showRemove) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Remove",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        expanded = false
                        onRemove()
                    }
                )
            }
        }
    }
}

@Composable
private fun MemberDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@PreviewLightDark
@Composable
fun Preview_MembersTab() = KluvsTheme {
    MembersTab(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
        members = listOf(
            MemberListItemInfo("0", "Iván Garza Bermea", "@ivangarzab", "", role = Role.OWNER, userId = "u0"),
            MemberListItemInfo("1", "Monica Michelle Morales", "@monica", "", role = Role.ADMIN, userId = "u1"),
            MemberListItemInfo("2", "Marco \"Chitho\" Rivera", "@chitho23", "", role = Role.MEMBER, userId = "u2"),
            MemberListItemInfo("3", "Anacleto \"Keto\" Longoria", "@keto92", "", role = Role.MEMBER, userId = "u3"),
            MemberListItemInfo("4", "Joel Oscar Julian Salinas", "@josalinas", "", role = Role.MEMBER, userId = "u4"),
            MemberListItemInfo("5", "Ginseng Joaquin Guzman", "gino1", "", role = Role.MEMBER, userId = "u5"),
        ),
        currentUserId = "u0",
        userRole = Role.OWNER
    )
}
