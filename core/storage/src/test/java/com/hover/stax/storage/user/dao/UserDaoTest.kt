package com.hover.stax.storage.user.dao

import com.google.common.truth.Truth.assertThat
import com.hover.stax.storage.user.entity.StaxUser
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UserDaoTest {
    private lateinit var staxUser: StaxUser

    @MockK(relaxed = true)
    private lateinit var userDao: com.hover.stax.storage.user.dao.user.UserDao

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        staxUser = StaxUser(
            id = 1,
            username = "Stocks",
            "stocks@stax.hover.com",
            isMapper = true,
            marketingOptedIn = true,
            transactionCount = 1,
            bountyTotal = 10,
            totalPoints = 100
        )
    }

    @Test
    fun `get async stax user`() = runTest {
        // Given
        coEvery { userDao.getUserAsync() } returns flow {
            emit(staxUser)
        }

        // When
        var username = ""
        userDao.getUserAsync().collectLatest { username = it.username }

        // Then
        assertThat(staxUser.username).isEqualTo(username)
    }

    @Test
    fun `get stax user from local storage`() = runTest {
        // Given
        coEvery { userDao.getUser() } returns staxUser

        // When
        val cachedStaxUser = userDao.getUser()

        // Then
        assertThat(cachedStaxUser?.username).isEqualTo(staxUser.username)
    }

    @Test
    fun `get cached stax user on empty local storage`() = runTest {
        // Given
        coEvery { userDao.getUser() } returns null

        // When
        val cachedStaxUser = userDao.getUser()

        // Then
        assertThat(cachedStaxUser).isNull()
    }

    @Test
    fun `insert stax user into local storage`() = runTest {
        // Given
        coJustRun { userDao.insert(staxUser) }

        // When
        userDao.insert(staxUser)

        // Then
        coVerify(exactly = 1) { userDao.insert(staxUser) }
    }
}