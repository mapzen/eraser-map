package com.ecgreb.robogradleplugin;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MainActivityTest {
    private MainActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = new MainActivity();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertNotNull(activity);
    }
}
