/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.extensions.feedback;

import com.sun.mail.smtp.SMTPTransport;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class SendAction implements ActionListener {

    private final static java.util.ResourceBundle RESOURCES = java.util.ResourceBundle.getBundle("ancestris/extensions/feedback/Bundle");
    private final static String TEXTSEPARATOR = "\n=======================================\n";
    private final static String SEND = new String(NbBundle.getMessage(SendAction.class, "SEND_BUTTON"));
    private Preferences modulePreferences = NbPreferences.forModule(FeedBackPlugin.class);
    private File userDir;
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

    public void actionPerformed(ActionEvent e) {

        try {
            userDir = sendUserDir();
        } catch (Exception ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES.getString("fb.msg.senderror")
                    + "\n(" + ex.getMessage() + ").", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        if (modulePreferences.get("mail.host", "").equals("")) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES.getString("fb.msg.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } else {
            fbPanel = new FeedbackPanel(userDir.length());

            setDefaultValues(fbPanel);

            DialogDescriptor dd = new DialogDescriptor(fbPanel, NbBundle.getMessage(this.getClass(), "FeedbackPanel.title"));
            dd.setOptions(new Object[]{new String(SEND), DialogDescriptor.CANCEL_OPTION});
            DialogDisplayer.getDefault().createDialog(dd);
            DialogDisplayer.getDefault().notify(dd);
            if (dd.getValue().equals(SEND)) {
                // on sauvegarde qq valeurs par defaut
                saveDefaultValues(fbPanel);
                Thread t = new Thread(new SendWorker(), "SendFeedback");
                t.start();
            }
        }
    }

    public static String getSystemInfo() {
        Properties p = System.getProperties();
        StringBuilder sb = new StringBuilder();
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
        sb.append(TEXTSEPARATOR).append(RESOURCES.getString("fb.text.comment"));
        sb.append("\n");
        return sb.toString();
    }

    private class SendWorker implements Runnable {

        public void run() {
            String TS = new SimpleDateFormat("yyMMdd-HHmm").format(new Date());
            String subject = "[" + RESOURCES.getString("fb.tag.subject") + " " + TS + "] ";
            subject += fbPanel.jtSubject.getText().trim();
            String name = fbPanel.jtName.getText().trim();
            String from = fbPanel.jtEmail.getText().trim();
            String text = fbPanel.jtaText.getText().trim();
            String to = fbPanel.jtEmailTo.getText().trim();
            String mailhost = modulePreferences.get("mail.host", "");
            boolean failed = false;
            String response = "";

            Properties props = System.getProperties();
            props.put("mail.smtp.host", mailhost);
            props.put("mail.smtp.auth", "true");
            if (modulePreferences.getBoolean("mail.host.TLSSupport", false) == true) {
                props.put("mail.smtp.starttls.enable", "true");
            }
            props.put("mail.smtp.port", modulePreferences.get("mail.host.port", "25"));

            // Get a Session object
            Session session = Session.getInstance(props, null);

            /*
             * Construct the message and send it.
             */
            Message msg = new MimeMessage(session);
            SMTPTransport t = null;

            try {
                msg.setFrom(new InternetAddress(from));
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                msg.setSubject(subject);

                // Attach the specified file.
                if (fbPanel.jcbIncGenjLog.isSelected()) {
                    // We need a multipart message to hold the attachment.
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    mbp1.setText(text);
                    MimeBodyPart mbp2 = new MimeBodyPart();
                    mbp2.attachFile(userDir);
                    MimeMultipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);
                    mp.addBodyPart(mbp2);
                    msg.setContent(mp);
                } else {
                    msg.setText(text);
                }

                msg.setHeader("X-Mailer", "smtpsend");
                msg.setSentDate(new Date());

                t = (SMTPTransport) session.getTransport("smtp");

                if (modulePreferences.getBoolean("mail.host.AuthenticationRequired", false) == true) {
                    t.connect(modulePreferences.get("mail.host.login", ""), modulePreferences.get("mail.host.password", ""));
                } else {
                    t.connect();
                }
                t.sendMessage(msg, msg.getAllRecipients());
            } catch (Exception e) {
                response = e.getMessage();
                Logger.getLogger(SendAction.class.getPackage().getName()).log(Level.INFO, "{0}", e);
                failed = true;
            } finally {
                try {
                    t.close();
                } catch (MessagingException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
