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
package ancestris.modules.gedcom.searchduplicates;

import ancestris.modules.gedcom.utilities.matchers.MatcherOptionsPanel;
import ancestris.modules.gedcom.utilities.matchers.MatcherOptions;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class SearchDuplicatesPanel extends javax.swing.JPanel {

    private Registry registry = null;
    private Gedcom gedcom = null;
    private JButton OKButton = null;
    
    private TreeMap<String, JCheckBox> entitiesChoices = new TreeMap<String, JCheckBox>();
    private TreeMap<String, JButton> entitiesButtons = new TreeMap<String, JButton>();
    private TreeMap<String, MatcherOptions> selectedOptions = new TreeMap<String, MatcherOptions>();
    
    /**
     * Creates new form SearchDuplicatesPanel
     */
    public SearchDuplicatesPanel(Gedcom gedcom, JButton OKButton) {
        this.gedcom = gedcom;
        this.OKButton = OKButton;
        registry = Registry.get(getClass());
        
        initComponents();
        
        this.setPreferredSize(new Dimension(registry.get("duplicateWindowWidth", this.getPreferredSize().width), registry.get("duplicateWindowHeight", this.getPreferredSize().height)));
        
        //INDI, FAM, OBJE, NOTE, SOUR, SUBM, REPO
        entitiesChoices.put(Gedcom.INDI, jCheckBox1); 
        entitiesChoices.put(Gedcom.FAM, jCheckBox2);
        entitiesChoices.put(Gedcom.SUBM, jCheckBox3);
        entitiesChoices.put(Gedcom.REPO, jCheckBox4);
        entitiesChoices.put(Gedcom.SOUR, jCheckBox5);
        entitiesChoices.put(Gedcom.NOTE, jCheckBox6);
        entitiesChoices.put(Gedcom.OBJE, jCheckBox7);
        
        entitiesButtons.put(Gedcom.INDI, jButton1);
        entitiesButtons.put(Gedcom.FAM, jButton2);
        entitiesButtons.put(Gedcom.SUBM, jButton3);
        entitiesButtons.put(Gedcom.REPO, jButton4);
        entitiesButtons.put(Gedcom.SOUR, jButton5);
        entitiesButtons.put(Gedcom.NOTE, jButton6);
        entitiesButtons.put(Gedcom.OBJE, jButton7);
        
        for (String entityTag : entitiesChoices.keySet()) {
            JCheckBox cb = entitiesChoices.get(entityTag);
            int size = gedcom.getEntities(entityTag).size();
            cb.setText(Gedcom.getName(entityTag) + " (" + size + ")");
            cb.setEnabled(size > 0);
            cb.setSelected(size > 0 && entityTag.equals(Gedcom.INDI));
            JButton b = entitiesButtons.get(entityTag);
            b.setText(NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton.text"));
            b.setEnabled(size > 0);
            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setButton();
                }
            });
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openCriteria((JButton) e.getSource());
                }
            });
        }
        
        setButton();
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
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton5 = new javax.swing.JButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton6 = new javax.swing.JButton();
        jCheckBox3 = new javax.swing.JCheckBox();
        jButton7 = new javax.swing.JButton();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jScrollPane2.setBorder(null);

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(4);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(null);
        jScrollPane2.setViewportView(jTextArea1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jCheckBox1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton5, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jCheckBox2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton6, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox3, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jCheckBox3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton7, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox4, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jCheckBox4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox5, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jCheckBox5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox6, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jCheckBox6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox7, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jCheckBox7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(SearchDuplicatesPanel.class, "SearchDuplicatesPanel.jButton2.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox2)
                                    .addComponent(jCheckBox1)
                                    .addComponent(jCheckBox3)
                                    .addComponent(jCheckBox6)
                                    .addComponent(jCheckBox7)
                                    .addComponent(jCheckBox4)
                                    .addComponent(jCheckBox5))
                                .addGap(40, 40, 40)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton3)
                                    .addComponent(jButton4)
                                    .addComponent(jButton5)
                                    .addComponent(jButton6)
                                    .addComponent(jButton7)
                                    .addComponent(jButton1)
                                    .addComponent(jButton2))))
                        .addGap(5, 5, 5)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox2)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox3)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox4)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox5)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox6)
                    .addComponent(jButton6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox7)
                    .addComponent(jButton7))
                .addContainerGap())
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("duplicateWindowWidth", evt.getComponent().getWidth());
        registry.put("duplicateWindowHeight", evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables


    public List<String> getEntitiesToCheck() {
        List<String> ret = new ArrayList<String>();
        for (String entityTag : entitiesChoices.keySet()) {
            JCheckBox cb = entitiesChoices.get(entityTag);
            if (cb.isSelected()) {
                ret.add(entityTag);
            }
        }
        return ret;
    }

    public TreeMap<String, MatcherOptions> getSelectedOptions() {
        for (String entityTag : entitiesChoices.keySet()) {
            JCheckBox cb = entitiesChoices.get(entityTag);
            if (cb.isSelected()) {
                selectedOptions.put(entityTag, new MatcherOptionsPanel(entityTag).getMatcherOptions());
            }
        }
        return selectedOptions;
    }

    private void setButton() {
        boolean activated = false;
        for (JCheckBox cb : entitiesChoices.values()) {
            activated |= cb.isSelected();
        }
        OKButton.setEnabled(activated);
    }

    private void openCriteria(JButton b) {
        for (String entityTag : entitiesButtons.keySet()) {
            JButton readB = entitiesButtons.get(entityTag);
            if (b.equals(readB)) {
                MatcherOptionsPanel panel = new MatcherOptionsPanel(entityTag);
                Object o = DialogManager.create(NbBundle.getMessage(MatcherOptionsPanel.class, "CTL_MatcherOptionsTitle"), panel)
                        .setMessageType(DialogManager.PLAIN_MESSAGE)
                        .setOptions(new Object[]{DialogManager.OK_OPTION, DialogManager.CANCEL_OPTION})
                        .show();
                if (o != DialogManager.OK_OPTION) {
                    return;
                }
                selectedOptions.put(entityTag, panel.getMatcherOptions());
            }
        }
    }

}
