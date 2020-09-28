/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.api.lnf.LookAndFeelProvider;
import ancestris.core.TextOptions;
import ancestris.startup.settings.StartupOptions;
import ancestris.util.Lifecycle;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.swing.ToolTipManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@SuppressWarnings(value = {"unchecked", "rawtypes"})
final class OptionDisplayPanel extends javax.swing.JPanel {

    private final OptionDisplayOptionsPanelController controller;
    // Values
    Locale[] locales = {
        new Locale("br"),
        new Locale("es"),
        new Locale("ca"),
        new Locale("cs"),
        new Locale("da"),
        Locale.GERMAN,
        Locale.ENGLISH,
        new Locale("el"),
        // new Locale("eo"),
        new Locale("fi"),
        Locale.FRENCH,
        new Locale("hu"),
        Locale.ITALIAN,
        new Locale("lv"),
        new Locale("nl"),
        new Locale("no"),
        new Locale("pl"),
        new Locale("pt"),
        //new Locale("ru"),  // russian not available yet
        new Locale("sv")
    };
    private static final LookAndFeelProvider[] SKINS = LookAndFeelProvider.getProviders();

    private long memTotal;
    private String xmx;

    OptionDisplayPanel(OptionDisplayOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        jComboBoxAppearanceActionPerformed(null);
        ToolTipManager.sharedInstance().setDismissDelay(10000); // sets it for the other panels...
        // TODO listen to changes in form fields and call controller.changed()
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelLanguage = new javax.swing.JLabel();
        jcbLanguage = new javax.swing.JComboBox(initLanguages(NbBundle.getMessage(App.class, "options.lang.system")));
        jLabelOutput = new javax.swing.JLabel();
        jcbOutputLanguage = new javax.swing.JComboBox(initLanguages(NbBundle.getMessage(App.class, "options.lang.gui")));
        jLabelAppearance = new javax.swing.JLabel();
        jComboBoxAppearance = new javax.swing.JComboBox(SKINS);
        jLabelFontsize = new javax.swing.JLabel();
        jSpinnerFontsize = new javax.swing.JSpinner();
        jLabelWindow = new javax.swing.JLabel();
        jCheckBoxWindow = new javax.swing.JCheckBox();
        jLabelMemsize = new javax.swing.JLabel();
        jSpinnerMemsize = new javax.swing.JSpinner();
        jPanelDemo = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();

        jLabelLanguage.setFont(jLabelLanguage.getFont().deriveFont(jLabelLanguage.getFont().getStyle() | java.awt.Font.BOLD, jLabelLanguage.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelLanguage, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabelLanguage.text")); // NOI18N

        jcbLanguage.setMaximumRowCount(20);
        jcbLanguage.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jcbLanguage.toolTipText")); // NOI18N

        jLabelOutput.setFont(jLabelOutput.getFont().deriveFont(jLabelOutput.getFont().getStyle() | java.awt.Font.BOLD, jLabelOutput.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelOutput, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabelOutput.text")); // NOI18N

        jcbOutputLanguage.setMaximumRowCount(20);
        jcbOutputLanguage.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jcbOutputLanguage.toolTipText")); // NOI18N

        jLabelAppearance.setFont(jLabelAppearance.getFont().deriveFont(jLabelAppearance.getFont().getStyle() | java.awt.Font.BOLD, jLabelAppearance.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelAppearance, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabelAppearance.text")); // NOI18N

        jComboBoxAppearance.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jComboBoxAppearance.toolTipText")); // NOI18N
        jComboBoxAppearance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxAppearanceActionPerformed(evt);
            }
        });

        jLabelFontsize.setFont(jLabelFontsize.getFont().deriveFont(jLabelFontsize.getFont().getStyle() | java.awt.Font.BOLD, jLabelFontsize.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFontsize, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabelFontsize.text")); // NOI18N

        jSpinnerFontsize.setModel(new javax.swing.SpinnerNumberModel(12, 5, 40, 1));

        jLabelWindow.setFont(jLabelWindow.getFont().deriveFont(jLabelWindow.getFont().getStyle() | java.awt.Font.BOLD, jLabelWindow.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelWindow, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabelWindow.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxWindow, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jCheckBoxWindow.text")); // NOI18N
        jCheckBoxWindow.setToolTipText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jCheckBoxWindow.toolTipText")); // NOI18N

        jLabelMemsize.setFont(jLabelMemsize.getFont().deriveFont(jLabelMemsize.getFont().getStyle() | java.awt.Font.BOLD, jLabelMemsize.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMemsize, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabelMemsize.text")); // NOI18N

        jSpinnerMemsize.setModel(new javax.swing.SpinnerNumberModel(1, 1, 12, 1));

        jPanelDemo.setBackground(new java.awt.Color(179, 179, 179));
        jPanelDemo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelDemo.setPreferredSize(new java.awt.Dimension(199, 224));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionDisplayPanel.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout jPanelDemoLayout = new javax.swing.GroupLayout(jPanelDemo);
        jPanelDemo.setLayout(jPanelDemoLayout);
        jPanelDemoLayout.setHorizontalGroup(
            jPanelDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDemoLayout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelDemoLayout.setVerticalGroup(
            jPanelDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDemoLayout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelWindow)
                    .addComponent(jLabelMemsize)
                    .addComponent(jLabelOutput)
                    .addComponent(jLabelLanguage)
                    .addComponent(jLabelAppearance)
                    .addComponent(jLabelFontsize))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxAppearance, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jcbOutputLanguage, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jcbLanguage, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxWindow)
                            .addComponent(jSpinnerFontsize, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerMemsize, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jPanelDemo, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelDemo, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelLanguage)
                            .addComponent(jcbLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelOutput)
                            .addComponent(jcbOutputLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelAppearance)
                            .addComponent(jComboBoxAppearance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinnerFontsize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelFontsize))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelWindow)
                            .addComponent(jCheckBoxWindow))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelMemsize)
                            .addComponent(jSpinnerMemsize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxAppearanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxAppearanceActionPerformed
        LookAndFeelProvider provider = ((LookAndFeelProvider) jComboBoxAppearance.getSelectedItem());

        jLabel5.setIcon(provider == null ? null : provider.getSampleImage());
}//GEN-LAST:event_jComboBoxAppearanceActionPerformed

    void load() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

        List<String> arguments = runtimeMxBean.getInputArguments();
        for (String s : arguments) {
            if (s.contains("-Xmx")) {
                xmx = s.substring(4);
            }
        }

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            Object attribute = mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize");
            memTotal = 1 + Long.parseLong(attribute.toString()) / (1024 * 1024 * 1024);
            jSpinnerMemsize.setModel(new javax.swing.SpinnerNumberModel(1, 1, memTotal-1, 1));
        } catch (MalformedObjectNameException | MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException ex) {
            Exceptions.printStackTrace(ex);
        }

        StartupOptions stopts = new StartupOptions();
        setLanguage(stopts.getJvmLocale());
        setOutputLanguage(TextOptions.getInstance().getOutputLocale(null));

        String value = ancestris.app.AppOptions.getFontSize(); 
        if (value != null && !value.isEmpty()) {
            jSpinnerFontsize.setValue(Integer.valueOf(value));
        }
        jComboBoxAppearance.setSelectedItem(LookAndFeelProvider.getProviderFromName(stopts.getJvmParameter("--laf")));
        jCheckBoxWindow.setSelected(ancestris.app.AppOptions.isRestoreViews());
        if (xmx != null) {
            // Prevent exception if default value is less than 1g
            Integer i;
            try {
                i = Integer.valueOf(xmx.replace('g', ' ').trim());
            } catch (NumberFormatException e) {
                i = 1;
            }
            jSpinnerMemsize.setValue(i);
        }
    }

    void store() {
        boolean needRestart = false;

        StartupOptions stopts = new StartupOptions();

        needRestart |= stopts.setJvmLocale(getLanguage());
        TextOptions.getInstance().setOutputLocale(getOutputLanguage());

        needRestart |= stopts.setJvmParameter("--laf", ((LookAndFeelProvider) jComboBoxAppearance.getSelectedItem()).getName());
        stopts.setJvmParameter("--cp:p", ((LookAndFeelProvider) jComboBoxAppearance.getSelectedItem()).getClassPath());
        
        needRestart |= !ancestris.app.AppOptions.getFontSize().equals((String) jSpinnerFontsize.getValue().toString());
        ancestris.app.AppOptions.setFontSize((String) jSpinnerFontsize.getValue().toString());

        needRestart |= stopts.setJvmParameter("-J-Xmx", Integer.valueOf(jSpinnerMemsize.getValue().toString()) + "g");

        stopts.applyChanges();

        ancestris.app.AppOptions.setRestoreViews(jCheckBoxWindow.isSelected());

        StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(OptionDisplayPanel.class, "OptionPanel.saved.statustext"));
        if (needRestart) // the markForRestart is not applicable here as the restart process loop done in nbexec file
        // doesn't reread app.conf file which is read once before the loop.
        // W/O modifying nbexec and windows dll, the startup  settings are not re-read
        // So, as in a basic usage of ancestris the language preference will not be set by the user,
        // we tell the user to stop then start again ancestris. This way all the new startup settings are correctly read
        {
            Lifecycle.askForStopAndStart(null, getLanguage());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxWindow;
    private javax.swing.JComboBox jComboBoxAppearance;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelAppearance;
    private javax.swing.JLabel jLabelFontsize;
    private javax.swing.JLabel jLabelLanguage;
    private javax.swing.JLabel jLabelMemsize;
    private javax.swing.JLabel jLabelOutput;
    private javax.swing.JLabel jLabelWindow;
    private javax.swing.JPanel jPanelDemo;
    private javax.swing.JSpinner jSpinnerFontsize;
    private javax.swing.JSpinner jSpinnerMemsize;
    private javax.swing.JComboBox jcbLanguage;
    private javax.swing.JComboBox jcbOutputLanguage;
    // End of variables declaration//GEN-END:variables

    private String[] initLanguages(String defaultDesc) {
        ArrayList<String> langDescr = new ArrayList<>(locales.length);
        langDescr.add(defaultDesc);
        for (Locale locale : locales) {
            if (locale.getDisplayName(locale).equals("español")) {
                langDescr.add("Castellano");
            } else {
                langDescr.add(locale.getDisplayName(locale));
            }

        }
        return langDescr.toArray(new String[0]);
    }

    /**
     * Find the index in languagesfor the language string lang. If not found
     * returns -1
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
     * Set the language selector accorgingly to the locale setting passed in
     * parameter
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
     * Set the language selector accorgingly to the locale setting passed in
     * parameter
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
}
