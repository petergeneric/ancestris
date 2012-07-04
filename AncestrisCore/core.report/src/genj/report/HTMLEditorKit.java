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

import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;

/**
 * Our own implementation to use ClassLoaderImageView.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class HTMLEditorKit extends javax.swing.text.html.HTMLEditorKit {

    /**
     * Factory to create views.
     */
    private ViewFactory factory = new HTMLFactory();

    /**
     * Class to use when loading with classloader.
     */
    private Class from;

    /**
     * Constructs the object
     * @param from class to use when loading with classloader
     */
    public HTMLEditorKit(Class from) {
        this.from = from;
    }

    /**
     * Sets the class to use when loading with classloader
     */
    public void setFrom(Class from) {
        this.from = from;
    }

    /**
     * Fetch a factory that is suitable for producing views of any models that
     * are produced by this kit.
     *
     * @return the factory
     */
    public ViewFactory getViewFactory() {
        return factory;
    }

    /**
     * Factory to create views.
     */
    private class HTMLFactory extends
            javax.swing.text.html.HTMLEditorKit.HTMLFactory {

        /**
         * Creates a view from an element. If it's an IMG tag then use our
         * ClassLoaderImageView.
         *
         * @param elem the element
         * @return the view
         */
        public View create(Element elem) {
            Object o = elem.getAttributes().getAttribute(
                    StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag) {
                HTML.Tag kind = (HTML.Tag) o;
                if (kind == HTML.Tag.IMG)
                    return new ClassLoaderImageView(elem, from);
            }
            return super.create(elem);
        }
    }
}
