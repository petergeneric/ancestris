package genj.tree;

import ancestris.modules.views.tree.style.Style;
import ancestris.modules.views.tree.style.TreeStyleManager;
import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import static genj.tree.TreeView.REGISTRY;
import static genj.tree.TreeView.TITLE;
import genj.util.Resources;
import genj.util.swing.ColorsWidget;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
/**
 *
 * @author frederic
 */
public class TreeViewSettings extends javax.swing.JPanel {

    private final static Resources RESOURCES = Resources.get(TreeViewSettings.class);
    private final TreeView view;
    private final Commit commit;
    private final Styles styles;
    private final Bookmarks bookmarks;

    private final ColorsWidget colors;

    private boolean busy = true;
    private static final double MAXSIZE = 30.0;
    private static final double MAXPAD = 5.0;
    private static final int MAXTHICKNESS = 9;

    /**
     * Creates new form TreeViewSettings
     *
     * Note on styles : - A change of global style should change the style
     * elements (and update tree) - But any changes in style elements should
     * only change Perso style (and update tree)
     *
     */
    public TreeViewSettings(final TreeView view) {
        TimingUtility.getInstance().reset();

        this.view = view;
        commit = new Commit(view);

        // Set Styles
        styles = new Styles(view.getStyleManager().getStyles());
        styles.addListDataListener(commit);

        // Set models
        bookmarks = new Bookmarks(view.getModel().getBookmarks());
        bookmarks.addListDataListener(commit);

        // Set panel
        initComponents();

        // Set selections
        busy = true;

        // Main
        cbTreeAutoScroll.setSelected(TreeView.isAutoScroll());
        jcAction.setSelectedItem(TreeView.getOnAction());
        spingen.setValue((Integer) view.getModel().getMaxGenerations());
        cbShowPopup.setSelected(TreeView.showPopup());

        // Style
        stylesList.setCellRenderer(new ListEntryCellRenderer());
        stylesList.setSelectedValue(view.getStyle(), true);

        fontChooser.setCallBack(() -> {
            busy = true;
            fontChooser.setSelectedFont(view.getStyle().font);
            busy = false;
        });

        colors = new ColorsWidget();
        for (String key : TreeStyleManager.ORDERCOLORS) {
            colors.addColor(key, RESOURCES.getString("color." + key), view.getColors().get(key));
        }
        colorsPanel.add(colors);

        bendCheckBox.setSelected(view.getModel().isBendArcs());
        marrsymbolsCheckBox.setSelected(view.getModel().isMarrSymbols());
        antialiasingCheckBox.setSelected(view.isAntialising());
        roundedRectanglesCheckBox.setSelected(view.getModel().isRoundedRectangle());

        TreeMetrics m = view.getModel().getMetrics();
        initSpinner(wIndiSpinner, 0.4, m.wIndis * 0.1D, MAXSIZE, 0.1D);
        initSpinner(hIndiSpinner, 0.4, m.hIndis * 0.1D, MAXSIZE, 0.1D);
        initSpinner(wFamSpinner, 0.4, m.wFams * 0.1D, MAXSIZE, 0.1D);
        initSpinner(hFamSpinner, 0.4, m.hFams * 0.1D, MAXSIZE, 0.1D);
        initSpinner(paddingSpinner, 0.4, m.pad * 0.1D, MAXPAD, 0.1D);
        bIndiSpinner.setModel(new SpinnerNumberModel(m.indisThick, 1, MAXTHICKNESS, 1));
        bFamSpinner.setModel(new SpinnerNumberModel(m.famsThick, 1, MAXTHICKNESS, 1));

        // Listeners
        bList.getModel().addListDataListener(commit);
        colors.addChangeListener(commit);
        fontChooser.addChangeListener(commit);

        busy = false;
    }

    private void initSpinner(JSpinner spinner, double min, double val, double max, double inc) {
        val = Math.min(max, Math.max(val, min));
        spinner.setModel(new SpinnerNumberModel(val, min, max, inc));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, inc < 1 ? "##0.0" : "0");
        spinner.setEditor(editor);
        spinner.addChangeListener(editor);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        mainPanel = new javax.swing.JPanel();
        navigationLabel = new javax.swing.JLabel();
        cbTreeAutoScroll = new javax.swing.JCheckBox();
        douclickLabel = new javax.swing.JLabel();
        jcAction = new javax.swing.JComboBox();
        maxGenLabel = new javax.swing.JLabel();
        spingen = new javax.swing.JSpinner(new SpinnerNumberModel(view.getModel().getMaxGenerations(), 1, 100, 1));
        cbShowPopup = new javax.swing.JCheckBox();
        styleLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        stylesList = new javax.swing.JList();
        bookmarksPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        bList = new javax.swing.JList();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        delButton = new javax.swing.JButton();
        separatorButton = new javax.swing.JButton();
        colorsPanel = new javax.swing.JPanel();
        tuningPanel = new javax.swing.JPanel();
        fontLabel = new javax.swing.JLabel();
        fontChooser = new genj.util.swing.FontChooser();
        displayLabel = new javax.swing.JLabel();
        bendCheckBox = new javax.swing.JCheckBox();
        marrsymbolsCheckBox = new javax.swing.JCheckBox();
        antialiasingCheckBox = new javax.swing.JCheckBox();
        boxesLabel = new javax.swing.JLabel();
        roundedRectanglesCheckBox = new javax.swing.JCheckBox();
        paddingLabel = new javax.swing.JLabel();
        paddingSpinner = new javax.swing.JSpinner();
        indiLabel = new javax.swing.JLabel();
        famLabel = new javax.swing.JLabel();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        borderLabel = new javax.swing.JLabel();
        wIndiSpinner = new javax.swing.JSpinner();
        hIndiSpinner = new javax.swing.JSpinner();
        bIndiSpinner = new javax.swing.JSpinner();
        wFamSpinner = new javax.swing.JSpinner();
        hFamSpinner = new javax.swing.JSpinner();
        bFamSpinner = new javax.swing.JSpinner();

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 400));

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(350, 400));

        navigationLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(navigationLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.navigationLabel.text")); // NOI18N

        cbTreeAutoScroll.setSelected(TreeView.isAutoScroll());
        org.openide.awt.Mnemonics.setLocalizedText(cbTreeAutoScroll, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.cbTreeAutoScroll.text")); // NOI18N
        cbTreeAutoScroll.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.cbTreeAutoScroll.toolTipText")); // NOI18N
        cbTreeAutoScroll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTreeAutoScrollActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(douclickLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.douclickLabel.text")); // NOI18N
        douclickLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.douclickLabel.toolTipText")); // NOI18N

        jcAction.setModel(new javax.swing.DefaultComboBoxModel(genj.tree.TreeViewSettings.OnAction.values()));
        jcAction.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.jcAction.toolTipText")); // NOI18N
        jcAction.setSelectedItem(TreeView.getOnAction());
        jcAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcActionActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(maxGenLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.maxGenLabel.text")); // NOI18N
        maxGenLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.maxGenLabel.toolTipText")); // NOI18N

        spingen.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.spingen.toolTipText")); // NOI18N
        spingen.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spingenStateChanged(evt);
            }
        });

        cbShowPopup.setSelected(TreeView.showPopup());
        org.openide.awt.Mnemonics.setLocalizedText(cbShowPopup, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.cbShowPopup.text")); // NOI18N
        cbShowPopup.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.cbShowPopup.toolTipText")); // NOI18N
        cbShowPopup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbShowPopupActionPerformed(evt);
            }
        });

        styleLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(styleLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.styleLabel.text")); // NOI18N

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        stylesList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        stylesList.setModel(styles);
        stylesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        stylesList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        stylesList.setOpaque(false);
        stylesList.setVisibleRowCount(-1);
        stylesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                stylesListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(stylesList);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(navigationLabel)
                    .addComponent(styleLabel)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbTreeAutoScroll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbShowPopup)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(douclickLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(maxGenLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spingen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2))))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(navigationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbTreeAutoScroll)
                .addGap(7, 7, 7)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(douclickLabel))
                .addGap(2, 2, 2)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxGenLabel)
                    .addComponent(spingen, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(cbShowPopup)
                .addGap(18, 18, 18)
                .addComponent(styleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.mainPanel.TabConstraints.tabTitle"), mainPanel); // NOI18N

        bList.setModel(bookmarks);
        bList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        bList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                bListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(bList);

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.upButton.text")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.downButton.text")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(delButton, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.delButton.text")); // NOI18N
        delButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(separatorButton, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.separatorButton.text")); // NOI18N
        separatorButton.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.separatorButton.toolTipText")); // NOI18N
        separatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separatorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bookmarksPanelLayout = new javax.swing.GroupLayout(bookmarksPanel);
        bookmarksPanel.setLayout(bookmarksPanelLayout);
        bookmarksPanelLayout.setHorizontalGroup(
            bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
            .addGroup(bookmarksPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(upButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(separatorButton)
                .addContainerGap())
        );
        bookmarksPanelLayout.setVerticalGroup(
            bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bookmarksPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(upButton)
                    .addComponent(downButton)
                    .addComponent(delButton)
                    .addComponent(separatorButton))
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.bookmarksPanel.TabConstraints.tabTitle"), bookmarksPanel); // NOI18N

        colorsPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.colorsPanel.TabConstraints.tabTitle"), colorsPanel); // NOI18N

        tuningPanel.setPreferredSize(new java.awt.Dimension(400, 300));

        fontLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(fontLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.fontLabel.text")); // NOI18N

        fontChooser.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fontChooserStateChanged(evt);
            }
        });

        displayLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(displayLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.displayLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bendCheckBox, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.bendCheckBox.text")); // NOI18N
        bendCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.bendCheckBox.toolTipText")); // NOI18N
        bendCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bendCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(marrsymbolsCheckBox, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.marrsymbolsCheckBox.text")); // NOI18N
        marrsymbolsCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.marrsymbolsCheckBox.toolTipText")); // NOI18N
        marrsymbolsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                marrsymbolsCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(antialiasingCheckBox, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.antialiasingCheckBox.text")); // NOI18N
        antialiasingCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.antialiasingCheckBox.toolTipText")); // NOI18N
        antialiasingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                antialiasingCheckBoxActionPerformed(evt);
            }
        });

        boxesLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(boxesLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.boxesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(roundedRectanglesCheckBox, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.roundedRectanglesCheckBox.text")); // NOI18N
        roundedRectanglesCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.roundedRectanglesCheckBox.toolTipText")); // NOI18N
        roundedRectanglesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roundedRectanglesCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(paddingLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.paddingLabel.text")); // NOI18N

        paddingSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.paddingSpinner.toolTipText")); // NOI18N
        paddingSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                paddingSpinnerStateChanged(evt);
            }
        });

        indiLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(indiLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.indiLabel.text")); // NOI18N

        famLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(famLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.famLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(widthLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.widthLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(heightLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.heightLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(borderLabel, org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.borderLabel.text")); // NOI18N

        wIndiSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.wIndiSpinner.toolTipText")); // NOI18N
        wIndiSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                wIndiSpinnerStateChanged(evt);
            }
        });

        hIndiSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.hIndiSpinner.toolTipText")); // NOI18N
        hIndiSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                hIndiSpinnerStateChanged(evt);
            }
        });

        bIndiSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.bIndiSpinner.toolTipText")); // NOI18N
        bIndiSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bIndiSpinnerStateChanged(evt);
            }
        });

        wFamSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.wFamSpinner.toolTipText")); // NOI18N
        wFamSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                wFamSpinnerStateChanged(evt);
            }
        });

        hFamSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.hFamSpinner.toolTipText")); // NOI18N
        hFamSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                hFamSpinnerStateChanged(evt);
            }
        });

        bFamSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.bFamSpinner.toolTipText")); // NOI18N
        bFamSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bFamSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout tuningPanelLayout = new javax.swing.GroupLayout(tuningPanel);
        tuningPanel.setLayout(tuningPanelLayout);
        tuningPanelLayout.setHorizontalGroup(
            tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tuningPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tuningPanelLayout.createSequentialGroup()
                        .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fontChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(tuningPanelLayout.createSequentialGroup()
                                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fontLabel)
                                    .addComponent(displayLabel)
                                    .addGroup(tuningPanelLayout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(marrsymbolsCheckBox)
                                            .addComponent(bendCheckBox)
                                            .addComponent(antialiasingCheckBox))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(tuningPanelLayout.createSequentialGroup()
                        .addComponent(boxesLabel)
                        .addGap(138, 138, 138))
                    .addGroup(tuningPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(tuningPanelLayout.createSequentialGroup()
                                .addComponent(roundedRectanglesCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(paddingLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(paddingSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(tuningPanelLayout.createSequentialGroup()
                                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(widthLabel)
                                    .addComponent(heightLabel)
                                    .addComponent(borderLabel))
                                .addGap(48, 48, 48)
                                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(indiLabel)
                                    .addComponent(wIndiSpinner)
                                    .addComponent(hIndiSpinner)
                                    .addComponent(bIndiSpinner))
                                .addGap(18, 18, 18)
                                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(famLabel)
                                    .addComponent(wFamSpinner)
                                    .addComponent(hFamSpinner)
                                    .addComponent(bFamSpinner))))
                        .addGap(0, 35, Short.MAX_VALUE))))
        );
        tuningPanelLayout.setVerticalGroup(
            tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tuningPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fontLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(displayLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bendCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(marrsymbolsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(antialiasingCheckBox)
                .addGap(18, 18, 18)
                .addComponent(boxesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(roundedRectanglesCheckBox)
                    .addComponent(paddingLabel)
                    .addComponent(paddingSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(indiLabel)
                    .addComponent(famLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthLabel)
                    .addComponent(wIndiSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wFamSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heightLabel)
                    .addComponent(hIndiSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hFamSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tuningPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(borderLabel)
                    .addComponent(bIndiSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFamSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(TreeViewSettings.class, "TreeViewSettings.tuningPanel.TabConstraints.tabTitle"), tuningPanel); // NOI18N

        jScrollPane1.setViewportView(jTabbedPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbTreeAutoScrollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTreeAutoScrollActionPerformed
        commit.actionPerformed(null);
    }//GEN-LAST:event_cbTreeAutoScrollActionPerformed

    private void jcActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcActionActionPerformed
        commit.actionPerformed(null);
    }//GEN-LAST:event_jcActionActionPerformed

    private void spingenStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spingenStateChanged
        commit.stateChanged(null);
    }//GEN-LAST:event_spingenStateChanged

    private void cbShowPopupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbShowPopupActionPerformed
        commit.actionPerformed(null);
    }//GEN-LAST:event_cbShowPopupActionPerformed

    private void bListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_bListValueChanged
        int i = bList.getSelectedIndex(),
                n = bookmarks.getSize();
        upButton.setEnabled(i > 0);
        downButton.setEnabled(i >= 0 && i < n - 1);
        delButton.setEnabled(i >= 0);
    }//GEN-LAST:event_bListValueChanged

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int i = bList.getSelectedIndex();
        bookmarks.swap(i, i - 1);
        bList.setSelectedIndex(i - 1);
        // save bookmarks
        saveBookmarks();
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int i = bList.getSelectedIndex();
        bookmarks.swap(i, i + 1);
        bList.setSelectedIndex(i + 1);
        // save bookmarks
        saveBookmarks();
    }//GEN-LAST:event_downButtonActionPerformed

    private void delButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delButtonActionPerformed
        bookmarks.delete(bList.getSelectedIndex());
        // save bookmarks
        saveBookmarks();
    }//GEN-LAST:event_delButtonActionPerformed

    private void bendCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bendCheckBoxActionPerformed
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_bendCheckBoxActionPerformed

    private void marrsymbolsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_marrsymbolsCheckBoxActionPerformed
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_marrsymbolsCheckBoxActionPerformed

    private void antialiasingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_antialiasingCheckBoxActionPerformed
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_antialiasingCheckBoxActionPerformed

    private void roundedRectanglesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roundedRectanglesCheckBoxActionPerformed
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_roundedRectanglesCheckBoxActionPerformed

    private void paddingSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_paddingSpinnerStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_paddingSpinnerStateChanged

    private void wIndiSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_wIndiSpinnerStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_wIndiSpinnerStateChanged

    private void wFamSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_wFamSpinnerStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_wFamSpinnerStateChanged

    private void hIndiSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_hIndiSpinnerStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_hIndiSpinnerStateChanged

    private void hFamSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_hFamSpinnerStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_hFamSpinnerStateChanged

    private void bIndiSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bIndiSpinnerStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_bIndiSpinnerStateChanged

    private void bFamSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bFamSpinnerStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_bFamSpinnerStateChanged

    private void stylesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_stylesListValueChanged
        if (!busy) {
            commit.actionPerformed(new ActionEvent(evt.getSource(), 0, "style"));
        }
    }//GEN-LAST:event_stylesListValueChanged

    private void fontChooserStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fontChooserStateChanged
        commit.actionPerformed(new ActionEvent(evt.getSource(), 1, "settings"));
    }//GEN-LAST:event_fontChooserStateChanged

    private void separatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separatorButtonActionPerformed
        // Ask for name of bookmark
        String text = RESOURCES.getString("bookmark.name");
        // FL : 10/2019. 
        // We need imput field to be long. DialogID does not work for InputLine
        // => trick : make string longer that 81 characteres to force dialog to display 2 lines
        text += "                                                                                 ".substring(text.length());
        final String value = DialogManager.create(TITLE, text, "").show();
        if (value == null) {
            return;
        }

        // create it
        Bookmark newSeparator = new BookmarkSeparator(value);
        view.getModel().addBookmark(newSeparator);
        bookmarks.add(newSeparator);

        // save bookmarks
        saveBookmarks();
        
    }//GEN-LAST:event_separatorButtonActionPerformed

    private void setStyle(Style style) {
        busy = true;

        colors.removeAllColors();
        for (String key : TreeStyleManager.ORDERCOLORS) {
            colors.addColor(key, RESOURCES.getString("color." + key), style.colors.get(key));
        }
        fontChooser.setSelectedFont(style.font);
        bendCheckBox.setSelected(style.bend);
        marrsymbolsCheckBox.setSelected(style.marr);
        antialiasingCheckBox.setSelected(style.antialiasing);
        roundedRectanglesCheckBox.setSelected(style.roundrect);
        wIndiSpinner.setValue((double) style.tm.wIndis * 0.1D);
        hIndiSpinner.setValue((double) style.tm.hIndis * 0.1D);
        wFamSpinner.setValue((double) style.tm.wFams * 0.1D);
        hFamSpinner.setValue((double) style.tm.hFams * 0.1D);
        paddingSpinner.setValue((double) style.tm.pad * 0.1D);
        bIndiSpinner.setValue((int) style.tm.indisThick);
        bFamSpinner.setValue((int) style.tm.famsThick);

        busy = false;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox antialiasingCheckBox;
    private javax.swing.JSpinner bFamSpinner;
    private javax.swing.JSpinner bIndiSpinner;
    private javax.swing.JList bList;
    private javax.swing.JCheckBox bendCheckBox;
    private javax.swing.JPanel bookmarksPanel;
    private javax.swing.JLabel borderLabel;
    private javax.swing.JLabel boxesLabel;
    private javax.swing.JCheckBox cbShowPopup;
    private javax.swing.JCheckBox cbTreeAutoScroll;
    private javax.swing.JPanel colorsPanel;
    private javax.swing.JButton delButton;
    private javax.swing.JLabel displayLabel;
    private javax.swing.JLabel douclickLabel;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel famLabel;
    private genj.util.swing.FontChooser fontChooser;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JSpinner hFamSpinner;
    private javax.swing.JSpinner hIndiSpinner;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JLabel indiLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox jcAction;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JCheckBox marrsymbolsCheckBox;
    private javax.swing.JLabel maxGenLabel;
    private javax.swing.JLabel navigationLabel;
    private javax.swing.JLabel paddingLabel;
    private javax.swing.JSpinner paddingSpinner;
    private javax.swing.JCheckBox roundedRectanglesCheckBox;
    private javax.swing.JButton separatorButton;
    private javax.swing.JSpinner spingen;
    private javax.swing.JLabel styleLabel;
    private javax.swing.JList stylesList;
    private javax.swing.JPanel tuningPanel;
    private javax.swing.JButton upButton;
    private javax.swing.JSpinner wFamSpinner;
    private javax.swing.JSpinner wIndiSpinner;
    private javax.swing.JLabel widthLabel;
    // End of variables declaration//GEN-END:variables

    private void saveBookmarks() {
        Entity root = view.getModel().getRoot();
        if (root != null) {
            REGISTRY.put(root.getGedcom().getName() + ".bookmarks", view.getModel().getBookmarks());
        }
    }

    private static class ListEntryCellRenderer extends JLabel implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Style style = (Style) value;

            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.TOP);

            setPreferredSize(new Dimension(120, 105));
            setIcon(style.icon);
            setText(style.name);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createRaisedBevelBorder()));
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                //setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createLineBorder(Color.gray, 1, true)));
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);

            return this;
        }
    }

    private class Styles extends AbstractListModel {

        private final ArrayList<Style> list;

        Styles(Collection<Style> list) {
            this.list = new ArrayList<>(list);
        }

        @Override
        public Object getElementAt(int index) {
            return list.get(index);
        }

        @Override
        public int getSize() {
            return list.size();
        }

        public List<Style> get() {
            return Collections.unmodifiableList(list);
        }
    }

    private class Bookmarks extends AbstractListModel {

        private final ArrayList<Bookmark> list;

        Bookmarks(List<Bookmark> list) {
            this.list = new ArrayList<>(list);
        }

        @Override
        public Object getElementAt(int index) {
            return list.get(index);
        }

        @Override
        public int getSize() {
            return list.size();
        }

        public void swap(int i, int j) {
            if (i == j) {
                return;
            }
            Bookmark b = list.get(i);
            list.set(i, list.get(j));
            list.set(j, b);
            fireContentsChanged(this, Math.min(i, j), Math.max(i, j));
        }

        public void delete(int i) {
            list.remove(i);
            fireIntervalRemoved(this, i, i);
        }

        public void add(Bookmark b) {
            list.add(b);
            fireIntervalAdded(this, 0, 1);
        }

        public List<Bookmark> get() {
            return Collections.unmodifiableList(list);
        }
    }

    public class Commit implements ChangeListener, ActionListener, ListDataListener {

        private final TreeView view;

        private Commit(TreeView view) {
            this.view = view;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e != null && e.getSource() instanceof ColorsWidget) {
                actionPerformed(new ActionEvent(e.getSource(), 1, "settings"));
            } else {
                actionPerformed(null);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (busy) {
                return;
            }

            if (e == null) {
                // Main
                TreeView.setAutoScroll(cbTreeAutoScroll.isSelected());
                TreeView.setOnAction((OnAction) jcAction.getSelectedItem());
                view.getModel().setMaxGenerations((Integer) spingen.getValue());
                TreeView.setShowPopup(cbShowPopup.isSelected());

                // Bookmarks
                view.getModel().setBookmarks(bookmarks.get());

                // done
                return;
            }

            if (e.getActionCommand().equals("style")) {
                // If style changes from non perso to perso, save settings before changing style
                Style currentStyle = view.getStyle();
                Style newStyle = (Style) stylesList.getSelectedValue();
                if (newStyle != currentStyle && currentStyle.key.equals(TreeStyleManager.PERSOSTYLE)) { // save settings of perso style while leaving perso style
                    view.saveStyle();
                }
                // Propagate style variable to model and view => it will update tree
                view.setStyle(newStyle);
                // Set settings to new style
                setStyle(newStyle);

                // done
                return;
            }

            if (e.getActionCommand().equals("settings")) {

                // First warn user if current style is not perso style and changes could overwrite it. Ask for confirmation.
                if (!view.confirmStyleOverwrite()) {
                    return;
                }

                // Switch style to personal style before modifying the settings
                Style persoStyle = view.getStyleManager().getPersoStyle();
                persoStyle.blueprintIndi = view.getStyle().blueprintIndi; // force blueprint as well
                persoStyle.blueprintFam = view.getStyle().blueprintFam;
                view.setStyle(persoStyle);
                busy = true;
                stylesList.setSelectedIndex(0);
                busy = false;

                // Colors
                view.setColors(colors.getColors());

                // Tuning
                view.setContentFont(fontChooser.getSelectedFont());

                view.getModel().setBendArcs(bendCheckBox.isSelected());
                view.getModel().setMarrSymbols(marrsymbolsCheckBox.isSelected());
                view.setAntialiasing(antialiasingCheckBox.isSelected());

                view.getModel().setRoundedRectangle(roundedRectanglesCheckBox.isSelected());
                view.getModel().setMetrics(new TreeMetrics(
                        (int) (((Double) wIndiSpinner.getModel().getValue()) * 10),
                        (int) (((Double) hIndiSpinner.getModel().getValue()) * 10),
                        (int) (((Double) wFamSpinner.getModel().getValue()) * 10),
                        (int) (((Double) hFamSpinner.getModel().getValue()) * 10),
                        (int) (((Double) paddingSpinner.getModel().getValue()) * 10),
                        (Integer) bIndiSpinner.getModel().getValue(),
                        (Integer) bFamSpinner.getModel().getValue()
                ));

                // done
            }
            // done
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            actionPerformed(null);
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            actionPerformed(null);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            actionPerformed(null);
        }
    }

    /**
     * Enum for operation done on an action (ie double clic). Possible values
     * are:
     * <li/>NONE: Nothing special. In fact the same as selection
     * <li/>CENTER: Centers tree view on this entity if possible (entity
     * displayed in tree)
     * <li/>SETROOT: this entity becomes the new root for this tree view
     */
    public enum OnAction {

        NONE("tv.action.none"),
        CENTER("tv.action.center"),
        SETROOT("tv.action.setroot");
        private final String description;

        private OnAction(String desc) {
            description = RESOURCES.getString(desc);
        }

        @Override
        public String toString() {
            return description + "     ";
        }
    };

}
