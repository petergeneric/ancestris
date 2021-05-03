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

import static ancestris.util.Utilities.html2text;
import genj.gedcom.Entity;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.renderer.BlueprintRenderer;
import genj.renderer.MediaRenderer;
import genj.util.Dimension2d;
import genj.util.swing.ImageIcon;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import java.awt.geom.Dimension2D;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.swing.text.Element;

/**
 * A view that wraps a property and its value.
 *
 * @author forhan
 */
public class PropertyView extends MyView {

    /**
     * Logger.
     */
    private final static Logger LOG = getLogger("ancestris.renderer");

    private final static Stroke DEBUG_STROKE = new BasicStroke(1.0f, CAP_SQUARE, JOIN_MITER, 10.0f, new float[]{1, 2}, 0.0f);

    private final static String STARS = "*****";

    private final static int IMAGE_GAP = 4;

    /**
     * Hint for text.
     */
    private static final String HINT_KEY_TXT = "txt";

    /**
     * Hint for image.
     */
    private static final String HINT_KEY_IMG = "img";

    /**
     * Hint for short.
     */
    private static final String HINT_KEY_SHORT = "short";

    /**
     * Hint for true.
     */
    private static final String HINT_VALUE_TRUE = "yes";

    /**
     * Hint for false.
     */
    private static final String HINT_VALUE_FALSE = "no";

    /**
     * configuration
     */
    private final Map<String, String> attributes;
    private String path = null;
    private String elsePath = null;

    /**
     * cached information
     */
    private Property cachedProperty = null;
    private Dimension2D cachedSize = null;

    /**
     * Constructor
     */
    public PropertyView(Element elem, BlueprintRenderer rdr) {
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

        // grab default path
        elsePath = attributes.get("default");

    }

    /**
     * Get Property
     */
    private Property getProperty(String thePath) {
        // still looking for property?
        if (cachedProperty != null) {
            return cachedProperty;
        }

        final Property entity = getRenderer().getEntity();

        if (entity == null || thePath == null || thePath.isEmpty()) {
            return null;
        }

        try {
            Property p = entity.getProperty(createPath(thePath));
            if (p == null) {
                return null;
            }
            cachedProperty = p;
        } catch (NullPointerException e) {
            return null;
        }
        //   }
        return cachedProperty;
    }

    private TagPath createPath(String thePath) {
        try {
            return new TagPath(thePath, true);
        } catch (IllegalArgumentException e) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "got wrong path {0}", thePath);
            }
        }
        return null;
    }

    private Property getProperty() {
        Property prop = getProperty(path);
        if (prop == null) {
            prop = getProperty(elsePath);
        }
        return prop;
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
        Color fg = super.getForeground();

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
        render(prop, graphics, r);
        g.setClip(old);

        // done
    }

    private void render(Property prop, Graphics2D g, Rectangle r) {

        // entities (specifically NOTE) can be a multi-line - let fall through
        if (!(prop instanceof Entity) && prop instanceof MultiLineProperty) {
            render((MultiLineProperty) prop, g, r);
            return;
        }
        if (prop instanceof PropertyFile || prop instanceof PropertyBlob) {
            MediaRenderer.render(g, r, prop);
            return;
        }
        // image?
        if (HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_IMG))) {
            //XXX: prop or prop.getParent may be null. We must handle this before prop is rendered
            if (prop != null && prop.getParent() != null) {
                render(prop instanceof PropertyDate ? prop.getParent().getImage(false) : prop.getImage(false), g, r);
            }
        }
        // text?
        if (!HINT_VALUE_FALSE.equals(attributes.get(HINT_KEY_TXT))) {
            render(getText(prop), g, r);
        }
    }

    private void render(MultiLineProperty mle, Graphics2D g, Rectangle r) {
        // get lines
        MultiLineProperty.Iterator line = mle.getLineIterator();

        // paint
        Graphics2D graphics = g;
        Font font = g.getFont();
        FontRenderContext context = graphics.getFontRenderContext();

        float x = (float) r.getX(),
                y = (float) r.getY();

        do {

            // analyze line
            String txt = line.getValue();
            LineMetrics lm = font.getLineMetrics(txt, context);
            y += lm.getHeight();

            // draw line
            graphics.drawString(txt, x, y - lm.getDescent());

            // .. break if line doesn't fit anymore
            if (y > r.getMaxY()) {
                break;
            }

        } while (line.next());

        // done
    }

    private void render(ImageIcon img, Graphics2D g, Rectangle bounds) {

        // no space?
        if (bounds.getHeight() == 0 || bounds.getWidth() == 0) {
            return;
        }

        // draw image with maximum height of a character
        int w = img.getIconWidth(),
                max = g.getFontMetrics().getHeight();

        AffineTransform at = getTranslateInstance(bounds.getX(), bounds.getY());
        if (max < img.getIconHeight()) {
            float scale = max / (float) img.getIconHeight();
            at.scale(scale, scale);
            w = (int) ceil(w * scale);
        }
        g.drawImage(img.getImage(), at, null);

        // patch bounds for skip
        bounds.x += w + IMAGE_GAP;
        bounds.width -= w + IMAGE_GAP;
    }

    private String getText(Property prop) {
        if (prop instanceof Entity) {
            return getText((Entity) prop);
        }
        if (prop.isPrivate()) {
            return STARS;
        }
        if (prop instanceof PropertyPlace) {
            return getText((PropertyPlace) prop);
        }
        if (prop instanceof PropertySex) {
            return getText((PropertySex) prop);
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

    private String getText(PropertySex sex) {

        if (!attributes.containsKey(HINT_KEY_TXT)) {
            attributes.put(HINT_KEY_TXT, HINT_VALUE_FALSE);
        }
        if (!attributes.containsKey(HINT_KEY_IMG)) {
            attributes.put(HINT_KEY_IMG, HINT_VALUE_TRUE);
        }

        String result = sex.getDisplayValue();
        if (result.length() > 0 && HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_SHORT))) {
            result = result.substring(0, 1);
        }
        return result;
    }

    private String getText(PropertyPlace place) {
        return html2text(place.format(attributes.get("format")));
    }

    /**
     * @see genj.renderer.EntityRenderer.MyView#getPreferredSpan()
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

    private Dimension2D getSize() {
        Property prop = getProperty();
        if (prop == null) {
            return new Dimension();
        }

        if (!(prop instanceof Entity) && prop instanceof MultiLineProperty) {
            return getSize((MultiLineProperty) prop);
        }
        if (prop instanceof PropertyFile || prop instanceof PropertyBlob) {
            return MediaRenderer.getSize(prop);
        }
        return getSize(prop);
    }

    private Dimension2D getSize(Property prop) {

        String txt = getText(prop);

        double w = 0,
                h = 0;

        // calculate text size (the default size we use)
        getRenderer().getGraphics().setFont(super.getFont());
        FontMetrics fm = getRenderer().getGraphics().getFontMetrics();
        if (!HINT_VALUE_FALSE.equals(attributes.get(HINT_KEY_TXT)) && txt.length() > 0) {
            w += fm.stringWidth(txt);
            h = max(h, fm.getAscent() + fm.getDescent());
        }
        // add image size
        if (HINT_VALUE_TRUE.equals(attributes.get(HINT_KEY_IMG))) {
            ImageIcon img = prop.getImage(false);
            float max = fm.getHeight();
            float scale = 1F;
            if (max < img.getIconHeight()) {
                scale = max / img.getIconHeight();
            }
            w += (int) ceil(img.getIconWidth() * scale) + IMAGE_GAP;
            h = max(h, fm.getHeight());
        }

        // done
        return new Dimension2d(w, h);
    }

    private Dimension2D getSize(MultiLineProperty mle) {

        // count 'em
        getRenderer().getGraphics().setFont(super.getFont());
        FontMetrics fm = getRenderer().getGraphics().getFontMetrics();
        double width = 0;
        double height = 0;
        MultiLineProperty.Iterator line = mle.getLineIterator();
        do {
            width = max(width, fm.stringWidth(line.getValue()));
            height += fm.getHeight();
        } while (line.next());

        // done
        return new Dimension2d(width, height);
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
} //PropertyView
