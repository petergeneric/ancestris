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
package modules.editors.gedcomproperties.utils;

import ancestris.util.swing.FileChooserBuilder;
import genj.gedcom.PropertyFile;
import genj.io.input.FileInput;
import genj.util.Registry;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class SearchFilePanel extends javax.swing.JPanel {

    private Registry registry = null;

    private String currentPath = "";
    private PathData pd = null;

    private DefaultListModel<File> filesModel = null;
    private Set<String> filesNames = null;
    private DefaultListModel<String> pathsModel = null;
    
    private boolean isBusy = false;

    /**
     * Creates new form SearchFilePanel
     * @param pd
     * @param currentPath
     */
    public SearchFilePanel(PathData pd, String currentPath) {
        this.pd = pd;
        this.currentPath = currentPath;

        filesModel = new DefaultListModel();
        filesNames = new TreeSet<>();
        pathsModel = new DefaultListModel();
        getFiles();

        registry = Registry.get(getClass());

        initComponents();

        filesList.setCellRenderer(new FileCellRenderer());
        jTextField1.setText(currentPath);
        
        recalc();
        
        this.setPreferredSize(new Dimension(registry.get("searchFilePanelWidth", this.getPreferredSize().width), registry.get("searchFilePanelHeight", this.getPreferredSize().height)));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        filesList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        pathsList = new javax.swing.JList();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel1.text")); // NOI18N

        jLabel2.setForeground(new java.awt.Color(0, 102, 51));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel2.text")); // NOI18N

        jLabel3.setForeground(new java.awt.Color(204, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel3.text")); // NOI18N

        filesList.setModel(filesModel);
        jScrollPane1.setViewportView(filesList);

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel4.text")); // NOI18N

        jTextField1.setEditable(false);
        jTextField1.setText(org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton1.toolTipText")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton2.toolTipText")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton3.text")); // NOI18N
        jButton3.setToolTipText(org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton3.toolTipText")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel5.text")); // NOI18N

        pathsList.setModel(pathsModel);
        pathsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pathsListMouseClicked(evt);
            }
        });
        pathsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                pathsListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(pathsList);

        jLabel6.setForeground(new java.awt.Color(204, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel6.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator2)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jTextField1.setText(currentPath);
        recalc();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jTextField1.setText((String) pathsList.getSelectedValue());
        recalc();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String defaultDir = jTextField1.getText();
        if (defaultDir.isEmpty()) {
            defaultDir = System.getProperty("user.home");
        }
        File file = new FileChooserBuilder(SearchFilePanel.class)
                .setDirectoriesOnly(false)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(SearchFilePanel.class, "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(SearchFilePanel.class, "FileChooserButton"))
                .setDefaultExtension(FileChooserBuilder.getGedcomFilter().getExtensions()[0])
                .setFileFilter(null)
                .setAcceptAllFileFilterUsed(true)
                .setFileHiding(true)
                .setParent(this)
                .setDefaultPreviewer()
                .setSelectedFile(new File(defaultDir))
                .setDefaultWorkingDirectory(new File(defaultDir))
                .showOpenDialog();
        if (file != null) {
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            jTextField1.setText(file.getAbsolutePath());
            recalc();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void pathsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_pathsListValueChanged
        updateSelection();
    }//GEN-LAST:event_pathsListValueChanged

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("searchFilePanelWidth", evt.getComponent().getWidth());
        registry.put("searchFilePanelHeight", evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized

    private void pathsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pathsListMouseClicked
        JList list = (JList)evt.getSource();
        if (evt.getClickCount() == 2) {
            //int index = list.locationToIndex(evt.getPoint());
            jTextField1.setText((String) pathsList.getSelectedValue());
            recalc();
        }
    }//GEN-LAST:event_pathsListMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList filesList;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JList pathsList;
    // End of variables declaration//GEN-END:variables

    /**
     * Get files to be looked for into the model and into list of file names
     *
     * @param pd
     */
    private void getFiles() {
        filesModel.clear();
        for (PropertyFile file : pd.getFiles()) {
            File f = file.getInput().isPresent()?((FileInput) file.getInput().get()).getFile() : new File(file.getValue());
            filesModel.addElement(f);
            filesNames.add(f.getName());
        }

    }

    
    private void updateSelection() {
        boolean selection = !pathsList.isSelectionEmpty();
        jButton2.setToolTipText(NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton2.toolTipText", selection ? (String) pathsList.getSelectedValue() : ""));
        jButton2.setEnabled(selection);
    }
    
    public String getNewPath() {
        String path = jTextField1.getText();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }
    
    /**
     * Recalculate the two lists 
     * 1/ List of files to be found : refresh colors
     * depending on currently suggested path displayed; Green = found ; red =
     * unfound 
     * 2/ Search recursively from path all forlders that include at
     * least one of the searched files
     */
    private void recalc() {
        
        if (isBusy) {
            return;
        }
        isBusy = true;
        
        // Labels
        File dir = new File(jTextField1.getText());
        jLabel6.setVisible(!dir.exists());
        int total = filesModel.getSize();
        int found = 0;
        for (int i = 0 ; i < filesModel.getSize() ; i++) {
            File f = (File) filesModel.get(i);
            found += testFound(f) ? 1 : 0;
        }
        int unfound = total - found;
        
        jLabel1.setText(NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel1.text", total));
        jLabel2.setText(NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel2.text", found));
        jLabel2.setVisible(found > 0);
        jLabel3.setText(NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jLabel3.text", unfound));
        jLabel3.setVisible(unfound > 0);
        
        // Refresh files list
        jScrollPane1.repaint();
        
        // Research path list
        pathsModel.clear();
        Set<String> paths = findPaths(jTextField1.getText());
        for (String path : paths) {
            pathsModel.addElement(path);
        }
        pathsList.setModel(pathsModel);
        
        // Buttons
        jButton1.setToolTipText(NbBundle.getMessage(SearchFilePanel.class, "SearchFilePanel.jButton1.toolTipText", currentPath));
        updateSelection();
        
        isBusy = false;
    }

    
    
    private boolean testFound(File fSource) {
        File fTested = new File(jTextField1.getText() + File.separator + fSource.getName());
        return fTested.exists();
    }
    
    private Set<String> findPaths(String root) {
        Set<String> ret = new TreeSet<String>();
        File dir = null;
        
        // Search from root of all names
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        File currentDir = new File(root);
        Stack<File> dirs = new Stack<File>();
        dirs.push(currentDir);

        try {

            // Loops
            do {
                dir = dirs.pop();
                if (dir == null || !dir.canRead() || dir.isHidden()) {
                    continue;
                }
                for (File f : dir.listFiles()) {
                    if (f.isDirectory()) {
                        dirs.push(f);
                    } else {
                        for (String name : filesNames) {
                            if (f.getName().equals(name)) {
                                ret.add(f.getParentFile().getCanonicalPath());
                            }
                        }

                    }
                }
            } while (!dirs.isEmpty());

        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        return ret;
}    

    
    
    
    

    
    
    private class FileCellRenderer extends JLabel implements ListCellRenderer<Object> {

        private Color foreground_found = new Color(55, 107, 53);
        private Color foreground_unfound = new Color(200, 0, 0);

        public FileCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (value == null || !(value instanceof File)) {
                return this;
            }
            
            if (isSelected) {
                setBackground(Color.white);
            } else {
                setBackground(list.getBackground());
            }
            File fSource = (File) value;
            setForeground(testFound(fSource) ? foreground_found : foreground_unfound);

            setText(fSource.getName());
            setIcon(null);
            
            return this;
        }

    }

}
