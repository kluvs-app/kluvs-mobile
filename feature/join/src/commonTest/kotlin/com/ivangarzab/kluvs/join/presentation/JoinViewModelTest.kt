package com.ivangarzab.kluvs.join.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.JoinRepository
import com.ivangarzab.kluvs.join.domain.JoinClubUseCase
import com.ivangarzab.kluvs.join.domain.PreviewInviteUseCase
import com.ivangarzab.kluvs.model.ClubPreview
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class JoinViewModelTest {

    private lateinit var joinRepository: JoinRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var previewInvite: PreviewInviteUseCase
    private lateinit var joinClub: JoinClubUseCase
    private lateinit var isAuthenticatedFlow: MutableStateFlow<Boolean>

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testPreview = ClubPreview(id = "club-1", name = "Sci-Fi Club")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        joinRepository = mock<JoinRepository>()
        authRepository = mock<AuthRepository>()

        isAuthenticatedFlow = MutableStateFlow(false)
        every { authRepository.isAuthenticated } returns isAuthenticatedFlow

        previewInvite = PreviewInviteUseCase(joinRepository)
        joinClub = JoinClubUseCase(joinRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = JoinViewModel(previewInvite, joinClub, authRepository)

    @Test
    fun `previewInvite success updates preview`() = runTest {
        val viewModel = createViewModel()
        everySuspend { joinRepository.previewInvite("token-1") } returns Result.success(testPreview)

        viewModel.onTokenChanged("token-1")
        viewModel.previewInvite()

        val state = viewModel.state.value
        assertFalse(state.isLoadingPreview)
        assertEquals(testPreview, state.preview)
        assertNull(state.previewError)
    }

    @Test
    fun `previewInvite failure surfaces invalid or expired error`() = runTest {
        val viewModel = createViewModel()
        everySuspend { joinRepository.previewInvite("bad-token") } returns Result.failure(Exception("404"))

        viewModel.onTokenChanged("bad-token")
        viewModel.previewInvite()

        val state = viewModel.state.value
        assertFalse(state.isLoadingPreview)
        assertNull(state.preview)
        assertEquals("This invite link is invalid or has expired.", state.previewError)
    }

    @Test
    fun `onJoinClicked while signed in joins immediately`() = runTest {
        isAuthenticatedFlow.value = true
        val viewModel = createViewModel()
        everySuspend { joinRepository.joinClub("token-1") } returns Result.success("club-1")

        viewModel.onTokenChanged("token-1")
        viewModel.onJoinClicked()

        val state = viewModel.state.value
        assertFalse(state.isJoining)
        assertEquals("club-1", state.joinedClubId)
        assertFalse(state.needsSignIn)
    }

    @Test
    fun `onJoinClicked while signed out defers join and does not call repository`() = runTest {
        isAuthenticatedFlow.value = false
        val viewModel = createViewModel()

        viewModel.onTokenChanged("token-1")
        viewModel.onJoinClicked()

        val state = viewModel.state.value
        assertTrue(state.needsSignIn)
        assertNull(state.joinedClubId)
        verifySuspend(mode = dev.mokkery.verify.VerifyMode.not) { joinRepository.joinClub(any()) }
    }

    @Test
    fun `onJoinClicked failure surfaces error message`() = runTest {
        isAuthenticatedFlow.value = true
        val viewModel = createViewModel()
        everySuspend { joinRepository.joinClub("token-1") } returns Result.failure(Exception("Already a member"))

        viewModel.onTokenChanged("token-1")
        viewModel.onJoinClicked()

        val state = viewModel.state.value
        assertFalse(state.isJoining)
        assertNull(state.joinedClubId)
        assertEquals("Already a member", state.joinError)
    }

    @Test
    fun `onConsumeNeedsSignIn resets the flag`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTokenChanged("token-1")
        viewModel.onJoinClicked()
        assertTrue(viewModel.state.value.needsSignIn)

        viewModel.onConsumeNeedsSignIn()

        assertFalse(viewModel.state.value.needsSignIn)
    }
}
