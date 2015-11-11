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

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
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
    
    private static String[] imgExtensions =   { "jpg", "jpeg", "png", "gif", "tiff", "bmp", "svg" };
    private static String[] videoExtensions = { "mp4", "flv", "ogg", "avi", "mov", "mpeg" };
    private static String[] soundExtensions = { "mp3", "wav", "ogg", "flac" };
    
    
    
    
    public static ImageIcon getResizedIcon(ImageIcon imageIcon, int width, int height) {
        int imageWidth = imageIcon.getImage().getWidth(null);
        int imageHeight = imageIcon.getImage().getHeight(null);
        if ((imageWidth > 0) && (imageHeight > 0)) {
            double imageRatio = (double) imageWidth / (double) imageHeight;
            double targetWidth = width;
            double targetHeight = height;
            double targetRatio = targetWidth / targetHeight;

            if (targetRatio < imageRatio) {
                targetHeight = targetWidth / imageRatio;
            } else {
                targetWidth = targetHeight * imageRatio;
            }
            return new ImageIcon(imageIcon.getImage().getScaledInstance((int) targetWidth, (int) targetHeight, Image.SCALE_DEFAULT));
        }
        return null;
    }

    public static Image getImageFromFile(File file, Class clazz) {
        
        if (clazz != null) {
            try {
                IMG_INVALID_PHOTO = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/invalid_photo.png"));
                IMG_VIDEO = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/video.png"));
                IMG_SOUND = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/sound.png"));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Image image = null;

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
    
    
}
