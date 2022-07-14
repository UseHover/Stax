package com.hover.stax.channels

import org.junit.Assert
import org.junit.Test

class ChannelTest {
	@Test
	fun getHniList() {
		val hnis = """ ["1","2", "3"]"""
		val channel = Channel()
		channel.hniList = hnis
		Assert.assertEquals(channel.getHniList().size, 3)
	}
}