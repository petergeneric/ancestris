/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.explorer;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.Image;
import java.util.Collection;

/**
 *
 * @author daniel
 */
class GedcomEntities {

    private Integer number;
    private String category;
    private String title;
    private String name;
    private static Entity entity;
    private final String tag;
    private Gedcom gedcom;

    /** Creates a new instance of Instrument */

    GedcomEntities(Gedcom gedcom, String tag) {
        this.tag = tag;
        this.gedcom = gedcom;
    }

    public String getTitle() {
        int count = gedcom.getEntities(tag).size();
        return Gedcom.getName(tag, true)+" ("+count+")";
    }

    public Image getImage(){
        return Gedcom.getEntityImage(tag).getImage();
    }

    public Collection<? extends Entity> getEntities(){
        return gedcom.getEntities(tag);
    }

    public String getTag(){
        return tag;
    }

    public Gedcom getGedcom(){
        return gedcom;
    }

}