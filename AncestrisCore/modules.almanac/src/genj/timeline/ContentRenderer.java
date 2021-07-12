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

import genj.gedcom.Gedcom;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.util.swing.ImageIcon;
import genj.util.swing.UnitGraphics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class ContentRenderer {

    /**
     * size of a dot
     */
    protected Point2D.Double dotSize = new Point2D.Double();

    /**
     * a mark used for demarking time spans
     */
    protected GeneralPath fromMark, toMark, eventMark;

    /**
     * whether we paint tags or not
     */
    /*package*/ boolean paintTags = false;

    /**
     * whether we paint dates or not
     */
    /*package*/ boolean paintDates = true;

    /**
     * whether we paint a grid or not
     */
    /*package*/ boolean paintGrid = false;

    /**
     * current selection
     */
    /*package*/ 
    List<Model.Event> selectionEvent = new LinkedList<>();
    List<Model.EventSerie> selectionEventSerie = new LinkedList<>();

    /**
     * background color
     */
    /*package*/ Color cBackground = null;

    /**
     * text color
     */
    /*package*/ Color cText = null;

    /**
     * tag color
     */
    /*package*/ Color cTag = null;

    /**
     * date color
     */
    /*package*/ Color cDate = null;

    /**
     * timespane color
     */
    /*package*/ 
    Color cTimespanM = null;
    Color cTimespanF = null;
    Color cTimespanU = null;

    /**
     * timespane color
     */
    /*package*/ Color cGrid = null;

    /**
     * selected color
     */
    /*package*/ Color cSelected = null, cSelectedBg = null;

    /**
     * Renders the model
     */
    public void render(UnitGraphics graphics, Model model, int mode) {
        // calculate parameters
        init(graphics);
        // render background
        renderBackground(graphics, model);
        // render grid
        renderGrid(graphics, model);
        // render layers
        renderLayers(graphics, model, mode);
        // done
    }

    /**
     * Renders the background
     */
    protected void renderBackground(UnitGraphics g, Model model) {
        if (cBackground == null) {
            return;
        }
        g.setColor(cBackground);
        Rectangle2D r = new Rectangle2D.Double(model.min, 0, model.max - model.min, model.getMaxLayersNumber());
        g.draw(r, 0, 0, true);
    }

    /**
     * Renders a grid
     */
    private void renderGrid(UnitGraphics g, Model model) {
        // check 
        if (!paintGrid) {
            return;
        }
        // color 
        g.setColor(cGrid);
        // loop
        Rectangle2D r = g.getClip();
        double from = Math.floor(r.getMinX()),
                to = Math.ceil(r.getMaxX());
        double step = getYearStep(g, model);
        for (double year = Math.ceil(from / step) * step; year < to; year += step) {
            g.draw(year, 0, year, model.getMaxLayersNumber());
        }
        // done
    }

    /**
     * Renders layers
     */
    private void renderLayers(UnitGraphics g, Model model, int mode) {
        List eventLayers = mode == TimelineView.INDI_MODE ? model.indiLayers : model.eventLayers;
        if (eventLayers == null) {
            return;
        }
        // check clip as we go
        Rectangle2D clip = g.getClip();
        // loop on layers to display, events or indis, depending on user switch 
        for (int l = 0; l < eventLayers.size(); l++) {
            if (l < Math.floor(clip.getMinY()) || l > Math.ceil(clip.getMaxY())) {
                continue;
            }
            List layer = (List) eventLayers.get(l);
            if (mode == TimelineView.INDI_MODE) {
                renderEventSeries(g, model, layer, l);
            } else {
                renderEvents(g, model, layer, l);
            }
        }
        // done
    }

    /**
     * Renders a layer of Events
     */
    private void renderEvents(UnitGraphics g, Model model, List layer, int level) {
        // check clip as we go
        Rectangle2D clip = g.getClip();
        // loop through events
        Iterator events = layer.iterator();
        Model.Event event = (Model.Event) events.next();
        while (true) {
            // already grabbing next because we paint as much as we can
            Model.Event next = events.hasNext() ? (Model.Event) events.next() : null;
            // check clipping and draw
            if ((next == null || next.from > clip.getMinX()) && (event.from < clip.getMaxX())) {
                renderEvent(g, model, event, next, level);
            }
            // no more?
            if (next == null) {
                break;
            }
            // one more!
            event = next;
        }
        // done
    }

    /**
     * Renders a layer of EventSeries
     */
    private void renderEventSeries(UnitGraphics g, Model model, List layer, int level) {
        // check clip as we go
        Rectangle2D clip = g.getClip();
        // loop through event series
        Iterator eventSeries = layer.iterator();
        Model.EventSerie eventSerie = (Model.EventSerie) eventSeries.next();
        while (true) {
            // already grabbing next because we paint as much as we can
            Model.EventSerie next = eventSeries.hasNext() ? (Model.EventSerie) eventSeries.next() : null;
            // check clipping and draw
            if ((next == null || next.from > clip.getMinX()) && (eventSerie.from < clip.getMaxX())) {
                renderEventSerie(g, model, eventSerie, next, level);
            }
            // no more?
            if (next == null) {
                break;
            }
            // one more!
            eventSerie = next;
        }
        // done
    }

    /**
     * Renders an Event
     */
    private void renderEvent(UnitGraphics g, Model model, Model.Event event, Model.Event next, int level) {

        // calculate some parameters
        boolean em = selectionEvent.contains(event);
        FontMetrics fm = g.getFontMetrics();

        // draw it's extend
        int sex = event.getSex();
        g.setColor(sex == PropertySex.MALE ? cTimespanM : (sex == PropertySex.FEMALE ? cTimespanF : cTimespanU));

        PropertyDate.Format format = event.pd.getFormat();
        if (format == PropertyDate.AFTER || format == PropertyDate.FROM) {
            g.draw(fromMark, event.from, level + 1, true);
        } else if (format == PropertyDate.BEFORE || format == PropertyDate.TO) {
            g.draw(toMark, event.from, level + 1, true);
        } else {
            g.draw(fromMark, event.from, level + 1, true);
            g.draw(event.from, level + 1 - dotSize.y, event.to, level + 1 - dotSize.y);
            g.draw(toMark, event.to, level + 1, true);
        }

        int dx = 0;
        double left = event.from-1;
        double right = left + 1 + 10/model.cmPerYear;
        if (next != null) {
            right = Math.min(right, next.from);
        }

        // draw its image
        if (!paintTags) {
            ImageIcon img = event.pe.getImage(false);
            g.pushClip(left, level, right, level + 2);
            g.draw(img, event.from, level + 0.5, 0, 0.5);
            g.popClip();
            dx += img.getIconWidth() + 2;
        }

        // draw its tag    
        if (paintTags) {
            String tag = Gedcom.getName(event.pe.getTag());
            g.setColor(cTag);
            g.pushClip(left, level, right, level + 2);
            g.draw(tag, event.from, level + 1, 0, 1, dx, 0);
            g.popClip();
            dx += fm.stringWidth(tag) + fm.charWidth(' ');
        }

        // draw its text 
        String txt = event.content;
        g.pushClip(left, level, right, level + 2);
        g.draw(txt, event.from, level + 1, 0, 1, dx, 0, em ? cSelected : cText, em ? cSelectedBg : null);   // null background: do not repaint background when not necessary
        g.popClip();
        dx += fm.stringWidth(txt) + fm.charWidth(' ');

        // draw its date
        if (paintDates) {
            String date = " (" + event.pd.getDisplayValue() + ')';
            g.setColor(cDate);
            g.pushClip(left, level, right, level + 2);
            g.draw(date, event.from, level + 1, 0, 1, dx, 0);
            g.popClip();
        }

        // done
    }

    /**
     * Renders an EventSerie
     */
    private void renderEventSerie(UnitGraphics g, Model model, Model.EventSerie eventSerie, Model.EventSerie next, int level) {

        // discard empty series
        if (eventSerie.indi == null) {
            return;
        }
        
        // calculate some parameters
        boolean em = selectionEventSerie.contains(eventSerie);
        FontMetrics fm = g.getFontMetrics();
        Stroke dotted = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{2}, 0);

        // draw it's extend
        int sex = eventSerie.getSex();
        g.setColor(sex == PropertySex.MALE ? cTimespanM : (sex == PropertySex.FEMALE ? cTimespanF : cTimespanU));
        g.draw(fromMark, eventSerie.from, level + 1, true);
        g.draw(eventSerie.getFrom(), level + 1 - dotSize.y, eventSerie.getFirstEvent().from, level + 1 - dotSize.y, dotted);
        g.draw(eventSerie.getFirstEvent().from, level + 1 - dotSize.y, eventSerie.getLastEvent().to, level + 1 - dotSize.y);
        g.draw(eventSerie.getLastEvent().to, level + 1 - dotSize.y, eventSerie.to, level + 1 - dotSize.y, dotted);
        g.draw(toMark, eventSerie.getTo(), level + 1, true);
        
        // drawn all marks based on dates
        for (double d : eventSerie.getDates()) {
            if (d == eventSerie.from|| d == eventSerie.to) {
                continue;
            }
            g.draw(eventMark, d, level + 1, true);
        }
        
        
        int dx = 6;
        double left = eventSerie.from-1;
        double right = left + 1 + 10/model.cmPerYear;
        if (next != null) {
            right = Math.min(right, next.from);
        }

        // draw its image
        if (!paintTags) {
            ImageIcon img = eventSerie.getImage();
            g.pushClip(left, level, right, level + 2);
            g.draw(img, eventSerie.from, level + 0.5, 0, 0.5, dx, 0);
            g.popClip();
            dx += img.getIconWidth() + 2;
        }

        // draw its tag    
        if (paintTags) {
            String tag = Gedcom.getName(eventSerie.getTag());
            g.setColor(cTag);
            g.pushClip(left, level, right, level + 2);
            g.draw(tag, eventSerie.from, level + 1, 0, 1, dx, 0);
            g.popClip();
            dx += fm.stringWidth(tag) + fm.charWidth(' ');
        }

        // draw its text 
        String txt = eventSerie.content;
        g.pushClip(left, level, right, level + 2);
        g.draw(txt, eventSerie.from, level + 1, 0, 1, dx, 0, em ? cSelected : cText, em ? cSelectedBg : null);   // null background: do not repaint background when not necessary
        g.popClip();
        dx += fm.stringWidth(txt) + fm.charWidth(' ');

        // draw its date
        if (paintDates) {
            String date = " (" + eventSerie.getDisplayDates() + ')';
            g.setColor(cDate);
            g.pushClip(left, level, right, level + 2);
            g.draw(date, eventSerie.from, level + 1, 0, 1, dx, 0);
            g.popClip();
        }

        // done
    }

    double getYearStep(UnitGraphics graphics, Model model) {
        // prepare some stuff
        FontMetrics fm = graphics.getFontMetrics();
        double width = fm.stringWidth(" 0000 ") * dotSize.x;
        double step = Math.ceil(width);
        if (step == 1) {
            return step;
        }
        if (step <= 5) {
            return 5;
        }
        if (step <= 50) {
            return (10 * (Math.ceil(step / 10)));
        }
        return (100 * (Math.ceil(step / 100)));
    }

    /**
     * inits painting
     */
    protected void init(UnitGraphics graphics) {

        // calculate dot-size
        dotSize.setLocation(1D / graphics.getUnit().getX(), 1D / graphics.getUnit().getY());

        // calculate fromMark
        fromMark = new GeneralPath();
        fromMark.moveTo((float) (4F * dotSize.x), (float) (-1F * dotSize.y));
        fromMark.lineTo((float) (-1F * dotSize.x), (float) (-6F * dotSize.y));
        fromMark.lineTo((float) (-1F * dotSize.x), (float) (+4F * dotSize.y));
        fromMark.closePath();

        // calculate toMark
        toMark = new GeneralPath();
        toMark.moveTo((float) (-4F * dotSize.x), (float) (-1F * dotSize.y));
        toMark.lineTo((float) (1F * dotSize.x), (float) (-6F * dotSize.y));
        toMark.lineTo((float) (1F * dotSize.x), (float) (+4F * dotSize.y));
        toMark.closePath();

        // calculate eventMark
        eventMark = new GeneralPath();
        eventMark.moveTo((float) (-5F * dotSize.x), (float) (-1F * dotSize.y));
        eventMark.lineTo((float) (-1F * dotSize.x), (float) (-5F * dotSize.y));
        eventMark.lineTo((float) (3F * dotSize.x), (float) (-1F * dotSize.y));
        eventMark.lineTo((float) (-1F * dotSize.x), (float) (+3F * dotSize.y));
        eventMark.closePath();
        // done
    }

} //RulerRenderer
