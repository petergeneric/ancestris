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

import genj.gedcom.Gedcom;
import genj.gedcom.PropertyDate;
import genj.renderer.Options;
import genj.util.swing.ImageIcon;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class ContentRenderer {
  
  /** size of a dot */
  protected Point2D.Double dotSize = new Point2D.Double();
  
  /** a mark used for demarking time spans */
  protected GeneralPath fromMark, toMark; 
  
  /** whether we paint tags or not */
  /*package*/ boolean paintTags = false;
  
  /** whether we paint dates or not */
  /*package*/ boolean paintDates = true;
  
  /** whether we paint a grid or not */
  /*package*/ boolean paintGrid = false;
  
  /** current selection */
  /*package*/ Set<Model.Event> selection = new HashSet<Model.Event>();
  
  /** background color */
  /*package*/ Color cBackground = null;
  
  /** text color */
  /*package*/ Color cText = null;
  
  /** tag color */
  /*package*/ Color cTag = null;
  
  /** date color */
  /*package*/ Color cDate = null;
  
  /** timespane color */
  /*package*/ Color cTimespan= null;
  
  /** timespane color */
  /*package*/ Color cGrid = null;
  
  /** selected color */
  /*package*/ Color cSelected = null;
  
  /**
   * Renders the model
   */
  public void render(UnitGraphics graphics, Model model) {
    // calculate parameters
    init(graphics);
    // render background
    renderBackground(graphics, model);
    // render grid
    renderGrid(graphics, model);
    // render layers
    renderLayers(graphics, model);
    // done
  }
  
  /**
   * Renders the background
   */
  protected void renderBackground(UnitGraphics g, Model model) {
    if (cBackground==null) return;
    g.setColor(cBackground);
    Rectangle2D r = new Rectangle2D.Double(model.min, 0, model.max-model.min, 1024);
    g.draw(r, 0, 0, true);
  }
  
  /**
   * Renders a grid
   */
  private final void renderGrid(UnitGraphics g, Model model) {
    // check 
    if (!paintGrid) return;
    // color 
    g.setColor(cGrid);
    // loop
    Rectangle2D r = g.getClip();
    int layers = model.layers.size();
    double 
      from = Math.floor(r.getMinX()),
      to = Math.ceil(r.getMaxX());
    for (double year=from;year<=to;year++) {
      g.draw(year, 0, year, layers);
    }
    // done
  }

  /** 
   * Renders layers
   */
  private final void renderLayers(UnitGraphics g, Model model) {
    // check clip as we go
    Rectangle2D clip = g.getClip();
    // loop
    List layers = model.layers;
    for (int l=0; l<layers.size(); l++) {
      if (l<Math.floor(clip.getMinY())||l>Math.ceil(clip.getMaxY())) continue;
      List layer = (List)layers.get(l);
      renderEvents(g, model, layer, l);
    }
    // done
  }
  
  /** 
   * Renders a layer
   */
  private final void renderEvents(UnitGraphics g, Model model, List layer, int level) {
    // check clip as we go
    Rectangle2D clip = g.getClip();
    // loop through events
    Iterator events = layer.iterator();
    Model.Event event = (Model.Event)events.next();
    while (true) {
      // already grabbing next because we paint as much as we can
      Model.Event next = events.hasNext() ? (Model.Event)events.next() : null;
      // check clipping and draw
      if ((next==null||next.from>clip.getMinX())&&(event.from<clip.getMaxX())) {
        renderEvent(g, model, event, next, level);
      }
      // no more?
      if (next==null) break;
      // one more!
      event = next;
    } 
    // done
  }
  
  /**
   * Renders an event
   */
  private final void renderEvent(UnitGraphics g, Model model, Model.Event event, Model.Event next, int level) {

    // calculate some parameters
    boolean em  = selection.contains(event); 
    FontMetrics fm = g.getFontMetrics();
       
    // draw it's extend
    g.setColor(cTimespan);
    
    PropertyDate.Format format = event.pd.getFormat();
    if (format==PropertyDate.AFTER||format==PropertyDate.FROM) {
      g.draw(fromMark, event.from, level+1, true);
    } else if (format==PropertyDate.BEFORE||format==PropertyDate.TO) {
      g.draw(toMark, event.from, level+1, true);
    } else {
      g.draw(fromMark, event.from, level+1, true);
      g.draw( event.from, level + 1 - dotSize.y, event.to, level + 1 - dotSize.y );
      g.draw(toMark, event.to, level+1, true);
    }

    // clipping from here    
    g.pushClip(event.from, level, next==null?Integer.MAX_VALUE:next.from, level+1);

    int dx = 0;
    
    // draw its image
    if (!paintTags) {
      ImageIcon img = event.pe.getImage(false);
      g.draw(img, event.from, level+0.5, 0, 0.5);
      dx+=img.getIconWidth() + 2;
    }
    
    // draw its tag    
    if (paintTags) {
      String tag = Gedcom.getName(event.pe.getTag());
      g.setColor(cTag);
      g.draw(tag, event.from, level+1, 0, 1, dx, 0);
      dx+=fm.stringWidth(tag)+fm.charWidth(' ');
    }

    // draw its text 
    g.setFont(Options.getInstance().getDefaultFont());
    g.setColor(em ? cSelected : cText);
    String txt = event.content;
    g.draw(txt, event.from, level+1, 0, 1, dx, 0);
    dx+=fm.stringWidth(txt)+fm.charWidth(' ');
    
    // draw its date
    if (paintDates) {
      String date = " (" + event.pd.getDisplayValue() + ')';
      g.setColor(cDate);
      g.draw(date, event.from, level+1, 0, 1, dx, 0);
    }

    // done with clipping
    g.popClip();

    // done
  }
  
  /**
   * inits painting   */
  protected void init(UnitGraphics graphics) {
    
    // calculate dot-size
    dotSize.setLocation( 1D / graphics.getUnit().getX(), 1D / graphics.getUnit().getY() );

    // calculate fromMark
    fromMark = new GeneralPath();
    fromMark.moveTo((float)(3F*dotSize.x),(float)(-1F*dotSize.y));
    fromMark.lineTo((float)(-1F*dotSize.x),(float)(-5F*dotSize.y));
    fromMark.lineTo((float)(-1F*dotSize.x),(float)(+3F*dotSize.y));
    fromMark.closePath();

    // calculate toMark
    toMark = new GeneralPath();
    toMark  .moveTo((float)(-3F*dotSize.x),(float)(-1F*dotSize.y));
    toMark  .lineTo((float)( 1F*dotSize.x),(float)(-6F*dotSize.y));
    toMark  .lineTo((float)( 1F*dotSize.x),(float)(+4F*dotSize.y));
    toMark  .closePath();
    
    // done
  }
  
} //RulerRenderer
