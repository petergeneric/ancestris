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

import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.renderer.BlueprintEditor;
import genj.renderer.BlueprintRenderer;
import static genj.renderer.DPI.get;
import genj.renderer.MediaRenderer;
import static genj.renderer.MediaRenderer.getSize;
import genj.util.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import static java.util.logging.Level.FINER;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.swing.text.Element;

/**
 * A view that renders available media
 *
 * @author zurga
 */
public class MediaView extends MyView {
    /**
     * Logger.
     */
    private final static Logger LOG = getLogger("ancestris.renderer");
    
    /**
     * Image Icon for broken link.
     */
    private final static ImageIcon BROKEN = new ImageIcon(BlueprintEditor.class, "Broken.png");

    /**
     * Path to root element.
     */
    private TagPath path2root = null;

    /**
     * Constructor
     */
    public MediaView(Element elem, BlueprintRenderer rdr) {
        super(elem, rdr);

        Object p = elem.getAttributes().getAttribute("path");
        if (p != null) {
            try {
                path2root = new TagPath((String) p);
            } catch (IllegalArgumentException e) {
                if (LOG.isLoggable(FINER)) {
                    LOG.log(FINER, "got wrong path {0}", p);
                }
            }
        }
    }

    private Property getRoot() {
        Property result = null;
        if (path2root != null) {
            result = getRenderer().getEntity().getProperty(path2root);
        }
        return result != null ? result : getRenderer().getEntity();
    }

    @Override
    protected Dimension2D getPreferredSpan() {
        Dimension2D size = getSize(getRoot());
        if (getRenderer().isIsDebug() && size.getWidth() == 0 && size.getHeight() == 0) {
            return BROKEN.getSizeInPoints(get(getRenderer().getGraphics()));
        }
        return size;
    }

    @Override
    public void paint(Graphics g, Shape allocation) {

        Rectangle r = allocation.getBounds();

        if (getRenderer().isIsDebug()) {
            Dimension2D size = getSize(getRoot());
            if (size.getWidth() == 0 && size.getHeight() == 0) {
                BROKEN.paintIcon(g, r.x, r.y);
                return;
            }
        }
        MediaRenderer.render(g, r, getRoot());
    }

}
