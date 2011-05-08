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
package genj.view;

import genj.renderer.RenderSelectionHintKey;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

/**
 * An action for copying to an image
 */
public class ScreenshotAction extends Action2 {
  
  private final static ImageIcon IMG = new ImageIcon(ScreenshotAction.class, "images/Camera.png");
  private final static Resources RES = Resources.get(ScreenshotAction.class);
  
  private JComponent component;

  public ScreenshotAction(JComponent component) {
    setImage(IMG);
    setTip(RES.getString("screenshot"));
    this.component = component;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
    Rectangle rVisible = component.getVisibleRect();
    Rectangle r = new Rectangle(new Point(),component.getSize());
    
    if (r.width>rVisible.width || r.height>rVisible.height) {
    
      JRadioButton viewport = new JRadioButton(RES.getString("screenshot.asviewed"), true);
      JRadioButton all = new JRadioButton(RES.getString("screenshot.all", false));
      ButtonGroup group = new ButtonGroup();
      group.add(viewport);
      group.add(all);
      Box choices = new Box(BoxLayout.Y_AXIS);
      choices.add(viewport);
      choices.add(all);
      
      if (0!=DialogHelper.openDialog(getTip(), DialogHelper.QUESTION_MESSAGE, choices, Action2.okCancel(), e))
        return;

      if (viewport.isSelected())
        r = rVisible;
    }
    
    // Create image & copy
    try {
      BufferedImage image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = image.createGraphics();
      g.setRenderingHint(RenderSelectionHintKey.KEY, false);
      g.setClip(0, 0, r.width, r.height);
      g.translate(-r.x, -r.y);
      component.paint(g);
      g.dispose();
      ImageTransferable imageSelection = new ImageTransferable(image);
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      toolkit.getSystemClipboard().setContents(imageSelection, null);
    } catch (OutOfMemoryError oom) {
      long max = Runtime.getRuntime().maxMemory()/1024/1000;
      String msg = RES.getString("screenshot.oom", r.width*r.height*4/1024/1000, max, String.valueOf(max));
      Logger.getLogger("genj.view").log(Level.WARNING, msg, oom);
      DialogHelper.openDialog(getTip(), DialogHelper.ERROR_MESSAGE, msg, Action2.okOnly(), e);
    }
    
    
    // done
  }
  
  /**
   * A Transferable able to transfer an AWT Image. Similar to the JDK
   * StringSelection class.
   */
  private static class ImageTransferable implements Transferable {

    private Image image;

    private ImageTransferable(Image image) {
      this.image = image;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
      if (flavor.equals(DataFlavor.imageFlavor) == false)
        throw new UnsupportedFlavorException(flavor);
      return image;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return flavor.equals(DataFlavor.imageFlavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { DataFlavor.imageFlavor };
    }
  }
}
