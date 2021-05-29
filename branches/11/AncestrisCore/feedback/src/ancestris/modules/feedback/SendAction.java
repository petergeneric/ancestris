/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.feedback;

import genj.util.EnvironmentChecker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class SendAction implements ActionListener {

    private final static java.util.ResourceBundle RESOURCES = java.util.ResourceBundle.getBundle("ancestris/modules/feedback/Bundle");
    private final static String TEXTSEPARATOR = "\n=======================================\n";
    private final static String SEND = NbBundle.getMessage(SendAction.class, "SEND_BUTTON");
    private Preferences modulePreferences = NbPreferences.forModule(FeedBackPlugin.class);
    private File zipFile;
    private FeedbackPanel fbPanel;

    private void setDefaultValues(FeedbackPanel panel) {
        panel.jtaText.setText(getSystemInfo());
        panel.jtEmailTo.setText(RESOURCES.getString("fb.mailto.default"));
        panel.jtName.setText(modulePreferences.get("mail.name", ""));
        panel.jtEmail.setText(modulePreferences.get("mail.address", ""));
    }

    private void saveDefaultValues(FeedbackPanel panel) {
        modulePreferences.put("mail.name", panel.jtName.getText().trim());
        modulePreferences.put("mail.address", panel.jtEmail.getText().trim());
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        try {
            zipFile = sendUserDir();
        } catch (Exception ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES.getString("fb.msg.senderror")
                    + "\n(" + ex.getMessage() + ").", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        if (modulePreferences.get("mail.host", "").equals("")) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES.getString("fb.msg.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

            OptionsDisplayer.getDefault().open("Extensions/FeedBack");
        } else {
            fbPanel = new FeedbackPanel(zipFile);

            setDefaultValues(fbPanel);

            DialogDescriptor dd = new DialogDescriptor(fbPanel, NbBundle.getMessage(this.getClass(), "FeedbackPanel.title"));
            dd.setOptions(new Object[]{SEND, DialogDescriptor.CANCEL_OPTION});
            DialogDisplayer.getDefault().createDialog(dd);
            DialogDisplayer.getDefault().notify(dd);
            if (dd.getValue().equals(SEND)) {
                // on sauvegarde qq valeurs par defaut
                saveDefaultValues(fbPanel);
                String name = fbPanel.jtName.getText().trim();
                String from = fbPanel.jtEmail.getText().trim();
                String to = fbPanel.jtEmailTo.getText().trim();
                String messageBody = fbPanel.jtaText.getText().trim();
                String TS = new SimpleDateFormat("yyMMdd-HHmm").format(new Date());
                String subject = "[" + RESOURCES.getString("fb.tag.subject") + " " + TS + "] ";
                subject += fbPanel.jtSubject.getText().trim();
                File attachedZIP = null;
                if (fbPanel.sendLogCheckBox.isSelected()) {
                    attachedZIP = zipFile;
                }
                File attachedFile = null;
                if (fbPanel.attachmentFile != null) {
                    attachedFile = fbPanel.attachmentFile;
                }
                Thread t = new Thread(new SendMailWorker(name, from, to, subject, messageBody, attachedZIP, attachedFile), "SendFeedback");
                t.start();
            }
        }
    }

    public static String getSystemInfo() {
        Properties p = System.getProperties();
        StringBuilder sb = new StringBuilder();
        sb.append("Ancestris ");
        sb.append(Lookup.getDefault().lookup(ancestris.api.core.Version.class).getBuildString());
        sb.append(" (");
        sb.append(Locale.getDefault());
        sb.append(")\nOS : ");
        sb.append(p.get("os.name"));
        sb.append(" ");
        sb.append(p.get("os.version"));
        sb.append(" (");
        sb.append(p.get("os.arch"));
        sb.append(")\nJRE: ");
        sb.append(p.get("java.vendor"));
        sb.append(" ");
        sb.append(p.get("java.version"));
        sb.append("\n");
        sb.append(TEXTSEPARATOR).append(RESOURCES.getString("fb.text.comment"));
        sb.append("\n");
        return sb.toString();
    }

    private File sendUserDir() throws IOException {
        String baseDir = System.getProperty("netbeans.user");
        Zipper zipUD = new Zipper(new File(baseDir));
        zipUD.addIncludePatterns("ancestris/.*");
        zipUD.addExcludePatterns("ancestris/.*\\.lck");
        zipUD.addIncludePatterns("config/Preferences/.*");
//        zipUD.addIncludePatterns("config/Preferences/org/.*");
        zipUD.addIncludePatterns("config/Toolbars/.*");
        zipUD.addIncludePatterns("config/Windows2Local/.*");
        zipUD.addIncludePatterns("var/log/.*");
        File temp = File.createTempFile("ancestris", ".zip");
        zipUD.doExport(temp);
        return temp;
    }

    private class GenjLogFile {

        private File theFile;

        GenjLogFile() {
            File home = new File(EnvironmentChecker.getProperty(
                    "user.home.genj", null, "determining home directory"));
            theFile = new File(home, "ancestris.log");
        }

        long getSize() {
            return theFile.length();
        }

        String getContent() {
            String msg = "";
            try {
                BufferedReader in = new BufferedReader(new FileReader(theFile));

                while (in.ready()) {
                    msg += in.readLine() + "\n";
                }
            } catch (Exception e) {
            }
            return msg;
        }
    }
}
