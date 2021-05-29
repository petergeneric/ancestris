/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ActionAPropos.java
 *
 * Created on 28 févr. 2010, 00:02:23
 */
package ancestris.app;

import ancestris.api.core.Version;
import ancestris.core.pluginservice.PluginInterface;
import ancestris.util.swing.DialogManager;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class ActionAPropos extends JDialog implements ActionListener {

    private JButton jbAbout = new JButton();
    private JButton jbContrib = new JButton();
    private JButton jbLicence = new JButton();
    private JButton jbVersions = new JButton();
    private AboutPanel panel = new AboutPanel();

    /** Creates new form ActionAPropos */
    public ActionAPropos() {
        jbAbout.setText(org.openide.util.NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbAbout.text")); // NOI18N
        jbAbout.setFocusable(false);
        jbAbout.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new FlyerAction().actionPerformed(evt);
            }
        });

        jbContrib.setText(org.openide.util.NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbContrib.text")); // NOI18N
        jbContrib.setFocusable(false);
        jbContrib.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panel.startStop();
            }
        });

        jbLicence.setText(org.openide.util.NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbLicence.text")); // NOI18N
        jbLicence.setFocusable(false);
        jbLicence.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayLicence();
            }
        });

        jbVersions.setText(org.openide.util.NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbVersions.text")); // NOI18N
        jbVersions.setFocusable(false);
        jbVersions.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayVersions();
            }
        });

    }

    public void actionPerformed(ActionEvent e) {
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(ActionAPropos.class, "CTL_ActionAPropos").replaceAll("&",""),
                true,
                new Object[]{DialogDescriptor.CLOSED_OPTION},
                DialogDescriptor.CLOSED_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null);
        descriptor.setAdditionalOptions(new Object[]{jbAbout, jbContrib, jbLicence, jbVersions});
        descriptor.setClosingOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setResizable(false);
            panel.setTimer();
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }

    private void displayLicence() {
        String title = NbBundle.getMessage(ActionAPropos.class, "CTL_APropos_LicenceTitle");
        String text = NbBundle.getMessage(ActionAPropos.class, "CTL_APropos_LicenceText", ""+Year.now().getValue());
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(getScrollableText(text), title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }

    private void displayVersions() {
        String title = NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbVersions.text");
        String text = "<html><br><b>"
                + Lookup.getDefault().lookup(Version.class).getDescription()
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b>"
                + Lookup.getDefault().lookup(Version.class).getVersionString();
        text += "<br><br><br><b>" + NbBundle.getMessage(ActionAPropos.class, "CTL_APropos_VersionTitle") + "&nbsp;:&nbsp;&nbsp;&nbsp;&nbsp;</b><br>";
        text += "<table border='0'>";
        List<PluginInterface> plugins = new ArrayList(Lookup.getDefault().lookupAll(PluginInterface.class));
        Collections.sort(plugins);
        for (PluginInterface sInterface : plugins) {
            try {
                if (sInterface.getPluginDisplayName() != null) {
                    text += "<tr><td><b>&nbsp;&middot;&nbsp;" + sInterface.getPluginDisplayName() + " :</b></td>";
                    try {
                        text += "<td>" + sInterface.getPluginVersion() + "</td>";
                    } catch (Throwable e) {
                        text += "<td>" + "non disponible" + "</td>";
                        App.LOG.info(e.getMessage());
                    }
                    text += "</tr>";
                }
            } catch (Throwable e) {
                App.LOG.info(e.getMessage());
            }
        }
        text += "</table><br></html>";
        DialogManager.create(title, getScrollableText(text))
                .setMessageType(DialogManager.INFORMATION_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .setDialogId("ancestris.about.versions")
                .show();
    }

    private JScrollPane getScrollableText(String text) {
        JLabel area = new JLabel(text);
        area.setOpaque(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(null);
        return sp;
    }
}
