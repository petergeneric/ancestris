/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.utils;

import com.sun.mail.smtp.SMTPTransport;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.ancestris.trancestris.application.actions.DownloadBundleAction;
import org.ancestris.trancestris.application.actions.SendTranslationAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
public class SendMessageWorker implements Runnable {

    private final static Logger logger = Logger.getLogger(SendMessageWorker.class.getName());
    private String name;
    private String from;
    private String to;
    private String subject;
    private String messageBody;
    private File attachedFile;
    private Preferences modulePreferences = NbPreferences.forModule(SendMessageWorker.class);

    public SendMessageWorker(String name, String from, String to, String subject, String messageBody, File attachedFile) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.messageBody = messageBody;
        this.attachedFile = attachedFile;
    }

    @Override
    public void run() {
        Session session = null;
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DownloadBundleAction.class, "SendTranslationAction.SendProgress"));

        // Get a Session object
        logger.log(Level.INFO, "Get session");
        if (modulePreferences.getBoolean("mail.host.TLSSupport", false) == true) {
            session = this.createTLSSession();
        } else if (modulePreferences.getBoolean("mail.host.SSLSupport", false) == true) {
            session = this.createSSLSession();
        } else {
            session = this.createSession();
        }

        /*
         * Construct the message and send it.
         */
        progressHandle.start();
        logger.log(Level.INFO, "create message ...");
        Message msg = createMessage(session);
        logger.log(Level.INFO, "... done");

        try {
            logger.log(Level.INFO, "sending ...");
            Transport.send(msg);
            logger.log(Level.INFO, "... done");
            progressHandle.finish();
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.thankyou"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (MessagingException ex) {
            progressHandle.finish();
            logger.log(Level.SEVERE, "{0}", ex);
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.senderror")
                    + "\n(" + ex.getMessage()
                    + ").", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        /*
        SMTPTransport t = null;

        try {
        progressHandle.start();
        logger.log(Level.INFO, "session.getTransport({0})", "smtp");
        t = (SMTPTransport) session.getTransport("smtp");

        if (modulePreferences.getBoolean("mail.host.AuthenticationRequired", false) == true) {
        logger.log(Level.INFO, "connecting login {0} ...", modulePreferences.get("mail.host.login", "?"));
        t.connect(modulePreferences.get("mail.host.login", ""), modulePreferences.get("mail.host.password", "?"));
        logger.log(Level.INFO, "connected");
        } else {
        logger.log(Level.INFO, "connecting without password...");
        t.connect();
        logger.log(Level.INFO, "connected");
        }
        logger.log(Level.INFO, "sending ...");
        t.sendMessage(msg, msg.getAllRecipients());
        logger.log(Level.INFO, "message sent");
        progressHandle.finish();

        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.thankyou"), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
        } catch (Exception e) {
        Logger.getLogger(SendTranslationAction.class.getPackage().getName()).log(Level.INFO, "{0}", e);
        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.senderror")
        + "\n(" + e.getMessage() + ").", NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
        } finally {
        try {
        t.close();
        } catch (MessagingException ex) {
        Exceptions.printStackTrace(ex);
        }
        }
         */

    }

    private Session createSSLSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", modulePreferences.get("mail.host.port", "465"));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.host", modulePreferences.get("mail.host", ""));
        props.put("mail.smtp.port", modulePreferences.get("mail.host.port", "465"));

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(modulePreferences.get("mail.host.login", "username"), modulePreferences.get("mail.host.password", "password"));
                    }
                });

        return session;
    }

    private Session createTLSSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", modulePreferences.get("mail.host.port", "25"));
        props.put("mail.smtp.host", modulePreferences.get("mail.host", ""));

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(modulePreferences.get("mail.host.login", "username"), modulePreferences.get("mail.host.password", "password"));
                    }
                });
        return session;
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", modulePreferences.get("mail.host", ""));
        props.put("mail.smtp.port", modulePreferences.get("mail.host.port", "25"));
        if (modulePreferences.getBoolean("mail.host.AuthenticationRequired", false) == true) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }

        return Session.getInstance(props, null);
    }

    private Message createMessage(Session session) {
        try {
            Message message = new MimeMessage(session);
            InternetAddress fromInternetAddress = new InternetAddress(from);
            logger.log(Level.INFO, "setFrom {0}", fromInternetAddress);
            message.setFrom(fromInternetAddress);
            InternetAddress toInternetAddress = new InternetAddress(to);
            logger.log(Level.INFO, "setRecipient {0}", toInternetAddress);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            logger.log(Level.INFO, "setSubject {0}", subject);
            message.setSubject(subject);
            // We need a multipart message to hold the attachment.
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(messageBody);
            MimeBodyPart mbp2 = new MimeBodyPart();
            mbp2.attachFile(attachedFile);
            MimeMultipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            message.setContent(mp);
            message.setHeader("X-Mailer", "smtpsend");
            message.setSentDate(new Date());
            return message;
        } catch (MessagingException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
}
