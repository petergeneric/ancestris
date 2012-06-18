/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.feedback;

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
public class SendMailWorker implements Runnable {

    private final static Logger logger = Logger.getLogger(SendMailWorker.class.getName());
    private String name;
    private String from;
    private String to;
    private String subject;
    private String messageBody;
    private File attachedFile;
    private Preferences modulePreferences = NbPreferences.forModule(SendMailWorker.class);

    public SendMailWorker(String name, String from, String to, String subject, String messageBody, File attachedFile) {
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
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(this.getClass(), "SendMailWorker.Sending-In-Progress"));

        // Get a Session object
        if (modulePreferences.getBoolean("mail.host.TLSEncryption", false) == true) {
            logger.log(Level.INFO, "Get TLS session ...");
            session = this.createTLSSession();
        } else if (modulePreferences.getBoolean("mail.host.SSLEncryption", false) == true) {
            logger.log(Level.INFO, "Get SSL session ...");
            session = this.createSSLSession();
        } else {
            logger.log(Level.INFO, "Get no encryption session ...");
            session = this.createSession();
        }
        logger.log(Level.INFO, "... done");

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
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "fb.msg.thankyou"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (MessagingException ex) {
            progressHandle.finish();
            logger.log(Level.SEVERE, "{0}", ex);
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "fb.msg.senderror")
                    + "\n(" + ex.getMessage()
                    + ").", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private Session createSSLSession() {
        Session session = null;
        Properties props = new Properties();
        props.put("mail.smtp.socketFactory.port", modulePreferences.get("mail.host.port", "465"));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.host", modulePreferences.get("mail.host", ""));
        props.put("mail.smtp.port", modulePreferences.get("mail.host.port", "465"));

        if (modulePreferences.getBoolean("mail.host.AuthenticationRequired", false) == true) {
            logger.log(Level.INFO, "Authenticated SSL session");
            props.put("mail.smtp.auth", "true");
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {

                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(modulePreferences.get("mail.host.login", "username"), modulePreferences.get("mail.host.password", "password"));
                        }
                    });
        } else {
            logger.log(Level.INFO, "SSL session without Authenticatication");
            props.put("mail.smtp.auth", "false");
            session = Session.getInstance(props, null);
        }

        return session;
    }

    private Session createTLSSession() {
        Session session = null;
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", modulePreferences.get("mail.host.port", "25"));
        props.put("mail.smtp.host", modulePreferences.get("mail.host", ""));

        if (modulePreferences.getBoolean("mail.host.AuthenticationRequired", false) == true) {
            logger.log(Level.INFO, "Authenticated TLS session");
            session = Session.getInstance(props,
                    new javax.mail.Authenticator() {

                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(modulePreferences.get("mail.host.login", "username"), modulePreferences.get("mail.host.password", "password"));
                        }
                    });
        } else {
            logger.log(Level.INFO, "TLS session without Authenticatication");
            props.put("mail.smtp.auth", "false");
            session = Session.getInstance(props, null);
        }

        return session;
    }

    private Session createSession() {
        Session session = null;
        Properties props = new Properties();

        props.put("mail.smtp.host", modulePreferences.get("mail.host", ""));
        props.put("mail.smtp.port", modulePreferences.get("mail.host.port", "25"));

        if (modulePreferences.getBoolean("mail.host.AuthenticationRequired", false) == true) {
            logger.log(Level.INFO, "Authenticated session");
            props.put("mail.smtp.auth", "true");
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {

                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(modulePreferences.get("mail.host.login", "username"), modulePreferences.get("mail.host.password", "password"));
                        }
                    });
        } else {
            logger.log(Level.INFO, "session without Authenticatication");
            props.put("mail.smtp.auth", "false");
            session = Session.getInstance(props, null);
        }

        return session;
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
            if (attachedFile != null) {
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
            } else {
                message.setText(messageBody);

            }
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
