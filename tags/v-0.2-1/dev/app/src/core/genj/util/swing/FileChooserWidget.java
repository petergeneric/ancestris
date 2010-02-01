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

import genj.util.EnvironmentChecker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * Widget for choosing a file with textfield and button
 */
public class FileChooserWidget extends JPanel {

  /** text field  */
  private TextFieldWidget text = new TextFieldWidget("", 12);
  
  /** choose action */
  private Choose choose = new Choose();
  
  /** file extensions */
  private String extensions;
  
  /** extensions for executables */
  public final static String EXECUTABLES = "exe, bin, sh, cmd, bat";
  
  /** start directory */
  private String directory = EnvironmentChecker.getProperty(this, "user.home", ".", "file chooser directory");
  
  /** an accessory if any */
  private JComponent accessory;
  
  /** action listeners */
  private List listeners = new ArrayList();
  
  /** action listener connector to text field */
  private ActionListener actionProxy = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      fireActionEvent();
    }
  };
 
  /** 
   * constructor 
   */
  public FileChooserWidget() {
    this(null);
  }
  
  /**
   * delegate enabled to sub-components
   */
  public void setEnabled(boolean set) {
    super.setEnabled(set);
    choose.setEnabled(set);
    text.setEnabled(set);
  }
  
  /** 
   * constructor 
   * @param extensions comma-separated list of extensions applying to selectable files
   */
  public FileChooserWidget(String extensions) {
    super(new BorderLayout());
    
    add(BorderLayout.CENTER, text );
    add(BorderLayout.EAST  , new ButtonHelper().setInsets(0).create(choose));
    this.extensions = extensions;
  }
  
  /**
   * fire action event
   */
  private void fireActionEvent() {
    ActionEvent e = new ActionEvent(this, 0, "");
    ActionListener[] ls = (ActionListener[])listeners.toArray(new ActionListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++)
      ls[i].actionPerformed(e);
  }
  
  /**
   * Add listener
   */
  public void addChangeListener(ChangeListener l) {
    text.addChangeListener(l);
  }
  
  /**
   * Remove listener
   */
  public void removeChangeListener(ChangeListener l) {
    text.removeChangeListener(l);
  }
  
  /**
   * Add listener
   */
  public void addActionListener(ActionListener l) {
    // hook up to textfields action if this is the first listener
    if (listeners.isEmpty())
      text.addActionListener(actionProxy);
    listeners.add(l);
  }
  
  /**
   * Remove listener
   */
  public void removeActionListener(ActionListener l) {
    listeners.remove(l);
    // dehook from textfields action if this was the last listener
    if (listeners.isEmpty())
      text.removeActionListener(actionProxy);
  }
  
  /**
   * Setter - a start directory
   */
  public void setDirectory(String set) {
    directory = set;
  }
  
  /**
   * Getter - 'current' directory
   */
  public String getDirectory() {
    return directory;
  }
  
  /**
   * Whether there is an actual selection
   */
  public boolean isEmpty() {
    return text.isEmpty();
  }
  
  /**
   * Makes current text in chooser a template
   */
  public void setTemplate(boolean set) {
    text.setTemplate(set);
  }
  
  /**
   * Set current file selection
   */
  public void setFile(String file) {
    text.setText(file!=null ? file : "");
  }
  
  /**
   * Set current file selection
   */
  public void setFile(File file) {
    // 20060126 in version 1.7 I thought about using file's absolute path
    // from here on but sometimes that undersirable since file might
    // not contain a valid full path in the first place
    text.setText(file!=null ? file.getPath() : "");
  }
  
  /**
   * Get current file
   */
  public File getFile() {
    return new File(text.getText());
  }
  
  /** 
   * Setter - An accessory
   */
  public void setAccessory(JComponent set) {
    accessory = set;
  }

  /**
   * Focus goes to entry field
   */  
  public boolean requestFocusInWindow() {
    return text.requestFocusInWindow();
  }

  /**
   * Choose with file dialog
   */
  private class Choose extends Action2 {
    
    /** constructor */
    private Choose() {
      setText("...");
      setTarget(FileChooserWidget.this);
    }

    /** choose file */    
    protected void execute() {

      // create and show chooser      
      FileChooser fc = new FileChooser(FileChooserWidget.this, getName(), Action2.TXT_OK, extensions, directory);
      fc.setAccessory(accessory);
      fc.showDialog();
      
      // check result
      File file = fc.getSelectedFile();
      if (file!=null)  {
        setFile(file);
        directory = file.getParent();
        
        // notify
        fireActionEvent();
      }
      
      // done
    }
    
  } //Choose
 
} //FileChooserWidget
