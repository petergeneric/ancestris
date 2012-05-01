package ancestris.modules.releve;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
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

        Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I1");
        husband.setName("fatherFirstName", "fatherName");
        husband.setSex(PropertySex.MALE);

        Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "I2");
        wife.setName("motherFirstName", "motherName");
        wife.setSex(PropertySex.FEMALE);

        Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "I3");
        child1.setName("firstname1", "fatherName");
        child1.setSex(PropertySex.FEMALE);

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
