/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.window.WindowBroadcastEvent;
import genj.window.WindowBroadcastListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.RingVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.SquareVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;
import com.vividsolutions.jump.workbench.ui.zoom.PanTool;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomTool;

/**
 * The view showing gedcom data in geographic context
 */
public class GeoView extends JPanel implements WindowBroadcastListener, ToolBarSupport {
  
  /*package*/ final static Logger LOG = Logger.getLogger("genj.geo");
  
  private final static ImageIcon 
    IMG_MAP = new ImageIcon(GeoView.class, "images/Map.png"),
    IMG_ZOOM = new ImageIcon(GeoView.class, "images/Zoom.png"),
    IMG_ZOOM_EXTENT = new ImageIcon(GeoView.class, "images/ZoomExtend.png");
  
  private final static ImageIcon[] STATUS2IMG = {
    new ImageIcon(GeoView.class, "images/Ok.png"),
    new ImageIcon(GeoView.class, "images/Warning.png"),
    new ImageIcon(GeoView.class, "images/Error.png")
  };
  
  /*package*/ final static Resources RESOURCES = Resources.get(GeoView.class);
  
  /** gedcom we're looking at */
  private Gedcom gedcom;
  
  /** handle to view manager */
  private ViewManager viewManager;
  
  /** the current map */
  private GeoMap currentMap;
  
  /** split panel */
  private JSplitPane split;
  
  /** the current layer view panel */
  private LayerViewPanel layerPanel;
  
  /** action for update */
  private ActionLocate locate;
  
  /** registry */
  private Registry registry;
  
  /** our model & layer */
  private GeoModel model;
  private GeoList locationList;
  private LocationsLayer locationLayer;  
  private SelectionLayer selectionLayer;
  private CursorTool currentTool;
  
  /**
   * Constructor
   */
  public GeoView(String title, Gedcom gedcom, Registry registry) {
    
    // state to remember
    this.registry = registry;
    this.gedcom = gedcom;
    
    // create our model 
    model = new GeoModel();
    
    // create a location grid
    locationList = new GeoList(model, this, viewManager);
    
    // create layers
    locationLayer = new LocationsLayer();  
    selectionLayer = new SelectionLayer();
    
    // set layout
    split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, null, new JScrollPane(locationList));
    split.setResizeWeight(1.0D);
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, split);
    
    // done
  }
  
  /**
   * component lifecycle - we're needed
   */
  public void addNotify() {
    // override
    super.addNotify();
    // hook up gedcom with model
    model.setGedcom(gedcom);
    // show map 
    String map = registry.get("map", (String)null);
    GeoMap[] maps = GeoService.getInstance().getMaps();
    for (int i=0;i<maps.length;i++) {
      if (map==null||maps[i].getKey().equals(map)) {
        try { setMap(maps[i]); } catch (Throwable t) {}
        break;
      }
    }
    // done
  }
  
  /**
   * component lifecycle - we're not needed anymore
   */
  public void removeNotify() {
    // unhook gedcom from model
    model.setGedcom(null);
    // remember map
    if (currentMap!=null)
      registry.put("map", currentMap.getKey());
    // remember split
    registry.put("split", split.getDividerLocation());
    // override
    super.removeNotify();
  }
  
  /**
   * Callback for context changes
   */
  public boolean handleBroadcastEvent(WindowBroadcastEvent event) {
    // check for inbound context selection
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event, gedcom);
    if (event.isInbound() && cse!=null)
      locationList.setSelectedContext(cse.getContext());
    // continue
    return true;
  }
  
  /**
   * Callback for populating toolbar 
   */
  public void populate(JToolBar bar) {
    
    // get maps
    GeoMap[] maps = GeoService.getInstance().getMaps();
    List actions = new ArrayList(maps.length);
    for (int i=0;i<maps.length;i++) {
      actions.add(new ChooseMap(maps[i]));
    }

    // add a popup for them
    PopupWidget chooseMap = new PopupWidget(null, IMG_MAP, actions);
    chooseMap.setToolTipText(RESOURCES.getString("toolbar.map"));
    chooseMap.setEnabled(!actions.isEmpty());
    bar.add(chooseMap);
    
    // add zoom
    ButtonHelper bh = new ButtonHelper().setInsets(0);
    bh.setContainer(bar);
    bh.create(new ZoomExtent());
    bh.setButtonType(JToggleButton.class).create(new ZoomOnOff());
    
    // add locate button
    locate = new ActionLocate();
    bh.setButtonType(JButton.class).create(locate);
    
    
    // done
  }
  
  /**
   * Select a location
   */
  public void setSelection(GeoLocation location) {
    setSelection(location!=null ? Collections.singletonList(location) : Collections.EMPTY_LIST);
  }
  
  /**
   * Select a location
   */
  public void setSelection(Collection locations) {
    selectionLayer.setLocations(locations);
  }
  
  /**
   * Choose current map
   */
  public void setMap(GeoMap map) throws IOException {
    
    // keep
    currentMap = map;
    
    // setup layer manager and add our own feature collection that's wrapping the model
    LayerManager layerManager = new LayerManager();
    layerManager.addLayer("GenJ", locationLayer);
    layerManager.addLayer("GenJ", selectionLayer);
    
    selectionLayer.setLayerManager(layerManager);
    locationLayer.setLayerManager(layerManager);
  
    // load map
    map.load(layerManager);
    
    // pack into panel
    layerPanel = new LayerPanel(layerManager);
    layerPanel.setBackground(map.getBackground());
    if (currentTool!=null)
      layerPanel.setCurrentCursorTool(currentTool);
    
    // show
    split.setLeftComponent(layerPanel);
    revalidate();
    repaint();
    
    // set split position
    int pos = registry.get("split", -1);
    if (pos<0)
      split.setDividerLocation(0.7D);
    else {
      split.setLastDividerLocation(pos);
      split.setDividerLocation(pos);
    }
    
    // enable tooltips
    ToolTipManager.sharedInstance().registerComponent(layerPanel);
    
    // done
  }

  /**
   * View context for layer view panel
   */
  private class ViewContext implements LayerViewPanelContext {
    public void warnUser(String warning) {
      LOG.warning("[JUMP]"+warning);
    }
    public void handleThrowable(Throwable t) {
      LOG.log(Level.WARNING, "[JUMP]", t);
    }
    public void setStatusMessage(String message) {
      if (message!=null&&message.length()>0)
        LOG.warning("[JUMP]"+message);
    }
  }
  
  /**
   * Action - Zoom to Map Extent
   */
  private class ZoomExtent extends Action2 {

    /** constructor */
    private ZoomExtent() {
      setImage(IMG_ZOOM_EXTENT);
      setTip(RESOURCES, "toolbar.extent");
    }
    /** zoom to all */
    public void execute() {
      if (layerPanel!=null) try {
          layerPanel.getViewport().zoomToFullExtent();
        } catch (Throwable t) {
        }
      }
    
  } //ZoomAll

  /**
   * Action - Zoom On Off
   */
  private class ZoomOnOff extends Action2 {
    /** constructor */
    private ZoomOnOff() {
      setImage(IMG_ZOOM);
      setTip(RESOURCES, "toolbar.zoom");
    }
    /** choose current map */
    protected void execute() {
      currentTool =  currentTool instanceof ZoomTool ? (CursorTool)new PanTool(null) :  new ZoomTool(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      if (layerPanel!=null) 
        layerPanel.setCurrentCursorTool(currentTool);
    }
  }//ZoomOnOff
 
  /**
   * Action - choose a map
   */
  private class ChooseMap extends Action2 {
    private GeoMap map;
    /** constructor */
    private ChooseMap(GeoMap map) {
      this.map = map;
      setText(map.getName());
    }
    /** choose current map */
    protected void execute() {
      // remember split
      registry.put("split", split.getDividerLocation());
      // set it
      try {
        setMap(map);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    public Icon getImage() {
      return currentMap!=map ? null : IMG_MAP;
    }
  }//ChooseMap
 
  /**
   * A layer for our selection
   */
  private class SelectionLayer extends LocationsLayer{
    
    private List selection = Collections.EMPTY_LIST;
    
    /** constructor */
    private SelectionLayer() {
    }

    /** initializer */
    protected void initStyles() {
      
      // prepare some styles
      addStyle(new BasicStyle(Color.RED));
       
      VertexStyle vertices = new RingVertexStyle();
      vertices.setEnabled(true);
      vertices.setSize(5);
      addStyle(vertices);
       
      LabelStyle labels = new LabelStyle();
      labels.setEnabled(false);
      addStyle(labels);
      
      // done
    }
    
    /** set selection */
    public void setLocations(Collection set) {
      synchronized (this) {
        selection = new ArrayList(set);
      }
      LayerManager mgr = getLayerManager();
      if (mgr!=null)
        mgr.fireFeaturesChanged(new ArrayList(), FeatureEventType.ADDED, this);
    }
    
    /** geo model - a location has been updated */
    public void locationAdded(GeoLocation location) {
      setLocations(Collections.EMPTY_SET);
    }

    /** geo model - a location has been updated */
    public void locationUpdated(GeoLocation location) {
      setLocations(Collections.EMPTY_SET);
    }

    /** geo model - a location has been removed */
    public void locationRemoved(GeoLocation location) {
      setLocations(Collections.EMPTY_SET);
    }

    /** selection size */
    public int size() {
      return selection.size();
    }
    
    /** selection access */
    public List getFeatures() {
//      if (!selection.isEmpty()) {
//        GeoLocation one = (GeoLocation)selection.iterator().next();
//        FeatureSchema schema = getFeatureSchema();
//        int i =0;
//        //one.setAttribute(getFeatureSchema().getAttributeIndex())
//      }
      return selection;
    }
    
  } //SelectionLayer
  
  /**
   * A layer for our model's locations
   */
  private class LocationsLayer extends Layer implements FeatureCollection, GeoModelListener, ActionListener {
    
    private List locations = new ArrayList();
    
    protected Timer updateTimer;
    
    /** constructor */
    private LocationsLayer() {
      // prepare a timer for delayed updates
      updateTimer = new Timer(500, this);
      updateTimer.setRepeats(false);
      
      // connect us to Jumps internals
      setName(getClass().toString());
      setFeatureCollection(this);

      // listen to model
      model.addGeoModelListener(this);

      // init styles
      initStyles();
    }
    
    private void reset() {
      for (Iterator it = model.getLocations().iterator(); it.hasNext(); ) {
        GeoLocation location = (GeoLocation)it.next();
        if (location.isValid())
          locations.add(location);
      }
      updateTimer.start();
    }
    
    /** initializer */
    protected void initStyles() {
      
      // prepare some styles
      addStyle(new BasicStyle(Color.LIGHT_GRAY));
       
      VertexStyle vertices = new SquareVertexStyle();
      vertices.setEnabled(true);
      vertices.setSize(5);
      addStyle(vertices);
       
      LabelStyle labels = new LabelStyle();
      labels.setColor(Color.BLACK);
      labels.setEnabled(true);
      labels.setAttribute("PLAC");
      labels.setHidingOverlappingLabels(true);
      addStyle(labels);
      
      // done
    }
    
    /** timer - time to propagate update */
    public void actionPerformed(ActionEvent e) {
      LayerManager mgr = getLayerManager();
      if (mgr!=null)
        mgr.fireFeaturesChanged(new ArrayList(), FeatureEventType.ADDED, this);
    }
    
    /** geo model - done with async resolve */
    public void asyncResolveEnd(int status, String msg) {
      if (locate!=null) {
        locate.setEnabled(true);
        locate.setTip(msg + " - " + RESOURCES.getString("resolve.again"));
        locate.setImage(STATUS2IMG[status]);
      }
    }

    /** geo model - starts async resolve */
    public void asyncResolveStart() {
      if (locate!=null) locate.setEnabled(false);
    }

    /** geo model - a location has been added */
    public void locationAdded(GeoLocation location) {
      if (location.isValid())
        locations.add(location);
      updateTimer.start();
    }

    /** geo model - a location has been updated */
    public void locationUpdated(GeoLocation location) {
      if (location.isValid()&&!locations.contains(location))
        locations.add(location);
      updateTimer.start();
    }

    /** geo model - a location has been removed */
    public void locationRemoved(GeoLocation location) {
      locations.remove(location);
      updateTimer.start();
    }

    /** feature collection - our schema */
    public FeatureSchema getFeatureSchema() {
      return GeoLocation.SCHEMA;
    }

    /** feature collection - our envelope is empty by default */
    public Envelope getEnvelope() {
      return new Envelope();
    }
    
    /** feature collection - # of features */
    public int size() {
      return locations.size();
    }
    
    /** feature collection - feature access */
    public List getFeatures() {
      return locations;
    }
    
    /** feature collection - feature access */
    public List query(Envelope envelope) {
      List locations = getFeatures();
      ArrayList result = new ArrayList(locations.size());
      for (Iterator it = locations.iterator(); it.hasNext();) {
        Feature feature = (Feature) it.next();
        if (feature.getGeometry().getEnvelopeInternal().intersects(envelope)) 
          result.add(feature);
      }
      return result;
    }
    
  } //LocationsLayer
  
  /**
   * The layer panel
   */
  private class LayerPanel extends LayerViewPanel implements ComponentListener, MouseListener {

    public LayerPanel(LayerManager mgr) {
      super(mgr, new ViewContext());
      addComponentListener(this);
      addMouseListener(this);
    }
    
    private List getLocations(MouseEvent event) {
      try {
        List result = new ArrayList(layerPanel.featuresWithVertex(event.getPoint(), 3,  model.getLocations()));
        Collections.sort(result);
        return result;
      } catch (Throwable t) {
        return Collections.EMPTY_LIST;
      }
    }
    
    private Coordinate getCoordinate(MouseEvent event) {
      try {
        return getViewport().toModelCoordinate(event.getPoint());
      } catch (Throwable t) {
        return null;
      }
    }
    
    public String getToolTipText(MouseEvent event) {
      
      // convert to coordinate
      Coordinate coord  = getCoordinate(event);
      if (coord==null)
        return null;
      // find features
      Collection locations = getLocations(event);
      // generate text
      StringBuffer text = new StringBuffer();
      text.append("<html><body>");
      text.append( GeoLocation.getCoordinateAsString(coord));
      // loop over locations
      for (Iterator it = locations.iterator(); it.hasNext(); )  {
        GeoLocation location = (GeoLocation)it.next();
        text.append("<br><b>");
        text.append(location);
        text.append("</b>");
      }
      // done
      return text.toString();
    }

    /** component listener callback */
    public void componentHidden(ComponentEvent e) {
    }
    /** component listener callback */
    public void componentMoved(ComponentEvent e) {
    }
    /** component listener callback */
    public void componentResized(ComponentEvent e) {
      new ZoomExtent().trigger();
    }
    /** component listener callback */
    public void componentShown(ComponentEvent e) {
    }

    /** mouse callbacks */
    public void mouseClicked(MouseEvent e) {
      Collection locations = getLocations(e);
      if (!locations.isEmpty()) {
        locationList.setSelectedLocations(locations);
        selectionLayer.setLocations(locations);
      }
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    
  }

  /**
   * Action - locate GeoLocations through service
   */
  private class ActionLocate extends Action2 {
    private ActionLocate() {
      setImage(STATUS2IMG[GeoModel.ALL_MATCHED]);
    }
    protected void execute() {
      model.resolveAll();
    }
  }

} //GeoView
