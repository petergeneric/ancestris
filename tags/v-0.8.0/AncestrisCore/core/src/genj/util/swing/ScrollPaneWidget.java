/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * a custom improved scrollpane
 */
public class ScrollPaneWidget extends JScrollPane {
  
  private Draggin draggin;

  /**
   * Constructor
   */
  public ScrollPaneWidget(JComponent view) {
    super(view);
  }
  
  @Override
  public void setViewportView(Component view) {
    Component old = super.getViewport().getView();
    if (old!=null) {
      old.removeMouseListener(draggin);
      old.removeMouseMotionListener(draggin);
    }
    super.setViewportView(view);
    if (view!=null) {
      if (draggin==null)
        draggin = new Draggin();
      view.addMouseListener(draggin);
      view.addMouseMotionListener(draggin);
    }
  }

  public class Draggin extends MouseAdapter implements MouseMotionListener {
    
    private Point start = new Point();
    
    @Override
    public void mousePressed(MouseEvent e) {
      start.setLocation(e.getPoint());
    }
    @Override
    public void mouseReleased(MouseEvent e) {
      ((JComponent)e.getSource()).setCursor(null);
    }
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
      ((JComponent)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      Point v = viewport.getViewPosition();
      int dx = e.getPoint().x - start.x; 
      int dy = e.getPoint().y - start.y;
      v.x = Math.min(Math.max(0, v.x-dx), Math.max(0,viewport.getView().getWidth ()-viewport.getWidth ()));
      v.y = Math.min(Math.max(0, v.y-dy), Math.max(0,viewport.getView().getHeight()-viewport.getHeight())); 
      viewport.setViewPosition(v);
    }
  }

}
