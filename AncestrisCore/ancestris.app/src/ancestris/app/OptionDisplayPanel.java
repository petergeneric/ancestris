/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.api.lnf.LookAndFeelProvider;
import ancestris.core.CoreOptions;
import ancestris.core.TextOptions;
import ancestris.startup.settings.StartupOptions;
import ancestris.util.Lifecycle;
import genj.gedcom.GedcomOptions;
import genj.util.AncestrisPreferences;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

@SuppressWarnings(value = {"unchecked", "rawtypes"})
final class OptionDisplayPanel extends javax.swing.JPanel {

    private final OptionDisplayOptionsPanelController controller;
    // Values
    Locale[] locales = {
        new Locale("br"),
        new Locale("cs"),
        new Locale("da"),
        Locale.GERMAN,
        Locale.ENGLISH,
        new Locale("es"),
        Locale.FRENCH,
        Locale.ITALIAN,
        new Locale("nl"),
        new Locale("no"),
        new Locale("pl"),
        new Locale("pt"),
        new Locale("ru"),
        new Locale("fi"),
        new Locale("sv")
    };
    private static LookAndFeelProvider[] skins = LookAndFeelProvider.getProviders();

    OptionDisplayPanel(OptionDisplayOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        jComboBox2ActionPerformed(null);
        ToolTipManager.sharedInstance().setDismissDelay(10000); // sets it for the other panels...
        // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbSplitJuridictions = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jSpinner1 = new javax.swing.JSpinner(new SpinnerNumberModel(10, 10, 300, 5));
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox(skins);
        jcbLanguage = new javax.swing.JComboBox(initLanguages(NbBundle.getMessage(App.class, "options.lang.system")));
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        jcbOutputLanguage = new javax.swing.JComboBox(initLanguages(NbBundle.getMessage(App.class, "options.lang.gui")));

        setPreferredSize(new java.awt.Dimension(691, 503));

        org.openide.awt.Mnemonics.setLocalizedText(cbSplitJuridictions, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.cbSplitJuridictions.text")); // NOI18N
        cbSplitJuridictions.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.cbSplitJuridictions.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jCheckBox2.text")); // NOI18N
        jCheckBox2.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jCheckBox2.toolTipText")); // NOI18N

        jSpinner1.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jSpinner1.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel6.toolTipText")); // NOI18N

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel2.text")); // NOI18N

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel1.text")); // NOI18N

        jComboBox2.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jComboBox2.toolTipText")); // NOI18N
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jcbLanguage.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jcbLanguage.toolTipText")); // NOI18N

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel4.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel3.text")); // NOI18N

        jPanel1.setBackground(new java.awt.Color(179, 179, 179));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setPreferredSize(new java.awt.Dimension(199, 224));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jCheckBox1.toolTipText")); // NOI18N

        jLabel9.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel9.text")); // NOI18N

        jcbOutputLanguage.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jcbOutputLanguage.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jcbLanguage, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jcbOutputLanguage, 0, 209, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(72, 72, 72)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox2)
                                    .addComponent(cbSplitJuridictions)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel6)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel9});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jcbLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(jcbOutputLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(121, 121, 121)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jCheckBox1)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbSplitJuridictions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)))
                .addContainerGap(137, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        LookAndFeelProvider provider = ((LookAndFeelProvider) jComboBox2.getSelectedItem());

        jLabel5.setIcon(provider == null ? null : provider.getSampleImage());
}//GEN-LAST:event_jComboBox2ActionPerformed

    void load() {
        AncestrisPreferences gedcomPrefs = Registry.get(genj.gedcom.GedcomOptions.class);

        StartupOptions stopts = new StartupOptions();
        setLanguage(stopts.getJvmLocale());
        setOutputLanguage(TextOptions.getInstance().getOutputLocale(null));

        jComboBox2.setSelectedItem(LookAndFeelProvider.getProviderFromName(stopts.getJvmParameter("--laf")));
        jCheckBox1.setSelected(ancestris.app.AppOptions.isRestoreViews());
        jSpinner1.setValue(GedcomOptions.getInstance().getNumberOfUndos());
        cbSplitJuridictions.setSelected(CoreOptions.getInstance().isSplitJurisdictions());
//XXX:        setOpenEditor(editPrefs.get("isOpenEditor", ""));
    }

    void store() {
        boolean needRestart = false;

        AncestrisPreferences gedcomPrefs = Registry.get(genj.gedcom.GedcomOptions.class);

        StartupOptions stopts = new StartupOptions();

        needRestart |= stopts.setJvmLocale(getLanguage());
        TextOptions.getInstance().setOutputLocale(getOutputLanguage());

        needRestart |= stopts.setJvmParameter("--laf", ((LookAndFeelProvider) jComboBox2.getSelectedItem()).getName());
        stopts.setJvmParameter("--cp:p", ((LookAndFeelProvider) jComboBox2.getSelectedItem()).getClassPath());

        stopts.applyChanges();

        ancestris.app.AppOptions.setRestoreViews(jCheckBox1.isSelected());
        GedcomOptions.getInstance().setNumberOfUndos((Integer) (jSpinner1.getValue()));

        CoreOptions.getInstance().setSplitJurisdictions(cbSplitJuridictions.isSelected());

//XXX:        editPrefs.put("isOpenEditor", getOpenEditor());

        StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionPanel.saved.statustext"));
        if (needRestart) // the markForRestart is not applicable here as the restart process loop done in nbexec file
        // doesn't reread app.conf file wich is read once before the loop.
        // W/O modifying nbexec and windows dll, the startup  settings are not re-read
        // So, as in a basic usage of ancestris the language preference will not be set by the user,
        // we tell the user to stop the start again ancestris. This way all the new startup settings are correctly read
        {
            Lifecycle.askForStopAndStart(null, getLanguage());
        }
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbSplitJuridictions;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JComboBox jcbLanguage;
    private javax.swing.JComboBox jcbOutputLanguage;
    // End of variables declaration//GEN-END:variables

    private String[] initLanguages(String defaultDesc) {
        ArrayList<String> langDescr = new ArrayList<String>(locales.length);
        langDescr.add(defaultDesc);
        for (Locale locale : locales) {
            langDescr.add(locale.getDisplayName(locale));
        }
        return langDescr.toArray(new String[0]);
    }

    /**
     * Find the index in languagesfor the language string lang. If not found returns -1
     *
     * @param lang
     *
     * @return
     */
    private int findLanguageIndex(Locale locale) {
        if (locale == null) {
            return -1;
        }
        for (int i = 0; i < locales.length; i++) {
            if (locale.equals(locales[i])) {
                return i;
            }
        }
        // tries only language
        locale = new Locale(locale.getLanguage(), locale.getCountry());
        for (int i = 0; i < locales.length; i++) {
            if (locale.getLanguage().equals(locales[i].getLanguage())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Set the language selector accorgingly to the locale setting passed in parameter
     *
     * @param str
     */
    void setLanguage(Locale locale) {
        int i = findLanguageIndex(locale);
        // Index in combo is one more this value
        i += 1;
        jcbLanguage.setSelectedIndex(i);
    }

    // null means default locale from system
    Locale getLanguage() {
        int i = jcbLanguage.getSelectedIndex() - 1;
        if (i < 0 || i >= locales.length) {
            return null;
        }
        return locales[i];
    }

    /**
     * Set the language selector accorgingly to the locale setting passed in parameter
     *
     * @param str
     */
    void setOutputLanguage(Locale locale) {
        int i = findLanguageIndex(locale);
        // Index in combo is one more this value
        i += 1;
        jcbOutputLanguage.setSelectedIndex(i);
    }

    // null means default locale from system
    Locale getOutputLanguage() {
        int i = jcbOutputLanguage.getSelectedIndex() - 1;
        if (i < 0 || i >= locales.length) {
            return null;
        }
        return locales[i];
    }

    void setOpenEditor(String str) {
        jCheckBox2.setSelected(str.equals("true") ? true : false);
    }

    String getOpenEditor() {
        return jCheckBox2.isSelected() ? "true" : "false";
    }
}
