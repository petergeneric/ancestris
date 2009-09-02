/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

import genj.util.GridBagHelper;

import javax.swing.JOptionPane;
import javax.swing.JDialog;

import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

  
/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
  public class ProgressStatus {

    private String dlgTitle = "";     // Title of dialog
    private String message = "";      // First line of message
    private String status = "";       // Second line of message
    private double total = 0;         // Total value of counter
    private String button = "";       // Button text
    
    private double increment = 0;
    private JDialog dialog = null;
    private JPanel jpanel = null;
    private JOptionPane pane = null;
    private JProgressBar  progress;
    private JLabel title, state;
    private Timer timer;
    
    public ProgressStatus(String pDlgTitle, String pMessage, String pStatus, double pTotal, String pButton) {
       
       if (pTotal == 0) pTotal = 1;
       this.dlgTitle = pDlgTitle;
       this.message = pMessage;
       this.status = pStatus;
       this.total = pTotal;
       this.button = pButton;
    
       jpanel = new JPanel();
       GridBagHelper gh = new GridBagHelper(jpanel).setInsets(new Insets(2,2,2,2)) .setParameter(GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL);
       title = new JLabel(message,JLabel.CENTER);
       state = new JLabel(" ",JLabel.CENTER);
       progress = new JProgressBar();
       gh.add(title, 0, 0);
       gh.add(state, 0, 9);
       gh.add(progress, 0, 10);

       timer = new Timer(1000, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             NumberFormat nf = NumberFormat.getIntegerInstance();
             title.setText(message);
             state.setText( nf.format((int)increment) + " " + status );
             progress.setValue((int)(increment*100 / total));
             }
          });
       
       Object[] options = { button };
       pane = new JOptionPane(jpanel, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options);
       }
    
    public void reset() {
       reset(message, status, total);
       }
    
    public void reset(String message, String status, double total) {
       timer.start();
       this.message = message;
       this.status = status;
       this.total = total;
       this.increment = 0;
       dialog = pane.createDialog(null, dlgTitle);
       dialog.setResizable(true);
       dialog.setModal(false);
       dialog.show();
       }
    
    public void setTitle(String msg) {
       this.message = msg;
       }
    
    public void show() {
       dialog.show();
       }
    
    public void hide() {
       dialog.hide();
       }
    
    public boolean isActive() {
       return dialog.isShowing();
       }
    
    public void increment(double increment) {
       this.increment += increment;
       }
    
    public double getSize() {
       return increment;
       }

    public void terminate() {
       timer.stop();
       progress.setValue(100);
       dialog.dispose();
       }
     
} // End of object
  
