/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
 * Copyright (C) 2016 - 2017 Frederic Lapeyre <frederic@ancestris.org>
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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.ActionSaveViewAsGedcom;
import ancestris.swing.ToolBar;
import ancestris.view.SelectionDispatcher;
import genj.almanac.Almanac;
import genj.almanac.Event;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.io.Filter;
import genj.renderer.DPI;
import genj.renderer.RenderOptions;
import genj.renderer.RenderSelectionHintKey;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ImageIcon;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.view.ScreenshotAction;
import genj.view.SelectionListener;
import genj.view.SettingsAction;
import genj.view.View;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;

/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends View implements SelectionListener, Filter {

    /**
     * the units we use
     */
    private final DPI DPI;
    private final Point2D DPC;
    /**
     * resources
     */
    private Resources resources = Resources.get(this);
    private String prefix = resources.getString("info.almanac") + "-";

    /**
     * keeping track of our colors
     */
    /* package */ Map<String, Color> colors = new HashMap<>();
    /**
     * our model
     */
    private Model model;
    /**
     * our content
     */
    private final Content content;
    /**
     * our current selection
     */
    private List<Model.Event> selectionEvent = new LinkedList<>();
    private List<Model.EventSerie> selectionEventSerie = new LinkedList<>();
    /**
     * our ruler
     */
    private final Ruler ruler;
    /**
     * our slider for cm per year
     */
    private SliderWidget sliderCmPerYear;
    /**
     * our scrollpane
     */
    private JScrollPane scrollContent;
    /**
     * the renderer we use for the ruler
     */
    private RulerRenderer rulerRenderer = new RulerRenderer();
    /**
     * the renderer we use for the content
     */
    private ContentRenderer contentRenderer = new ContentRenderer();
    /**
     * almanac categories
     */
    private List<String> ignoredAlmanacCategories = new ArrayList<>();
    private List<String> ignoredAlmanacsList = new ArrayList<>();
    private int almanacSigLevel = AlmanacPanel.MAX_SIG;

    /**
     * min/max's
     */
    /* package */ final static double MIN_CM_PER_YEAR = 0.1D,
            DEF_CM_PER_YEAR = 1.0D,
            MAX_CM_PER_YEAR = 20.0D,
            MIN_CM_BEF_EVENT = 0.1D,
            DEF_CM_BEF_EVENT = 0.5D,
            MAX_CM_BEF_EVENT = 2.0D,
            MIN_CM_AFT_EVENT = 2.0D,
            DEF_CM_AFT_EVENT = 2.0D,
            MAX_CM_AFT_EVENT = 20.0D;
    /**
     * centimeters per year/event
     */
    private double cmPerYear = DEF_CM_PER_YEAR,
            cmBefEvent = DEF_CM_BEF_EVENT,
            cmAftEvent = DEF_CM_AFT_EVENT;
    /**
     * default font height
     */
    private int defaultLineHeight = 0;
    private int defaultFontHeight = 0;
    private String fontName = "Arial";
    private Font currentFont;
    /**
     * the centered year
     */
    private double centeredYear = 0;
    /**
     * settings
     */
    private boolean isPaintDates = true,
            isPaintGrid = false,
            isPaintTags = true,
            isPackIndi = false;

    /**
     * registry we keep
     */
    private final static Registry REGISTRY = Registry.get(TimelineView.class);
    private ModelListener callback = new ModelListener();
    /**
     * modes
     */
    public static int INDI_MODE = 0;
    public static int EVENT_MODE = 1;
    public int mode = INDI_MODE;
    private CenterToSelectionAction ctsButton;
    private CenterTreeToIndividual cttiButton;
    private JLabel rootTitle;
    /**
     * filter indis
     */
    private List<Indi> filteredIndis;

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
        colors.put("selectedBg", new Color(254, 255, 150)); // same color as in the Cygnus editor for selection of individual

        String[] ignoredNames = REGISTRY.get("almanac.ignorenames", new String[0]);
        ignoredAlmanacsList.addAll(Arrays.asList(ignoredNames));
        String[] ignored = REGISTRY.get("almanac.ignore", new String[0]);
        ignoredAlmanacCategories.addAll(Arrays.asList(ignored));
        almanacSigLevel = REGISTRY.get("almanac.siglevel", (int) AlmanacPanel.MAX_SIG);

        mode = REGISTRY.get("display.mode", mode);

        // create/keep our sub-parts
        model = new Model(TimelineView.this);
        model.setTimePerEvent(cmBefEvent / cmPerYear, cmAftEvent / cmPerYear, cmPerYear, false);
        model.setPackIndi(isPackIndi, false);

        String[] ps = REGISTRY.get("paths", (String[]) null);
        if (ps != null) {
            List<TagPath> paths = new ArrayList<>(ps.length);
            for (String p : ps) {
                try {
                    paths.add(new TagPath(p));
                } catch (IllegalArgumentException t) {
                }
            }
            model.setPaths(paths, true); // rebuild all
        } else {
            model.setPaths(null, false);
        }

        // Init panels
        ruler = new Ruler();
        content = new Content();
        
        // set default font height
        fontName = REGISTRY.get("fontName", UIManager.getDefaults().getFont("ScrollPane.font").getFontName());
        defaultFontHeight = REGISTRY.get("fontSize", getFontMetrics(UIManager.getDefaults().getFont("ScrollPane.font")).getHeight() + 1);
        updateLineHeight();
        currentFont = new Font(fontName, Font.PLAIN, defaultFontHeight);
        setFont(currentFont);

        // all that fits in a scrollpane
        scrollContent = new ScrollPaneWidget(content);
        scrollContent.setViewportBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        scrollContent.setBackground(Color.WHITE);
        scrollContent.setColumnHeaderView(new ViewPortAdapter(ruler));
        scrollContent.setFont(currentFont);
        scrollContent.getViewport().addChangeListener((ChangeEvent e) -> {
            // easy : translation and remember
            double x1 = scrollContent.getViewport().getViewPosition().x + scrollContent.getViewport().getSize().width / 2;
            centeredYear = pixel2year(x1);
        });

        // layout
        setLayout(new BorderLayout());
        add(scrollContent, BorderLayout.CENTER);
        centeredYear = REGISTRY.get("centeryear", 0F);

        

        // done
    }

    @Override
    public void addNotify() {
        // let super do its thing
        super.addNotify();
        // connect to model
        model.addListener(callback);
        // Used only for Filter interface
        AncestrisPlugin.register(this);
    }

    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    @Override
    public void removeNotify() {

        // disconnect from model
        model.removeListener(callback);

        // store stuff in registry
        saveInRegistry();

        // done
        AncestrisPlugin.unregister(this);
        super.removeNotify();
    }

    public void saveInRegistry() {

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
        REGISTRY.put("fontName", fontName);
        REGISTRY.put("fontSize", defaultFontHeight);

        String[] ignoredNames = new String[ignoredAlmanacsList.size()];
        for (int i = 0; i < ignoredNames.length; i++) {
            ignoredNames[i] = ignoredAlmanacsList.get(i);
        }
        REGISTRY.put("almanac.ignorenames", ignoredNames);

        String[] ignored = new String[ignoredAlmanacCategories.size()];
        for (int i = 0; i < ignored.length; i++) {
            ignored[i] = ignoredAlmanacCategories.get(i);
        }
        REGISTRY.put("almanac.ignore", ignored);
        REGISTRY.put("almanac.siglevel", almanacSigLevel);

        REGISTRY.put("display.mode", mode);
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
     * Accessor - the model
     */
    public void eraseModel() {
        model.eraseAll();
    }

    /**
     * Accessor - registry
     */
    public Registry getRegistry() {
        return REGISTRY;
    }

    /**
     * Accessor - almanac list
     */
    public List<String> getAlmanacList() {
        List<String> result = new ArrayList<>(Almanac.getInstance().getAlmanacs());
        result.removeAll(ignoredAlmanacsList);
        return result;
    }

    /**
     * Accessor - almanac categories
     */
    public List<String> getAlmanacCategories() {
        List<String> result = new ArrayList<>(Almanac.getInstance().getCategories());
        result.removeAll(ignoredAlmanacCategories);
        return result;
    }

    /**
     * Accessor - almanac list
     */
    public int getAlmanacSigLevel() {
        return almanacSigLevel;
    }

    /**
     * Accessor - hidden almanac category keys
     */
    public void setAlmanacs(Set<String> set) {
        ignoredAlmanacsList.clear();
        ignoredAlmanacsList.addAll(Almanac.getInstance().getAlmanacs());
        ignoredAlmanacsList.removeAll(set);
        repaint();
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
     * Accessor - set almanac importance level
     */
    public void setAlmanacSigLevel(int set) {
        almanacSigLevel = set;
        repaint();
    }

    /**
     * Accessor - get almanac color labels
     */
    public List<String> getAlmanacColorLabels() {
        List<String> ret = new ArrayList<>();
        Collator comparator = getCollator();
        comparator.setStrength(Collator.PRIMARY);
        List<String> alms = new ArrayList<>(Almanac.getInstance().getAlmanacs());
        Collections.sort(alms, comparator);
        alms.forEach((alm) -> {
            ret.add(prefix + alm);
        });
        return ret;
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
    public void setPackIndi(boolean set, boolean redraw) {
        isPackIndi = set;
        model.setPackIndi(set, redraw);
        repaint();
    }

    /**
     * Sets the time allocated per event
     */
    public void setCMPerEvents(double before, double after, boolean redraw) {
        // remember
        cmBefEvent = before;
        cmAftEvent = after;
        // update model
        model.setTimePerEvent(cmBefEvent / cmPerYear, cmAftEvent / cmPerYear, cmPerYear, redraw);
        repaint();
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

    public int getDefaulFontHeight() {
        return defaultFontHeight;
    }

    public void setFontSize(int defaulFontHeight) {
        this.defaultFontHeight = defaulFontHeight;
        currentFont = new Font(fontName, Font.PLAIN, defaulFontHeight);
        setFont(currentFont);
        scrollContent.setFont(currentFont);
        repaint();
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
        currentFont = new Font(fontName, Font.PLAIN, defaultFontHeight);
        setFont(currentFont);
        scrollContent.setFont(currentFont);
        repaint();
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
        ctsButton = new CenterToSelectionAction();
        cttiButton = new CenterTreeToIndividual();

        toolbar.setFloatable(false);
        toolbar.add(sliderCmPerYear);
        //toolbar.addSeparator();
        toolbar.add(new ToggleModeAction());
        toolbar.add(ctsButton);
        toolbar.add(cttiButton);
        toolbar.addSeparator();
        toolbar.add(new ScreenshotAction(content));
        rootTitle = new JLabel();
        rootTitle.setHorizontalAlignment(SwingConstants.CENTER);
        toolbar.add(rootTitle, "growx, pushx, center");
        rootTitle.setText("");
        toolbar.addSeparator();
        toolbar.add(new ActionSaveViewAsGedcom(content.getContext().getGedcom(), this));
        toolbar.add(new Settings());
    }

    public void setRootTitle(String title) {
        if (rootTitle != null && title != null) {
            rootTitle.setText("<html><center>" + resources.getString("root.name") + " " + title + "</center></html");
        }
    }

    /**
     * callback - context event
     */
    @Override
    public void setContext(Context context) {
        if (context == null) {
            model.setGedcom(null);
        } else {
            model.setGedcom(context);
        }
    }

    public void update() {
        selectionEvent = model.getEvents();
        selectionEventSerie = model.getIndis();
        updateLineHeight();
        int layer = scrollContent.getVerticalScrollBar().getValue() / defaultLineHeight;
        if ((mode == INDI_MODE && !selectionEventSerie.isEmpty()) || (mode == EVENT_MODE && !selectionEvent.isEmpty())) {
            centerToSelection();
        } else {
            scroll2year(centeredYear);
            scroll2layer(layer);
            ctsButton.setEnabled(false);
            ctsButton.setTip(false, "");
            cttiButton.setEnabled(false);
            cttiButton.setTip(false, "");
        }
        setTooltipText();
        repaint();
        scrollContent.setViewportView(content); // need to refresh vertical scroll bar
    }

    public void centerToSelection() {

        if (ctsButton == null || cttiButton == null || !model.isReady()) {
            return;
        }

        if (mode == INDI_MODE) {
            boolean enabled = !selectionEventSerie.isEmpty();
            Model.EventSerie eventSerie = enabled ? selectionEventSerie.get(selectionEventSerie.size() - 1) : null;
            String text = eventSerie == null ? "" : eventSerie.toString();
            ctsButton.setEnabled(enabled);
            ctsButton.setTip(enabled, text);
            cttiButton.setEnabled(enabled);
            cttiButton.setTip(enabled, text);
            if (enabled) {
                makeVisible(eventSerie);
            }
        } else {
            boolean enabled = !selectionEvent.isEmpty();
            Model.Event event = enabled ? selectionEvent.get(selectionEvent.size() - 1) : null;
            String text = event == null ? "" : event.toString();
            ctsButton.setEnabled(enabled);
            ctsButton.setTip(enabled, text);
            cttiButton.setEnabled(false);
            cttiButton.setTip(false, "");
            if (enabled) {
                makeVisible(event);
            }
        }
    }

    /**
     * Set tooltipText for slider and scrollbar
     */
    public void setTooltipText() {
        double cmPY = Math.floor(cmPerYear * 100) / 100;
        sliderCmPerYear.setToolTipText(cmPY + " " + resources.getString("view.peryear.tip") + " (" + sliderCmPerYear.getValue() + "%)");

        JScrollBar hsb = scrollContent.getHorizontalScrollBar();
        hsb.setUnitIncrement((int) (DPC.getX() * cmPerYear));
        String minYear = String.valueOf((int) pixel2year(hsb.getMinimum()));
        String year = String.valueOf((int) pixel2year(hsb.getValue()) + 1);
        String maxYear = String.valueOf((int) pixel2year(hsb.getMaximum()));
        hsb.setToolTipText(resources.getString("view.scrollyear.tip", minYear, year, maxYear));

        JScrollBar vsb = scrollContent.getVerticalScrollBar();
        vsb.setUnitIncrement((int) (2 * defaultLineHeight));
        String value = Integer.toString((int) vsb.getValue() / defaultLineHeight);
        String total = Integer.toString(model.getLayersNumber(mode));
        vsb.setToolTipText(resources.getString("view.scrolllayer.tip", value, total));
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
        int minX = scrollContent.getHorizontalScrollBar().getValue() + 10;
        int maxX = minX + scrollContent.getViewport().getWidth() - 90;
        int newX = (int) ((year - model.min) * DPC.getX() * cmPerYear);

        // Determines if newY is already Visible (between min and max)
        if ((maxX < minX) || (newX > minX && newX < maxX)) {
            return;
        }
        // Else show newY in the middle
        newX = newX - scrollContent.getViewport().getWidth() / 2;
        scrollContent.revalidate();
        scrollContent.getHorizontalScrollBar().setValue(newX);
    }

    protected void scroll2layer(int layer) {
        int windowHeight = scrollContent.getViewport().getHeight();
        int northBand = 2 * defaultLineHeight;
        int southBand = windowHeight - 4 * defaultLineHeight;
        int minY = scrollContent.getVerticalScrollBar().getValue() + northBand;
        int maxY = minY + southBand;
        int newY = (int) (layer * defaultLineHeight);

        // Determines if newY is already Visible (between min and max)
        if ((maxY < minY) || (newY > minY && newY < maxY)) {
            return;
        }

        // Else show newY in the middle
        if (newY <= minY && (minY - newY <= northBand)) {
            newY -= northBand;
        } else if (newY >= maxY && (newY - maxY <= southBand)) {
            newY -= southBand;
        } else {
            newY = (int) (newY - (windowHeight / 2));
        }
        if (newY < 0) {
            newY = 0;
        }
        scrollContent.revalidate();
        scrollContent.getVerticalScrollBar().setValue(newY);
    }

    /**
     * Make sure the given event is visible
     *
     */
    private void makeVisible(Model.Event event) {
        scroll2year(event.from);
        scroll2layer(model.getLayerFromEvent(event));
    }

    /**
     * Make sure the given event is visible
     *
     */
    private void makeVisible(Model.EventSerie eventSerie) {
        scroll2year(eventSerie.from);
        scroll2layer(model.getLayerFromEventSerie(eventSerie));
    }

    /**
     * Returns the event at given position
     */
    protected Model.Event getEventAt(Point pos) {
        double year = pixel2year(pos.x);
        int layer = pos.y / defaultLineHeight;
        return model.getEvent(year, layer);
    }

    /**
     * Returns the indi at given position
     */
    protected Model.EventSerie getIndiAt(Point pos) {
        double year = pixel2year(pos.x);
        int layer = pos.y / defaultLineHeight;
        return model.getEventSerie(year, layer);
    }

    public Collator getCollator() {
        if (content == null) {
            return Collator.getInstance(getLocale());
        }
        return content.getContext().getGedcom().getCollator();
    }

    @Override
    public String getFilterName() {
        return NbBundle.getMessage(TimelineView.class, "TTL_Filter", getIndividualsCount(), resources.getString("title"));
    }

    @Override
    public boolean veto(Property property) {
        // all non-entities are fine
        return false;
    }

    @Override
    public boolean veto(Entity entity) {
        // let submitter through if it's THE one
        if (entity == entity.getGedcom().getSubmitter()) {
            return false;
        }
        if (filteredIndis == null) {
            filteredIndis = model.getIndisFromLayers();
        }
        if (filteredIndis.contains(entity)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canApplyTo(Gedcom gedcom) {
        return (gedcom != null && gedcom.equals(model.getGedcom()));
    }

    private void updateLineHeight() {
        defaultLineHeight =  (int) (defaultFontHeight * 1.4) + 4;
    }

    @Override
    public int getIndividualsCount() {
        int sum = 0;
        for (Entity ent : model.getVisibleInviduals()) {
            if (ent instanceof Indi) {
                sum++;
            }
        }
        return sum;
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
         * @see
         * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
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
            getAlmanacColorLabels().forEach((alm) -> {
                colors.put(alm, Color.BLUE);
            });
            colors = REGISTRY.get("color", colors);
            rulerRenderer.cBackground = colors.get("background");
            rulerRenderer.cText = colors.get("text");
            rulerRenderer.cTick = rulerRenderer.cText;
            rulerRenderer.cTimespanM = colors.get("timespanM");
            rulerRenderer.cTimespanF = colors.get("timespanF");
            rulerRenderer.cTimespanU = colors.get("timespanU");
            rulerRenderer.almanacs = getAlmanacList();
            rulerRenderer.acats = getAlmanacCategories();
            rulerRenderer.sigLevel = getAlmanacSigLevel();
            // prepare UnitGraphics
            g.setFont(new Font(fontName, Font.PLAIN, defaultFontHeight));
            UnitGraphics graphics = new UnitGraphics(
                    g,
                    DPC.getX() * cmPerYear,
                    defaultLineHeight);
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
                    (int) ((model.max - model.min) * DPC.getX() * cmPerYear), // content.getPreferredSize().width,
                    defaultLineHeight);
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
            List<String> alms = getAlmanacList();
            int cursor = Cursor.DEFAULT_CURSOR;
            try {
                Iterator<Event> almanac = Almanac.getInstance().getEvents(when, days, getAlmanacList(), getAlmanacCategories(), getAlmanacSigLevel());
                if (almanac.hasNext()) {
                    text.append("<html><body><div width=\"" + TimelineView.this.getWidth() * 0.4 + "\"><ul>");
                    for (int i = 0; i < 10 && almanac.hasNext(); i++) {
                        Event event = almanac.next();
                        Color color = colors.get(prefix + event.getAlmanac());
                        String hex = "#" + Integer.toHexString(color.getRGB()).substring(2);
                        text.append("<li color=\"" + hex + "\">" + event.toString() + "</li><br>");
                    }
                    text.append("</ul></div>");
                    text.append("<div>" + resources.getString("almanac.legend"));
                    for (String alm : alms) {
                        Color color = colors.get(prefix + alm);
                        String hex = "#" + Integer.toHexString(color.getRGB()).substring(2);
                        text.append("<font color=\"" + hex + "\">" + alm + " " + "</font>");
                    }
                    text.append("</div>");
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

            List<Property> props = new LinkedList<>();
            if (mode == INDI_MODE) {
                selectionEventSerie.forEach((eventSerie) -> {
                    props.add(eventSerie.getProperty());
                });
            } else {
                selectionEvent.forEach((event) -> {
                    props.add(event.pe);
                });
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
                    model.getLayersNumber(mode) * defaultLineHeight);
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
                contentRenderer.selectionEventSerie = rsel ? selectionEventSerie : new LinkedList<>();
            } else {
                contentRenderer.selectionEvent = rsel ? selectionEvent : new LinkedList<>();
            }

            getAlmanacColorLabels().forEach((alm) -> {
                colors.put(alm, Color.BLUE);
            });
            colors = REGISTRY.get("color", colors);
            contentRenderer.cBackground = colors.get("background");
            contentRenderer.cText = colors.get("text");
            contentRenderer.cDate = colors.get("date");
            contentRenderer.cTag = colors.get("tag");
            contentRenderer.cTimespanM = colors.get("timespanM");
            contentRenderer.cTimespanF = colors.get("timespanF");
            contentRenderer.cTimespanU = colors.get("timespanU");
            contentRenderer.cGrid = colors.get("grid");
            contentRenderer.cSelected = colors.get("selected");
            contentRenderer.cSelectedBg = colors.get("selectedBg");
            contentRenderer.paintDates = isPaintDates;
            contentRenderer.paintGrid = isPaintGrid;
            contentRenderer.paintTags = isPaintTags;

            g.setFont(new Font(fontName, Font.PLAIN, defaultFontHeight));

            // prepare UnitGraphics
            UnitGraphics graphics = new UnitGraphics(
                    g,
                    DPC.getX() * cmPerYear,
                    defaultLineHeight);
            graphics.translate(-model.min, 0);

            // go for it     
            if (model.isReady()) {
                contentRenderer.render(graphics, model, mode);
            }

            // done
        }

        @Override
        public void mouseClicked(MouseEvent e) {

            // selection?
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            // find context click to select and tell about
            if (mode == INDI_MODE) {
                Model.EventSerie hit = getIndiAt(e.getPoint());
                if (hit != null) {
                    selectionEventSerie.clear();
                    selectionEventSerie.add(hit);
                    // tell about it
                    SelectionDispatcher.fireSelection(e, getContext());
                }
            } else {
                Model.Event hit = getEventAt(e.getPoint());
                if (hit != null) {
                    selectionEvent.clear();
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

        /**
         * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            // get the new value
            cmPerYear = MIN_CM_PER_YEAR + Math.exp(sliderCmPerYear.getValue() * 0.1) / Math.exp(10) * (MAX_CM_PER_YEAR - MIN_CM_PER_YEAR);
            // update model
            model.setTimePerEvent(cmBefEvent / cmPerYear, cmAftEvent / cmPerYear, cmPerYear, true);
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

        private final ImageIcon indiIcon, eventIcon;

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
            setTip(true, "");
        }

        private void setTip(boolean enabled, String text) {
            if (enabled) {
                setTip(resources.getString("centertree.tip", text));
            } else {
                setTip(resources.getString(mode == INDI_MODE ? "centertree.tip.none" : "centertree.tip.noneforthismode"));
            }
        }

        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            model.layoutLayers(true);
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
            setTip(true, "");
        }

        public void setTip(boolean enabled, String text) {
            if (enabled) {
                setTip(resources.getString("root.tip", text));
            } else {
                setTip(resources.getString("root.tip.none"));
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
