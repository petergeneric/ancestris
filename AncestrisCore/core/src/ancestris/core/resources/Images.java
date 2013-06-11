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
package ancestris.core.resources;

import genj.util.swing.ImageIcon;

/**
 * Wrapper for package used Images
 */
final public class Images {

    public static ImageIcon imgView = new ImageIcon(Images.class,"images/View");
    
    public static ImageIcon imgStickOn = new ImageIcon(Images.class,"images/StickOn");
    public static ImageIcon imgStickOff = new ImageIcon(Images.class,"images/StickOff");
    public static ImageIcon imgFocus = new ImageIcon(Images.class,"images/Focus");
    
    public static ImageIcon imgUndo = new ImageIcon(Images.class,"images/Undo");
    public static ImageIcon imgRedo = new ImageIcon(Images.class,"images/Redo");
    
    public static ImageIcon imgCut = new ImageIcon(Images.class,"images/Cut");
    public static ImageIcon imgCopy = new ImageIcon(Images.class,"images/Copy");
    public static ImageIcon imgPaste = new ImageIcon(Images.class,"images/Paste");
    
    public static ImageIcon imgAdd = new ImageIcon(Images.class,"images/Add");
	
    public static ImageIcon imgDel = new ImageIcon(Images.class,"images/Delete");
    public static ImageIcon imgNew = new ImageIcon(Images.class,"images/New");
    public static ImageIcon imgSave = new ImageIcon(Images.class,"images/Save");

  /**
   * Static class only
   */
  private Images() {}
}
