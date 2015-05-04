package com.mapzen.privatemaps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MainPresenterTest {
    private MainPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new MainPresenterImpl();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(presenter).isNotNull();
    }
}
