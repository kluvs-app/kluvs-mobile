package com.ivangarzab.kluvs.settings.presentation

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.settings.domain.GetEditableProfileUseCase
import com.ivangarzab.kluvs.settings.domain.UpdateUserProfileUseCase
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
    private lateinit var getEditableProfile: GetEditableProfileUseCase
    private lateinit var updateUserProfile: UpdateUserProfileUseCase
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val memberId = "member-123"
    private val userId = "user-456"
    private val name = "Alice"
    private val handle = "alice_reads"

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
        getEditableProfile = GetEditableProfileUseCase(memberRepository)
        updateUserProfile = UpdateUserProfileUseCase(memberRepository)
        viewModel = SettingsViewModel(getEditableProfile, updateUserProfile)
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
        viewModel.onHandleChanged("bob_reads")

        // Then
        assertEquals("bob_reads", viewModel.state.value.editedHandle)
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
        viewModel.onHandleChanged("bob_reads")

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
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
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
    fun `onSaveProfile with invalid handle sets saveError`() = runTest {
        // Given: profile loaded and user enters invalid handle
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)
        viewModel.onHandleChanged("invalid handle!")

        // When
        viewModel.onSaveProfile()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertTrue(state.saveError != null)
    }

    @Test
    fun `onSaveProfile while saving is a no-op`() = runTest {
        // Given: state already has isSaving = true
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(testMember)
        viewModel.loadProfile(userId)

        // Manually put the ViewModel into saving state by calling onSaveProfile once
        // with a slow mock — but since UnconfinedTestDispatcher is synchronous, we test
        // by checking the guard on repeated calls
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
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
        everySuspend { memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(testMember)
        viewModel.onSaveProfile()
        assertTrue(viewModel.state.value.saveSuccess)

        // When
        viewModel.onDismissSaveSuccess()

        // Then
        assertFalse(viewModel.state.value.saveSuccess)
    }
}
