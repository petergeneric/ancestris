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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * An updating widget showing memory consumption
 */
public class HeapStatusWidget extends JProgressBar {
  
  private final static NumberFormat FORMAT = new DecimalFormat("0.0");
  
  private MessageFormat tooltip = new MessageFormat("Heap: {0}MB used {1}MB free {2}MB max");

  /**
   * constructor
   */
  public HeapStatusWidget() {
    super(0,100);
    setValue(0);
    setBorderPainted(false);
    setStringPainted(true);
    new Timer(3000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        update();
      }
    }).start();
  }
  
  /** update status */
  private void update() {
    
    // calc values
    Runtime r = Runtime.getRuntime();
    long max = r.maxMemory();
    long free = r.freeMemory();
    long total = r.totalMemory();
    long used = total-free;
    int percent = (int)Math.round(used*100D/max);
    
    // set status
    setValue(percent);
    setString(format(used, true)+"MB ("+percent+"%)");

    // add tip
    super.setToolTipText(null);
    super.setToolTipText(tooltip.format(new String[]{ format(used, false), format(free, false), format(max, false)}));
    
    // done
  }
  
  private String format(long mb, boolean decimals) {
    double val = mb/1000000D;
    return decimals ? FORMAT.format(mb/1000000D) : Integer.toString((int)Math.round(val));
  }

  /**
   * Allow to set tooltip with placeholders {0} for used memory, {1} for free memory, {2} max memory 
   */
  public void setToolTipText(String text) {
    // remember
    this.tooltip = new MessageFormat(text);
    // blank for now
    super.setToolTipText("");
  }
  
}
