/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genj.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * portion of code from org.apache.common.StringEscapeUtils
 * @author daniel
 */
public class AncestrisUtils {
        public static String unescape(String str) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length());
            unescape(writer, str);
            return writer.toString();
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            throw new UnknownError();
        }
    }

    private static void unescape(Writer out, String str) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (str == null) {
            return;
        }
        int sz = str.length();
        StringBuffer unicode = new StringBuffer(4);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        out.write((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException("Unable to parse unicode value: " + unicode, nfe);
                    }
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
//                        case '\\':
//                            out.write('\\');
//                            break;
//                        case '\'':
//                            out.write('\'');
//                            break;
//                        case '\"':
//                            out.write('"');
//                            break;
//                        case 'r':
//                            out.write('\r');
//                            break;
//                        case 'f':
//                            out.write('\f');
//                            break;
//                        case 't':
//                            out.write('\t');
//                            break;
                        case 'n':
                            out.write('\n');
                            break;
//                        case 'b':
//                            out.write('\b');
//                            break;
                    case 'u': {
                        // uh-oh, we're in unicode country....
                        inUnicode = true;
                        break;
                    }
                    default:
                        out.write(ch);
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.write(ch);
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.write('\\');
        }
    }
}
