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

import ancestris.swing.UndoTextArea;
import genj.util.ChangeSupport;
import java.awt.KeyboardFocusManager;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

/**
 * Our own JTextArea
 */
public class TextAreaWidget extends UndoTextArea {

  /** change support */
  private ChangeSupport changeSupport = new ChangeSupport(this);

  /**
   * Constructor
   */
  public TextAreaWidget(String text, int rows, int cols) {
    this(text, rows, cols, true, false);
  }
  
  /**
   * Constructor
   */
  public TextAreaWidget(String text, int rows, int cols, boolean editable, boolean wrap) {
    super(text);
    this.setRows(rows);
    this.setColumns(cols);
    
    setAlignmentX(0);
    setEditable(editable);
    setLineWrap(wrap);
    setWrapStyleWord(true);
    setFont(new JTextField().getFont()); //don't use standard clunky text area font

    // restore default focus traversal keys (overriding
    // JTextArea's ctrl (shift) tab
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    
    getDocument().addDocumentListener(changeSupport);
  }
  
  /**
   * Add change listener
   */
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }
  
  /**
   * Remove change listener
   */
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }
  
} //TextAreaWidget
