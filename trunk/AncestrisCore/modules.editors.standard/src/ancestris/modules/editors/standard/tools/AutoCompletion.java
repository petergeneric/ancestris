/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard.tools;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
public class AutoCompletion extends PlainDocument {

    static Map<JComboBox, AutoCompletion> instances = null;
    
    final JComboBox comboBox;
    JTextComponent textEditor;
    List<String> comboList;
    boolean refreshingList = false;     // true if list is being refreshed in which case editor should not change (when changing list, combo tries to refresh editor)
    boolean refreshList = true;         // true if list is to be refreshed because filter has changed
    boolean showPopup = false;          // true if popup can be shown
    boolean readyToSelect = false;      // true if the enter key can select the selected item and then write in the editor

    public AutoCompletion(JComboBox combo, List<String> list) {
        this.comboBox = combo;
        this.comboList = list;
        
        this.comboBox.setEditable(true);
        
        setScrollBars();
        
        textEditor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        textEditor.setDocument(this);
        
        textEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                refreshList = false;
                refreshingList = false;
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    refreshingList = true;
                    showPopup = true;
                    readyToSelect = true;
                    comboBox.setPopupVisible(showPopup);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (comboBox.isPopupVisible()) {
                        String text = comboBox.getModel().getSelectedItem().toString().trim();
                        if (!text.isEmpty()) {
                            setText(text);
                        }
                    }
                    showPopup = false;
                    comboBox.setPopupVisible(showPopup);
                    resetList();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showPopup = false;
                    comboBox.setPopupVisible(showPopup);
                    readyToSelect = false;
                } else {
                    refreshList = true;
                    showPopup = true;
                    readyToSelect = false;
                }
            }
        });
        
        resetList();
    }

    private void resetList()  {
        refreshingList = true;
        comboList.add(""); // add a line at the end to have room to display the horizontal scrollbar
        comboBox.setModel(new DefaultComboBoxModel(comboList.toArray(new String[comboList.size()])));
        comboBox.setPopupVisible(false);
        refreshingList = false;
        refreshList = false;
    }
    
    private void refreshList()  {
        if (!refreshList) {
            return;
        }
        refreshingList = true;
        try {
            String filter = getText(0, getLength());
            String[] listArray = getFilteredList(filter);
            // If list is empty, add filter at the top of the list, replacing the previous string if necessary
            if (listArray.length == 0) {
                String str0 = (String) comboBox.getItemAt(0);
                if (comboBox.getItemCount() != 0 && !comboList.contains(str0)) {
                    comboBox.removeItemAt(0);
                }
                comboBox.insertItemAt(filter, 0);
                comboBox.setSelectedIndex(0);
            }
            if (listArray.length != 0) {
                comboBox.setModel(new DefaultComboBoxModel(listArray));
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        comboBox.setPopupVisible(showPopup);
        refreshingList = false;
        refreshList = false;
    }
    
    @Override
    public void remove(int offs, int len) throws BadLocationException {
        // return immediately when refreshing list
        if (refreshingList) {
            return;
        }
        super.remove(offs, len);
        refreshList();
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // return immediately when refreshing list
        if (refreshingList) {
            return;
        }
        // insert the string into the document
        super.insertString(offs, str, a);
        refreshList();
    }

    private void setText(String text) {
        try {
            // remove all text and insert the completed string
            super.remove(0, getLength());
            super.insertString(0, text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private String[] getFilteredList(String text) {
        List<String> list = new ArrayList<String>();
        for (String item : comboList) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                list.add(item);
            }
        }
        list.add(""); // add empty line in case scrollbar hides last line, therefore to force one more line to display
        return list.toArray(new String[list.size()]);
    }
    
    
    public static void reset(JComboBox comboBox, List<String> list) {
        if (instances == null) {
            instances = new HashMap<JComboBox, AutoCompletion>();
        } 
        
        AutoCompletion instance = instances.get(comboBox);
        
        if (instance == null) {
            instance = new AutoCompletion(comboBox, list);
            instances.put(comboBox, instance);
        } else {
            instance.comboList = list;
            instance.resetList();
        }
        
    }

    private void setScrollBars() {
        Object comp = comboBox.getUI().getAccessibleChild(comboBox, 0);
        if (!(comp instanceof JPopupMenu)) {
            return;
        }
        JPopupMenu popup = (JPopupMenu) comp;
        if (popup.getComponent(0) instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
            scrollPane.setHorizontalScrollBar(new JScrollBar(JScrollBar.HORIZONTAL));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
    }
    
}
