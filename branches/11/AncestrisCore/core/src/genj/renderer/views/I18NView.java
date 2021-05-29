/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.renderer.views;

import static genj.gedcom.Gedcom.getName;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.renderer.BlueprintRenderer;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import javax.swing.text.Element;

/**
 * Internationalization view. A view for translating text.
 *
 * @author zurga
 */
public class I18NView extends MyView {

    /**
     * the text to paint
     */
    private String txt = "?";
    private TagPath path = null;

    /**
     * Constructor
     */
    public I18NView(Element elem, BlueprintRenderer rdr) {
        super(elem, rdr);
        // resolve and localize text .. tag|entity
        Object o = elem.getAttributes().getAttribute("tag");
        if (o != null) {
            txt = getName(o.toString());
        } else if ((o = elem.getAttributes().getAttribute("path")) != null) {
            path = new TagPath(o.toString());
        } else {
            o = elem.getAttributes().getAttribute("entity");
            if (o != null) {
                txt = getName(o.toString());
            }
        }
        // done
    }

    /**
     * @see javax.swing.text.View#paint(java.awt.Graphics, java.awt.Shape)
     */
    @Override
    public void paint(Graphics g, Shape allocation) {
        Rectangle r = (allocation instanceof Rectangle) ? (Rectangle) allocation : allocation.getBounds();
        g.setFont(getFont());
        g.setColor(getForeground());
        render(getRenderedText(), (Graphics2D) g, r);
    }

    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
     */
    @Override
    protected Dimension2D getPreferredSpan() {
        FontMetrics fm = getRenderer().getGraphics().getFontMetrics(getFont());
        return new Dimension(
                fm.stringWidth(getRenderedText()),
                fm.getAscent() + fm.getDescent()
        );
    }

    private String getRenderedText() {
        if (path != null) {
            final Property prop = getRenderer().getEntity().getProperty(path);
            String text = prop.getTag();
            if ("EVEN".equals(text)) {
                text = prop.getPropertyValue("TYPE");
                if (text.isEmpty()) {
                    text = getName("EVEN");
                }
                return text;
            }
            return getName(prop.getTag());
        }
        return txt;
    }

} //LocalizeView

