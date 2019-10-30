/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkGEXF;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;
import org.openide.windows.WindowManager;
import spin.Spin;

/**
 * Graph Top Component.
 *
 * Author : Zurga
 */
@ServiceProvider(service = AncestrisViewInterface.class)
@RetainLocation(AncestrisDockModes.OUTPUT)
public final class GraphTopComponent extends AncestrisTopComponent {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    static final String ICON_PATH = "ancestris/modules/views/graph/resources/graphe.png";
    private static final String PREFERRED_ID = "GraphTopComponent";
    private static final String UI_STYLE = "ui.style";
    private static final String UI_LABEL = "label";
    private static final String CHILD = "child";
    private static final String MARIAGE = "mariage";
    private static final String CLASSE_ORIGINE = "classe.origine";
    private static final String UI_CLASS = "ui.class";
    private static final String STICKED = "sticked";
    private static final String SOSA = "sosa";
    private static final String MARRIAGE_SOSA = "mariagesosa";
    private static final String LAYOUTWEIGHT = "layout.weight";
    private static final String UISTYLESHEET = "ui.stylesheet";
    private static final String FAM = "famille";

    private static final String CSS = "node.sosa {"
            + "    fill-color:%s;"
            + "    size: %s;"
            + "}"
            + "node.mariage {"
            + "    fill-color:%s;"
            + "}"
            + "node.mariagesosa {"
            + "    fill-color:%s;"
            + "    size: %s;"
            + "}"
            + "edge.sosa {"
            + "    fill-color:%s;"
            + "    size: %s;"
            + "}"
            + "edge.mariage {"
            + "    fill-color:%s;"
            + "}"
            + "edge.child {"
            + "    fill-color:%s; "
            + "    shape:cubic-curve;"
            + "}"
            + "node.cujus {"
            + "    fill-color:%s;"
            + "	   size: %s;"
            + "}"
            + "edge {"
            + "    size: %s;"
            + "    fill-color:%s;"
            + "}"
            + "node {"
            + "	   size: %s;"
            + "    fill-color:%s;"
            + "}"
            + "node.sticked {"
            + "	fill-color:%s;"
            + "}"
            + "edge.sticked {"
            + "	fill-color:%s;"
            + "}"
            + "graph {"
            + "	fill-color:%s;"
            + "}";

    private static GraphTopComponent factory;

    private genj.util.Registry registry = null;

    private final Graph leGraphe = new AncestrisMultiGraph("Arbre");
    private final Viewer leViewer = new Viewer(leGraphe, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
    private final ViewPanel laVue = leViewer.addDefaultView(false);

    private final GrapheGedcomListenerAdapter listener;

    // Display Labels ?
    private boolean showLabel = false;
    // Auto Layoyt ?
    private boolean autoDisplay = true;
    // Center on click ?
    private boolean centerGraph = false;
    // Should recenter when select ?
    private boolean recenter = true;

    // Colors and sizes
    private String colorDef = "#000000";
    private String colorSosa = "#006400";
    private String colorMariage = "#FF4500";
    private String colorChild = "#708090";
    private String colorCujus = "#FF00FF";
    private String colorSticked = "#0000FF";
    private String colorBack = "#FFFFFF";
    private String colorMariageSosa = "#FFCC33";
    private String sizeEdge = "2";
    private String sizeNode = "8";
    private String sizeCujus = "20";
    private String sizeNodeSosa = "8";
    private String sizeEdgeSosa = "2";
    private double indiNodeWeight = 10.0;
    private double mariageNodeWeight = 5.0;
    private double edgeWeight = 1.0;

    public GraphTopComponent() {
        super();
        listener = new GrapheGedcomListenerAdapter(this);
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized GraphTopComponent getFactory() {
        if (factory == null) {
            factory = new GraphTopComponent();
        }
        return factory;
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage(ICON_PATH, true);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void setName() {
        setName(NbBundle.getMessage(GraphTopComponent.class, "CTL_GraphTopComponent"));
    }

    @Override
    public void setToolTipText() {
        setToolTipText(NbBundle.getMessage(GraphTopComponent.class, "HINT_GraphTopComponent"));
    }

    @Override
    public void init(Context context) {
        super.init(context);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

    }

    @Override
    public boolean createPanel() {
        // TopComponent window parameters
        initComponents();
        loadSettings();

        zoomSlider.setValue(100);

        updateCss();
        leGraphe.setAttribute("ui.antialias");
        leViewer.enableAutoLayout();

        graphPanel.add(laVue, BorderLayout.CENTER);
        laVue.setMouseManager(new AncestrisMouseManager());
        laVue.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                graphPanelMouseClicked(evt);
            }
        });
        laVue.getCamera().setAutoFitView(true);

        fillGraph();

        return true;
    }

    private void fillGraph() {
        final Gedcom gedcom = getContext().getGedcom();
        for (Indi indi : gedcom.getIndis()) {
            addIndiNode(indi);
        }
        for (Fam fam : gedcom.getFamilies()) {
            addFamNode(fam);
        }

    }

    private void addFamNode(Fam fam) {
        Node noeudCourant = leGraphe.getNode(fam.getId());
        double x = 0;
        double y = 0;
        double z = 0;

        // REmove node before recreate it.
        if (noeudCourant != null) {
            final GraphicNode graphicNode = leViewer.getGraphicGraph().getNode(noeudCourant.getId());
            x = graphicNode.getX();
            y = graphicNode.getY();
            z = graphicNode.getZ();
            leGraphe.removeNode(noeudCourant);
        }

        boolean famSosa = false;
        noeudCourant = leGraphe.addNode(fam.getId());
        if (x != 0 || y != 0 || z != 0) {
            noeudCourant.setAttribute("xyz", x, y, z);
        }
        noeudCourant.addAttribute(LAYOUTWEIGHT, mariageNodeWeight);
        noeudCourant.addAttribute(UI_CLASS, MARIAGE);
        noeudCourant.addAttribute(CLASSE_ORIGINE, MARIAGE);
        noeudCourant.addAttribute(UI_STYLE, getDisplayLabelMode());
        noeudCourant.addAttribute(FAM);

        if (fam.getMarriageDate() != null) {
            noeudCourant.addAttribute(UI_LABEL, fam.getMarriageDate().getDisplayValue());
        }
        Indi husband = fam.getSpouses().size() > 0 ? fam.getSpouse(0) : null;
        Indi wife = fam.getSpouses().size() > 1 ? fam.getSpouse(1) : null;
        boolean husbandSosa = calcSosa(husband);
        boolean wifeSosa = calcSosa(wife);
        if ((husbandSosa && wifeSosa) || (husbandSosa && wife == null) || (wifeSosa && husband == null)) {
            noeudCourant.addAttribute(UI_CLASS, MARRIAGE_SOSA);
            noeudCourant.addAttribute(CLASSE_ORIGINE, MARRIAGE_SOSA);
            famSosa = true;
        }

        // Remove all Edge and recreate them.
        for (Edge e : noeudCourant.getEachEdge()) {
            leGraphe.removeEdge(e);
        }

        if (husband != null) {
            createSpouseEdge(fam, husband, husbandSosa, famSosa);
        }
        if (wife != null) {
            createSpouseEdge(fam, wife, wifeSosa, famSosa);
        }

        for (Indi child : fam.getChildren()) {
            boolean childSosa = calcSosa(child);
            createChildEdge(fam, child, childSosa);
        }
    }

    private void createChildEdge(Fam fam, Indi child, boolean childSosa) {
        createEdge(fam, child, true, childSosa, true);
    }

    private void createSpouseEdge(Fam fam, Indi spouse, boolean spouseSosa, boolean famSosa) {
        createEdge(fam, spouse, famSosa, spouseSosa, false);
    }

    private void createEdge(Fam fam, Indi indi, boolean famSosa, boolean indiSosa, boolean childEdge) {
        Edge arcCourant = leGraphe.getEdge(fam.getId() + " - " + indi.getId());
        if (arcCourant == null && leGraphe.getNode(indi.getId()) != null) {
            final String uiClass;
            if (childEdge) {
                leGraphe.addEdge(fam.getId() + " - " + indi.getId(), fam.getId(), indi.getId(), true);
                uiClass = CHILD;
            } else {
                leGraphe.addEdge(fam.getId() + " - " + indi.getId(), indi.getId(), fam.getId(), true);
                uiClass = MARIAGE;
            }
            arcCourant = leGraphe.getEdge(fam.getId() + " - " + indi.getId());
            arcCourant.addAttribute(UI_CLASS, uiClass);
            arcCourant.addAttribute(CLASSE_ORIGINE, uiClass);
            arcCourant.addAttribute(LAYOUTWEIGHT, edgeWeight);
            if (indiSosa && famSosa) {
                arcCourant.addAttribute(UI_CLASS, SOSA);
                arcCourant.addAttribute(CLASSE_ORIGINE, SOSA);
            }
        }
    }

    private void addIndiNode(Indi indi) throws IdAlreadyInUseException {
        Node noeudCourant = leGraphe.getNode(indi.getId());
        double x = 0;
        double y = 0;
        double z = 0;
        if (noeudCourant != null) {
            final GraphicNode graphicNode = leViewer.getGraphicGraph().getNode(noeudCourant.getId());
            x = graphicNode.getX();
            y = graphicNode.getY();
            z = graphicNode.getZ();
            leGraphe.removeNode(indi.getId());
        }

        noeudCourant = leGraphe.addNode(indi.getId());
        if (x != 0 || y != 0 || z != 0) {
            noeudCourant.setAttribute("xyz", x, y, z);
        }
        noeudCourant.addAttribute(LAYOUTWEIGHT, indiNodeWeight);
        noeudCourant.addAttribute(UI_STYLE, getDisplayLabelMode());

        final SosaParser parsing = new SosaParser(indi.getSosaString());
        if (parsing.getSosa() != null) {
            if (parsing.getDaboville() == null) {
                noeudCourant.addAttribute(UI_CLASS, SOSA);
                noeudCourant.addAttribute(CLASSE_ORIGINE, SOSA);
                if (1L == parsing.getSosa()) {
                    noeudCourant.addAttribute(UI_CLASS, "cujus");
                    noeudCourant.addAttribute(CLASSE_ORIGINE, "cujus");
                }
            }
        }
        if (indi.getNameProperty() != null) {
            noeudCourant.addAttribute(UI_LABEL, indi.getNameProperty().getLastName());
        }

        for (Fam f : indi.getFamiliesWhereChild()) {
            addFamNode(f);
        }

        for (Fam f : indi.getFamiliesWhereSpouse()) {
            addFamNode(f);
        }
    }

    private boolean calcSosa(Indi indi) {
        if (indi == null) {
            return false;
        }
        final SosaParser parsing = new SosaParser(indi.getSosaString());
        return parsing.getSosa() != null && parsing.getDaboville() == null;
    }

    private String getDisplayLabelMode() {
        if (showLabel) {
            return "text-visibility-mode:normal;";
        }
        return "text-visibility-mode:hidden;";
    }

    @Override
    public void setContextImpl(Context newContext) {
        // Quit if new context is null  
        if (newContext == null || newContext.getEntity() == null) {
            return;
        }

        // Adjust new context
        Entity newEntity = newContext.getEntity();

        Node noeudCourant = leGraphe.getNode(newEntity.getId());
        if (noeudCourant != null && !STICKED.equals(noeudCourant.getAttribute(UI_CLASS))) {
            manageSelected(noeudCourant);
            centerView(noeudCourant);
        }
    }

    private void manageSelected(final Node noeudCourant) {
        for (Node n : leGraphe.getNodeSet()) {
            if (STICKED.equals(n.getAttribute(UI_CLASS))) {
                n.removeAttribute(UI_CLASS);
                final String classeOrigine = n.getAttribute(CLASSE_ORIGINE);
                if (classeOrigine != null) {
                    n.addAttribute(UI_CLASS, classeOrigine);
                }
                for (Edge e : n.getEachEdge()) {
                    if (STICKED.equals(e.getAttribute(UI_CLASS))) {
                        e.removeAttribute(UI_CLASS);
                        final String classeOrig = e.getAttribute(CLASSE_ORIGINE);
                        if (classeOrig != null) {
                            e.addAttribute(UI_CLASS, classeOrig);
                        }
                    }
                }
            }
        }

        noeudCourant.addAttribute(UI_CLASS, STICKED);
        for (Edge e : noeudCourant.getEachEdge()) {
            e.addAttribute(UI_CLASS, STICKED);
        }
    }

    private void centerView(final Node noeudCourant) {
        final String id = noeudCourant.getId();
        final GraphicNode graphicNode = leViewer.getGraphicGraph().getNode(id);
        if (recenter) {
            laVue.getCamera().setViewCenter(graphicNode.getX(), graphicNode.getY(), graphicNode.getZ());
        }
        recenter = true;
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        zoomSlider = new javax.swing.JSlider();
        jToogleButtonDisplay = new javax.swing.JToggleButton();
        jToogleButtonCenter = new javax.swing.JToggleButton();
        jButtonReset = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonLoad = new javax.swing.JButton();
        jButtonLabel = new javax.swing.JToggleButton();
        jButtonSettings = new javax.swing.JButton();
        jButtonPrint = new javax.swing.JButton();
        jButtonGEXF = new javax.swing.JButton();
        graphPanel = new javax.swing.JPanel();

        buttonPanel.setMaximumSize(new java.awt.Dimension(50, 32767));
        buttonPanel.setMinimumSize(new java.awt.Dimension(50, 100));
        buttonPanel.setPreferredSize(new java.awt.Dimension(50, 600));

        zoomSlider.setMajorTickSpacing(20);
        zoomSlider.setMinimum(1);
        zoomSlider.setMinorTickSpacing(5);
        zoomSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.zoomSlider.toolTipText")); // NOI18N
        zoomSlider.setValue(1);
        zoomSlider.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        zoomSlider.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        zoomSlider.setMaximumSize(new java.awt.Dimension(50, 150));
        zoomSlider.setMinimumSize(new java.awt.Dimension(50, 150));
        zoomSlider.setOpaque(false);
        zoomSlider.setPreferredSize(new java.awt.Dimension(50, 150));
        zoomSlider.setRequestFocusEnabled(false);
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomSliderStateChanged(evt);
            }
        });

        jToogleButtonDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/pause-16.png"))); // NOI18N
        jToogleButtonDisplay.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jToogleButtonDisplay.toolTipText")); // NOI18N
        jToogleButtonDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToogleButtonDisplayActionPerformed(evt);
            }
        });

        jToogleButtonCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/root.png"))); // NOI18N
        jToogleButtonCenter.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jToogleButtonCenter.toolTipText")); // NOI18N
        jToogleButtonCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToogleButtonCenterActionPerformed(evt);
            }
        });

        jButtonReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/reset.png"))); // NOI18N
        jButtonReset.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonReset.toolTipText")); // NOI18N
        jButtonReset.setMaximumSize(new java.awt.Dimension(49, 25));
        jButtonReset.setMinimumSize(new java.awt.Dimension(49, 25));
        jButtonReset.setPreferredSize(new java.awt.Dimension(49, 25));
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Save.png"))); // NOI18N
        jButtonSave.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonSave.toolTipText")); // NOI18N
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/reload.png"))); // NOI18N
        jButtonLoad.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonLoad.toolTipText")); // NOI18N
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });

        jButtonLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Advanced.png"))); // NOI18N
        jButtonLabel.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonLabel.toolTipText")); // NOI18N
        jButtonLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLabelActionPerformed(evt);
            }
        });

        jButtonSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Settings.png"))); // NOI18N
        jButtonSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSettingsActionPerformed(evt);
            }
        });

        jButtonPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Print.png"))); // NOI18N
        jButtonPrint.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonPrint.toolTipText")); // NOI18N
        jButtonPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintActionPerformed(evt);
            }
        });

        jButtonGEXF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/export.png"))); // NOI18N
        jButtonGEXF.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonGEXF.toolTipText")); // NOI18N
        jButtonGEXF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGEXFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addComponent(jToogleButtonCenter, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonReset, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                    .addComponent(jButtonLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jToogleButtonDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zoomSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                        .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(buttonPanelLayout.createSequentialGroup()
                                .addComponent(jButtonPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(buttonPanelLayout.createSequentialGroup()
                                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonGEXF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(jButtonLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(2, 2, 2))))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToogleButtonDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToogleButtonCenter)
                    .addComponent(jButtonReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(90, 90, 90)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSettings)
                    .addComponent(jButtonPrint))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSave))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonGEXF)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        graphPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        graphPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                graphPanelMouseWheelMoved(evt);
            }
        });
        graphPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                graphPanelMouseClicked(evt);
            }
        });
        graphPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(graphPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
        laVue.getCamera().setViewPercent(Math.pow(0.01D * zoomSlider.getValue(), 2));
    }//GEN-LAST:event_zoomSliderStateChanged

    private void graphPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseClicked
        if (centerGraph) {
            Point3 p = laVue.getCamera().getViewCenter();
            Point3 p2 = laVue.getCamera().transformPxToGu(evt.getX(), evt.getY());
            laVue.getCamera().setViewCenter(p2.x, p2.y, p.z);
        }

        final GraphicElement clicked = laVue.findNodeOrSpriteAt(evt.getX(), evt.getY());
        if (clicked instanceof Node) {
            // Don't recenter on select.
            recenter = false;
            SelectionDispatcher.fireSelection(evt, new Context(getContext().getGedcom().getEntity(clicked.getId())));
        }
    }//GEN-LAST:event_graphPanelMouseClicked


    private void jToogleButtonDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToogleButtonDisplayActionPerformed
        if (autoDisplay) {
            autoDisplay = false;
            leViewer.disableAutoLayout();
        } else {
            autoDisplay = true;
            leViewer.enableAutoLayout();
        }
    }//GEN-LAST:event_jToogleButtonDisplayActionPerformed

    private void jButtonLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLabelActionPerformed
        showLabel = !showLabel;
        manageDisplayLabels();
    }//GEN-LAST:event_jButtonLabelActionPerformed

    private void jToogleButtonCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToogleButtonCenterActionPerformed
        centerGraph = !centerGraph;
    }//GEN-LAST:event_jToogleButtonCenterActionPerformed

    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
        laVue.getCamera().resetView();
        zoomSlider.setValue(100);
        centerGraph = false;
        jToogleButtonCenter.setSelected(false);
        showLabel = false;
        jButtonLabel.setSelected(false);
        manageDisplayLabels();
    }//GEN-LAST:event_jButtonResetActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        String gedcomName = removeExtension(getGedcom().getName());
        File file = new FileChooserBuilder(GraphTopComponent.class)
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(GraphTopComponent.class, "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(GraphTopComponent.class, "FileChooserOKButton"))
                .setFileFilter(FileChooserBuilder.getTextFilter())
                .setAcceptAllFileFilterUsed(false)
                .setDefaultExtension(FileChooserBuilder.getTextFilter().getExtensions()[0])
                .setFileHiding(true)
                .setSelectedFile(new File(gedcomName + "-graph"))
                .showSaveDialog();

        if (file != null) {
            showWaitCursor();
            final GraphFileWriter gfw = new GraphFileWriter(file, leViewer.getGraphicGraph());
            gfw.start(getContext().getGedcom().getName());
            hideWaitCursor();
        }

    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
        String gedcomName = removeExtension(getGedcom().getName());
        File file = new FileChooserBuilder(GraphTopComponent.class)
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(GraphTopComponent.class, "FileOpenTitle"))
                .setApproveText(NbBundle.getMessage(GraphTopComponent.class, "FileOpenOKButton"))
                .setFileFilter(FileChooserBuilder.getTextFilter())
                .setAcceptAllFileFilterUsed(false)
                .setDefaultExtension(FileChooserBuilder.getTextFilter().getExtensions()[0])
                .setFileHiding(true)
                .setSelectedFile(new File(gedcomName + "-graph"))
                .showOpenDialog();

        if (file != null) {
            showWaitCursor();
            final GraphFileReader gfr = new GraphFileReader(file, leGraphe, getContext().getGedcom());
            gfr.start();
            fillGraph();
            hideWaitCursor();
        }
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jButtonSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSettingsActionPerformed
        final GraphSettings gs = new GraphSettings(this);
        DialogManager dm = DialogManager.create(NbBundle.getMessage(GraphTopComponent.class, "SettingsTitle"), gs).setOptionType(DialogManager.OK_ONLY_OPTION).setDialogId(GraphSettings.class);
        if (DialogManager.OK_OPTION.equals(dm.show())) {
            saveSettings();
        }
        dm.cancel();

    }//GEN-LAST:event_jButtonSettingsActionPerformed

    private void jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintActionPerformed
        String gedcomName = removeExtension(getGedcom().getName());
        File file = new FileChooserBuilder(GraphTopComponent.class)
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(GraphTopComponent.class, "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(GraphTopComponent.class, "FileChooserOKButton"))
                .setFileFilter(FileChooserBuilder.getImageFilter())
                .setAcceptAllFileFilterUsed(false)
                .setDefaultExtension(FileChooserBuilder.getImageFilter().getExtensions()[6])
                .setFileHiding(true)
                .setSelectedFile(new File(gedcomName + "-graph"))
                .showSaveDialog();

        if (file != null) {
            showWaitCursor();
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                final FileSink fs = new AncestrisFileSinkSvg(leGraphe);
                fs.writeAll(leViewer.getGraphicGraph(), writer);

                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Unable to write Graph File or open it.", e);
            }

            hideWaitCursor();
        }
    }//GEN-LAST:event_jButtonPrintActionPerformed

    private void jButtonGEXFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGEXFActionPerformed
        String gedcomName = removeExtension(getGedcom().getName());
        File file = new FileChooserBuilder(GraphTopComponent.class)
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(GraphTopComponent.class, "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(GraphTopComponent.class, "FileChooserOKButton"))
                .setFileFilter(new FileNameExtensionFilter("GEXF", "gexf"))
                .setAcceptAllFileFilterUsed(false)
                .setDefaultExtension("gexf")
                .setFileHiding(true)
                .setSelectedFile(new File(gedcomName + "-graph"))
                .showSaveDialog();

        if (file != null) {
            showWaitCursor();
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                final FileSink fs = new FileSinkGEXF();
                fs.writeAll(leGraphe, writer);

                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Unable to write Graph File or open it.", e);
            }
            hideWaitCursor();
        }
    }//GEN-LAST:event_jButtonGEXFActionPerformed

    private void graphPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_graphPanelMouseWheelMoved
        double changeZoom = 5 * evt.getPreciseWheelRotation();
        int newValue = zoomSlider.getValue() + Double.valueOf(changeZoom).intValue();
        if (newValue > 100) {
            newValue = 100;
        }
        if (newValue <1) {
            newValue = 1;
        }
        zoomSlider.setValue(newValue);
        
        
    }//GEN-LAST:event_graphPanelMouseWheelMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JButton jButtonGEXF;
    private javax.swing.JToggleButton jButtonLabel;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonPrint;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSettings;
    private javax.swing.JToggleButton jToogleButtonCenter;
    private javax.swing.JToggleButton jToogleButtonDisplay;
    private javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        Gedcom gedcom = getContext().getGedcom();
        if (gedcom != null) {
            gedcom.addGedcomListener((GedcomListener) Spin.over(listener));
        }
    }

    @Override
    public void componentClosed() {
        Gedcom gedcom = getContext().getGedcom();
        if (gedcom != null) {
            gedcom.removeGedcomListener((GedcomListener) Spin.over(listener));
        }
    }

    private void manageDisplayLabels() {
        for (Node noeud : leGraphe.getNodeSet()) {
            noeud.addAttribute(UI_STYLE, getDisplayLabelMode());
        }
    }

    public void changeDisplay(ModifEntity entities) {
        // Add new Indi nodes
        for (Entity e : entities.getIndiAdded()) {
            manageEntity(e);
        }

        // delete old Indi nodes
        for (Entity e : entities.getIndiDeleted()) {
            Node noeudCourant = leGraphe.getNode(e.getId());
            if (noeudCourant != null) {
                leGraphe.removeNode(noeudCourant.getId());
            }
        }

        // Add new Fam nodes
        for (Entity e : entities.getFamAdded()) {
            manageEntity(e);
        }

        // delete old Fam nodes
        for (Entity e : entities.getFamDeleted()) {
            Node noeudCourant = leGraphe.getNode(e.getId());
            if (noeudCourant != null) {
                leGraphe.removeNode(noeudCourant.getId());
            }
        }

        // Modify exiting Indi nodes
        for (Indi e : entities.getIndiModified()) {
            if (!entities.getIndiDeleted().contains(e)) {
                manageEntity(e);
            }
        }

        // Modify exiting Fam nodes
        for (Fam e : entities.getFamModified()) {
            if (!entities.getFamDeleted().contains(e)) {
                manageEntity(e);
            }
        }

        laVue.repaint();
    }

    private void manageEntity(Entity entity) {
        if (entity instanceof Indi) {
            addIndiNode((Indi) entity);
        }
        if (entity instanceof Fam) {
            addFamNode((Fam) entity);
        }
    }

    @Override
    public Gedcom getGedcom() {
        return getContext().getGedcom();
    }

    private String removeExtension(String filename) {

        String separator = System.getProperty("file.separator");

        // Remove the path upto the filename.
        int lastSeparatorIndex = filename.lastIndexOf(separator);
        if (lastSeparatorIndex != -1) {
            filename = filename.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    private static void showWaitCursor() {
        Mutex.EVENT.readAccess(() -> {
            JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
            mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            mainWindow.getGlassPane().setVisible(true);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.start"));
        });
    }

    private static void hideWaitCursor() {
        Mutex.EVENT.readAccess(() -> {
            StatusDisplayer.getDefault().setStatusText("");  //NOI18N
            JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
            mainWindow.getGlassPane().setVisible(false);
            mainWindow.getGlassPane().setCursor(null);
        });
    }

    public String getColorSosa() {
        return colorSosa;
    }

    public void setColorSosa(String colorSosa) {
        this.colorSosa = colorSosa;
    }

    public String getColorMariage() {
        return colorMariage;
    }

    public void setColorMariage(String colorMariage) {
        this.colorMariage = colorMariage;
    }

    public String getColorChild() {
        return colorChild;
    }

    public void setColorChild(String colorChild) {
        this.colorChild = colorChild;
    }

    public String getColorCujus() {
        return colorCujus;
    }

    public void setColorCujus(String colorCujus) {
        this.colorCujus = colorCujus;
    }

    public String getColorSticked() {
        return colorSticked;
    }

    public void setColorSticked(String colorSticked) {
        this.colorSticked = colorSticked;
    }

    public String getColorBack() {
        return colorBack;
    }

    public void setColorBack(String colorBack) {
        this.colorBack = colorBack;
    }

    public String getColorMariageSosa() {
        return colorMariageSosa;
    }

    public void setColorMariageSosa(String colorMariageSosa) {
        this.colorMariageSosa = colorMariageSosa;
    }

    public String getSizeEdge() {
        return sizeEdge;
    }

    public void setSizeEdge(String sizeEdge) {
        this.sizeEdge = sizeEdge;
    }

    public String getSizeNode() {
        return sizeNode;
    }

    public void setSizeNode(String sizeNode) {
        this.sizeNode = sizeNode;
    }

    public String getSizeCujus() {
        return sizeCujus;
    }

    public void setSizeCujus(String sizeCujus) {
        this.sizeCujus = sizeCujus;
    }

    public String getSizeNodeSosa() {
        return sizeNodeSosa;
    }

    public void setSizeNodeSosa(String sizeNodeSosa) {
        this.sizeNodeSosa = sizeNodeSosa;
    }

    public String getSizeEdgeSosa() {
        return sizeEdgeSosa;
    }

    public void setSizeEdgeSosa(String sizeEdgeSosa) {
        this.sizeEdgeSosa = sizeEdgeSosa;
    }

    public String getColorDef() {
        return colorDef;
    }

    public void setColorDef(String colorDef) {
        this.colorDef = colorDef;
    }

    public double getIndiNodeWeight() {
        return indiNodeWeight;
    }

    public void setIndiNodeWeight(double indiNodeWeight) {
        this.indiNodeWeight = indiNodeWeight;
    }

    public double getMariageNodeWeight() {
        return mariageNodeWeight;
    }

    public void setMariageNodeWeight(double mariageNodeWeight) {
        this.mariageNodeWeight = mariageNodeWeight;
    }

    public double getEdgeWeight() {
        return edgeWeight;
    }

    public void setEdgeWeight(double edgeWeight) {
        this.edgeWeight = edgeWeight;
    }

    public void updateCss() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format(CSS, colorSosa, sizeNodeSosa, colorMariage, colorMariageSosa, sizeNodeSosa, colorSosa,
                sizeEdgeSosa, colorMariage, colorChild, colorCujus, sizeCujus,
                sizeEdge, colorDef, sizeNode, colorDef, colorSticked, colorSticked, colorBack);
        leGraphe.setAttribute(UISTYLESHEET, sb.toString());

    }

    public void updateWeight() {
        for (Node n : leGraphe.getEachNode()) {
            if (n.hasAttribute(FAM)) {
                n.setAttribute(LAYOUTWEIGHT, mariageNodeWeight);
            } else {
                n.setAttribute(LAYOUTWEIGHT, indiNodeWeight);
            }
        }

        for (Edge e : leGraphe.getEachEdge()) {
            e.setAttribute(LAYOUTWEIGHT, edgeWeight);
        }
    }

    private void loadSettings() {
        if (registry == null) {
            registry = getGedcom().getRegistry();
        }
        colorDef = registry.get("GRAPH.color.default", "#000000");
        colorSosa = registry.get("GRAPH.color.sosa", "#006400");
        colorMariage = registry.get("GRAPH.color.marriage", "#FF4500");
        colorChild = registry.get("GRAPH.color.child", "#708090");
        colorCujus = registry.get("GRAPH.color.cujus", "#FF00FF");
        colorSticked = registry.get("GRAPH.color.sticked", "#0000FF");
        colorBack = registry.get("GRAPH.color.back", "#FFFFFF");
        colorMariageSosa = registry.get("GRAPH.color.marriage.sosa", "#FFCC33");
        sizeEdge = registry.get("GRAPH.size.edge", "2");
        sizeNode = registry.get("GRAPH.size.node", "8");
        sizeCujus = registry.get("GRAPH.size.cujus", "20");
        sizeNodeSosa = registry.get("GRAPH.size.node.sosa", "8");
        sizeEdgeSosa = registry.get("GRAPH.size.edge.sosa", "2");
        indiNodeWeight = Double.valueOf(registry.get("GRAPH.weight.node.indi", "10.0"));
        mariageNodeWeight = Double.valueOf(registry.get("GRAPH.weight.node.fam", "5.0"));
        edgeWeight = Double.valueOf(registry.get("GRAPH.weight.edge", "1.0"));
    }

    public void saveSettings() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            if (registry == null) {
                return;
            }
            registry.put("GRAPH.color.default", colorDef);
            registry.put("GRAPH.color.sosa", colorSosa);
            registry.put("GRAPH.color.marriage", colorMariage);
            registry.put("GRAPH.color.child", colorChild);
            registry.put("GRAPH.color.cujus", colorCujus);
            registry.put("GRAPH.color.sticked", colorSticked);
            registry.put("GRAPH.color.back", colorBack);
            registry.put("GRAPH.color.marriage.sosa", colorMariageSosa);
            registry.put("GRAPH.size.edge", sizeEdge);
            registry.put("GRAPH.size.node", sizeNode);
            registry.put("GRAPH.size.cujus", sizeCujus);
            registry.put("GRAPH.size.node.sosa", sizeNodeSosa);
            registry.put("GRAPH.size.edge.sosa", sizeEdgeSosa);
            registry.put("GRAPH.weight.node.indi", String.valueOf(indiNodeWeight));
            registry.put("GRAPH.weight.node.fam", String.valueOf(mariageNodeWeight));
            registry.put("GRAPH.weight.edge", String.valueOf(edgeWeight));
        });
    }
}
