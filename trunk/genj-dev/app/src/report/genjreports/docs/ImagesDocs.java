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
package genjreports.docs;

import genj.util.swing.ImageIcon;

/**
 * Wrapper for package used Images
 */
final public class ImagesDocs {

  private static ImagesDocs instance = new ImagesDocs();

  public static ImageIcon

    imgView,
    imgAdvanced,

    imgMenu,
    imgBirth,
    imgDeath,
    imgMarriage,
    imgStore,

    imgCut,
    imgCopy,
    imgPaste,

    imgFile,

    imgSearch,

    imgSettings,
    imgClose,

    imgNew;

  /**
   * Constructor which pre-loads all images
   */
  private ImagesDocs() {

    imgView      = new ImageIcon(this,"images/View.png");
    imgAdvanced  = new ImageIcon(this,"images/Advanced.png");

    imgCut       = new ImageIcon(this,"images/Cut.png");
    imgCopy      = new ImageIcon(this,"images/Copy.png");
    imgPaste     = new ImageIcon(this,"images/Paste.png");

    imgMenu      = new ImageIcon(this,"images/Menu.png");
    imgBirth     = new ImageIcon(this,"images/Birth.png");
    imgDeath     = new ImageIcon(this,"images/Death.png");
    imgMarriage  = new ImageIcon(this,"images/Marriage.png");
    imgStore     = new ImageIcon(this,"images/Repository.png");

    imgFile      = new ImageIcon(this,"images/Disk.png");

    imgSearch    = new ImageIcon(this,"images/Search.png");

    imgSettings  = new ImageIcon(this,"images/Settings.png");
    imgClose     = new ImageIcon(this,"images/Close.png");

    imgNew       = new ImageIcon(this,"images/New.png");

  }
}
