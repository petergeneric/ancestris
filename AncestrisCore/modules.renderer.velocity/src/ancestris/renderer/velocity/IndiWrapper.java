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
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;

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
        Property obje = ((Indi) property).getProperty("OBJE", true);
        String path = "";
        if (obje != null) {
            if (obje instanceof PropertyMedia) {
                PropertyMedia pm = (PropertyMedia) obje;
                Media media = (Media) pm.getTargetEntity();
                if (media != null) {
                    Property file = media.getProperty("FILE", false);
                    if (file instanceof PropertyFile) {
                        path = file.getValue();
                    }
                }
            } else {
                PropertyFile file = (PropertyFile) obje.getProperty("FILE");
                if (file != null) {
                    path = file.getValue();
                }
            }
        }
        return path;
    }
    
}
