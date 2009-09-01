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
package genj.util.swing;

import genj.util.GridBagHelper;
import genj.util.Trackable;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * A Dialog used to show the progress of an operation
 */
public class ProgressWidget extends JPanel {

  private final static String
    OPTION_CANCEL = UIManager.getString("OptionPane.cancelButtonText");

  /** using a progress bar for 0-100 */
  private JProgressBar  progress;

  /** what we track */
  private Trackable     trackable;
  
  /** the thread that'll finish */
  private Thread        worker;
  
  /** label for state */
  private JLabel        state;
  
  /** timer */
  private Timer timer;
  
  /**
   * Constructor
   */
  public ProgressWidget(Trackable trAckable, Thread woRker) {

    // remember
    trackable  = trAckable;
    worker     = woRker;

    // prepare components
    GridBagHelper gh = new GridBagHelper(this)
      .setInsets(new Insets(2,2,2,2))
      .setParameter(GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL);

    // .. explanation
    state = new JLabel(" ",JLabel.CENTER);
    gh.add(state, 0, 0);

    // .. progess
    progress = new JProgressBar();
    gh.add(progress, 0, 1);

    // prepare timer
    timer = new Timer(100, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // update progress bar      
        progress.setValue(trackable.getProgress());
        // update state
        state.setText(trackable.getState());
        // still going?
        if (!worker.isAlive())
          timer.stop();
        // done for now
      }
    });
       
    // done
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // start timer
    timer.start();

    // continue
    super.addNotify();
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    
    // make sure timer is stopped
    timer.stop();
    
    // cancel trackable if still running
    trackable.cancelTrackable();
    
    // continue
    super.removeNotify();
  }

} //ProgressWidget
