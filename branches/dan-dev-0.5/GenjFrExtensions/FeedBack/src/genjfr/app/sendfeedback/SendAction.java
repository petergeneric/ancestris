/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.sendfeedback;

import genj.Version;
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
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class SendAction implements ActionListener {

    private final static java.util.ResourceBundle RESOURCES = java.util.ResourceBundle.getBundle("genjfr/app/sendfeedback/Bundle");
    private final static String TEXTSEPARATOR = "\n=======================================\n";
    private final static String SEND = new String(NbBundle.getMessage(SendAction.class, "SEND_BUTTON"));
    private Preferences feedbackPrefs = NbPreferences.forModule(FeedbackPanel.class);
    private File userDir;
    private FeedbackPanel fbPanel;

    private void setDefaultValues(FeedbackPanel panel) {
        panel.jtaText.setText(getSystemInfo());
        panel.jtEmailTo.setText(RESOURCES.getString("fb.mailto.default"));
        panel.jtMailHost.setText(feedbackPrefs.get("mail.host", System.getProperty("mail.host")));
        panel.jtName.setText(feedbackPrefs.get("mail.name", ""));
        panel.jtEmail.setText(feedbackPrefs.get("mail.address", ""));
    }

    private void saveDefaultValues(FeedbackPanel panel) {
        String s = panel.jtMailHost.getText();
        if (!s.equalsIgnoreCase(System.getProperty("mail.host"))) {
            feedbackPrefs.put("mail.host", s);
        }

        feedbackPrefs.put("mail.name", panel.jtName.getText().trim());
        feedbackPrefs.put("mail.address", panel.jtEmail.getText().trim());

    }

    public void actionPerformed(ActionEvent e) {

        try {
            userDir = sendUserDir();
        } catch (Exception ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES.getString("fb.msg.senderror")
                    + "\n(" + ex.getMessage() + ").", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

        fbPanel = new FeedbackPanel(userDir.length());

        setDefaultValues(fbPanel);

        DialogDescriptor dd = new DialogDescriptor(fbPanel, NbBundle.getMessage(this.getClass(), "FeedbackPanel.title"));
        dd.setOptions(new Object[]{new String(SEND), DialogDescriptor.CANCEL_OPTION});
        DialogDisplayer.getDefault().createDialog(dd);
        DialogDisplayer.getDefault().notify(dd);
        if (dd.getValue().equals(SEND)) {
            // on sauvegarde qq valeurs par defaut
            saveDefaultValues(fbPanel);
            System.setProperty("mail.host", fbPanel.jtMailHost.getText());
            Thread t = new Thread(new SendWorker(), "SendFeedback");
            t.start();
        }
    }

    public static String getSystemInfo() {
        Properties p = System.getProperties();
        StringBuffer sb = new StringBuffer();
        sb.append("Ancestris ");
        sb.append(Version.getInstance().getBuildString());
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
        sb.append(TEXTSEPARATOR + RESOURCES.getString("fb.text.comment"));
        sb.append("\n");
        return sb.toString();
    }

    private class SendWorker implements Runnable {

        public void run() {
//			acSend.setEnabled(false);
//			acCancel.setEnabled(false);

            String TS = new SimpleDateFormat("yyMMdd-HHmm").format(new Date());
            String subject = "[" + RESOURCES.getString("fb.tag.subject") + " " + TS + "] ";
            subject += fbPanel.jtSubject.getText().trim();
            String name = fbPanel.jtName.getText().trim();
            String from = fbPanel.jtEmail.getText().trim();
            String msg = fbPanel.jtaText.getText().trim();


            boolean failed = false;
            String response = "";

            String to = fbPanel.jtEmailTo.getText().trim();

            try {

                EmailAttachment attachment = null;
                new EmailAttachment();
                if (fbPanel.jcbIncGenjLog.isSelected()) {
                    attachment = new EmailAttachment();
                    attachment.setURL(sendUserDir().toURI().toURL());
                    attachment.setDisposition(EmailAttachment.ATTACHMENT);
                    attachment.setDescription("Ancestris User Dir");
                    attachment.setName("userdir.zip");
                }

                // Create the email message
                MultiPartEmail email = new MultiPartEmail();


                email.addTo(to);
                email.setFrom(from, name);
                email.setSubject(subject);
                email.setMsg(msg);
                email.setHostName(fbPanel.jtMailHost.getText());
                if (attachment != null) {
                    email.attach(attachment);
                }
                email.send();
            } catch (Exception e) {
                response = e.getMessage();
                Logger.getLogger(SendAction.class.getPackage().getName()).info("" + e);
                failed = true;
            }

            if (failed) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES.getString("fb.msg.senderror")
                        + "\n(" + response + ").", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES.getString("fb.msg.thankyou"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }

        }
    }

    private File sendUserDir() throws IOException {
        String baseDir = System.getProperty("netbeans.user");
        Zipper zipUD = new Zipper(new File(baseDir));
        zipUD.addIncludePatterns("config/Preferences/genjfr/.*");
        zipUD.addIncludePatterns("config/Toolbars/.*");
        zipUD.addIncludePatterns("config/Windows2Local/.*");
        zipUD.addIncludePatterns("genjfr/.*");
        zipUD.addIncludePatterns("config/Preferences/org/.*");
        zipUD.addExcludePatterns("genjfr/.*\\.lck");
        File temp = File.createTempFile("genjfr", ".zip");
        zipUD.doExport(temp);
        return temp;
    }

    private class GenjLogFile {

        private File theFile;

        GenjLogFile() {
            File home = new File(EnvironmentChecker.getProperty(
                    "user.home.genj", null, "determining home directory"));
            theFile = new File(home, "genjfr.log");
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
