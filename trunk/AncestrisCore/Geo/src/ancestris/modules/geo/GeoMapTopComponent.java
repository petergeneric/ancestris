/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.api.place.ShowPlace;
import ancestris.api.search.SearchCommunicator;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.libs.geonames.GeonamesOptions;
import ancestris.modules.geo.renderer.NameWaypointRenderer;
import ancestris.modules.geo.renderer.NoNameWaypointRenderer;
import ancestris.modules.utilities.search.SearchTopComponent;
import ancestris.util.Utilities;
import ancestris.util.swing.DialogManager;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.SelectionActionEvent;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.io.Filter;
import genj.view.ScreenshotAction;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputListener;
import org.geonames.Toponym;
import org.geonames.WebService;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.util.ProjectProperties;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.empty.EmptyTileFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ServiceProviders(value={
    @ServiceProvider(service = AncestrisViewInterface.class),
    @ServiceProvider(service = ShowPlace.class)
    }
)
public final class GeoMapTopComponent extends AncestrisTopComponent implements GeoPlacesListener, ShowPlace, Filter {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private genj.util.Registry registry = null;

    /**
     * Handle internet connection
     */
    private boolean isConnectionOn = true;
    private URL osmUrl;
    private boolean isBusyChecking = false;
    private long lastCheckTimeStamp = 0;

    /**
     * path to the icon used by the component and its open action
     */
    static final String ICON_PATH = "ancestris/modules/geo/geo.png";
    private static final String PREFERRED_ID = "GeoMapTopComponent";
    //
    private GeoPlacesList gpl = null;
    private GeoNodeObject[] markers = null;
    private List<GeoPoint> geoPoints = new LinkedList<>();
    private HoverPanel hoverPanel = null;
    private int markersSizeMax = 50;
    private MapPopupMenu popupMenu;
    // Settings
    private int mapToDisplay = 0;
    private double mapCenterLat = 47;
    private double mapCenterLon = 3;
    private int mapZoom = 11;
    private boolean displayZoom = true;
    private boolean displayMiniMap = true;
    private boolean displayMarkers = true;
    private int markersSize = 10;
    private boolean resizeWithZoom = true;
    private Color markersColor;
    private boolean useNames = false;
    private GeoFilter geoFilter = new GeoFilter();
    //
    private boolean isBusyRecalc = false;
    private boolean refreshFlag = false;
    //
    private Set<Entity> filteredIndis;
    //
    private SearchCommunicator searchCommunicator = null;
    //
    private Lookup.Result<SelectionActionEvent> result;
    private DialogManager settingsDialog;
    //
    private Set<Entity> connectedEntities = new HashSet<>();

    public GeoMapTopComponent() {
        super();
    }

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.OUTPUT;
    }

    @Override
    public void open() {
        // Don't try to open if no connection.
        if (isConnectionOn) {
            super.open();
        }
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage(ICON_PATH, true);
    }

    @Override
    public void setName() {
        setName(NbBundle.getMessage(GeoMapTopComponent.class, "CTL_GeoMapTopComponent"));
    }

    @Override
    public void setToolTipText() {
        setToolTipText(NbBundle.getMessage(GeoMapTopComponent.class, "HINT_GeoMapTopComponent"));
    }

    @Override
    public void init(Context context) {
        LOG.fine("GeoMap enter init : " + displayTime(System.currentTimeMillis()));
        super.init(context);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        // Listener to the search view
        searchCommunicator = new SearchCommunicator() {
            @Override
            public void changedResults(Gedcom gedcom) {
                applyFilters();
            }

            @Override
            public void closing(Gedcom gedcom) {
                geoFilter.selectedSearch = false;
                applyFilters();
            }
        };
        searchCommunicator.setGedcom(context.getGedcom());

        // Listener to selected individual
        if (result == null) {
            result = addLookupListener(context);
        }
        LOG.fine("GeoMap exit init : " + displayTime(System.currentTimeMillis()));

    }

    @Override
    public boolean createPanel() {
        LOG.fine("GeoMap enter createPanel : " + displayTime(System.currentTimeMillis()));
        try {
            osmUrl = new URL("http://tile.openstreetmap.org/");
        } catch (MalformedURLException ex) {
        }
        checkConnection(false);
        // TopComponent window parameters
        LOG.fine("GeoMap enter initComponents : " + displayTime(System.currentTimeMillis()));
        initComponents();
        LOG.fine("GeoMap exit initComponents : " + displayTime(System.currentTimeMillis()));
        loadSettings();
        geoFilter.setGedcom(getGedcom());
        hoverPanel = new HoverPanel(this);
        hoverPanel.setVisible(false);
        LOG.fine("GeoMap enter JMapKit : " + displayTime(System.currentTimeMillis()));
        if (isConnectionOn) {
            jXMapKit1.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
        } else {
            jXMapKit1.setTileFactory(new EmptyTileFactory());
        }
        jXMapKit1.getMainMap().add(hoverPanel);
        
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "zoomout");
        getActionMap().put("zoomout", jXMapKit1.getZoomOutAction());
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "zoomin");
        getActionMap().put("zoomin", jXMapKit1.getZoomInAction());

        // Add listener for zoom adapter
        jXMapKit1.getMainMap().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jXMapKit1PropertyChange(evt);
            }
        });
        LOG.fine("GeoMap exit JMapKit : " + displayTime(System.currentTimeMillis()));

        // Set settings
        customiseFromSettings();
        setMouseListener();
        setPopuMenu();

        // Calculate and display markers
        jRefreshButton.setEnabled(false);
        initMarkersList();
        applyFilters();
        jXMapKit1.setDataProviderCreditShown(true);

        LOG.fine("GeoMap exit CreatePanel : " + displayTime(System.currentTimeMillis()));
        return true;
    }

    private void initMarkersList() {
        // Check we get a gedcom
        if (getGedcom() == null) {
            // TODO resolve when tc is null, should be provided by Control Center
            JOptionPane.showMessageDialog(null, "Vous devez d'abord ouvrir un fichier gedcom pour lancer le module Géographique");
            return;
        }
        // Launch search for markers and set listener
        gpl = GeoPlacesList.getInstance(getGedcom());
        if (gpl.getNodes() == null) {
            gpl.launchPlacesSearch(GeoNodeObject.GEO_SEARCH_LOCAL_ONLY, true, false, null, null);
        } else {
            geoPlacesChanged(gpl, GeoPlacesList.TYPEOFCHANGE_GEDCOM);
        }
        gpl.addGeoPlacesListener(this);
    }

    public GeoNodeObject[] getMarkers() {
        return markers;
    }

    // FL : code taken from TreeView.java
    private Lookup.Result<SelectionActionEvent> addLookupListener(Context context) {
        Lookup.Result<SelectionActionEvent> r;
        try {
            // Install action listener
            r = GedcomDirectory.getDefault().getDataObject(context).getLookup().lookupResult(SelectionActionEvent.class);
        } catch (GedcomDirectory.ContextNotFoundException ex) {
            r = null;
        }
        final Lookup.Result<SelectionActionEvent> returnValue = r;
        if (returnValue != null) {
            returnValue.addLookupListener(new LookupListener() {

                @Override
                public void resultChanged(LookupEvent ev) {
                    for (SelectionActionEvent e : returnValue.allInstances()) {
                        if (e != null) {
                            applyFilters();
                        }
                    }
                }
            });
        }
        return returnValue;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar = new javax.swing.JToolBar();
        jGoToListButton = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jToggleSliderButton = new javax.swing.JToggleButton();
        jViewAllButton = new javax.swing.JButton();
        jToggleOverviewButton = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jToggleMarkersButton = new javax.swing.JToggleButton();
        jSwapMarkersButton = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jPanel1 = new javax.swing.JPanel();
        jPlaceFilter = new javax.swing.JTextField();
        jToggleShowUnknown = new javax.swing.JToggleButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jCaptureButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jActiveFilters = new javax.swing.JLabel();
        jRefreshButton = new javax.swing.JButton();
        jSettingsButton = new javax.swing.JButton();
        blankLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();
        jXMapKit1 = new org.jxmapviewer.JXMapKit();

        setPreferredSize(new java.awt.Dimension(906, 627));

        jToolBar.setFloatable(false);
        jToolBar.setMinimumSize(new java.awt.Dimension(11, 30));
        jToolBar.setPreferredSize(new java.awt.Dimension(100, 30));
        jToolBar.setRequestFocusEnabled(false);
        jToolBar.setVerifyInputWhenFocusTarget(false);

        jGoToListButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/GoToList.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jGoToListButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jGoToListButton.text")); // NOI18N
        jGoToListButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jGoToListButton.toolTipText")); // NOI18N
        jGoToListButton.setFocusable(false);
        jGoToListButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jGoToListButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jGoToListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGoToListButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jGoToListButton);
        jToolBar.add(jSeparator5);

        jToggleSliderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Zoom.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jToggleSliderButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleSliderButton.text")); // NOI18N
        jToggleSliderButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleSliderButton.toolTipText")); // NOI18N
        jToggleSliderButton.setFocusable(false);
        jToggleSliderButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleSliderButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleSliderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleSliderButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jToggleSliderButton);

        jViewAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Zoomadjust.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jViewAllButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jViewAllButton.text")); // NOI18N
        jViewAllButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jViewAllButton.toolTipText")); // NOI18N
        jViewAllButton.setFocusable(false);
        jViewAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jViewAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jViewAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewAllButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jViewAllButton);

        jToggleOverviewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Overview.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jToggleOverviewButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleOverviewButton.text")); // NOI18N
        jToggleOverviewButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleOverviewButton.toolTipText")); // NOI18N
        jToggleOverviewButton.setFocusable(false);
        jToggleOverviewButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleOverviewButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleOverviewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleOverviewButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jToggleOverviewButton);
        jToolBar.add(jSeparator1);

        jToggleMarkersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Pointer.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jToggleMarkersButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleMarkersButton.text")); // NOI18N
        jToggleMarkersButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleMarkersButton.toolTipText")); // NOI18N
        jToggleMarkersButton.setFocusable(false);
        jToggleMarkersButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleMarkersButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleMarkersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleMarkersButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jToggleMarkersButton);

        jSwapMarkersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/SwitchPointers.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jSwapMarkersButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jSwapMarkersButton.text")); // NOI18N
        jSwapMarkersButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jSwapMarkersButton.toolTipText")); // NOI18N
        jSwapMarkersButton.setFocusable(false);
        jSwapMarkersButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSwapMarkersButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSwapMarkersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSwapMarkersButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jSwapMarkersButton);
        jToolBar.add(jSeparator2);

        jPanel1.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N

        jPlaceFilter.setColumns(10);
        jPlaceFilter.setText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jPlaceFilter.text")); // NOI18N
        jPlaceFilter.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jPlaceFilter.toolTipText")); // NOI18N
        jPlaceFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPlaceFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jPlaceFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPlaceFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jToolBar.add(jPanel1);

        jToggleShowUnknown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/unknown.png"))); // NOI18N
        jToggleShowUnknown.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jToggleShowUnknown, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleShowUnknown.text")); // NOI18N
        jToggleShowUnknown.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jToggleShowUnknown.toolTipText")); // NOI18N
        jToggleShowUnknown.setFocusable(false);
        jToggleShowUnknown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleShowUnknown.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleShowUnknown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleShowUnknownActionPerformed(evt);
            }
        });
        jToolBar.add(jToggleShowUnknown);
        jToolBar.add(jSeparator4);

        jCaptureButton.setAction(new ScreenshotAction(jXMapKit1));
        jCaptureButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Camera.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jCaptureButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCaptureButton.text")); // NOI18N
        jCaptureButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCaptureButton.toolTipText")); // NOI18N
        jCaptureButton.setFocusable(false);
        jCaptureButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCaptureButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(jCaptureButton);
        jToolBar.add(filler1);
        jToolBar.add(jSeparator3);

        jActiveFilters.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Filter.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jActiveFilters, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jActiveFilters.text")); // NOI18N
        jActiveFilters.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jActiveFilters.toolTipText")); // NOI18N
        jToolBar.add(jActiveFilters);

        jRefreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jRefreshButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRefreshButton.text")); // NOI18N
        jRefreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRefreshButton.toolTipText")); // NOI18N
        jRefreshButton.setFocusable(false);
        jRefreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jRefreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRefreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRefreshButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jRefreshButton);

        jSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/Settings.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jSettingsButton, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jSettingsButton.text")); // NOI18N
        jSettingsButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jSettingsButton.toolTipText")); // NOI18N
        jSettingsButton.setFocusable(false);
        jSettingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSettingsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSettingsButtonActionPerformed(evt);
            }
        });
        jToolBar.add(jSettingsButton);

        org.openide.awt.Mnemonics.setLocalizedText(blankLabel, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.blankLabel.text")); // NOI18N
        jToolBar.add(blankLabel);

        jScrollPane1.setFont(new java.awt.Font("Cantarell", 0, 12)); // NOI18N
        jScrollPane1.setPreferredSize(new java.awt.Dimension(908, 302));

        jPanel6.setFont(new java.awt.Font("Cantarell", 0, 12)); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(905, 300));

        jXMapKit1.setMiniMapVisible(false);
        jXMapKit1.setZoom(8);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, 905, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel6);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 911, Short.MAX_VALUE)
                .addGap(1, 1, 1))
            .addComponent(jToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleOverviewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleOverviewButtonActionPerformed
        displayMiniMap = jToggleOverviewButton.isSelected();
        jXMapKit1.setMiniMapVisible(displayMiniMap);
        saveSettings();
    }//GEN-LAST:event_jToggleOverviewButtonActionPerformed

    private void jToggleSliderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleSliderButtonActionPerformed
        displayZoom = jToggleSliderButton.isSelected();
        jXMapKit1.setZoomSliderVisible(displayZoom);
        jXMapKit1.setZoomButtonsVisible(displayZoom);
        saveSettings();
    }//GEN-LAST:event_jToggleSliderButtonActionPerformed

    private void jViewAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jViewAllButtonActionPerformed
        checkConnection(false);
        if (isConnectionOn) {
            jXMapKit1.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
            if (jXMapKit1 != null && jXMapKit1.getMainMap() != null) {
                jXMapKit1.getMainMap().calculateZoomFrom(getPositionsFromMarkers());
            }
        }
    }//GEN-LAST:event_jViewAllButtonActionPerformed

    private void jToggleMarkersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleMarkersButtonActionPerformed
        displayMarkers = jToggleMarkersButton.isSelected();
        jSwapMarkersButton.setEnabled(displayMarkers);
        saveSettings();
        displayMarkers();
    }//GEN-LAST:event_jToggleMarkersButtonActionPerformed

    private void jSwapMarkersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSwapMarkersButtonActionPerformed
        useNames = jSwapMarkersButton.isSelected();
        saveSettings();
        displayMarkers();
    }//GEN-LAST:event_jSwapMarkersButtonActionPerformed

    private void jPlaceFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPlaceFilterActionPerformed
        geoFilter.location = jPlaceFilter.getText();
        geoFilter.save();
        applyFilters();
    }//GEN-LAST:event_jPlaceFilterActionPerformed

    private void jSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSettingsButtonActionPerformed
        showSettings();
    }//GEN-LAST:event_jSettingsButtonActionPerformed

    private void jRefreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRefreshButtonActionPerformed
        checkConnection(true);
        if (isConnectionOn) {
            jXMapKit1.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
        } else {
            String txt = NbBundle.getMessage(GeoMapTopComponent.class, "MSG_RefreshingError");
            DialogManager.createError(NbBundle.getMessage(GeoMapTopComponent.class, "CTL_GeoMapTopComponent") + " - " + NbBundle.getMessage(GeoMapTopComponent.class, "TITL_ConnectionError"), txt).show();
            return;
        }
        jRefreshButton.setEnabled(false);
        GeoPlacesList.getInstance(getGedcom()).launchPlacesSearch(GeoNodeObject.GEO_SEARCH_WEB_ONLY, true, false, null, null);
        refreshFlag = true;
    }//GEN-LAST:event_jRefreshButtonActionPerformed

    private void jGoToListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGoToListButtonActionPerformed
        GeoNodeObject gno = null;
        showListAtLocation(gno);
    }//GEN-LAST:event_jGoToListButtonActionPerformed

    private void jToggleShowUnknownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleShowUnknownActionPerformed
        geoFilter.showUnknown = jToggleShowUnknown.isSelected();
        geoFilter.save();
        applyFilters();
    }//GEN-LAST:event_jToggleShowUnknownActionPerformed

    /**
     * Detect property change to zoom and save settings In addition, include
     * Internet connection detection : with no connection, the tiles retrieval
     * from the internet crashes with many messages. FL 2018-06-16 - : I cannot
     * grab the exception from within JXMapKit, therefore I retest connection
     * every time. It is a bit time consumming but otherwise, in case of lost
     * connection, there would be many error messages (1 for each tile). To
     * avoid slow map movements, leave 5 seconds between 2 checks.
     *
     * @param evt
     */
    private void jXMapKit1PropertyChange(java.beans.PropertyChangeEvent evt) {

        // Get property
        String pn = evt.getPropertyName();

        // No need to check in others cases.
        if ("zoom".equals(pn) || "center".equals(pn)) {

            // Detect internet connection status and dynamic
            boolean isBeforeOn = isConnectionOn;
            if (System.currentTimeMillis() - lastCheckTimeStamp > 5000) {
                checkConnection(true);
            }
            if (isConnectionOn) {
                if (!isBeforeOn) {
                    jXMapKit1.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps); // Connection is back, set tiles back on
                }
                if ("zoom".equals(pn) && resizeWithZoom) {
                    resizeWithZoom();
                }
            } else if (isBeforeOn) {
                jXMapKit1.setTileFactory(new EmptyTileFactory());  // Connection has been lost, prevent tile retrieval from the Internet
            }
            saveSettings();
        }

        jXMapKit1.setDataProviderCreditShown(true);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel blankLabel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jActiveFilters;
    private javax.swing.JButton jCaptureButton;
    private javax.swing.JButton jGoToListButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField jPlaceFilter;
    private javax.swing.JButton jRefreshButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JButton jSettingsButton;
    private javax.swing.JToggleButton jSwapMarkersButton;
    private javax.swing.JToggleButton jToggleMarkersButton;
    private javax.swing.JToggleButton jToggleOverviewButton;
    private javax.swing.JToggleButton jToggleShowUnknown;
    private javax.swing.JToggleButton jToggleSliderButton;
    private javax.swing.JToolBar jToolBar;
    private javax.swing.JButton jViewAllButton;
    private org.jxmapviewer.JXMapKit jXMapKit1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        Gedcom gedcom = getGedcom();
        if (gedcom != null) {
            GeoPlacesList gpl2 = GeoPlacesList.getInstance(getGedcom());
            gpl2.remove(gedcom);
            gpl2.removeGeoPlacesListener(this);
            geoFilter.save();
        }
        SearchCommunicator.unregister(searchCommunicator);
        AncestrisPlugin.unregister(this);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void loadSettings() {
        if (registry == null) {
            registry = getGedcom().getRegistry();
        }
        markersSize = registry.get("GEO.markers.size", 10);
        markersColor = registry.get("GEO.markers.color", Color.BLUE);
        resizeWithZoom = registry.get("GEO.markers.resizeWithZoom", true);
        mapCenterLat = Double.valueOf(registry.get("mapCenterLat", "47"));
        mapCenterLon = Double.valueOf(registry.get("mapCenterLon", "3"));
        mapZoom = Integer.valueOf(registry.get("mapZoom", "11"));
        mapToDisplay = Integer.valueOf(registry.get("mapToDisplay", "0"));
        displayZoom = Boolean.valueOf(registry.get("displayZoom", "true"));
        displayMiniMap = Boolean.valueOf(registry.get("displayMiniMap", "true"));
        displayMarkers = Boolean.valueOf(registry.get("displayMarkers", "true"));
        useNames = Boolean.valueOf(registry.get("useNames", "false"));
    }

    public void saveSettings() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                if (jXMapKit1 == null || registry == null) {
                    return;
                }
                registry.put("mapCenterLat", String.valueOf(jXMapKit1.getCenterPosition().getLatitude()));
                registry.put("mapCenterLon", String.valueOf(jXMapKit1.getCenterPosition().getLongitude()));
                registry.put("mapZoom", String.valueOf(jXMapKit1.getZoomSlider().getValue()));
                registry.put("mapToDisplay", String.valueOf(mapToDisplay));
                registry.put("displayZoom", Boolean.toString(displayZoom));
                registry.put("displayMiniMap", Boolean.toString(displayMiniMap));
                registry.put("displayMarkers", Boolean.toString(displayMarkers));
                registry.put("useNames", Boolean.toString(useNames));
            }
        });
    }

    public void getHelp() {
        String id = NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.HelpPage");
        try {
            Desktop.getDesktop().browse(new URI(id));
        } catch (URISyntaxException | IOException ex) {
            LOG.log(Level.FINE, "Unable to open File", ex);
        }
    }

    private void customiseFromSettings() {
        jXMapKit1.setCenterPosition(new GeoPosition(mapCenterLat, mapCenterLon));
        jXMapKit1.setZoom(mapZoom);

        jToggleSliderButton.setSelected(displayZoom);
        jXMapKit1.setZoomSliderVisible(displayZoom);
        jXMapKit1.setZoomButtonsVisible(displayZoom);

        jToggleOverviewButton.setSelected(displayMiniMap);
        jXMapKit1.setMiniMapVisible(displayMiniMap);

        jToggleMarkersButton.setSelected(displayMarkers);
        jSwapMarkersButton.setEnabled(displayMarkers);

        jSwapMarkersButton.setSelected(useNames);

        jPlaceFilter.setText(geoFilter.location);
        jToggleShowUnknown.setSelected(geoFilter.showUnknown);
    }

    @Override
    public void geoPlacesChanged(GeoPlacesList gpl, String change) {
        if (change.equals(GeoPlacesList.TYPEOFCHANGE_COORDINATES) || (change.equals(GeoPlacesList.TYPEOFCHANGE_NAME)) || (change.equals(GeoPlacesList.TYPEOFCHANGE_GEDCOM))) {
            hoverPanel.setVisible(false);
            markers = gpl.getNodes();
            applyFilters();
        }
        if (change.equals(GeoPlacesList.TYPEOFCHANGE_GEDCOM) && refreshFlag) {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                            NbBundle.getMessage(getClass(), "GeoMapTopComponent.jRefreshButton.resultText"),
                            NbBundle.getMessage(getClass(), "GeoMapTopComponent.jRefreshButton.toolTipText"),
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshFlag = false;
                }
            });
        }
    }

    private void applyFilters() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                applyFiltersNow();
            }
        });
    }

    private void applyFiltersNow() {
        LOG.fine("GeoMap enter geopointList : " + displayTime(System.currentTimeMillis()));
        if (isBusyRecalc) {
            return;
        }
        isBusyRecalc = true;
        String msg = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.inprogress");
        jActiveFilters.setToolTipText(msg);

        geoPoints.clear();
        boolean filterIsOn = false;
        if (markers != null) {
            if (geoFilter.selectedSearch && findSearchWindow() == null) {
                geoFilter.selectedSearch = false;
            }
            geoFilter.calculatesIndividuals(getGedcom());
            for (int i = 0; i < markers.length; i++) {
                GeoNodeObject geoNodeObject = markers[i];
                // Reject if node is an event (we are filtering locations here, not events)
                if (geoNodeObject.isEvent) {
                    continue;
                }
                if (geoFilter.compliesNode(geoNodeObject)) {
                    GeoPoint wp = new GeoPoint(geoNodeObject);
                    geoPoints.add(wp);
                } else {
                    filterIsOn = true;
                }
            }
            displayMarkers();
            jActiveFilters.setVisible(filterIsOn);
            jActiveFilters.setToolTipText(msg);

            if (filterIsOn) {
                msg = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.Applied");
                msg += " - ";
                msg += geoFilter.getShortDescription();
                jActiveFilters.setToolTipText(msg);

            }
        }
        hoverPanel.setVisible(false);
        jRefreshButton.setEnabled(true);
        isBusyRecalc = false;
        LOG.fine("GeoMap exit GeopointList : " + displayTime(System.currentTimeMillis()));
    }

    private void displayMarkers() {
        LOG.fine("GeoMap enter displayList : " + displayTime(System.currentTimeMillis()));
        WaypointPainter painter = new WaypointPainter();
        if (displayMarkers) {
            painter.setWaypoints(new HashSet(geoPoints));
            if (useNames) {
                painter.setRenderer(new NameWaypointRenderer(markersSize, markersColor));
            } else {
                // painter.setRenderer(new DefaultWaypointRenderer());
                painter.setRenderer(new NoNameWaypointRenderer(markersSize, markersColor));
            }
        }
        jXMapKit1.setAddressLocationPainter(painter);
        jXMapKit1.repaint();
        LOG.fine("GeoMap exit DisplayList : " + displayTime(System.currentTimeMillis()));
    }

    Set<GeoPosition> getPositionsFromMarkers() {
        if (markers == null) {
            return null;
        }
        Set<GeoPosition> set = new HashSet<>();
        for (int i = 0; i < markers.length; i++) {
            GeoNodeObject geoNodeObject = markers[i];
            set.add(geoNodeObject.getGeoPosition());
        }
        return set;
    }

    private void setMouseListener() {
        MouseInputListener mia = new GeoMouseInputListener();
        jXMapKit1.getMainMap().addMouseListener(mia);
        jXMapKit1.getMainMap().addMouseMotionListener(mia);
    }

    public void showMarker(GeoNodeObject geoNodeObject) {
        if (geoNodeObject != null && !geoNodeObject.isEvent) {
            hoverPanel.setPanel(geoNodeObject, markersColor);
            JXMapViewer map = jXMapKit1.getMainMap();
            GeoPosition gpm = geoNodeObject.getGeoPosition();
            Point2D gp_pt = map.getTileFactory().geoToPixel(gpm, map.getZoom());
            Rectangle rect = map.getViewportBounds();
            Point converted_gp_pt = new Point((int) gp_pt.getX() - rect.x, (int) gp_pt.getY() - rect.y);
            map.setLayout(null);
            hoverPanel.setSize(hoverPanel.getPreferredSize());
            hoverPanel.setLocation(converted_gp_pt.x + markersSize + 5, converted_gp_pt.y - 35);
            hoverPanel.setVisible(true);
        } else {
            hoverPanel.setVisible(false);
        }
    }

    public void centerMarker(GeoNodeObject geoNodeObject) {
        if (geoNodeObject != null) {
            jXMapKit1.getMainMap().setCenterPosition(geoNodeObject.getGeoPosition());
        }
    }

    public void setZoom(int zoom) {
        jXMapKit1.getMainMap().setZoom(zoom);
    }

    private void setPopuMenu() {
        popupMenu = new MapPopupMenu(jXMapKit1.getMainMap());
        popupMenu.addSubmenu("ACTION_MapSearchNearby");
        popupMenu.add(new MapPopupAction("ACTION_MapCopyPoint", null, popupMenu));
    }

    private void showSettings() {
        SettingsPanel settingsPanel = new SettingsPanel(this);
        settingsDialog = DialogManager.create(
                NbBundle.getMessage(getClass(), "TITL_Setting"), settingsPanel)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .setDialogId(SettingsPanel.class);
        settingsDialog.show();
        settingsPanel.saveDates();
        geoFilter.save();
    }

    public Color getMarkersColor() {
        return markersColor;
    }

    public void setMarkersColor(Color c) {
        markersColor = c;
        if (c != null) {
            registry.put("GEO.markers.color", c);
            displayMarkers();
        }
    }

    public int getMarkersSize() {
        return markersSize;
    }

    public void setMarkersSize(int s) {
        markersSize = s;
        if (s > 0 && s <= markersSizeMax) {
            registry.put("GEO.markers.size", markersSize);
            displayMarkers();
        }
    }

    public boolean getResizeWithZoom() {
        return resizeWithZoom;
    }

    public void setResizeWithZoom(boolean selected) {
        resizeWithZoom = selected;
        registry.put("GEO.markers.resizeWithZoom", resizeWithZoom);
        if (resizeWithZoom) {
            resizeWithZoom();
        }
    }

    public void resizeWithZoom() {
        int z = jXMapKit1.getMainMap().getZoom();
        int s = (int) (0.047*z*z - 3.46*z + 50);     // assume quadratic regression between zoom and marker size going through point (zoom = 10 and size = 20)
        setMarkersSize(s);
    }

    public GeoFilter getFilter() {
        return geoFilter;
    }

    public void setShownUnknown(boolean selected) {
        geoFilter.showUnknown = selected;
        applyFilters();
    }

    public void setFilterAscendants(boolean selected) {
        geoFilter.ascendants = selected;
        applyFilters();
    }

    void setFilterDescendants(boolean selected) {
        geoFilter.descendants = selected;
        applyFilters();
    }

    public void setFilterCousins(boolean selected) {
        geoFilter.cousins = selected;
        applyFilters();
    }

    public void setFilterAncestors(boolean selected) {
        geoFilter.otherAncestors = selected;
        applyFilters();
    }

    public void setFilterSelectedIndi(boolean selected) {
        geoFilter.selectedIndividual = selected;
        applyFilters();
    }

    private SearchTopComponent findSearchWindow() {
        SearchTopComponent searchWindow = null;
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof SearchTopComponent) {
                SearchTopComponent gltc = (SearchTopComponent) tc;
                if (gltc.getGedcom() == getGedcom()) {
                    searchWindow = gltc;
                    break;
                }
            }
        }
        return searchWindow;
    }

    public void setFilterSelectedSearch(boolean selected) {
        if (selected) {
            SearchTopComponent searchWindow = findSearchWindow();
            if (searchWindow == null) {
                searchWindow = new SearchTopComponent();
            }
            if (!searchWindow.isOpen) {
                searchWindow.init(getContext());
                searchWindow.open();
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        NbBundle.getMessage(getClass(), "GeoMapTopComponent.jSelectionWindow.Message"),
                        NbBundle.getMessage(getClass(), "GeoMapTopComponent.jSelectionWindow.Title"),
                        JOptionPane.INFORMATION_MESSAGE);
                searchWindow.requestActive();
                settingsDialog.cancel();  // close geo settings
            }
        }
        geoFilter.selectedSearch = selected;
        applyFilters();
    }

    public void setFilterMales(boolean selected) {
        geoFilter.males = selected;
        applyFilters();
    }

    public String setFilterRootIndi() {
        genj.gedcom.Indi indi = geoFilter.askRootIndi();
        if (indi != null) {
            geoFilter.rootIndi = indi;
        }
        applyFilters();
        return indi.toString(true);
    }

    public String setFilterDeCujusIndi() {
        genj.gedcom.Indi indi = geoFilter.getDeCujusIndi();
        if (indi == null) {
            indi = geoFilter.getRootIndi();
        }
        if (indi != null) {
            geoFilter.rootIndi = indi;
        }
        applyFilters();
        return indi != null ? indi.toString(true) : "";
    }

    public String setFilterSelectedIndi() {
        genj.gedcom.Indi indi = geoFilter.getSelectedIndi();
        if (indi != null) {
            geoFilter.rootIndi = indi;
        }
        applyFilters();
        return indi.toString(true);
    }

    public void setFilterFemales(boolean selected) {
        geoFilter.females = selected;
        applyFilters();
    }

    public void setFilterYearStart(String text) {
        geoFilter.yearStart = text;
        applyFilters();
    }

    public void setFilterYearEnd(String text) {
        geoFilter.yearEnd = text;
        applyFilters();
    }

    public void setFilterBirths(boolean selected) {
        geoFilter.births = selected;
        applyFilters();
    }

    public void setFilterMarriages(boolean selected) {
        geoFilter.marriages = selected;
        applyFilters();
    }

    public void setFilterDeaths(boolean selected) {
        geoFilter.deaths = selected;
        applyFilters();
    }

    public void setFilterEvents(boolean selected) {
        geoFilter.otherEvents = selected;
        applyFilters();
    }

    public boolean getShowUnknown() {
        return geoFilter.showUnknown;
    }

    public boolean getFilterAscendants() {
        return geoFilter.ascendants;
    }

    public boolean getFilterDescendants() {
        return geoFilter.descendants;
    }

    public boolean getFilterCousins() {
        return geoFilter.cousins;
    }

    public boolean getFilterAncestors() {
        return geoFilter.otherAncestors;
    }

    public boolean getFilterSelectedIndi() {
        return geoFilter.selectedIndividual;
    }

    public boolean getFilterSearch() {
        return geoFilter.selectedSearch;
    }

    public boolean getFilterMales() {
        return geoFilter.males;
    }

    public boolean getFilterFemales() {
        return geoFilter.females;
    }

    public String getFilterYearStart() {
        return geoFilter.yearStart;
    }

    public String getFilterYearEnd() {
        return geoFilter.yearEnd;
    }

    public boolean getFilterBirths() {
        return geoFilter.births;
    }

    public boolean getFilterMarriages() {
        return geoFilter.marriages;
    }

    public boolean getFilterDeaths() {
        return geoFilter.deaths;
    }

    public boolean getFilterEvents() {
        return geoFilter.otherEvents;
    }

    public String getFilerRootIndi() {
        return geoFilter.getRootIndi().toString(true);
    }

    public String getSelectedIndividual() {
        return geoFilter.getSelectedIndi().toString(true);
    }

    public void showListAtLocation(final GeoNodeObject gno) {
        int delay = 0;
        GeoListTopComponent theList = null;
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof GeoListTopComponent) {
                GeoListTopComponent gltc = (GeoListTopComponent) tc;
                if (gltc.getGedcom() == getGedcom()) {
                    theList = gltc;
                    break;
                }
            }
        }
        if (theList == null) {
            theList = new GeoListTopComponent();
            delay = 500;
        }
        if (!theList.isInitialised()) {
            theList.init(getContext());
            theList.open();
        }
        theList.requestActive();

        final GeoListTopComponent tmpList = theList;
        if (delay != 0) {
            javax.swing.Timer timer = new javax.swing.Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tmpList.showLocation(gno);
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            tmpList.showLocation(gno);
        }

    }

    public String getFilterName() {
        filteredIndis = getIndisFromGeoPoints();
        return NbBundle.getMessage(GeoMapTopComponent.class, "TTL_Filter", filteredIndis.size(), NbBundle.getMessage(GeoMapTopComponent.class, "CTL_GeoMapTopComponent"));
    }

    /**
     * Include all entities which depend on at least one Indi which is in the
     * tree (uses utility)
     *
     * @param entity
     * @return
     */
    public boolean veto(Entity entity) {
        if (filteredIndis == null) {
            filteredIndis = getIndisFromGeoPoints();
        }

        // Check if belongs to connected entities
        if (connectedEntities.isEmpty()) {
            for (Entity hit : filteredIndis) {
                connectedEntities.addAll(Utilities.getDependingEntitiesRecursively(hit));
            }
        }
        if (connectedEntities.contains(entity)) {
            return false;
        }

        return true;
    }

    /**
     * Exclude properties that reference individuals which are not part of the
     * tree
     *
     * @param property
     * @return
     */
    @Override
    public boolean veto(Property property) {
        if (property instanceof PropertyXRef) {
            PropertyXRef xref = (PropertyXRef) property;
            if (xref.isValid() && !connectedEntities.contains(xref.getTargetEntity())) {
                return true;
            }
        }
        return false;
    }

    public boolean canApplyTo(Gedcom gedcom) {
        return (gedcom != null && gedcom.equals(getGedcom()));
    }

    private Set<Entity> getIndisFromGeoPoints() {
        Set<Entity> ret = new HashSet<Entity>();
        if (geoPoints == null || geoPoints.isEmpty()) {
            return ret;
        }
        for (GeoPoint gno : geoPoints) {
            for (GeoNodeObject event : gno.getGeoNodeObject().getFilteredEvents(geoFilter)) {
                if (geoFilter.compliesEvent(event)) {
                    ret.add(event.getProperty().getEntity());
                }
            }
        }
        return ret;
    }

    // Check access to map tiles
    private void checkConnection(boolean mute) {
        LOG.fine("GeoMap enter checkConnection : " + displayTime(System.currentTimeMillis()));
        try {
            if (isBusyChecking) {
                return;
            }
            isBusyChecking = true;
            lastCheckTimeStamp = System.currentTimeMillis();
            LOG.fine("GeoMap check server : " + displayTime(System.currentTimeMillis()));
            URLConnection uc = osmUrl.openConnection();
            uc.setRequestProperty("User-Agent", ProjectProperties.INSTANCE.getName() + '/' + ProjectProperties.INSTANCE.getVersion());
            uc.getInputStream();
            LOG.fine("GeoMap end check server : " + displayTime(System.currentTimeMillis()));
        } catch (IOException ex) {
            if (!mute) {
                DialogManager.createError(
                        NbBundle.getMessage(GeoMapTopComponent.class, "CTL_GeoMapTopComponent") + " - "
                        + NbBundle.getMessage(GeoMapTopComponent.class, "TITL_ConnectionError"),
                        NbBundle.getMessage(GeoMapTopComponent.class, "MSG_ConnectionError"))
                        .show();
            }
            isConnectionOn = false;
            isBusyChecking = false;
            return;
        }
        isConnectionOn = true;
        isBusyChecking = false;
        LOG.fine("GeoMap exit checkConnexion : " + displayTime(System.currentTimeMillis()));
        return;
    }

    private static String displayTime(long time) {
        final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        final Date d = new Date(time);
        return df.format(d);
    }

    @Override
    public void showPlace(GeoPosition gpm) {
        if (gpm != null) {
            jXMapKit1.getMainMap().setCenterPosition(gpm);
            setZoom(5);
            this.requestActive();
        } else {
            DialogManager.create(NbBundle.getMessage(getClass(), "ACTION_ShowPlace").replaceAll("&", ""), NbBundle.getMessage(getClass(), "MSG_DisplayError"))
                .setMessageType(DialogManager.WARNING_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .setDialogId(SettingsPanel.class).show();
        }
    }

    private class GeoMouseInputListener implements MouseInputListener {

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                popupMenu.setPoint(e.getPoint());
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
            if (geoPoints == null || isBusyRecalc) {
                return;
            }
            // Warning : if map is moved too much to the right or to the left, points needs to be added +/-2kPi
            // FIXME : find another way to get mouse on map coordinates because map display marker correctly
            // It should be possible to get it right
            JXMapViewer map = jXMapKit1.getMainMap();
            GeoNodeObject geoNodeObject = null;
            Point converted_gp_pt = null;
            for (Iterator<GeoPoint> it = geoPoints.iterator(); it.hasNext();) {
                GeoPoint wp = it.next();
                GeoPosition gpm = wp.getGeoNodeObject().getGeoPosition();
                Point2D gp_pt = map.getTileFactory().geoToPixel(gpm, map.getZoom());
                Rectangle rect = map.getViewportBounds();
                converted_gp_pt = new Point((int) gp_pt.getX() - rect.x, (int) gp_pt.getY() - rect.y);
                if (converted_gp_pt.distance(e.getPoint()) < (10 + markersSize)) {
                    geoNodeObject = wp.getGeoNodeObject();
                    break;
                }
            }

            if (geoNodeObject != null) {
                hoverPanel.setPanel(geoNodeObject, markersColor);
                map.setLayout(null);
                hoverPanel.setSize(hoverPanel.getPreferredSize());
                hoverPanel.setLocation(converted_gp_pt.x + markersSize + 5, converted_gp_pt.y - 35);
                hoverPanel.setVisible(true);
            } else {
                hoverPanel.setVisible(false);
            }
        }
    }

    private class MapPopupMenu extends JPopupMenu {

        private JMenu submenu = null;
        private JXMapViewer map = null;
        private Point point = new Point(0, 0);

        public MapPopupMenu(JXMapViewer map) {
            super();
            this.map = map;
        }

        private void addSubmenu(String name) {
            submenu = new JMenu(NbBundle.getMessage(GeoMapTopComponent.class, name));
            popupMenu.add(submenu);
        }

        public void setPoint(Point point) {
            this.point = point;
            rebuildSubmenu(getGeoPoint());
            remove(1);
            add(new MapPopupAction("ACTION_MapCopyPoint", getCoordinates(), this));
        }

        public GeoPosition getGeoPoint() {
            return map.convertPointToGeoPosition(point);
        }

        public String getCoordinates() {
            return getCoordinates(getGeoPoint());
        }

        public String getCoordinates(GeoPosition geoPoint) {
            Double lat = geoPoint != null ? geoPoint.getLatitude() : 0;
            Double lon = geoPoint != null ? geoPoint.getLongitude() : 0;
            char we = 'E', ns = 'N';
            if (lat < 0) {
                lat = -lat;
                ns = 'S';
            }
            if (lon < 0) {
                lon = -lon;
                we = 'W';
            }
            DecimalFormat format = new DecimalFormat("0.00000");
            return ns + format.format(lat) + " " + we + format.format(lon);
        }

        private void rebuildSubmenu(final GeoPosition localGeoPoint) {
            final MapPopupMenu localPopupMenu = popupMenu;
            final JMenu localSubmenu = submenu;
            submenu.removeAll();
            new Thread(new Runnable() {

                public void run() {
                    List<Toponym> topos = getToposNearbyPoint(localGeoPoint);
                    if (topos != null) {
                        SortedMap<String, Toponym> uniqueMap = new TreeMap<String, Toponym>(); // use sortedmap to sort locations
                        for (Iterator<Toponym> it = topos.iterator(); it.hasNext();) {
                            Toponym toponym = it.next();
                            String name = toponym.getName() + " [" + getCoordinates(new GeoPosition(toponym.getLatitude(), toponym.getLongitude())) + "]";
                            uniqueMap.put(name, toponym);
                        }
                        for (Iterator<String> it = uniqueMap.keySet().iterator(); it.hasNext();) {
                            String name = it.next();
                            localSubmenu.add(new MapPopupAction(name, localPopupMenu.getCoordinates(), localPopupMenu, uniqueMap.get(name)));
                        }
                    }
                }

                private List<Toponym> getToposNearbyPoint(GeoPosition localGeoPoint) {
                    if (localGeoPoint == null) {
                        return null;
                    }
                    List<Toponym> topoList = new ArrayList<>();
                    try {
                        WebService.setUserName(GeonamesOptions.getInstance().getUserName());
                        topoList = WebService.findNearbyPlaceName(localGeoPoint.getLatitude(), localGeoPoint.getLongitude(),
                                8, 15); // radius, maxrows
                    } catch (Exception ex) {
                        return null;
                    }
                    return topoList;
                }
            }).start();
        }
    }

    static public Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(ExClipboard.class);
        if (c == null) {
            c = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return c;
    }

    private class MapPopupAction extends AbstractAction {

        private String actionName = "";
        private MapPopupMenu mpm = null;
        private Toponym topo = null;

        public MapPopupAction(String name, Object o, MapPopupMenu mpm) {
            this.actionName = name;
            this.mpm = mpm;
            putValue(NAME, NbBundle.getMessage(GeoMapTopComponent.class, name, o));
        }

        public MapPopupAction(String name, Object o, MapPopupMenu mpm, Toponym topo) {
            this.actionName = "location";
            this.mpm = mpm;
            this.topo = topo;
            putValue(NAME, name);
        }

        @SuppressWarnings("deprecation")
        public void actionPerformed(ActionEvent e) {
            if (actionName.equals("ACTION_MapCopyPoint")) {
                Clipboard clipboard = getClipboard();
                clipboard.setContents(new GeoToken(mpm.getGeoPoint()), null);
            } else if (topo != null) {
                Clipboard clipboard = getClipboard();
                clipboard.setContents(new GeoToken(topo), null);
            }
        }
    }
}
