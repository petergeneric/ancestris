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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

/**
 * A simple one-child 'container' that will wrap a component
 * for a ViewPort, making sure that this component is not
 * sized larger than getPreferredSize(). If necessary the
 * component is centered.
 */
public class ViewPortAdapter extends JComponent {
  
  /** the component that we adapt */
  private JComponent comp;

  /**
   * Constructor
   */
  public ViewPortAdapter(JComponent c) {
    comp = c;
    setLayout(new GridBagLayout());
    add(comp, new GridBagConstraints());
  }
  
  @Override
  public synchronized void addMouseListener(MouseListener l) {
    comp.addMouseListener(l);
  }
  
  @Override
  public synchronized void removeMouseListener(MouseListener l) {
    comp.removeMouseListener(l);
  }
  
  @Override
  public synchronized void addMouseMotionListener(MouseMotionListener l) {
    comp.addMouseMotionListener(l);
  }
  
  @Override
  public synchronized void removeMouseMotionListener(MouseMotionListener l) {
    comp.removeMouseMotionListener(l);
  }
  
  /**
   * @see java.awt.Component#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return comp.getPreferredSize();
  }
  
  /**
   * the view
   */
  public JComponent getComponent() {
    return comp;
  }  
} //ViewPortAdapter
