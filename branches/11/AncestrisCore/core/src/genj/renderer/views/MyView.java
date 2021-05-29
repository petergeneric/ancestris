/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.renderer.views;

import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.renderer.BlueprintRenderer;
import genj.util.Dimension2d;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;
import static java.lang.Integer.parseInt;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import static javax.swing.text.View.BadBreakWeight;
import static javax.swing.text.View.GoodBreakWeight;
import static javax.swing.text.View.X_AXIS;
import static javax.swing.text.View.Y_AXIS;
import javax.swing.text.ViewFactory;

/**
 * Abstract view for the graphical tree.
 *
 * @author zurga
 */
public abstract class MyView extends View {
    /**
     * Logger.
     */
    private final static Logger LOG = getLogger("ancestris.renderer");

    /**
     * element
     */
    private Element element;

    /**
     * the cached font we're using
     */
    private Font font = null;

    /**
     * the cached foreground we're using
     */
    private Color foreground = null;

    /**
     * the cached preferred span
     */
    private Dimension2D preferredSpan = null;

    /**
     * max span percent 0-100
     */
    private int maxAtt = 0;
    
    /**
     * Renderer of the view.
     */
    private BlueprintRenderer renderer;

    /**
     * Constructor
     */
    public MyView(Element elem, BlueprintRenderer rdr) {
        super(elem);
        element = elem;
        renderer = rdr;

        // minimum?
        try {
           String att = (String) elem.getAttributes().getAttribute("max");
            if (att != null) {
                maxAtt = parseInt(att);
            }
        } catch (NumberFormatException t) {
            LOG.log(Level.FINER, "Error with maxAtt conversion", t);
        }
    }

    /**
     * @see javax.swing.text.View#viewToModel(float, float, Shape, Bias[])
     */
    @Override
    public int viewToModel(float arg0, float arg1, Shape arg2, Position.Bias[] arg3) {
        throw new RuntimeException("viewToModel() is not supported");
    }

    /**
     * @see javax.swing.text.View#modelToView(int, Shape, Bias)
     */
    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        throw new RuntimeException("modelToView() is not supported");
    }

    /**
     * @see javax.swing.text.View#getBreakWeight(int, float, float)
     */
    @Override
    public int getBreakWeight(int axis, float pos, float len) {
        // not on vertical
        if (axis == Y_AXIS) {
            return BadBreakWeight;
        }
        // horizontal might work after our content
        if (len > getPreferredSpan(X_AXIS)) {
            return GoodBreakWeight;
        }
        return BadBreakWeight;
    }

    /**
     * @see javax.swing.text.View#breakView(int, int, float, float)
     */
    @Override
    public View breakView(int axis, int offset, float pos, float len) {
        return this;
    }

    /**
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    @Override
    public float getPreferredSpan(int axis) {
        // check cached preferred Span
        if (preferredSpan == null) {
            preferredSpan = getPreferredSpan();

            if (maxAtt > 0) {
                double maxWidth = renderer.getRoot().getWidth() * maxAtt / 100;
                if (preferredSpan.getWidth() > maxWidth) {
                    Object p = element.getAttributes().getAttribute("path");
                    Property prop = null;
                    if (p != null) {
                        prop = renderer.getProperty(renderer.getEntity(), new TagPath((String) p));
                    } else {
                        p = element.getAttributes().getAttribute("default");
                        if (p != null) {
                            prop = renderer.getProperty(renderer.getEntity(), new TagPath((String) p));
                        }
                    }
                    if ("media".equals(element.getName()) || prop instanceof PropertyFile || prop instanceof PropertyBlob) {
                        preferredSpan = new Dimension2d(maxWidth, preferredSpan.getHeight() * maxWidth / preferredSpan.getWidth());
                    } else {
                        preferredSpan = new Dimension2d(maxWidth, preferredSpan.getHeight());
                    }
                }
            }
        }
        return (float) (axis == X_AXIS ? preferredSpan.getWidth() : preferredSpan.getHeight());
    }

    @Override
    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    /**
     * @see javax.swing.text.View#getMaximumSpan(int)
     */
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    /**
     * @see javax.swing.text.View#getAlignment(int)
     */
    public float getAlignment(int axis) {
        // horizontal unchanged
        if (X_AXIS == axis) {
            return super.getAlignment(axis);
        }
        // height we prefer
        float height = (float) getPreferredSpan().getHeight();
        // where's first line's baseline
        FontMetrics fm = getGraphics().getFontMetrics();
        float h = fm.getHeight();
        float d = fm.getDescent();
        return (h - d) / height;
    }

    @Override
    public Graphics getGraphics() {
        renderer.getGraphics().setFont(getFont());
        return renderer.getGraphics();
    }

    /**
     * Returns the current fg color
     */
    protected Color getForeground() {

        // we cached the color so that it's retrieved only once
        // instead of using this view's attributes we get the
        // document's stylesheet and ask it for this view's
        // attributes
        if (foreground == null) {
            //foreground = doc.getForeground(getAttributes());
            foreground = renderer.getDoc().getForeground(renderer.getDoc().getStyleSheet().getViewAttributes(this));
        }

        return foreground;
    }

    /**
     * Returns the current font
     */
    protected Font getFont() {
        // we cached the font so that it's retrieved only once
        // instead of using this view's attributes we get the
        // document's stylesheet and ask it for this view's
        // attributes
        if (font == null) {
            font = renderer.getDoc().getFont(renderer.getDoc().getStyleSheet().getViewAttributes(this));
        }
        return font;
    }

    /**
     * Get the preferred span
     */
    protected abstract Dimension2D getPreferredSpan();

    /**
     * Invalidates this views current state
     */
    public void invalidate() {
        // invalidate state
        preferredSpan = null;
        font = null;
        // signal preference change through super
        super.preferenceChanged(this, true, true);
    }

    /**
     * we use our kit's view factory
     */
    @Override
    public ViewFactory getViewFactory() {
        return renderer.getFactory();
    }

    protected void render(String txt, Graphics2D g, Rectangle r) {

        // check for empty string
        if (txt.length() == 0) {
            return;
        }

        // prepare layout
        TextLayout layout = new TextLayout(txt, g.getFont(), g.getFontRenderContext());

        // draw it
        layout.draw(g, (float) r.getX(), (float) r.getY() + layout.getAscent());
    }

    /**
     * Getter for subclass.
     * @return  the maxAtt
     */
    protected int getMaxAtt() {
        return maxAtt;
    }

    /**
     * Getter of the renderer for subclass
     * @return the Renderer
     */
    protected BlueprintRenderer getRenderer() {
        return renderer;
    }


} //MyView
