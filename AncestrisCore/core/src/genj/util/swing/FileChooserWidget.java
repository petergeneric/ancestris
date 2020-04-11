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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.FileChooserBuilder;
import genj.io.InputSource;
import genj.util.EnvironmentChecker;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
  private String directory = EnvironmentChecker.getProperty("user.home", ".", "file chooser directory");
  
  /** an accessory if any */
  private JComponent accessory;
  
  /** action listeners */
  private List<ActionListener> listeners = new CopyOnWriteArrayList<ActionListener>();
  
  /** action listener connector to text field */
  private ActionListener actionProxy = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      fireActionEvent();
    }
  };

  /** action mode : open or save */
  private boolean open;
 
  /** 
   * constructor 
   */
  public FileChooserWidget() {
    this(null);
    this.open = true;
  }

  public FileChooserWidget(boolean open) {
    this(null);
    this.open = open;
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
    ActionListener[] ls = listeners.toArray(new ActionListener[listeners.size()]);
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
    if (file!=null&&file.getParentFile()!=null && file.getParentFile().isDirectory())
      setDirectory(file.getParentFile().toString());
  }
  
  /**
   * Get current file Name.
   * Could be used for Remote value.
   */
  public String getFile() {
    
    File file = new File(text.getText().trim());
    String name = file.getName();
    if (file.exists()) {
    
    if (extensions!=null&&name.indexOf(".")<0&&extensions.indexOf(',')<0) {
      String ext = extensions.trim();
      if (name.length()>0&&!name.endsWith("."+ext))
        file = new File(file.getParentFile(), name+"."+ext);
    }
        return file.getAbsolutePath();
    }
    return text.getText().trim();
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
  
  @Override
  public void requestFocus() {
    text.requestFocus();
  }

  /**
   * Choose with file dialog
   */
  private class Choose extends AbstractAncestrisAction implements PropertyChangeListener {
    
    /** constructor */
    private Choose() {
      setText("...");
    }

    /** choose file */    
    @Override
    public void actionPerformed(ActionEvent event) {

        File file = null;
        FileChooserBuilder fcb  = new FileChooserBuilder(FileChooserWidget.class)
                    .setDirectoriesOnly(false)
                    .setDefaultBadgeProvider()
                    .setAccessory(accessory)
                    .setDefaultTitle()
                    .setApproveText(AbstractAncestrisAction.TXT_OK)
                    .setParent(FileChooserWidget.this)
                    .setDefaultWorkingDirectory(new File(directory))
                    .setSelectedFile(new File(getFile()));
        
        if (open) {
            file = fcb.showOpenDialog();
        } else {
            file = fcb.showSaveDialog();
        }
                    
      if (file!=null)  {
        setFile(file);
        directory = file.getParent();
        fireActionEvent();
      }
      
      // done
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      
      if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
        File file = (File)evt.getNewValue();
        if (accessory instanceof ThumbnailWidget)
          ((ThumbnailWidget)accessory).setSource(file!=null ? InputSource.get(file).get() : null);
      }
    }
    
  } //Choose
 
} //FileChooserWidget
