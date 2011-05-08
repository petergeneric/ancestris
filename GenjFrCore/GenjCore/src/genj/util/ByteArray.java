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

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that represents an improved ByteArray
 */
public class ByteArray {

  /** the block size by which information is read */
  private final static int CLUSTER = 1024*4;

  /** an empty byte array */
  private final static byte[] EMPTY = new byte[]{};

  /** the bits */
  private byte[] bits = EMPTY;
  
  private boolean isAllowInterrupts;

  /**
   * Constructor
   */
  public ByteArray(InputStream in) throws InterruptedException, IOException {
    // 20030519 check available 
    this(in, Math.max(in.available(), CLUSTER), false);
  }

  /**
   * Constructor
   */
  public ByteArray(InputStream in, boolean allowInterrupts) throws InterruptedException, IOException {
    // 20030519 check available 
    this(in, Math.max(in.available(), CLUSTER), allowInterrupts);
  }

  /**
   * Constructor
   */
  public ByteArray(InputStream in, int cluster, boolean allowInterrupts) throws InterruptedException, IOException {
    
    isAllowInterrupts = allowInterrupts;

    // Read from stream - if the callee knows the size of the
    // file it might be passed in as 'cluster'. So we increase
    // that by 1 so maximal one cluster is created
    byte buffer[] = new byte[cluster+1];
    int len=0,total=0;

    while (true) {

      // Read !
      len = in.read(buffer,total,buffer.length-total);

      // Interrupted?
      if (isAllowInterrupts&&Thread.currentThread().isInterrupted())
        throw new InterruptedException();

      // End of stream ?
      if (len<0) break;

      // Increment amount read !
      total+=len;
      
      // Did it fit and end ?
      if (total<buffer.length)
        continue;

      // More than fit !
      byte tmp[] = new byte[buffer.length*2];
      System.arraycopy(buffer,0,tmp,0,buffer.length);
      buffer = tmp;

      // Read on !
    }

    // Remember
    bits = new byte[total];
    System.arraycopy(buffer, 0, bits, 0, total);
    buffer = null;
       
    // Done
  }

  /**
   * Accessor for bytes
   */
  public byte[] getBytes() {
    return bits;
  }

} //ByteArray
