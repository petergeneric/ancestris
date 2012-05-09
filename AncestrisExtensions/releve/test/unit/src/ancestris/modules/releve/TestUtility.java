package ancestris.modules.releve;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Michel
 */
public class TestUtility {

    public static File createFile( String data ) throws IOException  {
        FileWriter writer = null;
        File file = new File(System.getProperty("user.home") + File.separator + "ancestris.txt");
        boolean append = false;
        writer = new FileWriter(file, append);
        writer.write(data);            
        writer.close();
        return file;

    }

    /**
     * creation 'un gedcom minimal
     * @return
     * @throws GedcomException
     */
    public static Gedcom createGedcom() throws GedcomException {

        Gedcom gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);
        Property birth;
        Property date;
        PropertySource source;

        Source source1 = (Source) gedcom.createEntity(Gedcom.SOUR, "S1");
        source1.addProperty("TITL", "75000 Paris BMS");
        Source source2 = (Source) gedcom.createEntity(Gedcom.SOUR, "S2");
        source2.addProperty("TITL", "75000 Paris Etat civil");
        Source source3 = (Source) gedcom.createEntity(Gedcom.SOUR, "S3");
        source2.addProperty("TITL", "35000 Brest BMS");


        Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I1");
        husband.setName("fatherFirstName", "FATHERLASTNAME");
        husband.setSex(PropertySex.MALE);
        birth = husband.addProperty("BIRT","" );
        birth.addProperty("DATE","01 JAN 1970", 1);
        birth.addProperty("PLACE","Paris", 2);
        birth.addProperty("SOUR","@S1@",3);

        Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "I2");
        wife.setName("motherFirstName", "MOTHERLASTNAME");
        wife.setSex(PropertySex.FEMALE);

        Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "I3");
        child1.setName("firstname1", "FATHERLASTNAME");
        child1.setSex(PropertySex.FEMALE);
        birth = child1.addProperty("BIRT", "01/01/2000");
        birth.addProperty("DATE","01 JAN 2000");
        birth.addProperty("PLACE","Brest");
        birth.addProperty("SOUR","@S2@");
        
        Indi child2 = (Indi) gedcom.createEntity(Gedcom.INDI, "I4");
        child2.setName("firstname2", "FATHERLASTNAME");
        child2.setSex(PropertySex.FEMALE);
        birth = child2.addProperty("BIRT", "01/01/2000");
        birth.addProperty("DATE","01 JAN 2000");
        birth.addProperty("PLACE","Brest");
        Property sourcexref = birth.addProperty("SOUR","@S2@");
        //((PropertyXRef)sourcexref).link();


        Indi child3 = (Indi) gedcom.createEntity(Gedcom.INDI, "I5");
        child2.setName("firstname2", "FATHERLASTNAME");
        child2.setSex(PropertySex.FEMALE);

        Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F1");
        family.setHusband(husband);
        family.setWife(wife);
        family.addChild(child1);
        family.addChild(child2);
        family.addChild(child3);

        Indi cousin = (Indi) gedcom.createEntity(Gedcom.INDI, "I10");
        cousin.setName("cousin", "FATHERLASTNAME");
        cousin.setSex(PropertySex.MALE);
        birth = cousin.addProperty("BIRT","" );
        birth.addProperty("DATE","02 FEB 1972", 1);
        birth.addProperty("PLACE","Paris", 2);
        birth.addProperty("SOUR","@S1@",3);

        for(Property property : gedcom.getProperties(new TagPath("INDI:BIRT:SOUR"))) {
            ((PropertyXRef)property).link();
        }

        return gedcom;
    }

    /**
     * creation 'un gedcom minimal
     * @return
     * @throws GedcomException
     */
    public static Gedcom createGedcom2() throws GedcomException {

        Gedcom gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);

        Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I1");
        husband.setName("fatherFirstName", "fatherName");
        husband.setSex(PropertySex.MALE);
        husband.addProperty("OCCU", "profession1");


        Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "I2");
        wife.setName("motherFirstName", "motherName");
        wife.setSex(PropertySex.FEMALE);
        husband.addProperty("OCCU", "profession mere");

        Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "I3");
        child1.setName("firstname1", "fatherName");
        child1.setSex(PropertySex.FEMALE);
        husband.addProperty("OCCU", "profession1");

        Indi child2 = (Indi) gedcom.createEntity(Gedcom.INDI, "I4");
        child2.setName("firstname2", "fatherName");
        child2.setSex(PropertySex.FEMALE);

        Indi child3 = (Indi) gedcom.createEntity(Gedcom.INDI, "I5");
        child2.setName("firstname2", "fatherName");
        child2.setSex(PropertySex.FEMALE);

        Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F1");
        family.setHusband(husband);
        family.setWife(wife);
        family.addChild(child1);
        family.addChild(child2);
        family.addChild(child3);
        return gedcom;
    }

    protected static Component getComponentByName(Component parent, String name) {

        if (parent.getName() != null && parent.getName().equals(name)) {
            return parent;
        }

        if (parent instanceof Container) {
            Component[] components = ((Container) parent).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component child = getComponentByName(components[i], name);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }
}
