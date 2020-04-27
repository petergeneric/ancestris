package ancestris.modules.releve.imageAligner;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
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
    private CoordinateListener coordinateListener;
    private CoordImage alignCoords;
    private CropRectangle imageCropRect= new CropRectangle(); 

    void frameUpdated() {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                imageCropRect.updatePanelCoords();
                repaint();
            }
        });
        
    }

    // mode d'utilisation de la souris
    protected static enum MouseMode {

        MOVE, // deplacement de l'image dans la fenetre
        CROP_X1,   // selection de la zone de crop
        CROP_Y1,
        CROP_X2,
        CROP_Y2
    }
    MouseMode mouseMode = MouseMode.MOVE;
    

    /**
     * constructeur 
     */
    public ImagePanel() {
        setOpaque(false);
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
            public void mouseClicked(MouseEvent e) {


                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    
                    // double clic : j'aligne l'image (FL : = translation de l'image depuis le point cliqué jusqu'au réticule => pourquoi ???)
                    //CoordImage coords = convertPanelToImageCoords(e.getPoint());
                    if (alignCoords != null) {
                        AffineTransform tx = new AffineTransform();
                        // tx.translate(alignCoords.x - coords.x, alignCoords.y - coords.y);
                        tx.translate(-alignCoords.x, -alignCoords.y);
                        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                        image = op.filter(image, null);
                    }
                    
                    // je refraichis l'affichage
                    repaint();
                    int     tmpCropX = imageCropRect.imageCoords.x, 
                            tmpCropY = imageCropRect.imageCoords.y, 
                            tmpCropW = imageCropRect.imageCoords.width, 
                            tmpCropH = imageCropRect.imageCoords.height; 
                    
                    if (tmpCropX < 0) {
                        tmpCropX = 0;
                    }
                    if (tmpCropY < 0) {
                        tmpCropY = 0;
                    }
                    if (tmpCropX + tmpCropW > image.getWidth()) {
                        tmpCropW = Math.max(0, image.getWidth() - tmpCropX);
                    }
                    if (tmpCropY + tmpCropH > image.getHeight()) {
                        tmpCropH = Math.max(0, image.getHeight() - tmpCropY);
                    }
                    
                    try {
                        final BufferedImage croppedImage = image.getSubimage(tmpCropX, tmpCropY, tmpCropW, tmpCropH);

                        // je lance la sauvegarde de l'image dans un fichier
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                coordinateListener.saveCurrentImage(croppedImage);
                            }
                        });

                    } catch (Exception ex) {
//                        JOptionPane.showConfirmDialog(null, NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jButtonLeft.cannotCroptMsg", 
//                                imageCropRect.imageCoords.x, imageCropRect.imageCoords.width, 
//                                imageCropRect.imageCoords.y, imageCropRect.imageCoords.height, 
//                                image.getWidth(), image.getHeight()));

                    }
                    
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (image == null) {
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                } else {
                    // clic droit : je memorise la position d'alignement sauf s'il est déjà mémorisé, auquel cas je le remet à zéro
                    if (alignCoords == null) {
                        alignCoords = convertPanelToImageCoords(e.getPoint());
                        coordinateListener.updateAlignCoordinates(alignCoords);
                    } else {
                        alignCoords = null;
                        coordinateListener.updateAlignCoordinates(alignCoords);
                    }
                    
                    // je refraichis l'affichage pour faire apparaitre le reticule sur le point sélectionné
                    repaint();
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
                        // j'affiche les corodonnées
                        coordinateListener.updateMouseCoordinates(convertPanelToImageCoords(e.getPoint()));
                
                        switch (mouseMode) {
                            case CROP_X1:
                                imageCropRect.dragX1(e.getX());
                                repaint();
                                break;
                            case CROP_Y1:
                                imageCropRect.dragY1(e.getY());
                                repaint();
                                break;
                            case CROP_X2:
                                imageCropRect.dragX2(e.getX());
                                repaint();
                                break;
                            case CROP_Y2:
                                imageCropRect.dragY2(e.getY());
                                repaint();
                                break;
                            default:
                                break;
                        }   
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //we need the mouse position so that after zooming
                //that position of the image is maintained
                previousMousePosition = e.getPoint();

                // j'affiche les corodonnées
                coordinateListener.updateMouseCoordinates(convertPanelToImageCoords(previousMousePosition));

                switch (mouseMode) {
                    case MOVE:
                        // je verifie si on est sur la limite du crop
                        if( imageCropRect.isCropX1(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                            mouseMode = MouseMode.CROP_X1;
                        } else if (imageCropRect.isCropY1(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                            mouseMode = MouseMode.CROP_Y1;
                        } else if (imageCropRect.isCropX2(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                            mouseMode = MouseMode.CROP_X2;
                        } else if (imageCropRect.isCropY2(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                            mouseMode = MouseMode.CROP_Y2;
                        }   
                        break;
                    case CROP_X1:
                        // je verifie si on est sur la limite du crop
                        if( ! imageCropRect.isCropX1(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            mouseMode = MouseMode.MOVE;
                        }   
                        break;
                    case CROP_Y1:
                        // je verifie si on est sur la limite du crop
                        if( ! imageCropRect.isCropY1(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            mouseMode = MouseMode.MOVE;
                        }   
                        break;
                    case CROP_X2:
                        // je verifie si on est sur la limite du crop
                        if( ! imageCropRect.isCropX2(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            mouseMode = MouseMode.MOVE;
                        }   
                        break;
                    case CROP_Y2:
                        // je verifie si on est sur la limite du crop
                        if( ! imageCropRect.isCropY2(e.getPoint())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            mouseMode = MouseMode.MOVE;
                        }   
                        break;
                    default:
                        break;
                }
            }
        });

        // j'active le zoom avec la molette de la souris
        wheelZoomDevice = new WheelZoomDevice();
        addMouseWheelListener(wheelZoomDevice);
        // je selectionne le curseur de deplacement
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    
    void setCoordinateListener(CoordinateListener coordinateListener) {
        this.coordinateListener = coordinateListener;
    }
    
    /**
     * affiche une image
     */
    public void showImage(BufferedImage newImage) {
        image = newImage;
        // raz de l'image ajustee    
        repaint();
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

    private CoordImage convertPanelToImageCoords(Point p) {
        return new CoordImage((p.x - originX) / scale, (p.y - originY) / scale);
    }

    private CoordPanel convertImageToPanelCoords(CoordImage p) {
        return new CoordPanel((p.x * scale) + originX, (p.y * scale) + originY);
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
            CoordImage imageP = convertPanelToImageCoords(zoomingCenter);

            if (imageP.x < 0) {
                imageP.x = 0;
            }
            if (imageP.y < 0) {
                imageP.y = 0;
            }
            if (imageP.x >= image.getWidth()) {
                imageP.x = image.getWidth() - 1;
            }
            if (imageP.y >= image.getHeight()) {
                imageP.y = image.getHeight() - 1;
            }

            CoordPanel correctedP = convertImageToPanelCoords(imageP);
            scale = zoomToScale(newZoom);

            CoordPanel panelP = convertImageToPanelCoords(imageP);
            originX += (correctedP.x - panelP.x);
            originY += (correctedP.y - panelP.y);
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
        
        originX += xDelta;
        originY += yDelta;
        previousMousePosition = newMousePosition;
        
        imageCropRect.updatePanelCoords();
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
                        
            imageCropRect.setImageBounds(0, 0, image.getWidth(), image.getHeight());
        }

        // j'affiche l'image
        g.drawImage(image, originX, originY, getScreenImageWidth(), getScreenImageHeight(), null);

        if (alignCoords != null && alignCoords.x != 0  && alignCoords.y != 0) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.RED);
            CoordPanel coordPanel = convertImageToPanelCoords(alignCoords);
            g2d.drawLine(coordPanel.x, 0, coordPanel.x, getScreenImageHeight());
            g2d.setColor(Color.ORANGE);
            g2d.drawLine(0, coordPanel.y, getScreenImageWidth(), coordPanel.y);            
        }
        
        // j'affiche le rectangle crop
        {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.RED);
//            g2d.drawRect((int) (imageCropRect.imageCoords.x * scale) + originX, 
//                    (int) (imageCropRect.imageCoords.y * scale) + originY , 
//                    (int) (imageCropRect.imageCoords.width * scale), 
//                    (int) (imageCropRect.imageCoords.height * scale) );

            //imageCropRect.updatePanelCoords();
            g2d.draw(imageCropRect);
        
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

            CoordImage imageP = convertPanelToImageCoords(previousMousePosition);
            scale *= zoomFactor;

            CoordPanel coordPanel = convertImageToPanelCoords(imageP);
            originX += (previousMousePosition.x - coordPanel.x);
            originY += (previousMousePosition.y - coordPanel.y);

            //crop rectangle
           imageCropRect.updatePanelCoords();
          
            repaint();
        }
    }
    
    
    interface CoordinateListener {
        void updateMouseCoordinates(CoordImage coords);
        void updateAlignCoordinates(CoordImage coords);
        void saveCurrentImage(BufferedImage currentImage);
    }

    protected class CropRectangle  extends Rectangle {
        Rectangle imageCoords = new Rectangle(); 
        private final int marge = 3;
        
        void setImageBounds(int x, int y, int width, int height) {
            imageCoords.x = x; 
            imageCoords.y = y;
            imageCoords.width = width;
            imageCoords.height = height;    
            
            updatePanelCoords();
        }
        
        void updatePanelCoords() {
            this.x = (int) ((imageCoords.x * scale) + originX);
            this.y = (int) ((imageCoords.y * scale) + originY);
            this.width = (int) (imageCoords.width * scale);
            this.height = (int) (imageCoords.height * scale);
        }
        
        boolean isCropX1(Point point) {
            return point.x > x - marge && point.x < x + marge  && point.y > y && point.y < y + height ; 
        }

        boolean isCropY1(Point point) {
            return point.x > x && point.x < x + width  && point.y > y - marge && point.y < y + marge ; 
        }
        
        boolean isCropX2(Point point) {
            return point.x > x +width - marge && point.x < x + width + marge  && point.y > y && point.y < y + height ; 
        }

        boolean isCropY2(Point point) {
            return point.x > x && point.x < x + width  && point.y > y + height - marge && point.y < y + height + marge ; 
        }
        
        void dragX1(int x1) {
            this.width += this.x - x1;
            this.x = x1; 
            
            imageCoords.x  = (int) ((this.x - originX) / scale); 
            imageCoords.width = (int) (this.width / scale);            
        }
        
        void dragY1(int y1) {           
            this.height += this.y - y1;
            this.y = y1; 
            
            imageCoords.y  = (int) ((this.y - originY) / scale); 
            imageCoords.height = (int) (this.height / scale);
        }
        
        void dragX2(int x2) {           
            this.width = x2 - this.x ;
            imageCoords.width = (int) (this.width / scale);
        }
                                
        void dragY2(int y2) {           
            this.height = y2 - this.y ;
            imageCoords.height = (int) (this.height / scale);
        }
        
        
    }
    
    protected class CoordImage {

        public int x;
        public int y;

        public CoordImage(double x, double y) {
            this.x = (int) Math.round(x);
            this.y = (int) Math.round(y);
        }
        
        public CoordImage(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "[Coords: x=" + x + ",y=" + y + "]";
        }
    }
    
    protected class CoordPanel extends Point {

//        public int x;
//        public int y;
//
//        public CoordPanel(int x, int y) {
//            this.x = x;
//            this.y = y;
//        }
        
        public CoordPanel(double x, double y) {
            this.x = (int) Math.round(x);
            this.y = (int) Math.round(y);
        }

        @Override
        public String toString() {
            return "[Coords: x=" + x + ",y=" + y + "]";
        }
    }
}
