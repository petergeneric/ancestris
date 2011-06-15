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
    private Vector content;
    private HashMap<String, String> translation;
    private int not_translated;
//    private ResourceDir parent;

    public ResourceFile(File file, boolean flag) throws IOException {
        not_translated = 0;
        content = new Vector(100);
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        for (String s = null; (s = bufferedreader.readLine()) != null;) {
            s = s.trim();
            if (flag) {
                s = ResourceLine.decode(s);
            }
            if (s.length() != 0) {
                content.addElement(s);
                if (ResourceLine.getKey(s).length() > 0) {
                    not_translated++;
                }
            }
        }
        translation = new HashMap();
    }

    public void writeTo(File file, boolean flag) throws IOException {
        PrintStream outputStream = new PrintStream(file, "UTF8");

        Iterator iterator = translation.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();;
            String value = (String) translation.get(key);
            if (flag) {
                ResourceLine.encode(key + "  = " + value, outputStream);
            } else {
                outputStream.println(key + "  = " + value);
            }
        }

        outputStream.close();
    }

    public void setTranslation(File file, boolean flag) throws IOException {
        HashMap hashtable = new HashMap();
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        for (String s = null; (s = bufferedreader.readLine()) != null;) {
            if (s.length() != 0) {
                if (flag) {
                    s = ResourceLine.decode(s);
                }
                hashtable.put(ResourceLine.getKey(s), ResourceLine.getValue(s));
            }
        }

        for (Enumeration enumeration = content.elements(); enumeration.hasMoreElements();) {
            String s1 = (String) enumeration.nextElement();
            String s2 = ResourceLine.getKey(s1);
            if (s2.length() != 0) {
                String s3 = (String) hashtable.get(s2);
                if (s3 != null) {
                    translation.put(s2, s3);
                    not_translated = Math.max(0, not_translated - 1);
                }
            }
        }

    }

    public boolean isTranslated() {
        return not_translated == 0;
    }

    public int getLineCount() {
        return content.size();
    }

    public String getLine(int i) {
        return (String) content.elementAt(i);
    }

    public String getLineTranslation(int i) {
        String s = getLine(i);
        String s1 = ResourceLine.getKey(s);
        String s2 = (String) translation.get(s1);
        if (s2 != null) {
            return s2;
        } else {
            return ResourceLine.getValue(s);
        }
    }

    public void setLineTranslation(int i, String s) {
        String s1 = getLine(i);
        String s2 = ResourceLine.getKey(s1);
        if (translation.get(s2) == null) {
            not_translated--;
        }
        translation.put(s2, s);
    }

    public int getLineState(int i) {
        String s = getLine(i);
        if (s.startsWith("#")) {
            return -1;
        } else {
            return translation.containsKey(ResourceLine.getKey(s)) ? 1 : 0;
        }
    }
}
