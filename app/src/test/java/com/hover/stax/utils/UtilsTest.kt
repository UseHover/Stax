package com.hover.stax.utils

import org.junit.Assert
import org.junit.Test


class UtilsTest {
	@Test
	fun formatAmount_UsingADouble_IsCorrect() {
		val result = Utils.formatAmount(43.98)
		Assert.assertEquals("43.98", result)
	}
}