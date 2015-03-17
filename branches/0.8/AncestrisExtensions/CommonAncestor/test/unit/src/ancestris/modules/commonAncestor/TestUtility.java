package ancestris.modules.commonAncestor;

import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;

/**
 *
 * @author Michel
 */
public class TestUtility {

    protected static final int MALE = 1;
    protected static final int FEMALE = 2;

    public static Context createContext() throws GedcomException {
        return new Context(createGedcom());
    }

    public static Gedcom createGedcom() throws GedcomException {

        Gedcom gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);

        Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I1");
        husband.setName("husband", "Test");
        husband.setSex(MALE);

        Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "I2");
        wife.setName("wife", "WifeName");
        wife.setSex(FEMALE);

        Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "I3");
        child1.setName("child1", "Test");
        child1.setSex(FEMALE);

        Indi child2 = (Indi) gedcom.createEntity(Gedcom.INDI, "I4");
        child2.setName("child2", "Test");
        child2.setSex(FEMALE);

        Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F1");
        family.setHusband(husband);
        family.setWife(wife);
        family.addChild(child1);
        family.addChild(child2);
        return gedcom;
    }

//    public static  Gedcom loadGedcom() {
//        Gedcom gedcom = null;
//
//        System.setProperty("netbeans.user", "michel");
//        GedcomReader reader;
//        Origin origin = null;
//        try {
//            //URL url = new URL("file://c:/test.ged");
//            URL url = new URL("file", "", "D:/Généalogie/test.ged");
//            origin = Origin.create(url);
//            // .. prepare our reader
//            reader = GedcomReaderFactory.createReader(origin, null);
//            Context context = null;
//            gedcom = reader.read();
//
//        } catch (GedcomIOException ex) {
//            fail("read to" + origin);
//        } catch (IOException ex) {
//            fail("Connect to " + origin);
//            System.out.println(ex);
//        }
//        return gedcom;
//    }
}
