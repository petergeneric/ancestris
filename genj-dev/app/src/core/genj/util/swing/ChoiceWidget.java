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

import genj.util.ChangeSupport;
import genj.util.EnvironmentChecker;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;

/**
 * Our own JComboBox
 */
public class ChoiceWidget extends JComboBox {
  
  private final static boolean IS_JAVA_15 = EnvironmentChecker.isJava15(null);
  
  private boolean blockAutoComplete = false;
  
  /** our own model */
  private Model model = new Model();
  
  /** wether we match ignoring case */
  private boolean isIgnoreCase = false;
  
  /** change support */
  private ChangeSupport changeSupport = new ChangeSupport(this);
  
  /** auto complete support */
  private AutoCompleteSupport autoComplete;
  
  /**
   * Constructor
   */
  public ChoiceWidget() {
    this(new Object[0], null);
  }
  
  /**
   * Constructor
   */
  public ChoiceWidget(List values) {
    this(values.toArray(), null);
  }
  
  /**
   * Constructor
   */     
  public ChoiceWidget(Object[] values, Object selection) {

    // default is editable
    setEditable(true);

    // default max rows is 16
    setMaximumRowCount(8);

    // do our model
    setModel(model);

    // set the values now
    model.setValues(values);
       
    // alignment fix
    setAlignmentX(LEFT_ALIGNMENT);
    
    // try to set selection - not in values is ignored
    setSelectedItem(selection);
    
    // done
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
  
  /**
   * set values
   */
  public void setValues(List values) {
    setValues(values.toArray());
  }

  /**
   * set values
   */
  public void setValues(Object[] set) {
    try {
      blockAutoComplete = true;
      model.setValues(set);
    } finally {
      blockAutoComplete = false;
    }
  }

  /**
   * Patch preferred size. The default behavior of JComboBox can
   * lead to pretty wide preferred sizes if contained values are
   * long.
   */
  public Dimension getPreferredSize() {
    Dimension result = super.getPreferredSize();
    result.width = Math.min(128, result.width);
    return result;
  }
      
  /**
   * @see javax.swing.JComponent#getMaximumSize()
   */
  public Dimension getMaximumSize() {
    // 20040223 seems like maximum width should be pretty big really
    return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
  }
    
  /**
   * Accessor - whether a selectAll() should occur on focus gained
   */
  public void setSelectAllOnFocus(boolean set) {
    // ignored - currently LnF dependant
  }
    
  /**
   * Current text value
   */
  public String getText() {
    if (isEditable()) 
      return getEditor().getItem().toString();
    return super.getSelectedItem().toString();
  }
  
  /**
   * Set text value
   */
  public void setText(String text) {
    if (!isEditable) 
      throw new IllegalArgumentException("setText && !isEditable n/a");
    model.setSelectedItem(null);
    try {
      blockAutoComplete = true;
      
      try {
        getTextEditor().setText(text);
      } catch (Throwable t) {
        
        try {
          // retry with a new document
          Document doc = new PlainDocument(new StringContent(255));
          doc.insertString(0, text, null);
          getTextEditor().setDocument(doc);
        } catch (Throwable retry) {
          Logger.getLogger("genj.util.swing").log(Level.FINE, "Couldn't retry "+getTextEditor().getClass()+".setText("+text+")", retry);
          // as reported by Peter this might fail in PlainView - putting in debugging for that case
          Logger.getLogger("genj.util.swing").log(Level.WARNING, "Couldn't call "+getTextEditor().getClass()+".setText("+text+") - giving up", t);
          //  java.lang.NullPointerException
          //  at javax.swing.text.PlainView.getLineWidth(PlainView.java:631)
          //  at javax.swing.text.PlainView.updateDamage(PlainView.java:534)
          //  at javax.swing.text.PlainView.insertUpdate(PlainView.java:422)
          //  at javax.swing.text.FieldView.insertUpdate(FieldView.java:276)
          //  at javax.swing.plaf.basic.BasicTextUI$RootView.insertUpdate(BasicTextUI.java:1506)
          //  at javax.swing.plaf.basic.BasicTextUI$UpdateHandler.insertUpdate(BasicTextUI.java:1749)
          //  at javax.swing.text.AbstractDocument.fireInsertUpdate(AbstractDocument.java:184)
          //  at javax.swing.text.AbstractDocument.handleInsertString(AbstractDocument.java:754)
          //  at javax.swing.text.AbstractDocument.insertString(AbstractDocument.java:711)
          //  at javax.swing.text.PlainDocument.insertString(PlainDocument.java:114)
          //  at javax.swing.text.AbstractDocument.replace(AbstractDocument.java:673)
          //  at javax.swing.text.JTextComponent.setText(JTextComponent.java:1441)
          //  at genj.util.swing.ChoiceWidget.setText(ChoiceWidget.java:183)      
          
        }
        

      }
      
    } finally {
      blockAutoComplete = false;
    }
    
  }
  
  /**
   * Access to editor
   */
  public JTextField getTextEditor() {
    return (JTextField)getEditor().getEditorComponent();
  }
  
  /**
   * Enable case ignore matching with editor autocomplete
   */
  public void setIgnoreCase(boolean set) {
    isIgnoreCase = set;
  }
  
  /**
   * @see javax.swing.JComboBox#setPopupVisible(boolean)
   */
  public void setPopupVisible(boolean v) {
    // show it
    super.setPopupVisible(v);
    // try to find prefix in combo
    if (v) { 
      // not via this.addPopupMenuListener() in 1.4 only 
      String pre = getText();
      for (int i=0; i<getItemCount(); i++) {
        String item = getItemAt(i).toString();
        if (item.regionMatches(true, 0, pre, 0, pre.length())) {
          setSelectedIndex(i);
          break;
        }
      }
    }
    // done
  }

  /**
   * @see javax.swing.JComponent#requestFocus()
   */
  public void requestFocus() {
    if (isEditable())
      getEditor().getEditorComponent().requestFocus();
    else
      super.requestFocus();
  }
  
  /**
   * @see javax.swing.JComponent#requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    if (isEditable())
      return getEditor().getEditorComponent().requestFocusInWindow();
    return super.requestFocusInWindow();
  }
  
  /**
   * @see javax.swing.JComboBox#setEditor(javax.swing.ComboBoxEditor)
   */
  public void setEditor(ComboBoxEditor set) {

    // we're only allowing text fields
    if (!(set.getEditorComponent() instanceof JTextField))
      throw new IllegalArgumentException("Only JTextEditor editor components are allowed");
    
    // keep it
    super.setEditor(set);
    
    // add our auto-complete hook
    if (autoComplete==null) 
      autoComplete = new AutoCompleteSupport();
    autoComplete.attach(getTextEditor());
    
    // done
  }
  
  /**
   * @see javax.swing.JComboBox#addActionListener(java.awt.event.ActionListener)
   */
  public void addActionListener(ActionListener l) {
    getEditor().addActionListener(l);
  }
      
  /**
   * @see javax.swing.JComboBox#removeActionListener(java.awt.event.ActionListener)
   */
  public void removeActionListener(ActionListener l) {
    getEditor().removeActionListener(l);
  }

  /**
   * Auto complete support
   */
  private class AutoCompleteSupport extends KeyAdapter implements DocumentListener, ActionListener, FocusListener {

    private JTextField text;
    private Timer timer = new Timer(250, this);
    
    /**
     * Constructor
     */
    private AutoCompleteSupport() {
      // setup timer
      timer.setRepeats(false);
      // done
    }
    
    private void attach(JTextField set) {
      
      // old?
      if (text!=null) {
        text.getDocument().removeDocumentListener(this);
        text.removeFocusListener(this);
        text.removeKeyListener(this);
      }
      
      // new!
      text = set;
      text.getDocument().addDocumentListener(this);
      text.addFocusListener(this);
      text.addKeyListener(this);
    }
    
    /**
     * DocumentListener - callback
     */
    public void removeUpdate(DocumentEvent e) {
      changeSupport.fireChangeEvent();
      // add a auto-complete callback
      if (!blockAutoComplete&&isEditable())
        timer.start();
    }
      
    /**
     * DocumentListener - callback
     */
    public void changedUpdate(DocumentEvent e) {
      changeSupport.fireChangeEvent();
      // add a auto-complete callback
      if (!blockAutoComplete&&isEditable())
        timer.start();
    }
      
    /**
     * When something is typed in the editor's document we 
     * invoke a (delayed) auto complete on run()
     * @see genj.util.swing.TextFieldWidget#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      changeSupport.fireChangeEvent();
      // add a auto-complete callback
      if (!blockAutoComplete&&isEditable())
        timer.start();
    }
      
    /**
     * Our auto-complete callback
     */
    public void actionPerformed(ActionEvent e) {
      
      // grab current 'prefix'
      String prefix = text.getText();
      if (prefix.length()==0)
        return;

      // try to select an item by prefix - save current caret pos
      int caretPos = text.getCaretPosition();
      String match = model.setSelectedPrefix(prefix);
      
      // no match
      if (match.length()==0) {
        hidePopup();
        return;
      }
      
      // restore the original text & selection
      blockAutoComplete = true;
      text.setText(prefix);
      blockAutoComplete = false;
      text.setCaretPosition(caretPos);
      
      // show where we're at in case of a partial match
      if (match.length()>=prefix.length()) {
        
        // NM 20070224 make sure we're not calling showPopup() if not showing (was reported by JPJ) 
        //  java.awt.IllegalComponentStateException: component must be showing on the screen to determine its location
        //  at java.awt.Component.getLocationOnScreen_NoTreeLock(Unknown Source)
        //  at java.awt.Component.getLocationOnScreen(Unknown Source)
        //  at javax.swing.JPopupMenu.show(Unknown Source)
        //  at javax.swing.JComboBox.setPopupVisible(Unknown Source)
        //  at genj.util.swing.ChoiceWidget.setPopupVisible(ChoiceWidget.java:208)
        //  at javax.swing.JComboBox.showPopup(Unknown Source)
        //  at genj.util.swing.ChoiceWidget$AutoCompleteSupport.actionPerformed(ChoiceWidget.java:368)      
        if (isShowing())
          showPopup();
      } 
      
      // done      
    }
    
    /** selectAll on focus gained */
    public void focusGained(FocusEvent e) {
      if (text.getDocument() != null) {
        text.setCaretPosition(text.getDocument().getLength());
        text.moveCaretPosition(0);
      }
    }
    
    public void focusLost(FocusEvent e) {
      // Java 1.5 doesn't cancel the popup on focus lost IF the value in the editor
      // equals the current selection in the model - @see
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5100422
      if (IS_JAVA_15)
        setPopupVisible(false);
    }

    /** check for enter - use as selection */
    public void keyPressed(KeyEvent e) {
      // make a popup selection?
      if (e.getKeyCode()==KeyEvent.VK_ENTER&&isPopupVisible()) {
        model.setSelectedItem(model.getSelectedItem());
        setPopupVisible(false);
      }
      // done
    }
  } //AutoCompleteSupport
  
  /**
   * our own model
   */
  private class Model extends AbstractListModel implements ComboBoxModel {
    
    /** list of values */
    private Object[] values = new Object[0];
    
    /** selection */
    private Object selection = null;

    /**
     * Setter - values
     */
    private void setValues(Object[] set) {
      selection = null;
      
      if (values.length>0) 
        fireIntervalRemoved(this, 0, values.length-1);
      values = set;
      if (values.length>0) 
        fireIntervalAdded(this, 0, values.length-1);
      
    }

    /**
     * @see javax.swing.ComboBoxModel#getSelectedItem()
     */
    public Object getSelectedItem() {
      return selection;
    }
    
    /**
     * selects an item by prefix
     * @return the matching item
     */
    private String setSelectedPrefix(String prefix) {
      
      if (isIgnoreCase)
        prefix = prefix.toLowerCase();
      
      // try to find a match
      for (int i=0;i<values.length;i++) {
        String value = values[i].toString();
        if ((isIgnoreCase ? value.toLowerCase() : value).startsWith(prefix)) {
          setSelectedItem(value);
          return value;        
        }
      }
      
      // no match
      return "";
    }

    /**
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    public void setSelectedItem(Object seLection) {
      // remember
      selection = seLection;
      // propagate to editor
      blockAutoComplete = true;
      getEditor().setItem(selection);
      blockAutoComplete = false;
      // notify about item state change
      fireItemStateChanged(new ItemEvent(ChoiceWidget.this, ItemEvent.ITEM_STATE_CHANGED, selection, ItemEvent.SELECTED));
      // and notify of data change - apparently the JComboBox
      // doesn't update visually on setSelectedItem() if this
      // isn't called - might lead to double itemSelectionCHange
      // notifications though :(
      // (see DefaultComboBoxModel)
      fireContentsChanged(this, -1, -1);
      // done
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
      return values[index];
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
      return values.length;
    }

  } //Model

} //ChoiceWidget
