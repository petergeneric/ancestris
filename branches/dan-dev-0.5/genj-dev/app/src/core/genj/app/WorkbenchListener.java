/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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
package genj.app;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Trackable;
import genj.view.View;

/**
 * Workbench callbacks
 */
public interface WorkbenchListener {

  /**
   * notification that selection has changed
   * @param context the new selection
   * @param isActionPerformed whether to perform action (normally double-click)
   */
  public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed);
  
  /**
   * notificaton that a long running process has started
   * @param workbench
   * @param process
   */
  public void processStarted(Workbench workbench, Trackable process);

  /**
   * notificaton that a long running process has finished
   * @param workbench
   * @param process
   */
  public void processStopped(Workbench workbench, Trackable process);

  /**
   * notification that commit of edits is requested
   */
    public void commitRequested(Workbench workbench, Context context);
  
  /** 
   * notification that workbench is closing
   * @return whether to veto the close operation (false=continue)
   */
  public void workbenchClosing(Workbench workbench);
  
  /** 
   * notification that gedcom was closed
   * @return whether to continue with close operation or not
   */
  public void gedcomClosed(Workbench workbench, Gedcom gedcom);
  
  /** 
   * notification that gedcom was opened
   * @return whether to continue with close operation or not
   */
  public void gedcomOpened(Workbench workbench, Gedcom gedcom);
  
  /**
   * notification that a view has been opened
   */
  public void viewOpened(Workbench workbench, View view);

  /**
   * notification that a view has been opened
   */
  public void viewClosed(Workbench workbench, View view);

}
