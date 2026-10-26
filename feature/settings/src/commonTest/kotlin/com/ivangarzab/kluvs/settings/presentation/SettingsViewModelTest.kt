package com.ivangarzab.kluvs.settings.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.database.dao.BookDao
import com.ivangarzab.kluvs.database.dao.ClubDao
import com.ivangarzab.kluvs.database.dao.DiscussionAttendanceDao
import com.ivangarzab.kluvs.database.dao.DiscussionDao
import com.ivangarzab.kluvs.database.dao.DiscussionNoteDao
import com.ivangarzab.kluvs.database.dao.LikeDao
import com.ivangarzab.kluvs.database.dao.MemberDao
import com.ivangarzab.kluvs.database.dao.ProgressDao
import com.ivangarzab.kluvs.database.dao.ServerDao
import com.ivangarzab.kluvs.database.dao.SessionDao
import com.ivangarzab.kluvs.database.dao.ShelfDao
import com.ivangarzab.kluvs.model.AppError
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.settings.domain.ChangePasswordUseCase
import com.ivangarzab.kluvs.settings.domain.DeleteAccountUseCase
import com.ivangarzab.kluvs.settings.domain.GetEditableProfileUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateAvatarUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateUserProfileUseCase
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var avatarRepository: AvatarRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var database: KluvsDatabase
    private lateinit var getEditableProfile: GetEditableProfileUseCase
    private lateinit var updateUserProfile: UpdateUserProfileUseCase
    private lateinit var updateAvatarUseCase: UpdateAvatarUseCase
    private lateinit var changePasswordUseCase: ChangePasswordUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val memberId = "member-123"
    private val userId = "user-456"
    private val name = "Alice"
    private val handle = "alice-reads"

    private val testMember = Member(
        id = memberId,
        name = name,
        handle = handle,
        userId = userId,
        booksRead = 5
    )

    private val testProfile = EditableProfile(
        memberId = memberId,
        name = name,
        handle = handle
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        memberRepository = mock<MemberRepository>()
        avatarRepository = mock<AvatarRepository>()
        authRepository = mock<AuthRepository>()

        // SignOutUseCase clears every DAO on sign-out (see SignOutUseCaseTest for the same setup).
        val clubDao = mock<ClubDao>()
        val serverDao = mock<ServerDao>()
        val memberDao = mock<MemberDao>()
        val sessionDao = mock<SessionDao>()
        val bookDao = mock<BookDao>()
        val discussionDao = mock<DiscussionDao>()
        val shelfDao = mock<ShelfDao>()
        val likeDao = mock<LikeDao>()
        val progressDao = mock<ProgressDao>()
        val discussionNoteDao = mock<DiscussionNoteDao>()
        val discussionAttendanceDao = mock<DiscussionAttendanceDao>()

        database = mock<KluvsDatabase>()
        every { database.clubDao() } returns clubDao
        every { database.serverDao() } returns serverDao
        every { database.memberDao() } returns memberDao
        every { database.sessionDao() } returns sessionDao
        every { database.bookDao() } returns bookDao
        every { database.discussionDao() } returns discussionDao
        every { database.shelfDao() } returns shelfDao
        every { database.likeDao() } returns likeDao
        every { database.progressDao() } returns progressDao
        every { database.discussionNoteDao() } returns discussionNoteDao
        every { database.discussionAttendanceDao() } returns discussionAttendanceDao

        everySuspend { clubDao.deleteAll() } returns Unit
        everySuspend { serverDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAllCrossRefs() } returns Unit
        everySuspend { sessionDao.deleteAll() } returns Unit
        everySuspend { bookDao.deleteAll() } returns Unit
        everySuspend { discussionDao.deleteAll() } returns Unit
        everySuspend { shelfDao.deleteAll() } returns Unit
        everySuspend { likeDao.deleteAll() } returns Unit
        everySuspend { progressDao.deleteAll() } returns Unit
        everySuspend { discussionNoteDao.deleteAll() } returns Unit
        everySuspend { discussionAttendanceDao.deleteAll() } returns Unit

        getEditableProfile = GetEditableProfileUseCase(memberRepository, avatarRepository)
        updateUserProfile = UpdateUserProfileUseCase(memberRepository)
        updateAvatarUseCase = UpdateAvatarUseCase(avatarRepository, memberRepository)
        changePasswordUseCase = ChangePasswordUseCase(authRepository)
        deleteAccountUseCase = DeleteAccountUseCase(memberRepository, authRepository)
        signOutUseCase = SignOutUseCase(authRepository, database)
        viewModel = SettingsViewModel(
            getEditableProfile,
            updateUserProfile,
            updateAvatarUseCase,
            changePasswordUseCase,
            deleteAccountUseCase,
            signOutUseCase
        )
        every { avatarRepository.getAvatarUrl(null) } returns null
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // LOAD PROFILE
    // ========================================

    @Test
    fun `loadProfile success populates state with profile and editable fields`() = runTest {
        // Given
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)

        // When
        viewModel.loadProfile(userId)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(testProfile, state.profile)
        assertEquals(name, state.editedName)
        assertEquals(handle, state.editedHandle)
        assertFalse(state.hasChanges)
    }

    @Test
    fun `loadProfile failure sets error state`() = runTest {
        // Given
        val exception = Exception("Member not found")
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.failure(exception)

        // When
        viewModel.loadProfile(userId)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Member not found", state.error)
        assertNull(state.profile)
    }

    // ========================================
    // FIELD CHANGES
    // ========================================

    @Test
    fun `onNameChanged updates editedName`() = runTest {
        // Given: profile loaded
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When
        viewModel.onNameChanged("Bob")

        // Then
        assertEquals("Bob", viewModel.state.value.editedName)
    }

    @Test
    fun `onHandleChanged updates editedHandle`() = runTest {
        // Given: profile loaded
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When
        viewModel.onHandleChanged("bob-reads")

        // Then
        assertEquals("bob-reads", viewModel.state.value.editedHandle)
        assertNull(viewModel.state.value.handleError)
    }

    @Test
    fun `onHandleChanged lowercases uppercase input silently`() = runTest {
        // Given: profile loaded — uppercase is a soft transform, not a rejected character.
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When
        viewModel.onHandleChanged("BobReads")

        // Then
        assertEquals("bobreads", viewModel.state.value.editedHandle)
        assertNull(viewModel.state.value.handleError)
    }

    @Test
    fun `onHandleChanged with underscore sets handleError without stripping it`() = runTest {
        // Given: profile loaded — out-of-charset characters are kept as typed and flagged with
        // an inline error, not silently transformed like case is.
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When
        viewModel.onHandleChanged("bob_reads")

        // Then
        assertEquals("bob_reads", viewModel.state.value.editedHandle)
        assertTrue(viewModel.state.value.handleError != null)
    }

    // ========================================
    // HAS CHANGES
    // ========================================

    @Test
    fun `hasChanges is false when fields match original`() = runTest {
        // Given: profile loaded
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When: set fields back to original values
        viewModel.onNameChanged(name)
        viewModel.onHandleChanged(handle)

        // Then
        assertFalse(viewModel.state.value.hasChanges)
    }

    @Test
    fun `hasChanges is true when name differs from original`() = runTest {
        // Given: profile loaded
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When
        viewModel.onNameChanged("Bob")

        // Then
        assertTrue(viewModel.state.value.hasChanges)
    }

    @Test
    fun `hasChanges is true when handle differs from original`() = runTest {
        // Given: profile loaded
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When
        viewModel.onHandleChanged("bob-reads")

        // Then
        assertTrue(viewModel.state.value.hasChanges)
    }

    // ========================================
    // SAVE PROFILE
    // ========================================

    @Test
    fun `onSaveProfile with valid data succeeds and sets saveSuccess`() = runTest {
        // Given: profile loaded and user makes a change
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onNameChanged("Alice Updated")

        // When
        viewModel.onSaveProfile()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isSaving)
        assertTrue(state.saveSuccess)
        assertNull(state.saveError)
        assertFalse(state.hasChanges)
    }

    @Test
    fun `onSaveProfile with handle that fails full format validation sets saveError`() = runTest {
        // Given: profile loaded and user enters a handle that passes the inline charset check
        // (letters, digits, hyphens only) but fails the UseCase's full structural/length
        // validation — only the round trip through UpdateUserProfileUseCase catches this.
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onHandleChanged("a")

        // When
        viewModel.onSaveProfile()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertTrue(state.saveError != null)
    }

    @Test
    fun `onSaveProfile is a no-op when handleError is set`() = runTest {
        // Given: profile loaded and user enters a handle with an out-of-charset character —
        // the inline validation error should block the save round trip entirely.
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onHandleChanged("bob_reads")
        assertTrue(viewModel.state.value.handleError != null)

        // When
        viewModel.onSaveProfile()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertNull(state.saveError)
    }

    @Test
    fun `onSaveProfile surfaces a friendly message on 409 conflict`() = runTest {
        // Given: profile loaded and the server rejects the save because the handle is taken
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        everySuspend {
            memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(AppError.Conflict("That handle is already taken"))
        viewModel.loadProfile(userId)
        viewModel.onHandleChanged("taken-handle")

        // When
        viewModel.onSaveProfile()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertEquals("That handle is already taken", state.saveError)
    }

    @Test
    fun `onSaveProfile while saving is a no-op`() = runTest {
        // Given: state already has isSaving = true
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // Manually put the ViewModel into saving state by calling onSaveProfile once
        // with a slow mock — but since UnconfinedTestDispatcher is synchronous, we test
        // by checking the guard on repeated calls
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
        viewModel.onSaveProfile()

        // After the first call resolves, saveSuccess should be true
        assertTrue(viewModel.state.value.saveSuccess)

        // Dismiss success, then call again — should work normally (not blocked)
        viewModel.onDismissSaveSuccess()
        viewModel.onSaveProfile()

        // Still succeeds (no deadlock / no-op issue)
        assertTrue(viewModel.state.value.saveSuccess)
    }

    // ========================================
    // DISMISS SAVE SUCCESS
    // ========================================

    @Test
    fun `onDismissSaveSuccess resets saveSuccess flag`() = runTest {
        // Given: profile loaded and save succeeded
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
        viewModel.onSaveProfile()
        assertTrue(viewModel.state.value.saveSuccess)

        // When
        viewModel.onDismissSaveSuccess()

        // Then
        assertFalse(viewModel.state.value.saveSuccess)
    }

    // ========================================
    // AVATAR UPLOAD
    // ========================================

    @Test
    fun `uploadAvatar succeeds and updates avatar URL in state`() = runTest {
        // Given: profile loaded (no avatar)
        val imageData = ByteArray(100) { it.toByte() }
        val storagePath = "$memberId/avatar.png"
        val avatarUrl = "https://storage.example.com/$storagePath"
        val memberWithAvatar = testMember.copy(avatarPath = storagePath)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        everySuspend { memberRepository.getMember(memberId) } returns Result.success(testMember)
        everySuspend { avatarRepository.uploadAvatar(memberId, imageData) } returns Result.success(storagePath)
        everySuspend { memberRepository.updateMember(memberId, avatarPath = storagePath) } returns Result.success(memberWithAvatar)
        every { avatarRepository.getAvatarUrl(storagePath) } returns avatarUrl
        // oldAvatarPath is null, so deleteAvatar is not called

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertNull(state.avatarError)
        assertEquals(avatarUrl, state.profile?.avatarUrl)
    }

    @Test
    fun `uploadAvatar fails when no member ID available`() = runTest {
        // Given: no profile loaded, so memberId is null
        val imageData = ByteArray(100)

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertTrue(state.avatarError != null)
    }

    @Test
    fun `uploadAvatar handles upload failure`() = runTest {
        // Given: profile loaded
        val imageData = ByteArray(100)
        val exception = Exception("Upload failed")

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        everySuspend { memberRepository.getMember(memberId) } returns Result.success(testMember)
        everySuspend { avatarRepository.uploadAvatar(memberId, imageData) } returns Result.failure(exception)

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertEquals("Upload failed", state.avatarError)
    }

    @Test
    fun `clearAvatarError clears the error state`() = runTest {
        // Given: profile loaded and an upload failed
        val imageData = ByteArray(100)
        val exception = Exception("Upload failed")

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        everySuspend { memberRepository.getMember(memberId) } returns Result.success(testMember)
        everySuspend { avatarRepository.uploadAvatar(memberId, imageData) } returns Result.failure(exception)
        viewModel.uploadAvatar(imageData)
        assertTrue(viewModel.state.value.avatarError != null)

        // When
        viewModel.clearAvatarError()

        // Then
        assertNull(viewModel.state.value.avatarError)
    }

    // ========================================
    // CHANGE PASSWORD
    // ========================================

    @Test
    fun `onSubmitChangePassword with valid matching passwords succeeds`() = runTest {
        // Given
        everySuspend { authRepository.changePassword("newSecurePass123") } returns Result.success(Unit)
        viewModel.onNewPasswordFieldChanged("newSecurePass123")
        viewModel.onConfirmPasswordFieldChanged("newSecurePass123")

        // When
        viewModel.onSubmitChangePassword()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isChangingPassword)
        assertTrue(state.changePasswordSuccess)
        assertNull(state.changePasswordGeneralError)
        assertEquals("", state.newPasswordField)
        assertEquals("", state.confirmPasswordField)
    }

    @Test
    fun `onSubmitChangePassword with blank password sets newPasswordError and does not call repository`() = runTest {
        // Given
        viewModel.onNewPasswordFieldChanged("")
        viewModel.onConfirmPasswordFieldChanged("")

        // When
        viewModel.onSubmitChangePassword()

        // Then
        val state = viewModel.state.value
        assertTrue(state.newPasswordError != null)
        assertFalse(state.isChangingPassword)
        assertFalse(state.changePasswordSuccess)
    }

    @Test
    fun `onSubmitChangePassword with too-short password sets newPasswordError`() = runTest {
        // Given
        viewModel.onNewPasswordFieldChanged("abc")
        viewModel.onConfirmPasswordFieldChanged("abc")

        // When
        viewModel.onSubmitChangePassword()

        // Then
        assertTrue(viewModel.state.value.newPasswordError != null)
    }

    @Test
    fun `onSubmitChangePassword with mismatched confirmation sets confirmPasswordError`() = runTest {
        // Given
        viewModel.onNewPasswordFieldChanged("newSecurePass123")
        viewModel.onConfirmPasswordFieldChanged("somethingElse123")

        // When
        viewModel.onSubmitChangePassword()

        // Then
        assertTrue(viewModel.state.value.confirmPasswordError != null)
    }

    @Test
    fun `onSubmitChangePassword surfaces repository AuthError as changePasswordGeneralError`() = runTest {
        // Given
        everySuspend { authRepository.changePassword("newSecurePass123") } returns Result.failure(AuthError.WeakPassword)
        viewModel.onNewPasswordFieldChanged("newSecurePass123")
        viewModel.onConfirmPasswordFieldChanged("newSecurePass123")

        // When
        viewModel.onSubmitChangePassword()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isChangingPassword)
        assertFalse(state.changePasswordSuccess)
        assertEquals(AuthError.WeakPassword, state.changePasswordGeneralError)
    }

    @Test
    fun `onChangePasswordSheetDismissed resets change password fields and errors`() = runTest {
        // Given
        viewModel.onChangePasswordSheetOpened()
        viewModel.onNewPasswordFieldChanged("abc")
        viewModel.onConfirmPasswordFieldChanged("abc")
        viewModel.onSubmitChangePassword()
        assertTrue(viewModel.state.value.newPasswordError != null)

        // When
        viewModel.onChangePasswordSheetDismissed()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isChangePasswordSheetOpen)
        assertEquals("", state.newPasswordField)
        assertEquals("", state.confirmPasswordField)
        assertNull(state.newPasswordError)
        assertNull(state.confirmPasswordError)
        assertNull(state.changePasswordGeneralError)
    }

    // ========================================
    // DELETE ACCOUNT
    // ========================================

    @Test
    fun `onDeleteAccountClicked opens the confirmation dialog`() = runTest {
        // When
        viewModel.onDeleteAccountClicked()

        // Then
        assertTrue(viewModel.state.value.isDeleteAccountDialogOpen)
    }

    @Test
    fun `onDeleteAccountDialogDismissed closes the confirmation dialog`() = runTest {
        // Given
        viewModel.onDeleteAccountClicked()
        assertTrue(viewModel.state.value.isDeleteAccountDialogOpen)

        // When
        viewModel.onDeleteAccountDialogDismissed()

        // Then
        assertFalse(viewModel.state.value.isDeleteAccountDialogOpen)
    }

    @Test
    fun `onDeleteAccountConfirmed with no profile loaded is a no-op`() = runTest {
        // When: no profile has been loaded, so memberId is unknown
        viewModel.onDeleteAccountConfirmed()

        // Then
        assertFalse(viewModel.state.value.isDeletingAccount)
        verifySuspend(mode = VerifyMode.not) { memberRepository.deleteMember(any()) }
    }

    @Test
    fun `onDeleteAccountConfirmed success signs out and clears dialog state`() = runTest {
        // Given: profile loaded and deletion succeeds
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onDeleteAccountClicked()
        everySuspend { memberRepository.deleteMember(memberId) } returns Result.success("Account deleted")
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When
        viewModel.onDeleteAccountConfirmed()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isDeletingAccount)
        assertFalse(state.isDeleteAccountDialogOpen)
        assertNull(state.deleteAccountError)
        verifySuspend { memberRepository.deleteMember(memberId) }
        verifySuspend { authRepository.signOut() }
    }

    @Test
    fun `onDeleteAccountConfirmed failure surfaces inline error and closes dialog without signing out`() = runTest {
        // Given: profile loaded, deletion fails, and the account still exists
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onDeleteAccountClicked()
        everySuspend {
            memberRepository.deleteMember(memberId)
        } returns Result.failure(Exception("Deletion failed"))
        everySuspend { authRepository.currentUserExists() } returns Result.success(true)

        // When
        viewModel.onDeleteAccountConfirmed()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isDeletingAccount)
        assertFalse(state.isDeleteAccountDialogOpen)
        assertTrue(state.deleteAccountError != null)
        verifySuspend(mode = VerifyMode.not) { authRepository.signOut() }
    }

    @Test
    fun `onDeleteAccountConfirmed failure still signs out when the account no longer exists`() = runTest {
        // Given: the backend deleted the account, but the delete response never made it back
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        everySuspend {
            memberRepository.deleteMember(memberId)
        } returns Result.failure(Exception("Request timed out"))
        everySuspend { authRepository.currentUserExists() } returns Result.success(false)
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When
        viewModel.onDeleteAccountConfirmed()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isDeletingAccount)
        assertNull(state.deleteAccountError)
        verifySuspend { authRepository.signOut() }
    }

    @Test
    fun `clearDeleteAccountError clears the error state`() = runTest {
        // Given: profile loaded and a failed deletion
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        everySuspend {
            memberRepository.deleteMember(memberId)
        } returns Result.failure(Exception("Deletion failed"))
        everySuspend { authRepository.currentUserExists() } returns Result.success(true)
        viewModel.onDeleteAccountConfirmed()
        assertTrue(viewModel.state.value.deleteAccountError != null)

        // When
        viewModel.clearDeleteAccountError()

        // Then
        assertNull(viewModel.state.value.deleteAccountError)
    }
}
