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
package genj.util.swing;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * A button that opens a context-menu on press
 */
public class PopupWidget extends JButton {
  
  /** list of actions */
  private List<JComponent> items = new ArrayList<JComponent>();

  /** whether we fire the first of the available actions on popup click within ms */
  private long fireOnClickWithin = 0;
  
  /** current popup */
  private JPopupMenu popup;
    
  /**
   * Constructor  
   */
  public PopupWidget() {
    this((Icon)null);
  }

  /**
   * Constructor  
   */
  public PopupWidget(Icon icon) {
    this(null, icon);
  }

  /**
   * Constructor  
   */
  public PopupWidget(String text) {
    this(text, null);
  }

  /**
   * Constructor  
   */
  public PopupWidget(String text, Icon icon) {
    // delegate
    super(text, icon);
    // our own model
    setModel(new Model());
    // make non-focusable
    setFocusable(false);
    // small guy
    setMargin(new Insets(2,2,2,2));
    // popup
    popup = new JPopupMenu();
    // done
  }
  
//  /**
//   * intercept add
//   */
//  public void addNotify() {
//    // continue
//    super.addNotify();
//    // check container - don't mind resizing in toolbar
//    if (getParent() instanceof JToolBar) 
//      setMaximumSize(new Dimension(128,128));
//  }

  
  /**
   * Cancel pending popup
   */
  public void cancelPopup() {
    popup.setVisible(false);
  }
  
  protected void setPopupSize(Dimension d) {
    // resize all components with same size
    Component c = getPopup();
    Dimension min = c.getMinimumSize();
    d.width = Math.max(min.width, d.width);
    d.height = Math.max(min.height, d.height);
    Dimension old = c.getSize();
    while (c!=null && c.getSize().equals(old)) {
      c.setSize(d);
      c = c.getParent();
    }
    getPopup().revalidate();
  }
  
  /**
   * Change popup's visibility
   */
  public void showPopup() {
    
    // old lingering around?
    cancelPopup();

    // create it
    popup = getPopup();
    if (popup==null)
      return;
  
    // calc position
    int x=0, y=0;

    if (!(JToolBar.class.isAssignableFrom(getParent().getClass()))) {
      x += getWidth();
    } else {
      JToolBar bar = (JToolBar)getParent();
      if (JToolBar.VERTICAL==bar.getOrientation()) {
        x += bar.getLocation().x==0 ? getWidth() : -popup.getPreferredSize().width;
      } else {
        y += bar.getLocation().y<getHeight() ? getHeight() : -popup.getPreferredSize().height;
      }
    }
    
    // show it
    popup.show(PopupWidget.this, x, y);

  }
  
  /**
   * implementation for popup generation
   */
  protected JPopupMenu getPopup() {
    return popup;
  }
  
  /**
   * add an action to a popup
   */
  public void addItem(Component c) {
    popup.add(c);
  }

  public void addItems(List<? extends Action> actions) {
    for (Action action : actions)
      addItem(action);
  }

  public void addItem(Action action) {
    popup.add(new JMenuItem(action));
  }
  
  public void removeItems() {
    popup.removeAll();
  }

  /**
   * Setting this to a value >0 will fire first available action
   * on popup button click if clicked within that give time from
   * pressing the button
   */
  public void setFireOnClickWithin(long ms) {
    fireOnClickWithin = ms;
  }

  /**
   * Our special model
   */
  private class Model extends DefaultButtonModel implements Runnable {
    private long triggerTime = 0;
    /** our menu trigger */
    public void setPressed(boolean b) {
      // continue
      super.setPressed(b);
      // show menue (delayed)
      if (b) 
        SwingUtilities.invokeLater(this);
    }
    /** EDT callback */
    public void run() { 
      showPopup(); 
      triggerTime = System.currentTimeMillis();
    }
    /**
     * action performed
     */
    protected void fireActionPerformed(ActionEvent e) {
      // fire action on popup button press?
      if (fireOnClickWithin>0 && System.currentTimeMillis()-triggerTime<fireOnClickWithin) { 
        for (int i=0;i<popup.getComponentCount();i++) {
          Component c = popup.getComponent(i);
          if (c instanceof AbstractButton) {
            cancelPopup();
            ((AbstractButton)c).doClick();
            break;
          }
        }
      }
    }
  } //Model
  
} //PopupButton
