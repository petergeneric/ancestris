/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.geo;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genjfr.app.App;
import genjfr.app.GenjViewTopComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputListener;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.WebService;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.netbeans.api.javahelp.Help;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//genjfr.app.geo//GeoMap//EN",
autostore = false)
public final class GeoMapTopComponent extends GenjViewTopComponent implements GeoPlacesListener {

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "genjfr/app/geo/geo.png";
    private static final String PREFERRED_ID = "GeoMapTopComponent";
    //
    private String[] maps = new String[]{
        NbBundle.getMessage(GeoMapTopComponent.class, "maps.openstreetmap"),
        NbBundle.getMessage(GeoMapTopComponent.class, "maps.googlemap"),
        NbBundle.getMessage(GeoMapTopComponent.class, "maps.cassini")
    };
    private Gedcom gedcom = null;
    private GeoPlacesList gpl = null;
    private GeoNodeObject[] markers = null;
    private Set<GeoPoint> geoPoints = new HashSet<GeoPoint>();
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
    private Color markersColor = Color.BLUE;
    private boolean useNames = false;
    private GeoFilter geoFilter = new GeoFilter();
    //
    private boolean isBusyRecalc = false;

    public GeoMapTopComponent() {
        super();
    }

    public void init(Gedcom gedParam) {
        // Init gedcom
        initGedcom(gedParam);
        if (gedcom == null) {
            close();
        }

        // TopComponent name, tooltip and Gedcom
        setName(NbBundle.getMessage(GeoMapTopComponent.class, "CTL_GeoMapTopComponent"));
        setToolTipText(NbBundle.getMessage(GeoMapTopComponent.class, "HINT_GeoMapTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        String name;
        if ((gedcom != null) && ((name = gedcom.getName()) != null)) {
            setName(name);
            setToolTipText(getToolTipText() + ": " + name);
        }

        // TopComponent window parameters
        initComponents();
        jXMapKit1.setDataProviderCreditShown(true);
        jXMapKit1.getMainMap().setRecenterOnClickEnabled(true);
        hoverPanel = new HoverPanel(this);
        hoverPanel.setVisible(false);
        jXMapKit1.getMainMap().add(hoverPanel);

        // Set settings
        customiseFromSettings();
        setMouseListener();
        setPopuMenu();

        // Calculate and display markers
        jButton6.setEnabled(false);
        initMarkersList();
        applyFilters();
    }

    private void initGedcom(Gedcom gedParam) {
        if (gedcom == null) {
            if (gedParam == null) {
                gedcom = App.center.getSelectedGedcom(); // get selected gedcom
                if (gedcom == null) { // if none selected, take first one
                    Iterator it = GedcomDirectory.getInstance().getGedcoms().iterator();
                    if (it.hasNext()) { // well, apparently no gedcom exist in the list
                        gedcom = (Gedcom) it.next();
                    }
                }
            } else {
                gedcom = gedParam;
            }
            geoFilter.setGedcom(gedcom);
        }
        super.setGedcom(gedcom);
        super.addLookup();
    }

    private void initMarkersList() {
        // Check we get a gedcom
        if (gedcom == null) {
            // TODO resolve when tc is null, should be provided by Control Center
            JOptionPane.showMessageDialog(null, "Vous devez d'abord ouvrir un fichier gedcom pour lancer le module Géographique");
            return;
        }
        // Launch search for markers and set listener
        gpl = GeoPlacesList.getInstance(gedcom);
        if (gpl.getPlaces() == null) {
            gpl.launchPlacesSearch();
        } else {
            geoPlacesChanged(gpl, "gedcom");
        }
        gpl.addGeoPlacesListener(this);
    }

    public Gedcom getGedcom() {
        return gedcom;
    }

    public GeoNodeObject[] getMarkers() {
        return markers;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();
        jXMapKit1 = new org.jdesktop.swingx.JXMapKit();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox(maps);
        jSpinner1 = new javax.swing.JSpinner(new SpinnerNumberModel(10, 0, markersSizeMax, 1));
        jButton4 = new javax.swing.JButton() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setPaint(markersColor);
                g2.fill(new Rectangle(2,2,jButton4.getSize().width-4,jButton4.getSize().height-4));
            }
        };
        jButton5 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox11 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        jPanel6.setPreferredSize(new java.awt.Dimension(988, 300));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jPanel4.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton1, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton1.text")); // NOI18N
        jRadioButton1.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton1.toolTipText")); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton2, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton2.text")); // NOI18N
        jRadioButton2.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton2.toolTipText")); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton4, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton4.text")); // NOI18N
        jRadioButton4.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton4.toolTipText")); // NOI18N
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton7, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton7.text")); // NOI18N
        jRadioButton7.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jRadioButton7.toolTipText")); // NOI18N
        jRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton7ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jLabel1.text")); // NOI18N

        jComboBox1.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jComboBox1.toolTipText")); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jSpinner1.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jSpinner1.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jSpinner1.toolTipText")); // NOI18N
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        jButton4.setOpaque(true);
        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton4.text")); // NOI18N
        jButton4.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton4.toolTipText")); // NOI18N
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setOpaque(true);
        org.openide.awt.Mnemonics.setLocalizedText(jButton5, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton5.text")); // NOI18N
        jButton5.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton5.toolTipText")); // NOI18N
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addGap(24, 24, 24)
                        .addComponent(jRadioButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton7))
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jRadioButton1)
                        .addComponent(jRadioButton2)
                        .addComponent(jRadioButton7))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jRadioButton4)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jPanel1.border.title"))); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jTextField1.text")); // NOI18N
        jTextField1.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jTextField1.toolTipText")); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel2))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jPanel2.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox1.toolTipText")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox2.text")); // NOI18N
        jCheckBox2.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox2.toolTipText")); // NOI18N
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox3, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox3.text")); // NOI18N
        jCheckBox3.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox3.toolTipText")); // NOI18N
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox4, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox4.text")); // NOI18N
        jCheckBox4.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox4.toolTipText")); // NOI18N
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox5, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox5.text")); // NOI18N
        jCheckBox5.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox5.toolTipText")); // NOI18N
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox6, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox6.text")); // NOI18N
        jCheckBox6.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox6.toolTipText")); // NOI18N
        jCheckBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox6ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox7, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox7.text")); // NOI18N
        jCheckBox7.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox7.toolTipText")); // NOI18N
        jCheckBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox7ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton2.toolTipText")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox7))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox3))
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox5)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox3)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox6)
                    .addComponent(jCheckBox7))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jPanel3.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jLabel4.text")); // NOI18N

        jTextField2.setText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jTextField2.text")); // NOI18N
        jTextField2.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jTextField2.toolTipText")); // NOI18N
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jLabel5.text")); // NOI18N

        jTextField3.setText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jTextField3.text")); // NOI18N
        jTextField3.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jTextField3.toolTipText")); // NOI18N
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox8, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox8.text")); // NOI18N
        jCheckBox8.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox8.toolTipText")); // NOI18N
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox8ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox9, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox9.text")); // NOI18N
        jCheckBox9.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox9.toolTipText")); // NOI18N
        jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox9ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox10, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox10.text")); // NOI18N
        jCheckBox10.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox10.toolTipText")); // NOI18N
        jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox10ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox11, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox11.text")); // NOI18N
        jCheckBox11.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jCheckBox11.toolTipText")); // NOI18N
        jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(jCheckBox8)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox9)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox10)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox11))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel4)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel5)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox8)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox11))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton3.text")); // NOI18N
        jButton3.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton3.toolTipText")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton6, org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton6.text")); // NOI18N
        jButton6.setToolTipText(org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton6.toolTipText")); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, 0, 290, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3)
                            .addComponent(jButton6)
                            .addComponent(jButton1))))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
            .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, 1004, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jScrollPane1.setViewportView(jPanel6);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1006, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        GeoListTopComponent theList = null;
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof GeoListTopComponent) {
                GeoListTopComponent gltc = (GeoListTopComponent) tc;
                if (gltc.getGedcom() == gedcom) {
                    theList = gltc;
                    break;
                }
            }
        }
        if (theList == null) {
            theList = new GeoListTopComponent();
        }
        if (!theList.isInitialised()) {
            theList.init(gedcom);
            theList.open();
        }
        theList.requestActive();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        int i = jComboBox1.getSelectedIndex();
        if (i == 0) {
            jXMapKit1.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
        } else if (i == 1) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation("Les fonds de carte Google ne sont pas encore disponibles dans cette version", "Cartes à utiliser", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            jComboBox1.setSelectedIndex(0);
        } else if (i == 2) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation("Les cartes de Cassini ne sont pas encore disponible dans cette version", "Cartes à utiliser", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            jComboBox1.setSelectedIndex(0);
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        jXMapKit1.setZoomSliderVisible(jRadioButton1.isSelected());
        jXMapKit1.setZoomButtonsVisible(jRadioButton1.isSelected());
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        jXMapKit1.setMiniMapVisible(jRadioButton2.isSelected());
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        displayMarkers = jRadioButton4.isSelected();
        jSpinner1.setEnabled(displayMarkers);
        jButton4.setEnabled(displayMarkers);
        jRadioButton7.setEnabled(displayMarkers);
        displayMarkers();
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        markersSize = (int) Integer.valueOf(jSpinner1.getValue().toString());
        displayMarkers();
    }//GEN-LAST:event_jSpinner1StateChanged

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        JColorChooser cc = new JColorChooser();
        cc.setPreviewPanel(new JPanel());
        cc.setColor(markersColor);
        int ret = JOptionPane.showConfirmDialog(this, cc, NbBundle.getMessage(GeoMapTopComponent.class, "GeoMapTopComponent.jButton4.text.choose"), JOptionPane.OK_CANCEL_OPTION);
        if (ret == 0) {
            Color newColor = cc.getColor();
            if (newColor != null) {
                markersColor = newColor;
                jButton4.repaint();
            }
        }
        displayMarkers();
}//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        jXMapKit1.getMainMap().calculateZoomFrom(getPositionsFromMarkers());
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton7ActionPerformed
        useNames = jRadioButton7.isSelected();
        displayMarkers();
    }//GEN-LAST:event_jRadioButton7ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        geoFilter.location = jTextField1.getText();
        applyFilters();
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        geoFilter.ascendants = jCheckBox1.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        geoFilter.cousins = jCheckBox2.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        geoFilter.otherAncestors = jCheckBox3.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed
        geoFilter.selectedIndividual = jCheckBox4.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox4ActionPerformed

    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
        geoFilter.selectedSearch = jCheckBox5.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox5ActionPerformed

    private void jCheckBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox6ActionPerformed
        geoFilter.males = jCheckBox6.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox6ActionPerformed

    private void jCheckBox7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox7ActionPerformed
        geoFilter.females = jCheckBox7.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox7ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        geoFilter.yearStart = jTextField2.getText();
        applyFilters();
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        geoFilter.yearEnd = jTextField3.getText();
        applyFilters();
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox8ActionPerformed
        geoFilter.births = jCheckBox8.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox8ActionPerformed

    private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox9ActionPerformed
        geoFilter.marriages = jCheckBox9.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox9ActionPerformed

    private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox10ActionPerformed
        geoFilter.deaths = jCheckBox10.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox10ActionPerformed

    private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox11ActionPerformed
        geoFilter.otherEvents = jCheckBox11.isSelected();
        applyFilters();
    }//GEN-LAST:event_jCheckBox11ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        geoFilter.askRootIndi();
        applyFilters();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        getHelp();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jButton6.setEnabled(false);
        GeoPlacesList.getInstance(gedcom).launchPlacesSearch();
    }//GEN-LAST:event_jButton6ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private org.jdesktop.swingx.JXMapKit jXMapKit1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        if (gedcom != null) {
            GeoPlacesList gpl = GeoPlacesList.getInstance(gedcom);
            gpl.removeGeoPlacesListener(this);
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        if (gedcom != null) {
            p.setProperty("gedcom", gedcom.getOrigin().toString());
        }
        if (jXMapKit1 == null) {
            return;
        }
        p.setProperty("mapCenterLat", String.valueOf(jXMapKit1.getCenterPosition().getLatitude()));
        p.setProperty("mapCenterLon", String.valueOf(jXMapKit1.getCenterPosition().getLongitude()));
        p.setProperty("mapZoom", String.valueOf(jXMapKit1.getZoomSlider().getValue()));
        p.setProperty("mapToDisplay", String.valueOf(mapToDisplay));
        p.setProperty("displayZoom", Boolean.toString(displayZoom));
        p.setProperty("displayMiniMap", Boolean.toString(displayMiniMap));
        p.setProperty("displayMarkers", Boolean.toString(displayMarkers));
        p.setProperty("markersColor", String.valueOf(markersColor.getRGB()));
        p.setProperty("markersSize", String.valueOf(markersSize));
        p.setProperty("useNames", Boolean.toString(useNames));
        p.setProperty("geoFilter.location", geoFilter.location);
        p.setProperty("geoFilter.ascendants", Boolean.toString(geoFilter.ascendants));
        p.setProperty("geoFilter.cousins", Boolean.toString(geoFilter.cousins));
        p.setProperty("geoFilter.otherAncestors", Boolean.toString(geoFilter.otherAncestors));
        p.setProperty("geoFilter.selectedIndividual", Boolean.toString(geoFilter.selectedIndividual));
        p.setProperty("geoFilter.selectedSearch", Boolean.toString(geoFilter.selectedSearch));
        p.setProperty("geoFilter.males", Boolean.toString(geoFilter.males));
        p.setProperty("geoFilter.females", Boolean.toString(geoFilter.females));
        p.setProperty("geoFilter.yearStart", geoFilter.yearStart);
        p.setProperty("geoFilter.yearEnd", geoFilter.yearEnd);
        p.setProperty("geoFilter.births", Boolean.toString(geoFilter.births));
        p.setProperty("geoFilter.marriages", Boolean.toString(geoFilter.marriages));
        p.setProperty("geoFilter.deaths", Boolean.toString(geoFilter.deaths));
        p.setProperty("geoFilter.otherEvents", Boolean.toString(geoFilter.otherEvents));
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        final String gedName = p.getProperty("gedcom");
        mapCenterLat = Double.valueOf(p.getProperty("mapCenterLat", "47"));
        mapCenterLon = Double.valueOf(p.getProperty("mapCenterLon", "3"));
        mapZoom = Integer.valueOf(p.getProperty("mapZoom", "11"));
        mapToDisplay = Integer.valueOf(p.getProperty("mapToDisplay", "0"));
        displayZoom = Boolean.valueOf(p.getProperty("displayZoom", "true"));
        displayMiniMap = Boolean.valueOf(p.getProperty("displayMiniMap", "true"));
        displayMarkers = Boolean.valueOf(p.getProperty("displayMarkers", "true"));
        markersColor = new Color((int) (Integer.valueOf(p.getProperty("markersColor", "-16776961"))));
        markersSize = Integer.valueOf(p.getProperty("markersSize", "10"));
        useNames = Boolean.valueOf(p.getProperty("useNames", "false"));
        geoFilter.location = p.getProperty("geoFilter.location", "");
        geoFilter.ascendants = Boolean.valueOf(p.getProperty("geoFilter.ascendants", "true"));
        geoFilter.cousins = Boolean.valueOf(p.getProperty("geoFilter.cousins", "true"));
        geoFilter.otherAncestors = Boolean.valueOf(p.getProperty("geoFilter.otherAncestors", "true"));
        geoFilter.selectedIndividual = Boolean.valueOf(p.getProperty("geoFilter.selectedIndividual", "true"));
        geoFilter.selectedSearch = Boolean.valueOf(p.getProperty("geoFilter.selectedSearch", "true"));
        geoFilter.males = Boolean.valueOf(p.getProperty("geoFilter.males", "true"));
        geoFilter.females = Boolean.valueOf(p.getProperty("geoFilter.females", "true"));
        geoFilter.yearStart = p.getProperty("geoFilter.yearStart", "");
        geoFilter.yearEnd = p.getProperty("geoFilter.yearEnd", "");
        geoFilter.births = Boolean.valueOf(p.getProperty("geoFilter.births", "true"));
        geoFilter.marriages = Boolean.valueOf(p.getProperty("geoFilter.marriages", "true"));
        geoFilter.deaths = Boolean.valueOf(p.getProperty("geoFilter.deaths", "true"));
        geoFilter.otherEvents = Boolean.valueOf(p.getProperty("geoFilter.otherEvents", "true"));

        // start
        if (gedName == null) {
            return;
        }
        waitStartup(gedName);
    }

    //FIXME: revoir la synchro avec le CC
    void waitStartup(String name) {
        final String gedName = name;
        new Thread(new Runnable() {

            public void run() {
                while (!App.center.isReady(0));
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        init(App.center.getOpenedGedcom(gedName));
                    }
                });
            }
        }).start();

    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void getHelp() {
        String id = "genjfr.app.view.geo";
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null && help.isValidID(id, true).booleanValue()) {
            help.showHelp(new HelpCtx(id));
        } else {
            //    Toolkit.getDefaultToolkit().beep();
        }
        return;
    }

    private void customiseFromSettings() {
        jComboBox1.setSelectedIndex(mapToDisplay);
        jXMapKit1.setCenterPosition(new GeoPosition(mapCenterLat, mapCenterLon));
        jXMapKit1.setZoom(mapZoom);
        jRadioButton1.setSelected(displayZoom);
        jRadioButton2.setSelected(displayMiniMap);
        jRadioButton4.setSelected(displayMarkers);
        jRadioButton7.setSelected(useNames);
        jSpinner1.setValue(markersSize);
        jTextField1.setText(geoFilter.location);
        jCheckBox1.setSelected(geoFilter.ascendants);
        jCheckBox2.setSelected(geoFilter.cousins);
        jCheckBox3.setSelected(geoFilter.otherAncestors);
        jCheckBox4.setSelected(geoFilter.selectedIndividual);
        jCheckBox5.setSelected(geoFilter.selectedSearch);
        jCheckBox6.setSelected(geoFilter.males);
        jCheckBox7.setSelected(geoFilter.females);
        jTextField2.setText(geoFilter.yearStart);
        jTextField3.setText(geoFilter.yearEnd);
        jCheckBox8.setSelected(geoFilter.births);
        jCheckBox9.setSelected(geoFilter.marriages);
        jCheckBox10.setSelected(geoFilter.deaths);
        jCheckBox11.setSelected(geoFilter.otherEvents);
    }

    public void geoPlacesChanged(GeoPlacesList gpl, String change) {
        if (change.equals("cood")) {
        } else if (change.equals("name")) {
        } else if (change.equals("gedcom")) {
            markers = gpl.getPlaces();
            geoFilter.calculatesIndividuals(gedcom, true); // refresh lists from gedcom changes
        }
        applyFilters();
    }

    private void applyFilters() {
        isBusyRecalc = true;
        geoPoints.clear();
        if (markers != null) {
            geoFilter.calculatesIndividuals(gedcom, false); // refresh lists from selections being made in the editor or the list, not from gedcom changes
            for (int i = 0; i < markers.length; i++) {
                GeoNodeObject geoNodeObject = markers[i];
                if (geoFilter.complies(geoNodeObject)) {
                    GeoPoint wp = new GeoPoint(geoNodeObject);
                    geoPoints.add(wp);
                }
            }
            displayMarkers();
            StatusDisplayer.getDefault().setStatusText(" ", StatusDisplayer.IMPORTANCE_ANNOTATION);
            if (geoPoints.size() < markers.length) {
                String msg = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.Applied");
                msg += " - ";
                msg += org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.DeCujus") + " " + geoFilter.decujusIndi.toString();
                StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION);
            }
        }
        hoverPanel.setVisible(false);
        jButton6.setEnabled(true);
        isBusyRecalc = false;
    }

    @SuppressWarnings("unchecked")
    private void displayMarkers() {
        WaypointPainter painter = new WaypointPainter();
        if (displayMarkers) {
            painter.setWaypoints(geoPoints);
            if (useNames) {
                painter.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                        // get name
                        double coex = ((double) markersSize) / 10;
                        String name = ((GeoPoint) wp).getGeoNodeObject().getCity();
                        g.setFont(new Font("Dialog", Font.PLAIN, (int) (12 * coex)));
                        double width = (int) g.getFontMetrics().getStringBounds(name, g).getWidth();
                        //draw tab
                        GradientPaint colortowhite = new GradientPaint(0, 0, markersColor, 0, (int) (20 * coex), Color.WHITE, true);
                        g.setPaint(markersColor);
                        Polygon triangle = new Polygon();
                        triangle.addPoint(0, 0);
                        triangle.addPoint((int) (7 * coex), (int) (-11 * coex));
                        triangle.addPoint((int) (-7 * coex), (int) (-11 * coex));
                        g.fill(triangle);
                        g.setPaint(colortowhite);
                        g.fillRoundRect((int) ((-width / 2 - 5)), (int) (-30 * coex), (int) ((width + 10)), (int) (20 * coex), 10, 10);
                        //draw text w/ shadow
                        //g.setPaint(Color.GRAY);
                        //g.drawString(name, -width / 2 + 2, -16 + 2); //shadow
                        g.setPaint(markersColor);
                        g.drawString(name, (int) ((-width / 2)), (int) ((-16) * coex)); //text
                        return false;
                    }
                });
            } else {
                painter.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                        g.setStroke(new BasicStroke((int) (((double) markersSize) / 8 + 1)));
                        g.setColor(markersColor);
                        g.drawOval(-markersSize, -markersSize, 2 * markersSize, 2 * markersSize);
                        g.setStroke(new BasicStroke(1f));
                        g.drawLine(-markersSize, 0, markersSize, 0);
                        g.drawLine(0, -markersSize, 0, markersSize);
                        return false;
                    }
                });
            }
        }
        jXMapKit1.getMainMap().setOverlayPainter(painter);
        jXMapKit1.getMainMap().repaint();
    }

    Set<GeoPosition> getPositionsFromMarkers() {
        if (markers == null) {
            return null;
        }
        Set<GeoPosition> set = new HashSet<GeoPosition>();
        for (int i = 0; i < markers.length; i++) {
            GeoNodeObject geoNodeObject = markers[i];
            set.add(new GeoPosition(geoNodeObject.getLatitude(), geoNodeObject.getLongitude()));
        }
        return set;
    }

    private void setMouseListener() {
        MouseInputListener mia = new GeoMouseInputListener();
        jXMapKit1.getMainMap().addMouseListener(mia);
        jXMapKit1.getMainMap().addMouseMotionListener(mia);
    }

    public void ShowMarker(GeoNodeObject geoNodeObject) {
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

    public void CenterMarker(GeoNodeObject geoNodeObject) {
        if (geoNodeObject != null) {
            jXMapKit1.getMainMap().setCenterPosition(geoNodeObject.getGeoPosition());
            jXMapKit1.getMainMap().setZoom(8);
        }
    }

    private void setPopuMenu() {
        popupMenu = new MapPopupMenu(jXMapKit1.getMainMap());
        popupMenu.addSubmenu("ACTION_MapSearchNearby");
        popupMenu.add(new MapPopupAction("ACTION_MapCopyPoint", null, popupMenu));
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
            DecimalFormat format = new DecimalFormat("0.00");
            return ns + format.format(lat) + " " + we + format.format(lon);
        }

        private void rebuildSubmenu(final GeoPosition localGeoPoint) {
            final MapPopupMenu localPopupMenu = popupMenu;
            final JMenu localSubmenu = submenu;
            submenu.removeAll();
            new Thread(new Runnable() {

                public void run() {
                    List<Toponym> topos = getToposNearbyPoint(localGeoPoint);
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

                private List<Toponym> getToposNearbyPoint(GeoPosition localGeoPoint) {
                    if (localGeoPoint == null) {
                        return null;
                    }
                    List<Toponym> topoList = new ArrayList<Toponym>();
                    try {
                        topoList = WebService.findNearbyPlaceName(localGeoPoint.getLatitude(), localGeoPoint.getLongitude(),
                                8, 15, // radius, maxrows
                                Style.FULL, // style
                                NbPreferences.forModule(App.class).get("language", "").equals("2") ? "fr" : "en"); // language
                    } catch (Exception ex) {
                        return null;
                    }
                    return topoList;
                }
            }).start();
        }
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
                Clipboard clipboard = GeoPlaceEditor.getClipboard();
                clipboard.setContents(new GeoToken(mpm.getGeoPoint()), null);
            } else if (topo != null) {
                Clipboard clipboard = GeoPlaceEditor.getClipboard();
                clipboard.setContents(new GeoToken(topo), null);
            }
        }
    }
}

