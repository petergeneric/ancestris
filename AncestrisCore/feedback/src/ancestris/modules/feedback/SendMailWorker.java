/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.feedback;

import ancestris.util.swing.DialogManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
    private File attachedZIP;
    private File attachedFile;
    private Preferences modulePreferences = NbPreferences.forModule(SendMailWorker.class);

    public SendMailWorker(String name, String from, String to, String subject, String messageBody, File attachedZIP, File attachedFile) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.messageBody = messageBody;
        this.attachedZIP = attachedZIP;
        this.attachedFile = attachedFile;
    }

    @Override
    public void run() {
        
        if (!checkConnection()) {
            return;
        }
        
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
        if (session != null) {
            logger.log(Level.INFO, "... done");
        } else {
            logger.log(Level.INFO, "... canceled by user");
            return;
        }

        /*
         * Construct the message and send it.
         */
        progressHandle.start();
        logger.log(Level.INFO, "create message ...");
        Message msg = createMessage(session);
        logger.log(Level.INFO, "... done");
        
        logger.log(Level.INFO, "sending ...");
        boolean ok = false;
        Exception exception = null;
        try {
            try {
                Transport.send(msg);
                logger.log(Level.INFO, "... done");
                progressHandle.finish();
                DialogManager.create(NbBundle.getMessage(this.getClass(), "fb.title"), NbBundle.getMessage(this.getClass(), "fb.msg.thankyou")).show();
                ok = true;
            } catch (Exception ex) {
                exception = ex;
                logger.log(Level.SEVERE, "{0}", ex);
            }
        } catch (Exception ex) {
            exception = ex;
            progressHandle.finish();
            logger.log(Level.SEVERE, "{0}", ex);
        }
        if (!ok) {
            progressHandle.finish();
            DialogManager.createError(NbBundle.getMessage(this.getClass(), "fb.title"), 
                                      NbBundle.getMessage(this.getClass(), "fb.msg.senderror") + "\n\n(" + exception.getMessage() + ").")
                    .show();
        }
    }

    private String getPassword() {
        PasswordPanel passwordPanel = new PasswordPanel();
        if (DialogManager.OK_OPTION == DialogManager.create(NbBundle.getMessage(this.getClass(), "FeedBackPasswordPanel.title"), passwordPanel)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .setDialogId(this.getClass())
                .show()) {
            return passwordPanel.getPassword();
        } else {
            return null;
        }
        
    }

    private boolean checkConnection() {
        try {
            new URL("https://www.ancestris.org/").openStream();
        } catch (IOException ex) {
            DialogManager.createError(
                    NbBundle.getMessage(this.getClass(), "fb.title"),
                    NbBundle.getMessage(this.getClass(), "fb.nointernet") + "\n" + NbBundle.getMessage(this.getClass(), "fb.msg.senderror"))
                    .show();
            return false;
        }
        return true;
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
                    return new PasswordAuthentication(modulePreferences.get("mail.host.login", "username"), getPassword());
                }
            });
        } else {
            logger.log(Level.INFO, "SSL session without Authentication");
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
                            return new PasswordAuthentication(modulePreferences.get("mail.host.login", "username"), getPassword());
                        }
                    });
        } else {
            logger.log(Level.INFO, "TLS session without Authentication");
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
            logger.log(Level.INFO, "session without Authentication");
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
            for (String recipient : to.split(",")) {
                InternetAddress recipientInternetAddress = new InternetAddress(recipient);
                logger.log(Level.INFO, "addRecipient {0}", recipientInternetAddress);
                message.addRecipient(Message.RecipientType.TO, recipientInternetAddress);
            }
            logger.log(Level.INFO, "setSubject {0}", subject);
            message.setSubject(subject);
            if (attachedZIP != null || attachedFile != null) {
                // We need a multipart message to hold the attachment.
                MimeBodyPart mbp1 = new MimeBodyPart();
                mbp1.setText(messageBody);
                MimeBodyPart mbp2 = null;
                MimeBodyPart mbp3 = null;
                if (attachedZIP != null) {
                    mbp2 = new MimeBodyPart();
                    mbp2.attachFile(attachedZIP);
                }
                if (attachedFile != null) {
                    mbp3 = new MimeBodyPart();
                    mbp3.attachFile(attachedFile);
                }
                MimeMultipart mp = new MimeMultipart();
                mp.addBodyPart(mbp1);
                if (mbp2 != null) {
                    mp.addBodyPart(mbp2);
                }
                if (mbp3 != null) {
                    mp.addBodyPart(mbp3);
                }
                message.setContent(mp);
                message.setHeader("X-Mailer", "smtpsend");
                message.setSentDate(new Date());
            } else {
                message.setText(messageBody);

            }
            return message;
        } catch (Exception ex) {
            DialogManager.createError(
                    NbBundle.getMessage(this.getClass(), "fb.title"),
                    ex.getLocalizedMessage() + "\n\n" + NbBundle.getMessage(this.getClass(), "fb.msg.senderror"))
                    .show();
            logger.log(Level.INFO, ex.getLocalizedMessage());
            return null;
        }
    }
}
