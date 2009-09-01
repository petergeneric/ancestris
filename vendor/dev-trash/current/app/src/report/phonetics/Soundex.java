package phonetics;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The soundex implementation modified from the com.generationjava.util package
 * to cope with accented characters better
 * com.generationjava.util
 * it was origionally Licensed under the BSD license
 * see http://www.generationjava.com/licencing.shtml
 */
public class Soundex implements Phonetics {
  
    // Soundex substitutions (one character unicode followed by string of substitution)
    private final static String ACCENTS = "ŠS ŽZ šs žz ŸY ÀA ÁA ÂA ÃA ÄA ÅA ÇC ÈE ÉE ÊE ËE ÌI ÍI ÎI ÏI ÑN ÒO ÓO ÔO ÕO ÖO ØO ÙU ÚU ÛU ÜU ÝY àa áa âa ãa äa åa çc èe ée êe ëe ìi íi îi ïi ñn òo óo ôo õo öo øo ùu úu ûu üu ýy µu ÞTH þth ÐDH ðdh ßss ŒOE œoe ÆAE æae";
    static public final char[] US_ENGLISH_SOUNDEX_MAPPING = "01230120022455012623010202".toCharArray();

    private String[] accents;

    private char[] soundexMapping;

    /** constructor */
    public Soundex() {
        this(US_ENGLISH_SOUNDEX_MAPPING);
    }

    /** constructor */
    public Soundex(char[] mapping) {
      // remember soundex mapping
      this.soundexMapping = mapping;
      // done
    }

    /**
     * Substitute an accent (if applicable) with a non-accented character
     * as specified in soundex.accents of ReportPhonetics.properties
     */
    public String substituteAccents(String str) {

      // have we parsed soundex accents yet?
      if (accents==null) {
        Vector buffer = new Vector(256);
        try {
          // loop over soundex accent tokens
          StringTokenizer tokens = new StringTokenizer(ACCENTS);
            while (tokens.hasMoreTokens()) {
              String token = tokens.nextToken();
              int unicode = token.charAt(0);
              String substitute = token.substring(1);
              if (buffer.size()<unicode+1)
                buffer.setSize(unicode+1);
              buffer.set(unicode, substitute);
            }
        } catch (Throwable t) {
        }
        // now we have
        accents = (String[])buffer.toArray(new String[buffer.size()]);
      }

      // gather result
      StringBuffer result = new StringBuffer(str.length() * 2);
      for (int i = 0; i < str.length(); i++) {
          char c = str.charAt(i);
          if (c<accents.length&&accents[c]!=null)
            result.append(accents[c]);
          else
            result.append(c);
      }

      // done
      return result.toString();
    }

    /**
     * Get the SoundEx value of a string.
     * it will return the SoundEx code for the FIRST word in the string
     */
    public String encode(String s) {

      // safety check
      if (s == null || s.length() == 0)
          return null;

        // should get a true code for each acented character
        String str = substituteAccents(s);

        // check the first letter is a character
        if (!Character.isLetter(str.charAt(0)))
            return encode(str.substring(1));

        char out[] = { '0', '0', '0', '0' };
        char last, mapped;
        int incount = 1, count = 1;
        out[0] = Character.toUpperCase(str.charAt(0));
        last = getMappingCode(str.charAt(0));
        while ((incount < str.length()) && (mapped = getMappingCode(str.charAt(incount++))) != 0 && (count < 4)) {
            if ((mapped != '0') && (mapped != last)) {
                out[count++] = mapped;
            }
            last = mapped;
        }
        return new String(out);
    }

    /**
     * Used internally by the SoundEx algorithm.
     */
    private char getMappingCode(char c) {
        if (!Character.isLetter(c)) {
            return '0';
        } else {
            int loc = Character.toUpperCase(c) - 'A';
            if (loc < 0 || loc > (soundexMapping.length - 1))
                return '0';
            return soundexMapping[loc];
        }
    }

    public String toString() {
      return "Soundex";
    }
}