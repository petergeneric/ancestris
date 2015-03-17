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

import genj.util.WordBuffer;

import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Enhanced file chooser that accepts filter+description
 */
public class FileChooser extends JFileChooser {

  /** the textual command on the non-cancel button */
  private String command;

  /** the owning component */
  private JComponent owner;

  /**
   * Constructor
   */
  public FileChooser(JComponent owner, String title, String command, String extensions, String baseDir) {
    
    super(baseDir!=null?baseDir:".");

    setDialogTitle(title);
    
    this.owner  = owner;
    this.command= command;

    if (extensions!=null) {
      Filter filter = new Filter(extensions);
      addChoosableFileFilter(filter);
      setFileFilter(filter);
    }
  }

  /**
   * show it
   */
  public int showDialog() {
    int rc = showDialog(owner,command);
    // unselect selected file if not 'ok'
    if (rc!=0)
      setSelectedFile(null);
    return rc;
  }


  /**
   * Filter Definition
   */
  private class Filter extends FileFilter {
    
    /** extensions we're looking for */
    private String[] exts;
    
    /** description */
    private String descr;

    /**
     * Constructor
     */
    private Filter(String extensions) {

      StringTokenizer tokens = new StringTokenizer(extensions, ",");
      exts = new String[tokens.countTokens()];
      if (exts.length==0)
        throw new IllegalArgumentException("extensions required");
        
      WordBuffer buf = new WordBuffer(",");
      for (int i=0; i<exts.length; i++) {
        exts[i] = tokens.nextToken().toLowerCase().trim();
        buf.append("*."+exts[i]);
      }
      descr = buf.toString();
    }

    /**
     * Files to accept
     */
    public boolean accept(File f) {

      // directory is o.k.
      if (f.isDirectory())
        return true;

      // check extension
      String name = f.getName();
      int dot = name.lastIndexOf('.');
      if (dot<0)
        return false;
      String ext = name.substring(dot+1); 
        
      // loop
      for (int i=0;i<exts.length;i++) {
        if (exts[i].equalsIgnoreCase(ext))
          return true;
      }
      
      // not found
      return false;
    }

    /**
     * Description
     */
    public String getDescription() {
      return descr;
    }

  } //Filter

} //FileChooser
