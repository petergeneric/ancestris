/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.util;

/**
 * Class for en/decoding Base64
 */
public class Base64 {

  /**
   * Decode a Base64 char to int-value
   */
  public static byte decode(char c) throws IllegalArgumentException {
    if (c >= 'A' && c <= 'Z') {
      return (byte) (c - 'A');
    }
    if (c >= 'a' && c <= 'z') {
      return (byte) (c - 'a' + 26);
    }
    if (c >= '0' && c <= '9') {
      return (byte) (c - '0' +52);
    }
    if (c == '+') {
      return (byte) 62;
    }
    if (c == '/') {
      return (byte) 63;
    }
    if (c == '=') {
      return (byte) 0;
    }
    throw new IllegalArgumentException("Illegal Base64 byte ("+c+")");
  }

  /**
   * Decodes base64 String to byte data
   */
  public static byte[] decode(String in) throws IllegalArgumentException {

    if ( ( (in.length() % 4) != 0) || (in.length()==0) ) {
      throw new IllegalArgumentException("Illegal Base64 String");
    }

    // Calculate pad and length
    int pad = 0;
    for (int i=in.length()-1; in.charAt(i)=='='; i--)
      pad++;
    int len = in.length() * 3 / 4 - pad;

    // Transform to byte
    byte[] raw = new byte[len];
    int rawIndex = 0;

    for (int i=0; i<in.length(); i+=4) {

      // get next 4 values
      int block =
      (Base64.decode(in.charAt(i + 0)) << 18) +
      (Base64.decode(in.charAt(i + 1)) << 12) +
      (Base64.decode(in.charAt(i + 2)) <<  6) +
      (Base64.decode(in.charAt(i + 3))      ) ;

      // build next 3 raw
      for (int j=0; j<3 && rawIndex+j < raw.length; j++)
      raw[ rawIndex+j ] = (byte) ((block >> (8 * (2-j))) & 0xff);

      rawIndex +=3;
    }

    // Done
    return raw;
  }

  /**
   * Encodes raw bytes to base64 String
   */
  public static String encode(byte[] raw) {

    StringBuffer encoded = new StringBuffer( (raw.length+1)*4/3 );
    for (int i=0; i<raw.length; i+=3) {
      encoded.append(encodeBlock(raw,i));
    }

    return encoded.toString();
  }

  /**
   * Encode an (six bit) int-value to Base64 char
   */
  public static char encode(int i) {
    if (i >= 0 && i <= 25) {
      return (char)( 'A' + i );
    }
    if (i >= 26 && i <= 51) {
      return (char)( 'a' + (i - 26) );
    }
    if (i >= 52 && i <=61 ) {
      return (char)( '0' + (i - 52) );
    }
    if (i == 62) {
      return (char)( '+' );
    }
    if (i == 63) {
      return (char)( '/' );
    }
    return (char)( '?' );
  }

  /**
   * Encodes raw bytes block to base64 String
   */
  private static char[] encodeBlock(byte[] raw, int offset) {

    // Investigate parms
    int block = 0;
    int left  = raw.length - offset -1;
    int bsize = (left>=2) ? 2 : left;

    // Get block 3
    for (int i=0; i<=bsize; i++) {
      byte b = raw[offset+i];
      block += (b<0?b+256:b) << (8* (2-i));
    }

    // Build base64 4
    char[] base64 = new char[4];
    for (int i=0; i<4; i++) {
      int sixbit = (block >>> (6 * (3-i))) & 0x3f ;
      base64[i] = encode(sixbit);
    }

    if (left<1) {
      base64[2] = '=';
    }
    if (left<2) {
      base64[3] = '=';
    }

    return base64;
  }

}
