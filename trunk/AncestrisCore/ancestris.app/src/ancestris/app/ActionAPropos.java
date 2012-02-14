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

import ancestris.core.pluginservice.PluginInterface;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
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

    private JButton jbContrib = new JButton();
    private JButton jbLicence = new JButton();
    private JButton jbVersions = new JButton();
    private AboutPanel panel = new AboutPanel();

    /** Creates new form ActionAPropos */
    public ActionAPropos() {
        jbContrib.setText(org.openide.util.NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbContrib.text")); // NOI18N
        jbContrib.setFocusable(false);
        jbContrib.setPreferredSize(new java.awt.Dimension(120, 29));
        jbContrib.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panel.startStop();
            }
        });

        jbLicence.setText(org.openide.util.NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbLicence.text")); // NOI18N
        jbLicence.setFocusable(false);
        jbLicence.setPreferredSize(new java.awt.Dimension(120, 29));
        jbLicence.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayLicence();
            }
        });

        jbVersions.setText(org.openide.util.NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbVersions.text")); // NOI18N
        jbVersions.setFocusable(false);
        jbVersions.setPreferredSize(new java.awt.Dimension(120, 29));
        jbVersions.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayVersions();
            }
        });

    }

    public void actionPerformed(ActionEvent e) {
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(ActionAPropos.class, "CTL_ActionAPropos"),
                true,
                new Object[]{jbContrib, jbLicence, jbVersions, Box.createGlue(), DialogDescriptor.CLOSED_OPTION},
                DialogDescriptor.CLOSED_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null);
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

    // Take last 72 charachters of userdir if too long
    private String getUserDir() {
        String str = System.getProperty("netbeans.user");
        int length = str.length();
        int index = length > 72 ? length - 72 : 0;
        return (index > 0 ? "..." : "") + str.substring(index);
    }

    private void displayLicence() {
        String title = NbBundle.getMessage(ActionAPropos.class, "CTL_APropos_LicenceTitle");
        String text = NbBundle.getMessage(ActionAPropos.class, "CTL_APropos_LicenceText");
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(getScrollableText(text), title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }

    private void displayVersions() {
        String title = NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.jbVersions.text");
        String text = "<html><br><b>"
                + App.getPluginShortDescription() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b>"
                + App.getPluginVersion();
        text += "<br><br><br><b>" + NbBundle.getMessage(ActionAPropos.class, "CTL_APropos_VersionTitle") + "&nbsp;:&nbsp;&nbsp;&nbsp;&nbsp;</b><br>";
        text += "<table border='0'>";
        for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
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
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(getScrollableText(text), title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }

    private String getContributors() {
        String contributors = NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.contributors"); // NOI18N
        String translators = NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.translators"); // NOI18N

        return NbBundle.getMessage(ActionAPropos.class, "ActionAPropos.contributors.text",
                "<br>" + contributors.replaceAll(",", "<br>"),
                "<br>" + translators.replaceAll(",", "<br>") + "<br><br><br>" + "-:-:-:-:-:-:-:-:-:-:-:-:-" + "<br><br><br>"); // NOI18N
    }

    private JScrollPane getScrollableText(String text) {
        JLabel area = new JLabel(text);
        area.setOpaque(true);
        return new JScrollPane(area);
    }
}
