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
package genj.window;

import java.awt.Component;

/**
 * An event broadcasted within the window managers known components
 */
public abstract class WindowBroadcastEvent {

  private Component source;
  private boolean isOutbound = true;
  
  /**
   * constructor
   */
  protected WindowBroadcastEvent(Component source) {
    this.source = source;
  }
  
  /**
   * the originating source (if any)
   */
  public Component getSource() {
    return source;
  }
  
  /**
   * Whether this event is flowing from a sub-component up its containment hierarchy
   */
  public boolean isOutbound() {
    return isOutbound;
  }
  
  /**
   * Whether this event is flowing a container into its contained sub-components
   */
  public boolean isInbound() {
    return !isOutbound;
  }
  
  /**
   * Set to outbound
   */
  /*package*/ void setInbound() {
    isOutbound = false;
  }

  /**
   * Mark event as broadcasted
   */
  protected void setBroadcasted() {
    // noop
  }
  
}
