/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genjfr.app.sendfeedback;


import genj.Version;
import genj.app.Images;
import genj.util.EnvironmentChecker;
import genj.util.Resources;
import genj.view.ViewManager;
import genj.window.WindowManager;
import genjfr.app.App;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class FeedbackDialog extends JPanel {
//	private final static Resources RESOURCES = Resources
//			.get(FeedbackDialog.class);
        private final static java.util.ResourceBundle RESOURCES = java.util.ResourceBundle.getBundle("genjfr/app/sendfeedback/Bundle");

	private final static String TEXTSEPARATOR = "\n=======================================\n";

	private JTextArea jtaInfo;
	private JTextField jtName;
	private JTextField jtEmail;
	private JTextField jtEmailTo;
	private JTextField jtMailHost;
	private JTextField jtSubject;
	private JTextArea jtaText;
	private JCheckBox jcbIncGenjLog;
	private SendAction acSend = new SendAction();
	private MailAction acMail = new MailAction();
	private CancelAction acCancel = new CancelAction();
	private CloseAction acClose = new CloseAction();

	/** the view manager */
	private GenjLogFile logFile = new GenjLogFile();

	// --- Constructor(s) ---

	FeedbackDialog() {


		JTextArea jtaText = new JTextArea(logFile.getContent(), 30, 30);

		jtaText.setLineWrap(true);
		jtaText.setWrapStyleWord(true);
		jtaText.setEditable(false);
		jtaText.setBorder(new EmptyBorder(5, 5, 5, 5));
//		jtaText.setSize(300, 400);
		JScrollPane jspText = new JScrollPane(jtaText);

/*
 		viewManager.getWindowManager().openWindow("genjlog",
				RESOURCES.getString("fb.title.genjlog"), Images.imgHelp,
				jspText, null, null);
*/
		String intro = new String(RESOURCES.getString("fb.intro"));

		// info
		jtaInfo = new JTextArea(intro);
		jtaInfo.setLineWrap(true);
		jtaInfo.setWrapStyleWord(true);
		jtaInfo.setEditable(false);

		// text
		setLayout(new GridBagLayout());

		GridBagHelper.addPanel(this, jspText);

		/* button panel */
		JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		if (! RESOURCES.getString("fb.mailto.default").equals("fb.mailto.default")){
			jpButtons.add(new JButton(acMail));
		}
		jpButtons.add(new JButton(acClose));

		GridBagHelper.add(this, jpButtons);
	}


	// --- Method(s) ---

	public static String getSystemInfo() {
		Properties p = System.getProperties();
		StringBuffer sb = new StringBuffer();
		sb.append("GenJ ");
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
	
	private class MailAction extends AbstractAction {

		private JPanel jpanel = new JPanel();
		
		public MailAction() {
			putValue(Action.NAME, RESOURCES.getString("fb.button.email"));
			putValue(Action.SHORT_DESCRIPTION, RESOURCES
					.getString("fb.tt.button.email"));
			// putValue(Action.SMALL_ICON, ...);
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
		}

		public void actionPerformed(ActionEvent event) {

				String intro = new String(RESOURCES.getString("fb.intro"));

				// info
				jtaInfo = new JTextArea(intro);
				jtaInfo.setLineWrap(true);
				jtaInfo.setWrapStyleWord(true);
				jtaInfo.setEditable(false);

				// text
				jpanel.setLayout(new GridBagLayout());


				GridBagHelper.add(jpanel, jtaInfo);

				GridBagHelper.addLabel(jpanel, RESOURCES.getString("fb.label.subject"));
				jtSubject = new JTextField(20);
				GridBagHelper.add(jpanel, jtSubject);

				GridBagHelper.addLabel(jpanel, RESOURCES.getString("fb.label.message"));
				jtaText = new JTextArea(getSystemInfo(), 12, 40);
				jtaText.setLineWrap(true);
				jtaText.setWrapStyleWord(true);
				jtaText.setEditable(true);
				jtaText.setBorder(new EmptyBorder(5, 5, 5, 5));
				JScrollPane jspText = new JScrollPane(jtaText);
				GridBagHelper.addPanel(jpanel, jspText);

				GridBagHelper.addLabel(jpanel, RESOURCES
						.getString("fb.label.reportername"));
				jtName = new JTextField(20);
				GridBagHelper.add(jpanel, jtName);

				GridBagHelper.addLabel(jpanel, RESOURCES
						.getString("fb.label.reportermail"));
				jtEmail = new JTextField(20);
				GridBagHelper.add(jpanel, jtEmail);

				GridBagHelper.addLabel(jpanel, RESOURCES.getString("fb.label.mailto"));
				jtEmailTo = new JTextField(20);
				jtEmailTo.setText(RESOURCES.getString("fb.mailto.default"));
				GridBagHelper.add(jpanel, jtEmailTo);

				GridBagHelper.addLabel(jpanel, RESOURCES
						.getString("fb.label.mailhost"));
				jtMailHost = new JTextField(20);
				jtMailHost.setText((String)System.getProperty("mail.host") );

				GridBagHelper.add(jpanel, jtMailHost);

				GridBagHelper.addLabel(jpanel, "");
				String fileSize = "" + logFile.getSize();
				String s = RESOURCES.getString("fb.label.includelog");
				jcbIncGenjLog = new JCheckBox(s, (logFile.getSize() > 0));
				GridBagHelper.add(jpanel, jcbIncGenjLog);
		
				/* button panel */
				JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				jpButtons.add(new JButton(acSend));
				jpButtons.add(new JButton(acCancel));

				GridBagHelper.add(jpanel, jpButtons);
				/* content */

				jtSubject.grabFocus();
				App.center.getWindowManager().openWindow("email",
						RESOURCES.getString("fb.title"), Images.imgHelp,
						jpanel, null, null);
			}
		}

	private class SendAction extends AbstractAction {

		public SendAction() {
			putValue(Action.NAME, RESOURCES.getString("fb.button.send"));
			putValue(Action.SHORT_DESCRIPTION, RESOURCES
					.getString("fb.tt.button.send"));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}

		public void actionPerformed(ActionEvent event) {
			if (jtaText.getText().trim().length() == 0) {
				JOptionPane.showMessageDialog(FeedbackDialog.this, RESOURCES
						.getString("fb.msg.noempty"), RESOURCES
						.getString("fb.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (jtMailHost != null){
				if (jtMailHost.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(FeedbackDialog.this, RESOURCES
							.getString("fb.msg.nosmtp"), RESOURCES
							.getString("fb.title"), JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					System.setProperty("mail.host",jtMailHost.getText().trim() );
				}
			}
			if (jtEmail.getText().trim().length() == 0) {
				int response = JOptionPane.showConfirmDialog(
						FeedbackDialog.this, RESOURCES
								.getString("fb.msg.nomail"), RESOURCES
								.getString("fb.title"),
						JOptionPane.YES_NO_OPTION);

				if (response == JOptionPane.NO_OPTION) {
					return;
				}
			}

			Thread t = new Thread(new SendWorker(), "SendFeedback");
			t.start();
		}

	}

	private class CancelAction extends AbstractAction {

		public CancelAction() {
			putValue(Action.NAME, RESOURCES.getString("fb.button.cancel"));
			putValue(Action.SHORT_DESCRIPTION, RESOURCES
					.getString("fb.tt.button.cancel"));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent event) {
			App.center.getWindowManager().close("email");
		}
	}

	private class CloseAction extends AbstractAction {

		public CloseAction() {
			putValue(Action.NAME, RESOURCES.getString("fb.button.close"));
			putValue(Action.SHORT_DESCRIPTION, RESOURCES
					.getString("fb.tt.button.close"));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent event) {
			App.center.getWindowManager().close("feedback");
			App.center.getWindowManager().close("email");
		}
	}

	private class SendWorker implements Runnable {

		public void run() {
			acSend.setEnabled(false);
			acCancel.setEnabled(false);
			String subject = "["+RESOURCES.getString("fb.tag.subject")+"] "+ jtSubject.getText().trim();
			String name = jtName.getText().trim();
			String email = jtEmail.getText().trim();
			String msg = jtaText.getText().trim();

			if (jcbIncGenjLog.isSelected() && logFile.getSize() > 0) {
				msg += "\n" + TEXTSEPARATOR + "genj.log:\n"
						+ logFile.getContent();
			}

			boolean failed = false;
			String response = "";

			String to = jtEmailTo.getText().trim();
			// Establish a network connection for sending mail
			try {
				URL u = new URL("mailto:" + to); // Create a mailto: URL
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
				JOptionPane.showMessageDialog(FeedbackDialog.this, RESOURCES
						.getString("fb.msg.senderror")
						+ "\n(" + response + ").", "Feedback",
						JOptionPane.ERROR_MESSAGE);
				acSend.setEnabled(true);
				acCancel.setEnabled(true);

			} else {
				JOptionPane.showMessageDialog(FeedbackDialog.this, RESOURCES
						.getString("fb.msg.thankyou"), "Feedback",
						JOptionPane.INFORMATION_MESSAGE);
				App.center.getWindowManager().close("email");
				App.center.getWindowManager().close("feedback");
			}

		}
	}

	private class GenjLogFile {
		private File theFile;

		GenjLogFile() {
			File home = new File(EnvironmentChecker.getProperty(App.class,
					"user.home.genj", null, "determining home directory"));
			theFile = new File(home, "genj.log");
		}

		long getSize() {
			return theFile.length();
		}

		String getContent() {
			String msg = "";
			try {
				BufferedReader in = new BufferedReader(new FileReader(theFile));

				while (in.ready())
					msg += in.readLine() + "\n";
			} catch (Exception e) {
			}
			return msg;
		}
	}
}

/*
 * Gridbag helper for feedback
 */
class GridBagHelper {

	// --- Constant(s) ---

	public static final Insets COMPONENT_INSETS = new Insets(5, 5, 5, 5);

	public static final Insets LABEL_INSETS = new Insets(7, 5, 0, 5);

	public static final Insets PANEL_INSETS = new Insets(5, 5, 0, 5);

	public static final Insets STRING_INSETS = new Insets(5, 0, 0, 5);

	// --- Method(s) ---

	public static void add(Container p, Component c, Insets insets,
			boolean fill, int anchor) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = anchor;
		if (fill) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
		}
		gbc.insets = insets;
		gbc.weightx = 1;
		((GridBagLayout) p.getLayout()).setConstraints(c, gbc);
		p.add(c);
	}

	public static void add(Container p, Component c, Insets insets, boolean fill) {
		add(p, c, insets, fill, GridBagConstraints.NORTHWEST);
	}

	public static void add(Container p, Component c, boolean fill) {
		add(p, c, COMPONENT_INSETS, fill);
	}

	public static void add(Container p, Component c) {
		add(p, c, COMPONENT_INSETS, true);
	}

	public static JLabel add(Container p, String s) {
		JLabel l = new JLabel(s);
		add(p, l, STRING_INSETS, true);
		return l;
	}

	/**
	 * Adds Component without a break.
	 */
	public static void addComponent(Container p, Component c) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = COMPONENT_INSETS;
		((GridBagLayout) p.getLayout()).setConstraints(c, gbc);
		p.add(c);
	}

	/**
	 * Print label a bit more south than the rest of the controls.
	 */
	public static JLabel addLabel(Container p, String s) {
		JLabel l = new JLabel(s);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = LABEL_INSETS;
		((GridBagLayout) p.getLayout()).setConstraints(l, gbc);
		p.add(l);

		return l;
	}

	public static void addPanel(Container p, Component c) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = PANEL_INSETS;
		gbc.weightx = 1;
		gbc.weighty = 1;
		((GridBagLayout) p.getLayout()).setConstraints(c, gbc);
		p.add(c);
	}

	public static void addVerticalSpacer(Container p) {
		Canvas c = new Canvas();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1;
		((GridBagLayout) p.getLayout()).setConstraints(c, gbc);
		p.add(c);
	}

}
