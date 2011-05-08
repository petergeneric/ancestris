/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell.swing;

import java.awt.event.ActionEvent;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * A base Action for the shell
 */
public abstract class Action2 extends AbstractAction implements Runnable {

  /** async modes */
  public static final int 
    ASYNC_NOT_APPLICABLE = 0,
    ASYNC_SAME_INSTANCE  = 1,
    ASYNC_NEW_INSTANCE   = 2;

  /** whether we're async or not */
  private int asynchronous = ASYNC_NOT_APPLICABLE;
  
  /** the thread executing asynchronously */
  private Thread thread;
  private Object threadLock = new Object();

  /** 
   * Constructor 
   */
  protected Action2() {
  }
  
  /** 
   * Constructor 
   */
  protected Action2(String name) {
    super(name);
  }
  
  /**
   * Callback - action needs to perform
   */
  public final void actionPerformed(ActionEvent e) {
    trigger();
  }
  
  /**
   * Callback - action needs to perform
   */
  public void run() {
    trigger();
  }
  
  /**
   * Acessor - async
   */
  protected void setAsync(int set) {
    asynchronous=set;
  }
  
  /**
   * Acessor - async
   */
  protected int getAsync() {
    return asynchronous;
  }
  
  /** 
   * Stops asynchronous execution
   */
  public void cancel(boolean wait) {
    Thread t = getThread();
    if (t!=null&&t.isAlive()) {
      t.interrupt();
      if (wait) try {
        t.join();
      } catch (InterruptedException e) {
      }
    }
  }
  
  /**
   * Triggers the action
   */
  public final Action2 trigger() {
    
    int tasync = getAsync();
    
    // do we have to create a new instance?
    if (tasync==ASYNC_NEW_INSTANCE) {
      try {
        Action2 action = (Action2)clone();
        action.setAsync(ASYNC_SAME_INSTANCE);
        return action.trigger();
      } catch (Throwable t) {
        t.printStackTrace();
        handleThrowable("trigger", new RuntimeException("Couldn't clone instance of "+getClass().getName()+" for ASYNC_NEW_INSTANCE"));
      }
      return this;
    }
    
    // pre
    boolean preExecuteOk;
    try {
      preExecuteOk = preExecute();
    } catch (Throwable t) {
      handleThrowable("preExecute",t);
      preExecuteOk = false;
    }
    
    // execute
    if (preExecuteOk) try {
      if (tasync!=ASYNC_NOT_APPLICABLE) {
        synchronized (threadLock) {
          getThread().start();
        }
      }
      else execute();
    } catch (Throwable t) {
      handleThrowable("execute(sync)", t);
    }
    
    // post
    if ((tasync==ASYNC_NOT_APPLICABLE)||(!preExecuteOk)) try {
      postExecute();
    } catch (Throwable t) {
      handleThrowable("postExecute", t);
    }
    
    // done
    return this;
    
  }
  
  /**
   * The thread running this asynchronously
   * @return thread or null
   */
  public Thread getThread() {
    if (getAsync()!=ASYNC_SAME_INSTANCE) return null;
    synchronized (threadLock) {
      if (thread==null) thread=new Thread(new CallAsyncExecute());
      return thread;
    }
  }
  
  /**
   * Sets the name of the action
   */
  public void setName(String name) {
    super.putValue(NAME, name);
  }
  
  public String getName() {
    return (String)super.getValue(NAME);
  }
  
  /**
   * Sets the icon of the action
   */
  public void setIcon(Icon icon) {
    super.putValue(SMALL_ICON, icon);
  }
  
  /**
   * Whether this Action is selected or not - override if
   * Action is toggleable
   */
  public boolean isSelected() {
    return false;    
  }
  
  /**
   * Implementor's functionality (always sync to EDT)
   */
  protected boolean preExecute() throws Exception {
    // Default 'yes, continue'
    return true;
  }
  
  /**
   * Run to be implemented
   */
  protected abstract void execute() throws Exception;

  /**
   * Implementor's functionality (always sync to EDT)
   */
  protected void postExecute() throws Exception {
    // Default NOOP
  }
  
  /** 
   * Handle an uncaught throwable
   */
  protected void handleThrowable(String phase, Throwable t) {
    // show stacktrace
    //t.printStackTrace(System.err);
    CharArrayWriter ca = new CharArrayWriter(256);
    t.printStackTrace(new PrintWriter(ca));
    // open dialog
    SwingHelper.showDialog(
      null, 
      "Sorry", 
      new JScrollPane(new JTextArea(ca.toString(),8,32)), 
      SwingHelper.DLG_OK);
  }

  /**
   * Async Execution
   */
  private class CallAsyncExecute implements Runnable {
    public void run() {
      try {
        execute();
      } catch (Throwable t) {
        SwingUtilities.invokeLater(new CallSyncHandleThrowable(t));
      }
      synchronized (threadLock) {
        thread=null;
      }
      SwingUtilities.invokeLater(new CallSyncPostExecute());
    }
  } //AsyncExecute
  
  /**
   * Sync (EDT) handle throwable
   */
  private class CallSyncHandleThrowable implements Runnable {
    private Throwable t;
    protected CallSyncHandleThrowable(Throwable set) {
      t=set;
    }
    public void run() {
      // an async throwable we're going to handle now?
      try {
        handleThrowable("execute(async)",t);
      } catch (Throwable t) {
      }
    }
  }

  /**
   * Sync (EDT) Post Execute
   */
  private class CallSyncPostExecute implements Runnable {
    public void run() {
      // postExecute        
      try {
        postExecute();
      } catch (Throwable t) {
        handleThrowable("postExecute", t);
      }
    }
  } //SyncPostExecute


}
