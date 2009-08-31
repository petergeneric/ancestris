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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * A server for receiving inter process calls from clients
 */
public class Server {
  
  private final static Logger LOG = Logger.getLogger("genj.ipc");

  /** buffer overrun protection */
  private int maxBuffer = 256;
  
  /** our socket */
  private ServerSocket local;
  
  /** our handler */
  private CallHandler handler;
  
  /** our default timeout */
  private int timeout = 200;
  
  /** our default terminator */
  private String[] terminators = IPCHelper.TERMINATORS;
  
  /** our thread */
  private Thread thread;
  
  /**
   * Constructor
   */
  public Server(int port, CallHandler handler) throws IOException {
    
    this.handler = handler;
    
    // setup server socket
    local = new ServerSocket(port);
    
    // setup async listening
    thread = new Thread(new Runnable() {
      public void run() {
        while (true) try {
          read();
        } catch (Throwable t) {
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
    
    // done
  }
  
  /**
   * Read  request - asynchronous
   */
  private void read() throws IOException {
    // block for remote socket
    Socket remote = local.accept();
    // read
    try {
      
      // make sure we're not waiting too long
      remote.setSoTimeout(timeout);
      
      // read msg until end of message (=0) is received
      String in = IPCHelper.read(remote.getInputStream(), terminators, maxBuffer);
      
      // pass to handler
      String out = handler.handleCall(in);
      
      // write msg back
      IPCHelper.write(remote.getOutputStream(), out+terminators[0]);
      
    } catch (IOException e){
      LOG.info(e.getMessage()+" ("+remote.getRemoteSocketAddress()+")");
    } finally {
      remote.close();
    }
    // done
  }

}
