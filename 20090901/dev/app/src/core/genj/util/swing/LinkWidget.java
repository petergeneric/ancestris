/**
 *
 */
package genj.util.swing;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * 
 */
public class LinkWidget extends JLabel {
  
  /** status hover */
  private boolean hover = false;

  /** action */
  private Action2 action;
  
  /** normal color */
  private Color normal;
  
  /**
   * Constructor
   */
  public LinkWidget(Action2 action) {
    this(action.getText(), action.getImage());
    setToolTipText(action.getTip());
    this.action = action;
  }
  
  /**
   * Constructor
   */
  public LinkWidget(String text, Icon img) {
    super(text, img, SwingConstants.LEFT);
    addMouseListener(new Callback());
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }
  
  /**
   * Constructor
   */
  public LinkWidget(ImageIcon img) {
    this(null, img);
  }
  
  /**
   * Constructor
   */
  public LinkWidget() {
    this(null,null);
  }
   
  /** 
   * action performed
   */
  protected void fireActionPerformed() {
    if (action!=null)
      action.actionPerformed(new ActionEvent(this, 0, ""));
  }

  /**
   * overridden paint
   */
  protected void paintComponent(Graphics g) {
    // let the UI do its thing
    super.paintComponent(g);
    // add a line
    if (!hover) return;
    g.setColor(getForeground());
    g.drawLine(1,getHeight()-1,getWidth()-1-1,getHeight()-1);
    // done
  }
  
  /**
   * A private callback code block
   */
  private class Callback extends MouseAdapter {
    
    /** click -> action */
    public void mouseClicked(MouseEvent e) {
      fireActionPerformed();
    }
    /** exit -> plain */
    public void mouseExited(MouseEvent e) {
      hover = false;
      repaint();
    }
    /** exit -> underlined */
    public void mouseEntered(MouseEvent e) {
      hover = true;
      repaint();
    }
    
  } //Callback

} //LinkWidget

