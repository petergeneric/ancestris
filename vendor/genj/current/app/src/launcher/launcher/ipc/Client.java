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
import java.net.Socket;

/**
 * A client for performing inter process calls to a server
 */
public class Client {
  
  /** our default timeout */
  private int timeout = 500;
  
  /** our default terminator */
  private String[] terminators = IPCHelper.TERMINATORS;
  
  /** buffer overrun protection */
  private int maxBuffer = 256;
  
  /**
   * Send a string message
   */
  public String send(int port, String msg) throws IOException {
    
    Socket remote = new Socket((String)null, port);
    try {
      remote.setSoTimeout(timeout);
      IPCHelper.write(remote.getOutputStream(), msg);
      return IPCHelper.read(remote.getInputStream(), terminators, 16);
    } finally {
      remote.close();
    }
    
  }

}
