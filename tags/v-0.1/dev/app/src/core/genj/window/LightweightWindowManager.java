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
package genj.window;

import genj.util.Registry;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * The window manager for 'lightweight' JInternalFrames etc.
 */
public class LightweightWindowManager extends DefaultWindowManager {
  
  /** one desktop */
  private JDesktopPane desktop;
  
  /** 
   * Constructor
   */
  public LightweightWindowManager(Registry registry, ImageIcon defaultDialogImage) {
    super(registry, defaultDialogImage);
  }
  
  /**
   * Accessor to desktop
   */
  private JDesktopPane getDesktop(String title, ImageIcon img) {
    // already there?
    if (desktop!=null) 
      return desktop;
    // create one
    desktop = new JDesktopPane() {
      /** max window */
      public Dimension getPreferredSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
      }
    };
    // and show
    JFrame frame = new JFrame(title);
    frame.setIconImage(img.getImage());
    frame.getContentPane().add(new JScrollPane(desktop));
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
    // done
    return desktop; 
  }

  /**
   * @see genj.window.DefaultWindowManager#show(java.lang.String)
   */
  public boolean show(String key) {
    // one of our internals?
    Object frame = recall(key);
    if (frame instanceof JInternalFrame) {
      ((JInternalFrame)frame).toFront();
    }
    // continue in default
    return super.show(key);
  }
  
  /**
   * Our implementation for opening a frame
   */
  protected Component openWindowImpl(final String key, String title, ImageIcon image, JComponent content, JMenuBar menu, Rectangle bounds, boolean maximized, final Action onClosing) {
    
    // Create a frame
    final JInternalFrame frame = new JInternalFrame(title, true, true, true, true) {
      /** our dispose serves as onClose - WindowListener.onClose() is one frame too late */
      public void dispose() {
        // forget and keep bounds
        closeNotify(key, getBounds(), isMaximum());
        // continue
        super.dispose();
      }
    };

    // setup looks
    if (image!=null) frame.setFrameIcon(image);
    if (menu !=null) frame.setJMenuBar(menu);

    // add content
    frame.getContentPane().add(content);

    // DISPOSE_ON_CLOSE?
    if (onClosing==null) {
      frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    } else {
      // delegate responsibility to close
      frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
      frame.addInternalFrameListener(new InternalFrameAdapter() {
        public void internalFrameClosing(InternalFrameEvent e) {
          onClosing.actionPerformed(new ActionEvent(this, 0, key));
        }
      });
    }

    // place
    JDesktopPane desktop = getDesktop(title, image);
    Rectangle screen = new Rectangle(desktop.getSize());
    
    if (bounds==null) { 
      frame.pack();
      Dimension dim = frame.getSize();
      bounds = new Rectangle(screen.width/2-dim.width/2, screen.height/2-dim.height/2,dim.width,dim.height);
    }
    frame.setBounds(bounds.intersection(screen));
    
    if (maximized) try {
      frame.setMaximum(true);
    } catch (PropertyVetoException veto) {
    }

    // show
    desktop.add(frame);
    
    frame.show();
    
    // done
    return frame;
  }
  
  /**
   * @see genj.window.WindowManager#closeFrame(java.lang.String)
   */
  public void close(String key) {
    
    Object frame = recall(key);
    if (frame instanceof JInternalFrame) {
      ((JInternalFrame)frame).dispose();
    }
    
    // delegate to super
    super.close(key);
    
    // done
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponents()
   */
  public List getRootComponents() {
    List result = super.getRootComponents();
    if (desktop!=null)
      result.add(desktop);
    return result;
  }
  
  /**
   * @see genj.window.WindowManager#getContent(String)
   */
  public JComponent getContent(String key) {

    // lookup frame
    Object frame = recall(key);
    if (frame instanceof JInternalFrame)
      return (JComponent)((JInternalFrame)frame).getContentPane().getComponent(0);
    
    // delegate to super
    return super.getContent(key);
  }
  
} //DefaultWindowManager