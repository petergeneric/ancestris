/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EditorOpenActionPanel.java
 *
 * Created on 16 juin 2011, 23:11:29
 */
package org.ancestris.trancestris.editors.actions;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.windows.WindowManager;

/**
 *
 * @author dominique
 */
public class EditorOpenActionPanel extends javax.swing.JPanel {

    final static Locale[] locales = Locale.getAvailableLocales();
    final static HashMap<String, Locale> localeList = new HashMap<String, Locale>();
    Locale selectedLocale = Locale.getDefault();
    File defaultBundleFile = null;

    private class LocaleComboBoxModel extends DefaultComboBoxModel {

        public LocaleComboBoxModel() {
            super();
            for (Locale locale : locales) {
                localeList.put(locale.getDisplayLanguage(), locale);
            }
        }

        @Override
        public int getSize() {
            return localeList.size();
        }

        @Override
        public Object getElementAt(int i) {
            return locales[i].getDisplayLanguage();
        }

        @Override
        public void setSelectedItem(Object o) {
            selectedLocale = localeList.get((String) o);
        }

        @Override
        public Object getSelectedItem() {
            return selectedLocale.getDisplayLanguage();
        }
    }

    /** Creates new form EditorOpenActionPanel */
    public EditorOpenActionPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox(new LocaleComboBoxModel());
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(32789, 24));
        setOpaque(false);
        setLayout(new java.awt.GridLayout(2, 0));

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/ancestris/trancestris/editors/actions/translate.gif"))); // NOI18N
        jLabel1.setText(org.openide.util.NbBundle.getMessage(EditorOpenActionPanel.class, "EditorOpenActionPanel.jLabel1.text")); // NOI18N
        jPanel1.add(jLabel1);

        jPanel1.add(jComboBox1);

        add(jPanel1);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jTextField1.setText(org.openide.util.NbBundle.getMessage(EditorOpenActionPanel.class, "EditorOpenActionPanel.jTextField1.text")); // NOI18N
        jPanel2.add(jTextField1);

        jButton1.setText(org.openide.util.NbBundle.getMessage(EditorOpenActionPanel.class, "EditorOpenActionPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        add(jPanel2);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("Properties files", "properties");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            defaultBundleFile = fileChooser.getSelectedFile();
            jTextField1.setText(defaultBundleFile.getName());
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
