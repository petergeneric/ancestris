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

import java.awt.Image;
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

        // nom, prénom, ville, pays, email, photo, h/f
        public String lastname = "";
        public String firstname = "";
        public String city = "";
        public String country = "";
        public String email = "";
        public byte[] photoBytes = null;   // size 155x186
        
        public MemberProfile() {
        }

    public ImageIcon getIcon() {
        if (photoBytes == null) {
            return null;
        } else {
            return new ImageIcon(getPhoto().getScaledInstance(16, 19, Image.SCALE_DEFAULT));
        }
    }

    public void setPhotoBytes(File f) {
        try {
            BufferedImage img = ImageIO.read(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            baos.flush();
            photoBytes = baos.toByteArray();
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
    }
    
    public Image getPhoto() {
        Image image = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(photoBytes));
            image = bufferedImage.getScaledInstance(155, 186, Image.SCALE_DEFAULT);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return image;
    }
}
