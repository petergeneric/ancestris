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
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author frederic
 */
public class Utils {
    
    
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

    public static ImageIcon getImageIconFromFile(File file) {
        ImageIcon imageIcon = null;
        try {
            imageIcon = new ImageIcon(ImageIO.read(new FileInputStream(file)));
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
        return imageIcon;
    }

    public static Image getImageFromFile(File file) {
        Image image = null;
        try {
            image = ImageIO.read(new FileInputStream(file));
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
        return image;
    }



}
