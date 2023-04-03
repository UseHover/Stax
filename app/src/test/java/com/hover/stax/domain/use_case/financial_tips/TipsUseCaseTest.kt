/*
 * Copyright 2023 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.domain.use_case.financial_tips

import com.google.common.truth.Truth.assertThat
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.FinancialTipsRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TipsUseCaseTest {

    private val financialTipsRepository = mockk<FinancialTipsRepository>(relaxed = true)
    private lateinit var testSubject: TipsUseCase

    @Before
    fun setup() {
        testSubject = TipsUseCase(financialTipsRepository)
    }

    @Test
    fun `invoke should emit Resource Loading then Success with tips from the repository`() = runBlocking {
        val fakeTips = listOf(
            FinancialTip(
                id = "1234",
                title = "fakeTitle",
                content = "fakeContent",
                snippet = "fakeSnippet",
                date = 2L,
                shareCopy = "fakeCopy",
                deepLink = "fakeLink"
            ),
            FinancialTip(
                id = "4321",
                title = "randomTitle",
                content = "randomContent",
                snippet = "randomSnippet",
                date = 2L,
                shareCopy = "randomCopy",
                deepLink = "randomLink"
            )
        )
        coEvery { financialTipsRepository.getTips() } returns fakeTips
        val result = testSubject.invoke().toList()
        assertThat(result.size).isEqualTo(2)
        assertThat(result[0] is Resource.Loading).isTrue()
        assertThat(result[1] is Resource.Success).isTrue()
        assertThat(fakeTips).isEqualTo((result[1] as Resource.Success).data)
    }

    @Test
    fun `invoke should emit Resource Error if an exception is thrown`() = runBlocking {
        coEvery { financialTipsRepository.getTips() } throws Exception("Error fetching tips")
        val result = testSubject.invoke().toList()
        assertThat(result.size).isEqualTo(2)
        assertThat(result[0] is Resource.Loading).isTrue()
        assertThat(result[1] is Resource.Error).isTrue()
        assertThat("Error fetching tips").isEqualTo((result[1] as Resource.Error).message)
    }

    @Test
    fun `getDismissedTipId should return the id of the dismissed tip from the repository `() = runBlocking {
        val expectedId = "10"
        coEvery { financialTipsRepository.getDismissedTipId() } returns expectedId
        val result = testSubject.getDismissedTipId()
        assertThat(result).isEqualTo(expectedId)
    }

    @Test
    fun `dismissTip should call the repository's dismiss tip`() = runBlocking {
        val tipId = "1"
        testSubject.dismissTip(tipId)
        verify { financialTipsRepository.dismissTip(tipId) }
    }
}