package ancestris.modules.releve.imageBrowser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorConvertOp;
import java.awt.image.LookupOp;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Affiche une image
 Commandes publiques :
    ImagePanel()
        constructeur
    void showImage(BufferedImage newImage)
        affiche une image
    void adjustAreaColor()
        ajuste les couleur d'une zone a selectionner avec la souris
 */
public class ImagePanel extends JPanel {

    public static final String ZOOM_LEVEL_CHANGED_PROPERTY = "zoomLevel";
    public static final String ZOOM_INCREMENT_CHANGED_PROPERTY = "zoomIncrement";
    public static final String IMAGE_CHANGED_PROPERTY = "image";
    private double zoomIncrement = 0.2;
    private double zoomFactor = 1.0 + zoomIncrement;
    private BufferedImage image;
    private double initialScale = 0.0;
    private double scale = 0.0;
    private int originX = 0;
    private int originY = 0;
    private Point previousMousePosition;
    private Dimension previousPanelSize;
    private WheelZoomDevice wheelZoomDevice = null;

    // mode d'utilisation de la souris
    protected static enum MouseMode {

        MOVE, // deplacement de l'image dans la fenetre
        SELECT // selection d'une zone rectangulaire
    }
    MouseMode mouseMode = MouseMode.MOVE;
    // area selection
    private int areaSrcx, areaSrcy;
    private int areaDestx, areaDesty;
    private BasicStroke areaStroke;
    private GradientPaint areaGradient;
    // Adjust image
    private BufferedImage adjustImage = null;
    private int adjustX;
    private int adjustY;

    /**
     * constructeur 
     */
    public ImagePanel() {
        setOpaque(false);

        // definitions pour la zone de selection
        areaGradient = new GradientPaint(0.0f, 0.0f, Color.blue, 0.0f, 0.0f, Color.white, true);
        areaStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{1, 0}, 0);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (scale > 0.0) {
                    if (isFullImageInPanel()) {
                        centerImage();
                    } else if (isImageEdgeInPanel()) {
                        scaleOrigin();
                    }
                    repaint();
                }
                previousPanelSize = getSize();
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (image == null) {
                    return;
                }

                if (mouseMode == MouseMode.SELECT) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        adjustStart(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (image == null) {
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (mouseMode == MouseMode.MOVE) {
                    } else {
                        adjustEnd();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (image == null) {
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (mouseMode == MouseMode.MOVE) {
                        // mode MOVE
                        moveImage(e.getPoint());
                    } else {
                        // mode SELECT
                        adjustMove(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (mouseMode == MouseMode.MOVE) {
                    //we need the mouse position so that after zooming
                    //that position of the image is maintained
                    previousMousePosition = e.getPoint();
                } else {
                }
            }
        });

        // j'active le zoom avec la molette de la souris
        wheelZoomDevice = new WheelZoomDevice();
        addMouseWheelListener(wheelZoomDevice);
        // je selectionne le curseur de deplacement
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    
    /**
     * affiche une image
     */
    public void showImage(BufferedImage newImage) {
        //BufferedImage oldImage = this.image;
        image = newImage;
        // raz de l'image ajustee
        adjustImage = null;
        areaSrcx = areaSrcy = areaDestx = areaDesty = 0;
        //firePropertyChange(IMAGE_CHANGED_PROPERTY, (Image) oldImage, (Image) image);
        repaint();
    }

    /**
     * Passe en mode de selection d'une zone a ajuster
     * A la fin de la selection, le mode MOVE est retabli automatiquement
     */
    public void adjustAreaColor() {
        if (image != null) {
            if (mouseMode == MouseMode.SELECT) {
                mouseMode = MouseMode.MOVE;
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else {
                adjustImage = null;
                mouseMode = MouseMode.SELECT;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
        } 
        
    }

    /**
     * deplace l'image contre le bord gauche
     */
    public void moveToLeft() {
        if (image != null) {
            originX = 0;
            repaint();
        } else {
            Toolkit.getDefaultToolkit().beep();
        } 
    }

    /**
     * deplace l'image contre le bord droit
     */
    public void moveToRight() {
        if (image != null) {
            originX = getWidth() - getScreenImageWidth();
            repaint();
        } else {
            Toolkit.getDefaultToolkit().beep();
        } 
    }

    /**
     * deplace l'image contre le bord haut
     */
    public void moveToTop() {
        if (image != null) {
            originY = 0;
            repaint();
        } else {
            Toolkit.getDefaultToolkit().beep();
        } 
        
    }

    /**
     * deplace l'image contre le bord bas
     */
    public void moveToBottom() {
        if (image != null) {
            originY = getHeight() - getScreenImageHeight();
            repaint();
        } else {
            Toolkit.getDefaultToolkit().beep();
        } 
    }

    public boolean isleftSideVisible() {
        return (originX >= 0);
    }

    public boolean isRightSideVisible() {
        if (image != null) {
            return (originX + getScreenImageWidth()) <= getWidth();
        } else {
            return true;
        }
    }

    public boolean isTopSideVisible() {
        return (originY >= 0);
    }

    public boolean isBottomSideVisible() {
        if( image!= null) {
            return (originY + getScreenImageHeight()) <= getHeight();
        } else {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Méthodes privées
    ///////////////////////////////////////////////////////////////////////////

    private Coords panelToImageCoords(Point p) {
        return new Coords((p.x - originX) / scale, (p.y - originY) / scale);
    }

    private Coords imageToPanelCoords(Coords p) {
        return new Coords((p.x * scale) + originX, (p.y * scale) + originY);
    }

    private boolean isImageEdgeInPanel() {
        if (previousPanelSize == null) {
            return false;
        }
        return (originX > 0 && originX < previousPanelSize.width
                || originY > 0 && originY < previousPanelSize.height);
    }

    private boolean isFullImageInPanel() {
        return (originX >= 0 && (originX + getScreenImageWidth()) < getWidth()
                && originY >= 0 && (originY + getScreenImageHeight()) < getHeight());
    }

    private void scaleOrigin() {
        originX = originX * getWidth() / previousPanelSize.width;
        originY = originY * getHeight() / previousPanelSize.height;
        repaint();
    }

    //Converts the specified zoom level	to scale.
    private double zoomToScale(double zoom) {
        return initialScale * zoom;
    }

    public double getZoom() {
        return scale / initialScale;
    }

    public void setZoom(double newZoom, Point zoomingCenter) {
        
        if (image != null) {
            Coords imageP = panelToImageCoords(zoomingCenter);

            if (imageP.x < 0.0) {
                imageP.x = 0.0;
            }
            if (imageP.y < 0.0) {
                imageP.y = 0.0;
            }
            if (imageP.x >= image.getWidth()) {
                imageP.x = image.getWidth() - 1.0;
            }
            if (imageP.y >= image.getHeight()) {
                imageP.y = image.getHeight() - 1.0;
            }

            Coords correctedP = imageToPanelCoords(imageP);
            scale = zoomToScale(newZoom);

            Coords panelP = imageToPanelCoords(imageP);
            originX += (correctedP.getIntX() - (int) panelP.x);
            originY += (correctedP.getIntY() - (int) panelP.y);
        }
        
        repaint();
    }

    private void centerImage() {
        originX = (getWidth() - getScreenImageWidth()) / 2;
        originY = (getHeight() - getScreenImageHeight()) / 2;
    }

    private void moveImage(Point newMousePosition) {
        int xDelta = newMousePosition.x - previousMousePosition.x;
        int yDelta = newMousePosition.y - previousMousePosition.y;

        //if ((originX + xDelta <= 0) && (originX + xDelta > getWidth() - getScreenImageWidth())) {
        originX += xDelta;
        if (adjustImage != null) {
            adjustX += xDelta;
        }
        //}

        //if ((originY + yDelta <= 0) && (originY + yDelta > getHeight() - getScreenImageHeight())) {
        originY += yDelta;
        if (adjustImage != null) {
            adjustY += yDelta;
        }
        //}

        previousMousePosition = newMousePosition;
        repaint();
    }

    private void adjustStart(int mouseX, int mouseY) {
        areaDestx = areaSrcx = mouseX;
        areaDesty = areaSrcy = mouseY;
        repaint();
    }

    private void adjustMove(int mouseX, int mouseY) {
        if ((mouseX >= 0) && (mouseX < getWidth())) {
            areaDestx = mouseX;
        }

        if ((mouseY >= 0) && (mouseY < getHeight())) {
            areaDesty = mouseY;
        }
        repaint();

    }

    /**
     * ajuste les couleurs de la zone selectionnée
     */
    private void adjustEnd() {
        if (areaSrcx == areaDestx && areaSrcy == areaDesty) {
            return;
        }
        int x1 = (areaSrcx < areaDestx) ? areaSrcx : areaDestx;
        int y1 = (areaSrcy < areaDesty) ? areaSrcy : areaDesty;
        int x2 = (areaSrcx > areaDestx) ? areaSrcx : areaDestx;
        int y2 = (areaSrcy > areaDesty) ? areaSrcy : areaDesty;
        adjustX = x1;
        adjustY = y1;
        Coords point1 = panelToImageCoords(new Point(x1, y1));
        Coords point2 = panelToImageCoords(new Point(x2, y2));

        if (point1.x < 0) {
            adjustX = originX;
            point1.x = 0;
        }
        if (point1.y < 0) {
            adjustY = originY;
            point1.y = 0;
        }

        int width = (int) (point2.x - point1.x) + 1;
        int height = (int) (point2.y - point1.y) + 1;
        
        if (point1.x + width > image.getWidth()) {
            width = image.getWidth() - (int) point1.x;
        }
        if (point1.y + height > image.getHeight()) {
            height = image.getHeight() - (int) point1.y;
        }
        
        BufferedImage inputImage = null;
        try { // catch remaining outside raster exception
            inputImage = image.getSubimage((int) point1.x, (int) point1.y, width, height);
        } catch (Exception e) {
            return;
        }
        

        // je convertis l'image en niveau de gris
        ColorConvertOp gray = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage grayImage = gray.filter(inputImage, null);

        // je calcule l'histogramme
        int[] histogram = new int[256];
        //final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final byte[] pixels = (byte[]) grayImage.getRaster().getDataElements(0, 0, width, height, null);
        for (int p = 0; p < width * height; p++) {
            histogram[(int) pixels[p] & 0xFF]++;
        }

//        for(int i=0; i<image.getWidth(); i++) {
//            for(int j=0; j<image.getHeight(); j++) {
//                int red = new Color(image.getRGB (i, j)).getAlpha();
//                histogram[red]++;
//            }
//        }

        // je calcule la Lut
        final float lutScale = (float) (255.0 / (width * height));
        byte[] lut = new byte[256];
        long sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            byte val = (byte) (sum * lutScale);
            if (val > 255) {
                lut[i] = (byte) 255;
            } else {
                lut[i] = val;
            }
        }

        // j'applique la transformation
        ByteLookupTable table = new ByteLookupTable(0, lut);
        LookupOp lookupOp = new LookupOp(table, null);
        adjustImage = lookupOp.filter(grayImage, null);

        // je desactive la zone de selection
        areaSrcx = areaSrcy = areaDestx = areaDesty = 0;
        mouseMode = MouseMode.MOVE;
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        System.out.println("ImagePanel MOVE_CURSOR");
        repaint();
    }

    /**
     *  affiche l'image et la zone selectionnee si elle existe
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paints the background

        if (image == null) {
            return;
        }

        if (scale == 0.0) {
            double xScale = (double) getWidth() / image.getWidth();
            double yScale = (double) getHeight() / image.getHeight();
            initialScale = Math.min(xScale, yScale);
            scale = initialScale;
            centerImage();
        }

        // j'affiche l'image
        g.drawImage(image, originX, originY, getScreenImageWidth(), getScreenImageHeight(), null);

        // j'affiche la zone de selection
        if (areaSrcx != areaDestx || areaSrcy != areaDesty) {
            int x1 = (areaSrcx < areaDestx) ? areaSrcx : areaDestx;
            int y1 = (areaSrcy < areaDesty) ? areaSrcy : areaDesty;
            int x2 = (areaSrcx > areaDestx) ? areaSrcx : areaDestx;
            int y2 = (areaSrcy > areaDesty) ? areaSrcy : areaDesty;

            Rectangle rectSelection = new Rectangle();
            rectSelection.x = x1;
            rectSelection.y = y1;
            rectSelection.width = (x2 - x1) + 1;
            rectSelection.height = (y2 - y1) + 1;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 255, 20));
            g2d.fill(rectSelection);
            g2d.setStroke(areaStroke);
            g2d.setPaint(areaGradient);
            g2d.draw(rectSelection);
        }

        // affiche la zone ajustée
        if (adjustImage != null) {
            g.drawImage(adjustImage, adjustX, adjustY, (int) (scale * adjustImage.getWidth()), (int) (scale * adjustImage.getHeight()), null);
        }
    }

    private int getScreenImageWidth() {
        if ( image != null){
            return (int) (scale * image.getWidth());
        } else {
            return 0;
        }
    }

    private int getScreenImageHeight() {
        if ( image != null){
            return (int) (scale * image.getHeight());
        } else {
            return 0;
        }
    }

    private class WheelZoomDevice implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            Point p = e.getPoint();
            boolean zoomIn = (e.getWheelRotation() < 0);
            if (zoomIn) {
                zoomFactor = 1.0 + zoomIncrement;
            } else {
                zoomFactor = 1.0 - zoomIncrement;
            }

            Coords imageP = panelToImageCoords(previousMousePosition);
            Coords adjustImageP = new Coords((previousMousePosition.x - adjustX) / scale, (previousMousePosition.y - adjustY) / scale);
            double oldZoom = getZoom();
            scale *= zoomFactor;

            Coords panelP = imageToPanelCoords(imageP);
            originX += (previousMousePosition.x - (int) panelP.x);
            originY += (previousMousePosition.y - (int) panelP.y);

            if (adjustImage != null) {
                Coords ajustPanelP = new Coords((adjustImageP.x * scale) + adjustX, (adjustImageP.y * scale) + adjustY);
                adjustX += (previousMousePosition.x - (int) ajustPanelP.x);
                adjustY += (previousMousePosition.y - (int) ajustPanelP.y);
            }

            //firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom), new Double(getZoom()));

            repaint();
        }
    }

    private class Coords {

        public double x;
        public double y;

        public Coords(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public int getIntX() {
            return (int) Math.round(x);
        }

        public int getIntY() {
            return (int) Math.round(y);
        }

        @Override
        public String toString() {
            return "[Coords: x=" + x + ",y=" + y + "]";
        }
    }
}
