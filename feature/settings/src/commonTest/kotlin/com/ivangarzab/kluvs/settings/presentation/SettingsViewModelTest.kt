package com.ivangarzab.kluvs.settings.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.database.dao.BookDao
import com.ivangarzab.kluvs.database.dao.BookEnrichmentDao
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
import com.ivangarzab.kluvs.presentation.effect.NoticeKey
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.effect.SnackbarMessage
import com.ivangarzab.kluvs.presentation.effect.SnackbarTone
import com.ivangarzab.kluvs.settings.domain.ChangePasswordUseCase
import com.ivangarzab.kluvs.settings.domain.DeleteAccountUseCase
import com.ivangarzab.kluvs.settings.domain.GetEditableProfileUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateAvatarUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateUserProfileUseCase
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
        val bookEnrichmentDao = mock<BookEnrichmentDao>()
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
        every { database.bookEnrichmentDao() } returns bookEnrichmentDao
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
        everySuspend { bookEnrichmentDao.deleteAll() } returns Unit
        everySuspend { discussionDao.deleteAll() } returns Unit
        everySuspend { shelfDao.deleteAll() } returns Unit
        everySuspend { likeDao.deleteAll() } returns Unit
        everySuspend { progressDao.deleteAll() } returns Unit
        everySuspend { progressDao.deleteAllQueries() } returns Unit
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
        val exception = AppError.ServerError
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.failure(exception)

        // When
        viewModel.loadProfile(userId)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Something went wrong on our end. Please try again.", state.error)
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
    fun `onSaveProfile with valid data succeeds and shows the save notice`() = runTest {
        val effects = collectScreenEffects()
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
        assertEquals(listOf(saveSuccessNotice), effects)
        assertNull(state.saveError)
        assertFalse(state.hasChanges)
    }

    @Test
    fun `onSaveProfile with handle that fails full format validation sets saveError`() = runTest {
        val effects = collectScreenEffects()
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
        assertTrue(effects.isEmpty())
        assertTrue(state.saveError != null)
    }

    @Test
    fun `onSaveProfile is a no-op when handleError is set`() = runTest {
        val effects = collectScreenEffects()
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
        assertTrue(effects.isEmpty())
        assertNull(state.saveError)
    }

    @Test
    fun `onSaveProfile surfaces a friendly message on 409 conflict`() = runTest {
        val effects = collectScreenEffects()
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
        assertTrue(effects.isEmpty())
        assertEquals("That handle is already taken", state.saveError)
    }

    @Test
    fun `onSaveProfile can save again after a save finishes`() = runTest {
        // Given
        val effects = collectScreenEffects()
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // When: UnconfinedTestDispatcher finishes each save before the next call
        viewModel.onSaveProfile()
        viewModel.onSaveProfile()

        // Then: neither save was blocked
        assertEquals(listOf(saveSuccessNotice, saveSuccessNotice), effects)
    }

    // ========================================
    // AVATAR UPLOAD
    // ========================================

    @Test
    fun `uploadAvatar succeeds and updates avatar URL in state`() = runTest {
        val effects = collectScreenEffects()
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
        assertTrue(effects.isEmpty())
        assertEquals(avatarUrl, state.profile?.avatarUrl)
    }

    @Test
    fun `uploadAvatar fails when no member ID available`() = runTest {
        val effects = collectScreenEffects()
        // Given: no profile loaded, so memberId is null
        val imageData = ByteArray(100)

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertEquals(SnackbarTone.DANGER, effects.singleSnackbar().tone)
    }

    @Test
    fun `uploadAvatar handles upload failure`() = runTest {
        val effects = collectScreenEffects()
        // Given: profile loaded
        val imageData = ByteArray(100)
        val exception = AppError.NoConnection

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        everySuspend { memberRepository.getMember(memberId) } returns Result.success(testMember)
        everySuspend { avatarRepository.uploadAvatar(memberId, imageData) } returns Result.failure(exception)

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertEquals(ScreenEffect.ShowSnackbar.error("Check your connection and try again."), effects.single())
    }

    // ========================================
    // CHANGE PASSWORD
    // ========================================

    @Test
    fun `onSubmitChangePassword with valid matching passwords succeeds`() = runTest {
        val effects = collectScreenEffects()
        // Given
        everySuspend { authRepository.changePassword("newSecurePass123") } returns Result.success(Unit)
        viewModel.onOpenSheet(SettingsSheet.ChangePassword)
        viewModel.onNewPasswordFieldChanged("newSecurePass123")
        viewModel.onConfirmPasswordFieldChanged("newSecurePass123")

        // When
        viewModel.onSubmitChangePassword()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isChangingPassword)
        assertEquals(listOf(passwordUpdatedNotice), effects)
        assertNull(state.changePasswordGeneralError)
        assertEquals("", state.newPasswordField)
        assertEquals("", state.confirmPasswordField)
        assertNull(state.activeSheet, "A successful change closes the sheet")
    }

    @Test
    fun `onSubmitChangePassword with blank password sets newPasswordError and does not call repository`() = runTest {
        val effects = collectScreenEffects()
        // Given
        viewModel.onNewPasswordFieldChanged("")
        viewModel.onConfirmPasswordFieldChanged("")

        // When
        viewModel.onSubmitChangePassword()

        // Then
        val state = viewModel.state.value
        assertTrue(state.newPasswordError != null)
        assertFalse(state.isChangingPassword)
        assertTrue(effects.isEmpty())
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
        val effects = collectScreenEffects()
        // Given
        everySuspend { authRepository.changePassword("newSecurePass123") } returns Result.failure(AuthError.WeakPassword)
        viewModel.onNewPasswordFieldChanged("newSecurePass123")
        viewModel.onConfirmPasswordFieldChanged("newSecurePass123")

        viewModel.onOpenSheet(SettingsSheet.ChangePassword)

        // When
        viewModel.onSubmitChangePassword()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isChangingPassword)
        assertTrue(effects.isEmpty())
        assertEquals(AuthError.WeakPassword, state.changePasswordGeneralError)
        assertEquals(SettingsSheet.ChangePassword, state.activeSheet, "A failed change leaves the sheet open")
    }

    @Test
    fun `dismissing Change Password resets its fields and errors`() = runTest {
        // Given
        viewModel.onOpenSheet(SettingsSheet.ChangePassword)
        viewModel.onNewPasswordFieldChanged("abc")
        viewModel.onConfirmPasswordFieldChanged("abc")
        viewModel.onSubmitChangePassword()
        assertTrue(viewModel.state.value.newPasswordError != null)

        // When
        viewModel.onDismissSheet()

        // Then
        val state = viewModel.state.value
        assertNull(state.activeSheet)
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
    fun `onOpenDialog opens the delete account confirmation`() = runTest {
        // When
        viewModel.onOpenDialog(SettingsDialog.DeleteAccount)

        // Then
        assertEquals(SettingsDialog.DeleteAccount, viewModel.state.value.activeDialog)
    }

    @Test
    fun `onDismissDialog closes the delete account confirmation`() = runTest {
        // Given
        viewModel.onOpenDialog(SettingsDialog.DeleteAccount)

        // When
        viewModel.onDismissDialog()

        // Then
        assertNull(viewModel.state.value.activeDialog)
    }

    @Test
    fun `the credits sheet opens and dismisses`() = runTest {
        viewModel.onOpenSheet(SettingsSheet.Credits)
        assertEquals(SettingsSheet.Credits, viewModel.state.value.activeSheet)

        viewModel.onDismissSheet()
        assertNull(viewModel.state.value.activeSheet)
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
        val effects = collectScreenEffects()
        // Given: profile loaded and deletion succeeds
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onOpenDialog(SettingsDialog.DeleteAccount)
        everySuspend { memberRepository.deleteMember(memberId) } returns Result.success("Account deleted")
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When
        viewModel.onDeleteAccountConfirmed()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isDeletingAccount)
        assertNull(state.activeDialog)
        assertTrue(effects.isEmpty())
        verifySuspend { memberRepository.deleteMember(memberId) }
        verifySuspend { authRepository.signOut() }
    }

    @Test
    fun `onDeleteAccountConfirmed failure surfaces inline error and closes dialog without signing out`() = runTest {
        val effects = collectScreenEffects()
        // Given: profile loaded, deletion fails, and the account still exists
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onOpenDialog(SettingsDialog.DeleteAccount)
        everySuspend {
            memberRepository.deleteMember(memberId)
        } returns Result.failure(Exception("Deletion failed"))
        everySuspend { authRepository.currentUserExists() } returns Result.success(true)

        // When
        viewModel.onDeleteAccountConfirmed()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isDeletingAccount)
        assertNull(state.activeDialog)
        assertEquals(SnackbarTone.DANGER, effects.singleSnackbar().tone)
        verifySuspend(mode = VerifyMode.not) { authRepository.signOut() }
    }

    @Test
    fun `onDeleteAccountConfirmed failure still signs out when the account no longer exists`() = runTest {
        val effects = collectScreenEffects()
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
        assertTrue(effects.isEmpty())
        verifySuspend { authRepository.signOut() }
    }

    @Test
    fun `onDeleteAccountConfirmed still signs out when the ViewModel is cleared mid-delete`() = runTest {
        // Given: the ViewModel lives in a store, like a screen's (cleared when the screen closes)
        val store = ViewModelStore()
        val factory = viewModelFactory { initializer { viewModel } }
        val scopedViewModel = ViewModelProvider.create(store, factory)[SettingsViewModel::class]
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        scopedViewModel.loadProfile(userId)
        val deleteResponse = CompletableDeferred<Result<String>>()
        everySuspend { memberRepository.deleteMember(memberId) } calls { deleteResponse.await() }
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When: the screen closes while the delete request is in flight
        scopedViewModel.onDeleteAccountConfirmed()
        store.clear()
        deleteResponse.complete(Result.success("Account deleted"))

        // Then: the account is gone server-side, so the client must still sign out
        verifySuspend { authRepository.signOut() }
    }

    /** Collects every screen effect the ViewModel sends from here on. */
    private fun TestScope.collectScreenEffects(): List<ScreenEffect> {
        val effects = mutableListOf<ScreenEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.screenEffects.toList(effects) }
        return effects
    }

    private fun List<ScreenEffect>.singleSnackbar() = single() as ScreenEffect.ShowSnackbar

    private val saveSuccessNotice = ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(NoticeKey.SAVE_SUCCESS))
    private val passwordUpdatedNotice = ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(NoticeKey.PASSWORD_UPDATED))
}
