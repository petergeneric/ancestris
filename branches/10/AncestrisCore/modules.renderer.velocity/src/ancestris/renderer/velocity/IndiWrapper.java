/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.renderer.velocity;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author daniel
 */
public class IndiWrapper extends EntityWrapper {

    IndiWrapper(Indi i) {
        super(i);
    }

    public String getShortValue() {
        return toString();
    }

    @Override
    public String toString() {
        // Might be null
        if (property == null) {
            return "";
        }
        String birth = ((Entity) property).format("BIRT", OPTIONS.getBirthSymbol() + " {$V }{$D}{ $P}");
        String death = ((Entity) property).format("DEAT", OPTIONS.getDeathSymbol() + " {$V }{$D}{ $P}");
        return property.toString() + " " + birth + " " + death;
    }

//    public String getString(String male, String female, String unknown) {
//        if (((Indi) property).getSex() == PropertySex.MALE) {
//            return male;
//        }
//        if (((Indi) property).getSex() == PropertySex.FEMALE) {
//            return female;
//        }
//        return unknown;
//    }
//
//    public String getString(String male, String female) {
//        return getString(male, female, male);
//    }

    public PropertyWrapper getFamc() {
        // Parents
        Fam famc = ((Indi) property).getFamilyWhereBiologicalChild();
        return create(famc);
    }

    //		// And we loop through its families
    public FamWrapper[] getFams() {
        Fam[] fams = ((Indi) property).getFamiliesWhereSpouse();
        FamWrapper[] reportFams = new FamWrapper[fams.length];
        for (int f = 0; f < fams.length; f++) {
            // .. here's the fam and spouse
            reportFams[f] = new FamWrapper(fams[f], (Indi) property);
        }
        return reportFams;
    }

    public String getSosaString() {
        return ((Indi) property).getSosaString();
    }

    public String getMediaFilePath() {
        File f = ((Indi) property).getMediaFile();
        return f != null ? f.getAbsolutePath() : "";
    }

    public String getWidthForMedia(int height) {
        File f = ((Indi) property).getMediaFile();
        try {
            BufferedImage image = ImageIO.read(new FileInputStream(f));
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            if (h != 0) {
                return ""+ (int) (100 * w / h);
            }
            return "0";
        } catch (IOException ex) {
            //Logger.getLogger(IndiWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return f != null ? f.getAbsolutePath() : "";
    }

    
}
