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

import ancestris.core.TextOptions;
import ancestris.modules.treesharing.panels.FriendGedcomEntity;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
public class EntityConversion {

    public static GedcomIndi indiToGedcomIndi(Indi indi) {
        GedcomIndi gedcomIndi = new GedcomIndi();
        gedcomIndi.gedcomName = indi.getGedcom().getName();
        gedcomIndi.entityID = indi.getId();
        gedcomIndi.indiSex = ""+indi.getSex();
        gedcomIndi.indiLastName = indi.getLastName();
        gedcomIndi.indiFirstName = indi.getFirstName();
        gedcomIndi.indiBirthDate = getYear(indi.getBirthDate());
        gedcomIndi.indiBirthPlace = getBirthPlace(indi);
        gedcomIndi.indiDeathDate = getYear(indi.getDeathDate());
        gedcomIndi.indiDeathPlace = getDeathPlace(indi);
        return gedcomIndi;
    }

    public static GedcomFam famToGedcomFam(Fam fam) {
        GedcomFam gedcomFam = new GedcomFam();
        gedcomFam.gedcomName = fam.getGedcom().getName();
        gedcomFam.entityID = fam.getId();

        gedcomFam.famMarrDate = getYear(fam.getMarriageDate());
        gedcomFam.famMarrPlace = getMarrPlace(fam);
        Indi husband = fam.getHusband();
        if (husband != null) {
            gedcomFam.husbID = husband.getId();
            gedcomFam.husbSex = ""+husband.getSex();
            gedcomFam.husbLastName = husband.getLastName();
            gedcomFam.husbFirstName = husband.getFirstName();
            gedcomFam.husbBirthDate = getYear(husband.getBirthDate());
            gedcomFam.husbBirthPlace = getBirthPlace(husband);
            gedcomFam.husbDeathDate = getYear(husband.getDeathDate());
            gedcomFam.husbDeathPlace = getDeathPlace(husband);
        } else {
            gedcomFam.husbID = "";
            gedcomFam.husbSex = "";
            gedcomFam.husbLastName = "";
            gedcomFam.husbFirstName = "";
            gedcomFam.husbBirthDate = "0";
            gedcomFam.husbBirthPlace = PropertyPlace.JURISDICTION_SEPARATOR;
            gedcomFam.husbDeathDate = "0";
            gedcomFam.husbDeathPlace = PropertyPlace.JURISDICTION_SEPARATOR;
        }
        Indi wife = fam.getWife();
        if (wife != null) {
            gedcomFam.wifeID = wife.getId();
            gedcomFam.wifeSex = ""+wife.getSex();
            gedcomFam.wifeLastName = wife.getLastName();
            gedcomFam.wifeFirstName = wife.getFirstName();
            gedcomFam.wifeBirthDate = getYear(wife.getBirthDate());
            gedcomFam.wifeBirthPlace = getBirthPlace(wife);
            gedcomFam.wifeDeathDate = getYear(wife.getDeathDate());
            gedcomFam.wifeDeathPlace = getDeathPlace(wife);
        } else {
            gedcomFam.wifeID = "";
            gedcomFam.wifeSex = "";
            gedcomFam.wifeLastName = "";
            gedcomFam.wifeFirstName = "";
            gedcomFam.wifeBirthDate = "0";
            gedcomFam.wifeBirthPlace = PropertyPlace.JURISDICTION_SEPARATOR;
            gedcomFam.wifeDeathDate = "0";
            gedcomFam.wifeDeathPlace = PropertyPlace.JURISDICTION_SEPARATOR;
        }
        return gedcomFam;
    }

    
    
    
    private static String getYear(PropertyDate date) {
        if (date == null || !date.isValid()) {
            return "0";
        }
        PointInTime pit = date.getStart();
        if (pit == null) {
            return "0";
        }
        PointInTime pit2;
        try {
            pit2 = pit.getPointInTime(PointInTime.GREGORIAN);
        } catch (GedcomException ex) {
            return "0";
        }
        return (pit2 != null ? ""+pit2.getYear() : "0");
    }

    private static String getBirthPlace(Indi indi) {
        PropertyPlace prop = (PropertyPlace) indi.getProperty(new TagPath("INDI:BIRT:PLAC"));
        return getCityCountry(prop);
    }

    private static String getDeathPlace(Indi indi) {
        PropertyPlace prop = (PropertyPlace) indi.getProperty(new TagPath("INDI:DEAT:PLAC"));
        return getCityCountry(prop);
    }

    private static String getMarrPlace(Fam fam) {
        PropertyPlace prop = (PropertyPlace) fam.getProperty(new TagPath("FAM:MARR:PLAC"));
        return getCityCountry(prop);
    }

    private static String getCityCountry(PropertyPlace prop) {
        if (prop == null) {
            return "";
        }
        String city = prop.getCity();
        String[] bits = prop.getFormat();
        String country = "";
        if (bits != null && bits.length != 0) {
            String juri = prop.getJurisdiction(bits.length-1);
            if (juri != null) {
                country = juri;
            }
        }
        return city + PropertyPlace.JURISDICTION_SEPARATOR + country;
    }


    
    
    
    public static String getStringFromEntity(Entity entity) {
        if (entity instanceof Indi) {
            FriendGedcomEntity fge = new FriendGedcomEntity("", EntityConversion.indiToGedcomIndi((Indi) entity));
            return getStringFromEntity(fge);
        }
        if (entity instanceof Fam) {
            FriendGedcomEntity fge = new FriendGedcomEntity("", EntityConversion.famToGedcomFam((Fam) entity));
            return getStringFromEntity(fge);
        }
        return "";
    }

    
    
    public static String getStringFromEntity(FriendGedcomEntity fge) {
        
        String b = TextOptions.getInstance().getBirthSymbol();
        String m = TextOptions.getInstance().getMarriageSymbol();
        String d = TextOptions.getInstance().getDeathSymbol();

        StringBuilder ret = new StringBuilder(fge.indiLastName);
        ret.append(", ");
        ret.append(fge.indiFirstName);
        ret.append(" (");
        ret.append(fge.indiID);
        ret.append(") {"+b);
        ret.append(fge.indiBirthDate.equals("0") ? "?" : fge.indiBirthDate);
        ret.append(" ");
        ret.append(fge.indiBirthPlace.equals(",") ? "?" : fge.indiBirthPlace);
        ret.append(" "+d);
        ret.append(fge.indiDeathDate.equals("0") ? "?" : fge.indiDeathDate);
        ret.append(" ");
        ret.append(fge.indiDeathPlace.equals(",") ? "?" : fge.indiDeathPlace);
        ret.append("}");
        
        if (fge.type.equals(Gedcom.FAM)) {
            ret.append(" {"+m);
            ret.append(fge.famMarrDate.equals("0") ? "?" : fge.famMarrDate);
            ret.append(" ");
            ret.append(fge.famMarrPlace.equals(",") ? "?" : fge.famMarrPlace);
            ret.append("} ");
            ret.append(fge.spouLastName);
            ret.append(", ");
            ret.append(fge.spouFirstName);
            ret.append(" (");
            ret.append(fge.spouID);
            ret.append(") {"+b);
            ret.append(fge.spouBirthDate.equals("0") ? "?" : fge.spouBirthDate);
            ret.append(" ");
            ret.append(fge.spouBirthPlace.equals(",") ? "?" : fge.spouBirthPlace);
            ret.append(" "+d);
            ret.append(fge.spouDeathDate.equals("0") ? "?" : fge.spouDeathDate);
            ret.append(" ");
            ret.append(fge.spouDeathPlace.equals(",") ? "?" : fge.spouDeathPlace);
            ret.append("} (");
            ret.append(fge.entityID);
            ret.append(")");
        }

        return ret.toString();
    }


    
    
    
}
