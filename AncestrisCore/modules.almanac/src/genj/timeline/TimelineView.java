/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
 * Copyright (C) 2016 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.timeline;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.view.SelectionDispatcher;
import genj.almanac.Almanac;
import genj.almanac.Event;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.renderer.DPI;
import genj.renderer.RenderOptions;
import genj.renderer.RenderSelectionHintKey;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.view.ScreenshotAction;
import genj.view.SettingsAction;
import ancestris.swing.ToolBar;
import genj.util.swing.ImageIcon;
import genj.view.View;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends View {

    /** the units we use */
    private final DPI DPI;
    private final Point2D DPC;
    /** resources */
    private Resources resources = Resources.get(this);
    /** keeping track of our colors */
    /* package */ Map<String, Color> colors = new HashMap<String, Color>();
    /** our model */
    private Model model;
    /** our content */
    private Content content;
    /** our current selection */
    private Set<Model.Event> selectionEvent = new HashSet<Model.Event>();
    private Set<Model.EventSerie> selectionEventSerie = new HashSet<Model.EventSerie>();
    /** our ruler */
    private Ruler ruler;
    /** our slider for cm per year */
    private SliderWidget sliderCmPerYear;
    /** our scrollpane */
    private JScrollPane scrollContent;
    /** the renderer we use for the ruler */
    private RulerRenderer rulerRenderer = new RulerRenderer();
    /** the renderer we use for the content */
    private ContentRenderer contentRenderer = new ContentRenderer();
    /** almanac categories */
    private List<String> ignoredAlmanacCategories = new ArrayList<String>();
    /** min/max's */
    /* package */ final static double MIN_CM_PER_YEAR = 0.1D,
            DEF_CM_PER_YEAR = 1.0D,
            MAX_CM_PER_YEAR = 20.0D,
            MIN_CM_BEF_EVENT = 0.1D,
            DEF_CM_BEF_EVENT = 0.5D,
            MAX_CM_BEF_EVENT = 2.0D,
            MIN_CM_AFT_EVENT = 2.0D,
            DEF_CM_AFT_EVENT = 2.0D,
            MAX_CM_AFT_EVENT = 9.0D;
    /** centimeters per year/event */
    private double cmPerYear = DEF_CM_PER_YEAR,
            cmBefEvent = DEF_CM_BEF_EVENT,
            cmAftEvent = DEF_CM_AFT_EVENT;
    /** the centered year */
    private double centeredYear = 0;
    /** settings */
    private boolean isPaintDates = true,
            isPaintGrid = false,
            isPaintTags = true,
            isPackIndi = false;
    /** registry we keep */
    private final static Registry REGISTRY = Registry.get(TimelineView.class);
    private ModelListener callback = new ModelListener();
    /** modes */
    public static int INDI_MODE = 0;
    public static int EVENT_MODE = 1;
    public int mode = INDI_MODE;
    private CenterTreeToIndividual cttiButton;
    private CenterToSelectionAction ctsButton;

    /**
     * Constructor
     */
    public TimelineView() {

        // remember
        DPI = RenderOptions.getInstance().getDPI();
        DPC = new Point2D.Float(
                DPI.horizontal() / 2.54F,
                DPI.vertical() / 2.54F);

        // read some stuff from registry
        cmPerYear = Math.max(MIN_CM_PER_YEAR, Math.min(MAX_CM_PER_YEAR, REGISTRY.get("cmperyear", (float) DEF_CM_PER_YEAR)));
        cmBefEvent = Math.max(MIN_CM_BEF_EVENT, Math.min(MAX_CM_BEF_EVENT, REGISTRY.get("cmbefevent", (float) DEF_CM_BEF_EVENT)));
        cmAftEvent = Math.max(MIN_CM_AFT_EVENT, Math.min(MAX_CM_AFT_EVENT, REGISTRY.get("cmaftevent", (float) DEF_CM_AFT_EVENT)));
        isPaintDates = REGISTRY.get("paintdates", true);
        isPaintGrid = REGISTRY.get("paintgrid", false);
        isPaintTags = REGISTRY.get("painttags", false);
        isPackIndi = REGISTRY.get("packindi", false);

        colors.put("background", Color.WHITE);
        colors.put("text", Color.BLACK);
        colors.put("tag", Color.GREEN);
        colors.put("date", Color.GRAY);
        colors.put("timespanM", Color.BLUE);
        colors.put("timespanF", Color.RED);
        colors.put("timespanU", Color.GRAY);
        colors.put("grid", Color.LIGHT_GRAY);
        colors.put("selected", Color.RED);
        colors = REGISTRY.get("color", colors);

        String[] ignored = REGISTRY.get("almanac.ignore", new String[0]);
        ignoredAlmanacCategories.addAll(Arrays.asList(ignored));
        
        mode = REGISTRY.get("display.mode", mode);

        // create/keep our sub-parts
        model = new Model();
        model.setTimePerEvent(cmBefEvent / cmPerYear, cmAftEvent / cmPerYear);
        model.setPackIndi(isPackIndi);

        String[] ps = REGISTRY.get("paths", (String[]) null);
        if (ps != null) {
            List<TagPath> paths = new ArrayList<TagPath>(ps.length);
            for (String p : ps) {
                try {
                    paths.add(new TagPath(p));
                } catch (Throwable t) {
                }
            }
            model.setPaths(paths);
        } else {
            model.setPaths(null);
        }

        content = new Content();
        ruler = new Ruler();

        // all that fits in a scrollpane
        scrollContent = new ScrollPaneWidget(content);
        scrollContent.setViewportBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        scrollContent.setBackground(Color.WHITE);
        scrollContent.setColumnHeaderView(new ViewPortAdapter(ruler));
        scrollContent.getViewport().addChangeListener(new ChangeListener() {
        
            @Override
            public void stateChanged(ChangeEvent e) {
                // easy : translation and remember
                double x = scrollContent.getViewport().getViewPosition().x + scrollContent.getViewport().getSize().width / 2;
                centeredYear = pixel2year(x);
            }
        });

        // layout
        setLayout(new BorderLayout());
        add(scrollContent, BorderLayout.CENTER);

        // scroll to last centered year
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                centeredYear = REGISTRY.get("centeryear", 0F);
                scroll2year(centeredYear);
            }
        });

        // done
    }

    @Override
    public void addNotify() {
        // let super do its thing
        super.addNotify();
        // connect to model
        model.addListener(callback);
    }

    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    @Override
    public void removeNotify() {

        // disconnect from model
        model.removeListener(callback);

        // store stuff in registry
        REGISTRY.put("cmperyear", (float) Math.rint(cmPerYear * 10) / 10);
        REGISTRY.put("cmbefevent", (float) cmBefEvent);
        REGISTRY.put("cmaftevent", (float) cmAftEvent);
        REGISTRY.put("paintdates", isPaintDates);
        REGISTRY.put("paintgrid", isPaintGrid);
        REGISTRY.put("packindi", isPackIndi);
        REGISTRY.put("painttags", isPaintTags);
        REGISTRY.put("filter", model.getPaths());
        REGISTRY.put("centeryear", (float) centeredYear);
        REGISTRY.put("color", colors);
        REGISTRY.put("paths", model.getPaths());

        String[] ignored = new String[ignoredAlmanacCategories.size()];
        for (int i = 0; i < ignored.length; i++) {
            ignored[i] = ignoredAlmanacCategories.get(i).toString();
        }
        REGISTRY.put("almanac.ignore", ignored);

        REGISTRY.put("display.mode", mode);
        
        // done
        super.removeNotify();
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(480, 256);
    }

    /**
     * Accessor - the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Accessor - almanac categories
     */
    public Set<String> getAlmanacCategories() {
        HashSet<String> result = new HashSet<String>(Almanac.getInstance().getCategories());
        result.removeAll(ignoredAlmanacCategories);
        return result;
    }

    /**
     * Accessor - hidden almanac category keys
     */
    public void setAlmanacCategories(Set<String> set) {
        ignoredAlmanacCategories.clear();
        ignoredAlmanacCategories.addAll(Almanac.getInstance().getCategories());
        ignoredAlmanacCategories.removeAll(set);
        repaint();
    }

    /**
     * Accessor - paint tags
     */
    public boolean isPaintTags() {
        return isPaintTags;
    }

    /**
     * Accessor - paint tags
     */
    public void setPaintTags(boolean set) {
        isPaintTags = set;
        repaint();
    }

    /**
     * Accessor - paint dates
     */
    public boolean isPaintDates() {
        return isPaintDates;
    }

    /**
     * Accessor - paint dates
     */
    public void setPaintDates(boolean set) {
        isPaintDates = set;
        repaint();
    }

    /**
     * Accessor - paint grid
     */
    public boolean isPaintGrid() {
        return isPaintGrid;
    }

    /**
     * Accessor - paint grid
     */
    public void setPaintGrid(boolean set) {
        isPaintGrid = set;
        repaint();
    }

    /**
     * Accessor - pack individuals
     */
    public boolean isPackIndi() {
        return isPackIndi;
    }

    /**
     * Accessor - pack individuals
     */
    public void setPackIndi(boolean set) {
        isPackIndi = set;
        model.setPackIndi(isPackIndi);
        createLayers();
        selectionEventSerie = model.getIndis(content.getContext());
        centerToSelection();
    }

    /**
     * Sets the time allocated per event
     */
    public void setCMPerEvents(double before, double after) {
        // remember
        cmBefEvent = before;
        cmAftEvent = after;
        // update model
        model.setTimePerEvent(cmBefEvent / cmPerYear, cmAftEvent / cmPerYear);
    }

    /**
     * Accessor - time per event
     */
    public double getCmBeforeEvents() {
        return cmBefEvent;
    }

    /**
     * Accessor - time per event
     */
    public double getCmAfterEvents() {
        return cmAftEvent;
    }

    /**
     * Set tooltipText for slider and scrollbar
     */
    public void setTooltipText() {
        double cmPY = Math.floor(cmPerYear*100) / 100;
        sliderCmPerYear.setToolTipText(cmPY + " " + resources.getString("view.peryear.tip") + " ("+sliderCmPerYear.getValue()+"%)");

        JScrollBar sb = scrollContent.getHorizontalScrollBar();
        String minYear = String.valueOf((int)pixel2year(sb.getMinimum()));
        String year = String.valueOf((int) pixel2year(sb.getValue()) + 1);
        String maxYear = String.valueOf((int)pixel2year(sb.getMaximum()));
        sb.setToolTipText(resources.getString("view.scrollyear.tip", minYear, year, maxYear));

        int h = getFontMetrics(getFont()).getHeight() + 1;
        JScrollBar vsb = scrollContent.getVerticalScrollBar();
        String value = Integer.toString((int) vsb.getValue() / h);
        String total = Integer.toString(model.getLayersNumber(mode));
        vsb.setToolTipText(resources.getString("view.scrolllayer.tip", value, total)); // + " ; increment = "+vsb.getUnitIncrement());
    }
    
    /**
     * @see genj.view.ToolBarSupport#populate(JToolBar)
     */
    @Override
    public void populate(ToolBar toolbar) {

        // create a slider for cmPerYear
        int value = (int) (Math.log((cmPerYear - MIN_CM_PER_YEAR) / (MAX_CM_PER_YEAR - MIN_CM_PER_YEAR) * Math.exp(10)) * 10);
        sliderCmPerYear = new SliderWidget(1, 100, Math.min(100, Math.max(1, value)));
        setTooltipText();
        sliderCmPerYear.addChangeListener(new ChangeCmPerYear());
        sliderCmPerYear.setOpaque(false);
        cttiButton = new CenterTreeToIndividual();
        ctsButton = new CenterToSelectionAction();

        toolbar.add(sliderCmPerYear);
        toolbar.addSeparator();
        toolbar.add(new ToggleModeAction());
        toolbar.add(ctsButton);
        toolbar.add(cttiButton);
        toolbar.addSeparator();
        toolbar.add(new ScreenshotAction(content));
        toolbar.add(new JLabel(" "), "growx, pushx, center");
        toolbar.addSeparator();
        toolbar.add(new Settings());

    }

    /**
     * callback - context event
     */
    @Override
    public void setContext(Context context) {

        if (context == null) {
            model.setGedcom(null);
            selectionEvent.clear();
            selectionEventSerie.clear();
        } else {
            model.setGedcom(context);
            selectionEvent = model.getEvents(context);
            selectionEventSerie = model.getIndis(context);
        }
        
        if (cttiButton != null & ctsButton != null) {
            cttiButton.setEnabled(!selectionEventSerie.isEmpty());
            cttiButton.setTip();
            ctsButton.setEnabled(!selectionEventSerie.isEmpty() && !selectionEvent.isEmpty());
            ctsButton.setTip();
        }
        
        // do a repaint, too
        content.repaint();

        centerToSelection();

        // done
    }

    /**
     * Returns the event at given position
     */
    protected Model.Event getEventAt(Point pos) {
        double year = pixel2year(pos.x);
        int layer = pos.y / (getFontMetrics(getFont()).getHeight() + 1);
        return model.getEvent(year, layer);
    }

    /**
     * Returns the indi at given position
     */
    protected Model.EventSerie getIndiAt(Point pos) {
        double year = pixel2year(pos.x);
        int layer = pos.y / (getFontMetrics(getFont()).getHeight() + 1);
        return model.getEventSerie(year, layer);
    }
    /**
     * Calculates a year from given pixel position
     */
    protected double pixel2year(double x) {
        return model.min + x / (DPC.getX() * cmPerYear);
    }

    /**
     * Scrolls so that given year is centered in view
     */
    protected void scroll2year(double year) {
        centeredYear = year;
        int x = (int) ((year - model.min) * DPC.getX() * cmPerYear) - scrollContent.getViewport().getWidth() / 2;
        scrollContent.getHorizontalScrollBar().setValue(x);
        scrollContent.getHorizontalScrollBar().setUnitIncrement((int) (DPC.getX() * cmPerYear));
        scrollContent.getVerticalScrollBar().setUnitIncrement((int) (Math.max(3, Math.log10(model.getLayersNumber(mode))) * (getFontMetrics(getFont()).getHeight() + 1)));
    }

    protected void scroll2layer(int layer) {
        int y = (int) (layer * (getFontMetrics(getFont()).getHeight() + 1)    -    (scrollContent.getViewport().getHeight() / 3));
        if (y < 0) {
            y = 0;
        }
        scrollContent.revalidate();
        scrollContent.getVerticalScrollBar().setValue(y);
    }

    /**
     * Make sure the given event is visible
     * 
     */
    protected void makeVisible(Model.Event event) {
        scroll2year(event.from);
        scroll2layer(model.getLayerFromEvent(event));
    }

    /**
     * Make sure the given event is visible
     * 
     */
    protected void makeVisible(Model.EventSerie eventSerie) {
        scroll2year(eventSerie.from);
        scroll2layer(model.getLayerFromEventSerie(eventSerie));
    }

    private void centerToSelection() {
        if (mode == INDI_MODE) {
            if (!selectionEventSerie.isEmpty()) {
                makeVisible(selectionEventSerie.iterator().next());
            }
        } else {
            if (!selectionEvent.isEmpty()) {
                makeVisible(selectionEvent.iterator().next());
            }
        }
    }

    private void createLayers() {
        model.createIndiLayers();
        revalidate();
        repaint();
        scrollContent.setViewportView(content); // need to refresh vertical scroll bar
    }

    /**
     * The ruler 'at the top'
     */
    private class Ruler extends JComponent implements MouseMotionListener, ChangeListener {

        /**
         * init on add
         */
        @Override
        public void addNotify() {
            // continue with super
            super.addNotify();
            // setup listening
            addMouseMotionListener(this);
            Almanac.getInstance().addChangeListener(this);
            // ok this might not be fair but we'll increase
            // the tooltip dismiss delay now for everyone
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        }

        /**
         * un-init on remove
         */
        @Override
        public void removeNotify() {
            // setup listening
            removeMouseMotionListener(this);
            Almanac.getInstance().removeChangeListener(this);
            // continue with super
            super.removeNotify();
        }

        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            repaint();
        }

        /**
         * @see javax.swing.JComponent#paintComponent(Graphics)
         */
        @Override
        protected void paintComponent(Graphics g) {
            // let the renderer do its work
            rulerRenderer.cBackground = colors.get("background");
            rulerRenderer.cText = colors.get("text");
            rulerRenderer.cTick = rulerRenderer.cText;
            rulerRenderer.cTimespanM = colors.get("timespanM");
            rulerRenderer.cTimespanF = colors.get("timespanF");
            rulerRenderer.cTimespanU = colors.get("timespanU");
            rulerRenderer.acats = getAlmanacCategories();
            // prepare UnitGraphics
            UnitGraphics graphics = new UnitGraphics(
                    g,
                    DPC.getX() * cmPerYear,
                    getFontMetrics(getFont()).getHeight() + 1);
            graphics.translate(-model.min, 0);
            // let ruler do its things      
            rulerRenderer.render(graphics, model);
            // Set tooltips
            setTooltipText();
            // done
        }

        /**
         * @see java.awt.Component#getPreferredSize()
         */
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                    (int) ((model.max - model.min) * DPC.getX() * cmPerYear),   // content.getPreferredSize().width,
                    getFontMetrics(getFont()).getHeight() + 1);
        }

        /**
         * ignored
         */
        @Override
        public void mouseDragged(MouseEvent e) {
        }

        /**
         * update tip
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            // calculate year
            double year = pixel2year(e.getPoint().x);
            // calculate time and days around it
            PointInTime when = Model.toPointInTime(year);
            int days = (int) Math.ceil(5F / DPC.getX() / cmPerYear * 365);
            // collect events and their text
            WordBuffer text = new WordBuffer();
            int cursor = Cursor.DEFAULT_CURSOR;
            try {
                Iterator<Event> almanac = Almanac.getInstance().getEvents(when, days, getAlmanacCategories());
                if (almanac.hasNext()) {
                    text.append("<html><body>");
                    for (int i = 0; i < 10 && almanac.hasNext(); i++) {
                        text.append("<div width=\"" + TimelineView.this.getWidth() / 2 + "\">");
                        text.append(almanac.next());
                        text.append("</div>");
                    }
                    text.append("</body></html>");
                    cursor = Cursor.TEXT_CURSOR;
                }
            } catch (GedcomException ex) {
            }
            // set tooltip
            setCursor(Cursor.getPredefinedCursor(cursor));
            setToolTipText(text.length() == 0 ? null : text.toString());
            // done
        }
    } //Ruler

    /**
     * The content for displaying the timeline model
     */
    private class Content extends JComponent implements MouseListener {

        /**
         * constructor
         */
        private Content() {
            addMouseListener(this);
        }

        /**
         * ContextProvider - callback
         */
        //FIXME: only for selection callback
        private ViewContext getContext() {

            // context?
            Gedcom gedcom = model.getGedcom();
            if (gedcom == null) {
                return null;
            }

            List<Property> props = new ArrayList<Property>();
            if (mode == INDI_MODE) {
                for (Model.EventSerie eventSerie : selectionEventSerie) {
                    props.add(eventSerie.getProperty());
                }
            } else {
                for (Model.Event event : selectionEvent) {
                    props.add(event.pe);
                }
            }

            return new ViewContext(gedcom, null, props);
        }

        /**
         * @see java.awt.Component#getPreferredSize()
         */
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                    (int) ((model.max - model.min) * DPC.getX() * cmPerYear),
                    model.getLayersNumber(mode) * (getFontMetrics(getFont()).getHeight() + 1));
        }

        /**
         * @see javax.swing.JComponent#paintComponent(Graphics)
         */
        @Override
        protected void paintComponent(Graphics g) {

            // render selection?
            Boolean rsel = (Boolean) ((Graphics2D) g).getRenderingHint(RenderSelectionHintKey.KEY);
            if (rsel == null) {
                rsel = true;
            }

            // let the renderer do its work
            if (mode == INDI_MODE) {
                contentRenderer.selectionEventSerie = rsel ? selectionEventSerie : Collections.<Model.EventSerie>emptySet();
            } else {
                contentRenderer.selectionEvent = rsel ? selectionEvent : Collections.<Model.Event>emptySet();
            }
            
            contentRenderer.cBackground = colors.get("background");
            contentRenderer.cText = colors.get("text");
            contentRenderer.cDate = colors.get("date");
            contentRenderer.cTag = colors.get("tag");
            contentRenderer.cTimespanM = colors.get("timespanM");
            contentRenderer.cTimespanF = colors.get("timespanF");
            contentRenderer.cTimespanU = colors.get("timespanU");
            contentRenderer.cGrid = colors.get("grid");
            contentRenderer.cSelected = colors.get("selected");
            contentRenderer.paintDates = isPaintDates;
            contentRenderer.paintGrid = isPaintGrid;
            contentRenderer.paintTags = isPaintTags;

            // prepare UnitGraphics
            UnitGraphics graphics = new UnitGraphics(
                    g,
                    DPC.getX() * cmPerYear,
                    getFontMetrics(getFont()).getHeight() + 1);
            graphics.translate(-model.min, 0);

            // go for it      
            contentRenderer.render(graphics, model, mode);

            // done
        }

        @Override
        public void mouseClicked(MouseEvent e) {

            // selection?
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            if (!e.isShiftDown()) {
                selectionEvent.clear();
                selectionEventSerie.clear();
            }

            // find context click to select and tell about
            if (mode == INDI_MODE) {
                Model.EventSerie hit = getIndiAt(e.getPoint());
                if (hit != null) {
                    selectionEventSerie.add(hit);
                    // tell about it
                    SelectionDispatcher.fireSelection(e, getContext());
                }
            } else {
                Model.Event hit = getEventAt(e.getPoint());
                if (hit != null) {
                    selectionEvent.add(hit);
                    // tell about it
                    SelectionDispatcher.fireSelection(e, getContext());
                }
            }

            // show
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }
    } //Content

    /**
     * Listening to changes on cm per year (slider)
     */
    private class ChangeCmPerYear implements ChangeListener {

        /** @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent) */
        @Override
        public void stateChanged(ChangeEvent e) {
            double center = centeredYear;
            int layer = scrollContent.getVerticalScrollBar().getValue() / (getFontMetrics(getFont()).getHeight() + 1);
            // get the new value
            cmPerYear = MIN_CM_PER_YEAR + Math.exp(sliderCmPerYear.getValue() * 0.1) / Math.exp(10) * (MAX_CM_PER_YEAR - MIN_CM_PER_YEAR);
            // update model
            model.setTimePerEvent(cmBefEvent / cmPerYear, cmAftEvent / cmPerYear);
            // re-center
            if ((mode == INDI_MODE && !selectionEventSerie.isEmpty()) || (mode == EVENT_MODE && !selectionEvent.isEmpty())) {
                centerToSelection();
            } else {
                scroll2year(center);
                scroll2layer(layer);
            }
            
            // update tootip
            setTooltipText();
            // done
        }
    } //ChangeCmPerYear

    /**
     * We're also listening to the model
     */
    private class ModelListener implements Model.Listener {

        /**
         * @see genj.timeline.Model.Listener#dataChanged()
         */
        @Override
        public void dataChanged() {
            repaint();
        }

        /**
         * @see genj.timeline.Model.Listener#structureChanged()
         */
        @Override
        public void structureChanged() {
            ruler.revalidate();
            content.revalidate();
            repaint();
        }
    } // ModelListener

    private class Settings extends SettingsAction {

        @Override
        protected TimelineViewSettings getEditor() {
            return new TimelineViewSettings(TimelineView.this);
        }
    }
    
    /**
     * Action to toggle between individual mode or event mode
     */
    private class ToggleModeAction extends AbstractAncestrisAction {

        private ImageIcon indiIcon, eventIcon;
        
        /**
         * Constructor
         */
        private ToggleModeAction() {
            indiIcon = new ImageIcon(this, "indi");
            eventIcon = new ImageIcon(this, "event");
            setIcon();
            setTip();
        }
        
        private void setIcon() {
            setImage(mode == INDI_MODE ? indiIcon : eventIcon);
            cttiButton.setEnabled(mode == INDI_MODE);
        }

        private void setTip() {
            setTip(resources.getString(mode == INDI_MODE ? "toggle.toEvent.tip" : "toggle.toIndi.tip"));
        }
        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            mode = 1 - mode;
            setIcon();
            setTip();
            revalidate();
            repaint();
            scrollContent.setViewportView(content); // need to refresh vertical scroll bar
            centerToSelection();
        }

    } 

    /**
     * Action to center to selection
     */
    private class CenterTreeToIndividual extends AbstractAncestrisAction {

        /**
         * Constructor
         */
        private CenterTreeToIndividual() {
            setImage(new ImageIcon(this, "centertree"));
            setTip();
        }
        
        private void setTip() {
            if (selectionEventSerie.isEmpty()) {
                setTip(resources.getString("centertree.tip.none"));
            } else {
                setTip(resources.getString("centertree.tip", selectionEventSerie.iterator().next()));
            }
        }
        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            createLayers();
            repaint();
            centerToSelection();
        }

    } 

    /**
     * Action to center to selection
     */
    private class CenterToSelectionAction extends AbstractAncestrisAction {

        /**
         * Constructor
         */
        private CenterToSelectionAction() {
            setImage(new ImageIcon(this, "root"));
            setTip();
        }
        
        public void setTip() {
            if (selectionEventSerie.isEmpty()) {
                setTip(resources.getString("root.tip.none"));
            } else {
                setTip(resources.getString("root.tip", selectionEventSerie.iterator().next()));
            }
        }
        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            centerToSelection();
        }

    } 
    
} //TimelineView
