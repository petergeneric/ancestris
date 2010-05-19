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
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

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
  private int ICON_TEXT_GAP = 4;

  /** simple text */
  private String txt = "";
        
  /** the view */
  private View view;
    
  /** the icon */
  private Icon icon;
  
  /** the icon location */
  private float iconLocation = 0.0F;
    
  /** whether we're opaque or not */
  private boolean isOpaque = false;
  
  /** a cached font */
  private Font font;

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
  
  public void setFont(Font set) {
    font = set;
  }
  
  public Font getFont() {
    if (font==null)
      font = super.getFont();
    return font;
  }

  /**
   * Set HTML to render
   */
  public View setHTML(String set) {
    return setView(BasicHTML.createHTMLView(this, set));
  }
  
  /**
   * Set View to render
   */
  public View setView(View set) {
    view = set;
    txt = "";
    return set;
  }
    
  /**
   * Set txt to render
   */
  public void setText(String set) {
    view = null;
    txt = set!=null ? set : "";
  }
    
  /**
   * Set Image to render
   */
  public void setIcon(Icon icOn) {
    icon = icOn;
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
    // ask rootView or text
    if (view!=null) {
      width = (int)view.getPreferredSpan(View.X_AXIS);
      height= (int)view.getPreferredSpan(View.Y_AXIS);
    } else {
      FontMetrics fm = getFontMetrics(getFont());
      width = fm.stringWidth(txt);
      height = fm.getHeight();
    }
    // check image
    if (icon!=null) {
      width += icon.getIconWidth();
      height = Math.max(height,icon.getIconHeight());
    }
    // gap?
    if ((view!=null||txt.length()>0)&&icon!=null) 
      width += ICON_TEXT_GAP;
    // max
//    Dimension max = getMaximumSize();
//    width = Math.min(max.width, width);
//    height = Math.min(max.height, height);
    // done
    return new Dimension(width, height);
  }
    
  /**
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  public void paint(Graphics g) {
    Rectangle bounds = getBounds();
    bounds.x=0;
    bounds.y=0;
    // fill background
    if (isOpaque) {
      g.setColor(getBackground());
      g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
    }
    // render icon
    if (icon!=null) {
      int
        w = icon.getIconWidth(),
        h = icon.getIconHeight();
      icon.paintIcon(null, g, 0, (int)(iconLocation*(bounds.height - h)));
      bounds.x += w+ICON_TEXT_GAP;
      bounds.width -= w+ICON_TEXT_GAP;
    }
    // render html or txt
    g.setColor(getForeground());
    if (view!=null) {
      view.setSize(bounds.width, bounds.height);
      view.paint(g, bounds);
    } else {
      Font font = getFont();
      g.setFont(font);
      g.drawString(txt, bounds.x, getFontMetrics(font).getMaxAscent());       
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
