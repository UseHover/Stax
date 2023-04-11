package com.hover.stax.transfers

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.hover.stax.R
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact
import com.hover.stax.requests.Request
import com.hover.stax.requests.RequestRepo
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TransferViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val application = mockk<Application>(relaxed = true)
    private val requestRepo = mockk<RequestRepo>(relaxed = true)
    private val contactRepo = mockk<ContactRepo>(relaxed = true)
    private val scheduleRepo = mockk<ScheduleRepo>(relaxed = true)
    private lateinit var testSubject: TransferViewModel

    @Before
    fun setup() {
        testSubject = TransferViewModel(application, requestRepo, contactRepo, scheduleRepo)
    }

    @Test
    fun `setAmount updates amount LiveData correctly with non-null value`() = runTest {
        val amount = "1000"
        testSubject.setAmount(amount)
        assertThat(testSubject.amount.value).isEqualTo(amount)
    }

    @Test
    fun `setRecipientNumber updates contacts LiveData correctly with valid phone number`() = runTest {
        val phoneNumber = "+254713212213"
        val staxContact = mockk<StaxContact>(relaxed = true)
        coEvery { contactRepo.getContactByPhone(phoneNumber) } returns staxContact
        testSubject.setRecipientNumber(phoneNumber)
        assertThat(testSubject.contact.value).isEqualTo(staxContact)
    }

    @Test
    fun `test amount errors with valid input`() = runTest {
        testSubject.setAmount("1500")
        val result = testSubject.amountErrors(null)
        assertThat(result).isNull()
    }

    @Test
    fun `test amount errors with invalid input`() = runTest {
        testSubject.setAmount("fake")
        val result = testSubject.amountErrors(null)
        assertThat(application.getString(R.string.amount_fielderror)).isEqualTo(result)
        assertThat(result).isNotNull()
    }

    @Test
    fun `load sets isLoading to true`() = runTest {
        val encryptedString = "fake string"
        coEvery { requestRepo.decrypt(encryptedString, any()) } returns mockk()
        testSubject.load(encryptedString)
        assertThat(testSubject.isLoading.value).isTrue()
    }

    @Test
    fun `load sets recipient and amount if request is not null`() = runTest {
        val encryptedString = "fake string"
        val fakeNumber = "+254713212213"
        val fakeCountryAlpha = "KE"
        val fakeAmount = "1000"
        val fakeNote = "fake note"
        val mockRequest = mockk<Request>().also {
            every { it.requester_number } returns fakeNumber
            every { it.requester_country_alpha2 } returns fakeCountryAlpha
            every { it.amount } returns fakeAmount
            every { it.note } returns fakeNote
        }
        coEvery { requestRepo.decrypt(encryptedString, any()) } returns mockRequest
        coEvery { contactRepo.getContactByPhone(fakeNumber) } returns null
        testSubject.load(encryptedString)

        assertThat(testSubject.isLoading.value).isFalse()
        assertThat(testSubject.amount.value).isEqualTo(fakeAmount)
        assertThat(testSubject.note.value).isEqualTo(fakeNote)
    }

    @Test
    fun `saveContact should save contact and update lastUsedTimestamp`() = runTest {
        testSubject.contact.value = StaxContact().apply {
            this.accountNumber = "+254713212213"
            this.name = "fake contact"
        }
        testSubject.saveContact()
        verify { contactRepo.save(match{
            it.lastUsedTimestamp !=null
        }) }
        verify { contactRepo.save(any()) }
    }

    @Test
    fun `reset function resets LiveData values to null values`() = runTest {
        testSubject.amount.value = "1000"
        testSubject.contact.value = StaxContact("+254713212213")
        testSubject.note.value = "fake note"
        testSubject.reset()
        assertThat(testSubject.amount.value).isNull()
        assertThat(testSubject.contact.value).isNull()
        assertThat(testSubject.note.value).isNull()
    }
}