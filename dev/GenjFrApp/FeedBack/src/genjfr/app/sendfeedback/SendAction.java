/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.sendfeedback;

import genj.Version;
import genj.util.EnvironmentChecker;
import genjfr.app.App;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.prefs.Preferences;
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

    GenjLogFile logFile = new GenjLogFile();
    FeedbackPanel fbPanel = new FeedbackPanel(logFile.getSize());

    private void setDefaultValues(FeedbackPanel panel){
        panel.jtaText.setText(getSystemInfo());
        panel.jtEmailTo.setText(RESOURCES.getString("fb.mailto.default"));
        panel.jtMailHost.setText(feedbackPrefs.get("mail.host",System.getProperty("mail.host")));
        panel.jtName.setText(feedbackPrefs.get("mail.name",""));
        panel.jtEmail.setText(feedbackPrefs.get("mail.address",""));
    }
    private void saveDefaultValues(FeedbackPanel panel){
        String s = panel.jtMailHost.getText();
        if (!s.equalsIgnoreCase(System.getProperty("mail.host"))) {
            feedbackPrefs.put("mail.host", s);
        }

        feedbackPrefs.put("mail.name", panel.jtName.getText().trim());
        feedbackPrefs.put("mail.address", panel.jtEmail.getText().trim());

    }
    public void actionPerformed(ActionEvent e) {
        setDefaultValues(fbPanel);

        DialogDescriptor dd = new DialogDescriptor(fbPanel,NbBundle.getMessage(this.getClass(), "FeedbackPanel.title"));
        dd.setOptions(new Object[]{new String(SEND), DialogDescriptor.CANCEL_OPTION});
        DialogDisplayer.getDefault().createDialog(dd);
        DialogDisplayer.getDefault().notify(dd);
        if (dd.getValue().equals(SEND)){
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
		sb.append("GenjFr ");
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
			String subject = "["+RESOURCES.getString("fb.tag.subject")+" "+TS+"] ";
                        subject += fbPanel.jtSubject.getText().trim();
			String name = fbPanel.jtName.getText().trim();
			String email = fbPanel.jtEmail.getText().trim();
			String msg = fbPanel.jtaText.getText().trim();

			if (fbPanel.jcbIncGenjLog.isSelected() && logFile.getSize() > 0) {
				msg += "\n" + TEXTSEPARATOR + "genj.log:\n"
						+ logFile.getContent();
			}

			boolean failed = false;
			String response = "";

			String to = fbPanel.jtEmailTo.getText().trim();
			// Establish a network connection for sending mail
			try {
				URL u = new URL("mailto:" + to); // Create a mailto: URL
                                // TODO: utiliser MailToURLConnection
				URLConnection c = u.openConnection(); // Create a
                                					// URLConnection for it
				c.setDoInput(false); // Specify no input from this URL
				c.setDoOutput(true); // Specify we'll do output
				c.connect(); // Connect to mail host
				PrintWriter out = // Get output stream to mail host
				new PrintWriter(new OutputStreamWriter(c.getOutputStream()));

				// Write out mail headers. Don't let users fake the From address
				out.println("From: \"" + name + "\" <" + email + ">");
				out.println("To: " + to);
				out.println("Subject: " + subject);
				out.println("Content-Type: text/plain; charset="+
						System.getProperties().get("file.encoding")+
						"; format=flowed");
				out.println("Content-Transfer-Encoding: 8bit");
				out.println(); // blank line to end the list of headers

				out.println(msg);
				out.close();

			} catch (Exception e) {
				response = e.getMessage();
				failed = true;
			}

			if (failed) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES
						.getString("fb.msg.senderror")
                                                + "\n(" + response + ")."
                                                , NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);

			} else {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(RESOURCES
						.getString("fb.msg.thankyou")
                                                , NotifyDescriptor.INFORMATION_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);
			}

		}
	}

    private class GenjLogFile {

        private File theFile;

        GenjLogFile() {
            File home = new File(EnvironmentChecker.getProperty(App.class,
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
