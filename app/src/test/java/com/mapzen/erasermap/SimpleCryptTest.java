package com.mapzen.erasermap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SimpleCryptTest {
    @Test
    public void shouldBeSymmetric() throws Exception {
        SimpleCrypt simpleCrypt = SimpleCrypt.withSpecialSalt("for-testing");
        String expected = "hello this is a regular string";
        String encoded = simpleCrypt.encode(expected);
        assertThat(simpleCrypt.decode(encoded)).isEqualTo(expected);
    }
}
