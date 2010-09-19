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
package genjfr.util;

import genj.util.MnemonicAndText;
import genj.util.swing.Action2;
import genj.view.ActionProvider.SeparatorAction;
import java.awt.Component;

import java.util.Stack;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class which provides some static helpers for menu-handling
 */
@ServiceProvider(service=ActionPresenterProvider.class, position=1)
public class MyActionPresenter extends ActionPresenterProvider {

  private Stack<JComponent> menus = new Stack<JComponent>();  // JMenu or JPopupMenu or JMenuBar
  
  /** Setters */    
  public MyActionPresenter popMenu() { 
    menus.pop(); 
    return this; 
  }
  
  public MyActionPresenter pushMenu(JComponent menu) { 
    if (!menus.isEmpty())
      menus.peek().add(menu);
    menus.push(menu);
    
    return this;
  }
  
  public JMenu createMenu(Action2.Group action) {
    JMenu result = new JMenu(action);
    pushMenu(result);
    for (Action2 sub : action)
      createPopupPresenter(sub);
    return result;
  }
  
  /**
   * Creates a PopupMenu
   */
  public JPopupMenu createEmptyPopup() {
    // create one
    JPopupMenu result = new JPopupMenu();
    // done
    return result;
  }

  /**
   * Creates items from list of ActionDelegates
   * @param actions either ActionDelegates or lists of ActionDelegates that
   * will be separated visually by createSeparator
   */
  private void createItems(Iterable<Action2> actions) {
    // historically - supported null
    if (actions==null)
      return;
    // Loop through list
    boolean first = true;
    for (Action2 action : actions) {
      if (first) {
        createSeparator();
        first = false;
      }
      createPopupPresenter(action);
    }
    // done
  }
  
  public final JMenuItem createPopupPresenter(Action action) {
    
    // an action group?
    if (action instanceof Action2.Group) {
      Action2.Group group = (Action2.Group)action;
      if (group.size()==0)
        return null;
      JMenu sub = new JMenu(action);
      if (action instanceof Action2)
        sub.setMnemonic(((Action2)action).getMnemonic());
      pushMenu(sub);
      createItems(group);
      popMenu();
      return sub;
    }
    
    // a NOOP results in separator
    // TODO this should not refer to something from genj.view
    if (action instanceof SeparatorAction) {
      createSeparator();
      return null;
    }
    
    // create a menu item
    JMenuItem result;
//    if (action.getValue(Action2.KEY_SELECTED)!=null)
//      result = new JCheckBoxMenuItem();
//    else
      result = new JMenuItem();
    set(action, result);
    if (action instanceof Action2)
      result.setMnemonic(((Action2)action).getMnemonic());
    
    // add it to current menu on stack  
    if (!menus.isEmpty())
        menus.peek().add(result);
      
    // done
    return result;
  }
  
  protected void set(Action action, JMenuItem item) {
    item.setAction(action);
  }

  /**
   * Creates an separator
   */
  public MyActionPresenter createSeparator() {
    // try to create one
    if (menus.isEmpty()) return this;
    JComponent menu = menus.peek();
    if (menu instanceof JMenu) {
      JMenu jmenu = (JMenu)menu;
      int count = jmenu.getMenuComponentCount();
      if (count>0 && jmenu.getMenuComponent(count-1).getClass() != JPopupMenu.Separator.class)
        jmenu.addSeparator();
    }
    if (menu instanceof JPopupMenu) {
      JPopupMenu pmenu = (JPopupMenu)menu;
      int count = pmenu.getComponentCount();
      if (count>0 && pmenu.getComponent(count-1).getClass() != JPopupMenu.Separator.class)
        pmenu.addSeparator();
    }
    // done      
    return this;
  }

        public JMenuItem createMenuPresenter(Action action) {
            return new JMenuItem(action);
        }

        public Component createToolbarPresenter(Action action) {
            return new JButton(action);
        }

        public Component[] convertComponents(Component comp) {
            return new Component[] {comp};
        }
  
} //MenuHelper

