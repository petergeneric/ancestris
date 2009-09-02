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

import genj.util.swing.ImageIcon;

/**
 * Wrapper for package used Images
 */
final public class Images {

  private static Images instance = new Images();

  public static ImageIcon
  
    imgView,
    imgAdvanced,
    
    imgStickOn,
    imgStickOff,
    imgBack,
    imgForward,
    
    imgUndo,
    imgRedo,
    
    imgCut,
    imgCopy, 
    imgPaste,
    
    imgNew,
	
    imgDelEntity,
    imgNewEntity;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {
    
    imgView      = new ImageIcon(this,"images/View");
    imgAdvanced  = new ImageIcon(this,"images/Advanced");

    imgStickOn   = new ImageIcon(this,"images/StickOn");
    imgStickOff  = new ImageIcon(this,"images/StickOff");
    imgBack    = new ImageIcon(this,"images/Return");
    imgForward  = new ImageIcon(this,"images/Forward");

    imgUndo      = new ImageIcon(this,"images/Undo");
    imgRedo      = new ImageIcon(this,"images/Redo");
    
    imgCut       = new ImageIcon(this,"images/Cut");
    imgCopy      = new ImageIcon(this,"images/Copy");
    imgPaste     = new ImageIcon(this,"images/Paste");
    
    imgNew     = new ImageIcon(this,"images/New");
    
    imgNewEntity   = new ImageIcon(this,"images/entity/New");
    imgDelEntity    = new ImageIcon(this,"images/entity/Delete");
  }
}
