/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.view;

import ancestris.util.swing.FileChooserBuilder;
import genj.renderer.RenderSelectionHintKey;
import genj.util.Registry;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JComponent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class ScreenshotPanel extends javax.swing.JPanel {

    private Registry registry = null;

    private ImagePanel imagePanel;
    private Rectangle rVisible = null;
    private Rectangle rWhole = null;
    private double factor = 0d;
    
    /**
     * Creates new form ScreenshotPanel
     * @param component
     */
    public ScreenshotPanel(JComponent component) {
        registry = Registry.get(getClass());
        
        initComponents();

        int width = Math.max(360, registry.get("captureWindowWidth", this.getPreferredSize().width));
        int height = Math.max(370, registry.get("captureWindowHeight", this.getPreferredSize().height));
        this.setMinimumSize(new Dimension(360, 370));
        this.setPreferredSize(new Dimension(width, height));
        
        visibleAreaButton.setSelected(registry.get("captureView", true));
        wholeAreaButton.setSelected(!visibleAreaButton.isSelected());
        clipboardButton.setSelected(registry.get("captureTarget", true));
        fileButton.setSelected(!clipboardButton.isSelected());
        borderPanel.setVisible(!visibleAreaButton.isSelected());
        fileTextField.setEnabled(!clipboardButton.isSelected());
        fileSearchButton.setEnabled(!clipboardButton.isSelected());
        msgLabel.setVisible(!visibleAreaButton.isSelected());
    
        rVisible = component.getVisibleRect();
        rWhole = new Rectangle(new Point(), component.getSize());

        // Init image panel with :
        // - a buffered image that is small enough to be displayed in the background panel
        double maxSize = Runtime.getRuntime().maxMemory() / 8;
        double capX = rWhole.width, capY = rWhole.height;
        double calcSize = capX * capY * 8;  // 8 is depth of image (?)
        factor = Math.min(1d, maxSize / calcSize);
        imagePanel.setImage(Runtime.getRuntime().maxMemory() / 8, getImageFromComponent(component, factor));
        fileTextField.setText(registry.get("captureFilename", ""));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupArea = new javax.swing.ButtonGroup();
        buttonGroupTarget = new javax.swing.ButtonGroup();
        areaLabel = new javax.swing.JLabel();
        visibleAreaButton = new javax.swing.JRadioButton();
        wholeAreaButton = new javax.swing.JRadioButton();
        targetLabel = new javax.swing.JLabel();
        clipboardButton = new javax.swing.JRadioButton();
        fileButton = new javax.swing.JRadioButton();
        fileTextField = new javax.swing.JTextField();
        fileSearchButton = new javax.swing.JButton();
        borderPanel = new javax.swing.JPanel();
        imagePanel = new ImagePanel();
        areaPanel = imagePanel;
        msgLabel = new javax.swing.JLabel();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(areaLabel, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.areaLabel.text")); // NOI18N

        buttonGroupArea.add(visibleAreaButton);
        org.openide.awt.Mnemonics.setLocalizedText(visibleAreaButton, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.visibleAreaButton.text")); // NOI18N
        visibleAreaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visibleAreaButtonActionPerformed(evt);
            }
        });

        buttonGroupArea.add(wholeAreaButton);
        org.openide.awt.Mnemonics.setLocalizedText(wholeAreaButton, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.wholeAreaButton.text")); // NOI18N
        wholeAreaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wholeAreaButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(targetLabel, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.targetLabel.text")); // NOI18N

        buttonGroupTarget.add(clipboardButton);
        org.openide.awt.Mnemonics.setLocalizedText(clipboardButton, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.clipboardButton.text")); // NOI18N
        clipboardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clipboardButtonActionPerformed(evt);
            }
        });

        buttonGroupTarget.add(fileButton);
        org.openide.awt.Mnemonics.setLocalizedText(fileButton, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.fileButton.text")); // NOI18N
        fileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileButtonActionPerformed(evt);
            }
        });

        fileTextField.setText(org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.fileTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fileSearchButton, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.fileSearchButton.text")); // NOI18N
        fileSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileSearchButtonActionPerformed(evt);
            }
        });

        borderPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        borderPanel.setPreferredSize(new java.awt.Dimension(260, 188));

        areaPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                areaPanelMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                areaPanelMouseMoved(evt);
            }
        });
        areaPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                areaPanelMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                areaPanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                areaPanelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout areaPanelLayout = new javax.swing.GroupLayout(areaPanel);
        areaPanel.setLayout(areaPanelLayout);
        areaPanelLayout.setHorizontalGroup(
            areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        areaPanelLayout.setVerticalGroup(
            areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 186, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout borderPanelLayout = new javax.swing.GroupLayout(borderPanel);
        borderPanel.setLayout(borderPanelLayout);
        borderPanelLayout.setHorizontalGroup(
            borderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(areaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        borderPanelLayout.setVerticalGroup(
            borderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(areaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        msgLabel.setForeground(new java.awt.Color(255, 0, 0));
        msgLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(msgLabel, org.openide.util.NbBundle.getMessage(ScreenshotPanel.class, "ScreenshotPanel.msgLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(areaLabel)
                            .addComponent(visibleAreaButton)
                            .addComponent(wholeAreaButton)
                            .addComponent(targetLabel)
                            .addComponent(clipboardButton))
                        .addGap(0, 26, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(msgLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(borderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
                .addGap(23, 23, 23))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(areaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(visibleAreaButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wholeAreaButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(borderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(msgLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(targetLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clipboardButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileButton)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileSearchButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fileSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileSearchButtonActionPerformed
        chooseFile(fileTextField);
    }//GEN-LAST:event_fileSearchButtonActionPerformed

    private void wholeAreaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wholeAreaButtonActionPerformed
        borderPanel.setVisible(wholeAreaButton.isSelected());
        msgLabel.setVisible(wholeAreaButton.isSelected());
    }//GEN-LAST:event_wholeAreaButtonActionPerformed

    private void visibleAreaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visibleAreaButtonActionPerformed
        borderPanel.setVisible(wholeAreaButton.isSelected());
        msgLabel.setVisible(wholeAreaButton.isSelected());
    }//GEN-LAST:event_visibleAreaButtonActionPerformed

    private void clipboardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clipboardButtonActionPerformed
        fileTextField.setEnabled(fileButton.isSelected());
        fileSearchButton.setEnabled(fileButton.isSelected());
    }//GEN-LAST:event_clipboardButtonActionPerformed

    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        fileTextField.setEnabled(fileButton.isSelected());
        fileSearchButton.setEnabled(fileButton.isSelected());
        fileTextField.requestFocusInWindow();
    }//GEN-LAST:event_fileButtonActionPerformed

    private void areaPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_areaPanelMouseClicked
        if (imagePanel != null) {
            imagePanel.mouseClicked(evt);
        }
    }//GEN-LAST:event_areaPanelMouseClicked

    private void areaPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_areaPanelMouseMoved
        if (imagePanel != null) {
            imagePanel.mouseMoved(evt);
        }
    }//GEN-LAST:event_areaPanelMouseMoved

    private void areaPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_areaPanelMousePressed
        if (imagePanel != null) {
            imagePanel.mousePressed(evt);
        }
    }//GEN-LAST:event_areaPanelMousePressed

    private void areaPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_areaPanelMouseDragged
        if (imagePanel != null) {
            imagePanel.mouseDragged(evt);
        }
    }//GEN-LAST:event_areaPanelMouseDragged

    private void areaPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_areaPanelMouseReleased
        if (imagePanel != null) {
            imagePanel.mouseReleased(evt);
        }
    }//GEN-LAST:event_areaPanelMouseReleased

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("captureWindowWidth", evt.getComponent().getWidth());
        registry.put("captureWindowHeight", evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel areaLabel;
    private javax.swing.JPanel areaPanel;
    private javax.swing.JPanel borderPanel;
    private javax.swing.ButtonGroup buttonGroupArea;
    private javax.swing.ButtonGroup buttonGroupTarget;
    private javax.swing.JRadioButton clipboardButton;
    private javax.swing.JRadioButton fileButton;
    private javax.swing.JButton fileSearchButton;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel msgLabel;
    private javax.swing.JLabel targetLabel;
    private javax.swing.JRadioButton visibleAreaButton;
    private javax.swing.JRadioButton wholeAreaButton;
    // End of variables declaration//GEN-END:variables


    private void chooseFile(javax.swing.JTextField jTF) {
        String path = Registry.get(genj.gedcom.GedcomOptions.class).get("reportDir", System.getProperty("user.home"));
        File outputfile = new File(path + File.separator + "ancestris_capture.png");

        File file  = new FileChooserBuilder(ScreenshotPanel.class.getCanonicalName())
                    .setFilesOnly(true)
                    .setDefaultBadgeProvider()
                    .setTitle(NbBundle.getMessage(getClass(), "TITL_CaptureTargetFile"))
                    .setApproveText(NbBundle.getMessage(getClass(), "OK_Select"))
                    .setDefaultExtension(FileChooserBuilder.getImageFilter().getExtensions()[0])
                    .setFileFilter(FileChooserBuilder.getImageFilter())
                    .setAcceptAllFileFilterUsed(false)
                    .setFileHiding(true)
                    .setParent(this)
                    .setSelectedFile(new File(jTF.getText()))
                    .setDefaultWorkingDirectory(outputfile.getParentFile())
                    .showSaveDialog();
            if (file != null) {
                jTF.setText(file.getAbsolutePath());
            }
            
    }

    

    public int getCaptureX() {
        if (visibleAreaButton.isSelected()) {
            return rVisible.x;
        }
        return imagePanel.getCaptureX();
    }

    public int getCaptureY() {
        if (visibleAreaButton.isSelected()) {
            return rVisible.y;
        }
        return imagePanel.getCaptureY();
    }

    public int getCaptureW() {
        if (visibleAreaButton.isSelected()) {
            return rVisible.width;
        }
        return Math.max(imagePanel.getCaptureW(), 1);
    }

    public int getCaptureH() {
        if (visibleAreaButton.isSelected()) {
            return rVisible.height;
        }
        return Math.max(imagePanel.getCaptureH(), 1);
    }

    public String getFile() {
        return fileTextField.getText();
    }

    public void savePreferences() {
        registry.put("captureFilename", fileTextField.getText());
        registry.put("captureView", visibleAreaButton.isSelected());
        registry.put("captureTarget", clipboardButton.isSelected());
    }

    public boolean isClipboard() {
        return clipboardButton.isSelected();
    }

    public boolean isFile() {
        String file = getFile();
        File f = new File(file);
        return fileButton.isSelected() && file != null && !file.isEmpty() && (!f.exists() || (f.exists() && f.isFile()));
    }

    private BufferedImage getImageFromComponent(JComponent component, double factor) {
        Rectangle r = new Rectangle(new Point(), component.getSize());
        if (r.width == 0 || r.height == 0) {
            return null;
        }
        try {
            BufferedImage image = new BufferedImage((int) (r.width*factor), (int) (r.height*factor), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderSelectionHintKey.KEY, false);
            g.setClip(0, 0, r.width, r.height);
            g.scale(factor, factor);
            g.translate(0, 0);
            component.paint(g);
            g.dispose();
            return image;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    
    
    
    

    private class ImagePanel extends javax.swing.JPanel {

        private BufferedImage image = null;
        private double maxXY = 0;
        private int DIM_RESIZE = 6;
        private double zoomX = 0, zoomY = 0;
        private double resX = 0, resY = 0;
        private Rectangle capture = null;
        private Point dragOffset = null;
        private boolean isResize = false;
        private boolean init = true;

        
        public void setImage(double maxXY, BufferedImage image) {
            this.maxXY = maxXY;
            this.image = image;
            capture = new Rectangle(0, 0, 1, 1);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                return;
            }
            // redraw image
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
            init = (zoomX == 0);
            zoomX = (double) (this.getWidth()) / (double) image.getWidth();
            zoomY = (double) (this.getHeight()) / (double) image.getHeight();
            g2d.scale(zoomX, zoomY);
            g2d.drawImage(image, 0, 0, null);

            if (init) {
                // redraw capture
                capture = new Rectangle(0, 0, (int) (50/zoomX), (int) (30/zoomY));
                
                // set min size of capture
                resX = Math.max(3*DIM_RESIZE/zoomX, 1); // min width
                resY = Math.max(3*DIM_RESIZE/zoomY, 1); // min height
                double sizeOfArea = resX * resY / factor / factor;
                if (sizeOfArea > maxXY) {
                    resX = Math.max(1/zoomX, 1);
                    resY = Math.max(1/zoomY, 1);
                }
            }
            g2d.setColor(new Color(20, 104, 0, 64));
            g.drawRect(capture.x, capture.y, capture.width, capture.height);
            g2d.setColor(new Color(0, 255, 0, 64));
            g.fillRect(capture.x, capture.y, capture.width, capture.height);
            g2d.setColor(new Color(20, 104, 0, 64));
            g.drawLine(capture.x+capture.width-(int)(2*DIM_RESIZE/zoomX), capture.y+capture.height-(int)(DIM_RESIZE/zoomY), capture.x+capture.width-(int)(DIM_RESIZE/zoomX), capture.y+capture.height-(int)(DIM_RESIZE/zoomY));
            g.drawLine(capture.x+capture.width-(int)(DIM_RESIZE/zoomX), capture.y+capture.height-(int)(DIM_RESIZE/zoomY), capture.x+capture.width-(int)(DIM_RESIZE/zoomX), capture.y+capture.height-(int)(2*DIM_RESIZE/zoomY));
        }

        private void mouseClicked(MouseEvent e) {
            if (image == null) {
                return;
            }
            Point p = e.getPoint();
            p = new Point((int) (p.x/zoomX), (int) (p.y/zoomY));
            int x = Math.max(0, p.x - capture.width/2);
            int y = Math.max(0, p.y - capture.height/2);
            if (image.getWidth() - x < capture.width) {
                x = image.getWidth() - capture.width;
            }
            if (image.getHeight() - y < capture.height) {
                y = image.getHeight() - capture.height;
            }
            capture = new Rectangle(x, y, capture.width, capture.height);
            repaint();
        }

        private void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            p = new Point((int) (p.x/zoomX), (int) (p.y/zoomY));
            int cursor = Cursor.DEFAULT_CURSOR;
            if (capture != null && capture.contains(p)) {
                cursor = Cursor.MOVE_CURSOR;
            }
            if (isResize(p)) {
                cursor = Cursor.SE_RESIZE_CURSOR;
            }
            setCursor(Cursor.getPredefinedCursor(cursor));
        }

        private void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            p = new Point((int) (p.x/zoomX), (int) (p.y/zoomY));
            isResize = isResize(p);
            dragOffset = capture.contains(p) ? new Point(capture.x - p.x, capture.y - p.y) : null;
        }

        private void mouseDragged(MouseEvent e) {
            if (image == null) {
                return;
            }
            Point p = e.getPoint();
            p = new Point((int) (p.x/zoomX), (int) (p.y/zoomY));
            if (isResize) {
                int w = p.x - capture.x, h = p.y - capture.y;
                if (p.x - capture.x < resX) { 
                    w = (int) resX;
                }
                if (p.y - capture.y < resY) { 
                    h = (int) resY;
                }
                if (p.x >= image.getWidth()) {
                    w = (int) (image.getWidth() - capture.x);
                }
                if (p.y >= image.getHeight()) {
                    h = (int) (image.getHeight() - capture.y);
                }
                double sizeOfArea = w * h / factor / factor;
                if (sizeOfArea < maxXY) { 
                    capture = new Rectangle(capture.x, capture.y, w, h);
                    repaint();
                    msgLabel.setText(" ");
                } else {
                    msgLabel.setText(NbBundle.getMessage(getClass(), "MSG_MaxSizeReached"));
                }
                return;
            }
            if (dragOffset == null) {
                return;
            }
            int x = Math.max(0, p.x + dragOffset.x);
            int y = Math.max(0, p.y + dragOffset.y);
            if (image.getWidth() - x < capture.width) {
                x = image.getWidth() - capture.width;
            }
            if (image.getHeight() - y < capture.height) {
                y = image.getHeight() - capture.height;
            }
            capture = new Rectangle(x, y, capture.width, capture.height);
            repaint();
        }

        private void mouseReleased(MouseEvent evt) {
            dragOffset = null;
        }

        private boolean isResize(Point p) {
            double resX = DIM_RESIZE/zoomX;
            double resY = DIM_RESIZE/zoomY;
            return (p.x > (capture.x + capture.width - resX)) && (p.y > (capture.y + capture.height - resY))
                && (p.x < (capture.x + capture.width + resX)) && (p.y < (capture.y + capture.height + resY));
        }

        private int getCaptureX() {
            return (int) (capture.x / factor);
        }

        private int getCaptureY() {
            return (int) (capture.y / factor);
        }

        private int getCaptureW() {
            return (int) (capture.width / factor);
        }

        private int getCaptureH() {
            return (int) (capture.height / factor);
        }

    }
    
}