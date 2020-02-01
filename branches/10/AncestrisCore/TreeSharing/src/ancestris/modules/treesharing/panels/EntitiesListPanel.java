/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.treesharing.panels;

import ancestris.modules.treesharing.communication.EntityConversion;
import ancestris.modules.treesharing.options.TreeSharingOptionsPanel;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicComboBoxUI;
import org.apache.commons.lang.StringEscapeUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class EntitiesListPanel extends javax.swing.JPanel {

    private final Set<MatchData> list;
    private StringBuffer textToPaste;
    
    private final static int IMG_MEDIUM_WIDTH = 51;
    private final static int ITEMS_PER_PAGE = 50;

    private final ImageIcon DEFPROF_PHOTO = new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/nophoto.png"));
    private final static ImageIcon nophoto = new ImageIcon(ImageUtilities.loadImage("ancestris/modules/treesharing/resources/nophoto.png"));
    private final static ImageIcon ArrowButton = new ImageIcon(ImageUtilities.loadImage("ancestris/modules/treesharing/resources/dropdownarrow.png"));
    private final static ImageIcon AllMembers = new ImageIcon(ImageUtilities.loadImage("ancestris/modules/treesharing/resources/allMembers.png"));
    private final static String allGedcoms = NbBundle.getMessage(EntitiesListPanel.class, "STR_AllGedcoms");
    private final static String allMembers = NbBundle.getMessage(EntitiesListPanel.class, "STR_AllMembers");
    
    private SortedSet<String> myGedcoms = new TreeSet<String>();
    private SortedSet<String> memberGedcoms = new TreeSet<String>();
    private TreeMap<String, ImageIcon> members = new TreeMap<String, ImageIcon>();
    
    private String[] arrayMyGedcoms;
    private String[] arrayMemberGedcoms;
    private ImageIcon[] arrayMemberIcons;
    private String[] arrayMemberStrings;
    
    private TreeMap<String, MatchData> sortedMatches = new TreeMap<String, MatchData>();   //  String key is matchresult(asc)/mygedcomname(asc)/myEntityString(asc)
    private int currentPage = 0;
    private boolean busy = false;
    
    /**
     * Creates new form ListEntitiesPanel
     */
    public EntitiesListPanel(String gedcomName, String friend, Set<MatchData> list, String typeOfEntity) {
        this.list = list;
        this.textToPaste = new StringBuffer("");

        // Initialise with lists
        initComponents();
        resultScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        resultScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        photoOtherComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new JButton() {
                    @Override
                    public int getWidth() {
                        return 20;
                    }
                    @Override
                    public int getX() {
                        return IMG_MEDIUM_WIDTH+1;
                    }
                    @Override
                    public Icon getIcon() {
                        return ArrowButton;
                    }
                };
            }
        
        });
   
        // Set my picture
        pseudoMeLabel.setText(TreeSharingOptionsPanel.getPseudo());
        ImageIcon myPhoto = TreeSharingOptionsPanel.getProfile().photoBytes == null ? DEFPROF_PHOTO : TreeSharingOptionsPanel.getPhoto(2, TreeSharingOptionsPanel.getProfile().photoBytes); 
        photoMeLabel.setIcon(myPhoto);
        
        // Set checkboxes
        busy = true;
        indiCheckBox.setSelected(typeOfEntity.equals(Gedcom.INDI));
        famCheckBox.setSelected(typeOfEntity.equals(Gedcom.FAM));
        
        // Build comboboxes and select corresponding lines
        buildFilteredLists(gedcomName, friend);
        
        // Display matches corresponding to selections
        updatePanelDisplay();
        busy = false;
        
        // Set keyboard keys for the buttons
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "doFirst");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "doPrevious");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "doNext");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "doLast");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK), "doCopy");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "doCopy");
        Action firstAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstButton.isEnabled()) {
                    firstButton.requestFocusInWindow();
                    firstButtonActionPerformed(e);
                }
            }
        };
        Action previousAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (previousButton.isEnabled()) {
                    previousButton.requestFocusInWindow();
                    previousButtonActionPerformed(e);
                }
            }
        };
        Action nextAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nextButton.isEnabled()) {
                    nextButton.requestFocusInWindow();
                    nextButtonActionPerformed(e);
                }
            }
        };
        Action lastAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lastButton.isEnabled()) {
                    lastButton.requestFocusInWindow();
                    lastButtonActionPerformed(e);
                }
            }
        };
        Action copyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (copyButton.isEnabled()) {
                    copyButton.requestFocusInWindow();
                    copyButtonActionPerformed(e);
                }
            }
        };
        getActionMap().put("doFirst", firstAction);
        getActionMap().put("doPrevious", previousAction);
        getActionMap().put("doNext", nextAction);
        getActionMap().put("doLast", lastAction);
        getActionMap().put("doCopy", copyAction);

        // Page up and Down to scroll up and down
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "doPageUp");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "doPageDown");
        Action pageupAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultScrollPane.getVerticalScrollBar().setValue(resultScrollPane.getVerticalScrollBar().getValue()-60);
            }
        };
        getActionMap().put("doPageUp", pageupAction);
        Action pagedownAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultScrollPane.getVerticalScrollBar().setValue(resultScrollPane.getVerticalScrollBar().getValue()+60);
            }
        };
        getActionMap().put("doPageDown", pagedownAction);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        photoMeLabel = new javax.swing.JLabel();
        pseudoMeLabel = new javax.swing.JLabel();
        indiCheckBox = new javax.swing.JCheckBox();
        indiPictoLabel = new javax.swing.JLabel();
        gedcomMeComboBox = new javax.swing.JComboBox();
        famPictoLabel = new javax.swing.JLabel();
        famCheckBox = new javax.swing.JCheckBox();
        pseudoOtherLabel = new javax.swing.JLabel();
        gedcomOtherComboBox = new javax.swing.JComboBox();
        photoOtherComboBox = new javax.swing.JComboBox();
        resultScrollPane = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        firstButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        lastButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        pageLabel = new javax.swing.JLabel();
        totalLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(photoMeLabel, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.photoMeLabel.text")); // NOI18N
        photoMeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        photoMeLabel.setOpaque(true);

        org.openide.awt.Mnemonics.setLocalizedText(pseudoMeLabel, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.pseudoMeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(indiCheckBox, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.indiCheckBox.text")); // NOI18N
        indiCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.indiCheckBox.toolTipText")); // NOI18N
        indiCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiCheckBoxActionPerformed(evt);
            }
        });

        indiPictoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Indi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(indiPictoLabel, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.indiPictoLabel.text")); // NOI18N
        indiPictoLabel.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.indiPictoLabel.toolTipText")); // NOI18N

        gedcomMeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gedcomMeComboBoxActionPerformed(evt);
            }
        });

        famPictoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Fam.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(famPictoLabel, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.famPictoLabel.text")); // NOI18N
        famPictoLabel.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.famPictoLabel.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(famCheckBox, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.famCheckBox.text")); // NOI18N
        famCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.famCheckBox.toolTipText")); // NOI18N
        famCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                famCheckBoxActionPerformed(evt);
            }
        });

        pseudoOtherLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(pseudoOtherLabel, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.pseudoOtherLabel.text")); // NOI18N

        gedcomOtherComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gedcomOtherComboBoxActionPerformed(evt);
            }
        });

        photoOtherComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.photoOtherComboBox.toolTipText")); // NOI18N
        photoOtherComboBox.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        photoOtherComboBox.setMinimumSize(new java.awt.Dimension(51, 62));
        photoOtherComboBox.setPreferredSize(new java.awt.Dimension(51, 62));
        photoOtherComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                photoOtherComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));
        resultScrollPane.setViewportView(jPanel1);

        firstButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/first.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(firstButton, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.firstButton.text")); // NOI18N
        firstButton.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.firstButton.toolTipText")); // NOI18N
        firstButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstButtonActionPerformed(evt);
            }
        });

        previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/previous.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(previousButton, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.previousButton.text")); // NOI18N
        previousButton.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.previousButton.toolTipText")); // NOI18N
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/next.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(nextButton, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.nextButton.text")); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.nextButton.toolTipText")); // NOI18N
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        lastButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/last.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lastButton, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.lastButton.text")); // NOI18N
        lastButton.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.lastButton.toolTipText")); // NOI18N
        lastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastButtonActionPerformed(evt);
            }
        });

        copyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/Copy.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(copyButton, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.copyButton.text")); // NOI18N
        copyButton.setToolTipText(org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.copyButton.toolTipText")); // NOI18N
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(pageLabel, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.pageLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(totalLabel, org.openide.util.NbBundle.getMessage(EntitiesListPanel.class, "EntitiesListPanel.totalLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(photoMeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pseudoMeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                                .addComponent(indiCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(indiPictoLabel))
                            .addComponent(gedcomMeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(famPictoLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(famCheckBox)
                                .addGap(33, 33, 33)
                                .addComponent(pseudoOtherLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                            .addComponent(gedcomOtherComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(photoOtherComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(resultScrollPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pageLabel)
                            .addComponent(totalLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(firstButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previousButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(copyButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(photoMeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(photoOtherComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(famCheckBox)
                            .addComponent(famPictoLabel)
                            .addComponent(indiCheckBox)
                            .addComponent(indiPictoLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gedcomMeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gedcomOtherComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pseudoOtherLabel)
                            .addComponent(pseudoMeLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(copyButton)
                        .addComponent(lastButton)
                        .addComponent(nextButton)
                        .addComponent(previousButton)
                        .addComponent(firstButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(totalLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pageLabel)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void photoOtherComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_photoOtherComboBoxActionPerformed
        if (!busy) {
            updatePanelDisplay();
        }
    }//GEN-LAST:event_photoOtherComboBoxActionPerformed

    private void gedcomMeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gedcomMeComboBoxActionPerformed
        if (!busy) {
            updatePanelDisplay();
        }
    }//GEN-LAST:event_gedcomMeComboBoxActionPerformed

    private void gedcomOtherComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gedcomOtherComboBoxActionPerformed
        if (!busy) {
            updatePanelDisplay();
        }
    }//GEN-LAST:event_gedcomOtherComboBoxActionPerformed

    private void indiCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiCheckBoxActionPerformed
        if (!busy) {
            updatePanelDisplay();
        }
    }//GEN-LAST:event_indiCheckBoxActionPerformed

    private void famCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_famCheckBoxActionPerformed
        if (!busy) {
            updatePanelDisplay();
        }
    }//GEN-LAST:event_famCheckBoxActionPerformed

    private void firstButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstButtonActionPerformed
        currentPage = 0;
        displayPage();
    }//GEN-LAST:event_firstButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        currentPage--;
        if (currentPage < 0) {
            currentPage = 0;
        }
        displayPage();
    }//GEN-LAST:event_previousButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        currentPage++;
        if (currentPage > getMaxPageNb()) {
            currentPage = getMaxPageNb();
        }
        displayPage();        
    }//GEN-LAST:event_nextButtonActionPerformed

    private void lastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastButtonActionPerformed
        currentPage = getMaxPageNb();
        displayPage();                
    }//GEN-LAST:event_lastButtonActionPerformed

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        String str = textToPaste.toString();
        StringSelection stringSelection = new StringSelection(str);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }//GEN-LAST:event_copyButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton copyButton;
    private javax.swing.JCheckBox famCheckBox;
    private javax.swing.JLabel famPictoLabel;
    private javax.swing.JButton firstButton;
    private javax.swing.JComboBox gedcomMeComboBox;
    private javax.swing.JComboBox gedcomOtherComboBox;
    private javax.swing.JCheckBox indiCheckBox;
    private javax.swing.JLabel indiPictoLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton lastButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel pageLabel;
    private javax.swing.JLabel photoMeLabel;
    private javax.swing.JComboBox photoOtherComboBox;
    private javax.swing.JButton previousButton;
    private javax.swing.JLabel pseudoMeLabel;
    private javax.swing.JLabel pseudoOtherLabel;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JLabel totalLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Build comboboxes and make selections
     * @param gedcomName
     * @param friend
     * @param memberGedcomName 
     */
    
    private void buildFilteredLists(String gedcomName, String friend) {

        // Clear lists
        myGedcoms.clear();
        members.clear();
        memberGedcoms.clear();

        // Add generic elements
        myGedcoms.add(allGedcoms);
        members.put(allMembers, AllMembers);
        memberGedcoms.add(allGedcoms);
        
        // Fill in with filtered criteria
        String iGedcomName = "";
        String iFriend = "";
        String iMemberGedcomName = "";
        for (MatchData line : list) {
            iGedcomName = line.myEntity.getGedcom().getName();
            if (!myGedcoms.contains(iGedcomName)) {
                myGedcoms.add(iGedcomName);
            }
            iFriend = line.friendGedcomEntity.friend;
            if (!members.containsKey(iFriend)) {
                AncestrisFriend af = line.friendGedcomEntity.afriend;
                ImageIcon icon = (af != null ? (af.getFriendProfile() != null ? TreeSharingOptionsPanel.getPhoto(2, af.getFriendProfile().photoBytes) : null) : null);
                if (icon == null) {
                    icon = nophoto;
                }
                members.put(iFriend, icon);
            }
            iMemberGedcomName = line.friendGedcomEntity.gedcomName;
            if (!memberGedcoms.contains(iMemberGedcomName)) {
                memberGedcoms.add(iMemberGedcomName);
            }
        }
        
        // Overwrite arrays
        arrayMyGedcoms = myGedcoms.toArray(new String[myGedcoms.size()]);
        arrayMemberStrings = members.keySet().toArray(new String[members.keySet().size()]);
        arrayMemberIcons = members.values().toArray(new ImageIcon[members.values().size()]);
        arrayMemberGedcoms = memberGedcoms.toArray(new String[memberGedcoms.size()]);

        // update comboboxes
        gedcomMeComboBox.setModel(new javax.swing.DefaultComboBoxModel(arrayMyGedcoms));
        photoOtherComboBox.setModel(new javax.swing.DefaultComboBoxModel(arrayMemberIcons));
        gedcomOtherComboBox.setModel(new javax.swing.DefaultComboBoxModel(arrayMemberGedcoms));
        
        // Make selecitons
        gedcomMeComboBox.setSelectedItem(gedcomName);
        photoOtherComboBox.setSelectedItem(members.get(friend) == null ? AllMembers : members.get(friend));
        gedcomOtherComboBox.setSelectedItem(allGedcoms);
        
    }


    private boolean match(String type, String gedcomName, String friend, String memberGedcomName, String iType, String iGedcomName, String iFriend, String iMemberGedcomName) {

        String gn = gedcomName, f= friend, mgn = memberGedcomName;

        if (type == null || type.isEmpty()) {
            type = iType;
        }
        if (gn == null || gn.isEmpty() || gn.equals(allGedcoms)) {
            gn = iGedcomName;
        }
        if (f == null || f.isEmpty() || f.equals(allMembers)) {
            f = iFriend;
        }
        if (mgn == null || mgn.isEmpty() || mgn.equals(allGedcoms)) {
            mgn = iMemberGedcomName;
        }
        
        return (type.equals(iType) && gn.equals(iGedcomName) && f.equals(iFriend) && mgn.equals(iMemberGedcomName));
    }



    /**
     * Extract all matches corresponding to selections (entity type, sharedGedcom, friend's gedcom, friend)
     */
    private void updatePanelDisplay() {

        sortedMatches.clear();
        
        // Set label
        pseudoOtherLabel.setText(arrayMemberStrings[photoOtherComboBox.getSelectedIndex()]);

        // Get criteria
        String type = "";
        if (indiCheckBox.isSelected() && !famCheckBox.isSelected()) type = Gedcom.INDI;
        if (!indiCheckBox.isSelected() && famCheckBox.isSelected()) type = Gedcom.FAM;
        String gedcomName = arrayMyGedcoms[gedcomMeComboBox.getSelectedIndex()];
        String friend = arrayMemberStrings[photoOtherComboBox.getSelectedIndex()];
        String memberGedcomName = arrayMemberGedcoms[gedcomOtherComboBox.getSelectedIndex()];
        
        // Scan list and build sorted maps
        String key = "";
        String iType = "";
        String iGedcomName = "";
        String iFriend = "";
        String iMemberGedcomName = "";
        for (MatchData line : list) {
            iType = line.myEntity instanceof Indi ? Gedcom.INDI : Gedcom.FAM;
            iGedcomName = line.myEntity.getGedcom().getName();
            iFriend = line.friendGedcomEntity.friend;
            iMemberGedcomName = line.friendGedcomEntity.gedcomName;
            if (match(type, gedcomName, friend, memberGedcomName, iType, iGedcomName, iFriend, iMemberGedcomName)) {
                key = line.matchResult + "-" + line.myEntity.getGedcom().getName() + "-" + line.myEntity.getId() + "-" + iFriend+ "-" + iGedcomName + "-" + line.friendGedcomEntity.entityID;
                sortedMatches.put(key, line);
            }
        }
        
        // Display sortedMap by page of 50
        currentPage = 0;
        displayPage();

    }

    private List<String> getPageKeys() {
        List<String> ret = new ArrayList<String>();
        int item = 0;
        for (String key : sortedMatches.keySet()) {
            if ((item / ITEMS_PER_PAGE) == currentPage) {
                ret.add(key);
                if (ret.size() == ITEMS_PER_PAGE) {
                    break;
                }
            }
            item++;
        }
        return ret;
    }

    private void displayPage() {
        textToPaste.delete(0, textToPaste.length());
        jPanel1.removeAll();
        jPanel1.repaint();
        updateButtons();
        if (sortedMatches.isEmpty()) {
            return;
        }
        
        BoxLayout layout = new BoxLayout(jPanel1, BoxLayout.PAGE_AXIS);
        jPanel1.setLayout(layout);
        String group = "", strItem = "", str = "";
        Entity currentEntity = null;
        List<MatchData> subList = new LinkedList<MatchData>();
        int i = 0;
        for (String key : getPageKeys()) {
            MatchData line = sortedMatches.get(key);
            textToPaste.append(convertToText(line));
            strItem = EntityConversion.getStringFromEntity(line.myEntity, false);
            if (!group.equals(strItem)) { // Group break
                if (i == 0) {               // Start of list
                    group = strItem;
                    currentEntity = line.myEntity;
                } else {                    // End of previous group
                    addEntityBloc(currentEntity, subList);
                    group = strItem;
                    currentEntity = line.myEntity;
                }
                i = 0;
                subList.clear();
            }
            i++;
            subList.add(line);
        }
        if (i != 0) {
            addEntityBloc(currentEntity, subList);
        }
        jPanel1.repaint();
        jPanel1.validate();
    }
    
    
    private void addEntityBloc(Entity currentEntity, List<MatchData> subList) {
        EntityBean bean = new EntityBean(currentEntity, subList);
        jPanel1.add(bean);
    }

    private StringBuffer convertToText(MatchData line) {
        StringBuffer sb = new StringBuffer("");
        sb.append(line.myEntity.getGedcom().getName());
        sb.append("\t");
        sb.append(StringEscapeUtils.unescapeHtml(EntityConversion.getStringFromEntity(line.myEntity, false)));
        sb.append("\t");
        sb.append(StringEscapeUtils.unescapeHtml(EntityConversion.getStringFromEntity(line.friendGedcomEntity, false)));
        sb.append("\t");
        sb.append(line.friendGedcomEntity.gedcomName);
        sb.append("\t");
        sb.append(line.friendGedcomEntity.friend);
        sb.append("\t");
        sb.append(line.matchResult);
        sb.append("\n");
        return sb;
    }

    private void updateButtons() {
        totalLabel.setText(NbBundle.getMessage(EntitiesListPanel.class, "totalMatch", sortedMatches.size(), ITEMS_PER_PAGE));
        pageLabel.setText(NbBundle.getMessage(EntitiesListPanel.class, "currentPage", currentPage+1, getMaxPageNb()+1));  // humans count starting from 1
        boolean first = currentPage == 0;
        boolean listNotEmpty = !sortedMatches.isEmpty();
        boolean last = currentPage == getMaxPageNb();
        copyButton.setEnabled(listNotEmpty);
        firstButton.setEnabled(!first);
        previousButton.setEnabled(!first);
        nextButton.setEnabled(!last);
        lastButton.setEnabled(!last);
    }

    private int getMaxPageNb() {
        return sortedMatches.size() / ITEMS_PER_PAGE;
    }

}
