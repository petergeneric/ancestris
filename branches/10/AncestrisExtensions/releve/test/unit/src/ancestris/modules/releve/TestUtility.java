package ancestris.modules.releve;

import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.merge.MergeDialog;
import ancestris.modules.releve.merge.MergeManager;
import ancestris.modules.releve.merge.MergeRecord;
import ancestris.modules.releve.merge.ProposalHelper;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.filechooser.FileSystemView;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class TestUtility  {

    public static File createFile( String data ) throws IOException  {
        File file = new File(System.getProperty("user.home") + File.separator + "ancestris.txt");
        boolean append = false;
        FileWriter writer = new FileWriter(file, append);
        writer.write(data);
        writer.close();
        return file;

    }

    static {
        GedcomOptions.getInstance().setUseSpacedPlaces(true);
        //GedcomOptions.getInstance().setReplaceSpaceSeparatorWithComma(false);
    }

    /**
     * creation 'un gedcom minimal
     * @return
     * @throws GedcomException
     * @throws java.net.MalformedURLException
     */
    public static Gedcom createGedcomEmpty() throws GedcomException, MalformedURLException {
        Gedcom gedcom;
        gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);
        return gedcom;
    }


    /**
     * creation 'un gedcom minimal
     * @return
     * @throws GedcomException
     * @throws java.net.MalformedURLException
     */
    public static Gedcom createGedcom() throws GedcomException, MalformedURLException {

        // Gedcom options


        Gedcom gedcom;
        gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);


        Source source1 = (Source) gedcom.createEntity(Gedcom.SOUR, "S1");
        source1.addProperty("TITL", "BMS Paris");
        Source source2 = (Source) gedcom.createEntity(Gedcom.SOUR, "S2");
        source2.addProperty("TITL", "Etat civil Paris");
        Source source3 = (Source) gedcom.createEntity(Gedcom.SOUR, "S3");
        source3.addProperty("TITL", "Etat civil Brest");


        {
            Property birth;
            Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I1");

            husband.setName("Fatherfirstname", "FATHERLASTNAME");
            husband.setSex(PropertySex.MALE);
            birth = husband.addProperty("BIRT", "");
            birth.addProperty("DATE", "01 JAN 1970", 1);
            birth.addProperty("PLAC", "Paris,75000,,state,country,", 2);
            birth.addProperty("SOUR", "@S1@", 3);
            husband.addProperty("OCCU", "I1occupation");
            PropertyPlace placeParis = (PropertyPlace) birth.getProperty("PLAC");
            placeParis.setCoordinates("N48.8534", "E2.3486");

            Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "Wife2");
            wife.setName("Motherfirstname", "MOTHERLASTNAME");
            wife.setSex(PropertySex.FEMALE);

            Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "child1");
            child1.setName("One First Name", "FATHERLASTNAME");
            child1.setSex(PropertySex.FEMALE);
            birth = child1.addProperty("BIRT", "");
            birth.addProperty("DATE", "01 JAN 2000");
            birth.addProperty("PLAC", "Brest");
            birth.addProperty("SOUR", "@S2@");

            PropertyPlace placeBrest = (PropertyPlace) birth.getProperty("PLAC");
            placeBrest.setCoordinates("N52.09755", "E23.68775");

            Indi child2 = (Indi) gedcom.createEntity(Gedcom.INDI, "child2");
            child2.setName("Two-First-Name", "FATHERLASTNAME");
            child2.setSex(PropertySex.FEMALE);
            birth = child2.addProperty("BIRT", "");
            birth.addProperty("DATE", "BEF 2004");
            birth.addProperty("PLAC", "Brest");
            //Property sourcexref = birth.addProperty("SOUR","@S2@");

            Indi child3 = (Indi) gedcom.createEntity(Gedcom.INDI, "child3");
            child3.setName("Three, First, Name", "FATHERLASTNAME");
            child3.setSex(PropertySex.FEMALE);

            Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F1");
            family.setHusband(husband);
            family.setWife(wife);
            family.addChild(child1);
            family.addChild(child2);
            family.addChild(child3);
        }

        {
            Indi cousin = (Indi) gedcom.createEntity(Gedcom.INDI, "I10");
            cousin.setName("cousin", "FATHERLASTNAME");
            cousin.setSex(PropertySex.MALE);
            Property birth = cousin.addProperty("BIRT", "");
            birth.addProperty("DATE", "02 FEB 1972", 1);
            birth.addProperty("PLAC", "Paris", 2);
            birth.addProperty("SOUR", "@S1@", 3);

            Indi sansfamille1 = (Indi) gedcom.createEntity(Gedcom.INDI, "sansfamille1");
            sansfamille1.setName("sansfamille1", "FATHERLASTNAME");
            sansfamille1.setSex(PropertySex.MALE);
            birth = sansfamille1.addProperty("BIRT", "");
            birth.addProperty("DATE", "01 JAN 2000");

            // je complete les referecences entre les entités et sources
            for (Property property : gedcom.getProperties(new TagPath("INDI:BIRT:SOUR"))) {
                ((PropertyXRef) property).link();
            }
        }




        return gedcom;
    }

    /**
     * creation d'un gedcom minimal
     * @return
     * @throws GedcomException
     */
    public static Gedcom createGedcomF2() throws GedcomException {
        // Gedcom options

        Gedcom gedcom = new Gedcom();
        gedcom.setGrammar(Grammar.V55);

        Source source1 = (Source) gedcom.createEntity(Gedcom.SOUR, "S1");
        source1.addProperty("TITL", "BMS Paris");
        Source source2 = (Source) gedcom.createEntity(Gedcom.SOUR, "S2");
        source2.addProperty("TITL", "Etat civil Paris");

        {
            // famille 2
            Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "IF2husband");
            husband.setName("Jupiter", "PLANET");
            husband.setSex(PropertySex.MALE);
            Property birth = husband.addProperty("BIRT", "");
            birth.addProperty("DATE", "01 JAN 1950", 1);
            birth.addProperty("PLAC", "Paris,75000,,state,country,", 2);
            birth.addProperty("SOUR", "@S1@", 3);
            husband.addProperty("OCCU", "I1occupation");
            PropertyPlace placeParis = (PropertyPlace) birth.getProperty("PLAC");
            placeParis.setCoordinates("N48.8534", "E2.3486");

            Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "IF2Wife");
            wife.setName("Venus", "GALAXY");
            wife.setSex(PropertySex.FEMALE);
            husband.addProperty("OCCU", "F2Wifeoccupation");

            Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "IF2child1");
            child1.setName("Mercure", "PLANET");
            child1.setSex(PropertySex.MALE);
            birth = child1.addProperty("BIRT", "");
            birth.addProperty("DATE", "01 APR 1980");
            birth.addProperty("PLAC", "Brest");
            birth.addProperty("SOUR", "@S2@");

            Indi child2 = (Indi) gedcom.createEntity(Gedcom.INDI, "IF2child2");
            child2.setName("Janus", "PLANET");
            child2.setSex(PropertySex.FEMALE);
            birth = child1.addProperty("BIRT", "");
            birth.addProperty("DATE", "01 SEP 1982");
            birth.addProperty("PLAC", "Brest");
            birth.addProperty("SOUR", "@S2@");

            Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F2");
            family.setHusband(husband);
            family.setWife(wife);
            PropertyDate marriage = family.getMarriageDate(true);  // create=true
            marriage.setValue("05 FEB 1979");

            // je complete les referecences entre les entités et sources
            for (Property property : gedcom.getProperties(new TagPath("INDI:BIRT:SOUR"))) {
                ((PropertyXRef) property).link();
            }


        }
        return gedcom;
    }

    static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("Paris","75000","","state","country");
        return recordsInfoPlace;
    }

    public static RecordBirth getRecordBirth() {
        RecordBirth record = new RecordBirth();
        record.setFieldValue(FieldType.eventDate, "01/01/2000");
        record.setFieldValue(FieldType.cote, "cote");
        record.setFieldValue(FieldType.freeComment,  "photo");
        record.setFieldValue(FieldType.generalComment, "general comment");
        record.setIndi("indiFirstname", "indiLastname", "M", "", "5/4/1842", "indiBirthPlace", "indiBirthAddress", "", "indiResidence", "indiAddress", "indiComment");
        record.setIndiFather("indiFatherFirstName", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "indiFatherDead", "70y");
        record.setIndiMother("indiMotherFirstName", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "indiMotherDead", "72y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }

    public static RecordMarriage getRecordMarriage() {
        RecordMarriage record = new RecordMarriage();
        record.setFieldValue(FieldType.eventDate, "01/01/2000");
        record.setFieldValue(FieldType.cote, "cote");
        record.setFieldValue(FieldType.freeComment,  "photo");
        record.setFieldValue(FieldType.generalComment, "general comment");
        record.setIndi("indiFirstname", "indiLastname", "M", "20y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.setIndiMarried("indiMarriedFirstname", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "true");
        record.setIndiFather("indiFatherFirstname", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "false", "70y");
        record.setIndiMother("indiMotherFirstname", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "false", "72y");
        record.setWife("wifeFirstname", "wifeLastname", "F", "wifeAge", "02/02/1992", "wifeBirthPlace", "wifeBirthAddress", "wifeOccupation", "wifeResidence", "wifeAddress", "wifeComment");
        record.setWifeMarried("wifeMarriedFirstname", "wifeMarriedLastname", "wifeMarriedOccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifeMarriedComment", "ALIVE");
        record.setWifeFather("wifeFatherFirstname", "wifeFatherLastname", "wifeFatherOccupation", "wifeFatherResidence", "wifeFatherAddress", "wifeFatherComment", "false", "71y");
        record.setWifeMother("wifeMotherFirstname", "wifeMotherLastname", "wifeMotherOccupation", "wifeMotherResidence", "wifeMotherAddress", "wifeMotherComment", "false", "73y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }

    public static RecordDeath getRecordDeath() {
        RecordDeath record = new RecordDeath();
        record.setFieldValue(FieldType.eventDate, "11/11/2000");
        record.setFieldValue(FieldType.cote, "cote");
        record.setFieldValue(FieldType.generalComment, "generalcomment");
        record.setFieldValue(FieldType.freeComment,  "photo");
        record.setIndi("indiFirstname", "indiLastname", "M", "30y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.setIndiMarried("indiMarriedFirstame", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "false");
        record.setIndiFather("indiFatherFirstname", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "false", "70y");
        record.setIndiMother("indiMotherFirstname", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "false", "72y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }

    public static RecordMisc getRecordMisc() {
        RecordMisc record = new RecordMisc();
        record.setFieldValue(FieldType.eventDate, "29/02/2012");
        record.setFieldValue(FieldType.secondDate, "04/04/2012");
        record.setFieldValue(FieldType.cote, "cote");
        record.setFieldValue(FieldType.parish, "parish");
        record.setFieldValue(FieldType.notary, "Notary");
        record.setFieldValue(Record.FieldType.eventType, "eventname");
        record.setFieldValue(FieldType.generalComment, "generalcomment");
        record.setFieldValue(FieldType.freeComment,  "photo");
        record.setIndi("indiFirstname", "indiLastname", "M", "20y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.setIndiMarried("indiMarriedFirstname", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "true");
        record.setIndiFather("indiFatherFirstname", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "true", "70y");
        record.setIndiMother("indiMotherFirstname", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "true", "72y");
        record.setWife("wifeFirstname", "wifeLastname", "F", "wifeAge", "02/02/1992", "wifeBirthPlace", "wifeBirthAddress", "wifeOccupation", "wifeResidence", "wifeAddress", "wifeComment");
        record.setWifeMarried("wifeMarriedFirstname", "wifeMarriedLastname", "wifeMarriedOccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifeMarriedComment", "true");
        record.setWifeFather("wifeFatherFirstname", "wifeFatherLastname", "wifeFatherOccupation", "wifeFatherResidence", "wifeFatherAddress", "wifeFatherComment", "false", "71y");
        record.setWifeMother("wifeMotherFirstname", "wifeMotherLastname", "wifeMotherOccupation", "wifeMotherResidence", "wifeMotherAddress", "wifeMotherComment", "false", "73y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }

    public static Record getRecordBirthF2() {
        RecordBirth record = new RecordBirth();
                record.setFieldValue(Record.FieldType.eventDate, "01/04/1980");
                record.setFieldValue(Record.FieldType.cote, "cote");
                record.setFieldValue(Record.FieldType.freeComment,  "photo");
                record.setIndi("Mercure", "PLANET", "M", "", "", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
                record.setIndiFather("Jupiter", "PLANET", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "", "70y");
                record.setIndiMother("Venus", "GALAXY", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "true", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(Record.FieldType.generalComment, "generalcomment");
        return record;
    }

    public static RecordMarriage getRecordMarriageF2() {
        RecordMarriage record = new RecordMarriage();
        record.setFieldValue(FieldType.eventDate, "05/02/1979");
        record.setFieldValue(FieldType.cote, "cote");
        record.setFieldValue(FieldType.freeComment,  "photo");
        record.setFieldValue(FieldType.generalComment, "general comment");
        record.setIndi("Jupiter", "PLANET", "M", "30y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.setIndiMarried("indiMarriedFirstname", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "DEAD");
        record.setIndiFather("indiFatherFirstname", "PLANET", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "false", "70y");
        record.setIndiMother("indiMotherFirstname", "ASTRE", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "false", "72y");
        record.setWife("Venus", "GALAXY", "19y", "wifeAge", "02/02/1992", "wifeBirthPlace", "wifeBirthAddress", "wifeOccupation", "wifeResidence", "wifeAddress", "wifeComment");
        record.setWifeMarried("wifeMarriedFirstname", "wifeMarriedLastname", "wifeMarriedOccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifeMarriedComment", "ALIVE");
        record.setWifeFather("wifeFatherFirstname", "GALAXY", "wifeFatherOccupation", "wifeFatherResidence", "wifeFatherAddress", "wifeFatherComment", "false", "71y");
        record.setWifeMother("wifeMotherFirstname", "COMET", "wifeMotherOccupation", "wifeMotherResidence", "wifeMotherAddress", "wifeMotherComment", "false", "73y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }

    public static RecordMisc getRecordMiscOtherF2() {
        RecordMisc record = new RecordMisc();
        record.setFieldValue(FieldType.eventDate, "05/02/1979");
        record.setFieldValue(FieldType.cote, "cote");
        record.setFieldValue(Record.FieldType.eventType, "eventname");
        record.setFieldValue(FieldType.freeComment,  "photo");
        record.setFieldValue(FieldType.generalComment, "general comment");
        record.setIndi("Jupiter", "PLANET", "M", "30y", "01/02/1990", "indiBirthPlace", "indiBirthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
        record.setIndiMarried("indiMarriedFirstname", "indiMarriedLastname", "indiMarriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "indiMarriedComment", "DEAD");
        record.setIndiFather("indiFatherFirstname", "PLANET", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "false", "70y");
        record.setIndiMother("indiMotherFirstname", "ASTRE", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "false", "72y");
        record.setWife("Venus", "GALAXY", "19y", "wifeAge", "02/02/1992", "wifeBirthPlace", "wifeBirthAddress", "wifeOccupation", "wifeResidence", "wifeAddress", "wifeComment");
        record.setWifeMarried("wifeMarriedFirstname", "wifeMarriedLastname", "wifeMarriedOccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifeMarriedComment", "ALIVE");
        record.setWifeFather("wifeFatherFirstname", "GALAXY", "wifeFatherOccupation", "wifeFatherResidence", "wifeFatherAddress", "wifeFatherComment", "false", "71y");
        record.setWifeMother("wifeMotherFirstname", "COMET", "wifeMotherOccupation", "wifeMotherResidence", "wifeMotherAddress", "wifeMotherComment", "false", "73y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }

    public static MergeRecord createMergeRecord(Record record ) throws Exception {
        RecordInfoPlace infoPlace = TestUtility.getRecordsInfoPlace();
        String fileName = "";
        TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, infoPlace, fileName, record);
        return new MergeRecord(data);
    }

    protected static Component getComponentByName(Component parent, String name) {

        if (parent.getName() != null && parent.getName().equals(name)) {
            return parent;
        }

        if (parent instanceof Container) {
            Component[] components = ((Container) parent).getComponents();
            for (Component component : components) {
                Component child = getComponentByName(component, name);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    public static void showMergeDialog(TransferableRecord.TransferableData data, Gedcom gedcom, Entity selectedEntity ) {
        MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, selectedEntity, data, true);
        TestUtility.waitForDialogClose(dialog);
    }

    public static void showMergeDialog(RecordInfoPlace recordsInfoPlace, String fileName, Record record, Gedcom gedcom, Entity selectedEntity ) {
        TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, recordsInfoPlace, fileName, record);
        MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, selectedEntity, data, true);
        TestUtility.waitForDialogClose(dialog);
    }

    public static MergeManager createMergeManager(TransferableRecord.TransferableData data, Gedcom gedcom, Entity selectedEntity ) throws Exception {
        MergeManager mergeManager = new MergeManager(data, gedcom, selectedEntity);
        mergeManager.createProposals();
        return mergeManager;
    }

    public static MergeManager createMergeManager(RecordInfoPlace recordsInfoPlace, String fileName, Record record, Gedcom gedcom, Entity selectedEntity ) throws Exception {
        TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, recordsInfoPlace, fileName, record);
        MergeManager mergeManager = new MergeManager(data, gedcom, selectedEntity);
        mergeManager.createProposals();
        return mergeManager;
    }

    public static ProposalHelper createProposalHelper(Record record, Gedcom gedcom ) throws Exception {
        RecordInfoPlace infoPlace = TestUtility.getRecordsInfoPlace();
        String fileName = "";
        TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, infoPlace, fileName, record);
        MergeRecord mergeRecord = new MergeRecord(data);
        return new ProposalHelper(mergeRecord, null, gedcom);
    }



    public static final Object lock = new Object();
    public static void waitForDialogClose(final java.awt.Window dialog) {
        int visible = 1;
        do {
            if (dialog.isVisible()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                break;
            }
        } while ( visible == 1);
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
