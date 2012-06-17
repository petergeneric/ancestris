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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * Helper for button creation etc.
 */
public class ButtonHelper {
  
  /** Members */
  private Class buttonType        = JButton.class;
  private Insets insets           = null;
  private JComponent container     = null;
  private ButtonGroup group       = null;
  private int fontSize            = -1;
  
  /** Setters */    
  public ButtonHelper setButtonType(Class set) { buttonType=set; return this; }
  public ButtonHelper setInsets(Insets set) { insets=set; return this; }
  public ButtonHelper setInsets(int val) { insets=new Insets(val,val,val,val); return this; }
  public ButtonHelper setContainer(JComponent set) { container=set; return this; }
  public ButtonHelper setFontSize(int set) { fontSize=set; return this; }
  
  /**
   * Creates a buttonGroup that successive buttons will belong to     */
  public ButtonGroup createGroup() {
    group = new ButtonGroup();
    return group;
  }

  /**
   * Creates a toggle button
   */
  public AbstractButton create(Action action, ImageIcon toggle, boolean state) {
    
    JToggleButton result = (JToggleButton)create(action, JToggleButton.class);
    result.setSelectedIcon(toggle);
    result.setSelected(state);
    return result;
  }

  /**
   * Creates the button
   */
  public AbstractButton create(Action action) {
    return create(action, buttonType);
  }
      
  /**
   * Creates the button
   */
  private AbstractButton create(final Action action, Class type) {
    
    // no mnemonic in JToolbars please
    if (container instanceof JToolBar)
      action.putValue(Action.MNEMONIC_KEY, null);
    
    // create the button and hook it up to action
    final AbstractButton result = createButton(type);
    if (result instanceof JButton) {
        result.setVerticalTextPosition(SwingConstants.BOTTOM);
        result.setHorizontalTextPosition(SwingConstants.CENTER);
    }
    result.setAction(action);
    
    // patch its look
    if (insets!=null)
      result.setMargin(insets);
    if (fontSize>0) {
      Font f = result.getFont();
      result.setFont(new Font(f.getName(), f.getStyle(), fontSize));
    }
    
    // context
    if (group!=null) {
      group.add(result);
    }
    if (container!=null) {
      container.add(result);
      if (container instanceof JToolBar) result.setMaximumSize(new Dimension(128,128));
    }

    // done
    return result;
  }

  /**
   * Helper that instantiates the AbstractButton
   */  
  private AbstractButton createButton(Class type) {
    try {
      return (AbstractButton)type.newInstance();
    } catch (Throwable t) {
      throw new IllegalStateException("Couldn't create AbstractButton for "+buttonType);
    }
  }
  
} //ButtonHelper
