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
package genj.edit.actions;

import genj.gedcom.PropertyFile;
import genj.util.swing.Action2;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * External action 
 */
public class RunExternal extends Action2 {
  
  /** the wrapped file */
  private File file;
  
  /**
   * Constructor
   */
  public RunExternal(File file) {
    this.file = file;
    super.setImage(PropertyFile.DEFAULT_IMAGE);
    super.setText("Open");
    setEnabled(file.exists());
  }
  
  /**
   * @see genj.util.swing.Action2#execute()
   */
  public void actionPerformed(ActionEvent event) {
    if (file==null)
      return;
    try {
      Desktop.getDesktop().open(file);
    } catch (Throwable t) {
      Logger.getLogger("genj.edit.actions").log(Level.INFO, "can't open "+file, t);
    }
  }
  
} //RunExternal
