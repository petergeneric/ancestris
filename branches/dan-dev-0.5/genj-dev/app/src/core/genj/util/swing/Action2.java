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


import genj.util.MnemonicAndText;
import genj.util.Resources;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/**
 * An Action
 */
public class Action2 extends AbstractAction {
  
  /*package*/ final static String 
    KEY_TEXT = Action.NAME,
    KEY_OLDTEXT = Action.NAME+".old",
    KEY_SHORT_TEXT = "shortname",
    KEY_TIP = Action.SHORT_DESCRIPTION,
    KEY_ENABLED = "enabled",
    KEY_MNEMONIC = Action.MNEMONIC_KEY,
    KEY_ICON = Action.SMALL_ICON,
    KEY_SELECTED = "SwingSelectedKey";
    
  private final static Logger LOG = Logger.getLogger("genj.actions");
  
  /** predefined strings */
  public final static String
    TXT_YES         = UIManager.getString("OptionPane.yesButtonText"),
    TXT_NO          = UIManager.getString("OptionPane.noButtonText"),
    TXT_OK          = UIManager.getString("OptionPane.okButtonText"),
    TXT_CANCEL  = UIManager.getString("OptionPane.cancelButtonText");
  
  /** constructor */
  public Action2() {
  }
  
  /** constructor */
  public Action2(Resources resources, String text) {
    this(resources.getString(text));
  }
  
  /** constructor */
  public Action2(String text) {
    setText(text);
  }
  
  /** constructor */
  public Action2(String text, boolean enabled) {
    this(text);
    setEnabled(enabled);
  }
  
  /** default noop implementation of action invocation */
  public void actionPerformed(ActionEvent e) {
  }
  
  /**
   * Intercepted super class getter - delegate calls to getters so they can be overridden. this
   * method is not supposed to be called from sub-types to avoid a potential endless-loop
   */
  public Object getValue(String key) {
    if (KEY_TEXT.equals(key))
      return getText();
    if (KEY_ICON.equals(key))
      return getImage();
    if (KEY_TIP.equals(key))
      return getTip();
    return super.getValue(key);
  }
  
  /**
   * accessor - image 
   */
  public Action2 setImage(Icon icon) {
    super.putValue(KEY_ICON, icon);
    return this;
  }
  
  /**
   * accessor - text
   */
  public Action2 restoreText() {
    setText((String)super.getValue(KEY_OLDTEXT));
    return this;
  }
  
  /**
   * accessor - text
   */
  public Action2 setText(String txt) {
    return setText(null, txt);
  }
    
  /**
   * accessor - text
   */
  public Action2 setText(Resources resources, String txt) {
    
    // translate?
    if (resources!=null)
      txt = resources.getString(txt);
      
    // remember old
    super.putValue(KEY_OLDTEXT, getText());
    
    // check txt?
    if (txt!=null&&txt.length()>0)  {
        // calc mnemonic 
        MnemonicAndText mat = new MnemonicAndText(txt);
        txt  = mat.getText();
        // use accelerator keys on mac and mnemonic otherwise
        setMnemonic(mat.getMnemonic());
    }
    
    // remember new 
    super.putValue(KEY_TEXT, txt);
    
    return this;
  }
  
  /**
   * acessor - mnemonic
   */
  public Action2 setMnemonic(char c) {
    super.putValue(KEY_MNEMONIC, c==0 ? null : new Integer(c));
    return this;
  }
  
  public char getMnemonic() {
    Integer val = (Integer)super.getValue(KEY_MNEMONIC);
    return val != null ? (char)val.intValue() : (char)0;
  }

  /**
   * accessor - text
   */
  public String getText() {
    return (String)super.getValue(KEY_TEXT);
  }
  
  /**
   * accessor - tip
   */
  public Action2 setTip(String tip) {
    return setTip(null, tip);
  }
  
  /**
   * accessor - tip
   */
  public Action2 setTip(Resources resources, String tip) {
    if (resources!=null) tip = resources.getString(tip);
    super.putValue(KEY_TIP, tip);
    return this;
  }
  
  /**
   * accessor - tip
   */
  public String getTip() {
    return (String)super.getValue(KEY_TIP);
  }

  /**
   * accessor - image 
   */
  public Icon getImage() {
    return (Icon)super.getValue(KEY_ICON);
  }

  /** convenience factory */
  public static Action2 yes() {
    return new Constant(Action2.TXT_YES);
  }

  /** convenience factory */
  public static Action2 no() {
    return new Constant(Action2.TXT_NO);
  }

  /** convenience factory */
  public static Action2 ok() {
    return new Constant(Action2.TXT_OK);
  }

  /** convenience factory */
  public static Action2 cancel() {
    return new Constant(Action2.TXT_CANCEL);
  }

  /** convenience factory */
  public static Action2[] yesNo() {
    return new Action2[]{ yes(), no() };
  }
  
  /** convenience factory */
  public static Action2[] yesNoCancel() {
    return new Action2[]{ yes(), no(), cancel() };
  }
  
  /** convenience factory */
  public static Action2[] okCancel() {
    return new Action2[]{ ok(), cancel() };
  }
  
  /** convenience factory */
  public static Action2[] andCancel(Action2 ok) {
    return new Action2[]{ ok, cancel() };
  }
  
  /** convenience factory */
  public static Action2[] okAnd(Action2 action) {
    return new Action2[]{ ok(), action };
  }
  
  /** convenience factory */
  public static Action2[] okOnly() {
    return new Action2[]{ ok() };
  }
  
  /** convenience factory */
  public static Action2[] cancelOnly() {
    return new Action2[]{ cancel() };
  }
  
  private static class Constant extends Action2 {
    private Constant(String txt) { super(txt); }
    public void actionPerformed(ActionEvent e) {};
  };
  
  public Action2 install(JComponent component, String shortcut) {
    return install(component,shortcut,JComponent.WHEN_IN_FOCUSED_WINDOW);
  }
  
  public Action2 install(JComponent component, String shortcut, int condition) {
    InputMap inputs = component.getInputMap(condition);
    inputs.put(KeyStroke.getKeyStroke(shortcut), this);
    component.getActionMap().put(this, this);
    return this;
  }
  
  public static void uninstall(JComponent component, String shortcut) {
    uninstall(component, component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), shortcut);
    uninstall(component, component.getInputMap(JComponent.WHEN_FOCUSED), shortcut);
    uninstall(component, component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), shortcut);
  }
  
  private static void uninstall(JComponent component, InputMap map, String shortcut) {
    KeyStroke key = KeyStroke.getKeyStroke(shortcut);
    Object o = map.get(key);
    if (o instanceof Action2) {
      map.put(key, null);
      component.getActionMap().remove(o);
    }
  }

  public boolean isSelected() {
    return Boolean.TRUE.equals((Boolean)getValue(KEY_SELECTED));
  }
  
  public boolean setSelected(boolean selected) {
    boolean old = isSelected();
    putValue(KEY_SELECTED, selected ? Boolean.TRUE : Boolean.FALSE);
    return old;
  }
  
  /**
   * An action group
   */
  public static class Group extends Action2 implements Iterable<Action2> {
    
    private ArrayList<Action2> actions = new ArrayList<Action2>(4);
    private boolean sort;
    
    /** constructor */
    public Group(String text, Icon icon, boolean sort) {
      super(text);
      setImage(icon);
      this.sort = sort;
    }
    
    /** constructor */
    public Group(String text, Icon icon) {
      super(text);
      setImage(icon);
    }
    
    /** constructor */
    public Group(String text, ImageIcon imageIcon, List<Action2> actions) {
      super(text);
      setImage(imageIcon);
      
      this.actions.addAll(actions);
    }
    
    public Group(String text) {
    	this(text,null);
    }
    
    public Group add(Action2 action) {
      // merge into known
      for (Action old : this) {
        if (old.equals(action)) {
          if (old instanceof Group) for (Action2 a : (Group)action)
            ((Group)old).add(a);
          return this;
        }
      }
      // keep
      if (sort) {
        for (int i=0;i<actions.size();i++) {
          if (actions.get(i).getText().compareTo(action.getText())>0) {
            actions.add(i, action);
            return this;
          }
        }
      }
      actions.add(action);
      return this;
    }

    public Group addAll(Group group) {
      for (Action2 action : group)
        add(action);
      return this;
    }
    
    public Group addAll(List<Action2> actions) {
      for (Action2 action : actions)
        add(action);
      return this;
    }
    
    public Group clear() {
      actions.clear();
      return this;
    }

    public int size() {
      return actions.size();
    }

    public Iterator<Action2> iterator() {
      return actions.iterator();
    }
    
    public void actionPerformed(ActionEvent e) {
      throw new IllegalArgumentException("group doesn't support actionPerformed()");
    }
    
    @Override
    public void setEnabled(boolean newValue) {
      for (Action action : actions)
        action.setEnabled(newValue);
    }

  } //Group

} //Action2

