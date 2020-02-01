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

import genj.renderer.BlueprintRenderer;
import java.awt.Graphics;
import java.awt.Shape;
import static java.util.logging.Level.FINE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * RootView onto a HTML Document
 *
 * @author zurga
 */
public class RootView extends View {

    /**
     * Logger.
     */
    private final static Logger LOG = getLogger("ancestris.renderer");

    /**
     * the root of the html's view hierarchy
     */
    private final View view;

    /**
     * the size of the root view
     */
    private float width, height;

    /**
     * Renderer of the view.
     */
    private final BlueprintRenderer renderer;

    /**
     * Constructor
     */
    public RootView(View view, BlueprintRenderer rdr) {

        // block super
        super(null);

        renderer = rdr;

        // keep view
        this.view = view;
        view.setParent(this);

        // done
    }

    @Override
    public float getPreferredSpan(int axis) {
        throw new RuntimeException("getPreferredSpan() is not supported");
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
     * we don't have any attributes
     */
    @Override
    public AttributeSet getAttributes() {
        return null;
    }

    /**
     * we let the wrapped view do the painting
     */
    @Override
    public void paint(Graphics g, Shape allocation) {
        view.paint(g, allocation);
    }

    /**
     * our document is the parsed html'
     */
    @Override
    public Document getDocument() {
        return renderer.getDoc();
    }

    @Override
    public Graphics getGraphics() {
        return renderer.getGraphics();
    }

    /**
     * the wrapped view needs to be sized
     */
    @Override
    public void setSize(float wIdth, float heIght) {
        // remember
        width = wIdth;
        height = heIght;
        // delegate
        try {
            view.setSize(width, height);
        } catch (Throwable t) {
            LOG.log(FINE, "unexpected", t);
        }
        // done
        // done
    }

    /**
     * we use our kit's view factory
     */
    @Override
    public ViewFactory getViewFactory() {
        return renderer.getFactory();
    }

    /**
     * Getter.
     * @return The Width 
     */
    public float getWidth() {
        return width;
    }

    /**
     * Getter.
     * @return The Height 
     */
    public float getHeight() {
        return height;
    }
}
