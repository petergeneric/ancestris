package org.ancestris.trancestris.resources;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceLine.java

public class ResourceLine {

    String propertyKey = null;
    String propertyValue = null;

    ResourceLine() {
    }

    ResourceLine(String key, String value) {
        propertyKey = key;
        propertyValue = value;
    }

    public String getKey() {
        return this.propertyKey;
    }

    public String getValue() {
        return this.propertyValue;
    }
}
