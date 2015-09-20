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

package ancestris.modules.treesharing.communication;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
public class MemberProfile implements Serializable {

    private final static int IMG_SMALL_WIDTH = 16;
    private final static int IMG_SMALL_HEIGHT = 19;
    
    private final static int IMG_MEDIUM_WIDTH = 51;
    private final static int IMG_MEDIUM_HEIGHT = 62;
    
    private final static int IMG_LARGE_WIDTH = 155;
    private final static int IMG_LARGE_HEIGHT = 186;

    
    
    // nom, prénom, ville, pays, email, photo, h/f
    public String lastname = "";
    public String firstname = "";
    public String city = "";
    public String country = "";
    public String email = "";
    public byte[] photoBytes = null;   // size 155x186

    public String username = "";
    public String userdir = "";
    public String osname = "";
    public String osversion = "";

    public MemberProfile() {
        username = System.getProperty("user.name");
        userdir = System.getProperty("user.dir");
        osname = System.getProperty("os.name");
        osversion = System.getProperty("os.version");
    }

    public void setPhotoBytes(File f) {
        try {
            BufferedImage img = resizeImage(ImageIO.read(f));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            baos.flush();
            photoBytes = baos.toByteArray();
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
    }
    
    public ImageIcon getPhoto(int size) {
        Image image = null;
        if (photoBytes == null) {
            return null;
        }
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(photoBytes));
            if (size == 1) {
                image = bufferedImage.getScaledInstance(IMG_SMALL_WIDTH, IMG_SMALL_HEIGHT, Image.SCALE_DEFAULT);
            } else if (size == 2) {
                image = bufferedImage.getScaledInstance(IMG_MEDIUM_WIDTH, IMG_MEDIUM_HEIGHT, Image.SCALE_DEFAULT);
            } else if (size == 3) {
                image = bufferedImage.getScaledInstance(IMG_LARGE_WIDTH, IMG_LARGE_HEIGHT, Image.SCALE_DEFAULT);
            } else {
                image = bufferedImage.getScaledInstance(IMG_MEDIUM_WIDTH, IMG_MEDIUM_HEIGHT, Image.SCALE_DEFAULT);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new ImageIcon(image);
    }

    private BufferedImage resizeImage(BufferedImage img) {
        BufferedImage dimg = new BufferedImage(IMG_LARGE_WIDTH, IMG_LARGE_HEIGHT, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, IMG_LARGE_WIDTH, IMG_LARGE_HEIGHT, 0, 0, img.getWidth(), img.getHeight(), null);
        g.dispose();
        return dimg;
    }

    
}
