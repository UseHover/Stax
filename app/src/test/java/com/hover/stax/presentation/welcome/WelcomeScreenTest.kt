package com.hover.stax.presentation.welcome

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.hover.stax.ui.theme.StaxTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class WelcomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `Test home poster is displayed`() {
        composeTestRule.setContent {
            StaxTheme {
                WelcomeScreen(
                    {},
                    {},
                    true
                )
            }
        }
        composeTestRule.onNodeWithTag("welcome_header_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_header_description").assertIsDisplayed()
    }
}