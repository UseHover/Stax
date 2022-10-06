package com.hover.stax.utils

import org.junit.Assert
import org.junit.Test


class UtilsTest {
	@Test
	fun check_that_formatAmount_UsingADouble_With_SingleDecimalInputs_IsCorrect() {
		val variation1Input  = 43.0
		val variation2Input = 43.1
		val variation3Input = 43.9

		val variation1Result = Utils.formatAmount(variation1Input)
		val variation2Result = Utils.formatAmount(variation2Input)
		val variation3Result = Utils.formatAmount(variation3Input)

		Assert.assertEquals("43.00", variation1Result)
		Assert.assertEquals("43.10", variation2Result)
		Assert.assertEquals("43.90", variation3Result)
	}

	@Test
	fun check_that_formatAmount_UsingADouble_With_TwoDecimalInputs_IsCorrect() {
		val variation1Input = 43.98
		val variation2Input = 43.93

		val variation1Result = Utils.formatAmount(variation1Input)
		val variation2Result = Utils.formatAmount(variation2Input)

		Assert.assertEquals(variation1Input.toString(), variation1Result)
		Assert.assertEquals(variation2Input.toString(), variation2Result)
	}

	@Test
	fun check_that_formatAmount_UsingADouble_With_ThreeDecimalInputs_IsCorrect() {
		val variation1Input = 43.986
		val variation2Input = 43.234

		val variation1Result = Utils.formatAmount(variation1Input)
		val variation2Result = Utils.formatAmount(variation2Input)

		Assert.assertEquals("43.99", variation1Result)
		Assert.assertEquals("43.23", variation2Result)
	}


}