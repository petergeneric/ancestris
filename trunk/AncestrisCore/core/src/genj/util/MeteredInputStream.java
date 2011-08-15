/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2006 Nils Meier <nils@meiers.net>
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
 * An input stream that counts the bytes read
 */
public class MeteredInputStream extends InputStream {

  private long meter = 0;
  private long marked = -1;
  private InputStream in;

  public MeteredInputStream(InputStream in) {
    this.in = in;
  }

  public long getCount() {
    return meter;
  }

  public int available() throws IOException {
    return in.available();
  }

  public void close() throws IOException {
    in.close();
  }

  public synchronized void mark(int readlimit) {
    in.mark(readlimit);
    marked = meter;
  }

  public boolean markSupported() {
    return in.markSupported();
  }

  public int read() throws IOException {
    meter++;
    return in.read();
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int read = in.read(b, off, len);
    meter+=read;
    return read;
  }

  public int read(byte[] b) throws IOException {
    int read = in.read(b);
    meter+=read;
    return read;
  }

  public synchronized void reset() throws IOException {
    if (marked<0)
      throw new IOException("reset() without mark()");
    in.reset();
    meter = marked;
  }

  public long skip(long n) throws IOException {
    int skipped = (int)super.skip(n);
    meter+=skipped;
    return skipped;
  }

}