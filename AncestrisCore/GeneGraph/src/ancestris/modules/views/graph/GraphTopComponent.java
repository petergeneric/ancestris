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

import ancestris.modules.views.graph.graphstream.AncestrisAStar;
import ancestris.modules.views.graph.graphstream.AncestrisFileSinkGEXF;
import ancestris.modules.views.graph.graphstream.AncestrisFileSinkSvg;
import ancestris.modules.views.graph.graphstream.AncestrisMouseManager;
import ancestris.modules.views.graph.graphstream.AncestrisMultiGraph;
import ancestris.util.SosaParser;
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
import genj.gedcom.PropertyAssociation;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.stream.file.FileSink;
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
import org.openide.windows.WindowManager;
import spin.Spin;

/**
 * Graph Top Component.
 *
 * Author : Zurga
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class GraphTopComponent extends AncestrisTopComponent {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private static final String ICON_PATH = "ancestris/modules/views/graph/resources/graphe.png";
    private static final String PREFERRED_ID = "GraphTopComponent";
    private static final String UI_STYLE = "ui.style";
    private static final String UI_LABEL = "label";
    private static final String CHILD = "child";
    private static final String ASSO = "asso";
    private static final String MARIAGE = "mariage";
    private static final String CLASSE_ORIGINE = "classe.origine";
    private static final String UI_CLASS = "ui.class";
    private static final String STICKED = "sticked";
    private static final String SOSA = "sosa";
    private static final String MARRIAGE_SOSA = "mariagesosa";
    private static final String LAYOUTWEIGHT = "layout.weight";
    private static final String UISTYLESHEET = "ui.stylesheet";
    private static final String FAM = "famille";
    private static final String LABEL_FAM_LIEU = "FAM:MARR:PLACE";
    private static final String LABEL_FAM_SIGN = "S_FAM:MARR:DATE";
    private static final String LABEL_FAM_ID = "FAM_ID_DATE";
    private static final String LABEL_FAM_DATE = "FAM:MARR:DATE";
    private static final String LABEL_INDI_GIVN = "INDI:NAME:GIVN";
    private static final String LABEL_INDI_NAME = "INDI:NAME:SURN";
    private static final String GENERATION = "generation";
    private static final String SOSA_NUMBER = "sosa";

    private static GraphTopComponent factory;

    private genj.util.Registry registry = null;

    private final Graph leGraphe = new AncestrisMultiGraph("Arbre");
    private final Viewer leViewer = new Viewer(leGraphe, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
    private final ViewPanel laVue = leViewer.addDefaultView(false);

    private final GrapheGedcomListenerAdapter listener;

    private GraphParameter graphParam = new GraphParameter();

    private final JPopupMenu hidePopup;
    private Node hideNode;
    private final Map<String, double[]> nodesHidden = new HashMap<>();
    private final Set<HideEdge> edgesHidden = new HashSet<>();
    
    private Integer maxGeneration = Integer.valueOf(0);
    private Integer minGeneration = Integer.valueOf(0);

    private String pathNode;

    // Should recenter when select ?
    private boolean recenter = true;

    public GraphTopComponent() {
        super();
        listener = new GrapheGedcomListenerAdapter(this);
        hidePopup = new JPopupMenu();
    }
    
    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.OUTPUT;
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

        zoomSlider.setValue(1);

        updateCss();
        leGraphe.setAttribute("ui.antialias");
        leViewer.enableAutoLayout();
        leViewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);

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

        // prepare popupMenu
        final JMenuItem itemAsc = new JMenuItem(NbBundle.getMessage(GraphTopComponent.class, "Popup.ascendency"));
        itemAsc.addActionListener((ActionEvent e) -> {
            hideNode(true);
        });
        hidePopup.add(itemAsc);
        final JMenuItem itemDesc = new JMenuItem(NbBundle.getMessage(GraphTopComponent.class, "Popup.descendency"));
        itemDesc.addActionListener((ActionEvent e) -> {
            hideNode(false);
        });
        hidePopup.add(itemDesc);

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
        manageLabels();
        manageAsso();
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
        noeudCourant = createFamNode(fam, x, y, z);
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

    private Node createFamNode(Fam fam, double x, double y, double z) {
        Node noeudCourant;
        noeudCourant = leGraphe.addNode(fam.getId());
        if (x != 0 || y != 0 || z != 0) {
            noeudCourant.setAttribute("xyz", x, y, z);
        }
        noeudCourant.addAttribute(LAYOUTWEIGHT, graphParam.getMariageNodeWeight());
        noeudCourant.addAttribute(UI_CLASS, MARIAGE);
        noeudCourant.addAttribute(CLASSE_ORIGINE, MARIAGE);
        noeudCourant.addAttribute(UI_STYLE, getDisplayLabelMode());
        noeudCourant.addAttribute(FAM);
        if (fam.getMarriageDate() != null) {
            noeudCourant.addAttribute(LABEL_FAM_DATE, fam.getMarriageDate().getDisplayValue());
            noeudCourant.addAttribute(LABEL_FAM_SIGN, "x " + fam.getMarriageDate().getDisplayValue());
            noeudCourant.addAttribute(LABEL_FAM_ID, fam.getId() + " " + fam.getMarriageDate().getDisplayValue());
        }
        if (fam.getMarriagePlace() != null) {
            noeudCourant.addAttribute(LABEL_FAM_LIEU, fam.getMarriagePlace().getCity());
        }
        return noeudCourant;
    }

    private void createChildEdge(Fam fam, Indi child, boolean childSosa) {
        createEdge(fam, child, true, childSosa, CHILD);
    }

    private void createSpouseEdge(Fam fam, Indi spouse, boolean spouseSosa, boolean famSosa) {
        createEdge(fam, spouse, famSosa, spouseSosa, MARIAGE);
    }

    private void createEdge(Fam fam, Indi indi, boolean famSosa, boolean indiSosa, String uiClass) {
        Edge arcCourant = leGraphe.getEdge(fam.getId() + " - " + indi.getId());
        if (arcCourant == null && leGraphe.getNode(indi.getId()) != null) {
            if (CHILD.equals(uiClass)) {
                leGraphe.addEdge(fam.getId() + " - " + indi.getId(), fam.getId(), indi.getId(), true);
            } else {
                leGraphe.addEdge(fam.getId() + " - " + indi.getId(), indi.getId(), fam.getId(), true);
            }

            arcCourant = leGraphe.getEdge(fam.getId() + " - " + indi.getId());
            arcCourant.addAttribute(UI_CLASS, uiClass);
            arcCourant.addAttribute(CLASSE_ORIGINE, uiClass);
            arcCourant.addAttribute(LAYOUTWEIGHT, graphParam.getEdgeWeight());
            if (indiSosa && famSosa) {
                arcCourant.addAttribute(UI_CLASS, SOSA);
                arcCourant.addAttribute(CLASSE_ORIGINE, SOSA);
            }
        }
    }

    private void addIndiNode(Indi indi) {
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

        createIndiNode(indi, x, y, z);

        for (Fam f : indi.getFamiliesWhereChild()) {
            addFamNode(f);
        }

        for (Fam f : indi.getFamiliesWhereSpouse()) {
            addFamNode(f);
        }
    }

    private void createIndiNode(Indi indi, double x, double y, double z) {
        Node noeudCourant = leGraphe.addNode(indi.getId());
        if (x != 0 || y != 0 || z != 0) {
            noeudCourant.setAttribute("xyz", x, y, z);
        }
        noeudCourant.addAttribute(LAYOUTWEIGHT, graphParam.getIndiNodeWeight());
        noeudCourant.addAttribute(UI_STYLE, getDisplayLabelMode());
        final SosaParser parsing = new SosaParser(indi.getSosaString());
        if (parsing.getSosa() != null) {
            noeudCourant.addAttribute(SOSA_NUMBER, indi.getSosaString());
            if (parsing.getGeneration() != null) {
                Integer generation = parsing.getGeneration();
                noeudCourant.addAttribute(GENERATION, generation);
                if (maxGeneration < generation) {
                    maxGeneration = generation;
                }
                if (minGeneration > generation) {
                    minGeneration = generation;
                }
            }
            if (parsing.getDaboville() == null) {
                noeudCourant.addAttribute(UI_CLASS, SOSA);
                noeudCourant.addAttribute(CLASSE_ORIGINE, SOSA);
                if (BigInteger.ONE.equals(parsing.getSosa())) {
                    noeudCourant.addAttribute(UI_CLASS, "cujus");
                    noeudCourant.addAttribute(CLASSE_ORIGINE, "cujus");
                }
            }
        }
        if (indi.getNameProperty() != null) {
            noeudCourant.addAttribute(LABEL_INDI_NAME, indi.getNameProperty().getLastName());
            noeudCourant.addAttribute(LABEL_INDI_GIVN, indi.getNameProperty().getDisplayValue());
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
        if (graphParam.isShowLabel()) {
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

        if (noeudCourant != null) {
            noeudCourant.addAttribute(UI_CLASS, STICKED);
            for (Edge e : noeudCourant.getEachEdge()) {
                e.addAttribute(UI_CLASS, STICKED);
            }
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

        graphPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        zoomSlider = new javax.swing.JSlider();
        jToolBar1 = new javax.swing.JToolBar();
        jToogleButtonDisplay = new javax.swing.JToggleButton();
        jButtonLabel = new javax.swing.JToggleButton();
        jToogleButtonCenter = new javax.swing.JToggleButton();
        jToogleButtonHide = new javax.swing.JToggleButton();
        jToggleButtonAsso = new javax.swing.JToggleButton();
        jToggleButtonPath = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonSave = new javax.swing.JButton();
        jButtonLoad = new javax.swing.JButton();
        jButtonPrint = new javax.swing.JButton();
        jButtonGEXF = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jButtonReset = new javax.swing.JButton();
        jButtonSettings = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(6, 32767));

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

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        zoomSlider.setMinimum(1);
        zoomSlider.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.zoomSlider.toolTipText")); // NOI18N
        zoomSlider.setValue(100);
        zoomSlider.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        zoomSlider.setMaximumSize(new java.awt.Dimension(204, 25));
        zoomSlider.setRequestFocusEnabled(false);
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomSliderStateChanged(evt);
            }
        });

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jToogleButtonDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/pause-16.png"))); // NOI18N
        jToogleButtonDisplay.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jToogleButtonDisplay.toolTipText")); // NOI18N
        jToogleButtonDisplay.setMaximumSize(new java.awt.Dimension(27, 25));
        jToogleButtonDisplay.setMinimumSize(new java.awt.Dimension(27, 25));
        jToogleButtonDisplay.setPreferredSize(new java.awt.Dimension(27, 25));
        jToogleButtonDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToogleButtonDisplayActionPerformed(evt);
            }
        });
        jToolBar1.add(jToogleButtonDisplay);

        jButtonLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Advanced.png"))); // NOI18N
        jButtonLabel.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonLabel.toolTipText")); // NOI18N
        jButtonLabel.setMaximumSize(new java.awt.Dimension(27, 25));
        jButtonLabel.setMinimumSize(new java.awt.Dimension(27, 25));
        jButtonLabel.setPreferredSize(new java.awt.Dimension(27, 25));
        jButtonLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLabelActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonLabel);

        jToogleButtonCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/root.png"))); // NOI18N
        jToogleButtonCenter.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jToogleButtonCenter.toolTipText")); // NOI18N
        jToogleButtonCenter.setMaximumSize(new java.awt.Dimension(27, 25));
        jToogleButtonCenter.setMinimumSize(new java.awt.Dimension(27, 25));
        jToogleButtonCenter.setPreferredSize(new java.awt.Dimension(27, 25));
        jToogleButtonCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToogleButtonCenterActionPerformed(evt);
            }
        });
        jToolBar1.add(jToogleButtonCenter);

        jToogleButtonHide.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/fantome.png"))); // NOI18N
        jToogleButtonHide.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jToogleButtonHide.toolTipText")); // NOI18N
        jToogleButtonHide.setMaximumSize(new java.awt.Dimension(27, 25));
        jToogleButtonHide.setMinimumSize(new java.awt.Dimension(27, 25));
        jToogleButtonHide.setPreferredSize(new java.awt.Dimension(27, 25));
        jToogleButtonHide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToogleButtonHideActionPerformed(evt);
            }
        });
        jToolBar1.add(jToogleButtonHide);

        jToggleButtonAsso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/asso.png"))); // NOI18N
        jToggleButtonAsso.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jToggleButtonAsso.toolTipText")); // NOI18N
        jToggleButtonAsso.setMaximumSize(new java.awt.Dimension(27, 25));
        jToggleButtonAsso.setMinimumSize(new java.awt.Dimension(27, 25));
        jToggleButtonAsso.setPreferredSize(new java.awt.Dimension(27, 25));
        jToggleButtonAsso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonAssoActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonAsso);

        jToggleButtonPath.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/link.png"))); // NOI18N
        jToggleButtonPath.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jToggleButtonPath.toolTipText")); // NOI18N
        jToggleButtonPath.setMaximumSize(new java.awt.Dimension(27, 25));
        jToggleButtonPath.setMinimumSize(new java.awt.Dimension(27, 25));
        jToggleButtonPath.setPreferredSize(new java.awt.Dimension(27, 25));
        jToggleButtonPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonPathActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonPath);
        jToolBar1.add(jSeparator1);

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Save.png"))); // NOI18N
        jButtonSave.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonSave.toolTipText")); // NOI18N
        jButtonSave.setMaximumSize(new java.awt.Dimension(27, 25));
        jButtonSave.setMinimumSize(new java.awt.Dimension(27, 25));
        jButtonSave.setPreferredSize(new java.awt.Dimension(27, 25));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSave);

        jButtonLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/reload.png"))); // NOI18N
        jButtonLoad.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonLoad.toolTipText")); // NOI18N
        jButtonLoad.setMaximumSize(new java.awt.Dimension(27, 25));
        jButtonLoad.setMinimumSize(new java.awt.Dimension(27, 25));
        jButtonLoad.setPreferredSize(new java.awt.Dimension(27, 25));
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonLoad);

        jButtonPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Print.png"))); // NOI18N
        jButtonPrint.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonPrint.toolTipText")); // NOI18N
        jButtonPrint.setMaximumSize(new java.awt.Dimension(27, 25));
        jButtonPrint.setMinimumSize(new java.awt.Dimension(27, 25));
        jButtonPrint.setPreferredSize(new java.awt.Dimension(27, 25));
        jButtonPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonPrint);

        jButtonGEXF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/export.png"))); // NOI18N
        jButtonGEXF.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonGEXF.toolTipText")); // NOI18N
        jButtonGEXF.setMaximumSize(new java.awt.Dimension(27, 25));
        jButtonGEXF.setMinimumSize(new java.awt.Dimension(27, 25));
        jButtonGEXF.setPreferredSize(new java.awt.Dimension(27, 25));
        jButtonGEXF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGEXFActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonGEXF);
        jToolBar1.add(filler2);

        jButtonReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/reset.png"))); // NOI18N
        jButtonReset.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonReset.toolTipText")); // NOI18N
        jButtonReset.setMaximumSize(new java.awt.Dimension(27, 25));
        jButtonReset.setMinimumSize(new java.awt.Dimension(27, 25));
        jButtonReset.setPreferredSize(new java.awt.Dimension(27, 25));
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonReset);

        jButtonSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/views/graph/resources/Settings.png"))); // NOI18N
        jButtonSettings.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jButtonSettings.toolTipText")); // NOI18N
        jButtonSettings.setMaximumSize(new java.awt.Dimension(27, 25));
        jButtonSettings.setMinimumSize(new java.awt.Dimension(27, 25));
        jButtonSettings.setPreferredSize(new java.awt.Dimension(27, 25));
        jButtonSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSettingsActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSettings);
        jToolBar1.add(filler1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
        laVue.getCamera().setViewPercent(Math.pow(0.01D * (100-zoomSlider.getValue()), 2));
    }//GEN-LAST:event_zoomSliderStateChanged

    private void graphPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_graphPanelMouseClicked
        if (graphParam.isCenterGraph()) {
            Point3 p = laVue.getCamera().getViewCenter();
            Point3 p2 = laVue.getCamera().transformPxToGu(evt.getX(), evt.getY());
            laVue.getCamera().setViewCenter(p2.x, p2.y, p.z);
        }

        final GraphicElement clicked = laVue.findNodeOrSpriteAt(evt.getX(), evt.getY());
        if (clicked instanceof Node) {
            // Don't recenter on select.
            recenter = false;
            SelectionDispatcher.fireSelection(evt, new Context(getContext().getGedcom().getEntity(clicked.getId())));

            if (graphParam.isHideNodes()) {
                hideNode = leGraphe.getNode(clicked.getId());
                hidePopup.show(this, evt.getX(), evt.getY());
            }

            if (graphParam.isDoPath()) {
                if (pathNode != null) {
                    showPath(clicked.getId());
                } else {
                    pathNode = clicked.getId();
                }
            }
        }
    }//GEN-LAST:event_graphPanelMouseClicked


    private void jToogleButtonDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToogleButtonDisplayActionPerformed
        if (graphParam.isAutoDisplay()) {
            graphParam.setAutoDisplay(false);
            leViewer.disableAutoLayout();
        } else {
            graphParam.setAutoDisplay(true);
            leViewer.enableAutoLayout();
        }
    }//GEN-LAST:event_jToogleButtonDisplayActionPerformed

    private void jButtonLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLabelActionPerformed
        graphParam.setShowLabel(!graphParam.isShowLabel());
        manageDisplayLabels();
    }//GEN-LAST:event_jButtonLabelActionPerformed

    private void jToogleButtonCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToogleButtonCenterActionPerformed
        graphParam.setCenterGraph(!graphParam.isCenterGraph());
    }//GEN-LAST:event_jToogleButtonCenterActionPerformed

    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
        laVue.getCamera().resetView();
        zoomSlider.setValue(1);
        graphParam.setCenterGraph(false);
        jToogleButtonCenter.setSelected(false);
        graphParam.setShowLabel(false);
        jButtonLabel.setSelected(false);
        graphParam.setHideNodes(false);
        jToogleButtonHide.setSelected(false);
        graphParam.setShowAsso(false);
        jToggleButtonAsso.setSelected(false);
        graphParam.setDoPath(false);
        jToggleButtonPath.setSelected(false);
        displayNodes();
        manageDisplayLabels();
        manageAsso();
        manageSelected(null);
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
                final FileSink fs = new AncestrisFileSinkGEXF();
                fs.writeAll(leGraphe, writer);

                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Unable to write Graph File or open it.", e);
            }
            hideWaitCursor();
        }
    }//GEN-LAST:event_jButtonGEXFActionPerformed

    private void graphPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_graphPanelMouseWheelMoved
        double changeZoom = -5 * evt.getPreciseWheelRotation();
        int newValue = zoomSlider.getValue() + Double.valueOf(changeZoom).intValue();
        if (newValue > 100) {
            newValue = 100;
        }
        if (newValue < 1) {
            newValue = 1;
        }
        zoomSlider.setValue(newValue);


    }//GEN-LAST:event_graphPanelMouseWheelMoved

    private void jToogleButtonHideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToogleButtonHideActionPerformed
        // Hides Nodes and Search for path are mutually exclusives
        graphParam.setHideNodes(!graphParam.isHideNodes());
        graphParam.setDoPath(false);
        jToggleButtonPath.setSelected(false);

    }//GEN-LAST:event_jToogleButtonHideActionPerformed

    private void jToggleButtonAssoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonAssoActionPerformed
        graphParam.setShowAsso(!graphParam.isShowAsso());
        manageAsso();
    }//GEN-LAST:event_jToggleButtonAssoActionPerformed

    private void jToggleButtonPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonPathActionPerformed
        // Hides Nodes and Search for path are mutually exclusives
        graphParam.setDoPath(!graphParam.isDoPath());
        graphParam.setHideNodes(false);
        jToogleButtonHide.setSelected(false);
    }//GEN-LAST:event_jToggleButtonPathActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JButton jButtonGEXF;
    private javax.swing.JToggleButton jButtonLabel;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonPrint;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSettings;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToggleButton jToggleButtonAsso;
    private javax.swing.JToggleButton jToggleButtonPath;
    private javax.swing.JToggleButton jToogleButtonCenter;
    private javax.swing.JToggleButton jToogleButtonDisplay;
    private javax.swing.JToggleButton jToogleButtonHide;
    private javax.swing.JToolBar jToolBar1;
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
        leViewer.disableAutoLayout();
        leViewer.removeView(Viewer.DEFAULT_VIEW_ID);
    }

    private void hideNode(boolean ascendency) {
        if (hideNode == null) {
            return;
        }
        final Map<String, double[]> currentHidden = new HashMap<>();
        hideNode(hideNode, currentHidden, ascendency);

        currentHidden.keySet().forEach((key) -> {
            leGraphe.removeNode(key);
        });

        nodesHidden.putAll(currentHidden);
    }

    private void hideNode(Node n, Map<String, double[]> currentHidden, boolean ascendency) {
        GraphicNode gNode = leViewer.getGraphicGraph().getNode(n.getId());
        double[] point = {gNode.getX(), gNode.getY(), gNode.getZ()};
        currentHidden.put(n.getId(), point);
        for (Edge e : n.getEachEdge()) {
            edgesHidden.add(fillHiddenEdge(e));
        }
        if (ascendency) {

            for (Edge e : n.getEachEnteringEdge()) {
                hideNode(e.getNode0(), currentHidden, ascendency);
            }
        } else {
            for (Edge e : n.getEachLeavingEdge()) {
                hideNode(e.getNode1(), currentHidden, ascendency);
            }
        }
    }

    private HideEdge fillHiddenEdge(Edge e) {
        final HideEdge he = new HideEdge(e.getId());
        he.setNodeInitial(e.getNode0().getId());
        he.setNodeFinal(e.getNode1().getId());
        he.setDirected(e.isDirected());
        he.setClasse(e.getAttribute(UI_CLASS));
        he.setClassOrigine(e.getAttribute(CLASSE_ORIGINE));
        return he;
    }

    private void manageDisplayLabels() {
        leGraphe.getNodeSet().forEach((noeud) -> {
            noeud.addAttribute(UI_STYLE, getDisplayLabelMode());
        });
    }

    /**
     * Remove hide attribute.
     */
    private void displayNodes() {
        for (String key : nodesHidden.keySet()) {
            if (leGraphe.getNode(key) != null) {
                // Node already exists, nothing to do.
                continue;
            }
            final Entity e = getGedcom().getEntity(key);
            if (e != null) {
                double[] point = nodesHidden.get(key);
                if (e instanceof Indi) {
                    createIndiNode((Indi) e, point[0], point[1], point[2]);
                }
                if (e instanceof Fam) {
                    Fam fam = (Fam) e;
                    Node noeudCourant = createFamNode(fam, point[0], point[1], point[2]);
                    Indi husband = fam.getSpouses().size() > 0 ? fam.getSpouse(0) : null;
                    Indi wife = fam.getSpouses().size() > 1 ? fam.getSpouse(1) : null;
                    boolean husbandSosa = calcSosa(husband);
                    boolean wifeSosa = calcSosa(wife);
                    if ((husbandSosa && wifeSosa) || (husbandSosa && wife == null) || (wifeSosa && husband == null)) {
                        noeudCourant.addAttribute(UI_CLASS, MARRIAGE_SOSA);
                        noeudCourant.addAttribute(CLASSE_ORIGINE, MARRIAGE_SOSA);
                    }
                }
            }
        }
        nodesHidden.clear();

        for (HideEdge he : edgesHidden) {
            if (leGraphe.getEdge(he.getId()) != null) {
                // Edge already exists, nothing to do
                continue;
            }
            final Edge e = leGraphe.addEdge(he.getId(), he.getNodeInitial(), he.getNodeFinal(), he.isDirected());
            e.addAttribute(UI_CLASS, he.getClasse());
            e.addAttribute(CLASSE_ORIGINE, he.getClassOrigine());
            e.addAttribute(LAYOUTWEIGHT, graphParam.getEdgeWeight());
        }
        edgesHidden.clear();

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

    public void updateCss() {
        leGraphe.removeAttribute(UISTYLESHEET);
        leGraphe.setAttribute(UISTYLESHEET, graphParam.getCss());
        if (graphParam.isUseGenerationScheme()){
            leGraphe.setAttribute(UISTYLESHEET, graphParam.getGenerationScheme());
            updateSchemeNode();
        }
        manageLabels();
        manageDisplayLabels();
    }
    
    private void updateSchemeNode() {
        for (Node n : leGraphe.getNodeSet()) {
            if (n.hasAttribute(GENERATION)) {
                Integer generation = n.getAttribute(GENERATION);
                
                if (generation >= 0) {
                    double value = generation.doubleValue() / maxGeneration.doubleValue();
                    n.setAttribute("ui.color", 0.5 + value/2);
                } else {
                    double value = generation.doubleValue() / minGeneration.doubleValue();
                    n.setAttribute("ui.color", 0.5 - value/2);
                }
            }
        }
    }

    private void manageLabels() {
        for (Node n : leGraphe.getNodeSet()) {
            if (n.hasAttribute(FAM)) {
                switch (graphParam.getLabelFam()) {
                    case FAM_DATE:
                        manageLabels(n, LABEL_FAM_DATE);
                        break;
                    case FAM_ID_DATE:
                        manageLabels(n, LABEL_FAM_ID);
                        break;
                    case FAM_PLACE:
                        manageLabels(n, LABEL_FAM_LIEU);
                        break;
                    case FAM_SIGNE:
                        manageLabels(n, LABEL_FAM_SIGN);
                        break;
                }
            } else {
                switch (graphParam.getLabelIndi()) {
                    case INDI_NAME:
                        manageLabels(n, LABEL_INDI_NAME);
                        break;
                    case INDI_GIVE_NAME:
                        manageLabels(n, LABEL_INDI_GIVN);
                        break;
                    case INDI_NAME_GENE:
                        manageNameGeneLabels(n);
                        break;
                    case INDI_NAME_ID:
                        manageNameIdLabels(n);
                        break;
                    case INDI_ID_NAME_GENE:
                        manageIdNameGeneLabels(n);
                        break;
                }
            }
        }
    }

    private void manageLabels(Node n, String attribute) {
        if (n.hasAttribute(attribute)) {
            String myLabel = n.getAttribute(attribute);
            n.setAttribute(UI_LABEL, myLabel);
        } else if (n.hasAttribute(UI_LABEL)) {
            n.removeAttribute(UI_LABEL);
        }
    }

    private void manageNameGeneLabels(Node n) {
        String geneLabel = "";
        String nameLabel = "";
        if (n.hasAttribute(GENERATION)) {
            final Integer generation = n.getAttribute(GENERATION);
            geneLabel = generation.toString();
        }
        if (n.hasAttribute(LABEL_INDI_NAME)) {
            nameLabel = n.getAttribute(LABEL_INDI_NAME);
        }
        final StringBuilder sb = new StringBuilder();
        if (!"".equals(nameLabel)) {
            sb.append(nameLabel);
        }
        if (!"".equals(geneLabel)) {
            sb.append(" (").append(geneLabel).append(")");
        }
        if (sb.length() > 0) {
            n.setAttribute(UI_LABEL, sb.toString());
        } else if (n.hasAttribute(UI_LABEL)) {
            n.removeAttribute(UI_LABEL);
        }
    }

    private void manageNameIdLabels(Node n) {
        String nameLabel = "";
        if (n.hasAttribute(LABEL_INDI_NAME)) {
            nameLabel = n.getAttribute(LABEL_INDI_NAME);
        }
        final StringBuilder sb = new StringBuilder();
        if (!"".equals(nameLabel)) {
            sb.append(nameLabel);
        }

        sb.append(" (").append(n.getId()).append(")");
        n.setAttribute(UI_LABEL, sb.toString());
    }

    private void manageIdNameGeneLabels(Node n) {
        String geneLabel = "";
        String nameLabel = "";
        if (n.hasAttribute(GENERATION)) {
            final Integer generation = n.getAttribute(GENERATION);
            geneLabel = generation.toString();
        }
        if (n.hasAttribute(LABEL_INDI_NAME)) {
            nameLabel = n.getAttribute(LABEL_INDI_NAME);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(n.getId()).append(' ');
        if (!"".equals(nameLabel)) {
            sb.append(nameLabel);
        }
        if (!"".equals(geneLabel)) {
            sb.append(" (").append(geneLabel).append(")");
        }
        n.setAttribute(UI_LABEL, sb.toString());

    }

    public void updateWeight() {
        for (Node n : leGraphe.getEachNode()) {
            if (n.hasAttribute(FAM)) {
                n.setAttribute(LAYOUTWEIGHT, graphParam.getMariageNodeWeight());
            } else {
                n.setAttribute(LAYOUTWEIGHT, graphParam.getIndiNodeWeight());
            }
        }

        for (Edge e : leGraphe.getEachEdge()) {
            e.setAttribute(LAYOUTWEIGHT, graphParam.getEdgeWeight());
        }
    }

    private void manageAsso() {
        if (graphParam.isShowAsso()) {
            displayAsso();
        } else {
            removeAsso();
        }
    }

    private void displayAsso() {
        for (Indi indi : getGedcom().getIndis()) {
            final Node courant = leGraphe.getNode(indi.getId());
            if (courant == null) {
                continue;
            }
            for (PropertyAssociation asso : indi.getProperties(PropertyAssociation.class)) {
                final Entity ent = asso.getTargetEntity();
                if (ent == null || ent.getId() == null) {
                    continue;
                }
                final Node autre = leGraphe.getNode(ent.getId());
                if (autre != null) {
                    Edge e = leGraphe.getEdge("ASSO:" + indi.getId() + " - " + ent.getId());
                    if (e == null) {
                        e = leGraphe.addEdge("ASSO:" + indi.getId() + " - " + ent.getId(), courant, autre, false);
                        e.addAttribute(UI_CLASS, ASSO);
                        e.addAttribute(CLASSE_ORIGINE, ASSO);
                    }

                }
            }
        }
    }

    private void removeAsso() {
        for (Indi indi : getGedcom().getIndis()) {
            final Node courant = leGraphe.getNode(indi.getId());
            if (courant == null) {
                continue;
            }
            for (PropertyAssociation asso : indi.getProperties(PropertyAssociation.class)) {
                final Entity ent = asso.getTargetEntity();
                final Node autre = leGraphe.getNode(ent.getId());
                if (autre != null) {
                    final Edge e = leGraphe.getEdge("ASSO:" + indi.getId() + " - " + ent.getId());
                    if (e != null) {
                        leGraphe.removeEdge(e);
                    }
                }
            }
        }
    }

    private void loadSettings() {
        if (registry == null) {
            registry = getGedcom().getRegistry();
        }
        graphParam.loadSettings(registry);
    }

    public void saveSettings() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            if (registry == null) {
                return;
            }
            graphParam.saveSettings(registry);
        });
    }

    public void setGraphParam(GraphParameter graphParam) {
        this.graphParam = graphParam;
    }

    public GraphParameter getGraphParam() {
        return graphParam;
    }

    private void showPath(String destination) {
        if (pathNode == null) {
            // Nothing to display.
            return;
        }

        final AncestrisAStar algo = new AncestrisAStar(leGraphe);
        algo.setCosts(new AStar.DistanceCosts());
        algo.compute(pathNode, destination);
        System.out.println(algo.getShortestPath());
        if (algo.noPathFound()) {
            final String message = NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.NoPath");
            DialogManager.create("Chemin le plus court", message)
                    .setMessageType(DialogManager.INFORMATION_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();
        } else {
            final Path chemin = algo.getShortestPath();
            final List<DisplayPathElement> pathList = new ArrayList<>();
            int compteur = 0;
            for (Node n : chemin.getNodeSet()) {
                n.addAttribute(UI_CLASS, STICKED);
                Entity e = getGedcom().getEntity(n.getId());
                final DisplayPathElement dpe = new DisplayPathElement(e, compteur);
                pathList.add(dpe);
                compteur++;
            }
            for (Edge e : chemin.getEdgeSet()) {
                e.addAttribute(UI_CLASS, STICKED);
            }

            final ResultsList results = new ResultsList();
            results.add(pathList);
            final ShortPathPanel spp = new ShortPathPanel(results, getContext());

            DialogManager dm = DialogManager.create(NbBundle.getMessage(GraphTopComponent.class, "ShortPathTitle"), spp).setOptionType(DialogManager.OK_ONLY_OPTION).setDialogId(ShortPathPanel.class);
            dm.show();
            dm.cancel();

        }

        pathNode = null;
    }

}
