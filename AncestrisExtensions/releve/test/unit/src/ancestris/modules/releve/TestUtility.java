package ancestris.modules.releve;

import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.filechooser.FileSystemView;
import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class TestUtility extends TestCase {

    public static File createFile( String data ) throws IOException  {
        File file = new File(System.getProperty("user.home") + File.separator + "ancestris.txt");
        boolean append = false;
        FileWriter writer = writer = new FileWriter(file, append);
        writer.write(data);            
        writer.close();        
        return file;

    }
    
    /**
     * creation 'un gedcom minimal
     * @return
     * @throws GedcomException
     */
    public static Gedcom createGedcom() throws GedcomException, MalformedURLException {

        // Gedcom options
        GedcomOptions.getInstance().setUseSpacedPlaces(false);
        
        Gedcom gedcom;
        gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);
        Property birth;
        
        Source source1 = (Source) gedcom.createEntity(Gedcom.SOUR, "S1");
        source1.addProperty("TITL", "BMS Paris");
        Source source2 = (Source) gedcom.createEntity(Gedcom.SOUR, "S2");
        source2.addProperty("TITL", "Etat civil Paris");
        Source source3 = (Source) gedcom.createEntity(Gedcom.SOUR, "S3");
        source3.addProperty("TITL", "Etat civil Brest");


        Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I1");
        husband.setName("Fatherfirstname", "FATHERLASTNAME");
        husband.setSex(PropertySex.MALE);
        birth = husband.addProperty("BIRT","" );
        birth.addProperty("DATE","01 JAN 1970", 1);
        birth.addProperty("PLAC","Paris,75000,,state,country,", 2);
        birth.addProperty("SOUR","@S1@",3);
        husband.addProperty("OCCU", "I1occupation");
        PropertyPlace placeParis = (PropertyPlace) birth.getProperty("PLAC");
        placeParis.setCoordinates("N48.8534", "E2.3486");

        Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "Wife2");
        wife.setName("Motherfirstname", "MOTHERLASTNAME");
        wife.setSex(PropertySex.FEMALE);

        Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "child1");
        child1.setName("OneFirstName", "FATHERLASTNAME");
        child1.setSex(PropertySex.FEMALE);
        birth = child1.addProperty("BIRT", "");
        birth.addProperty("DATE","01 JAN 2000");
        birth.addProperty("PLAC","Brest");
        birth.addProperty("SOUR","@S2@");

        PropertyPlace placeBrest = (PropertyPlace) birth.getProperty("PLAC");
        placeBrest.setCoordinates("N52.09755", "E23.68775");
        
        Indi child2 = (Indi) gedcom.createEntity(Gedcom.INDI, "child2");
        child2.setName("TwoFirstName", "FATHERLASTNAME");
        child2.setSex(PropertySex.FEMALE);
        birth = child2.addProperty("BIRT", "");
        birth.addProperty("DATE","BEF 2004");
        birth.addProperty("PLAC","Brest");
        //Property sourcexref = birth.addProperty("SOUR","@S2@");
        

        Indi child3 = (Indi) gedcom.createEntity(Gedcom.INDI, "child3");
        child3.setName("ThreeFirstName", "FATHERLASTNAME");
        child3.setSex(PropertySex.FEMALE);

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
        birth.addProperty("PLAC","Paris", 2);
        birth.addProperty("SOUR","@S1@",3);
        
        Indi sansfamille1 = (Indi) gedcom.createEntity(Gedcom.INDI, "sansfamille1");
        sansfamille1.setName("sansfamille1", "FATHERLASTNAME");
        sansfamille1.setSex(PropertySex.MALE);
        birth = sansfamille1.addProperty("BIRT","" );
        birth.addProperty("DATE","01 JAN 2000");
        
        // je complete les referecences entre les entités et sources
        for(Property property : gedcom.getProperties(new TagPath("INDI:BIRT:SOUR"))) {
            ((PropertyXRef)property).link();
        }

        return gedcom;
    }

    /**
     * creation d'un gedcom minimal
     * @return
     * @throws GedcomException
     */
    public static Gedcom createGedcom2() throws GedcomException {
        // Gedcom options
        GedcomOptions.getInstance().setUseSpacedPlaces(false);
        
        Gedcom gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);

        Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I1");
        husband.setName("FatherFirstName", "FATHERLASTNAME");
        husband.setSex(PropertySex.MALE);
        husband.addProperty("OCCU", "profession1");


        Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "I2");
        wife.setName("MotherFirstName", "MOTHERLASTNAME");
        wife.setSex(PropertySex.FEMALE);
        wife.addProperty("OCCU", "profession mere");

        Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "I3");
        child1.setName("firstname1", "FATHERLASTNAME");
        child1.setSex(PropertySex.FEMALE);
        child1.addProperty("OCCU", "profession1");

        Indi child2 = (Indi) gedcom.createEntity(Gedcom.INDI, "I4");
        child2.setName("firstname2", "FATHERLASTNAME");
        child2.setSex(PropertySex.FEMALE);

        Indi child3 = (Indi) gedcom.createEntity(Gedcom.INDI, "I5");
        child2.setName("firstname2", "FATHERLASTNAME");
        child2.setSex(PropertySex.FEMALE);

        Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F1");
        family.setHusband(husband);
        family.setWife(wife);
        family.addChild(child1);
        family.addChild(child2);
        family.addChild(child3);
        return gedcom;
    }
    
    
    public static RecordBirth getRecordBirth() {
        RecordBirth record = new RecordBirth();
        record.setEventDate("01/01/2000");
        record.setCote("cote");
        record.setFreeComment("photo");
        record.setGeneralComment("general comment");
        record.getIndi().set("indiFirstname", "indiLastname", "M", "", "5/4/1842", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.getIndi().setFather("indiFatherFirstName", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "indiFatherDead", "70y");
        record.getIndi().setMother("indiMotherFirstName", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "indiMotherDead", "72y");
        record.getWitness1().setValue("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.getWitness2().setValue("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.getWitness3().setValue("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.getWitness4().setValue("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }

    public static RecordMarriage getRecordMarriage() {
        RecordMarriage record = new RecordMarriage();
        record.setEventDate("01/01/2000");
        record.setCote("cote");
        record.setFreeComment("photo");
        record.setGeneralComment("general comment");
        record.getIndi().set("indiFirstname", "indiLastname", "M", "20y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.getIndi().setMarried("indiMarriedFirstname", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "true");
        record.getIndi().setFather("indiFatherFirstname", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "false", "70y");
        record.getIndi().setMother("indiMotherFirstname", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "false", "72y");
        record.getWife().set("wifeFirstname", "wifeLastname", "F", "wifeAge", "02/02/1992", "wifeBirthPlace", "wifeBirthAddress", "wifeOccupation", "wifeResidence", "wifeAddress", "wifeComment");
        record.getWife().setMarried("wifeMarriedFirstname", "wifeMarriedLastname", "wifeMarriedOccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifeMarriedComment", "false");
        record.getWife().setFather("wifeFatherFirstname", "wifeFatherLastname", "wifeFatherOccupation", "wifeFatherResidence", "wifeFatherAddress", "wifeFatherComment", "false", "71y");
        record.getWife().setMother("wifeMotherFirstname", "wifeMotherLastname", "wifeMotherOccupation", "wifeMotherResidence", "wifeMotherAddress", "wifeMotherComment", "false", "73y");
        record.getWitness1().setValue("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.getWitness2().setValue("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.getWitness3().setValue("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.getWitness4().setValue("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }
    
    public static RecordDeath getRecordDeath() {
        RecordDeath record = new RecordDeath();
        record.setEventDate("11/11/2000");
        record.setCote("cote");
        record.setGeneralComment("generalcomment");
        record.setFreeComment("photo");
        record.getIndi().set("indiFirstname", "indiLastname", "M", "30y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.getIndi().setMarried("indiMarriedFirstame", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "false");
        record.getIndi().setFather("indiFatherFirstname", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "false", "70y");
        record.getIndi().setMother("indiMotherFirstname", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "false", "72y");
        record.getWitness1().setValue("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.getWitness2().setValue("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.getWitness3().setValue("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.getWitness4().setValue("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }
    
    public static RecordMisc getRecordMisc() {
        RecordMisc record = new RecordMisc();
        record.setEventDate("29/02/2012");
        record.setSecondDate("04/04/2012");
        record.setCote("cote");
        record.setParish("parish");
        record.setNotary("Notary");
        record.setEventType("eventname");
        record.setGeneralComment("generalcomment");
        record.setFreeComment("photo");
        record.getIndi().set("indiFirstname", "indiLastname", "M", "20y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.getIndi().setMarried("indiMarriedFirstname", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "true");
        record.getIndi().setFather("indiFatherFirstname", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "true", "70y");
        record.getIndi().setMother("indiMotherFirstname", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "true", "72y");
        record.getWife().set("wifeFirstname", "wifeLastname", "F", "wifeAge", "02/02/1992", "wifeBirthPlace", "wifeBirthAddress", "wifeOccupation", "wifeResidence", "wifeAddress", "wifeComment");
        record.getWife().setMarried("wifeMarriedFirstname", "wifeMarriedLastname", "wifeMarriedOccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifeMarriedComment", "true");
        record.getWife().setFather("wifeFatherFirstname", "wifeFatherLastname", "wifeFatherOccupation", "wifeFatherResidence", "wifeFatherAddress", "wifeFatherComment", "false", "71y");
        record.getWife().setMother("wifeMotherFirstname", "wifeMotherLastname", "wifeMotherOccupation", "wifeMotherResidence", "wifeMotherAddress", "wifeMotherComment", "false", "73y");
        record.getWitness1().setValue("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.getWitness2().setValue("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.getWitness3().setValue("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.getWitness4().setValue("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
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

    public static final Object lock = new Object();
    public static void waitForDialogClose(final java.awt.Window dialog) {
        try {
            Thread t = new Thread() {

                @Override
                public void run() {
                    synchronized (lock) {
                        while (dialog.isVisible()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                //e.printStackTrace();
                            }
                        }
                        lock.notifyAll();
                    }
                }
            };
            t.start();
            dialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent arg0) {
                    synchronized (lock) {
                        dialog.setVisible(false);
                        lock.notifyAll();
                    }
                }
            });
            t.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    public void testDefaultDirectory() {

        // default home
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File f = fsv.getDefaultDirectory();
        System.out.println("testDefaultDirectory="+f);
    }

     public void testOS() {

        System.out.println("testOS="+System.getProperty("os.name"));
    }
}
