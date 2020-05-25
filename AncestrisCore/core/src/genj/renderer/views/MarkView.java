/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Zurga.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.renderer.views;

import static ancestris.util.Utilities.html2text;
import genj.gedcom.Entity;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.renderer.BlueprintRenderer;
import genj.util.Dimension2d;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import static java.lang.Math.max;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.swing.text.Element;

/**
 * Specific view for mark tag.
 *
 * @author Zurga
 */
public class MarkView extends MyView {

    /**
     * Logger.
     */
    private final static Logger LOG = getLogger("ancestris.renderer");

    private final static Stroke DEBUG_STROKE = new BasicStroke(1.0f, CAP_SQUARE, JOIN_MITER, 10.0f, new float[]{1, 2}, 0.0f);

    private final static String POS_SEPA = "?";

    private static final String MARKER = "\u2588";

    /**
     * configuration
     */
    private final Map<String, String> attributes;
    private String path = null;
    private String repetition = null;
    private String value = null;
    private Integer pos;
    private String displayValue;

    /**
     * cached information
     */
    private Property cachedProperty = null;
    private Dimension2D cachedSize = null;

    /**
     * Constructor.
     *
     * @param elem element
     * @param rdr renderer.
     */
    public MarkView(Element elem, BlueprintRenderer rdr) {
        super(elem, rdr);

        // prepare attributes
        attributes = new HashMap<>();

        for (Enumeration<?> as = elem.getAttributes().getAttributeNames(); as.hasMoreElements();) {
            Object key = as.nextElement();
            if (key instanceof String) {
                attributes.put((String) key, (String) elem.getAttributes().getAttribute(key));
            }
        }

        // grab path
        path = attributes.get("path");

        //grab repetition
        repetition = attributes.get("repeat");

        // grab compare value
        value = attributes.get("test");

    }
    
     /**
      * Get the preferred span.
      * @return the span.
      */
    @Override
    protected Dimension2D getPreferredSpan() {
        // cached?
        if (cachedSize != null) {
            return cachedSize;
        }
        // check property
        cachedSize = getSize();
        return cachedSize;
    }

    /**
     * Get Property
     */
    private Property getProperty() {
        // still looking for property?
        if (cachedProperty != null) {
            return cachedProperty;
        }

        final Property entity = getRenderer().getEntity();

        if (entity == null || path == null || path.isEmpty()) {
            return null;
        }

        TagPath pathBefore = null;
        String pathAfter = null;
        if (path.contains(POS_SEPA)) {
            int sepa = path.indexOf(POS_SEPA);
            pathBefore = createPath(path.substring(0, sepa));
            int next = path.indexOf(TagPath.SEPARATOR, sepa);
            try {
                if (next != -1) {
                    pos = Integer.valueOf(path.substring(sepa + 1, next));
                    pathAfter = path.substring(next + 1);
                } else {
                    pos = Integer.valueOf(path.substring(sepa + 1));
                }
            } catch (NumberFormatException e) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Unable to retrieve pos {0}", path);
                }
            }
        }

        if (pos != null) {
            final Property[] props = entity.getProperties(pathBefore);
            // User always use 1 for the first occurrence
            final Integer thePos = pos - 1 >= 0 ? pos - 1 : 0;

            // Check if there is several values.
            if (props.length > thePos) {
                cachedProperty = props[thePos];
            } else {
                return null;
            }
            //Get end of tag, place at current entity tag.
            if (pathAfter != null) {
                cachedProperty = cachedProperty.getProperty(createPath(cachedProperty.getTag() + TagPath.SEPARATOR_STRING + pathAfter));
            }

        } else {
            cachedProperty = entity.getProperty(createPath(path));
        }
        return cachedProperty;
    }

    private TagPath createPath(String thePath) {
        try {
            return new TagPath(thePath);
        } catch (IllegalArgumentException e) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "got wrong path " + thePath, e);
            }
        }
        return null;
    }

    /**
     * @see javax.swing.text.View#paint(Graphics, Shape)
     */
    @Override
    public void paint(Graphics g, Shape allocation) {

        Property prop = getProperty();
        if (prop == null) {
            return;
        }

        Graphics2D graphics = (Graphics2D) g;

        // setup painting attributes and bounds
        Rectangle r = (allocation instanceof Rectangle) ? (Rectangle) allocation : allocation.getBounds();
        Color fg = getForeground();

        // debug?
        if (getRenderer().isIsDebug()) {
            Stroke stroke = graphics.getStroke();
            graphics.setStroke(DEBUG_STROKE);
            g.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 32));
            graphics.draw(r);
            graphics.setStroke(stroke);
        }

        // clip and render
        Shape old = graphics.getClip();
        graphics.clip(r);
        g.setColor(fg);
        g.setFont(super.getFont());
        render(createDisplayValue(prop), graphics, r);
        g.setClip(old);

        // done
    }
    
    private String createDisplayValue(Property prop) {
        StringBuilder display = new StringBuilder("");
        int nb = 1;
        if (repetition != null) {
            nb = Integer.parseInt(repetition);
        }
        String text = getText(prop);
        if (text != null) {
            for (int i = 0; i<nb;i++) {
                    display.append(MARKER);
                }
            if (value != null && !text.equals(value)){
                display = new StringBuilder("");
            }
        }
        displayValue = display.toString();
        return displayValue;
    }

    private String getText(Property prop) {
        if (prop instanceof Entity) {
            return getText((Entity) prop);
        }

        if (prop instanceof PropertyPlace) {
            return getText((PropertyPlace) prop);
        }

        if (prop instanceof MultiLineProperty) {
            return prop.getValue();
        }

        if (attributes.get("format") != null) {
            return prop.format(attributes.get("format"));
        }
        return prop.getDisplayValue();
    }

    private String getText(Entity entity) {
        if (attributes.get("format") != null) {
            return entity.format(attributes.get("format"));
        }
        return entity.getId();
    }

    private String getText(PropertyPlace place) {
        return html2text(place.format(attributes.get("format")));
    }


    private Dimension2D getSize() {
        Property prop = getProperty();
        if (prop == null) {
            return new Dimension();
        }
        if (displayValue == null) {
            displayValue = createDisplayValue(prop);
        }
        
        return getSize(displayValue);
    }

    private Dimension2D getSize(String txt) {
        double w = 0,
                h = 0;

        // calculate text size (the default size we use)
        getRenderer().getGraphics().setFont(super.getFont());
        FontMetrics fm = getRenderer().getGraphics().getFontMetrics();
        if (txt.length() > 0) {
            w += fm.stringWidth(txt);
            h = max(h, fm.getAscent() + fm.getDescent());
        }
        // done
        return new Dimension2d(w, h);
    }

    /**
     * Invalidates this views current state
     */
    @Override
    public void invalidate() {
        // invalidate cached information that's depending
        // on the current entity's properties
        cachedProperty = null;
        cachedSize = null;
        super.invalidate();
    }

}
