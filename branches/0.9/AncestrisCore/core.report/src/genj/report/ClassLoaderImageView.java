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
 *
 * $Revision$ $Author$ $Date$
 */
package genj.report;

import genj.util.swing.ImageIcon;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.Icon;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.ImageView;

/**
 * View of an image loaded by the classloader.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class ClassLoaderImageView extends ImageView {

    /**
     * The loaded image.
     */
    private Image image;

    /**
     * Image width.
     */
    private int width;

    /**
     * Image height.
     */
    private int height;

    /**
     * Creates a new view that represents an IMG element.
     *
     * @param elem the element to create a view for
     */
    public ClassLoaderImageView(Element elem, Class from) {
        super(elem);
        String src = (String) getElement().getAttributes().getAttribute(
                HTML.Attribute.SRC);
        try {
            image = new ImageIcon(from, src).getImage();
            width = image.getWidth(null);
            height = image.getHeight(null);
        } catch (Exception e) {
            image = null;
            Icon icon = getNoImageIcon();
            if (icon != null) {
                width = getNoImageIcon().getIconWidth();
                height = getNoImageIcon().getIconHeight();
            }
        }
    }

    /**
     * Paints the View.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     */
    public void paint(Graphics g, Shape a) {

        Rectangle rect = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
        Rectangle clip = g.getClipBounds();

        if (clip != null)
            g.clipRect(rect.x, rect.y, rect.width, rect.height);

        if (image != null)
            g.drawImage(image, rect.x, rect.y, width, height, null);
        else {
            Icon icon = getNoImageIcon();
            if (icon != null)
                icon.paintIcon(getContainer(), g, rect.x, rect.y);
        }

        if (clip != null)
            g.setClip(clip.x, clip.y, clip.width, clip.height);
    }

    /**
     * Determines the preferred span for this view along an axis.
     */
    public float getPreferredSpan(int axis) {
        if (axis == View.X_AXIS)
            return width;
        return height;
    }

    /**
     * Does nothing. Has to be here though.
     */
    public void setSize(float width, float height) {
    }
}
