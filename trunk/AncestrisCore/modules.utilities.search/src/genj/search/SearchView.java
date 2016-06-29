/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.search;

import ancestris.awt.FilteredMouseAdapter;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import ancestris.swing.ToolBar;
import ancestris.util.swing.DialogManager;
import genj.gedcom.PropertyDate;
import genj.view.Images;
import genj.view.View;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractListModel;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import spin.Spin;

/**
 *
 * @author frederic
 */
public class SearchView extends View {

    
    /** default values */
    private int max_hits;
    private boolean case_sensitive;
    
    private final static String[] DEFAULT_VALUES = {
        "L(a|e)pe(i|y)re", "Paris.+France", "^(M|F)"
    },
            DEFAULT_TAGS = {
        "NAME", "BIRT", "BIRT, PLAC", "OCCU", "NOTE", "BIRT, NOTE", "RESI", "PLAC"
    },
            DEFAULT_STR = {
        
    };
    
    private final static ImageIcon IMG_START = new ImageIcon(SearchView.class, "images/Start"),
            IMG_STOP = new ImageIcon(SearchView.class, "images/Stop"),
            IMG_CLEAN = new ImageIcon(SearchView.class, "images/Clean"),
            IMG_CLEAR = new ImageIcon(SearchView.class, "images/ClearHistory"),
            IMG_SETTINGS = Images.imgSettings;
    
    /** how many old values we remember */
    private final static int MAX_OLD = 16;
    
    /** resources */
    /* package */ final static Resources RESOURCES = Resources.get(SearchView.class);

    /** registry */
    private final static Registry REGISTRY = Registry.get(SearchView.class);
    
    
    
    /** current context */
    private Context context = new Context();
    
    /** shown results */
    private Results results1 = new Results();
    private Results results2 = new Results();
    private ResultWidget listResults1 = new ResultWidget(results1);
    private ResultWidget listResults2 = new ResultWidget(results2);
    
    /** criterias */
    private ChoiceWidget choiceLastname, choiceFirstname, choicePlace;
    private ChoiceWidget choiceTag, choiceValue;
    private JCheckBox checkRegExp;
    private JLabel labelCount2;
    
    /** history */
    private LinkedList<String> oldLastnames, oldFirstnames, oldPlaces;
    private LinkedList<String> oldTags, oldValues;
    
    /** worker */
    private AbstractAncestrisAction actionStart = new ActionStart(), 
            actionStop = new ActionStop(), 
            actionClean = new ActionClean(), 
            actionClearHistory = new ActionClearHistory(),
            actionSettings = new ActionSettings();
    private WorkerMulti worker1;
    private WorkerTag worker2;

    
    
    /**
     * Constructor
     */
    public SearchView() {
        
        // prepare an action listener connecting to click
        ActionListener aclick = new ActionListener() {

            /** button */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (actionStop.isEnabled()) {
                    stop();
                }
                if (actionStart.isEnabled()) {
                    start();
                }
            }
        };

        // Settings
        SettingsPanel settingsPanel = new SettingsPanel(REGISTRY);
        max_hits = settingsPanel.getMaxHits();
        case_sensitive = settingsPanel.getCaseSensitive();
        
        // prepare search criteria for MultiCriteria panel
        oldLastnames = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.lastnames", DEFAULT_STR)));
        choiceLastname = new ChoiceWidget(oldLastnames);
        choiceLastname.addActionListener(aclick);
        oldFirstnames = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.firstnames", DEFAULT_STR)));
        choiceFirstname = new ChoiceWidget(oldFirstnames);
        choiceFirstname.addActionListener(aclick);
        oldPlaces = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.places", DEFAULT_STR)));
        choicePlace = new ChoiceWidget(oldPlaces);
        choicePlace.addActionListener(aclick);
        
        initComponents();
        
        birthDateBean.addActionListener(aclick);
        deathDateBean.addActionListener(aclick);
        birthDateBean.setVisibleCalendar(false);
        deathDateBean.setVisibleCalendar(false);
        
        // setup worker
        worker1 = new WorkerMulti((WorkerListener) Spin.over(new WorkerListener() {

            @Override
            public void more(List<Hit> hits) {
                results1.add(hits);
                labelCount1.setText("" + results1.getSize());
            }

            @Override
            public void started() {
                // clear current results
                results1.clear();
                labelCount1.setText("");
                actionStart.setEnabled(false);
                actionStop.setEnabled(true);
            }

            @Override
            public void stopped() {
                actionStop.setEnabled(false);
                actionStart.setEnabled(context.getGedcom() != null);
            }
        }));

        worker2 = new WorkerTag((WorkerListener) Spin.over(new WorkerListener() {

            @Override
            public void more(List<Hit> hits) {
                results2.add(hits);
                labelCount2.setText("" + results2.getSize());
            }

            @Override
            public void started() {
                // clear current results
                results2.clear();
                labelCount2.setText("");
                actionStart.setEnabled(false);
                actionStop.setEnabled(true);
            }

            @Override
            public void stopped() {
                actionStop.setEnabled(false);
                actionStart.setEnabled(context.getGedcom() != null);
            }
        }));


        // prepare search criteria for tag panel
        oldTags = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.tags", DEFAULT_TAGS)));
        oldValues = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.values", DEFAULT_VALUES)));
        boolean useRegEx = REGISTRY.get("regexp", false);

        JLabel labelValue = new JLabel(RESOURCES.getString("label.value"));
        checkRegExp = new JCheckBox(RESOURCES.getString("label.regexp"), useRegEx);

        choiceValue = new ChoiceWidget(oldValues);
        choiceValue.addActionListener(aclick);

        PopupWidget popupPatterns = new PopupWidget("...", null);
        popupPatterns.addItems(createPatternActions());
        popupPatterns.setMargin(new Insets(0, 0, 0, 0));

        JLabel labelTag = new JLabel(RESOURCES.getString("label.tag"));
        choiceTag = new ChoiceWidget(oldTags);
        choiceTag.addActionListener(aclick);

        PopupWidget popupTags = new PopupWidget("...", null);
        popupTags.addItems(createTagActions());
        popupTags.setMargin(new Insets(0, 0, 0, 0));

        labelCount2 = new JLabel();

        JPanel paneCriteria = new JPanel();
        try {
            paneCriteria.setFocusCycleRoot(true);
        } catch (Throwable t) {
        }

        GridBagHelper gh = new GridBagHelper(paneCriteria);
        // .. line 0
        gh.add(labelValue, 0, 0, 2, 1, 0, new Insets(0, 0, 0, 8));
        gh.add(checkRegExp, 2, 0, 1, 1, GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL);
        gh.add(labelCount2, 3, 0, 1, 1);
        // .. line 1
        gh.add(popupPatterns, 0, 1, 1, 1);
        gh.add(choiceValue, 1, 1, 3, 1, GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL, new Insets(3, 3, 3, 3));
        // .. line 2
        gh.add(labelTag, 0, 2, 4, 1, GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL);
        // .. line 3
        gh.add(popupTags, 0, 3, 1, 1);
        gh.add(choiceTag, 1, 3, 3, 1, GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL, new Insets(0, 3, 3, 3));

        
        
        // prepare layout1
        birthDateBean.setPropertyImpl(null);
        deathDateBean.setPropertyImpl(null);
        birthDateBean.setFormat(PropertyDate.BETWEEN_AND);
        deathDateBean.setFormat(PropertyDate.BETWEEN_AND);
        result1Panel.setLayout(new BorderLayout());
        result1Panel.add(BorderLayout.CENTER, new JScrollPane(listResults1));
        labelCount1.setText("");
        

        // prepare layout2
        tabTag.setLayout(new BorderLayout());
        tabTag.add(BorderLayout.NORTH, paneCriteria);
        tabTag.add(BorderLayout.CENTER, new JScrollPane(listResults2));
        //choiceValue.requestFocusInWindow();

        // FIXME: right clic doesn't work because selection is handled by ListSelectionListener rather than MouseListener
//        setExplorerHelper(new ExplorerHelper(listResults2));
        // done
        
        
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                choiceLastname.requestFocusInWindow();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        tabMulti = new javax.swing.JPanel();
        labelCount1 = new javax.swing.JLabel();
        lastnameLabel = new javax.swing.JLabel();
        firstnameLabel = new javax.swing.JLabel();
        birthLabel = new javax.swing.JLabel();
        deathLabel = new javax.swing.JLabel();
        placeLabel = new javax.swing.JLabel();
        lastnameText = choiceLastname;
        firstnameText = choiceFirstname;
        birthDateBean = new genj.edit.beans.DateBean();
        deathDateBean = new genj.edit.beans.DateBean();
        placetext = choicePlace;
        maleCb = new javax.swing.JCheckBox();
        femaleCb = new javax.swing.JCheckBox();
        unknownCb = new javax.swing.JCheckBox();
        marrCb = new javax.swing.JCheckBox();
        singleCb = new javax.swing.JCheckBox();
        result1Panel = new javax.swing.JPanel();
        tabTag = new javax.swing.JPanel();

        tabMulti.setPreferredSize(new java.awt.Dimension(150, 354));

        labelCount1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(labelCount1, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.labelCount1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lastnameLabel, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.lastnameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(firstnameLabel, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.firstnameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(birthLabel, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.birthLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deathLabel, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.deathLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(placeLabel, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.placeLabel.text")); // NOI18N

        maleCb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(maleCb, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.maleCb.text")); // NOI18N

        femaleCb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(femaleCb, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.femaleCb.text")); // NOI18N

        unknownCb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(unknownCb, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.unknownCb.text")); // NOI18N

        marrCb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(marrCb, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.marrCb.text")); // NOI18N

        singleCb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(singleCb, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.singleCb.text")); // NOI18N

        javax.swing.GroupLayout result1PanelLayout = new javax.swing.GroupLayout(result1Panel);
        result1Panel.setLayout(result1PanelLayout);
        result1PanelLayout.setHorizontalGroup(
            result1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        result1PanelLayout.setVerticalGroup(
            result1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout tabMultiLayout = new javax.swing.GroupLayout(tabMulti);
        tabMulti.setLayout(tabMultiLayout);
        tabMultiLayout.setHorizontalGroup(
            tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(result1Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(tabMultiLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelCount1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(tabMultiLayout.createSequentialGroup()
                        .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lastnameLabel)
                            .addComponent(firstnameLabel)
                            .addComponent(birthLabel)
                            .addComponent(deathLabel)
                            .addComponent(placeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lastnameText, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(firstnameText, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(placetext, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deathDateBean, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(birthDateBean, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(tabMultiLayout.createSequentialGroup()
                        .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(tabMultiLayout.createSequentialGroup()
                                .addComponent(marrCb)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(singleCb))
                            .addGroup(tabMultiLayout.createSequentialGroup()
                                .addComponent(maleCb)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(femaleCb)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unknownCb)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        tabMultiLayout.setVerticalGroup(
            tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabMultiLayout.createSequentialGroup()
                .addComponent(labelCount1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastnameLabel)
                    .addComponent(lastnameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firstnameLabel)
                    .addComponent(firstnameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(birthLabel)
                    .addComponent(birthDateBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(deathLabel)
                    .addComponent(deathDateBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(placeLabel)
                    .addComponent(placetext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maleCb)
                    .addComponent(femaleCb)
                    .addComponent(unknownCb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabMultiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(singleCb)
                    .addComponent(marrCb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(result1Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.tabMulti.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/genj/search/images/multiSearch.png")), tabMulti, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.tabMulti.TabConstraints.tabToolTip")); // NOI18N

        javax.swing.GroupLayout tabTagLayout = new javax.swing.GroupLayout(tabTag);
        tabTag.setLayout(tabTagLayout);
        tabTagLayout.setHorizontalGroup(
            tabTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 367, Short.MAX_VALUE)
        );
        tabTagLayout.setVerticalGroup(
            tabTagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 399, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.tabTag.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/genj/search/images/tagSearch.png")), tabTag, org.openide.util.NbBundle.getMessage(SearchView.class, "SearchView.tabTag.TabConstraints.tabToolTip")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
    }// </editor-fold>//GEN-END:initComponents


    


    public void start() {

        // if context
        if (context == null) {
            return;
        }

        // stop worker
        getSelectedWorker().stop();

        Worker worker = getSelectedWorker();
        if (worker instanceof WorkerMulti) {
            remember(choiceLastname, oldLastnames, choiceLastname.getText());
            remember(choiceFirstname, oldFirstnames, choiceFirstname.getText());
            remember(choicePlace, oldPlaces, choicePlace.getText());
            worker.start(context.getGedcom(), max_hits, case_sensitive,
                    choiceLastname.getText(), choiceFirstname.getText(), 
                    birthDateBean, 
                    deathDateBean,
                    choicePlace.getText(),
                    maleCb.isSelected(), femaleCb.isSelected(), unknownCb.isSelected(),
                    marrCb.isSelected(), singleCb.isSelected()
            );
        } else if (worker instanceof WorkerTag) {
            String value = choiceValue.getText();
            String tags = choiceTag.getText();
            remember(choiceValue, oldValues, value);
            remember(choiceTag, oldTags, tags);
            worker.start(context.getGedcom(), max_hits, case_sensitive,
                    tags, value, checkRegExp.isSelected());
        }
    }

    public void stop() {
        getSelectedWorker().stop();
    }

    public void clean() {
        if (jTabbedPane1.getSelectedComponent() == tabMulti) {
            choiceLastname.setText("");
            choiceFirstname.setText("");
            choicePlace.setText("");
            birthDateBean.setPropertyImpl(null);
            birthDateBean.setFormat(PropertyDate.BETWEEN_AND);
            deathDateBean.setPropertyImpl(null);
            deathDateBean.setFormat(PropertyDate.BETWEEN_AND);
            maleCb.setSelected(true);
            femaleCb.setSelected(true);
            unknownCb.setSelected(true);
            marrCb.setSelected(true);
            singleCb.setSelected(true);
            choiceLastname.requestFocusInWindow();
        } else {
            choiceTag.setText("");
            choiceValue.setText("");
            choiceValue.requestFocusInWindow();
        }
        getSelectedResults().clear();
        labelCount1.setText("");
    }

    public void clearHistory() {
        if (DialogManager.YES_OPTION != DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ConfirmClear"), NbBundle.getMessage(getClass(), "MSG_ConfirmClear"))
                .setMessageType(DialogManager.YES_NO_OPTION)
                .setOptionType(DialogManager.YES_NO_OPTION).show()) {
            return;
        }
        if (jTabbedPane1.getSelectedComponent() == tabMulti) {
            REGISTRY.remove("old.lastnames");
            REGISTRY.remove("old.firstnames");
            REGISTRY.remove("old.places");
            oldLastnames = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.lastnames", DEFAULT_STR)));
            oldFirstnames = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.firstnames", DEFAULT_STR)));
            oldPlaces = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.places", DEFAULT_STR)));
            choiceLastname.setValues(oldLastnames);
            choiceFirstname.setValues(oldFirstnames);
            choicePlace.setValues(oldPlaces);
        } else {
            REGISTRY.remove("regexp");
            REGISTRY.remove("old.values");
            REGISTRY.remove("old.tags");
            oldTags = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.tags", DEFAULT_TAGS)));
            oldValues = new LinkedList<String>(Arrays.asList(REGISTRY.get("old.values", DEFAULT_VALUES)));
            choiceTag.setValues(oldValues);
            choiceValue.setValues(oldTags);
        }
    }

    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    @Override
    public void removeNotify() {
        // keep old (multi)
        REGISTRY.put("old.lastnames", oldLastnames);
        REGISTRY.put("old.firstnames", oldFirstnames);
        REGISTRY.put("old.places", oldPlaces);
        // keep old (tags)
        REGISTRY.put("regexp", checkRegExp.isSelected());
        REGISTRY.put("old.values", oldValues);
        REGISTRY.put("old.tags", oldTags);
        // continue
        super.removeNotify();
    }

    @Override
    public void setContext(Context newContext) {

        // disconnect old
        if (context.getGedcom() != null && context.getGedcom() != newContext.getGedcom()) {

            stop();
            results1.clear();
            results2.clear();
            labelCount1.setText("");
            labelCount2.setText("");
            actionStart.setEnabled(false);

            context.getGedcom().removeGedcomListener((GedcomListener) Spin.over(results1));
            context.getGedcom().removeGedcomListener((GedcomListener) Spin.over(results2));
        }

        // keep new
        context = newContext;

        // connect new
        if (context.getGedcom() != null) {
            context = new Context(newContext.getGedcom());
            context.getGedcom().addGedcomListener((GedcomListener) Spin.over(results1));
            context.getGedcom().addGedcomListener((GedcomListener) Spin.over(results2));
            actionStart.setEnabled(true);
        }

    }

    /**
     * @see genj.view.ToolBarSupport#populate(javax.swing.JToolBar)
     */
    @Override
    public void populate(ToolBar toolbar) {
        toolbar.add(actionStart);
        toolbar.add(actionStop);
        toolbar.add(actionClean);
        toolbar.add(actionClearHistory);
        toolbar.addGlue();
        toolbar.addSeparator();
        toolbar.add(actionSettings);
    }

    /**
     * Remembers a value
     */
    private void remember(ChoiceWidget choice, LinkedList<String> old, String value) {
        // not if empty
        if (value.trim().length() == 0) {
            return;
        }
        // keep (up to max)
        old.remove(value);
        old.addFirst(value);
        if (old.size() > MAX_OLD) {
            old.removeLast();
        }
        // update choice
        choice.setValues(old);
        choice.setText(value);
        // done
    }

    /**
     * Create preset Tag Actions
     */
    private List<AbstractAncestrisAction> createTagActions() {

        // loop through DEFAULT_TAGS
        List<AbstractAncestrisAction> result = new ArrayList<AbstractAncestrisAction>();
        for (String tag : DEFAULT_TAGS) {
            result.add(new ActionTag(tag));
        }

        // done
        return result;
    }

    /**
     * Create RegExp Pattern Actions
     */
    private List<AbstractAncestrisAction> createPatternActions() {
        // loop until ...
        List<AbstractAncestrisAction> result = new ArrayList<AbstractAncestrisAction>();
        for (int i = 0;; i++) {
            // check text and pattern
            String key = "regexp." + i,
                    txt = RESOURCES.getString(key + ".txt", false),
                    pat = RESOURCES.getString(key + ".pat", false);
            // no more?
            if (txt == null) {
                break;
            }
            // pattern?
            if (pat == null) {
                continue;
            }
            // create action
            result.add(new ActionPattern(txt, pat));
        }
        return result;
    }

    /**
     * Return list of results
     */
    public List<Property> getResults() {
        List<Property> props = new ArrayList<Property>();
        for (Hit hit : getSelectedResults().hits) {
            props.add(hit.getProperty());
        }
        return props;
    }

    
    private Worker getSelectedWorker() {
        Worker worker = null;
        if (jTabbedPane1.getSelectedComponent() == tabMulti) {
            worker = worker1;
        } else {
            worker = worker2;
        }
        return worker;
    }
    
    private Results getSelectedResults() {
        Results results = null;
        if (jTabbedPane1.getSelectedComponent() == tabMulti) {
            results = results1;
        } else {
            results = results2;
        }
        return results;
    }
    
    
    private void displaySettings() {
        SettingsPanel settingsPanel = new SettingsPanel(REGISTRY);
        DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChangeSettings"), settingsPanel)
                .setMessageType(DialogManager.PLAIN_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION).show();
        settingsPanel.setSettings();
        max_hits = settingsPanel.getMaxHits();
        case_sensitive = settingsPanel.getCaseSensitive();
    }
    
    
    
    
    /**
     * Action - select predefined paths
     */
    private class ActionTag extends AbstractAncestrisAction {

        private final String tags;

        /**
         * Constructor
         */
        private ActionTag(String tags) {
            this.tags = tags;

            WordBuffer txt = new WordBuffer(", ");
            for (String t : tags.split(",")) {
                txt.append(Gedcom.getName(t.trim()));
            }
            setText(txt.toString());
        }

        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            choiceTag.setText(tags);
        }
    } //ActionPath

    
    
    
    
    /**
     * Action - insert regexp construct
     * {0} all text
     * {1} before selection
     * {2} (selection)
     * {3} after selection
     */
    private class ActionPattern extends AbstractAncestrisAction {

        /** pattern */
        private final String pattern;

        /**
         * Constructor
         */
        private ActionPattern(String txt, String pat) {
            // make first word bold
            int i = txt.indexOf(' ');
            if (i > 0) {
                txt = "<html><b>" + txt.substring(0, i) + "</b>&nbsp;&nbsp;&nbsp;" + txt.substring(i) + "</html>";
            }

            setText(txt);
            pattern = pat;
        }

        /**
         * @see genj.util.swing.AbstractAncestrisAction#execute()
         */
        @Override
        public void actionPerformed(ActionEvent event) {

            // analyze what we've got
            final JTextField field = choiceValue.getTextEditor();
            int selStart = field.getSelectionStart(),
                    selEnd = field.getSelectionEnd();
            if (selEnd <= selStart) {
                selStart = field.getCaretPosition();
                selEnd = selStart;
            }
            // {0} all text
            String all = field.getText();
            // {1} before selection
            String before = all.substring(0, selStart);
            // {2} (selection)
            String selection = selEnd > selStart ? '(' + all.substring(selStart, selEnd) + ')' : "";
            // {3] after selection
            String after = all.substring(selEnd);

            // calculate result
            final String result = MessageFormat.format(pattern, new Object[]{all, before, selection, after});

            // invoke this later - selection might otherwise not work correctly
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    int pos = result.indexOf('#');

                    // show
                    field.setText(result.substring(0, pos) + result.substring(pos + 1));
                    field.select(0, 0);
                    field.setCaretPosition(pos);

                    // make sure regular expressions are enabled now
                    checkRegExp.setSelected(true);
                }
            });

            // done
        }
    } //ActionInsert

    
    
    
    
    /**
     * Action - trigger search
     */
    private class ActionStart extends AbstractAncestrisAction {

        /** constructor */
        private ActionStart() {
            setImage(IMG_START);
            setTip(RESOURCES.getString("start.tip"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            stop();
            start();
        }
    } //ActionSearch

    /**
     * Action - stop search
     */
    private class ActionStop extends AbstractAncestrisAction {

        /** constructor */
        private ActionStop() {
            setImage(IMG_STOP);
            setTip(RESOURCES.getString("stop.tip"));
            setEnabled(false);
        }

        /** run */
        @Override
        public void actionPerformed(ActionEvent event) {
            stop();
        }
    } //ActionStop

    /**
     * Action - clean search criteria
     */
    private class ActionClean extends AbstractAncestrisAction {

        /** constructor */
        private ActionClean() {
            setImage(IMG_CLEAN);
            setTip(RESOURCES.getString("clean.tip"));
            //setEnabled(false);
        }

        /** run */
        @Override
        public void actionPerformed(ActionEvent event) {
            clean();
        }
    } //ActionStop

    
    /**
     * Action - clear history of values
     */
    private class ActionClearHistory extends AbstractAncestrisAction {

        /** constructor */
        private ActionClearHistory() {
            setImage(IMG_CLEAR);
            setTip(RESOURCES.getString("clearHistory.tip"));
            //setEnabled(false);
        }

        /** run */
        @Override
        public void actionPerformed(ActionEvent event) {
            clearHistory();
            clean();
        }
    } //ActionStop

    
    /**
     * Action - clear history of values
     */
    private class ActionSettings extends AbstractAncestrisAction {

        /** constructor */
        private ActionSettings() {
            setImage(IMG_SETTINGS);
            setTip(RESOURCES.getString("settings.tip"));
            //setEnabled(false);
        }

        /** run */
        @Override
        public void actionPerformed(ActionEvent event) {
            displaySettings();
        }
    } //ActionStop

    
    
    
    
    /**
     * Our result bucket
     */
    private static class Results extends AbstractListModel implements GedcomListener {

        /** the results */
        private List<Hit> hits = new ArrayList<Hit>();

        /**
         * clear the results (sync to EDT)
         */
        private void clear() {
            // nothing to do?
            if (hits.isEmpty()) {
                return;
            }
            // clear&notify
            int size = hits.size();
            hits.clear();
            fireIntervalRemoved(this, 0, size - 1);
            // done
        }

        /**
         * add a result (sync to EDT)
         */
        private void add(List<Hit> list) {
            // nothing to do?
            if (list.isEmpty()) {
                return;
            }
            // remember 
            int size = hits.size();
            hits.addAll(list);
            fireIntervalAdded(this, size, hits.size() - 1);
            // done
        }

        /**
         * @see javax.swing.ListModel#getElementAt(int)
         */
        @Override
        public Object getElementAt(int index) {
            return hits.get(index);
        }

        /**
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize() {
            return hits.size();
        }

        /**
         * access to property
         */
        private Hit getHit(int i) {
            return hits.get(i);
        }

        @Override
        public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
            // TODO could do a re-search here
        }

        @Override
        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
            // ignored
        }

        @Override
        public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
            // TODO could do a re-search here
        }

        @Override
        public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
            for (int i = 0; i < hits.size(); i++) {
                Hit hit = hits.get(i);
                if (hit.getProperty() == prop) {
                    fireContentsChanged(this, i, i);
                }
            }
        }

        @Override
        public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
            for (int i = 0; i < hits.size();) {
                Hit hit = hits.get(i);
                if (hit.getProperty() == removed) {
                    hits.remove(i);
                    fireIntervalRemoved(this, i, i);
                } else {
                    i++;
                }
            }
        }
    } //Results

    
    
    
    
    /**
     * our specialized list
     */
    private class ResultWidget extends JList implements ListSelectionListener, ListCellRenderer {

        private final Results results;
        private final JTextPane text = new JTextPane();
        /** background colors */
        private final Color[] bgColors = new Color[3];

        /**
         * Constructor
         */
        private ResultWidget(final Results results) {
            super(results);
            this.results = results;
            // colors
            bgColors[0] = getSelectionBackground();
            bgColors[1] = getBackground();
            bgColors[2] = new Color(
                    Math.max(bgColors[1].getRed() - 16, 0),
                    Math.min(bgColors[1].getGreen() + 16, 255),
                    Math.max(bgColors[1].getBlue() - 16, 0));

            // rendering
            setCellRenderer(this);
            addListSelectionListener(this);
            text.setOpaque(true);
            addMouseListener(new FilteredMouseAdapter() {
                @Override
                public void mouseClickedFiltered(MouseEvent e) {
                    int row = getSelectedIndex();
                    if (row >= 0) {
                        // FIXME: action is handled here and selection is handled in changeSelection
                        Object cell = results.getHit(row).getProperty();
                        if (cell != null && cell instanceof Property) {
                            SelectionDispatcher.fireSelection(e, new Context((Property) cell));
                        }
                    }
                }
            });
        }

        /**
         * ContextProvider - callback
         */
        public ViewContext getContext() {

            if (context == null) {
                return null;
            }

            List<Property> properties = new ArrayList<Property>();
            Object[] selection = getSelectedValues();
            for (Object selection1 : selection) {
                Hit hit = (Hit) selection1;
                properties.add(hit.getProperty());
            }
            return new ViewContext(context.getGedcom(), null, properties);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Hit hit = (Hit) value;

            // prepare color
            int c = isSelected ? 0 : 1 + (hit.getEntity() & 1);
            text.setBackground(bgColors[c]);
            text.setBorder(isSelected ? createLineBorder(getSelectionBackground(), 1, false) : createEmptyBorder(2, 2, 2, 2));

            // show hit document (includes image and text)
            text.setDocument(hit.getDocument());
            return text;
        }

        /**
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int row = getSelectedIndex();
            if (row >= 0) {
                SelectionDispatcher.fireSelection(new Context(results.getHit(row).getProperty()));
            }
        }
    } //ResultWidget
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private genj.edit.beans.DateBean birthDateBean;
    private javax.swing.JLabel birthLabel;
    private genj.edit.beans.DateBean deathDateBean;
    private javax.swing.JLabel deathLabel;
    private javax.swing.JCheckBox femaleCb;
    private javax.swing.JLabel firstnameLabel;
    private javax.swing.JComboBox firstnameText;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelCount1;
    private javax.swing.JLabel lastnameLabel;
    private javax.swing.JComboBox lastnameText;
    private javax.swing.JCheckBox maleCb;
    private javax.swing.JCheckBox marrCb;
    private javax.swing.JLabel placeLabel;
    private javax.swing.JComboBox placetext;
    private javax.swing.JPanel result1Panel;
    private javax.swing.JCheckBox singleCb;
    private javax.swing.JPanel tabMulti;
    private javax.swing.JPanel tabTag;
    private javax.swing.JCheckBox unknownCb;
    // End of variables declaration//GEN-END:variables
}
