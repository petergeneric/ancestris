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
    private ArrayList<String> content = null;
    private int not_translated;
//    private ResourceDir parent;

    public ResourceFile(File file) throws IOException {
        DefaultLangageFile = file;
        not_translated = 0;
        InputStreamReader inputStream = new InputStreamReader(new FileInputStream(file));
        defaultLangage.load(inputStream);
        content = new ArrayList<String> (defaultLangage.size());
        Iterator it = defaultLangage.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            content.add((String)pairs.getKey()+ "=" + (String)pairs.getValue());
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
            if (translatedLangage.getProperty((String)pairs.getKey()) != null) {
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
        return (String) content.get(i);
    }

    public String getLineTranslation(int i) {
        String s = getLine(i);
        String s1 = ResourceLine.getKey(s);
        String s2 = (String) translatedLangage.get(s1);
        if (s2 != null) {
            return s2;
        } else {
            return ResourceLine.getValue(s);
        }
    }

    public void setLineTranslation(int i, String s) {
        String s1 = getLine(i);
        String s2 = ResourceLine.getKey(s1);
        if (translatedLangage.get(s2) == null) {
            not_translated--;
        }
        translatedLangage.put(s2, s);
    }

    public int getLineState(int i) {
        String s = getLine(i);
        if (s.startsWith("#")) {
            return -1;
        } else {
            return translatedLangage.containsKey(ResourceLine.getKey(s)) ? 1 : 0;
        }
    }

    public File getDefaultBundleFile () {
        return DefaultLangageFile;
    }
}
