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

import genj.util.Base64;

import java.io.IOException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DES based Enigma implementation which requires JDK 1.4
 */
/*package*/ class EnigmaImpl extends Enigma {

  private final static Logger LOG = Logger.getLogger("genj.crypto");
  
  /** the SecretKey we're using */
  private SecretKey key;
  
  /** the cipher we're using */
  private Cipher cipher;

  /** salt padding */
  private final static String SALT_PADDING = "GENEALOGYJ"; 
  
  /** algorithm used */
  private final static String ALGORITHM = "DES";

  /**
   * Initializer
   */
  protected Enigma init(String password) {
    
    try {
    
      // generate salt - byte sequence'd password (minimum length 8 for DES)
      byte[] salt = (password+SALT_PADDING).getBytes("UTF-8");

      // generate a keyspec     
      KeySpec keyspec = new DESKeySpec(salt);

      // generate cipher      
      cipher = Cipher.getInstance(ALGORITHM);
  
      // generate key
      key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keyspec);
      
    } catch (Throwable t) {
      
      LOG.log(Level.WARNING, "Couldn't initialize Enigma", t); 
      return null;
    }
  
    return this;
  }

  /**
   * @return byte_2_base64(byte-encrypt(java_2_utf-8-bytes(value)))
   */
  protected String encryptImpl(String value) throws IOException {
    
    try {
    
      // convert java into utf-8 bytes
      byte[] utf8bytes = value.getBytes("UTF-8");
      
      // DES encryt
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] desbytes = cipher.doFinal(utf8bytes); 
      
      // convert into base64
      String base64 = Base64.encode(desbytes).toString();
      
      // done
      return base64;
      
    } catch (Throwable t) {
      // not really expecting any exceptions to be thrown
      throw new IOException("Encrypt failed : "+t+'/'+t.getMessage());
    }
    
  }

  /**
   * @return utf-8-bytes_2_java(byte-decrypt(base64_2_byte(value)))
   */
  protected String decryptImpl(String value) throws IOException {
  
    try {
    
      // convert base64 into des bytes
      byte[] desbytes = Base64.decode(value);
      
      // DES decrypt
      cipher.init(Cipher.DECRYPT_MODE, key);
      byte[] utf8bytes = cipher.doFinal(desbytes);
      
      // convert utf-8 bytes into java string
      String javaString = new String(utf8bytes, "UTF-8");
      
      // done
      return javaString;

    } catch (Throwable t) {
      throw new IOException("Decrypt failed : "+t.getMessage());
    }
  }

} //EnigmaImpl
