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
package genj.crypto;

import java.io.IOException;

/**
 * The abstract base type for en/decrypting Gedcom information 
 */
public abstract class Enigma {
  
  /** the implementation class we use */
  private static final String IMPL = "genj.crypto.EnigmaImpl";
  
  /** encryption prefix */
  private final static String PREFIX = "[private]";
  
  /** availability */
  private static boolean isAvailable = getInstance("") != null;
  
  /**
   * Get access to an Enigma instance
   * @return instance of Enigma or null if no de/encryption implementation available 
   */
  public static Enigma getInstance(String password) {
    
    try {
      return ((Enigma)Class.forName(IMPL).newInstance()).init(password);
    } catch (Throwable t) {
      return null;
    }
    
  }
  
  /**
   * Availability test
   */
  public static boolean isAvailable() {
    return isAvailable;
  }
  
  /**
   * try to guess whether something is encrypted
   */
  public static boolean isEncrypted(String value) {
    return value.startsWith(PREFIX);
  }
  
  /**
   * encrypt a value
   * 
   *  "Nils" > "DES|MdwGEBqRFOM="
   * 
   * @param value the plain data as Java string to encrypt
   * @return the encrypted value
   */  
  public String encrypt(String value) throws IOException {
    return PREFIX+encryptImpl(value);
  }

  /**
   * decrypt
   * 
   *  "DES|MdwGEBqRFOM=" > "Nils"
   * 
   * @param value the encrypted data as Java string to decrypt 
   * @return the decrypted value
   */  
  public String decrypt(String value) throws IOException {
    if (!isEncrypted(value))
      throw new IOException("Not an encrypted value");
    return decryptImpl(value.substring(PREFIX.length()));
  }

  /**
   * implementation contract - initialization
   */
  protected abstract Enigma init(String password);
  
  /**
   * implementation contract - encrypt
   */
  protected abstract String encryptImpl(String value) throws IOException;
  
  /**
   * implementation contract - decrypt
   */
  protected abstract String decryptImpl(String value) throws IOException;
  
} //Enigma
