/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard.tools;

import ancestris.modules.editors.standard.IndiPanel;
import static ancestris.modules.editors.standard.tools.Utils.getImageFromFile;
import ancestris.util.swing.DialogManager;
import genj.io.FileAssociation;
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.io.input.URLInput;
import genj.renderer.RenderSelectionHintKey;
import genj.view.BigBufferedImage;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class ImagePanel extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger("ancestris.app");

    private IndiPanel callingPanel = null;
    private BufferedImage IMG_MAIN = null;
    private BufferedImage IMG_DEFAULT = null;

    private static final int DEFAULT_WIDTH = 197, DEFAULT_HEIGHT = 140;
    private static final String CROPPED = "-cropped";
    private BufferedImage image = null;
    private InputSource inputSource = null;
    private InputSource mainInputSource = null;

    private int x, y;
    private static int startX, startY;
    private double sourceZoom;
    private boolean ready;

    private final static RenderingHints TEXT_RENDER_HINTS = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    private final static RenderingHints IMAGE_RENDER_HINTS = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private final static RenderingHints RENDER_HINTS = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    /**
     * Creates new form imagePanel
     */
    public ImagePanel(IndiPanel callingPanel) {
        this.callingPanel = callingPanel;
        this.x = 0;
        this.y = 0;
        this.startX = 0;
        this.startY = 0;
        this.ready = false;
        initComponents();
    }
    
    public void setMedia(InputSource is, BufferedImage defaultImage) {
        setMedia(is, defaultImage, false);
    }

    public void setMedia(InputSource is, BufferedImage defaultImage, boolean isMainImage) {
        
        IMG_DEFAULT = defaultImage;

        // 2022-01-20-FL: TODO The imageIO.read() behind getImageFromFile() takes a while for certain pictures
        // so do not reload image if we deal with the main image that has already been loaded or if the image is identical
        if (isMainImage && mainInputSource != null && mainInputSource == is && IMG_MAIN != null) {
            inputSource = mainInputSource;
            image = IMG_MAIN;
        } else if (inputSource != is) {
            inputSource = is;
            image = getImageFromFile(inputSource, getClass(), defaultImage);
            if (isMainImage && image != defaultImage) {
                mainInputSource = is;
                IMG_MAIN = image;
            }
        }
        
        if (image == null || is == null) {
            image = defaultImage;
        }

        final ImagePanel ip = this;
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                sourceZoom = (double) (ip.getWidth()) / (double) image.getWidth();
                double s2 = (double) (ip.getHeight()) / (double) image.getHeight();
                if (s2 > sourceZoom) {
                    sourceZoom = s2;
                }
                if (sourceZoom > 20) {
                    sourceZoom = 20;
                }
                if (sourceZoom < 0.1) {
                    sourceZoom = 0.1;
                }
                x = (int) ((ip.getWidth() / 2 - (int) (image.getWidth() * sourceZoom / 2)) / sourceZoom);
                y = (int) ((ip.getHeight() / 2 - (int) (image.getHeight() * sourceZoom / 2)) / sourceZoom);
                ready = true;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        if (!ready) {
            return;
        }
        Graphics2D g2d = (Graphics2D) grphcs;
        applyRenderHints(g2d);
        g2d.scale(sourceZoom, sourceZoom);
        g2d.drawImage(image, x, y, null);
        ready = true;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static void applyRenderHints(Graphics2D g2d) {
        g2d.setRenderingHints(TEXT_RENDER_HINTS);
        g2d.setRenderingHints(IMAGE_RENDER_HINTS);
        g2d.setRenderingHints(RENDER_HINTS);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(null);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if (callingPanel != null && evt.getButton() == MouseEvent.BUTTON1) {
            //nothing, let indiPanel manage that click
        } else if ((callingPanel != null && evt.getButton() == MouseEvent.BUTTON3 && inputSource != null) 
                || (callingPanel == null && evt.getButton() == MouseEvent.BUTTON1 && inputSource != null)) {
            if (inputSource instanceof FileInput) {
                FileAssociation.getDefault().execute(((FileInput) inputSource).getFile().getAbsolutePath());
            }
            if (inputSource instanceof URLInput) {
                FileAssociation.getDefault().execute(((URLInput) inputSource).getURL());
            }
        }
    }//GEN-LAST:event_formMouseClicked

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        startX = (int) (evt.getX() - x * sourceZoom);
        startY = (int) (evt.getY() - y * sourceZoom);
    }//GEN-LAST:event_formMousePressed

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        x = (int) ((evt.getX() - startX) / sourceZoom);
        y = (int) ((evt.getY() - startY) / sourceZoom);
        repaint();
    }//GEN-LAST:event_formMouseDragged

    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        int notches = evt.getWheelRotation();
        double zoom = Math.pow(1.1f, -notches);
        double pointX = evt.getX();
        double pointY = evt.getY();
        double zoomafter = sourceZoom * zoom;
        if (zoomafter > 20) {
            zoomafter = 20;
        }
        if (zoomafter < 0.1) {
            zoomafter = 0.1;
        }
        x -= (double) ((pointX / sourceZoom) - (double) (pointX / zoomafter));
        y -= (double) ((pointY / sourceZoom) - (double) (pointY / zoomafter));
        sourceZoom = zoomafter;

        repaint();
    }//GEN-LAST:event_formMouseWheelMoved


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    public void redraw() {
        if (IMG_DEFAULT != null) {
            setMedia(this.inputSource, IMG_DEFAULT);
        }
    }

    public InputSource getInput() {
        return inputSource;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void cropAndSave() {
        try {
            int w = this.getWidth();
            int h = this.getHeight();
            BufferedImage subImage = BigBufferedImage.create(new File(System.getProperty("java.io.tmpdir")), w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = subImage.createGraphics();
            g.setRenderingHint(RenderSelectionHintKey.KEY, false);
            g.setClip(0, 0, w, h);
            this.paint(g);
            g.dispose();

            // Define new filename
            String ext = null;
            String s = inputSource.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            String filepath;
            String c = s.contains(CROPPED) ? "i" : CROPPED;  // only add "x" after the first cropped copy
            File file = new File(inputSource.getLocation());
            if (file.exists()) {
                filepath = file.getParentFile().getAbsolutePath() + File.separator + s.substring(0, i) + c + "." + ext;
            } else {
                //TODO : Add default path when defined.
                filepath = " ";

            }

            // Save new file
            file = new File(filepath);
            ImageIO.write(subImage, ext, file);
            setMedia(InputSource.get(file).orElse(null), subImage);
            DialogManager.create(NbBundle.getMessage(ImagePanel.class, "TITL_CroppedSuccessfully"),
                    NbBundle.getMessage(ImagePanel.class, "MSG_CroppedSuccessfully", file.getName())).setMessageType(DialogManager.INFORMATION_MESSAGE).show();

        } catch (Exception e) {
            DialogManager.create(NbBundle.getMessage(ImagePanel.class, "TITL_CannotSaveCopy"), e.getLocalizedMessage()).setMessageType(DialogManager.ERROR_MESSAGE).show();
        }
    }

}
