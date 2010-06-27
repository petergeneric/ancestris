/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.webbook.transfer;

import genj.util.GridBagHelper;
import genjfr.app.tools.webbook.WebBook;

import javax.swing.*;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.Image;
import java.io.File;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class FTPProgressStatus {

    private WebBook wbHandle;
    private String dlgTitle = "";     // Title of dialog
    private String message = "";      // First line of message
    private String status = "";       // Second line of message
    private int total = 0;            // Total value of counter
    private String button = "";       // Button text
    private int increment = 0;
    private JDialog dialog = null;
    private JPanel jpanel = null;
    private JOptionPane pane = null;
    private JProgressBar progress;
    private JLabel title, state;
    private FTPLog log;

    public FTPProgressStatus(WebBook wbHandle, String pDlgTitle, String pMessage, String pStatus, int pTotal, String pButton, FTPLog log) {
        if (pTotal == 0) {
            pTotal = 1;
        }
        this.wbHandle = wbHandle;
        this.dlgTitle = pDlgTitle;
        this.message = pMessage;
        this.status = pStatus;
        this.total = pTotal;
        this.button = pButton;
        this.log = log;

        jpanel = new JPanel();
        GridBagHelper gh = new GridBagHelper(jpanel).setInsets(new Insets(2, 2, 2, 2)).setParameter(GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL);
        title = new JLabel(message, JLabel.CENTER);
        state = new JLabel(" ", JLabel.CENTER);
        progress = new JProgressBar();
        gh.add(title, 0, 0);
        gh.add(state, 0, 9);
        gh.add(progress, 0, 10);
        Object[] buttons = {button};
        pane = new JOptionPane(jpanel, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, buttons);
        pane.addPropertyChangeListener(
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();
                        if ((e.getSource() == pane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                            interruption();
                        }
                    }
                });
    }

    @SuppressWarnings("deprecation")
    public void open() {
        JFrame frame = new JFrame();
        String fullname = (new File("report" + File.separator + "webbook" + File.separator + "upload.gif")).getAbsolutePath();
        Image logo = new ImageIcon(fullname).getImage();
        frame.setIconImage(logo);
        dialog = pane.createDialog(frame, dlgTitle);
        title.setText(message);
        state.setText(status);
        setValue(increment);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                interruption();
            }
        });
        dialog.setResizable(true);
        dialog.setBounds(dialog.getX() - 200, dialog.getY(), dialog.getWidth() + 400, dialog.getHeight());
        dialog.setModal(true);
        dialog.show();
    }

    public void setTitle(String msg) {
        this.message = msg;
        title.setText(msg);
    }

    public void setStatus(String status) {
        this.status = status;
        state.setText(status);
    }

    public void setTotal(int total) {
        if (total <= 0) {
            total = increment;
        }
        this.total = total;
        setValue(increment);
    }

    public void increment(int increment) {
        this.increment += increment;
        setValue(increment);
    }

    public void setButton(String label) {
        Object[] buttons = {label};
        this.pane.setOptions(buttons);
        //this.pane.repaint();
    }

    public boolean isActive() {
        return dialog.isShowing();
    }

    public void interruption() {
        String str = wbHandle.log.trs("upload_userCancelled");
        setStatus(str);
        //wbHandle.log.write(str);
        log.write(str);
        close();
    }

    public void close() {
        setValue(100);
        dialog.dispose();
    }

    private void setValue(int value) {
        if (total <= 0) {
            total = 1;
        }
        if (value > total) {
            value = total;
        }
        int percent = increment * 100 / total;
        progress.setValue(percent);
        dialog.setTitle(dlgTitle + " (" + percent + "%)");
    }
} // End of object
  
