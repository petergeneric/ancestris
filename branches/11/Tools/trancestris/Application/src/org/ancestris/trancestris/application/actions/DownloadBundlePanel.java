/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DownloadBundlePanel.java
 *
 * Created on 9 mai 2012, 22:08:12
 */
package org.ancestris.trancestris.application.actions;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author dominique
 */
public class DownloadBundlePanel extends javax.swing.JPanel {

    private File localBundleFile = null;
    private String bundleUrl = null;
    final FileNameExtensionFilter filter = new FileNameExtensionFilter("Zip files", "zip");
    private JFileChooser fileChooser = new JFileChooser();
    static HashMap<String, Locale> localeList = new HashMap<String, Locale>();
    static Locale[] locales = null;
    private Locale fromLocale = Locale.UK;
    private Locale toLocale = Locale.getDefault();
    String dirName = "";
    String fileName = "";

    {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (localeList.get(locale.getDisplayLanguage()) == null) {
                localeList.put(locale.getDisplayLanguage(), locale);
            }
        }
        Locale breton = new Locale("br");
        localeList.put(breton.getDisplayLanguage(), breton);

        Locale esp = new Locale("eo");
        localeList.put(esp.getDisplayLanguage(), esp);

        locales = new Locale[localeList.size()];
        SortedSet<String> sortedset = new TreeSet<String>(localeList.keySet());

        Iterator<String> iter = sortedset.iterator();

        int index = 0;
        while (iter.hasNext()) {
            locales[index++] = localeList.get(iter.next());
        }
    }

    private class LocaleComboBoxModel extends DefaultComboBoxModel <String>{

        String selectedLocale = Locale.getDefault().getDisplayLanguage();

        public LocaleComboBoxModel() {
            super();
        }

        @Override
        public int getSize() {
            return localeList.size();
        }

        @Override
        public String getElementAt(int i) {
            return locales[i].getDisplayLanguage();
        }

        @Override
        public void setSelectedItem(Object o) {
            selectedLocale = (String) o;
        }

        @Override
        public Object getSelectedItem() {
            return selectedLocale;
        }
    }

    /** Creates new form DownloadBundlePanel */
    public DownloadBundlePanel() {
        fromLocale = getLocaleFromString(NbPreferences.forModule(OpenZipBundlePanel.class).get("fromLocale", Locale.ENGLISH.toString()));
        toLocale = getLocaleFromString(NbPreferences.forModule(OpenZipBundlePanel.class).get("toLocale", Locale.getDefault().toString()));
        bundleUrl = NbPreferences.forModule(OpenZipBundlePanel.class).get("Url.address", NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.urlTextField.text"));

        initComponents();

        fromLocaleComboBox.setSelectedItem(fromLocale.getDisplayLanguage());
        toLoacaleComboBox.setSelectedItem(toLocale.getDisplayLanguage());
        dirName = NbPreferences.forModule(OpenZipBundlePanel.class).get("Dossier", System.getProperty("user.dir"));
        fileName = NbPreferences.forModule(OpenZipBundlePanel.class).get("Fichier", "");
        if (fileName.isEmpty() == true) {
            // Get only file name
            StringTokenizer st = new StringTokenizer(bundleUrl, "/");
            while (st.hasMoreTokens()) {
                fileName = st.nextToken();
            }
        }

        localBundleFile = new File(dirName + System.getProperty("file.separator") + fileName);
        localBundleTextField.setText(localBundleFile.toString());

        urlTextField.setText(bundleUrl);
    }

    /**
     * @return the localBundleFile
     */
    public File getLocalBundleFile() {
        return localBundleFile;
    }

    /**
     * @return the urlBundleFile
     */
    public String getBundleUrl() {
        return bundleUrl;
    }

    /**
     * @return the fromLocale
     */
    public Locale getFromLocale() {
        return fromLocale;
    }

    /**
     * @return the toLocale
     */
    public Locale getToLocale() {
        return toLocale;
    }

    private Locale getLocaleFromString(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        String locale[] = (str + "__").split("_", 3);

        return new Locale(locale[0], locale[1], locale[2]);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        urlTextField = new javax.swing.JTextField();
        localBundleTextField = new javax.swing.JTextField();
        openFileButton = new javax.swing.JButton();
        urlLabel = new javax.swing.JLabel();
        fromLocaleComboBox = new javax.swing.JComboBox<String>(new LocaleComboBoxModel());
        translationLabel = new javax.swing.JLabel();
        toLoacaleComboBox = new javax.swing.JComboBox<String>(new LocaleComboBoxModel());
        toLabel = new javax.swing.JLabel();

        openFileButton.setText(org.openide.util.NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.openFileButton.text")); // NOI18N
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileButtonActionPerformed(evt);
            }
        });

        urlLabel.setText(org.openide.util.NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.urlLabel.text")); // NOI18N

        fromLocaleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromLocaleComboBoxActionPerformed(evt);
            }
        });

        translationLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/ancestris/trancestris/application/actions/translate.gif"))); // NOI18N
        translationLabel.setText(org.openide.util.NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.translationLabel.text")); // NOI18N

        toLoacaleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toLoacaleComboBoxActionPerformed(evt);
            }
        });

        toLabel.setText(org.openide.util.NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.toLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(urlLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(urlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(localBundleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openFileButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(translationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fromLocaleComboBox, 0, 194, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toLoacaleComboBox, 0, 194, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localBundleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(translationLabel)
                    .addComponent(toLabel)
                    .addComponent(toLoacaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fromLocaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileButtonActionPerformed

        fileChooser.setFileFilter(filter);
        if (dirName.length() > 0) {
            // Set the current directory
            fileChooser.setCurrentDirectory(new File(dirName));
        }

        if (fileName.length() > 0) {
            fileChooser.setSelectedFile(new File(fileName));
        }

        if (fileChooser.showSaveDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            localBundleFile = fileChooser.getSelectedFile();
            localBundleTextField.setText(localBundleFile.getPath());
        }
    }//GEN-LAST:event_openFileButtonActionPerformed

    private void fromLocaleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromLocaleComboBoxActionPerformed
        fromLocale = localeList.get((String) fromLocaleComboBox.getSelectedItem());
}//GEN-LAST:event_fromLocaleComboBoxActionPerformed

    private void toLoacaleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toLoacaleComboBoxActionPerformed
        toLocale = localeList.get((String) toLoacaleComboBox.getSelectedItem());
}//GEN-LAST:event_toLoacaleComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> fromLocaleComboBox;
    private javax.swing.JTextField localBundleTextField;
    private javax.swing.JButton openFileButton;
    private javax.swing.JLabel toLabel;
    private javax.swing.JComboBox<String> toLoacaleComboBox;
    private javax.swing.JLabel translationLabel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
