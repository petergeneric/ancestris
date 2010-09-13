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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * A fast-drawing label containing Icon+HTML/txt
 * 
 * The following methods are overridden as a performance measure to 
 * to prune code-paths are often called in the case of renders
 * but which we know are unnecessary.  Great care should be taken
 * when writing your own renderer to weigh the benefits and 
 * drawbacks of overriding methods like these.
 */
public class HeadlessLabel extends JComponent {
    
  /** GAP */
  private int iconTextGap = 4;
  private int padding = 0;
  private String txt = "";
  private Icon icon;
  private float iconLocation = 0.0F;
  private boolean isOpaque = false;
  private Font font;
  private int horizontalAlignment = SwingConstants.LEFT;

  /**
   * Constructor
   */
  public HeadlessLabel() {
  }

  /**
   * Constructor
   */
  public HeadlessLabel(Font font) {
    setFont(font);
  }
  
  public void setHorizontalAlignment(int swingConstantAlignment) {
    switch (swingConstantAlignment) {
      case SwingConstants.LEFT:
        horizontalAlignment = SwingConstants.LEFT;
        break;
      case SwingConstants.RIGHT:
        horizontalAlignment = SwingConstants.RIGHT;
        icon = null;
        break;
      case SwingConstants.CENTER:
        horizontalAlignment = SwingConstants.CENTER;
        icon = null;
      break;
    }
    
  }
  
  public void setFont(Font set) {
    font = set;
  }
  
  public Font getFont() {
    if (font==null)
      font = super.getFont();
    return font;
  }

  /**
   * Set txt to render
   */
  public void setText(String set) {
    txt = set!=null ? set : "";
  }
  
  @Override
  public void setBorder(Border border) {
    throw new IllegalArgumentException("don't set border on headless label");
  }
  
  public void setPadding(int padding) {
    this.padding = padding;
  }
  
  /**
   * Set Image to render
   */
  public void setIcon(Icon icOn) {
    icon = icOn;
    horizontalAlignment = SwingConstants.LEFT;
  }
    
  /**
   * @see java.awt.Component#isOpaque()
   */
  public boolean isOpaque() { 
    return isOpaque;
  }
    
  /**
   * @see javax.swing.JComponent#setOpaque(boolean)
   */
  public void setOpaque(boolean set) {
    isOpaque = set;
  }
  
  /**
   * Set relative icon location
   */
  public void setIconLocation(float set) {
    iconLocation = set;
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    int 
      width, 
      height;
    FontMetrics fm = getFontMetrics(getFont());
    width = fm.stringWidth(txt);
    height = fm.getHeight();
    // check image
    if (icon!=null) {
      width += icon.getIconWidth();
      height = Math.max(height,icon.getIconHeight());
    }
    // gap?
    if (txt.length()>0&&icon!=null) 
      width += iconTextGap;
    // padding
    width += padding+padding;
    height += padding+padding;
    // done
    return new Dimension(width, height);
  }
    
  /**
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  public void paint(Graphics g) {
    Font font = getFont();
    Rectangle box = new Rectangle(new Point(), getSize());
    // fill background
    if (isOpaque) {
      g.setColor(getBackground());
      g.fillRect(box.x, box.y, box.width, box.height);
    }
    // padding?
    if (padding>0) {
      box.x+=padding;
      box.y+=padding;
      box.width-=padding+padding;
      box.height-=padding+padding;
    }
    // render icon
    if (icon!=null) {
      int
        w = icon.getIconWidth(),
        h = icon.getIconHeight();
      icon.paintIcon(null, g, 0, (int)(iconLocation*(box.height - h)));
      box.x += w+iconTextGap;
      box.width -= w+iconTextGap;
    }
    // render text
    g.setColor(getForeground());
    g.setFont(font);

    switch (horizontalAlignment) {
      case SwingConstants.RIGHT:
        GraphicsHelper.render((Graphics2D)g, txt, box.getMaxX(), box.getCenterY(), 1, 0.5);
        break;
      case SwingConstants.LEFT:
        GraphicsHelper.render((Graphics2D)g, txt, box.getMinX(), box.getCenterY(), 0, 0.5);
        break;
      case SwingConstants.CENTER:
        GraphicsHelper.render((Graphics2D)g, txt, box.getCenterX(), box.getCenterY(), 0.5, 0.5);
        break;
    }
    // done
  }
  
  /**
   * @see javax.swing.JComponent#getAlignmentY()
   */
  public float getAlignmentY() {
    return 0;
  }


  public void validate() {
  }

  public void revalidate() {
  }

  public void repaint(long tm, int x, int y, int width, int height) {
  }

  public void repaint(Rectangle r) { 
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
  }

  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
  }
    
} //HeadlessLabel
