/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.editorstd.media;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import genj.gedcom.Entity;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.net.URLConnection;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author frederic
 */
public class MediaWrapper {

    private Property property;
    private File file;
    private String mimeType = "";
    private ImageIcon image;
    private static ImageIcon imagePhoto = new ImageIcon(ImageUtilities.loadImage("genjfr/app/editorstd/media/photoIcon.jpg", true));
    private static ImageIcon imageAudio = new ImageIcon(ImageUtilities.loadImage("genjfr/app/editorstd/media/audioIcon.jpg", true));
    private static ImageIcon imageVideo = new ImageIcon(ImageUtilities.loadImage("genjfr/app/editorstd/media/videoIcon.jpg", true));
    private static ImageIcon imageMedia = new ImageIcon(ImageUtilities.loadImage("genjfr/app/editorstd/media/mediaIcon.jpg", true));
    static private final int sizeX = 64;
    static private final int sizeY = 64;

    public MediaWrapper(Property property) {
        // Set property
        if (property instanceof PropertyXRef) {
            PropertyXRef pRef = (PropertyXRef) property;
            Entity entity = pRef.getTargetEntity();
            if (entity instanceof Media) {
                this.property = entity;
            }
        } else {
            this.property = property;
        }


        // Set file
        Property prop = this.property.getProperty("FILE");
        if (prop instanceof PropertyFile) {
            this.file = ((PropertyFile) prop).getFile();
        } else {
            prop = this.property.getProperty("BLOB");
            if (prop instanceof PropertyBlob) {
                PropertyBlob blob = (PropertyBlob) prop;
                this.file = new File("dummy.jpg");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(blob.getBlobData());
                    fos.close();
                } catch (Exception ex) {
                    this.file = null;
                }

            } else {
                this.file = null;
            }
        }

        // Set Mimetype
        if (file != null) {
            this.mimeType = new MimeMap().getContentTypeFor(file.toURI().toString());
        } else {
            this.mimeType = "";
        }
        System.out.println("DEBUG - mimetype="+mimeType);

        // Set image
        if (mimeType == null) {
            image = imageMedia;
        } else if (mimeType.toLowerCase().indexOf("image") > -1) {
            try {
                image = new ImageIcon((Image) ImageIO.read(scaleImage(new FileInputStream(file))));
            } catch (Exception ex) {
                image = imagePhoto;
            }
        } else if (mimeType.toLowerCase().indexOf("audio") > -1) {
            image = imageAudio;
        } else if (mimeType.toLowerCase().indexOf("video") > -1) {
            image = imageVideo;
        } else {
            image = imageMedia;
        }
    }

    public Property getProperty() {
        return property;
    }

    public File getFile() {
        return file;
    }

    public String getMimeType() {
        return mimeType;
    }

    public ImageIcon getImage() {
        return image;
    }

    public Component getComponent() {
        JPanel component = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel jl = new JLabel(image);
        jl.setPreferredSize(new Dimension(sizeX, sizeY));
        component.add(jl);
        component.add(new JLabel(toString()));
        return component;
    }

    @Override
    public String toString() {
        if (property == null) {
            return null;
        }
        Property titleProp = property.getProperty("TITL");
        if (titleProp != null) {
            String title = titleProp.getValue();
            if (!title.trim().isEmpty()) {
                return title;
            }
        }
        if (file == null) {
            return NbBundle.getMessage(MediaWrapper.class, "No_media_attached");
        }
        return file.toString();
    }

    public static InputStream scaleImage(InputStream p_image) throws Exception {

        InputStream imageStream = new BufferedInputStream(p_image);
        Image image = (Image) ImageIO.read(imageStream);

        int thumbWidth = sizeX;
        int thumbHeight = sizeY;

        // Make sure the aspect ratio is maintained, so the image is not skewed
        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }

        // Draw the scaled image
        BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

        // Write the scaled image to the outputstream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
        int quality = 100; // Use between 1 and 100, with 100 being highest quality
        quality = Math.max(0, Math.min(quality, 100));
        param.setQuality((float) quality / 100.0f, false);
        encoder.setJPEGEncodeParam(param);
        encoder.encode(thumbImage);
        ImageIO.write(thumbImage, "jpg", out);

        // Read the outputstream into the inputstream for the return value
        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

        return bis;
    }
}
