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
package genj.app;

import genj.util.swing.ImageIcon;

/**
 * Wrapper for package used Images
 */
final public class Images {

  private static Images instance = new Images();

  public static ImageIcon
    imgHelp, imgClose, imgNew, imgOpen, imgExit, imgSave, imgAbout;

  /**
   * Constructor which pre-loads all images
   */
  private Images() {
    imgHelp         = new ImageIcon(this,"images/Help");
    imgClose        = new ImageIcon(this,"images/Close");
    imgNew         = new ImageIcon(this,"images/New");
    imgOpen         = new ImageIcon(this,"images/Open");
    imgExit         = new ImageIcon(this,"images/Exit");
    imgSave         = new ImageIcon(this,"images/Save");
    imgAbout        = new ImageIcon(this,"images/About");
  }
}
