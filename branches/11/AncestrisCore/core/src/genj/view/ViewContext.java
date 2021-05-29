/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.view;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.util.swing.ImageIcon;
import java.awt.Color;
import java.util.List;
import javax.swing.Action;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom an entity and a property
 */
public class ViewContext extends Context implements Comparable<ViewContext> {

    private ImageIcon img = null;
    private String txt = null;
    private String code = null;
    private Action action = null;
    private Color txtColor = null;

    /**
     * Constructor
     */
    public ViewContext(String text, Context context) {
        super(context);
        setText(text);
    }

    /**
     * Constructor
     */
    public ViewContext(String text, ImageIcon img, Context context) {
        super(context);
        setText(text);
        setImage(img);
    }

    /**
     * Constructor
     */
    public ViewContext(Context context) {
        super(context);
    }

    /**
     * Constructor
     */
    public ViewContext(Gedcom gedcom, List<Entity> entities, List<Property> properties) {
        super(gedcom, entities, properties);
    }

    /**
     * Constructor
     */
    public ViewContext(Gedcom ged) {
        super(ged);
    }

    /**
     * Constructor
     */
    public ViewContext(Property prop) {
        super(prop);
    }

    /**
     * Constructor
     */
    public ViewContext(Entity entity) {
        super(entity);
    }

    /**
     * Accessor.
     * @return text content.
     */
    public final String getText() {

        if (txt != null) {
            return txt;
        }

        List<? extends Property> ps = getProperties();
        List<? extends Entity> es = getEntities();
        if (ps.size() == 1) {
            StringBuilder buf = new StringBuilder();
            Property p = ps.get(0);
            buf.append(p.getPropertyName());
            while (!(p.getParent() instanceof Entity)) {
                p = p.getParent();
                if (p instanceof PropertyEvent) {
                    buf.append("|");
                    buf.append(((PropertyEvent) p).getPropertyDisplayValue("DATE"));
                    break;
                }
            }
            buf.append("|");
            buf.append(p.getEntity());
            txt = buf.toString();
        } else if (!ps.isEmpty()) {
            txt = Property.getPropertyNames(ps, 5);
        } else if (es.size() == 1) {
            txt = es.get(0).toString();
        } else if (!es.isEmpty()) {
            txt = Entity.getPropertyNames(es, 5);
        } else {
            txt = getGedcom() == null ? null : getGedcom().getName();
        }

        return txt == null ? "" : txt;
    }

    /**
     * Accessor.
     * @param text Text to set
     * @return current ViewContext for fluent use.
     */
    public final ViewContext setText(String text) {
        txt = text;
        return this;
    }

    /**
     * Accessor
     */
    public String getCode() {
        return code;
    }

    /**
     * Accessor
     * @param txt Text to set
     * @return current ViewContext for fluent use.
     */
    public ViewContext setCode(String txt) {
        code = txt;
        return this;
    }

    /**
     * Accessor
     */
    public Action getAction() {
        return action;
    }

    /**
     * Accessor
     */
    public ViewContext setAction(Action set) {
        this.action = set;
        return this;
    }

    /**
     * Accessor
     */
    public Color getColor() {
        return txtColor;
    }

    /**
     * Accessor
     */
    public ViewContext setColor(Color set) {
        this.txtColor = set;
        return this;
    }

    /**
     * Accessor
     */
    public ImageIcon getImage() {
        // an override?
        if (img != null) {
            return img;
        }
        // check prop/entity/gedcom
        if (getProperties().size() == 1) {
            img = getProperties().get(0).getImage(false);
        } else if (getEntities().size() == 1) {
            img = getEntities().get(0).getImage(false);
        } else {
            img = Gedcom.getImage();
        }
        return img;
    }

    /**
     * Accessor
     */
    public ViewContext setImage(ImageIcon set) {
        img = set;
        return this;
    }

    /**
     * comparison
     */
    public int compareTo(ViewContext that) {
        if (this.txt == null) {
            return -1;
        }
        if (that.txt == null) {
            return 1;
        }
        return this.txt.compareTo(that.txt);
    }

} //Context
