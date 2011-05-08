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

import genj.util.Trackable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * A component to show the progress of a trackable
 */
public class ProgressWidget extends JPanel {
  
  private final static ImageIcon IMG_CANCEL = new ImageIcon(ProgressWidget.class, "Cancel.png");

  /** using a progress bar for 0-100 */
  private JProgressBar  progress = new JProgressBar(0, 100);

  /** what we track */
  private Trackable     track;
  
  /** timer */
  private Timer timer;
  
  private Dimension minPreferredSize;
  
  /**
   * Constructor
   */
  public ProgressWidget(Trackable trackable) {

    super(new BorderLayout());
    
    JButton cancel = new JButton(new Cancel());
    cancel.setRequestFocusEnabled(false);
    cancel.setFocusable(false);
    cancel.setMargin(new Insets(0,0,0,0));
    
    add(progress, BorderLayout.CENTER);
    add(cancel, BorderLayout.EAST);

    progress.setStringPainted(true);
    track = trackable;

    // prepare timer
    timer = new Timer(100, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // update progress bar      
        progress.setValue(track.getProgress());
        progress.setString(track.getState());
        revalidate();
        repaint();
      }
    });
       
    // done
  }
  
  @Override
  public Dimension getPreferredSize() {
    Dimension oldMin = minPreferredSize;
    minPreferredSize = super.getPreferredSize();
    if (oldMin!=null) {
      minPreferredSize.width = Math.max(minPreferredSize.width+16, oldMin.width);
      minPreferredSize.height= Math.max(minPreferredSize.height, oldMin.height);
    }
    return minPreferredSize;
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
    // continue
    super.removeNotify();
  }
  
  private class Cancel extends Action2 {
    private Cancel() {
      setImage(IMG_CANCEL);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      track.cancelTrackable();
    }
  }

} //ProgressWidget
