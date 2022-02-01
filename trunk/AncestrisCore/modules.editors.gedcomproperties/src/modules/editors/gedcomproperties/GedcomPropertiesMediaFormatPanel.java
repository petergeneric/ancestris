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
package modules.editors.gedcomproperties;

import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.util.Registry;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import modules.editors.gedcomproperties.utils.MediaManagerPanel;
import modules.editors.gedcomproperties.utils.RemoteMediaManagerPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class GedcomPropertiesMediaFormatPanel extends javax.swing.JPanel implements Constants {

    private Registry registry = null;
    private final String winWidth = "gedcomProperties5Width";
    private final String winHeight = "gedcomProperties5Height";

    private Map<PropertyFile, String> property2PathMap = null;  // will store the remapping of the unfound files (and potentially the found ones)
    private List<? extends Property> filesList = null;
    private int differentMediaFiles = 0;
    private int totalMediaFiles = 0;
    private Set<String> filesFullnames = null;  // counts different media files
    private int nbOfPaths = 0;
    private Set<String> filesPaths = null;      // lists different path names
    private DefaultListModel model = null;
    private String rootPath = "";
    private int totalPathsFound = 0;
    private int absPathsFound = 0;
    private int relPathsFound = 0;
    private String unfoundColor = "'black'";
    private int totalPathsUnfound = 0;
    private int absPathsUnfound = 0;
    private int relPathsUnfound = 0;
    private int remotePathsFound = 0;
    private Set<PropertyFile> remotePaths = null;

    public final static Pattern ABSOLUTE = Pattern.compile("([a-z]:).*|([A-Z]:).*|\\/.*|\\\\.*");

    /**
     * Creates new form GedcomPropertiesMediaFormatPanel
     */
    public GedcomPropertiesMediaFormatPanel(Gedcom gedcom) {

        rootPath = gedcom.getOrigin().getFile().getParentFile().getAbsolutePath();

        filesFullnames = new TreeSet<>();
        filesPaths = new TreeSet<>();
        remotePaths = new TreeSet<>();

        filesList = gedcom.getPropertiesByClass(PropertyFile.class);
        initFilesMap();

        // Calculate key figures and build map along the way
        totalMediaFiles = filesList.size();
        for (Property file : filesList) {
            if (file instanceof PropertyFile) {

                // Count new file value
                String value = file.getValue();
                if (value == null || value.isEmpty() || filesFullnames.contains(value)) {
                    continue;
                }
                filesFullnames.add(value);

                // Check existence
                PropertyFile pFile = (PropertyFile) file;
                if (pFile.isIsRemote()) {
                    remotePathsFound ++;
                    remotePaths.add(pFile);
                    continue;
                }
                              
                File f = gedcom.getOrigin().getFile(value);
                String path = value.substring(0, value.indexOf(f.getName()));
                filesPaths.add(path);
                if (f.exists()) {
                    totalPathsFound++;
                    if (ABSOLUTE.matcher(path).matches()) {
                        absPathsFound++;
                    } else {
                        relPathsFound++;
                    }
                } else {
                    totalPathsUnfound++;
                    if (ABSOLUTE.matcher(path).matches()) {
                        absPathsUnfound++;
                    } else {
                        relPathsUnfound++;
                    }
                }
            }
        }
        if (totalPathsUnfound > 0) {
            unfoundColor = "'red'";
        }
        differentMediaFiles = filesFullnames.size();

        model = new DefaultListModel();
        for (String str : filesPaths) {
            model.addElement(str);
        }
        nbOfPaths = filesPaths.size();

        initComponents();

        jLabel2.setVisible(false);
        jButton2.setVisible(false);
        if (totalMediaFiles == 0) {
            jLabel3.setEnabled(false);
            jLabel4.setEnabled(false);
            jLabel5.setEnabled(false);
            jLabel6.setEnabled(false);
            jButton1.setEnabled(false);
            jList1.setEnabled(false);
        }
        
        if (remotePathsFound == 0) {
            jButton3.setVisible(false);
        }

        registry = Registry.get(getClass());
        this.setPreferredSize(new Dimension(registry.get(winWidth, this.getPreferredSize().width), registry.get(winHeight, this.getPreferredSize().height)));
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
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(520, 355));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jLabel1.text", differentMediaFiles, totalMediaFiles));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jLabel3.text", nbOfPaths));

        jList1.setModel(model);
        jScrollPane1.setViewportView(jList1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jLabel4.text", rootPath));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jLabel5.text", totalPathsFound, relPathsFound, absPathsFound));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jLabel6.text", totalPathsUnfound, relPathsUnfound, absPathsUnfound, unfoundColor));

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jButton1.text", differentMediaFiles - remotePathsFound));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(204, 0, 0));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jLabel7.text", remotePathsFound));
        jLabel7.setName(""); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jButton3.text", remotePathsFound));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jButton2, javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put(winWidth, evt.getComponent().getWidth());
        registry.put(winHeight, evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        openMediaManager();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        initFilesMap();
        jLabel2.setVisible(false);
        jButton2.setVisible(false);

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        openRemoteManager();
    }//GEN-LAST:event_jButton3ActionPerformed

    @Override
    public String getName() {
        return NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "STEP_5_name");
    }

    private void initFilesMap() {
        property2PathMap = new HashMap<>();
        for (Property file : filesList) {
            if (file instanceof PropertyFile) {
                PropertyFile pFile = (PropertyFile) file;
                // Calc path
                String value = file.getValue();
                if (pFile.isIsRemote()) {
                    continue;
                } else {
                    File f = new File(value);
                    String name = f.getName();
                    String path = value.substring(0, value.indexOf(name));
                    property2PathMap.put(pFile, path);
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private void openMediaManager() {
        MediaManagerPanel panel = new MediaManagerPanel(rootPath, property2PathMap);
        Object o = DialogManager.create(NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jButton1.text", differentMediaFiles - remotePathsFound), panel)
                .setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).show();
        if (o == DialogManager.OK_OPTION) {
            // Update property2PathMap with rel flags
            //...
            if (panel.isMapModified()) {
                jLabel2.setVisible(true);
                jButton2.setVisible(true);
            }
        } else {
            // nothing
        }

    }
    
    private void openRemoteManager() {
        RemoteMediaManagerPanel panel = new RemoteMediaManagerPanel(remotePaths);
        Object o = DialogManager.create(NbBundle.getMessage(GedcomPropertiesMediaFormatPanel.class, "GedcomPropertiesMediaFormatPanel.jButton1.text", differentMediaFiles - remotePathsFound), panel)
                .setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).show();
         if (o == DialogManager.OK_OPTION) {
            // Update property2PathMap with rel flags
            //...
            if (panel.isMapModified()) {
                jLabel2.setVisible(true);
            }
        }
    }

    public boolean isModified() {
        return jLabel2.isVisible();
    }

    public Map<PropertyFile, String> getMediaMap() {
        return property2PathMap;
    }

}
