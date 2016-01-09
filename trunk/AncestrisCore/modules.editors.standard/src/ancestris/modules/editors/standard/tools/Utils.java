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

import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;




/**
 *
 * @author frederic
 */
public class Utils {
    
    private static Image IMG_INVALID_PHOTO = null;
    private static Image IMG_VIDEO = null;
    private static Image IMG_SOUND = null;
    private static Image IMG_NO_SOURCE_MEDIA = null;
    
    private static String[] imgExtensions =   { "jpg", "jpeg", "png", "gif", "tiff", "bmp", "svg" };
    private static String[] videoExtensions = { "mp4", "flv", "ogg", "avi", "mov", "mpeg" };
    private static String[] soundExtensions = { "mp3", "wav", "ogg", "flac" };
    
    public static boolean parentTagsContains(Property prop, String tag) {
        if (prop == null) {
            return false;
        }
        Property parent;
        if (prop instanceof PropertyXRef) {
            parent = ((PropertyXRef) prop).getTargetParent();
        } else {
            parent = prop.getParent();
        }
        if (parent == null) {
            return false;
        }
        if (parent.getTag().equals(tag)) {
            return true;
        }
        return parentTagsContains(parent, tag);
    }
    
    public static Image getImageFromFile(File file, Class clazz) {
        return getImageFromFile(file, clazz, false);
    }
    
    public static Image getImageFromFile(File file, Class clazz, boolean noText) {
        
        if (clazz != null) {
            try {
                IMG_INVALID_PHOTO = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/invalid_photo.png"));
                IMG_VIDEO = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/video.png"));
                IMG_SOUND = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/sound.png"));
                IMG_NO_SOURCE_MEDIA = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/source_no_media.png"));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Image image = null;

        if (file == null) {
            return clazz == SourceChooser.SourceThumb.class && !noText ? IMG_NO_SOURCE_MEDIA : IMG_INVALID_PHOTO;
        }
        if (Arrays.asList(imgExtensions).contains(getExtension(file))) {
            try {
                image = ImageIO.read(new FileInputStream(file));
            } catch (Exception ex) {
                //Exceptions.printStackTrace(ex);
                image = IMG_INVALID_PHOTO;
            }
        } else if (Arrays.asList(videoExtensions).contains(getExtension(file))) {
            image = IMG_VIDEO;
        } else if (Arrays.asList(soundExtensions).contains(getExtension(file))) {
            image = IMG_SOUND;
        } else {
            image = IMG_INVALID_PHOTO;
        }
        if ((image.getWidth(null) <= 0) || (image.getHeight(null) <= 0)) {
            return IMG_INVALID_PHOTO;
        }
    
        return image;
    }

    public static FileNameExtensionFilter getImageFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(Utils.class, "ImageTypes"), imgExtensions);
    }

    public static FileNameExtensionFilter getVideoFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(Utils.class, "VideoTypes"), videoExtensions);
    }

    public static FileNameExtensionFilter getSoundFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(Utils.class, "SoundTypes"), soundExtensions);
    }


    /*
     * Get the extension of a file.
     */
    private static String getExtension(File f) {
        if (f == null) {
            return "";
        }
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
    

    
    public static void getPropertiesRecursively(Property parent, String tag, List props) {
        Property[] children = parent.getProperties();
        for (Property child : children) {
            if (child.getTag().equals(tag)) {
                props.add(child);
            }
            getPropertiesRecursively(child, tag, props);
        }
    }
    
    
    public static Image scaleImage(File f, Class clazz, int width, int height) {
        return scaleImage(f, clazz, width, height, false);
    }
    
    public static Image scaleImage(File f, Class clazz, int width, int height, boolean noText) {
        Image image = getImageFromFile(f, clazz, noText);
        return scaleImage(image, width, height);
    }
    
    public static ImageIcon getResizedIcon(ImageIcon imageIcon, int targetWidth, int targetHeight) {
        return new ImageIcon(scaleImage(imageIcon.getImage(), targetWidth, targetHeight));
    }
    
    private static BufferedImage scaleImage(Image image, int targetWidth, int targetHeight) {

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if ((imageWidth <= 0) || (imageHeight <= 0)) {
            image.flush();
            return null;
        }
        
        double imageRatio = (double) imageWidth / (double) imageHeight;
        double targetRatio = (double) targetWidth / (double) targetHeight;
        if (targetRatio < imageRatio) {
            targetHeight = (int) (targetWidth / imageRatio);
        } else {
            targetWidth = (int) (targetHeight * imageRatio);
        }

        return resizeImage(image, targetWidth, targetHeight);
    }

     
    private static BufferedImage resizeImage(Image img, int width, int height) {
        BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
        return dimg;
    }

    
    private static String removeExtension(String name) {
        String ret = name;
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex >= 0) { // to prevent exception if there is no dot
            ret = name.substring(0, dotIndex);
        }
        return ret;
    }

    
}
