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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

/**
 * The default 'heavyweight' window manager
 */
public class DefaultWindowManager extends WindowManager {

  /** screen we're dealing with */
  private Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
  
  /** a hidden default frame */
  private JFrame defaultFrame = new JFrame();
  
  /** 
   * Constructor
   */
  public DefaultWindowManager(Registry registry, ImageIcon defaultDialogImage) {
    super(registry);
    if (defaultDialogImage!=null) defaultFrame.setIconImage(defaultDialogImage.getImage());
  }
  
  /**
   * Frame implementation
   */
  protected Component openWindowImpl(final String key, String title, ImageIcon image, JComponent content, JMenuBar menu, Rectangle bounds, boolean maximized, final Action onClosing) {
    
    // Create a frame
    final JFrame frame = new JFrame() {
      /**
       * dispose is our onClose hook because
       * WindowListener.windowClosed is too 
       * late (one frame) after dispose()
       */
      public void dispose() {
        // forget about key but keep bounds
        closeNotify(key, getBounds(), getExtendedState()==MAXIMIZED_BOTH);
        // continue
        super.dispose();
      }
    };

    // setup looks
    if (title!=null) frame.setTitle(title);
    if (image!=null) frame.setIconImage(image.getImage());
    if (menu !=null) frame.setJMenuBar(menu);

    // add content
    frame.getContentPane().add(content);

    // DISPOSE_ON_CLOSE?
    if (onClosing==null) {
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    } else {
      // responsibility to dispose passed to onClosing?
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          onClosing.actionPerformed(new ActionEvent(this, 0, key));
        }
      });
    }

    // place
    if (bounds==null) {
      frame.pack();
      Dimension dim = frame.getSize();
      bounds = new Rectangle(screen.width/2-dim.width/2, screen.height/2-dim.height/2,dim.width,dim.height);
      LOG.log(Level.FINE, "Sizing window "+key+" to "+bounds+" after pack()");
    }
    frame.setBounds(bounds.intersection(screen));
    
    if (maximized)
      frame.setExtendedState(Frame.MAXIMIZED_BOTH);

    // show
    frame.setVisible(true);
    
    // done
    return frame;
  }
  
  /**
   * Dialog implementation
   */
  protected Component openNonModalDialogImpl(final String key, String title,  int messageType, JComponent content, Action[] actions, Component owner, Rectangle bounds) {

    // create an option pane
    JOptionPane optionPane = new Content(messageType, content, actions);
    
    // let it create the dialog
    final JDialog dlg = optionPane.createDialog(owner != null ? owner : defaultFrame, title);
    dlg.setResizable(true);
    dlg.setModal(false);
    if (bounds==null) {
      dlg.pack();
      if (owner!=null)
        dlg.setLocationRelativeTo(owner.getParent());
    } else {
      if (owner==null) {
        dlg.setBounds(bounds.intersection(screen));
      } else {
        dlg.setBounds(new Rectangle(bounds.getSize()).intersection(screen));
        dlg.setLocationRelativeTo(owner.getParent());
      }
    }

    // hook up to the dialog being hidden by the optionpane - that's what is being called after the user selected a button (setValue())
    dlg.addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
        closeNotify(key, dlg.getBounds(), false);
        dlg.dispose();
      }
    });
    
    // show it
    dlg.setVisible(true);
    
    // return result
    return dlg;
  }
  
  /**
   * Dialog implementation
   */
  protected Object openDialogImpl(final String key, String title,  int messageType, JComponent content, Action[] actions, Component owner, Rectangle bounds) {

    // create an option pane
    JOptionPane optionPane = new Content(messageType, content, actions);
    
    // let it create the dialog
    final JDialog dlg = optionPane.createDialog(owner != null ? owner : defaultFrame, title);
    dlg.setResizable(true);
    dlg.setModal(true);
    if (bounds==null) {
      dlg.pack();
      if (owner!=null)
        dlg.setLocationRelativeTo(owner.getParent());
    } else {
      if (owner==null) {
        dlg.setBounds(bounds.intersection(screen));
      } else {
        dlg.setBounds(new Rectangle(bounds.getSize()).intersection(screen));
        dlg.setLocationRelativeTo(owner.getParent());
      }
    }

    // hook up to the dialog being hidden by the optionpane - that's what is being called after the user selected a button (setValue())
    dlg.addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
        closeNotify(key, dlg.getBounds(), false);
        dlg.dispose();
      }
    });
    
    // show it
    dlg.setVisible(true);
    
    // return result
    return optionPane.getValue();
  }

  @Override
  public void setTitle(String key, String title) {
    
    Object framedlg = recall(key);
    
    if (framedlg instanceof JFrame) {
      ((JFrame)framedlg).setTitle(title); 
      return;
    }

    if (framedlg instanceof JDialog) {
      ((JDialog)framedlg).setTitle(title);
      return;
    }
    
  }
  
  /**
   * @see genj.window.WindowManager#show(java.lang.String)
   */
  public boolean show(String key) {

    Object framedlg = recall(key);
    
    if (framedlg instanceof JFrame) {
      ((JFrame)framedlg).toFront(); 
      return true;
    }

    if (framedlg instanceof JDialog) {
      ((JDialog)framedlg).toFront();
      return true;
    }

    return false;
  }
  
  /**
   * @see genj.window.WindowManager#closeFrame(java.lang.String)
   */
  public void close(String key) {

    Object framedlg = recall(key);
    
    if (framedlg instanceof JFrame) {
      JFrame frame = (JFrame)framedlg;
      frame.dispose(); 
      return;
    }

    if (framedlg instanceof JDialog) {
      JDialog dlg = (JDialog)framedlg;
      dlg.setVisible(false); // we're using the optionpane signal for a closing dialog: hide it
      return;
    }

    // done
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponents()
   */
  public List getRootComponents() {

    List result = new ArrayList();
    
    // loop through keys    
    String[] keys = recallKeys();
    for (int k=0; k<keys.length; k++) {
      
      Object framedlg = recall(keys[k]);

      if (framedlg instanceof JFrame)      
        result.add(((JFrame)framedlg).getRootPane());

      if (framedlg instanceof JDialog)      
        result.add(((JDialog)framedlg).getRootPane());
    }
    
    // done
    return result;
  }
  
  /**
   * @see genj.window.WindowManager#getContent(java.lang.String)
   */
  public JComponent getContent(String key) {
    
    Object framedlg = recall(key);
    
    if (framedlg instanceof JFrame)
      return (JComponent)((JFrame)framedlg).getContentPane().getComponent(0); 

    if (framedlg instanceof JDialog)
      return (JComponent)((JDialog)framedlg).getContentPane().getComponent(0);

    return null;
  }

  /**
   * Get the window for given owner component
   */  
  private Window getWindowForComponent(Component c) {
    if (c instanceof Frame || c instanceof Dialog || c==null)
      return (Window)c;
    return getWindowForComponent(c.getParent());
  }
  
} //DefaultWindowManager