/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.renderer;

import genj.gedcom.Property;
import genj.gedcom.TagPath;
import static genj.renderer.DPI.get;
import genj.renderer.views.I18NView;
import genj.renderer.views.MarkView;
import genj.renderer.views.MediaView;
import genj.renderer.views.MyView;
import genj.renderer.views.PropertyView;
import genj.renderer.views.RootView;
import static genj.util.EnvironmentChecker.isWindows;
import java.awt.Font;
import static java.awt.Font.BOLD;
import static java.awt.Font.ITALIC;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import static javax.swing.text.StyleConstants.FontSize;
import static javax.swing.text.StyleConstants.getFontSize;
import static javax.swing.text.StyleConstants.isBold;
import static javax.swing.text.StyleConstants.isItalic;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;
import javax.swing.text.html.parser.DTD;
import static javax.swing.text.html.parser.DTD.getDTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * A renderer for entities - blueprint necessary.
 */
public class BlueprintRenderer {

    private final static Logger LOG = getLogger("ancestris.renderer");

    /**
     * a cached dtd for html
     */
    private static final DTD dtd = null;

    // this will initialize and load the html32 dtd
    // "/javax/swing/text/html/parser/html32.bdtd"
    static {
        new ParserDelegator();
    }

    /**
     * the root of our rendering view
     */
    private final RootView root;

    /**
     * the document we're looking at
     */
    private HTMLDocument doc = new MyHTMLDocument();

    /**
     * the factory we're using to create views
     */
    private MyHTMLFactory factory = new MyHTMLFactory();

    /**
     * the entity we're looking at
     */
    private Property entity;

    /**
     * all views that need to be invalidated per repaint
     */
    private List<MyView> volatileViews = new ArrayList<>(64);

    /**
     * all TableViews we know
     */
    private List<View> tableViews = new ArrayList<>(4);

    /**
     * whether we have a debug mode
     */
    private boolean isDebug = false;

    /**
     * current graphics context
     */
    private Graphics2D graphics;

    private Font plain, bold, italic, bolditalic;

    /**
     * Constructor.
     *
     * @param bp Blueprint to render.
     */
    public BlueprintRenderer(Blueprint bp) {

        // we wrap the html in html/body
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append(bp.getHTML());
        html.append("</body></html>");

        // read and parse the html
        try {

            // I started out to use a HTMLEditorkit for reading the html into the document
            //  new HTMLEditorKit().read(new StringReader(html), doc, 0);
            // but this won't allow me to fix a problem with HTMLDocument.HTMLReader 
            // so I'm using ParserDelegator directly allowing me to use MyHTMLReader
            //  new ParserDelegator().parse(new StringReader(html), new HTMLReader(doc), false);
            // but I also want to override the DocumentParser which is instantiated
            // inside ParserDelegator.
            //  new DocumentParser(dtd).parse(r, cb, ignoreCharSet);
            // so I'm reading the dtd myself and creat my own DocumentParser
            // .. we need out own html reader (javax.swing.text.html.HTMLDocument.HTMLReader)
            MyHTMLReader reader = new MyHTMLReader(doc);

            // .. trigger parsing (we want our own parser here so there's a little bit more magic at this point)
            new MyDocumentParser(getDTD("html32")).parse(new StringReader(html.toString()), reader, false);

            // .. flush reader
            reader.flush();

        } catch (IOException | BadLocationException t) {
            getLogger("ancestris.renderer").log(WARNING, "can't parse blueprint " + bp, t);
        }

        // create the root view
        root = new RootView(factory.create(doc.getDefaultRootElement()), this);

        // done    
    }

    /**
     * Render the entity on given context.
     *
     * @param g the graphic
     * @param e the property
     * @param r the rectangle
     */
    public void render(Graphics g, Property e, Rectangle r) {

        // keep the entity and graphics
        entity = e;
        graphics = (Graphics2D) g;

        // see http://www.3rd-evolution.de/tkrammer/docs/java_font_size.html
        // While Java assumes 72 dpi screen resolution Windows uses 96 dpi or 120 dpi depending on your font size setting in the display properties. 
        Font font = g.getFont();
        // Uniquement pour windows
        if (isWindows()) {
            float factor = get(graphics).vertical() / 72F;
            font = font.deriveFont(factor * font.getSize2D());
        }
        this.plain = font;
        this.bold = font.deriveFont(BOLD);
        this.italic = font.deriveFont(ITALIC);
        this.bolditalic = font.deriveFont(BOLD + ITALIC);

        try {

            // invalidate views
            volatileViews.forEach((pv) -> {
                {
                    pv.invalidate();
                }
            });
            // and make sure TableView's update their grid
            tableViews.forEach((tv) -> {
                // this will cause invalidateGrid on a javax.swing.text.html.TableView
                tv.replace(0, 0, null);
            });

            // set the size of root - this triggers a layout of the views
            root.setSize((float) r.getWidth(), (float) r.getHeight());

            // clip and paint it
            Rectangle oc = g.getClipBounds();
            g.clipRect(r.x, r.y, r.width, r.height);
            try {
                root.paint(g, r);
            } finally {
                g.setClip(oc.x, oc.y, oc.width, oc.height);
            }

        } catch (Throwable t) {
            LOG.log(WARNING, "can't render", t);
        }
        // done
    }

    /**
     * Sets debug mode
     *
     * @param set
     */
    public void setDebug(boolean set) {
        isDebug = set;
    }

    /**
     * Default implementation to lookup property from entity.
     *
     * @param entity The entity to lookup
     * @param path The path of the property
     * @return property value
     */
    public Property getProperty(Property entity, TagPath path) {
        return entity.getProperty(path);
    }
    
    /**
     * Getter.
     *
     * @return the rootView.
     */
    public RootView getRoot() {
        return root;
    }

    /**
     * Getter.
     *
     * @return the Doc.
     */
    public HTMLDocument getDoc() {
        return doc;
    }

    /**
     * Getter.
     *
     * @return The graphics
     */
    public Graphics2D getGraphics() {
        return graphics;
    }

    /**
     * Getter.
     *
     * @return The factory
     */
    public ViewFactory getFactory() {
        return factory;
    }

    /**
     * Getter.
     *
     * @return The Entity
     */
    public Property getEntity() {
        return entity;
    }

    /**
     * Getter.
     *
     * @return debug state
     */
    public boolean isIsDebug() {
        return isDebug;
    }

    /**
     * I've created my own subclass of HTMLDocument.HTMLReader a ParserCallback
     * to achieve some special behaviour overriding protected method:
     *
     * @see HTMLDocument.HTMLReader.blockClose(Tag t)
     */
    private static class MyHTMLReader extends HTMLDocument.HTMLReader {

        /**
         * whether we ignore content
         */
        private boolean skipContent = false;

        /**
         * Constructor.
         */
        protected MyHTMLReader(HTMLDocument doc) {
            doc.super(0);
        }

        /**
         * In the original HTMLReader this will add a newline to the end of a
         * block (if there was no newline already). For tables this means that
         * there will be a InlineView (\n) that might flow into a separate
         * *empty* line
         * <pre>
         *  if(!lastWasNewline) {
         *   addContent(NEWLINE, 0, 1, true);
         *   lastWasNewline = true;
         *  }
         * </pre>
         *
         * @see
         * javax.swing.text.html.HTMLDocument.HTMLReader#blockClose(javax.swing.text.html.HTML.Tag)
         */
        @Override
        protected void blockClose(Tag t) {
            // mark that we skip anything that might be added to content
            // in super.blockClose(). The super class' implementation
            // adds trailing \n's before a block-close. They tend to
            // flow into the next line resulting in empty full-height
            // lines when horizontal space is restricted :(
            skipContent = true;
            // delegate to super
            super.blockClose(t);
            // back to accepting content
            skipContent = false;
        }

        /**
         * @see javax.swing.text.html.HTMLDocument.HTMLReader#addContent(char,
         * int, int, boolean)
         */
        @Override
        protected void addContent(char[] data, int offs, int length, boolean generateImpliedPIfNecessary) {
            if (!skipContent) {
                super.addContent(data, offs, length, generateImpliedPIfNecessary);
            }
        }
    } //MyHTMLReader

    /**
     * Our own HTMLDocument
     */
    private class MyHTMLDocument extends HTMLDocument {

        /**
         * @see
         * javax.swing.text.DefaultStyledDocument#getFont(javax.swing.text.AttributeSet)
         */
        @Override
        public Font getFont(AttributeSet attr) {

            Font result = plain;
            if (isBold(attr) && isItalic(attr)) {
                result = bolditalic;
            } else if (isBold(attr)) {
                result = bold;
            } else if (isItalic(attr)) {
                result = italic;
            }
            if (attr.isDefined(FontSize)) {
                result = result.deriveFont(((float) getFontSize(attr)));
            }
            return result;
        }
    } //MyHTMLDocument

    /**
     * My own parser that overrides the original's property
     * <pre>strict=true</pre> - this will make the underlying parser not skip
     * spaces after close-tags
     */
    private class MyDocumentParser extends DocumentParser {

        /**
         * Constructor
         */
        private MyDocumentParser(DTD dtd) {
            super(dtd);
            // patch strictness
            strict = true;
            // done      
        }
    } //MyDocumentParser

    /**
     * My own HTMLFactory that extends the default one to support tags
     * <i>prop</i> and <i>i18n</i>
     */
    private class MyHTMLFactory extends HTMLFactory {

        /**
         * @see javax.swing.text.ViewFactory#create(Element)
         */
        @Override
        public View create(Element elem) {

            String name = elem.getName();

            // check if the element is "prop"
            if ("prop".equals(name) || "ifvalue".equals(name)) {
                final PropertyView result = new PropertyView(elem, BlueprintRenderer.this);
                volatileViews.add(result);
                return result;
            }
            
            // check if the element is "mark"
            if ("mark".equals(name)) {
                final MarkView result = new MarkView(elem, BlueprintRenderer.this);
                volatileViews.add(result);
                return result;
            }

            // maybe its "name" or "i18n"
            if ("name".equals(name) || "i18n".equals(name)) {
                return new I18NView(elem, BlueprintRenderer.this);
            }

            // a media item?
            if ("media".equals(name)) {
                final MediaView result = new MediaView(elem, BlueprintRenderer.this);
                volatileViews.add(result);
                return result;
            }

            // default to super
            View result = super.create(elem);

            // .. keep track of TableViews for later dynamic invalidation
            if ("table".equals(elem.getName())) {
                tableViews.add(result);
            }
            return result;
        }

    } //ModifiedHTMLFactory
}
