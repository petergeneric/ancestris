/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

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
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
    File zipOutputFile = null;
    SendTranslationPanel sendTranslationPanel = new SendTranslationPanel();
    String archiveName = "";
    String filePath = "";
    String prefix = "";
    String suffix = "";
    String toLocale = "";
    String fromLocale = "";

    private class SendMessageWorker implements Runnable {

        @Override
        public void run() {
            String subject = sendTranslationPanel.getSubjectFormattedTextField();
            String name = sendTranslationPanel.getNameFormattedTextField();
            String from = sendTranslationPanel.getEmailFormattedTextField();
            String text = sendTranslationPanel.getMessageTextArea();
            String to = sendTranslationPanel.getMailToFormattedTextField();
            String mailhost = modulePreferences.get("mail.host", "");
            String response = "";
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DownloadBundleAction.class, "SendTranslationAction.SendProgress"));

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
                progressHandle.start();
                t.sendMessage(msg, msg.getAllRecipients());
                progressHandle.finish();

                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.thankyou"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
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

            ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
            if (zipArchive != null) {
                zipArchive.write();
                if (zipArchive.hasTranslation() == true) {
                    archiveName = zipArchive.getName();
                    filePath = zipArchive.getZipFile().getParent();
                    prefix = archiveName.substring(0, archiveName.indexOf('.'));
                    suffix = archiveName.substring(archiveName.indexOf('.') + 1);
                    toLocale = zipArchive.getToLocale().getLanguage();
                    fromLocale = zipArchive.getFromLocale().getLanguage();

                    zipOutputFile = new File(filePath + File.separator + prefix + "_" + toLocale + "." + suffix);
                    if (!zipOutputFile.exists()) {
                        try {
                            zipOutputFile.createNewFile();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    zipArchive.saveTranslation(zipOutputFile);

                    setDefaultValues(sendTranslationPanel);
                    DialogDescriptor dd = new DialogDescriptor(sendTranslationPanel, NbBundle.getMessage(this.getClass(), "SendTranslationPanel.title"));
                    dd.setOptions(new Object[]{SEND, DialogDescriptor.CANCEL_OPTION});
                    DialogDisplayer.getDefault().createDialog(dd);
                    DialogDisplayer.getDefault().notify(dd);
                    if (dd.getValue().equals(SEND)) {
                        saveValues(sendTranslationPanel);
                        Thread t = new Thread(new SendMessageWorker());
                        t.start();
                    }
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.nothingToSend"), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        }
    }

    private void setDefaultValues(SendTranslationPanel sendTranslationPanel) {
        sendTranslationPanel.setMailToFormattedTextField(modulePreferences.get("mailto.address", "francois@ancestris.org"));
        sendTranslationPanel.setNameFormattedTextField(modulePreferences.get("mail.name", ""));
        sendTranslationPanel.setEmailFormattedTextField(modulePreferences.get("mail.address", ""));
        String TS = new SimpleDateFormat(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.subject.date")).format(new Date());
        String subject = "[" + NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.subject.tag", fromLocale, toLocale) + " " + TS + "] ";
        sendTranslationPanel.setSubjectFormattedTextField(subject);
    }

    private void saveValues(SendTranslationPanel sendTranslationPanel) {
        modulePreferences.put("mail.name", sendTranslationPanel.getNameFormattedTextField());
        modulePreferences.put("mail.address", sendTranslationPanel.getEmailFormattedTextField());
        modulePreferences.put("mailto.address", sendTranslationPanel.getMailToFormattedTextField());
    }
}
