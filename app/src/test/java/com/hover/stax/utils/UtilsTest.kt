/*
 * Copyright 2022 Stax
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
package com.hover.stax.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {

    @Test
    fun check_that_formatAmount_UsingADouble_With_SingleDecimalInputs_IsCorrect() {
        val variation1Input = 43.0
        val variation2Input = 43.1
        val variation3Input = 43.9

        val variation1Result = Utils.formatAmount(variation1Input)
        val variation2Result = Utils.formatAmount(variation2Input)
        val variation3Result = Utils.formatAmount(variation3Input)

        assertEquals("43.00", variation1Result)
        assertEquals("43.10", variation2Result)
        assertEquals("43.90", variation3Result)
    }

    @Test
    fun check_that_formatAmount_UsingADouble_With_TwoDecimalInputs_IsCorrect() {
        val variation1Input = 43.98
        val variation2Input = 43.93

        val variation1Result = Utils.formatAmount(variation1Input)
        val variation2Result = Utils.formatAmount(variation2Input)

        assertEquals(variation1Input.toString(), variation1Result)
        assertEquals(variation2Input.toString(), variation2Result)
    }

    @Test
    fun check_that_formatAmount_UsingADouble_With_ThreeDecimalInputs_IsCorrect() {
        val variation1Input = 43.986
        val variation2Input = 43.234

        val variation1Result = Utils.formatAmount(variation1Input)
        val variation2Result = Utils.formatAmount(variation2Input)

        assertEquals("43.99", variation1Result)
        assertEquals("43.23", variation2Result)
    }
}