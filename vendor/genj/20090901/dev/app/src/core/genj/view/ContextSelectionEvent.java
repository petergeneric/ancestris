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
package genj.view;

import genj.gedcom.Gedcom;
import genj.window.WindowBroadcastEvent;

import java.awt.Component;

/**
 * A class wrapping the event of a context selection 
 */
public class ContextSelectionEvent extends WindowBroadcastEvent {
  
  private static ViewContext lastContext = null;
  private ViewContext context;
  private boolean isActionPerformed = false;
  
  /**
   * Constructor
   */
  public ContextSelectionEvent(ViewContext context, Component source) {
    super(source);
    this.context = context;
  }
  
  /**
   * Constructor
   */
  public ContextSelectionEvent(ViewContext context, Component source, boolean isActionPerformed) {
    this(context, source);
    this.isActionPerformed = isActionPerformed;
  }
  
  /**
   * last selection context
   * @return null or last selection context
   */
  public static ViewContext getLastBroadcastedSelection() {
    return lastContext;
  }
  
  @Override
  protected void setBroadcasted() {
    lastContext = context;
  }
  
  
  /**
   * Read-Only Accessor
   */
  public ViewContext getContext() {
    return context;
  }

  /**
   * Read-Only Accessor
   */
  public boolean isActionPerformed() {
    return isActionPerformed;
  }
  
  /**
   * auto converter and check
   */
  public static ContextSelectionEvent narrow(WindowBroadcastEvent event, Gedcom gedcom) {
    ContextSelectionEvent cse = narrow(event);
    return cse==null || cse.getContext().getGedcom()!=gedcom ? null : cse;
  }

  /**
   * auto converter and check
   */
  public static ContextSelectionEvent narrow(WindowBroadcastEvent event) {
    return event instanceof ContextSelectionEvent ? (ContextSelectionEvent)event : null;
  }
}
