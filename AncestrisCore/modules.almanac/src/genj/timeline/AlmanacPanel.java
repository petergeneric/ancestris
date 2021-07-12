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
package genj.timeline;

import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import genj.almanac.Almanac;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import org.apache.commons.io.FileUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class AlmanacPanel extends javax.swing.JPanel {

    private final Almanac almanac;
    private final TimelineView view;
    private final TimelineViewSettings.Commit commit;

    private final DefaultListModel almanacModel, categoriesModel;
    private final SpinnerNumberModel spinmodel;
    public static int MAX_SIG = 9;
    public static int MIN_SIG = 0;
    
    private boolean allCheckedAlms = true;
    private boolean allCheckedCats = true;

    
    /**
     * Creates new form almanacPanel
     */
    public AlmanacPanel(Almanac almanac, TimelineView view, TimelineViewSettings.Commit commit) {

        this.almanac = almanac;
        this.view = view;
        this.commit = commit;
        
        // Almanacs & categories
        almanacModel = new DefaultListModel();
        categoriesModel = new DefaultListModel();
        reloadAlmanacs();
        reloadCategories();


        int value = Math.min(MAX_SIG, view.getAlmanacSigLevel());
        spinmodel = new SpinnerNumberModel(value, MIN_SIG, MAX_SIG, 1);
        
        initComponents();
        
        cbAllAlms.setSelected(allCheckedAlms);
        cbAllCats.setSelected(allCheckedCats);
        almList.setCellRenderer(new CheckBoxesListCellrenderer());
        catList.setCellRenderer(new CheckBoxesListCellrenderer());
        sigSpinner.addChangeListener(commit);
        cbAllAlms.addChangeListener(commit);
        cbAllCats.addChangeListener(commit);
    }
    
    private void reloadAlmanacs() {
        Collator comparator = view.getCollator();
        comparator.setStrength (Collator.PRIMARY);
        
        almanacModel.removeAllElements();
        List<String> alms = almanac.getAlmanacs();
        Collections.sort(alms, comparator);
        List<String> selectedAlm = view.getAlmanacList();
        for (String alm : alms) {
            JCheckBox cb = new JCheckBox(alm, selectedAlm.contains(alm));
            if (!cb.isSelected()) {
                allCheckedAlms = false;
            }
            cb.addChangeListener(commit);
            almanacModel.addElement(cb);
        }
        
        if (almList != null) {
            almList.setModel(almanacModel);
        }
        
    }
    
    private void reloadCategories() {
        Collator comparator = view.getCollator();
        comparator.setStrength (Collator.PRIMARY);
        
        List<String> selectedAlm = new ArrayList<>(getCheckedAlmanacs());
        
        categoriesModel.removeAllElements();
        Set<String> set = new HashSet<>();
        selectedAlm.forEach((alm) -> {
            set.addAll(almanac.getCategories(alm));
        });
        List<String> cats = new ArrayList<>(set);
        Collections.sort(cats, comparator);
        List<String> selectedCat = view.getAlmanacCategories();
        for (String cat : cats) {
            JCheckBox cb = new JCheckBox(cat, selectedCat.contains(cat));
            if (!cb.isSelected()) {
                allCheckedCats = false;
            }
            cb.addChangeListener(commit);
            categoriesModel.addElement(cb);
        }
        
        if (catList != null) {
            catList.setModel(categoriesModel);
        }
        
    }

    private void checkAllAlms() {
        for (int i = 0 ; i < almanacModel.getSize() ; i++) {
            JCheckBox cb = (JCheckBox) almanacModel.getElementAt(i);
            cb.removeChangeListener(commit);
            cb.setSelected(cbAllAlms.isSelected());
            cb.addChangeListener(commit);
        }
        almList.setModel(almanacModel);
    }
    
    private void manageAlmAllCheck() {
        boolean checked = true;
        for (int i = 0 ; i < almanacModel.getSize() ; i++) {
            JCheckBox cb = (JCheckBox) almanacModel.getElementAt(i);
            if (!cb.isSelected()) {
                checked = false;
            }
        }
        cbAllAlms.setSelected(checked);
    }
    
    private void checkAllCats() {
        for (int i = 0 ; i < categoriesModel.getSize() ; i++) {
            JCheckBox cb = (JCheckBox) categoriesModel.getElementAt(i);
            cb.removeChangeListener(commit);
            cb.setSelected(cbAllCats.isSelected());
            cb.addChangeListener(commit);
        }
        catList.setModel(categoriesModel);
    }
    
    private void manageCatAllCheck() {
        boolean checked = true;
        for (int i = 0 ; i < categoriesModel.getSize() ; i++) {
           JCheckBox cb = (JCheckBox) categoriesModel.getElementAt(i);
           if (!cb.isSelected()) {
                checked = false;
            }
        }
        cbAllCats.setSelected(checked);
    }
    
    
    public Set<String> getCheckedAlmanacs() {
        Set<String> ret = new HashSet<>();
        for (int i = 0 ; i < almanacModel.getSize() ; i++) {
            JCheckBox cb = (JCheckBox) almanacModel.getElementAt(i);
            if (cb.isSelected()) {
                ret.add(cb.getText());
            }
        }
        return ret;
    }

    public Set<String> getCheckedCategories() {
        Set<String> ret = new HashSet<>();
        for (int i = 0 ; i < categoriesModel.getSize() ; i++) {
            JCheckBox cb = (JCheckBox) categoriesModel.getElementAt(i);
            if (cb.isSelected()) {
                ret.add(cb.getText());
            }
        }
        return ret;
    }

    public int getAlmanacSigLevel() {
        return spinmodel.getNumber().intValue();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listLabel = new javax.swing.JLabel();
        cbAllAlms = new javax.swing.JCheckBox();
        listScrollPane = new javax.swing.JScrollPane();
        almList = new javax.swing.JList(almanacModel);
        catLabel = new javax.swing.JLabel();
        cbAllCats = new javax.swing.JCheckBox();
        catScrollPane = new javax.swing.JScrollPane();
        catList = new javax.swing.JList(categoriesModel);
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        sigLabel = new javax.swing.JLabel();
        sigSpinner = new javax.swing.JSpinner(spinmodel);

        org.openide.awt.Mnemonics.setLocalizedText(listLabel, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.listLabel.text")); // NOI18N

        cbAllAlms.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbAllAlms, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.cbAllAlms.text")); // NOI18N
        cbAllAlms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAllAlmsActionPerformed(evt);
            }
        });

        almList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        almList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                almListMouseClicked(evt);
            }
        });
        listScrollPane.setViewportView(almList);

        org.openide.awt.Mnemonics.setLocalizedText(catLabel, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.catLabel.text")); // NOI18N

        cbAllCats.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbAllCats, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.cbAllCats.text")); // NOI18N
        cbAllCats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAllCatsActionPerformed(evt);
            }
        });

        catList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                catListMouseClicked(evt);
            }
        });
        catScrollPane.setViewportView(catList);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sigLabel, org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.sigLabel.text")); // NOI18N

        sigSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.sigSpinner.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(listLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbAllAlms))
                    .addComponent(listScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(catScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sigLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sigSpinner))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(catLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbAllCats)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listLabel)
                    .addComponent(catLabel)
                    .addComponent(cbAllCats)
                    .addComponent(cbAllAlms))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(addButton))
                    .addComponent(catScrollPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeButton)
                    .addComponent(sigSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sigLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        File source = new FileChooserBuilder(AlmanacPanel.class.getCanonicalName()+"add")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.add"))
                .setApproveText(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.addButton"))
                .setDefaultExtension(FileChooserBuilder.getAlmanacFilter().getExtensions()[0])
                .setFileFilter(FileChooserBuilder.getAlmanacFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .showOpenDialog();
        if (source != null) {
            File dest = new File(almanac.getUserDir() + File.separator + source.getName());
            try {
                FileUtils.copyFile(source, dest);
                almanac.init();
                almanac.waitLoaded();
                reloadAlmanacs();
                reloadCategories();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        File file = new FileChooserBuilder(AlmanacPanel.class.getCanonicalName()+"remove")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setDefaultWorkingDirectory(almanac.getUserDir())
                .forceUseOfDefaultWorkingDirectory(true)
                .setTitle(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.remove"))
                .setApproveText(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.removeButton"))
                .setDefaultExtension(FileChooserBuilder.getAlmanacFilter().getExtensions()[0])
                .setFileFilter(FileChooserBuilder.getAlmanacFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .showOpenDialog();
        if (file != null) {
            Object o = DialogManager.create(NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.removeconfirm"),
                    NbBundle.getMessage(AlmanacPanel.class, "AlmanacPanel.title.removemsg", file.getName()))
                    .setMessageType(DialogManager.QUESTION_MESSAGE)
                    .setOptionType(DialogManager.YES_NO_OPTION)
                    .show();
            if (o == DialogManager.YES_OPTION) {
                file.delete();
                almanac.init();
                almanac.waitLoaded();
                reloadAlmanacs();
                reloadCategories();
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void almListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_almListMouseClicked
        int index = almList.locationToIndex(evt.getPoint());
        if (index != -1) {
            JCheckBox cb = (JCheckBox) almanacModel.get(index);
            cb.setSelected(!cb.isSelected());
            manageAlmAllCheck();
            almList.repaint();
            reloadCategories();
            checkAllCats();
            catList.repaint();
            commit.stateChanged(new ChangeEvent(cbAllCats));
        }

    }//GEN-LAST:event_almListMouseClicked

    private void catListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_catListMouseClicked
        int index = catList.locationToIndex(evt.getPoint());
        if (index != -1) {
            JCheckBox cb = (JCheckBox) categoriesModel.get(index);
            cb.setSelected(!cb.isSelected());
            manageCatAllCheck();
            catList.repaint();
        }
    }//GEN-LAST:event_catListMouseClicked

    private void cbAllAlmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAllAlmsActionPerformed
        checkAllAlms();
        reloadCategories();
        almList.repaint();
        cbAllCats.setSelected(true);
        checkAllCats();
        catList.repaint();
    }//GEN-LAST:event_cbAllAlmsActionPerformed

    private void cbAllCatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAllCatsActionPerformed
        checkAllCats();
        catList.repaint();
    }//GEN-LAST:event_cbAllCatsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList almList;
    private javax.swing.JLabel catLabel;
    private javax.swing.JList catList;
    private javax.swing.JScrollPane catScrollPane;
    private javax.swing.JCheckBox cbAllAlms;
    private javax.swing.JCheckBox cbAllCats;
    private javax.swing.JLabel listLabel;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel sigLabel;
    private javax.swing.JSpinner sigSpinner;
    // End of variables declaration//GEN-END:variables

    private class CheckBoxesListCellrenderer implements ListCellRenderer {

        public CheckBoxesListCellrenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JCheckBox cb = (JCheckBox) value;
            return cb;
        }
    }

}
