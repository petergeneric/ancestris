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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.tree;

import genj.util.swing.ImageIcon;

/**
 * A wrapper to the images we use in the TreeView
 */
final class Images {

    private static final Images instance = new Images();

    static ImageIcon imgView,
            imgOverview,
            imgHori,
            imgVert,
            imgDoFams,
            imgDontFams,
            imgFoldSymbols,
            imgUnfoldAll,
            imgFoldAll,
            imgGotoContext,
            imgGotoRoot;

    /**
     * Constructor which pre-loads all images
     */
    private Images() {

        imgView = new ImageIcon(this, "images/View");
        imgGotoRoot = new ImageIcon(this, "images/GotoRoot");
        imgGotoContext = new ImageIcon(this, "images/GotoContext");

        imgOverview = new ImageIcon(this, "images/Overview");

        imgHori = new ImageIcon(this, "images/Hori");
        imgVert = new ImageIcon(this, "images/Vert");

        imgDoFams = new ImageIcon(this, "images/DoFams");
        imgDontFams = new ImageIcon(this, "images/DontFams");

        imgFoldSymbols = new ImageIcon(this, "images/FoldUnfold");
        imgUnfoldAll = new ImageIcon(this, "images/UnfoldAll");
        imgFoldAll = new ImageIcon(this, "images/FoldAll");
    }

} //Images
