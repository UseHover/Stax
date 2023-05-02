package com.hover.stax.schedules

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.just
import io.mockk.verify
import io.mockk.coVerify
import io.mockk.every
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ScheduleDetailViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repo = mockk<ScheduleRepo>(relaxed = true)
    private val actionRepo = mockk<ActionRepo>(relaxed = true)
    private val contactRepo = mockk<ContactRepo>(relaxed = true)
    private val schedule = mockk<Schedule>(relaxed = true)
    private lateinit var testSubject: ScheduleDetailViewModel

    @Before
    fun setup() {
        testSubject = ScheduleDetailViewModel(repo, actionRepo, contactRepo)
    }
    @Test
    fun `setSchedule should update schedule LiveData `()= runTest {
        coEvery { repo.getSchedule(any()) } returns mockk()
        testSubject.setSchedule(1)
        val result = testSubject.schedule.value
        assertThat(testSubject.schedule.value).isNotNull()
        assertThat(result?.action_id).isEqualTo(schedule.action_id)
        coVerify(exactly = 1) { repo.getSchedule(1) }
    }

    @Test
    fun `deleteSchedule should call delete method in ScheduleRepo`()= runTest {
        every { repo.delete(any()) } just runs
        testSubject.schedule.value = schedule
        testSubject.deleteSchedule()
        verify(exactly = 1) { repo.delete(schedule) }
    }
}