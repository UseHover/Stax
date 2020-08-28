package com.hover.stax;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
@Test
public void addition_isCorrect() {
	assertEquals(4, 2 + 2);
}

@Test
public void test_encryption() {
	String value = "1234";
	BlowfishEncyption blowfishEncyption = new BlowfishEncyption();
	byte[] pin = blowfishEncyption.encrypt(value);

	Assert.assertEquals(value, blowfishEncyption.decrypt(pin));
}
}