/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.view;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.renderer.RenderSelectionHintKey;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;

/**
 * An action for copying to an image
 */
public class ScreenshotAction extends AbstractAncestrisAction {

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

        ScreenshotPanel panel = new ScreenshotPanel(component);

        Object o = DialogManager.create(getTip(), panel).setOptionType(DialogManager.OK_CANCEL_OPTION).show();
        panel.savePreferences();
        if (o != DialogManager.OK_OPTION) {
            return;
        }

        // Create image & copy
        final ImageCreator imageCreator = new ImageCreator(panel);
        final ProgressHandle ph = ProgressHandle.createHandle(RES.getString("progressTask"), () -> imageCreator.finish());
        imageCreator.setProgress(ph);
        ph.start();
        (new Thread(imageCreator)).start();

        // done
    }

    public void clearClipboard() {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[0];
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return false;
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    throw new UnsupportedFlavorException(flavor);
                }
            }, null);
        } catch (IllegalStateException e) {
        }
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

        public void clear() {
            image = null;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(DataFlavor.imageFlavor) == false) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.imageFlavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }
    }

    
    public class ImageCreator implements Runnable {
        
        private int capX = 0;
        private int capY = 0;
        private int capW = 0;
        private int capH = 0;
        private ScreenshotPanel panel = null;
        private ProgressHandle ph = null;
        
        public ImageCreator(ScreenshotPanel scp) {
            this.panel = scp;
            this.capX = panel.getCaptureX();
            this.capY = panel.getCaptureY();
            this.capW = panel.getCaptureW();
            this.capH = panel.getCaptureH();
        }
        
        public void setProgress(ProgressHandle ph) {
            this.ph = ph;
        }

        @Override
        public void run() {
        
            ImageTransferable imageSelection = null;
            BufferedImage image = null;
            String msg = "";
            try {
                image = BigBufferedImage.create(new File(System.getProperty("java.io.tmpdir")), capW, capH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setRenderingHint(RenderSelectionHintKey.KEY, false);
                g.setClip(0, 0, capW, capH);
                g.translate(-capX, -capY);
                component.paint(g);
                g.dispose();

                if (panel.isClipboard()) {
                    clearClipboard();
                    imageSelection = new ImageTransferable(image);
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    toolkit.getSystemClipboard().setContents(imageSelection, null);
                    msg = RES.getString("screenshot.copiedToClipboard");
                } else if (panel.isFile()) {
                    File outputfile = new File(panel.getFile());
                    ImageIO.write(image, "png", outputfile); // png allows bigger files
                    msg = RES.getString("screenshot.copiedToFile", outputfile.getAbsolutePath());
                } else {
                    msg = RES.getString("screenshot.nothingToDo");
                    return;
                }
                finish();
                DialogManager.create(getTip(), msg).show();

            } catch (OutOfMemoryError oom) { // we should never get there because calcSize is lower than maxSize, but just in case, I leave this old code below.
                finish();
                long maxSize = Runtime.getRuntime().freeMemory() / 1024 / 1024;
                long calcSize = capW * capH * 8 / 1024 / 1024;  // 8 is depth of image (?)
                msg = RES.getString("screenshot.oom", calcSize, maxSize, String.valueOf(maxSize));
                Logger.getLogger("ancestris.view").log(Level.WARNING, msg, oom);
                DialogManager.createError(getTip(), msg).show();

            } catch (IOException ex) {
                finish();
                Exceptions.printStackTrace(ex);
                
            }

            // Clear all used memory
            if (panel.isClipboard()) {
                clearClipboard();
            }
            if (imageSelection != null) {
                imageSelection.clear();
            }
            image = null;
            imageSelection = null;
            
            // done

        } // run

        private boolean finish() {
            ph.finish();
            return true;
        }

    }
    
    
}
