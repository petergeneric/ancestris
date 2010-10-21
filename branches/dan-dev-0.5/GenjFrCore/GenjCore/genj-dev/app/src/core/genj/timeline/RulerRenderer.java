/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
import java.util.Set;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class RulerRenderer extends ContentRenderer {
  
  /** ticks color */
  /*package*/ Color cTick = null;
  
  /** almanac categories */
  /*package*/ Set acats = null;
  
  /** a tick */
  private Shape tickMark, eventMark;
  
  /**
   * Renders the model
   */
  public void render(UnitGraphics graphics, Model model) {
    
    // init drawing
    init(graphics);
    
    // prepare some stuff
    FontMetrics fm = graphics.getFontMetrics();
    double
      from  = Math.ceil(model.min),
      to    = Math.floor(model.max),
      width = fm.stringWidth(" 0000 ") * dotSize.x;

    // render background
    renderBackground(graphics, model);

    // render first year and last
    renderYear(graphics, model, fm, from, 0.0D);
    renderYear(graphics, model, fm, to  , 1.0D);
    
    from += width;
    to += -width;
    
    // recurse binary
    renderSpan(graphics, model, fm, from, to, width);
    
    // render cday events
    renderAlmanac(graphics);
    
    // done
  }
  
  /**
   * Renders CDay event markers
   */
  private void renderAlmanac(UnitGraphics g) {
    
    // prepare drawing - color, clip, years etc.
    g.setColor(cTimespan);
    Rectangle2D clip = g.getClip();
    PointInTime 
      from = Model.toPointInTime(clip.getX()),
      to   = Model.toPointInTime(clip.getMaxX());

    // we calculate how much time is covered per pixel
    double timePerPixel = dotSize.x;
    
    // iterate over according years
    Iterator almanac = Almanac.getInstance().getEvents(from, to, acats);
    double last = 0;
    while (almanac.hasNext()) {
      // event to handle
      Event event = (Event)almanac.next();
      try {
        // calculate it's year
        PointInTime time = event.getTime();
        double year = Model.toDouble(time, false);
        // check if this would be a new pixel - this is for
        // efficiency reasons since we don't end up painting
        // on the same pixel multiple times
        if (year-last>=timePerPixel) {
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
    if (to-from<width||to-from<1) return;

    // clipp'd out?
    Rectangle2D clip = g.getClip();
    if (!clip.intersects(from, 0, to-from, 1)) return;
    
    // calculate center year    
    double year = Math.rint((from+to)/2);

    // still nough' space?
    if (year-from<width/2||to-year<width/2) {
      return;
    }

    // render
    renderYear(g, model, fm, year, 0.5D);
    
    // recurse into
    renderSpan(g, model, fm, year+width/2, to         , width);
    renderSpan(g, model, fm, from       , year-width/2, width);
    
    // done
  }
  
  /**
   * Renders one year
   */
  private void renderYear(UnitGraphics g, Model model,  FontMetrics fm, double year, double align) {
    // draw a vertical line
    g.setColor(cTick);
    g.draw(tickMark, year, 1, true);
    
    // draw the label
    g.setColor(cText);
    g.draw(Integer.toString((int)year), year, 1, align, 1.0);
    
    // done
  }

  /**
   * Initializes drawing
   */
  protected void init(UnitGraphics graphics) {
    super.init(graphics);
    
    GeneralPath gp = new GeneralPath();
    gp.moveTo( (float)( 0F*dotSize.x), (float)( 0F*dotSize.y) );
    gp.lineTo( (float)( 3F*dotSize.x), (float)(-3F*dotSize.y) );
    gp.lineTo( (float)(-3F*dotSize.x), (float)(-3F*dotSize.y) );
    gp.closePath();
    
    tickMark = gp;
    eventMark = new Line2D.Double(0,0,0,5F*dotSize.y);
  }
  
} //RulerRenderer
