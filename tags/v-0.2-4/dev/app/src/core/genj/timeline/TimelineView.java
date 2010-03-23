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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.time.PointInTime;
import genj.renderer.Options;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowBroadcastEvent;
import genj.window.WindowBroadcastListener;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends JPanel implements WindowBroadcastListener, ToolBarSupport {

  /** the units we use */
  private final Point DPI;
  private final Point2D DPC;
  
  /** resources */
  private Resources resources = Resources.get(this);
  
  /** keeping track of our colors */
  /*package*/ Map colors = new HashMap();
    
  /** our model */
  private Model model;
  
  /** our content */
  private Content content;
  
  /** our current selection */
  private Set selectedEvents = new HashSet();
  
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
  private List ignoredAlmanacCategories = new ArrayList();
  
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
  private Registry regstry;
  
  /** the view manager */
  private ViewManager manager;
  
  private ModelListener callback = new ModelListener();
    
  /**
   * Constructor
   */
  public TimelineView(String title, Gedcom gedcom, Registry registry, ViewManager mgr) {
    
    // remember
    manager = mgr;
    DPI = Options.getInstance().getDPI();
    DPC = new Point2D.Float(
      DPI.x / 2.54F,
      DPI.y / 2.54F
    );

    // read some stuff from registry
    regstry = registry;
    cmPerYear = Math.max(MIN_CM_PER_YEAR, Math.min(MAX_CM_PER_YEAR, regstry.get("cmperyear", (float)DEF_CM_PER_YEAR)));
    cmBefEvent = Math.max(MIN_CM_BEF_EVENT, Math.min(MAX_CM_BEF_EVENT, regstry.get("cmbefevent", (float)DEF_CM_BEF_EVENT)));
    cmAftEvent = Math.max(MIN_CM_AFT_EVENT, Math.min(MAX_CM_AFT_EVENT, regstry.get("cmaftevent", (float)DEF_CM_AFT_EVENT)));
    isPaintDates = regstry.get("paintdates", true);
    isPaintGrid  = regstry.get("paintgrid" , false);
    isPaintTags  = regstry.get("painttags" , false);

    colors.put("background", Color.WHITE);
    colors.put("text"      , Color.BLACK);
    colors.put("tag"       , Color.GREEN);
    colors.put("date"      , Color.GRAY );
    colors.put("timespan"  , Color.BLUE );
    colors.put("grid"      , Color.LIGHT_GRAY);
    colors.put("selected"  , Color.RED  );
    colors = regstry.get("color", colors);
   
    String[] ignored= regstry.get("almanac.ignore", new String[0]);
    for (int i=0;i<ignored.length;i++)
      ignoredAlmanacCategories.add(ignored[i]);
    
    // create/keep our sub-parts
    model = new Model(gedcom, regstry.get("filter", (String[])null));
    model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
    content = new Content();
    ruler = new Ruler();
    
    // all that fits in a scrollpane
    scrollContent = new JScrollPane(new ViewPortAdapter(content));
    scrollContent.setColumnHeaderView(new ViewPortAdapter(ruler));
    scrollContent.getHorizontalScrollBar().addAdjustmentListener(new ChangeCenteredYear());
   
    // layout
    setLayout(new BorderLayout());
    add(scrollContent, BorderLayout.CENTER);
    
    // scroll to last centered year
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        centeredYear = regstry.get("centeryear", 0F);
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
    regstry.put("cmperyear"  , (float)Math.rint(cmPerYear*10)/10);
    regstry.put("cmbefevent" , (float)cmBefEvent);
    regstry.put("cmaftevent" , (float)cmAftEvent);
    regstry.put("paintdates" , isPaintDates);
    regstry.put("paintgrid"  , isPaintGrid);
    regstry.put("painttags"  , isPaintTags);
    regstry.put("filter"     , model.getPaths());
    regstry.put("centeryear" , (float)centeredYear);
    regstry.put("color", colors);
    
    String[] ignored = new String[ignoredAlmanacCategories.size()];
    for (int i=0;i<ignored.length;i++)
      ignored[i] = ignoredAlmanacCategories.get(i).toString();
    regstry.put("almanac.ignore", ignored);

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
  public Set getAlmanacCategories() {
    HashSet result = new HashSet(Almanac.getInstance().getCategories());
    result.removeAll(ignoredAlmanacCategories);
    return result;
  }
  
  /**
   * Accessor - hidden almanac category keys
   */
  public void setAlmanacCategories(Set set) {
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
  public void populate(JToolBar bar) {
    
    // create a slider for cmPerYear
    int value = (int)(
      Math.log( (cmPerYear-MIN_CM_PER_YEAR) / (MAX_CM_PER_YEAR-MIN_CM_PER_YEAR) * Math.exp(10) ) * 10
    );

    sliderCmPerYear = new SliderWidget(1, 100, Math.min(100, Math.max(1,value)));
    sliderCmPerYear.setToolTipText(resources.getString("view.peryear.tip"));
    sliderCmPerYear.addChangeListener(new ChangeCmPerYear());
    bar.add(sliderCmPerYear);
    
    // done
  }

  /**
   * callback - context event
   */
  public boolean handleBroadcastEvent(WindowBroadcastEvent event) {
    
    // ignore outbound or !ContextSelectionEvent
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event, model.gedcom);
    if (event.isOutbound() || cse==null) 
      return true;
      
    // assemble selection
    selectedEvents = model.getEvents(cse.getContext());
    
    // do a repaint, too
    content.repaint();
      
    // done
    return false;
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
	      Iterator almanac = Almanac.getInstance().getEvents(when, days, getAlmanacCategories());
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
      ViewContext ctx = new ViewContext(model.gedcom);
      for (Iterator events = selectedEvents.iterator(); events.hasNext();) {
        Model.Event event = (Model.Event) events.next();
        ctx.addProperty(event.pe);
        //ctx.addProperty(event.pd);
      }
      return ctx;
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
      
      // let the renderer do its work
      contentRenderer.selection = selectedEvents;
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
        selectedEvents.clear();
      
      // find context click to select and tell about
      Model.Event hit = getEventAt(e.getPoint());
      if (hit!=null) {
        selectedEvents.add(hit);
        
        // tell about it
        WindowManager.broadcast(new ContextSelectionEvent(getContext(), this));
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
   * Listening to changes on the scrollpane
   */
  private class ChangeCenteredYear implements AdjustmentListener {
    /** @see java.awt.event.AdjustmentListener#adjustmentValueChanged(AdjustmentEvent) */
    public void adjustmentValueChanged(AdjustmentEvent e) {
      // swing's scrollbar doesn't distinguish between user-input
      // scrolling and propagated changes in its model (e.g. because of resize)\
      // we only update the centeredYear if getValueIsAdjusting()==true
      if (scrollContent.getHorizontalScrollBar().getValueIsAdjusting()) {
        // easy : translation and remember
        int x = scrollContent.getHorizontalScrollBar().getValue() + scrollContent.getViewport().getWidth()/2;
        centeredYear = pixel2year(x);
      } else {
        // no adjusting means we scroll back to 'our' remembered center
        // that means scrolling with the bar's buttons will not work!
        scroll2year(centeredYear);
      }
    }
  } //ChangeScroll 
  
  /**
   * Listening to changes on cm per year (slider)
   */
  private class ChangeCmPerYear implements ChangeListener {
    /** @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent) */
    public void stateChanged(ChangeEvent e) {
      // get the new value
      cmPerYear = MIN_CM_PER_YEAR + 
         Math.exp(sliderCmPerYear.getValue()*0.1)/Math.exp(10) * (MAX_CM_PER_YEAR-MIN_CM_PER_YEAR);
      // update model
      model.setTimePerEvent(cmBefEvent/cmPerYear, cmAftEvent/cmPerYear);
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
  
} //TimelineView
