package com.ivangarzab.kluvs.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.avatars.Avatar
import com.ivangarzab.kluvs.designsystem.components.buttons.PrimaryButton
import com.ivangarzab.kluvs.designsystem.components.fields.InputField
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

@Composable
fun EditProfileSection(
    modifier: Modifier = Modifier,
    avatarUrl: String?,
    name: String,
    isUploadingAvatar: Boolean = false,
    onAvatarClick: () -> Unit = {},
    editedName: String,
    editedHandle: String,
    handleError: String? = null,
    hasChanges: Boolean,
    isSaving: Boolean,
    saveError: String?,
    onNameChanged: (String) -> Unit,
    onHandleChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = stringResource(R.string.edit_profile).uppercase(),
            style = KluvsTheme.typography.eyebrow,
            color = KluvsTheme.colors.contentMuted
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Avatar(
                name = name,
                avatarUrl = avatarUrl,
                size = 64.dp,
                isOwn = true,
                contentDescription = stringResource(R.string.profile_picture),
                onClick = onAvatarClick,
                isLoading = isUploadingAvatar
            )

            // Edit icon overlay
            FloatingActionButton(
                onClick = onAvatarClick,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd),
                containerColor = KluvsTheme.colors.accent,
                contentColor = KluvsTheme.colors.onAccent,
            ) {
                Icon(
                    type = IconType.Edit,
                    contentDescription = stringResource(R.string.edit_profile_picture),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        InputField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_name),
            value = editedName,
            onValueChange = onNameChanged,
        )

        Spacer(Modifier.height(8.dp))

        InputField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_handle),
            value = editedHandle,
            onValueChange = onHandleChanged,
            prefix = "@",
            error = handleError,
        )

        if (saveError != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = saveError,
                color = KluvsTheme.colors.danger,
                style = KluvsTheme.typography.body.medium,
            )
        }

        Spacer(Modifier.height(16.dp))

        PrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSaveProfile,
            enabled = hasChanges && !isSaving && handleError == null,
            text = if (isSaving) stringResource(R.string.button_save) + "…"
                else stringResource(R.string.button_save),
        )
    }
}

@PreviewLightDark
@Composable
fun Preview_EditProfileSection() = KluvsTheme {
    EditProfileSection(
        avatarUrl = null,
        name = "Alice",
        editedName = "Alice",
        editedHandle = "alice_reads",
        hasChanges = true,
        isSaving = false,
        saveError = null,
        onNameChanged = {},
        onHandleChanged = {},
        onSaveProfile = {},
    )
}

@PreviewLightDark
@Composable
fun Preview_EditProfileSection_WithError() = KluvsTheme {
    EditProfileSection(
        avatarUrl = null,
        name = "Alice",
        editedName = "Alice",
        editedHandle = "alice-reads",
        hasChanges = true,
        isSaving = false,
        saveError = "That handle is already taken",
        onNameChanged = {},
        onHandleChanged = {},
        onSaveProfile = {},
    )
}

@PreviewLightDark
@Composable
fun Preview_EditProfileSection_WithHandleError() = KluvsTheme {
    EditProfileSection(
        avatarUrl = null,
        name = "Alice",
        editedName = "Alice",
        editedHandle = "alice_reads",
        handleError = "Handles can only contain lowercase letters, numbers, and hyphens",
        hasChanges = true,
        isSaving = false,
        saveError = null,
        onNameChanged = {},
        onHandleChanged = {},
        onSaveProfile = {},
    )
}
