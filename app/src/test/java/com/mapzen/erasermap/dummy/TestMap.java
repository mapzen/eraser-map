package com.mapzen.erasermap.dummy;

import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.map.Animator;
import org.oscim.map.Map;

public class TestMap extends Map {
    private Animator animator = new TestAnimator(this);
    private boolean updated = false;

    @Override
    public void updateMap(boolean b) {
        updated = b;
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

    @Override
    public Animator animator() {
        return animator;
    }

    public boolean isUpdated() {
        return updated;
    }

    public static class TestAnimator extends Animator {
        private static GeoPoint geoPoint;

        public TestAnimator(Map map) {
            super(map);
        }

        @Override
        public synchronized void animateTo(GeoPoint geoPoint) {
            TestAnimator.geoPoint = geoPoint;
        }

        @Override
        public synchronized void animateTo(long duration, MapPosition mapPosition) {
            TestAnimator.geoPoint = mapPosition.getGeoPoint();
        }

        public static GeoPoint getLastGeoPoint() {
            return geoPoint;
        }

        public static void clearLastGeoPoint() {
            geoPoint = null;
        }
    }
}
