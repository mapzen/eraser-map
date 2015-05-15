package com.mapzen.erasermap.dummy;

import org.jetbrains.annotations.NotNull;

import android.util.AttributeSet;

/**
 * Stub implementation of {@link AttributeSet} for testing custom views.
 */
public class TestAttributeSet implements AttributeSet {
    @Override public int getAttributeCount() {
        return 0;
    }

    @NotNull @Override public String getAttributeName(int index) {
        return null;
    }

    @Override public String getAttributeValue(int index) {
        return null;
    }

    @Override public String getAttributeValue(String namespace, String name) {
        return null;
    }

    @NotNull @Override public String getPositionDescription() {
        return null;
    }

    @Override public int getAttributeNameResource(int index) {
        return 0;
    }

    @Override public int getAttributeListValue(String namespace, String attribute, String[] options,
            int defaultValue) {
        return 0;
    }

    @Override public boolean getAttributeBooleanValue(String namespace, String attribute,
            boolean defaultValue) {
        return false;
    }

    @Override public int getAttributeResourceValue(String namespace, String attribute,
            int defaultValue) {
        return 0;
    }

    @Override public int getAttributeIntValue(String namespace, String attribute,
            int defaultValue) {
        return 0;
    }

    @Override public int getAttributeUnsignedIntValue(String namespace, String attribute,
            int defaultValue) {
        return 0;
    }

    @Override public float getAttributeFloatValue(String namespace, String attribute,
            float defaultValue) {
        return 0;
    }

    @Override public int getAttributeListValue(int index, String[] options, int defaultValue) {
        return 0;
    }

    @Override public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        return false;
    }

    @Override public int getAttributeResourceValue(int index, int defaultValue) {
        return 0;
    }

    @Override public int getAttributeIntValue(int index, int defaultValue) {
        return 0;
    }

    @Override public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return 0;
    }

    @Override public float getAttributeFloatValue(int index, float defaultValue) {
        return 0;
    }

    @Override public String getIdAttribute() {
        return null;
    }

    @Override public String getClassAttribute() {
        return null;
    }

    @Override public int getIdAttributeResourceValue(int defaultValue) {
        return 0;
    }

    @Override public int getStyleAttribute() {
        return 0;
    }
}
