package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.theme.KluvsTheme

private val assignableRoles = listOf(Role.ADMIN, Role.MEMBER)

/**
 * Bottom sheet for changing a member's role within a club.
 *
 * Only ADMIN and MEMBER are assignable — OWNER is reserved for the club creator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoleBottomSheet(
    memberName: String,
    currentRole: Role,
    onSave: (newRole: Role) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialSelectedRole = remember { currentRole.takeIf { it in assignableRoles } ?: Role.MEMBER }
    var selectedRole by remember { mutableStateOf(initialSelectedRole) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Change Role",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = memberName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            assignableRoles.forEach { role ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role }
                    )
                    Column {
                        Text(
                            text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (role) {
                                Role.ADMIN -> "Can create and manage sessions and discussions"
                                else -> "Regular club member"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = { onSave(selectedRole) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = selectedRole != initialSelectedRole
            ) {
                Text(
                    text = "Save",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_ChangeRoleBottomSheet() = KluvsTheme {
    ChangeRoleBottomSheet(
        memberName = "Bob Smith",
        currentRole = Role.MEMBER,
        onSave = {},
        onDismiss = {}
    )
}
