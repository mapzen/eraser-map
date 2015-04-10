package com.mapzen.privatemaps;

import org.oscim.map.Map;

public class TestMap extends Map {
    @Override
    public void updateMap(boolean b) {
    }

    @Override
    public void render() {
    }

    @Override
    public boolean post(Runnable runnable) {
        return false;
    }

    @Override
    public boolean postDelayed(Runnable runnable, long l) {
        return false;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
