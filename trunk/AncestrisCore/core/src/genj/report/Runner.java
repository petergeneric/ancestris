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
package genj.report;

import genj.gedcom.Gedcom;
import genj.gedcom.UnitOfWork;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A runner for reports
 */
/*package*/ class Runner implements Runnable {
  
  private final static Logger LOG = Logger.getLogger("genj.report");

  private final static long FLUSH_WAIT = 500;

  private Gedcom gedcom;
  private Object context;
  private Report report;
  private Callback callback;
  private Object result;
  
  /**
   * Constructor
   * @param gedcom Gedcom the report works on
   * @param context Context the report getsas input
   * @param report Report to run
   * @param callback Appendable or Closeable
   */
  /*package*/ Runner(Gedcom gedcom, Object context, Report report, Callback callback) {
    this.gedcom = gedcom;
    this.context= context;
    this.report = report;
    this.callback = callback;
  }
  
  public void run() {
    
    // set report context
    report.setOut(new PrintWriter(new WriterImpl()));
    
    // run
    try{
      if (report.isReadOnly()) {
        result = report.start(context);
      } else {
        final Object finalContext = context;
        gedcom.doUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) {
            try {
              result = report.start(finalContext);
            } catch (Throwable t) {
              throw new RuntimeException(t);
            }
          }
        });
      }    
    } catch (Throwable t) {
      if (t.getCause()!=null)
        t = t.getCause();
      result = t;
    } finally {
      // flush
      report.flush();
      report.getOut().close();
    }
    
    // signal done
    callback.handleResult(report, result);

  }
  
  /**
   * A printwriter that directs output to listener
   */
  private class WriterImpl extends Writer {

    /** buffer */
    private StringBuffer buffer = new StringBuffer(4*1024);

    /** timer */
    private long lastFlush = -1;
    
    /**
     * @see java.io.Writer#close()
     */
    public void close() {
      flush();
      LOG.log(Level.FINER, "close");
    }

    /**
     * @see java.io.Writer#flush()
     */
    public void flush() {

      // something to flush?
      if (buffer.length()==0)
        return;

      // mark
      lastFlush = System.currentTimeMillis();

      // dump buffer
      callback.handleOutput(report, buffer.toString());
        
      // clear it
      buffer.setLength(0);
      
      // done
    }

    
    /**
     * @see java.io.Writer#write(char[], int, int)
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
      // append to buffer - strip any \r from \r\n
      for (int i=0;i<len;i++) {
        char c = cbuf[off+i];
        if (c!='\r') buffer.append(c);
      }
      // check flush
      if (System.currentTimeMillis()-lastFlush > FLUSH_WAIT)
        flush();
      // done
    }

  } //OutputWriter
  
  /**
   * a runner callback
   */
  public interface Callback {
    
    public void handleOutput(Report report, String output);
    
    public void handleResult(Report report, Object result);
    
  }
}
