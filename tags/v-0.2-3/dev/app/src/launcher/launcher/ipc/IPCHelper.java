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
package launcher.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * A helper type for handling buffered data exchange between IPC clients and servers
 */
/*package*/ class IPCHelper {
  
  /*package*/ final static String[] TERMINATORS = { "\n\n", "\r\n\r\n" };
  
  /*package*/ static String read(InputStream stream, String[] terminators, int max) throws IOException {
    
    // read msg until end of message (=0) is received
    Reader in = new InputStreamReader(stream);
    char[] buffer = new char[max];
    int msglen = 0;
    loop: while (true) {
      // fill our buffer
      int read = in.read(buffer, msglen, buffer.length-msglen);
      if (read<0) break;
      msglen += read;
      // is our buffer full?
      if (msglen==buffer.length)
        break;
      // end marker?
      String snapshot = new String(buffer, 0, msglen);
      for (int i = 0; i < terminators.length; i++) {
        if (snapshot.endsWith(terminators[i])) {
          msglen -= terminators[i].length();
          break loop;
        }
      }
      // continue reading
    }
    
    // done
    return new String(buffer, 0, msglen);
  }

  /*package*/ static void write(OutputStream stream, String string) throws IOException {
    Writer out = new OutputStreamWriter(stream, Charset.forName("utf8"));
    out.write(string);
    out.flush();
  }

}
