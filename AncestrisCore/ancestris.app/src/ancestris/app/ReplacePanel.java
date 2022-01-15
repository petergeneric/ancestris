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
package ancestris.app;

import ancestris.api.search.SearchCommunicator;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyXRef;
import genj.gedcom.UnitOfWork;
import genj.util.Registry;
import genj.util.swing.ImageIcon;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class ReplacePanel extends javax.swing.JPanel {

    final static Logger LOG = Logger.getLogger("ancestris.find-and-replace");
    Gedcom gedcom;
    private Registry registry = null;

    boolean replaceMode = false;   // false if find only, true if find & replace
    private DefaultComboBoxModel cbFindModel = new DefaultComboBoxModel();
    JTextComponent comboFindText = null;
    private DefaultComboBoxModel cbReplaceModel = new DefaultComboBoxModel();
    JTextComponent comboReplaceText = null;
    private LinkedList<String> oldFindWhat, oldReplaceWith, oldSelectedEntities, oldSelectedCategories;
    private final static String[] DEFAULT_VALUES = {""};
    private final static int MAX_OLD = 16;

    private String SELECT_ALL = "";
    private String UNSELECT_ALL = "";
    private JPopupMenu entityPopupMenu, propertyPopupMenu;
    private List<Category> categories = null;
    private boolean categoryUpToDate = false;
    private List<Property> results = null;
    private int resultsTotal = 0;
    private int resultsCurrent = 0;
    private boolean ready = false;

    /**
     * Creates new form ReplacePanel
     *
     * @param gedcom
     */
    public ReplacePanel(Gedcom gedcom, boolean replaceMode) {

        registry = Registry.get(getClass());

        this.gedcom = gedcom;
        this.replaceMode = replaceMode;

        // Define parameters
        oldFindWhat = new LinkedList<>(Arrays.asList(registry.get("findreplace_old_findwhat", DEFAULT_VALUES)));
        oldReplaceWith = new LinkedList<>(Arrays.asList(registry.get("findreplace_old_replacewith", DEFAULT_VALUES)));
        updateModel(cbFindModel, oldFindWhat);
        updateModel(cbReplaceModel, oldReplaceWith);
        SELECT_ALL = NbBundle.getMessage(getClass(), "ReplacePanel.entityFilter.selectall");
        UNSELECT_ALL = NbBundle.getMessage(getClass(), "ReplacePanel.entityFilter.unselectall");
        results = new ArrayList<Property>();

        // Entity menu --------------------------
        oldSelectedEntities = new LinkedList<String>(Arrays.asList(registry.get("findreplace_old_selectedEntities", Gedcom.ENTITIES)));
        entityPopupMenu = new JPopupMenu();
        JCheckBoxMenuItem entityMenuItem = new JCheckBoxMenuItem(UNSELECT_ALL, null, false);
        entityMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                boolean set = item.getText().equals(SELECT_ALL);
                for (MenuElement element : entityPopupMenu.getSubElements()) {
                    ((JCheckBoxMenuItem) element).setSelected(set);
                }
                item.setText(set ? UNSELECT_ALL : SELECT_ALL);
                item.setSelected(false);
                showEntityMenu();
                resetCategories();
            }
        });
        entityPopupMenu.add(entityMenuItem);
        for (String str : Gedcom.ENTITIES) {
            boolean select = oldSelectedEntities.isEmpty();
            for (String oldEnt : oldSelectedEntities) {
                if (str.equals(oldEnt)) {
                    select = true;
                    break;
                }
            }
            int nb = gedcom.getEntities(str).size();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(Gedcom.getName(str) + " (" + nb + ")", Gedcom.getEntityImage(str), select);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showEntityMenu();
                    categoryUpToDate = false;
                }
            });
            entityPopupMenu.add(item);
        }

        // Property menu --------------------------
        oldSelectedCategories = new LinkedList<String>(Arrays.asList(registry.get("findreplace_old_selectedCategories", DEFAULT_VALUES)));
        initCategories();

        // All components
        initComponents();
        replacewithLabel.setVisible(replaceMode == true);
        replaceCombo.setVisible(replaceMode == true);
        FilterPopupListener fpl = new FilterPopupListener();
        entityPopupMenu.addPopupMenuListener(fpl);
        propertyPopupMenu.addPopupMenuListener(fpl);

        // Display parameters
        resetCategories();
        findCombo.setModel(cbFindModel);
        comboFindText = (JTextComponent) findCombo.getEditor().getEditorComponent();
        comboFindText.getDocument().addDocumentListener(new ComboListener());
        comboReplaceText = (JTextComponent) replaceCombo.getEditor().getEditorComponent();
        replaceCombo.setModel(cbReplaceModel);
        matchCheckBox.setSelected(registry.get("findreplace_old_matchcase", false));
        wholeWordCheckBox.setSelected(registry.get("findreplace_old_wholeword", false));
        selectionCheckBox.setSelected(registry.get("findreplace_old_selection", false));

        // Launch first search 
        ready = true;
        findResults(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        findwhatLabel1 = new javax.swing.JLabel();
        findCombo = new javax.swing.JComboBox();
        replacewithLabel = new javax.swing.JLabel();
        replaceCombo = new javax.swing.JComboBox();
        matchCheckBox = new javax.swing.JCheckBox();
        wholeWordCheckBox = new javax.swing.JCheckBox();
        filtersLabel = new javax.swing.JLabel();
        selectionCheckBox = new javax.swing.JCheckBox();
        matchesLabel = new javax.swing.JLabel();
        resultmatchesLabel = new javax.swing.JLabel();
        resultEntityLabel = new javax.swing.JLabel();
        resultEntity = new javax.swing.JLabel();
        resultPropertyLabel = new javax.swing.JLabel();
        resultProperty = new javax.swing.JLabel();
        showButton = new javax.swing.JButton();
        resultScrollPane = new javax.swing.JScrollPane();
        resultTextPane = new javax.swing.JTextPane();
        previousButton = new javax.swing.JButton();
        replaceButton = new javax.swing.JButton();
        replaceAllButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        entityFilter = new javax.swing.JButton();
        propertyFilter = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(600, 400));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(findwhatLabel1, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.findwhatLabel1.text")); // NOI18N

        findCombo.setEditable(true);
        findCombo.setMaximumRowCount(15);
        findCombo.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.findCombo.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(replacewithLabel, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.replacewithLabel.text")); // NOI18N

        replaceCombo.setEditable(true);
        replaceCombo.setMaximumRowCount(15);
        replaceCombo.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.replaceCombo.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(matchCheckBox, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.matchCheckBox.text")); // NOI18N
        matchCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.matchCheckBox.toolTipText")); // NOI18N
        matchCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(wholeWordCheckBox, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.wholeWordCheckBox.text")); // NOI18N
        wholeWordCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.wholeWordCheckBox.toolTipText")); // NOI18N
        wholeWordCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wholeWordCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(filtersLabel, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.filtersLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectionCheckBox, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.selectionCheckBox.text")); // NOI18N
        selectionCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.selectionCheckBox.toolTipText")); // NOI18N
        selectionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(matchesLabel, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.matchesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultmatchesLabel, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultmatchesLabel.text")); // NOI18N
        resultmatchesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultmatchesLabel.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultEntityLabel, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultEntityLabel.text")); // NOI18N

        resultEntity.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resultEntity, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultEntity.text")); // NOI18N
        resultEntity.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultEntity.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultPropertyLabel, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultPropertyLabel.text")); // NOI18N

        resultProperty.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resultProperty, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultProperty.text")); // NOI18N
        resultProperty.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultProperty.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showButton, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.showButton.text")); // NOI18N
        showButton.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.showButton.toolTipText")); // NOI18N
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });

        resultTextPane.setEditable(false);
        resultTextPane.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.resultTextPane.toolTipText")); // NOI18N
        resultTextPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                resultTextPaneKeyPressed(evt);
            }
        });
        resultScrollPane.setViewportView(resultTextPane);

        org.openide.awt.Mnemonics.setLocalizedText(previousButton, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.previousButton.text")); // NOI18N
        previousButton.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.previousButton.toolTipText")); // NOI18N
        previousButton.setPreferredSize(new java.awt.Dimension(100, 27));
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(replaceButton, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.replaceButton.text")); // NOI18N
        replaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.replaceButton.toolTipText")); // NOI18N
        replaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(replaceAllButton, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.replaceAllButton.text")); // NOI18N
        replaceAllButton.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.replaceAllButton.toolTipText")); // NOI18N
        replaceAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(nextButton, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.nextButton.text")); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.nextButton.toolTipText")); // NOI18N
        nextButton.setPreferredSize(new java.awt.Dimension(100, 27));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(entityFilter, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.entityFilter.text")); // NOI18N
        entityFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entityFilterActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(propertyFilter, org.openide.util.NbBundle.getMessage(ReplacePanel.class, "ReplacePanel.propertyFilter.text")); // NOI18N
        propertyFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertyFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(previousButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(replaceButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(replaceAllButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(matchesLabel)
                            .addComponent(filtersLabel)
                            .addComponent(replacewithLabel)
                            .addComponent(findwhatLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(findCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(replaceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(entityFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(propertyFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(matchCheckBox)
                                        .addGap(18, 18, 18)
                                        .addComponent(wholeWordCheckBox))
                                    .addComponent(selectionCheckBox)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(resultmatchesLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(showButton))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(resultEntityLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(resultEntity))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(resultPropertyLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(resultProperty)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(findwhatLabel1)
                    .addComponent(findCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(matchCheckBox)
                    .addComponent(wholeWordCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(replacewithLabel)
                    .addComponent(replaceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectionCheckBox)
                    .addComponent(filtersLabel)
                    .addComponent(entityFilter)
                    .addComponent(propertyFilter))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(matchesLabel)
                    .addComponent(resultEntityLabel)
                    .addComponent(resultEntity))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resultPropertyLabel)
                    .addComponent(resultProperty))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showButton)
                    .addComponent(resultmatchesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previousButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replaceButton)
                    .addComponent(replaceAllButton)
                    .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    }//GEN-LAST:event_formComponentResized

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        next();
    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        previous();
    }//GEN-LAST:event_previousButtonActionPerformed

    private void replaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceButtonActionPerformed
        replace();
        findResults(resultsCurrent);
    }//GEN-LAST:event_replaceButtonActionPerformed

    private void replaceAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllButtonActionPerformed
        replaceAll();
        findResults(0);
    }//GEN-LAST:event_replaceAllButtonActionPerformed

    private void entityFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entityFilterActionPerformed
        showEntityMenu();
    }//GEN-LAST:event_entityFilterActionPerformed

    private void propertyFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertyFilterActionPerformed
        showPropertyMenu();
    }//GEN-LAST:event_propertyFilterActionPerformed

    private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
        SelectionDispatcher.fireSelection(evt, new Context(results.get(resultsCurrent)));
    }//GEN-LAST:event_showButtonActionPerformed

    private void matchCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchCheckBoxActionPerformed
        findResults(0);
    }//GEN-LAST:event_matchCheckBoxActionPerformed

    private void wholeWordCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wholeWordCheckBoxActionPerformed
        findResults(0);
    }//GEN-LAST:event_wholeWordCheckBoxActionPerformed

    private void selectionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectionCheckBoxActionPerformed
        findResults(0);
    }//GEN-LAST:event_selectionCheckBoxActionPerformed

    private void resultTextPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resultTextPaneKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_PAGE_UP || evt.getKeyCode() == KeyEvent.VK_LEFT) {
            previous();
        } else if (evt.getKeyCode() == KeyEvent.VK_DOWN || evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN || evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            next();
        }
    }//GEN-LAST:event_resultTextPaneKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton entityFilter;
    private javax.swing.JLabel filtersLabel;
    private javax.swing.JComboBox findCombo;
    private javax.swing.JLabel findwhatLabel1;
    private javax.swing.JCheckBox matchCheckBox;
    private javax.swing.JLabel matchesLabel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JButton propertyFilter;
    private javax.swing.JButton replaceAllButton;
    private javax.swing.JButton replaceButton;
    private javax.swing.JComboBox replaceCombo;
    private javax.swing.JLabel replacewithLabel;
    private javax.swing.JLabel resultEntity;
    private javax.swing.JLabel resultEntityLabel;
    private javax.swing.JLabel resultProperty;
    private javax.swing.JLabel resultPropertyLabel;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JTextPane resultTextPane;
    private javax.swing.JLabel resultmatchesLabel;
    private javax.swing.JCheckBox selectionCheckBox;
    private javax.swing.JButton showButton;
    private javax.swing.JCheckBox wholeWordCheckBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Keep in registry
     */
    public void saveParams() {
        ready = false;
        rememberAll();
        registry.put("findreplace_old_findwhat", oldFindWhat);
        registry.put("findreplace_old_replacewith", oldReplaceWith);
        registry.put("findreplace_old_matchcase", matchCheckBox.isSelected());
        registry.put("findreplace_old_wholeword", wholeWordCheckBox.isSelected());
        registry.put("findreplace_old_selectedEntities", oldSelectedEntities);
        registry.put("findreplace_old_selectedCategories", oldSelectedCategories.toArray(new String[oldSelectedCategories.size()]));
        registry.put("findreplace_old_selection", selectionCheckBox.isSelected());
    }

    /**
     * Store all criteria
     */
    private void rememberAll() {
        // remember fields
        String text = (String) findCombo.getSelectedItem();
        remember(cbFindModel, oldFindWhat, text);
        text = (String) replaceCombo.getSelectedItem();
        remember(cbReplaceModel, oldReplaceWith, text);

        // remember selected entities
        oldSelectedEntities.clear();
        for (String str : Gedcom.ENTITIES) {
            for (MenuElement element : entityPopupMenu.getSubElements()) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) element;
                if (item.getText().startsWith(Gedcom.getName(str)) && item.isSelected()) {
                    oldSelectedEntities.add(str);
                    break;
                }
            }
        }

        // remember selected categories
        updateCategories();
    }

    private void updateCategories() {
        oldSelectedCategories.clear();
        for (Category category : categories) {
            for (MenuElement element : propertyPopupMenu.getSubElements()) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) element;
                if (item.getText().startsWith(category.getName())) {
                    category.setSelected(item.isSelected());                    // update
                    if (category.isSelected()) {
                        oldSelectedCategories.add(category.getId());            // and remember
                    }
                    break;
                }
            }
        }
    }

    /**
     * Remembers a value
     */
    private void remember(DefaultComboBoxModel model, LinkedList<String> old, String value) {
        // not if empty
        if (value == null || value.trim().length() == 0) {
            return;
        }
        // keep (up to max)
        old.remove(value);
        old.addFirst(value);
        if (old.size() > MAX_OLD) {
            old.removeLast();
        }
        updateModel(model, old);
    }

    /**
     * Put list in combo box
     */
    private void updateModel(DefaultComboBoxModel model, LinkedList<String> old) {
        model.removeAllElements();
        for (String str : old) {
            model.addElement(str);
        }
    }

    private void showEntityMenu() {
        entityPopupMenu.show(entityFilter, 3, entityFilter.getHeight() - 5);
    }

    private void showPropertyMenu() {
        if (!categoryUpToDate) {
            resetCategories();
            categoryUpToDate = true;
        }
        propertyPopupMenu.show(propertyFilter, 3, propertyFilter.getHeight() - 5);
    }

    private void initCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        categories.clear();
        categories.add(new Category("name", new ImageIcon(gedcom, "images/Name"), new String[]{"NAME", "GIVN", "SURN", "NICK", "NPFX", "AUTH", "SPFX", "NSFX", "CORP", "DATA", "SEX"}));
        categories.add(new Category("place", new ImageIcon(gedcom, "images/Place"), new String[]{"PLAC", "MAP", "LATI", "LONG"}));
        categories.add(new Category("address", new ImageIcon(gedcom, "images/Addr"), new String[]{"ADDR", "CITY", "POST", "PHON", "EMAI", "EMAIL", "STAE", "CTRY", "ADR1", "ADR2", "ADR3", "WWW", "FAX"}));
        categories.add(new Category("occupation", new ImageIcon(gedcom, "images/Occupation"), new String[]{"OCCU"}));
        categories.add(new Category("description", new ImageIcon(gedcom, "images/Type"), new String[]{"TYPE", "PEDI", "RESN", "STAT", "CAST", "DSCR", "EDUC", "NATI", "PROP", "RELI", "FACT", "ROLE", "FONE", "ROMN", "DESC", "ANCI", "DESI", "FAMF" }));
        categories.add(new Category("event", new ImageIcon(gedcom, "images/Event"), new String[]{"AGNC", "CAUS", "BIRT", "DEAT", "CHR", "BURI", "CREM", "ADOP", "CHRA", "CONF",
            "FCOM", "ORDN", "RETI", "CONL", "SLGC", "BAPM", "BARM", "BASM", "BLES", "NATU", "EMIG", "IMMI", "PROB", "WILL", "GRAD", "BAPL", "ENDL", "ORDI",
            "SLGS", "EVEN", "ANUL", "CENS", "DIV", "DIVF", "ENGA", "MARB", "MARC", "MARR", "MARL", "MARS", "RESI"}));
        categories.add(new Category("relation", new ImageIcon(gedcom, "images/Description"), new String[]{"RELA", "ALIA", "ASSO", "CHIL", "INDI", "CHIL", "FAM", "FAMC", "FAMS", "HUSB", "WIFE", "OBJE", "REPO", "SOUR", "SUBM"  }));
        categories.add(new Category("date", new ImageIcon(gedcom, "images/Date"), new String[]{"DATE", "CHAN", "TIME" }));
        categories.add(new Category("age", new ImageIcon(gedcom, "images/Birth"), new String[]{"AGE"}));
        categories.add(new Category("note", new ImageIcon(gedcom, "images/Note"), new String[]{"NOTE"}));
        categories.add(new Category("text", new ImageIcon(gedcom, "images/Title"), new String[]{"TEXT", "TITL", "ABBR", "PUBL"}));
        categories.add(new Category("number", new ImageIcon(gedcom, "images/IDNumber"), new String[]{"CALN", "REFN", "RIN", "AFN", "ANCE", "IDNO", "NMR", "PAGE", "RFN", "SSN", "NCHI"}));
        categories.add(new Category("filename", new ImageIcon(gedcom, "images/Disk"), new String[]{"FILE"}));
        categories.add(new Category("media", new ImageIcon(gedcom, "images/Media"), new String[]{"MEDI"}));
        categories.add(new Category("quality", new ImageIcon(gedcom, "images/Repository"), new String[]{"QUAY"}));
        categories.add(new Category("format", new ImageIcon(gedcom, "images/Format"), new String[]{"FORM", "CHAR", "COPR", "DEST", "GEDC", "LANG", "SUBN", "TEMP", "VERS" }));
        categories.add(new Category("user", new ImageIcon(gedcom, "images/Question"), new String[]{"_"}));
        
        propertyPopupMenu = new JPopupMenu();
        JCheckBoxMenuItem propertyMenuItem = new JCheckBoxMenuItem(UNSELECT_ALL, null, false);
        propertyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                boolean set = item.getText().equals(SELECT_ALL);
                for (MenuElement element : propertyPopupMenu.getSubElements()) {
                    ((JCheckBoxMenuItem) element).setSelected(set);
                }
                item.setText(set ? UNSELECT_ALL : SELECT_ALL);
                item.setSelected(false);
                showPropertyMenu();
            }
        });
        propertyPopupMenu.add(propertyMenuItem);
        for (Category category : categories) {
            boolean select = oldSelectedCategories.isEmpty();
            for (String oldCat : oldSelectedCategories) {
                if (category.getId().equals(oldCat)) {
                    select = true;
                    break;
                }
            }
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(category.getName() + " (" + category.getVolume() + ")", category.getIcon(), select);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showPropertyMenu();
                }
            });
            propertyPopupMenu.add(item);
        }

    }

    /**
     * Calculates number of properties per category, taking into account
     * selected entities in the entity filter
     *
     * @return
     */
    private void resetCategories() {
        // Reset counters
        for (Category category : categories) {
            category.reset();
        } // loop categories

        // Recalc counters
        for (String str : Gedcom.ENTITIES) {
            boolean selected = false;
            // Skip entities not selected
            for (MenuElement element : entityPopupMenu.getSubElements()) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) element;
                if (item.getText().startsWith(Gedcom.getName(str)) & item.isSelected()) {
                    selected = true;
                    break;
                }
            }
            if (!selected) {
                continue;
            }
            for (Entity entity : gedcom.getEntities(str)) {
                for (Property property : entity.getProperties(Property.class)) {
                    String tag = property.getTag();
                    for (Category category : categories) {
                        if (category.contains(tag)) {
                            category.incrementVolume();
                            break;
                        }
                    } // loop categories
                } // loop properties within entity
            } // loop entities
        } // loop entity types

        Collections.sort(categories, new CategoryComparator());

        // Rebuild menu
        for (Category category : categories) {
            for (MenuElement element : propertyPopupMenu.getSubElements()) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) element;
                if (item.getText().startsWith(category.getName())) {
                    item.setText(category.getName() + " (" + category.getVolume() + ")");
                    category.setSelected(item.isSelected());
                    break;
                }
            }
        } // loop categories

    }

    private void findResults(int indexToShow) {

        // Quit if not ready
        if (!ready) {
            return;
        }

        // Reset search
        String toBeFound = comboFindText.getText();
        results.clear();

        // Init advance search subset if any
        Set<Property> selection = new HashSet<>();
        if (selectionCheckBox.isSelected()) {
            selection.addAll(getSelection());
        }

        // Update categories
        updateCategories();

        // Search loops
        for (String str : Gedcom.ENTITIES) {
            boolean selected = false;
            // Skip entities not selected
            for (MenuElement element : entityPopupMenu.getSubElements()) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) element;
                if (item.getText().startsWith(Gedcom.getName(str)) & item.isSelected()) {
                    selected = true;
                    break;
                }
            }
            if (!selected) {
                continue;
            }
            // Loop entities
            for (Entity entity : gedcom.getEntities(str)) {
                // Loop properties
                for (Property property : entity.getProperties(Property.class)) {
                    // Exclude XRef, they are already included in their main entity
                    // Exclude NAME as it is already included in its sub-properties and would show doubles
                    if (property instanceof PropertyXRef || property.getTag().equals("NAME")) {
                        continue;
                    }
                    String tag = property.getTag();
                    for (Category category : categories) {
                        // Only consider selected categories
                        if (!category.isSelected() || !category.contains(tag)) {
                            continue;
                        }
                        String str1 = Normalizer.normalize(property.getDisplayValue(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                        String str2 = Normalizer.normalize(toBeFound, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                        Matcher m = match(str1, str2);
                        if (m != null && m.find() && (!selectionCheckBox.isSelected() || selection.contains(property) || selection.contains(property.getEntity()))) {
                            results.add(property);
                            break;
                        }
                    } // loop categories
                } // loop properties within entity
            } // loop entities
        } // loop entity types

        // Show results
        resultsTotal = results.size() - 1;
        resultsCurrent = indexToShow;
        if (resultsCurrent > resultsTotal) {
            resultsCurrent--;
        }
        displayResult();
        showButtons(!results.isEmpty());
    }

    private void displayResult() {

        resultTextPane.setText("");

        if (results == null || results.isEmpty() || resultsCurrent < 0 || resultsCurrent > resultsTotal) {
            displayNullResults();
            return;
        }
        resultmatchesLabel.setText(NbBundle.getMessage(getClass(), "ReplacePanel.resultmatchesLabel.text", resultsCurrent + 1, resultsTotal + 1));
        Property prop = results.get(resultsCurrent);
        resultEntity.setIcon(prop.getEntity().getImage());
        String str = prop.getEntity().toString(true);
        if (str.length() > 80) {
            str = str.substring(0, 80) + "...";
        }
        resultEntity.setText(str);
        resultProperty.setIcon(prop.getImage());
        resultProperty.setText(prop.getPath().getName());
        resultProperty.setToolTipText("<html>" + NbBundle.getMessage(getClass(), "ReplacePanel.resultProperty.toolTipText") + "<br>" + prop.getPropertyInfo() + "</html>");

        // Display document
        // 1. get elements
        String text = Normalizer.normalize(prop.getDisplayValue(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String toBeFound = Normalizer.normalize(comboFindText.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 2. include text
        StyledDocument doc = new DefaultStyledDocument();
        try {
            doc.insertString(0, prop.getDisplayValue(), null);
        } catch (BadLocationException ex) {
            //Exceptions.printStackTrace(ex);
            displayNullResults();
        }

        // 3. highlight in red where it matches
        SimpleAttributeSet highlighted = new SimpleAttributeSet();
        StyleConstants.setForeground(highlighted, Color.RED);
        StyleConstants.setBold(highlighted, true);
        Matcher m = match(text, toBeFound);
        int firstIndex = -1;
        while (m != null && m.find()) {
            if (firstIndex == -1) {
                firstIndex = m.start();
            }
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), highlighted, false);
        }
        resultTextPane.setDocument(doc);
        resultTextPane.setCaretPosition(Math.max(0, firstIndex));
    }

    private void displayNullResults() {
        resultmatchesLabel.setText(NbBundle.getMessage(getClass(), "ReplacePanel.resultmatchesLabel.text", 0, 0));
        resultEntity.setIcon(null);
        resultEntity.setText("-");
        resultProperty.setIcon(null);
        resultProperty.setText("-");
        resultProperty.setToolTipText(NbBundle.getMessage(getClass(), "ReplacePanel.resultProperty.toolTipText"));
        resultTextPane.setText("-");
    }

    /**
     * Check if toBeFound string is included in text with the Case, Whole word,
     * Masks criteria - standard : /blabla - whole word : /(\bblabla\b) - case
     * insensitive : /i at the end so /(\blaPIn\b)/i or /blabla/i
     *
     * @param text
     * @return
     */
    private Matcher match(String text, String toBeFound) {
        String textToAnalyse = text;
        String regex = "";
        if (wholeWordCheckBox.isSelected()) {
            regex += "\\b" + toBeFound + "\\b";
        } else {
            regex += toBeFound;
        }
        try {
            Pattern pattern = matchCheckBox.isSelected() ? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            return pattern.matcher(textToAnalyse);
        } catch (Exception e) {
        }
        return null;
    }

    private void showButtons(boolean set) {
        nextButton.setEnabled(set);
        previousButton.setEnabled(set);
        replaceAllButton.setEnabled(set && replaceMode);
        replaceButton.setEnabled(set && replaceMode);
        showButton.setEnabled(set);
        replaceAllButton.setVisible(replaceMode);
        replaceButton.setVisible(replaceMode);
    }

    /**
     * Get result properties of all Search TopComponent found
     *
     * @return
     */
    private List<Property> getSelection() {
        return SearchCommunicator.getResults(gedcom);
    }

    private void next() {
        resultsCurrent++;
        if (resultsCurrent > resultsTotal) {
            resultsCurrent = 0;
        }
        displayResult();
    }

    private void previous() {
        resultsCurrent--;
        if (resultsCurrent < 0) {
            resultsCurrent = resultsTotal;
        }
        displayResult();
    }

    private void replace() {
        try {
            if (gedcom.isWriteLocked()) {
                // do changes here
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        Property propToReplace = results.get(resultsCurrent);
                        String textFrom = comboFindText.getText();
                        Matcher m = match(propToReplace.getDisplayValue(), textFrom);
                        String textTo = comboReplaceText.getText();
                        Property parent = propToReplace.getParent();
                        if (parent instanceof PropertyName) {
                            replaceName((PropertyName) parent, propToReplace, m.replaceFirst(textTo));
                        } else {
                            propToReplace.setValue(m.replaceFirst(textTo));
                        }
                        log("ReplacePanel.replacingSingle", propToReplace, textFrom, textTo);
                    }

                });
            }

        } catch (GedcomException t) {
            LOG.log(Level.WARNING, "Error while replacing content in gedcom " + gedcom.getName(), t);
        } finally {
        }
    }

    private void replaceAll() {
        try {
            if (gedcom.isWriteLocked()) {
                // do changes here
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        for (Property propToReplace : results) {
                            String textFrom = comboFindText.getText();
                            Matcher m = match(propToReplace.getDisplayValue(), textFrom);
                            String textTo = comboReplaceText.getText();
                            Property parent = propToReplace.getParent();
                            if (parent instanceof PropertyName) {
                                replaceName((PropertyName) parent, propToReplace, m.replaceAll(textTo));
                            } else {
                                propToReplace.setValue(m.replaceAll(textTo));
                            }
                            log("ReplacePanel.replacingAll", propToReplace, textFrom, textTo);
                        }
                    }

                });
            }

        } catch (GedcomException t) {
            LOG.log(Level.WARNING, "Error while replacing content in gedcom " + gedcom.getName(), t);
        } finally {
        }
    }

    /**
     * Replacing name using PropertyName is necessary to make sure UNDO/REDO
     * will be captured // setName(String nPfx, String first, String sPfx,
     * String last, String suff, boolean replaceAllLastNames) NPFX GIVN SPFX
     * SURN NSFX // setNick(String nick) NICK
     *
     * @param name
     * @param p
     * @param to
     */
    public void replaceName(PropertyName name, Property p, String to) {
        String tag = p.getTag();
        String npfx = "NPFX".equals(tag) ? to : name.getNamePrefix();
        String givn = "GIVN".equals(tag) ? to : name.getFirstName();
        String spfx = "SPFX".equals(tag) ? to : name.getSurnamePrefix();
        String surn = "SURN".equals(tag) ? to : name.getLastName();
        String nsfx = "NSFX".equals(tag) ? to : name.getSuffix();
        String nick = "NICK".equals(tag) ? to : name.getNick();
        name.setName(npfx, givn, spfx, surn, nsfx);
        name.setNick(nick);
    }

    public void cancel(Gedcom gedcom, int undoNb) {
        int nbChanges = gedcom.getUndoNb() - undoNb;
        if (!gedcom.isWriteLocked()) {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
        LOG.log(Level.INFO, gedcom.getName() + " - " + NbBundle.getMessage(getClass(), "ReplacePanel.canceled", nbChanges));
    }

    private void log(String key, Property property, String from, String to) {
        Entity entity = property.getEntity();
        if (entity != null) {
            String entityStr = property.getEntity().toString(true);
            LOG.log(Level.FINE, gedcom.getName() + " - "
                    + NbBundle.getMessage(getClass(), key,
                            from,
                            to,
                            entity.getTag() + ":" + entityStr.substring(0, Math.min(25, entityStr.length())),
                            property.getPath().getName() + ":" + property.getPath().toString()));
        }
    }

    private class FilterPopupListener implements PopupMenuListener {

        public FilterPopupListener() {
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            findResults(0);
        }
    }

    private class ComboListener implements DocumentListener {

        public ComboListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            findResults(0);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            findResults(0);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            findResults(0);
        }
    }

    private class Category {

        private String id = "";
        private String name = "";
        private String[] tags = null;
        private ImageIcon icon = null;
        private int volume = 0;
        private boolean includeUserDefined = false;
        private boolean isSelected = false;

        public Category(String name, ImageIcon icon, String[] tags) {
            this.id = name;
            this.name = NbBundle.getMessage(getClass(), "ReplacePanel.findreplace_categ_" + name);
            this.icon = icon;
            this.tags = tags;
            for (String t : tags) {
                if (t.startsWith("_")) {
                    includeUserDefined = true;
                    break;
                }
            }
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public ImageIcon getIcon() {
            return icon;
        }

        public void reset() {
            volume = 0;
        }

        public void incrementVolume() {
            volume++;
        }

        public int getVolume() {
            return volume;
        }

        public void setSelected(boolean set) {
            isSelected = set;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public boolean contains(String tag) {
            if (tag.startsWith("_") && includeUserDefined) {
                return true;
            }

            for (String t : tags) {
                if (t.equals(tag)) {
                    return true;
                }
            }
            return false;
        }

    }

    private class CategoryComparator implements Comparator<Category> {

        @Override
        public int compare(Category c1, Category c2) {
            return c1.getName().compareTo(c2.getName());
        }

    }

}
