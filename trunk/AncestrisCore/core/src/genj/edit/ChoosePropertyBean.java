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
package genj.edit;

import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.util.Resources;
import genj.util.swing.NestedBlockLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A bean that allows to choose a property from a list of properties
 */
public class ChoosePropertyBean extends JComponent {
  
  private final static Resources RESOURCES = Resources.get(ChoosePropertyBean.class);

  private JRadioButton rbChoose,rbCustom;
  private JTextField tfCustom;
  private JList lChoose;
  private JTextPane tpInfo;
  private List<ActionListener> listeners = new CopyOnWriteArrayList<ActionListener>();
  private Callback callback = new Callback();

  /**
   * Constructor
   */
  public ChoosePropertyBean(Property parent) {
    
    // keep parent and calculate possible properties
    MetaProperty[] defs = parent.getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN | MetaProperty.WHERE_CARDINALITY_ALLOWS);
    init(defs, true);
  }    
  
  /**
   * Constructor
   */
  public ChoosePropertyBean(MetaProperty[] defs) {
    init(defs, false);
  }
  
  private void init(MetaProperty[] defs, boolean allowCustom) {
    
    Arrays.sort(defs, callback);
    
    // Layout
    setLayout(new NestedBlockLayout("<col><label1/><row><tags/><info gx=\"1\" gy=\"1\"/></row><label2/><tag/></col>"));

    // Checkbox for known props
    rbChoose = new JRadioButton(RESOURCES.getString("choose.known"),defs.length>0);
    rbChoose.setEnabled(defs.length>0);
    rbChoose.addItemListener(callback);
    rbChoose.setAlignmentX(0);
    
    if (allowCustom)
      add(rbChoose);
    else
      add(new JLabel(RESOURCES.getString("choose.known")));
    
    // .. List of tags
    lChoose = new JList(defs);
    lChoose.setVisibleRowCount(4);
    lChoose.setEnabled(defs.length>0);
    lChoose.setCellRenderer(new MetaDefRenderer());
    lChoose.addListSelectionListener(callback);
    lChoose.addMouseListener(callback);
    add(new JScrollPane(lChoose));

    // .. Info field
    tpInfo = new JTextPane();
    tpInfo.setText("");
    tpInfo.setEditable(false);
    tpInfo.setPreferredSize(new Dimension(256,256));
    add(new JScrollPane(tpInfo));

    // RadioButton for new props
    rbCustom = new JRadioButton(RESOURCES.getString("choose.new"),defs.length==0);
    rbCustom.addItemListener(callback);
    rbCustom.setAlignmentX(0);
    if (allowCustom)
      add(rbCustom);

    ButtonGroup group = new ButtonGroup();
    group.add(rbChoose);
    group.add(rbCustom);

    // Create Lower Part
    tfCustom = new JTextField();
    tfCustom.setEnabled(defs.length==0);
    tfCustom.setAlignmentX(0);
    if (allowCustom)
      add(tfCustom);

    // Pre select
    if (defs.length>0) 
      lChoose.setSelectedIndex(0);
    
    // Done
  }
  
  /**
   * Multi-selection vs single-selection
   */
  public void setSingleSelection(boolean set) {
    lChoose.setSelectionMode(set ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }

  /**
   * Returns the selected tags
   */
  public String[] getSelectedTags() {

    String[] result = null;

    // list of selected properties
    if (rbChoose.isSelected() == true) {
      Object[] objs = lChoose.getSelectedValues();
      result = new String[objs.length];
      for (int i=0;i<objs.length;i++) {
        result[i] = ((MetaProperty)objs[i]).getTag();
      }
      return result;
    }
    
    // single entered tag
    String tag = tfCustom.getText();
    return tag!=null ? new String[] { tag } : new String[0];
  }

  /**
   * add action listener
   */
  public void addActionListener(ActionListener listener) {
    listeners.add(listener);
  }

  /**
   * remove action listener
   */
  public void removeActionListener(ActionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Tag List Cell Renderer
   */
  class MetaDefRenderer extends DefaultListCellRenderer implements ListCellRenderer {

    /**
     * Return component for rendering list element
     */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        MetaProperty def = (MetaProperty)value;
        setText(def.getName()+" ("+def.getTag()+")");
        setIcon(def.getImage());
      return this;
    }

  } //MetaDefRenderer
  
  /**
   * Internal Callback
   */
  private class Callback extends MouseAdapter implements ItemListener, ListSelectionListener, Comparator<MetaProperty>  {
    
    /** compare meta properties for alphabetic sorting */
    public int compare(MetaProperty m1, MetaProperty m2) {
      return m1.getName().compareTo(m2.getName());
    }
    
    /** check double clicks */
    public void mouseClicked(MouseEvent event) {
      if (event.getClickCount()>1) {
        ActionEvent e = new ActionEvent(ChoosePropertyBean.this, 0, null);
        for (ActionListener al : listeners) 
          al.actionPerformed(e);
      }
    }
    
    /**
     * RadioButtons have been changed
     */
    public void itemStateChanged(ItemEvent e) {
      if (e.getSource() == rbChoose) {
        lChoose.setEnabled(true);
        tfCustom.setEnabled(false);
        lChoose.requestFocusInWindow();
      }
      if (e.getSource() == rbCustom) {
        lChoose.clearSelection();
        lChoose.setEnabled(false);
        tfCustom.setEnabled(true);
        tfCustom.requestFocusInWindow();
      }
    }

    /**
     * One of the tag-items in the item list has been (de-)selected
     */
    public void valueChanged(ListSelectionEvent e) {
    
      // Check selection
      Object[] selection = lChoose.getSelectedValues();
    
      // None selected
      if ((selection==null)||(selection.length==0)) {
        tpInfo.setText("");
        return;
      }
    
      // Show info of last selected
      MetaProperty meta = (MetaProperty)selection[selection.length-1];
      tpInfo.setText(meta.getInfo());
      if (!rbChoose.isSelected())
        rbChoose.doClick();
    
      // Done
    }
    
  }

} //ChoosePropertyBean

