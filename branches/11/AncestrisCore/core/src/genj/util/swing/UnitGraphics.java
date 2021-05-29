/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
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
package genj.util.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Stack;

/**
 * A graphics wrapper that prepares for unit-based drawing on a scaled Graphics
 * object (wrapped)
 */
public class UnitGraphics {

    /**
     * stack of transformations
     */
    private Stack<AffineTransform> stackTransformations = new Stack<AffineTransform>();

    /**
     * a stack of push'd clips
     */
    private Stack<Shape> clipStack = new Stack<Shape>();

    /**
     * scale
     */
    private double unitx = 1, unity = 1;

    /**
     * the wrapped
     */
    private Graphics2D graphics;

    /**
     * a reusable line
     */
    private Line2D.Double line = new Line2D.Double();

    /**
     * Constructor
     */
    public UnitGraphics(Graphics g, double unitX, double unitY) {

        // remember wrapped
        graphics = (Graphics2D) g;

        // setup rendering hints
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // push one initial at
        pushTransformation();

        // remember scale
        unitx = unitX;
        unity = unitY;
    }

    /**
     * Get the unit
     */
    public Point2D getUnit() {
        return new Point2D.Double(unitx, unity);
    }

    /**
     * Antialiasing
     */
    public void setAntialiasing(boolean set) {
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                set ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF
        );
    }

    /**
     * Translation
     */
    public void translate(double dx, double dy) {
    // 20030423
        // when not casting this to int tiny offsets happen
        // in TreeView on context-mouse-clicks
        graphics.translate((int) (dx * unitx), (int) (dy * unity));
    }

    /**
     * Scaling
     */
    public void scale(double sx, double sy) {
        graphics.scale(sx * unitx, sy * unity);
    }

    /**
     * Access to the underlying Graphics
     */
    public Graphics2D getGraphics() {
        return graphics;
    }

    /**
     * Clipping
     */
    public Rectangle2D getClip() {
        Rectangle r = graphics.getClipBounds();
        return new Rectangle2D.Double(
                r.getMinX() / unitx,
                r.getMinY() / unity,
                r.getWidth() / unitx,
                r.getHeight() / unity
        );
    }

    /**
     * FontMetrics
     */
    public FontMetrics getFontMetrics() {
        return graphics.getFontMetrics();
    }

    /**
     * Color
     */
    public void setColor(Color color) {
        if (color != null) {
            graphics.setColor(color);
        }
    }

    /**
     * Font
     */
    public void setFont(Font font) {
        if (font != null) {
            graphics.setFont(font);
        }
    }

    /**
     * Draw a line
     */
    public void draw(double x1, double y1, double x2, double y2) {
        line.setLine(x1 * unitx, y1 * unity, x2 * unitx, y2 * unity);
        graphics.draw(line);
    }

    /**
     * Draw a dotted line
     */
    public void draw(double x1, double y1, double x2, double y2, Stroke stroke) {
        Stroke defaultStroke = graphics.getStroke();
        graphics.setStroke(stroke);
        draw(x1, y1, x2, y2);
        graphics.setStroke(defaultStroke);
    }

    /**
     * Draw text
     */
    public void draw(String str, double x, double y, double xalign, double yalign) {
        draw(str, x, y, xalign, yalign, 0, 0);
    }

    /**
     * Draw text
     */
    public void draw(String str, double x, double y, double xalign, double yalign, int dx, int dy) {
        draw (str, x, y, xalign, yalign, dx, dy, null, null);
    }

    /**
     * Draw text with different color background
     */
    public void draw(String str, double x, double y, double xalign, double yalign, int dx, int dy, Color fgColor, Color bgColor) {
        FontMetrics fm = graphics.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(str, graphics);
        LineMetrics lm = fm.getLineMetrics(str, graphics);

        float w = (float) r.getWidth(),
                h = (float) r.getHeight();

        x = x * unitx - w * xalign + dx;
        y = y * unity - h * yalign + h - lm.getDescent() + dy;
        
        if (bgColor != null) {
            graphics.setColor(bgColor);
            graphics.fillRect((int) x, (int) y - fm.getAscent(), (int) r.getWidth(), (int) r.getHeight());
        }
        if (fgColor != null) {
            graphics.setColor(fgColor);
        }
        graphics.drawString(str, (float) x, (float) y);

    }

    /**
     * Draw shape
     */
    public void draw(Shape shape, double x, double y) {
        draw(shape, x, y, false);
    }

    /**
     * Draw shape
     */
    public void draw(Shape shape, double x, double y, int thickness) {
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(thickness));
        draw(shape, x, y, false);
        graphics.setStroke(oldStroke);
    }

    /**
     * Draw shape
     */
    public void draw(Shape shape, double x, double y, boolean fill) {

        AffineTransform at = new AffineTransform();
        at.scale(unitx, unity);
        at.translate(x, y);
        GeneralPath gp = new GeneralPath(shape);
        gp.transform(at);
        shape = gp;

        if (fill) {
            graphics.fill(shape);
        } else {
            graphics.draw(shape);
        }
    }

    /**
     * Draw Image at given position
     */
    public void draw(ImageIcon img, double x, double y, double xalign, double yalign) {
        draw(img, x, y, xalign, yalign, 0, 0);
    }

    /**
     * Draw Image at given position
     */
    public void draw(ImageIcon img, double x, double y, double xalign, double yalign, int dx, int dy) {
        int ix = (int) (x * unitx - xalign * img.getIconWidth() + dx),
                iy = (int) (y * unity - yalign * img.getIconHeight() + dy);
        img.paintIcon(graphics, ix, iy);
    }

    /**
     * Pops a saved AffineTransformation
     */
    public void popTransformation() {
        graphics.setTransform(stackTransformations.pop());
    }

    /**
     * Pushes a AffineTransformation for later
     */
    public void pushTransformation() {
        stackTransformations.push(graphics.getTransform());
    }

    /**
     * Pushes a pop'able clip
     */
    public void pushClip(double x, double y, Rectangle2D r) {
        pushClip(x + r.getMinX(), y + r.getMinY(), x + r.getMaxX(), y + r.getMaxY());
    }

    /**
     * Pushes a pop'able clip
     */
    public void pushClip(Rectangle2D r) {
        pushClip(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY());
    }

    /**
     * Pushes a pop'able clip
     */
    public void pushClip(double x1, double y1, double x2, double y2) {
        clipStack.push(graphics.getClip());
        double x = x1 * unitx,
                y = y1 * unity,
                w = (x2 * unitx) - x,
                h = (y2 * unity) - y;
        graphics.clip(new Rectangle2D.Double(x, y, w, h));
    }

    /**
     * Pop's a previously push'd clip
     */
    public void popClip() {
        graphics.setClip(clipStack.pop());
    }

    /**
     * Resolves a scaled rectangle
     */
    public Rectangle getRectangle(Rectangle2D bounds) {
        int x1 = (int) Math.ceil(bounds.getMinX() * unitx),
                y1 = (int) Math.ceil(bounds.getMinY() * unity),
                x2 = (int) Math.floor(bounds.getMaxX() * unitx),
                y2 = (int) Math.floor(bounds.getMaxY() * unity);

        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

} //GraphicsWrapper
