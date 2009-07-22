package webbook;

import genj.report.Report;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class SimpleEditor extends JDialog {
    private JTextArea    _editArea;
    private JFileChooser _fileChooser = new JFileChooser();
    private Report report = null; 
    private String filename = ""; 

    public SimpleEditor(Report report, String title, String fileText, String filename) {
        this.report = report;
        this.filename = filename;
        if (fileText == null) {
           fileText = "";
           }
        _editArea = new JTextArea(fileText, 15, 50);
        _editArea.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        _editArea.setFont(new Font("helvetica", Font.PLAIN, 16));
        JScrollPane scrollingText = new JScrollPane(_editArea);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(scrollingText, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = menuBar.add(new JMenu(report.translate("indexmsg_file")));
        fileMenu.setMnemonic('F');
        fileMenu.add(new OpenAction());
        fileMenu.add(new SaveAction());
        fileMenu.addSeparator(); 
        fileMenu.add(new ExitAction());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, 0));

        JButton b1 = new JButton(report.translate("indexmsg_sae"));
        b1.setMnemonic(KeyEvent.VK_S);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                new SaveAction().actionPerformed(event);
                new ExitAction().actionPerformed(event);
            }
        });
        b1.setToolTipText(report.translate("indexmsg_saetip"));
        buttonPanel.add(b1);

        buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));

        JButton b2 = new JButton(report.translate("indexmsg_dnsae"));
        b2.setMnemonic(KeyEvent.VK_T);
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                new ExitAction().actionPerformed(event);
            }
        });
        b2.setToolTipText(report.translate("indexmsg_dnsae"));
        buttonPanel.add(b2);

        content.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(content);
        setJMenuBar(menuBar);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(title);
        setModal(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    class OpenAction extends AbstractAction {
        public OpenAction() {
            super(report.translate("indexmsg_open"));
            putValue(MNEMONIC_KEY, new Integer('O'));
        }

        public void actionPerformed(ActionEvent e) {
            int retval = _fileChooser.showOpenDialog(SimpleEditor.this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File f = _fileChooser.getSelectedFile();
                try {
                    //FileReader reader = new FileReader(f);
                    FileInputStream fis = new FileInputStream(f);
                    InputStreamReader isr = new InputStreamReader(fis, "UTF8");
                    Reader reader = new BufferedReader(isr);
                    _editArea.read(reader, "");
                } catch (IOException ioex) {
                    System.out.println(e);
                    dispose();
                }
            }
        }
    }

    class SaveAction extends AbstractAction {
        SaveAction() {
            super(report.translate("indexmsg_save"));
            putValue(MNEMONIC_KEY, new Integer('S'));
        }
        public void actionPerformed(ActionEvent e) {
            try {
                 //FileWriter writer = new FileWriter(filename);
                 FileOutputStream fos = new FileOutputStream(filename);
                 Writer writer = new OutputStreamWriter(fos, "UTF8");
                 _editArea.write(writer);
             } catch (IOException ioex) {
                 JOptionPane.showMessageDialog(SimpleEditor.this, ioex);
                 dispose();
             }
        }
    }

    class ExitAction extends AbstractAction {
        public ExitAction() {
            super(report.translate("indexmsg_exit"));
            putValue(MNEMONIC_KEY, new Integer('X'));
        }
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }
}
