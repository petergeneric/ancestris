package org.ancestris.trancestris.resources;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceFile.java
import java.io.*;
import java.util.*;

public class ResourceFile {

    static final int COMMENT = -1;
    static final int TRANSLATE = 0;
    static final int TRANSLATED = 1;
    private File DefaultLangageFile = null;
    private Properties defaultLangage = new Properties();
    private Properties translatedLangage = new Properties();
    private ArrayList<ResourceLine> content = null;
    private int not_translated;

    public ResourceFile(File file) throws IOException {
        DefaultLangageFile = file;
        not_translated = 0;
        InputStreamReader inputStream = new InputStreamReader(new FileInputStream(file));
        defaultLangage.load(inputStream);
        content = new ArrayList<ResourceLine>(defaultLangage.size());
        Iterator it = defaultLangage.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            content.add(new ResourceLine((String) pairs.getKey(), (String) pairs.getValue()));
        }
        not_translated = defaultLangage.size();
    }

    public void writeTo(File file, boolean flag) throws IOException {
        PrintStream outputStream = new PrintStream(file, "UTF8");
        translatedLangage.store(outputStream, null);
        outputStream.close();
    }

    public void setTranslation(File file) throws IOException {
        not_translated = 0;
        InputStreamReader inputStream = new InputStreamReader(new FileInputStream(file));
        translatedLangage.load(inputStream);

        Iterator it = defaultLangage.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if (translatedLangage.getProperty((String) pairs.getKey()) != null) {
                not_translated = Math.max(0, not_translated - 1);
            }
        }
    }

    public boolean isTranslated() {
        return not_translated == 0;
    }

    public int getLineCount() {
        return defaultLangage.size();
    }

    public String getLine(int i) {
        return (String) content.get(i).propertyValue;
    }

    public String getLineTranslation(int i) {
        String key = content.get(i).getKey();
        String s2 = (String) translatedLangage.get(key);
        if (s2 != null) {
            return s2;
        } else {
            return content.get(i).getValue();
        }
    }

    public void setLineTranslation(int i, String s) {
        String key = content.get(i).getKey();
        if (translatedLangage.get(key) == null) {
            not_translated--;
        }
        translatedLangage.put(key, s);
    }

    public int getLineState(int i) {

        if (translatedLangage.containsKey(content.get(i).getKey()) == true) {
            String Origin = defaultLangage.getProperty(content.get(i).getKey());

            if (Origin.equalsIgnoreCase(translatedLangage.getProperty(content.get(i).getKey()))) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    public File getDefaultBundleFile() {
        return DefaultLangageFile;
    }
}
