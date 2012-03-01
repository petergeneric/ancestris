/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer.actions;

import com.sun.mail.smtp.SMTPTransport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class SendTranslationAction implements ActionListener {

    private final static String SEND = NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.button.send");
    private Preferences modulePreferences = NbPreferences.forModule(SendTranslationAction.class);
    private ZipArchive zipArchive = null;
    SendTranslationPanel sendTranslationPanel = null;

    private class SendMessageWorker implements Runnable {

        @Override
        public void run() {
            String TS = new SimpleDateFormat("yyMMdd-HHmm").format(new Date());
            String subject = "[" + NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.tag.subject") + " " + TS + "] ";
            subject += sendTranslationPanel.getSubjectFormattedTextField();
            String name = sendTranslationPanel.getNameFormattedTextField();
            String from = sendTranslationPanel.getEmailFormattedTextField();
            String text = sendTranslationPanel.getMessageTextArea();
            String to = sendTranslationPanel.getMailToFormattedTextField();
            String mailhost = modulePreferences.get("mail.host", "");
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

                // We need a multipart message to hold the attachment.
                MimeBodyPart mbp1 = new MimeBodyPart();
                mbp1.setText(text);

                MimeBodyPart mbp2 = new MimeBodyPart();
                String archiveName = zipArchive.getName();
                String filePath = zipArchive.getZipFile().getParent();
                String prefix = archiveName.substring(0, archiveName.indexOf('.'));
                String suffix = archiveName.substring(archiveName.indexOf('.') + 1);
                String locale = zipArchive.getTranslatedLocale().getLanguage();

                File zipOutputFile = new File(filePath + File.separator + prefix + "_" + locale + "." + suffix);
                if (!zipOutputFile.exists()) {
                    try {
                        zipOutputFile.createNewFile();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                zipArchive.saveTranslation(zipOutputFile);
                mbp2.attachFile(zipOutputFile);

                MimeMultipart mp = new MimeMultipart();
                mp.addBodyPart(mbp1);
                mp.addBodyPart(mbp2);
                msg.setContent(mp);

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
                Logger.getLogger(SendTranslationAction.class.getPackage().getName()).log(Level.INFO, "{0}", e);
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.senderror")
                        + "\n(" + response + ").", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

            } finally {
                try {
                    t.close();
                } catch (MessagingException ex) {
                    Exceptions.printStackTrace(ex);
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.thankyou"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (modulePreferences.get("mail.host", "").equals("")) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.msg.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            OptionsDisplayer.getDefault().open("SendTranslation");

        } else {
            TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
            zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
            if (zipArchive != null) {
                sendTranslationPanel = new SendTranslationPanel();
                setDefaultValues(sendTranslationPanel);
                DialogDescriptor dd = new DialogDescriptor(sendTranslationPanel, NbBundle.getMessage(this.getClass(), "SendTranslationPanel.title"));
                dd.setOptions(new Object[]{new String(SEND), DialogDescriptor.CANCEL_OPTION});
                DialogDisplayer.getDefault().createDialog(dd);
                DialogDisplayer.getDefault().notify(dd);
                if (dd.getValue().equals(SEND)) {
                    saveValues(sendTranslationPanel);
                    Thread t = new Thread(new SendMessageWorker());
                    t.start();
                }
            }
        }
    }

    private void setDefaultValues(SendTranslationPanel sendTranslationPanel) {
        sendTranslationPanel.setMailToFormattedTextField(modulePreferences.get("mailto.address", "arvernes@ancestris.org"));
        sendTranslationPanel.setNameFormattedTextField(modulePreferences.get("mail.name", ""));
        sendTranslationPanel.setEmailFormattedTextField(modulePreferences.get("mail.address", ""));
    }

    private void saveValues(SendTranslationPanel sendTranslationPanel) {
        modulePreferences.put("mail.name", sendTranslationPanel.getNameFormattedTextField());
        modulePreferences.put("mail.address", sendTranslationPanel.getEmailFormattedTextField());
        modulePreferences.put("mail.address", sendTranslationPanel.getMailToFormattedTextField());
    }
}
