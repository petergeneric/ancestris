package org.ancestris.trancestris.resources;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceLine.java
import java.io.IOException;
import java.io.PrintStream;

class ResourceLine {

    ResourceLine() {
    }

    static String getKey(String s) {
        if (s.startsWith("#")) {
            return "";
        }
        int i = s.indexOf("=");
        if (i < 0) {
            return "";
        } else {
            return s.substring(0, i).trim();
        }
    }

    static String getValue(String s) {
        int i = s.indexOf("=");
        if (i < 0) {
            return "";
        } else {
            return s.substring(i + 1).trim();
        }
    }

    static void encode(String s, PrintStream printstream)
            throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < ' ' || c >= '\177') {
                printstream.print('\\');
                printstream.print('u');
                printstream.print(hexDigit[c >> 12 & 0xf]);
                printstream.print(hexDigit[c >> 8 & 0xf]);
                printstream.print(hexDigit[c >> 4 & 0xf]);
                printstream.print(hexDigit[c >> 0 & 0xf]);
            } else {
                printstream.print(c);
            }
        }

        printstream.println();
    }

    static String decode(String s) {
        StringBuffer stringbuffer = new StringBuffer(s);
        StringBuffer stringbuffer1 = new StringBuffer(s.length());
        int i = 0;
        char ac[] = new char[4];
        label0:
        while (i < stringbuffer.length()) {
            char c = stringbuffer.charAt(i++);
            if (c != '\\') {
                stringbuffer1.append(c);
                continue;
            }
            c = stringbuffer.charAt(i++);
            if (c != 'u') {
                stringbuffer1.append('\\');
                stringbuffer1.append(c);
                continue;
            }
            int j = 0;
            for (int k = 0; k < 4; k++) {
                char c1 = stringbuffer.charAt(i++);
                ac[k] = c1;
                switch (c1) {
                    case 48: // '0'
                    case 49: // '1'
                    case 50: // '2'
                    case 51: // '3'
                    case 52: // '4'
                    case 53: // '5'
                    case 54: // '6'
                    case 55: // '7'
                    case 56: // '8'
                    case 57: // '9'
                        j = ((j << 4) + c1) - 48;
                        break;

                    case 97: // 'a'
                    case 98: // 'b'
                    case 99: // 'c'
                    case 100: // 'd'
                    case 101: // 'e'
                    case 102: // 'f'
                        j = ((j << 4) + 10 + c1) - 97;
                        break;

                    case 65: // 'A'
                    case 66: // 'B'
                    case 67: // 'C'
                    case 68: // 'D'
                    case 69: // 'E'
                    case 70: // 'F'
                        j = ((j << 4) + 10 + c1) - 65;
                        break;

                    case 58: // ':'
                    case 59: // ';'
                    case 60: // '<'
                    case 61: // '='
                    case 62: // '>'
                    case 63: // '?'
                    case 64: // '@'
                    case 71: // 'G'
                    case 72: // 'H'
                    case 73: // 'I'
                    case 74: // 'J'
                    case 75: // 'K'
                    case 76: // 'L'
                    case 77: // 'M'
                    case 78: // 'N'
                    case 79: // 'O'
                    case 80: // 'P'
                    case 81: // 'Q'
                    case 82: // 'R'
                    case 83: // 'S'
                    case 84: // 'T'
                    case 85: // 'U'
                    case 86: // 'V'
                    case 87: // 'W'
                    case 88: // 'X'
                    case 89: // 'Y'
                    case 90: // 'Z'
                    case 91: // '['
                    case 92: // '\\'
                    case 93: // ']'
                    case 94: // '^'
                    case 95: // '_'
                    case 96: // '`'
                    default:
                        stringbuffer1.append("\\u");
                        for (int l = 0; l <= k; l++) {
                            stringbuffer1.append(ac[l]);
                        }

                        continue label0;
                }
            }

            stringbuffer1.append((char) j);
        }
        return stringbuffer1.toString();
    }
    private static char hexDigit[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
}
