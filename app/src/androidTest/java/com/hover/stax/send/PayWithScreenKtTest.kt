package com.hover.stax.send

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class PayWithScreenKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pay_with_screen_should_have_a_header() {
        composeTestRule.setContent {
            PayWithScreen(onClickBack = {}, accounts = emptyList(), onAccountSelected = {})
        }
        composeTestRule.onNodeWithTag(PayWithScreenTags.PAY_WITH_SCREEN_HEADER).assertIsDisplayed()
    }

}