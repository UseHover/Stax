package com.hover.stax.utils

import org.junit.Assert
import org.junit.Test


class UtilsTest {
	@Test
	fun formatAmount_UsingADouble_IsCorrect() {
		val variation1Input  = 43.0
		val variation2Input = 43.1
		val variation3Input = 43.98
		val variation4Input = 43.986

		val variation1Result = Utils.formatAmount(variation1Input)
		val variation2Result = Utils.formatAmount(variation2Input)
		val variation3Result = Utils.formatAmount(variation3Input)
		val variation4Result = Utils.formatAmount(variation4Input)

		Assert.assertEquals("43.00", variation1Result)
		Assert.assertEquals("43.10", variation2Result)
		Assert.assertEquals(variation3Input.toString(), variation3Result)
		Assert.assertEquals("43.99", variation4Result)
	}
}