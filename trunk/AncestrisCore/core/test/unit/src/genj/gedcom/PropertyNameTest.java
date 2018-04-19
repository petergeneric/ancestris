/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.util.Origin;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

/**
 * Testing PropertyName
 */
public class PropertyNameTest extends TestCase {

    private Gedcom gedcom;

    private Indi indi;

    /**
     * Prepare a fake indi
     */
    @Before
    @Override
    protected void setUp() throws Exception {

        // create gedcom
        gedcom = new Gedcom(Origin.create("file://foo.ged"));

        // create individuals
        indi = (Indi) gedcom.createEntity("INDI");

        GedcomOptions.getInstance().setUpperCaseNames(false);
        // done
    }

//    public void testInternal() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
//        Method gedcomToValue = PropertyName.class.getDeclaredMethod("gedcomToValue", String.class);
//        gedcomToValue.setAccessible(true);
//        assertEquals("Pierre Paul", gedcomToValue.invoke(null, "Pierre,Paul"));
//        assertEquals("Pierre Paul", gedcomToValue.invoke(null, "Pierre, Paul"));
//    }
    @Test
    public void testGuessed(){
        PropertyName name;
        name = readName(new String[]{"Marie Madeleine/DUPONT/"});
        assertEquals("Marie Madeleine", name.getFirstName());
        assertEquals("Marie, Madeleine", name.getPropertyValue("GIVN"));
        assertEquals("DUPONT", name.getLastName());
        assertEquals("DUPONT", name.getPropertyValue("SURN"));
        assertNotNull(name.getProperty("GIVN"));
        assertTrue(name.getProperty("GIVN").isGuessed());
        assertNotNull(name.getProperty("SURN"));
        assertTrue(name.getProperty("SURN").isGuessed());
        name.getProperty("GIVN").setValue("Marie Françoise");
        assertEquals("Marie Françoise,", name.getFirstName());
        assertEquals("Marie Françoise", name.getPropertyValue("GIVN"));
        assertFalse(name.getProperty("GIVN").isGuessed());
        assertFalse(name.getProperty("SURN").isGuessed());
    }

    @Test
    public void testSubTags(){
        PropertyName name;
        name = readName(new String[]{"Marie Madeleine/DUPONT/"});
        name.getProperty("SURN").setValue("DUPOND");
        assertEquals("Marie Madeleine/DUPOND/", name.getValue());        
        name.getProperty("SURN").setValue("DUPOND DURAND");
        assertEquals("Marie Madeleine/DUPOND DURAND/", name.getValue());        
    }
    
    @Test
    public void testFromGedomStandard() {

        PropertyName name;
        System.out.println("given name only or surname not known");
        name = readName(new String[]{"William Lee"});
        assertEquals("William Lee", name.getFirstName());
        assertEquals("William, Lee", name.getPropertyValue("GIVN"));

        // Space in GIVN tag: one First Name only
        name = readName(new String[]{"William Lee", null, "William Lee"});
        assertEquals("William Lee,", name.getFirstName());
        assertEquals("William Lee", name.getPropertyValue("GIVN"));
        //William Lee /Parry/, space in GIVN
        name = readName(new String[]{"William Lee/Parry/", null, "William Lee"});
        name.fixNameValue();
        assertEquals("Parry", name.getLastName());
        assertEquals("William Lee,", name.getFirstName());
        assertEquals("William Lee", name.getPropertyValue("GIVN"));
        // First Name in NAME value should not contain ','
        assertEquals("William Lee/Parry/", name.getValue());

        // /Parry/ (surname only)
        name = readName(new String[]{"/Parry/"});
        assertEquals("Parry", name.getLastName());
        assertEquals("", name.getFirstName());

        //William Lee /Parry/
        name = readName(new String[]{"William Lee/Parry/"});
        assertEquals("Parry", name.getLastName());
        assertEquals("William Lee", name.getFirstName());
        assertEquals("William, Lee", name.getPropertyValue("GIVN"));

        //William Lee /Mac Parry/ (both parts (Mac and Parry) are surname parts
        name = readName(new String[]{"William Lee/Mac Parry/"});
        assertEquals("Mac Parry", name.getLastName());
        assertEquals("Mac Parry", name.getPropertyValue("SURN"));
        assertEquals("William Lee", name.getFirstName());
        assertEquals("William, Lee", name.getPropertyValue("GIVN"));

        //William /Lee/ Parry (surname imbedded in the name string)
        name = readName(new String[]{"William /Lee/ Parry"});
        assertEquals("Lee", name.getLastName());
        assertEquals("William", name.getFirstName());
        assertEquals("William", name.getPropertyValue("GIVN"));

        //Lt. Cmndr. Joseph /Allen/ jr. 
        name = readName(new String[]{"Lt. Cmndr. Joseph /Allen/ jr.", "Lt., Cmndr."});
        assertEquals("Allen", name.getLastName());
        assertEquals("Joseph", name.getFirstName());
        assertEquals("Lt. Cmndr.", name.getNamePrefix());
        assertEquals("Lt., Cmndr.", name.getPropertyValue("NPFX"));
        assertEquals("jr.", name.getSuffix());

    }

    public void testGivn() {
        // Read from gedcom (NAME, NPFX, GIVN, SPFX,SURN, NSFX, NICK.
        PropertyName name = readName(new String[]{"Marie Madeleine/DUPONT/", null, "Marie, Madeleine", null, "DUPONT"});
        assertEquals("DUPONT", name.getLastName());
        assertEquals("Marie Madeleine", name.getFirstName());
        assertEquals("DUPONT", name.getPropertyValue("SURN"));
        assertEquals("Marie, Madeleine", name.getPropertyValue("GIVN"));

        name.setName("Marie Madeleine,", "DUPONT");
        assertEquals("Marie Madeleine", name.getPropertyValue("GIVN"));
        assertEquals("Marie Madeleine/DUPONT/", name.getValue());
        assertEquals("Marie Madeleine,", name.getFirstName());
        assertEquals("DUPONT", name.getPropertyValue("SURN"));
        // Set GIVN tag
        Property givn = name.getProperty("GIVN");
        assertNotNull(givn);
        givn.setValue("Marie Madeleine, Françoise");
        assertEquals("Marie Madeleine, Françoise", givn.getValue());
        assertEquals("Marie Madeleine, Françoise", name.getFirstName());
        assertEquals("Marie Madeleine, Françoise/DUPONT/", name.getValue());
    }

//    @Ignore
//    @Test
    /*
     * tag NAME doesn't match SURN and GIVN: no gedcom file update except
     * when GIVN or SURN or any other is modified
     */
    public void testWrongName() {
        // Read from gedcom (NAME, NPFX, GIVN, SPFX,SURN, NSFX, NICK.
        PropertyName name = readName(new String[]{"John/DOE/", null, "Pierre,Paul", null, "DUPONT"});
        // Check internal structure
        assertEquals("DUPONT", name.getLastName());
        assertEquals("Pierre Paul", name.getFirstName());
        assertEquals("DUPONT", name.getPropertyValue("SURN"));
        assertEquals("Pierre,Paul", name.getPropertyValue("GIVN"));
        // Check write gedcom unchanged
        assertEquals("John/DOE/", name.getValue());
        // update with editor (setName and co)
        name.setName("DUPOND");
        // Check write gedcom
        assertEquals("Pierre Paul/DUPOND/", name.getValue());
        // GIVN and SURN reformatted
        assertEquals("DUPOND", name.getPropertyValue("SURN"));
        assertEquals("Pierre, Paul", name.getPropertyValue("GIVN")); // Should we reformat (insert a space)?
    }
//    @Ignore
//    @Test

    public void testTwoSurn() {
        // Read from gedcom (NAME, NPFX, GIVN, SPFX,SURN, NSFX, NICK.
        PropertyName name = readName(new String[]{"Pierre Paul/DUPONT/", null, null, null, "DUPONT, DUPOND"});
        // Check internal structure
        assertEquals("DUPONT, DUPOND", name.getLastName());
        assertEquals("Pierre Paul", name.getFirstName());
        assertEquals("DUPONT, DUPOND", name.getPropertyValue("SURN"));
        assertEquals("Pierre, Paul", name.getPropertyValue("GIVN"));
        // Check write gedcom unchanged
        assertEquals("Pierre Paul/DUPONT/", name.getValue());
        // update with editor (setName and co)
        name.setName("DUPOND, DUPONT");
        // Check internal structure
        assertEquals("DUPOND, DUPONT", name.getLastName());
        // Check write gedcom
        assertEquals("Pierre Paul/DUPOND, DUPONT/", name.getValue());
    }

    /**
     * Various tests. Read Property NAME from gedcom with sub properties. Then
     * apply fix and check result
     */
    public void testMisc() {
        // Read from gedcom (NAME, NPFX, GIVN, SPFX, SURN, NSFX, NICK.
        PropertyName name = assertUnchanged(new String[]{"Charles Justin /MAURIAMÉ/",
            null, null, null, null, null,
            "(tintin)"});
        assertEquals("(tintin)", name.getNick());

        name = assertUnchanged(new String[]{"Catherine /MAYER/ (MEYER)"});

        name = assertUnchanged(new String[]{"Marie Ernestine /CUNIN/",
            null, null, null, null, null,
            ""});
        assertEquals("", name.getNick());

        name = readName(new String[]{"Florent /MAYER/ (MAGER, MAIER)"});
        name.fixNameValue();
        // No change in NSFX
        assertEquals("Florent/MAYER/ (MAGER, MAIER)", name.getValue());

        name = assertUnchanged(new String[]{"Marie-Thérèse /de VOLDER/",
            null, null, "de", "VOLDER"});

        name = assertUnchanged(new String[]{"Georges /MAYER/",
            null, null, null, null, null,
            "(MEYER)"});

        name = assertUnchanged(new String[]{"Anne /GALL(E)/"});
        name = assertUnchanged(new String[]{"Catherine /BERTON/",
            null, null, null, null, null,
            "'BOURTON)"});
        assertEquals("'BOURTON)", name.getNick());

        name = assertUnchanged(new String[]{"Françoise /MONPEURT/ (MONPERT)",
            null, null, null, null, null,
            ""});

        name = assertUnchanged(new String[]{"Marguerite /d' ARGENT/",
            null, null, "d'", "ARGENT"});

        name = assertUnchanged(new String[]{"Jean Nicolas /HELLENBRANEN/",
            null, "Jean Nicolas", null, null, null,
            ""});
        assertEquals("", name.getNick());
        assertEquals("Jean Nicolas", name.getPropertyValue("GIVN"));
        assertEquals("Jean Nicolas,", name.getFirstName());

        name = assertUnchanged(new String[]{"Jeanne /de ANDREIS/",
            null, null, "de", "ANDREIS", null,
            "(Joana)"});

        name = assertUnchanged(new String[]{"Bietrix (Beatrix) /de LASSUS/",
            null, "Bietrix (Beatrix)", "de", "LASSUS"});

        name = assertUnchanged(new String[]{"Jehennon /./"});

        name = assertUnchanged(new String[]{"Salomé /?/"});

        name = assertUnchanged(new String[]{""});

    }

    private PropertyName assertUnchanged(String[] np) {
        PropertyName name = readName(np);
        name.fixNameValue();
        String nv = np[0].replaceAll(" */", "/");
        assertEquals(nv, name.getValue());
        return name;
    }

    public void testBourbon() {
        PropertyName name = assertUnchanged(new String[]{"Anne /HABSBOURG/",
            null, null, null, null, null,
            "D'Autriche"});

        name = assertUnchanged(new String[]{"Louis XIV /CAPET/"});

        name = assertUnchanged(new String[]{"Philippe I /CAPET/",
            null, null, null, null, null,
            "D'Orléans"});

        name = assertUnchanged(new String[]{"Louis /CAPET/",
            null, null, null, null, null,
            "Le Grand Dauphin"});

        name = assertUnchanged(new String[]{"Marie-Thérèse /HABSBOURG/",
            null, null, null, null, null,
            "D'Autriche"});

        name = assertUnchanged(new String[]{"Françoise /D'AUBIGNÉ/",
            null, null, null, null, null,
            "De Maintenon"});

        name = assertUnchanged(new String[]{"Marie Anne Victoire /DE BAVIÈRE/"});

        name = assertUnchanged(new String[]{"Charlotte-Élisabeth /VON DER PFALZ-SIMMERN/"});

        name = assertUnchanged(new String[]{"Marie-Josèphe /VON SACHSEN/",
            null, null, null, null, null,
            "De Saxe"});

        name = assertUnchanged(new String[]{"Marie Antoinette /DE HASBOURG-LORRAINE/",
            null, null, null, null, null,
            "D'Autriche"});
    }

//    @Ignore
//    @Test
    public void testTemplate() {
        // Read from gedcom (NAME, NPFX, GIVN, SPFX,SURN, NSFX, NIC.
        // Check internal structure
        // Check write gedcom unchanged
        // update with editor (setName and co)
        // Check internal structure
        // Check write gedcom
    }

    static final String[] TAGNAMES = {
        "NAME",
        "NPFX", "GIVN", "SPFX", "SURN", "NSFX",
        "NICK"};

    /**
     * Create a new PropertyName and link to indi (created in setUp).
     *
     * @param tags Array of tags value in NAME, NPFX,GIVN,SPFX,SURN,NSFX, NICK.
     *
     * null means no tag present, all missing tags are considered as not present
     * in gedcom
     * @return
     */
    private PropertyName readName(String[] tags) {
        indi.delProperties("NAME");
        PropertyName name = (PropertyName) indi.addProperty("NAME", "");
//        PropertyName name = new PropertyName();
        try {
            name.mutePropertyChange();
            for (int i = 1; i < tags.length; i++) { // Skip NAME as in PropertyReader
                if (tags[i] != null) {
                    Property p = name.addProperty(TAGNAMES[i], "");
                    p.setValue(tags[i]);
                }
            }
            name.setValue(tags[0]);
        } finally {
            name.unmutePropertyChange(true);
        }
        return name;
    }

    static class MyError extends ErrorCollector {

        public void verify() throws Throwable {
            super.verify();
        }
    }
} //PropertyNameTest
