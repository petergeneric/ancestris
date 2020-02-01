/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2016 Frederic Lapeyre <frederic@ancestris.org>
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
package genj.timeline;

import genj.almanac.Almanac;
import genj.almanac.Event;
import genj.gedcom.GedcomException;
import genj.gedcom.time.PointInTime;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class RulerRenderer extends ContentRenderer {

    /**
     * ticks color
     */
    /*package*/ Color cTick = null;

    /**
     * almanac categories
     */
    /*package*/ List<String> almanacs = null;
    /*package*/ List<String> acats = null;
    /*package*/ int sigLevel = AlmanacPanel.MAX_SIG;

    /**
     * a tick
     */
    private Shape tickMark, eventMark;
    
    /**
     * Renders the model
     */
    public void render(UnitGraphics graphics, Model model) {

        // init drawing
        init(graphics);

        // prepare some stuff
        FontMetrics fm = graphics.getFontMetrics();
        double from = Math.ceil(model.min),
                to = Math.floor(model.max),
                width = fm.stringWidth(" 0000 ") * dotSize.x;
        double step = getYearStep(graphics, model);

        // render background
        renderBackground(graphics, model);
        
        // render first year and last
        //renderYear(graphics, from, step, 0.0D);
        //renderYear(graphics, to, step, 1.0D);

        from += width;
        to += width;

        for (double year = Math.floor(from / step) * step; year <= to; year += step) {
            renderYear(graphics, year, step, 0.5D);
        }

        // render cday events
        renderAlmanac(graphics);

        // done
    }

    /**
     * Renders one year
     */
    private void renderYear(UnitGraphics g, double year, double step, double align) {
        
        double yearNext = year + step;
        
        // draw a vertical line
        g.setColor(cTick);
        g.draw(tickMark, year, 1, true);

        // draw the label
        g.setColor(cText);
        g.pushClip(year - step/2, 0, yearNext - step/2, 1);
        g.draw(Integer.toString((int) year), year, 1, align, 1.0, 0, -2);
        g.popClip();
        // done
    }

    
    
    
    
    /**
     * Initializes drawing
     */
    protected void init(UnitGraphics graphics) {
        super.init(graphics);
        
        float tickSize = 3F;

        // Define tick for years as a tip-down triangle
        GeneralPath gp = new GeneralPath();
        gp.moveTo((float) (0F * dotSize.x), (float) (0F * dotSize.y));
        gp.lineTo((float) (tickSize * dotSize.x), (float) (-tickSize * dotSize.y));
        gp.lineTo((float) (-tickSize * dotSize.x), (float) (-tickSize * dotSize.y));
        gp.closePath();
        tickMark = gp;

        // Define event mark
        eventMark = new Line2D.Double(0, 0, 0, 5F * dotSize.y);
    }


    /**
     * Renders CDay event markers
     */
    private void renderAlmanac(UnitGraphics g) {

        // prepare drawing - color, clip, years etc.
        g.setColor(cTimespanM);
        Rectangle2D clip = g.getClip();
        PointInTime from = Model.toPointInTime(clip.getX()),
                to = Model.toPointInTime(clip.getMaxX());

        // we calculate how much time is covered per pixel
        double timePerPixel = dotSize.x;

        // iterate over according years
        Iterator almanac = Almanac.getInstance().getEvents(from, to, almanacs, acats, sigLevel);
        double last = 0;
        while (almanac.hasNext()) {
            // event to handle
            Event event = (Event) almanac.next();
            try {
                // calculate it's year
                PointInTime time = event.getTime();
                double year = Model.toDouble(time, false);
                // check if this would be a new pixel - this is for
                // efficiency reasons since we don't end up painting
                // on the same pixel multiple times
                if (year - last >= timePerPixel) {
                    g.draw(eventMark, year, 0, false);
                    last = year;
                }
            } catch (GedcomException e) {
            }
            // next
        }
    }

    
    /**
     * Renders ticks recursively
     */
    private void renderSpan(UnitGraphics g, Model model, FontMetrics fm, double from, double to, double width) {

        // condition met (ran out of space)?
        if (to - from < width || to - from < 1) {
            return;
        }

        // clipp'd out?
        Rectangle2D clip = g.getClip();
        if (!clip.intersects(from, 0, to - from, 1)) {
            return;
        }

        // calculate center year    
        double year = Math.rint((from + to) / 2);

        // still nough' space?
        if (year - from < width / 2 || to - year < width / 2) {
            return;
        }

        // render
        double step = getYearStep(g, model);
        renderYear(g, year, step, 0.5D);

        // recurse into
        renderSpan(g, model, fm, year + width / 2, to, width);
        renderSpan(g, model, fm, from, year - width / 2, width);

        // done
    }

    
    
} //RulerRenderer
