package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.roleAdminOnDark
import com.ivangarzab.kluvs.theme.roleMemberLabel
import com.ivangarzab.kluvs.theme.roleOwner

/**
 * Uppercase role label with a colored dot for Owner/Admin — mirrors web's `RoleEyebrow`.
 * Supersedes the avatar-ring role indicator.
 */
@Composable
fun RoleEyebrow(
    role: Role,
    modifier: Modifier = Modifier,
) {
    val color = roleColor(role)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (role != Role.MEMBER) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
        Text(
            text = role.name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun roleColor(role: Role): Color = when (role) {
    Role.OWNER -> roleOwner
    Role.ADMIN -> roleAdminOnDark
    Role.MEMBER -> roleMemberLabel
}

@PreviewLightDark
@Composable
fun Preview_RoleEyebrow() = KluvsTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        RoleEyebrow(role = Role.OWNER)
        RoleEyebrow(role = Role.ADMIN)
        RoleEyebrow(role = Role.MEMBER)
    }
}
