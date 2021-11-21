package ancestris.modules.releve.imageBrowser;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.ReleveTopComponent;
import ancestris.util.swing.FileChooserBuilder;
import genj.io.FileAssociation;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileSystemView;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */

public class BrowserOptionsPanel extends javax.swing.JPanel {
    private static final Logger LOG = Logger.getLogger("ancestris.app");
    
    /**
     * Creates new form BrowserOptionsPanel
     */
    public BrowserOptionsPanel() {
        initComponents();
    }

    public void loadPreferences() {
        jCheckBoxBrowser.setSelected(Boolean.parseBoolean(NbPreferences.forModule(BrowserOptionsPanel.class).get("ImageBrowserVisible", "false")));
        jCheckBoxMenu.setSelected(Boolean.parseBoolean(NbPreferences.forModule(BrowserOptionsPanel.class).get("ViewMenuVisible", "false")));
        
        // je charge les repertoires 
        jList1.setModel(ImageDirectoryModel.getModel());
        if (ImageDirectoryModel.getModel().size() > 0 ) {
            jList1.setSelectedIndex(0);
        }
    }
    
    public void savePreferences() {
        NbPreferences.forModule(BrowserOptionsPanel.class).put("ImageBrowserVisible", String.valueOf(jCheckBoxBrowser.isSelected()));
        NbPreferences.forModule(BrowserOptionsPanel.class).put("ViewMenuVisible", String.valueOf(jCheckBoxMenu.isSelected()));

        // je notifie les editeurs pour rafraichir l'affichage
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
            tc.setBrowserVisible(jCheckBoxBrowser.isSelected());
        }

        // j'enregistre les repertoires
        ImageDirectoryModel.getModel().savePreferences();

    }
    
    static public boolean getImageBrowserVisible() {
        return Boolean.parseBoolean(NbPreferences.forModule(BrowserOptionsPanel.class).get("ImageBrowserVisible", "false"));
    } 

    static public boolean getViewMenuVisible() {
        return Boolean.parseBoolean(NbPreferences.forModule(BrowserOptionsPanel.class).get("ViewMenuVisible", "false"));
    } 

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButtonHelp = new javax.swing.JButton();
        jCheckBoxBrowser = new javax.swing.JCheckBox();
        jCheckBoxMenu = new javax.swing.JCheckBox();
        jLabelDirectory = new javax.swing.JLabel();
        jButtonAddDirectory = new javax.swing.JButton();
        jButtonRemoveDirectory = new javax.swing.JButton();
        jButtonSwapPreviousDirectory = new javax.swing.JButton();
        jButtonSwapNextDirectory = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<String>();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jButtonHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/information.png"))); // NOI18N
        jButtonHelp.setBorder(null);
        jButtonHelp.setMargin(null);
        jButtonHelp.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonHelp, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxBrowser, org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.jCheckBoxBrowser.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jCheckBoxBrowser, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxMenu, org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.jCheckBoxMenu.text")); // NOI18N
        jCheckBoxMenu.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.jCheckBoxMenu.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jCheckBoxMenu, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelDirectory, org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.jLabelDirectory.text")); // NOI18N
        jLabelDirectory.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.jLabelDirectory.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jLabelDirectory, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddDirectory, org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.jButtonAddDirectory.text")); // NOI18N
        jButtonAddDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        add(jButtonAddDirectory, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveDirectory, org.openide.util.NbBundle.getMessage(BrowserOptionsPanel.class, "BrowserOptionsPanel.jButtonRemoveDirectory.text")); // NOI18N
        jButtonRemoveDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        add(jButtonRemoveDirectory, gridBagConstraints);

        jButtonSwapPreviousDirectory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/arrowup16.png"))); // NOI18N
        jButtonSwapPreviousDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSwapPreviousDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        add(jButtonSwapPreviousDirectory, gridBagConstraints);

        jButtonSwapNextDirectory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/arrowdown16.png"))); // NOI18N
        jButtonSwapNextDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSwapNextDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        add(jButtonSwapNextDirectory, gridBagConstraints);

        jScrollPane2.setMaximumSize(new java.awt.Dimension(800, 130));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(50, 23));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(300, 60));
        jScrollPane2.setRequestFocusEnabled(false);

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setMaximumSize(new java.awt.Dimension(32767, 32767));
        jList1.setMinimumSize(new java.awt.Dimension(100, 80));
        jScrollPane2.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddDirectoryActionPerformed

        File defaultDirectory;
        if (jList1.getSelectedValue() != null) {
            defaultDirectory = new File(jList1.getSelectedValue());
        } else {
            FileSystemView fsv = FileSystemView.getFileSystemView();
            defaultDirectory = fsv.getDefaultDirectory();
        }

        File file = new FileChooserBuilder(BrowserOptionsPanel.class)
                .setDirectoriesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "BrowserOptionsPanel.dialogTitle.text"))
                .setApproveText(NbBundle.getMessage(getClass(), "BrowserOptionsPanel.dialogTitle.ok"))
                .setSelectedFile(defaultDirectory)
                .setFileHiding(true)
                .showOpenDialog();
        
        if (file != null) {
            try {
                String directory = file.getCanonicalPath();
                int index = ImageDirectoryModel.getModel().indexOf(directory);
                if ( index == -1 ) {
                    ImageDirectoryModel.getModel().addElement(directory);
                }
                jList1.setSelectedValue(directory, true);
            } catch (IOException ex) {
                return;
            }
        }
    }//GEN-LAST:event_jButtonAddDirectoryActionPerformed

    private void jButtonRemoveDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveDirectoryActionPerformed
        for (Iterator<String> it = jList1.getSelectedValuesList().iterator(); it.hasNext();) {
            String directory = it.next();
            ImageDirectoryModel.getModel().removeElement(directory);
        }
    }//GEN-LAST:event_jButtonRemoveDirectoryActionPerformed

    private void jButtonSwapPreviousDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSwapPreviousDirectoryActionPerformed
        int index = jList1.getSelectedIndex();
        if ( ImageDirectoryModel.getModel().swapPrevious(index) ) {
            jList1.setSelectedIndex(index -1);
        }
    }//GEN-LAST:event_jButtonSwapPreviousDirectoryActionPerformed

    private void jButtonSwapNextDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSwapNextDirectoryActionPerformed
        int index = jList1.getSelectedIndex();
        if ( ImageDirectoryModel.getModel().swapNext(index) ) {
            jList1.setSelectedIndex(index +1);
        }
    }//GEN-LAST:event_jButtonSwapNextDirectoryActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        try {
            FileAssociation.getDefault().execute(new URL(NbBundle.getMessage(BrowserOptionsPanel.class, "Releve.helpPage")));
        } catch (MalformedURLException ex) {
            LOG.log(Level.FINE, "Unable to open File", ex);
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButtonHelpActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddDirectory;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonRemoveDirectory;
    private javax.swing.JButton jButtonSwapNextDirectory;
    private javax.swing.JButton jButtonSwapPreviousDirectory;
    private javax.swing.JCheckBox jCheckBoxBrowser;
    private javax.swing.JCheckBox jCheckBoxMenu;
    private javax.swing.JLabel jLabelDirectory;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    // modele
    static public class ImageDirectoryModel extends DefaultListModel<String> {
        final static String ImageBrowserDirectoryPreference = "ImageBrowserDirectories";
        private static ImageDirectoryModel imageDirectoryModel = null;

        public File[] getImageBrowserDirectories() {
            ArrayList<File> directories = new ArrayList<File>();
            for (int i = 0; i < imageDirectoryModel.size(); i++) {
                File directory = new File(imageDirectoryModel.get(i));
                if( directory.exists()) {
                    directories.add(directory);
                }
            }

            return directories.toArray(new File[0]);
        }

        static public ImageDirectoryModel getModel() {

            if (imageDirectoryModel == null) {
                imageDirectoryModel = new ImageDirectoryModel();
                imageDirectoryModel.loadPreferences();
            }
            return imageDirectoryModel;
        }
       
        /**
         * charge les repertoires
         */
        private void loadPreferences() {
            this.clear();
            // je recupere la liste des valeurs similaires
            String similarString = NbPreferences.forModule(ImageDirectoryModel.class).get(
                    ImageBrowserDirectoryPreference, "");
            String[] values = similarString.split(";");
            for (int i = 0; i < values.length; i++) {
                if (!values[i].isEmpty()) {
                    this.addElement(values[i]);
                }
            }
        }

        /**
         * enregistre les repertoire
         */
        private void savePreferences() {
            StringBuilder values = new StringBuilder();

            for (int i = 0; i < this.size(); i++) {
                values.append(this.get(i)).append(";");
            }
            NbPreferences.forModule(ImageDirectoryModel.class).put(
                    ImageBrowserDirectoryPreference, values.toString());
        }

         private boolean swapNext(int index ) {
             if ( index < size() -1 && index != -1) {
                 String directory = remove(index);
                 insertElementAt(directory, index+1);
                 return true;
             } else {
                 Toolkit.getDefaultToolkit().beep();
                 return false;
             }

         }

         private boolean swapPrevious(int index ) {
             if ( index > 0) {
                 String directory = remove(index);
                 insertElementAt(directory, index-1);
                  return true;
             } else {
                 Toolkit.getDefaultToolkit().beep();
                 return false;
             }

         }
        
    }

}
