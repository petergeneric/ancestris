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

import genj.gedcom.Context;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * A baseclass for all our views
 */
public abstract class View extends JPanel implements SelectionListener {

  /**
   * Constructor
   */
  public View() {
    super(new BorderLayout()); 
    setMinimumSize(new Dimension());
  }
  
  /**
   * Constructor
   */
  public View(LayoutManager lm) {
    super(lm);
  }
  
  @Override
  public Component add(Component comp) {
    // first w/border layout goes into center
    if (getLayout() instanceof BorderLayout && getComponentCount()==0)
      super.add(comp, BorderLayout.CENTER);
    else
      super.add(comp);
    
    return comp;
  }
  
  /**
   * commit any outstanding changes
   */
  public void commit() {
  }

  /**
   * prepare to close
   */
  public void closing() {
  }

  /**
   * Find the view for given component
   */
  public static View getView(Component componentInView) {
    do {
      if (componentInView instanceof View)
        return (View)componentInView;
      
      if (componentInView instanceof JPopupMenu)
        componentInView = ((JPopupMenu)componentInView).getInvoker();
      else if (componentInView instanceof Window)
        componentInView = ((Window)componentInView).getOwner();
      else
        componentInView = componentInView.getParent();
    } while (componentInView!=null);
    
    throw new IllegalArgumentException("Cannot find view for component");
  }

  /**
   * set current context
   */
  public void setContext(Context context, boolean isActionPerformed) {
    // noop
  }

  /**
   * populate a toolbar
   */
  public void populate(ToolBar toolbar) {
    // noop
  }
  
}
