/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.renderer.DPI;
import genj.renderer.Options;
import genj.renderer.RenderSelectionHintKey;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.view.ContextProvider;
import genj.view.ScreenshotAction;
import genj.view.SelectionSink;
import genj.view.SettingsAction;
import genj.view.ToolBar;
import genj.view.View;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
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
  /*package*/ Map<String,Color> colors = new HashMap<String, Color>();
    
  /** our model */
  private Model model;
  
  /** our content */
  private Content content;
  
  /** our current selection */
  private Set<Model.Event> selection = new HashSet<Model.Event>();
  
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
  /*package*/ final static double 
    MIN_CM_PER_YEAR =  0.1D,
    DEF_CM_PER_YEAR =  1.0D,
    MAX_CM_PER_YEAR = 10.0D,
    
    MIN_CM_BEF_EVENT = 0.1D,
    DEF_CM_BEF_EVENT = 0.5D,
    MAX_CM_BEF_EVENT = 2.0D,

    MIN_CM_AFT_EVENT = 2.0D,
    DEF_CM_AFT_EVENT = 2.0D,
    MAX_CM_AFT_EVENT = 9.0D;
    
  /** centimeters per year/event */
  private double 
    cmPerYear = DEF_CM_PER_YEAR,
    cmBefEvent = DEF_CM_BEF_EVENT,
    cmAftEvent = DEF_CM_AFT_EVENT;
    
  /** the centered year */
  private double centeredYear = 0;
  
  /** settings */
  private boolean 
    isPaintDates = true,
    isPaintGrid = false,
    isPaintTags = true;

  /** registry we keep */
  private final static Registry REGISTRY = Registry.get(TimelineView.class);
  
  private ModelListener callback = new ModelListener();
    
  /**
   * Constructor
   */
  public TimelineView() {
    
    // remember
    DPI = Options.getInstance().getDPI();
    DPC = new Point2D.Float(
      DPI.horizontal() / 2.54F,
      DPI.vertical() / 2.54F
    );

    // read some stuff from registry
    cmPerYear = Math.max(MIN_CM_PER_YEAR, Math.min(MAX_CM_PER_YEAR, REGISTRY.get("cmperyear", (float)DEF_CM_PER_YEAR)));
    cmBefEvent = Math.max(MIN_CM_BEF_EVENT, Math.min(MAX_CM_BEF_EVENT, REGISTRY.get("cmbefevent", (float)DEF_CM_BEF_EVENT)));
    cmAftEvent = Math.max(MIN_CM_AFT_EVENT, Math.min(MAX_CM_AFT_EVENT, REGISTRY.get("cmaftevent", (float)DEF_CM_AFT_EVENT)));
    isPaintDates = REGISTRY.get("paintdates", true);
    isPaintGrid  = REGISTRY.get("paintgrid" , false);
    isPaintTags  = REGISTRY.get("painttags" , false);

    colors.put("background", Color.WHITE);
    colors.put("text"      , Color.BLACK);
    colors.put("tag"       , Color.GREEN);
    colors.put("date"      , Color.GRAY );
    colors.put("timespan"  , Color.BLUE );
    colors.put("grid"      , Color.LIGHT_GRAY);
    colors.put("selected"  , Color.RED  );
    colors = REGISTRY.get("color", colors);
   
    String[] ignored= REGISTRY.get("almanac.ignore", new String[0]);
    for (int i=0;i<ignored.length;i++)
      ignoredAlmanacCategories.add(ignored[i]);
    
    // create/keep our sub-parts
    model = new Model();
    model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
    
    String[] ps = REGISTRY.get("paths", (String[])null);
    if (ps!=null) {
      List<TagPath> paths = new ArrayList<TagPath>(ps.length);
      for (String p : ps) try {
        paths.add(new TagPath(p));
      } catch (Throwable t) {}
      model.setPaths(paths);
    }
    
    content = new Content();
    ruler = new Ruler();
    
    // all that fits in a scrollpane
    scrollContent = new ScrollPaneWidget(new ViewPortAdapter(content));
    scrollContent.setColumnHeaderView(new ViewPortAdapter(ruler));
    scrollContent.getViewport().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        // easy : translation and remember
        int x = scrollContent.getViewport().getViewPosition().x + scrollContent.getViewport().getSize().width/2;
        centeredYear = pixel2year(x);
      }
    });
   
    // layout
    setLayout(new BorderLayout());
    add(scrollContent, BorderLayout.CENTER);
    
    // scroll to last centered year
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        centeredYear = REGISTRY.get("centeryear", 0F);
        scroll2year(centeredYear);
      }
    });
    
    // done
  }

  public void addNotify() {
    // let super do its thing
    super.addNotify();
    // connect to model
    model.addListener(callback);
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    
    // disconnect from model
    model.removeListener(callback);
    
    // store stuff in registry
    REGISTRY.put("cmperyear"  , (float)Math.rint(cmPerYear*10)/10);
    REGISTRY.put("cmbefevent" , (float)cmBefEvent);
    REGISTRY.put("cmaftevent" , (float)cmAftEvent);
    REGISTRY.put("paintdates" , isPaintDates);
    REGISTRY.put("paintgrid"  , isPaintGrid);
    REGISTRY.put("painttags"  , isPaintTags);
    REGISTRY.put("filter"     , model.getPaths());
    REGISTRY.put("centeryear" , (float)centeredYear);
    REGISTRY.put("color", colors);
    REGISTRY.put("paths", model.getPaths());
    
    String[] ignored = new String[ignoredAlmanacCategories.size()];
    for (int i=0;i<ignored.length;i++)
      ignored[i] = ignoredAlmanacCategories.get(i).toString();
    REGISTRY.put("almanac.ignore", ignored);

    // done
    super.removeNotify();
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,256);
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
   * Sets the time allocated per event
   */
  public void setCMPerEvents(double before, double after) {
    // remember
    cmBefEvent = before;
    cmAftEvent = after;
    // update model
    model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
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
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(ToolBar toolbar) {

    // create a slider for cmPerYear
    int value = (int)(
      Math.log( (cmPerYear-MIN_CM_PER_YEAR) / (MAX_CM_PER_YEAR-MIN_CM_PER_YEAR) * Math.exp(10) ) * 10
    );

    sliderCmPerYear = new SliderWidget(1, 100, Math.min(100, Math.max(1,value)));
    sliderCmPerYear.setToolTipText(resources.getString("view.peryear.tip"));
    sliderCmPerYear.addChangeListener(new ChangeCmPerYear());
    sliderCmPerYear.setOpaque(false);

    toolbar.add(sliderCmPerYear);
    
    toolbar.add(new Settings());
    toolbar.add(new ScreenshotAction(content));
    
  }

  /**
   * callback - context event
   */
  public void setContext(Context context, boolean isActionPerformed) {

    if (context==null) {
      model.setGedcom(null);
      selection.clear();
    } else {
      model.setGedcom(context.getGedcom());
      selection = model.getEvents(context);
    }
    
    // do a repaint, too
    content.repaint();
      
    // done
  }

  /**
   * Returns the event at given position
   */
  protected Model.Event getEventAt(Point pos) {
    double year = pixel2year(pos.x);
    int layer = pos.y/(getFontMetrics(getFont()).getHeight()+1);
    return model.getEvent(year, layer);
  }
  
  /** 
   * Calculates a year from given pixel position
   */
  protected double pixel2year(int x) {
    return model.min + x/(DPC.getX()*cmPerYear);
  }

  /** 
   * Scrolls so that given year is centered in view
   */
  protected void scroll2year(double year) {
    centeredYear = year;
    int x = (int)((year-model.min)*DPC.getX()*cmPerYear) - scrollContent.getViewport().getWidth()/2;
    scrollContent.getHorizontalScrollBar().setValue(x);
    scrollContent.getHorizontalScrollBar().setUnitIncrement((int)(DPC.getX()*cmPerYear));
  }
  
  /**
   * Make sure the given event is visible
   */
  protected void makeVisible(Model.Event event) {
    double 
      min = model.min + scrollContent.getHorizontalScrollBar().getValue()/DPC.getX()/cmPerYear,
      max = min + scrollContent.getViewport().getWidth()/DPC.getX()/cmPerYear;

    if (event.to>max || event.from<min)      
      scroll2year(event.from);
  }
    
  /**
   * The ruler 'at the top'
   */
  private class Ruler extends JComponent implements MouseMotionListener, ChangeListener {
    
    /**
     * init on add
     */
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
    public void stateChanged(ChangeEvent e) {
      repaint();
    }
    
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // let the renderer do its work
      rulerRenderer.cBackground = (Color)colors.get("background");
      rulerRenderer.cText = (Color)colors.get("text");
      rulerRenderer.cTick = rulerRenderer.cText;
      rulerRenderer.cTimespan = (Color)colors.get("timespan");
      rulerRenderer.acats = getAlmanacCategories();
      // prepare UnitGraphics
      UnitGraphics graphics = new UnitGraphics(
        g,
        DPC.getX()*cmPerYear, 
        getFontMetrics(getFont()).getHeight()+1
      );
      graphics.translate(-model.min,0);
      // let ruler do its things      
      rulerRenderer.render(graphics, model);
      // done
    }
  
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      return new Dimension(
        content.getPreferredSize().width,
        getFontMetrics(getFont()).getHeight()+1
      );
    }
    
    /**
     * ignored
     */
    public void mouseDragged(MouseEvent e) {
    }

    /**
     * update tip
     */
    public void mouseMoved(MouseEvent e) {
      // calculate year
      double year = pixel2year(e.getPoint().x);
      // calculate time and days around it
      PointInTime when = Model.toPointInTime(year);
      int days = (int)Math.ceil(5F/DPC.getX()/cmPerYear*365);
      // collect events and their text
      WordBuffer text = new WordBuffer();
      int cursor = Cursor.DEFAULT_CURSOR;
      try {
	      Iterator<Event> almanac = Almanac.getInstance().getEvents(when, days, getAlmanacCategories());
	      if (almanac.hasNext()) {
		      text.append("<html><body>");
		      for (int i=0;i<10&&almanac.hasNext();i++) {
		        text.append("<div width=\""+TimelineView.this.getWidth()/2+"\">");
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
      setToolTipText(text.length()==0 ? null : text.toString());
      // done
    }
    
  } //Ruler

  /**
   * The content for displaying the timeline model
   */
  private class Content extends JComponent implements MouseListener, ContextProvider {
    
    /**
     * constructor
     */
    private Content() {
      addMouseListener(this);
    }

    /**
     * ContextProvider - callback
     */
    public ViewContext getContext() {
      
      // context?
      Gedcom gedcom = model.getGedcom();
      if (gedcom==null)
        return null;
      
      List<Property> props = new ArrayList<Property>();
      for (Model.Event event : selection) 
        props.add(event.pe);
      
      return new ViewContext(gedcom, null, props);
    }
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      return new Dimension(
        (int)((model.max-model.min) * DPC.getX()*cmPerYear),
         model.layers.size()  * (getFontMetrics(getFont()).getHeight()+1)
      );
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      
      // render selection?
      Boolean rsel = (Boolean) ((Graphics2D)g).getRenderingHint(RenderSelectionHintKey.KEY);
      if (rsel==null)
        rsel = true;
      
      // let the renderer do its work
      contentRenderer.selection = rsel ? selection : Collections.<Model.Event>emptySet();
      contentRenderer.cBackground = (Color)colors.get("background" );
      contentRenderer.cText       = (Color)colors.get("text"    );
      contentRenderer.cDate       = (Color)colors.get("date"    );
      contentRenderer.cTag        = (Color)colors.get("tag"     );
      contentRenderer.cTimespan   = (Color)colors.get("timespan");
      contentRenderer.cGrid       = (Color)colors.get("grid"    );
      contentRenderer.cSelected   = (Color)colors.get("selected");
      contentRenderer.paintDates = isPaintDates;
      contentRenderer.paintGrid = isPaintGrid;
      contentRenderer.paintTags = isPaintTags;
      
      // prepare UnitGraphics
      UnitGraphics graphics = new UnitGraphics(
        g,
        DPC.getX()*cmPerYear, 
        getFontMetrics(getFont()).getHeight()+1
      );
      graphics.translate(-model.min,0);

      // go for it      
      contentRenderer.render(graphics, model);
      
      // done
    }
    
    public void mouseClicked(MouseEvent e) {
      
      // selection?
      if (e.getButton()!=MouseEvent.BUTTON1)
        return;
      
      if (!e.isShiftDown())
        selection.clear();
      
      // find context click to select and tell about
      Model.Event hit = getEventAt(e.getPoint());
      if (hit!=null) {
        selection.add(hit);
        
        // tell about it
        SelectionSink.Dispatcher.fireSelection(e, getContext());
      }
      
      // show
      repaint();
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
  } //Content
  
  /**
   * Listening to changes on cm per year (slider)
   */
  private class ChangeCmPerYear implements ChangeListener {
    /** @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent) */
    public void stateChanged(ChangeEvent e) {
      double center = centeredYear;
      // get the new value
      cmPerYear = MIN_CM_PER_YEAR + 
         Math.exp(sliderCmPerYear.getValue()*0.1)/Math.exp(10) * (MAX_CM_PER_YEAR-MIN_CM_PER_YEAR);
      // update model
      model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
      // re-center
      scroll2year(center);
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
    public void dataChanged() {
      repaint();
    }
    /**
     * @see genj.timeline.Model.Listener#structureChanged()
     */
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

} //TimelineView
