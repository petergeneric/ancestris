package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.FieldPlace;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelMarriageTest extends TestCase {

    static public String getSourceTitle() {
        return "";
    }

    static public FieldPlace getRecordsInfoPlace() {
        FieldPlace recordsInfoPlace = new FieldPlace();
        recordsInfoPlace.setValue("Paris,75000,,state,country");
        return recordsInfoPlace;
    }

    static public FieldPlace getRecordsInfoPlaceVilleMariage() {
        FieldPlace recordsInfoPlace = new FieldPlace();
        recordsInfoPlace.setValue("ville_mariage,code_mariage,departement_mariage,region_mariage,pays_mariage");
        return recordsInfoPlace;
    }

    public static RecordMarriage createMarriageRecord(String id) {
        RecordMarriage record = new RecordMarriage();
        if ( id.equals("M1")) {
            record.setEventDate("01/03/1999");
            record.setCote("cote");
            record.setGeneralComment("generalcomment");
            record.setFreeComment("photo");
            record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "indiage", "01/01/1970", "indiBirthplace", "indioccupation22", "indiResidence", "indicomments");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "indimarrieddead");
            record.setIndiFather("indifathername", "FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "indifatherdead", "70y");
            record.setIndiMother("indimothername", "MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "indimotherdead", "72y");
            record.setWife("Motherfirstname", "WIFEFATHERLASTNAME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "wifemarrieddead");
            record.setWifeFather("wifefathername", "WIFEFATHERLASTNAME", "wifefatheroccupation", "wiferFatherResidence", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("wifemothername", "WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "wifemotherdead", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        }
        
       



        return record;        
    }

    /**
     * 
     */
    public void testAddMarriage() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlaceVilleMariage(), sourceTitle, createMarriageRecord("M1"));

            List<MergeModel> models;
            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F00004");
            assertEquals("Lien mariage vers source","@S00004@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S00004", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",511, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertNotSame("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance","",
                    fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent mariage date inchangé",mergeRecord.getIndi().getParentMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent mariage comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndi().getFatherLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndi().getFatherFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndi().getFatherOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndi().getMotherLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndi().getMotherFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndi().getMotherOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                    "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent mariage date",mergeRecord.getWife().getParentMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent mariage comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFatherLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFatherFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFatherOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMotherLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMotherFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMotherOccupation(),  wifeMother.getProperty("OCCU").getValue());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testSaveDataComment
     */
    public void testUpdateMariageAndFatherBirthDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlaceVilleMariage(), sourceTitle, createMarriageRecord("M1"));
            List<MergeModel> models;
            // je change la date de naissance du pere pour permettre la modification
            ((Indi)gedcom.getEntity("I1")).getBirthDate().setValue("BEF 1971");

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F00004");
            assertEquals("Lien mariage vers source","@S00004@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S00004", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",
                "Commentaire epoux: indicomments"+"\n"
                +"Commentaire père époux: indifathercomment, 70 années"+"\n"
                +"Commentaire mère époux: indimothercomment, 72 années"+"\n"
                +"Commentaire épouse: wifecomment"+"\n"
                +"Commentaire père épouse: wifefathercomment, 60 années"+"\n"
                +"Commentaire mère épouse: wifemothercomment, 62 années"+"\n"
                +"Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment"+"\n"
                +"generalcomment"+"\n"
                +"Photo: photo",
                fam.getValue(new TagPath("FAM:MARR:NOTE"),""));

            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance",
                "Date de naissance 1 jan 1970 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession",
                "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                occupation.getValue(new TagPath("OCCU:NOTE"),""));

            // épouse
            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                occupation.getValue(new TagPath("OCCU:NOTE"),""));


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * test l'ajout de l'epouse dans un mariage deja existant
     */
    public void testUpdateMarriageWithoutWife() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            // j'ajoute un mariage sans epouse
            Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I30");
            husband.setName("PerePrenomAA", "PERENOM_AA");
            husband.setSex(PropertySex.MALE);
            husband.addProperty("OCCU", "profession1");

            Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "I31");
            child1.setName("FilsPrenomBB", "PERENOM_AA");
            child1.setSex(PropertySex.FEMALE);
            child1.addProperty("OCCU", "profession1");

            Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F30");
            family.setHusband(husband);
            family.addChild(child1);

            // je cree le relevé contenant le nom de la mere qui n'existe pas dans le gedcom
            RecordMarriage record = new RecordMarriage();
            record.setEventDate("01/03/1999");
            record.setCote("cote");
            record.setGeneralComment("generalcomment");
            record.setFreeComment("photo");
            record.setIndi("PerePrenomAA", "PERENOM_AA", "M", "30y", "02/02/1970", "indiplace", "indioccupation", "indiResidence", "indicomments");
            record.setWife("MerePrenomBB", "MERENOM_BB", "F", "28y", "03/03/1973", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record.setWifeFather("PerePrenom", "PERENOM_BB", "wifefatheroccupation", "wiferFatherResidence", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("Mere prenom CC", "MERENOM_CC", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "wifemotherdead", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlaceVilleMariage(),sourceTitle, record);
            List<MergeModel> models;
            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);

            assertEquals("Nombre de propositions",2,models.size());
            // je verifie la premiere proposition
            String summary = models.get(0).getSummary(family);
            assertEquals("Porposition", "Modifier le mariage PERENOM_AA, PerePrenomAA x ---", summary);

            // j'applique la premiere proposition
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F30");
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",427, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertNotSame("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance",141, fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession",126, occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

            assertEquals("Wife : Nom",mergeRecord.getWife().getLastName(), fam.getWife().getLastName());
            assertEquals("Wife : Prénom",mergeRecord.getWife().getFirstName().toString(), fam.getWife().getFirstName().toString());
            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",141, fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",126, occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * modification de la date de deces du pere
     */
    public void testUpdateMariageAndFatherDeathDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            RecordMarriage mariageRecord = createMarriageRecord("M1");
            // je change la date de deces du pere et de la mere
            mariageRecord.setIndiFather("indifathername", "FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "true", "");
            mariageRecord.setIndiMother("indimothername", "MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "true", "");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlaceVilleMariage(), sourceTitle, mariageRecord);
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F00004");
            assertEquals("Lien mariage vers source","@S00004@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S00004", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());

            assertEquals("Mari mere date deces","BET 1970 AND 1999", mergeRecord.getIndi().getMotherDeathDate().getValue());

            Fam indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            Indi indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere date deces","BET 1969 AND 1999", indiFather.getDeathDate().getValue());
            Indi indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere date deces","BET 1970 AND 1999", indiMother.getDeathDate().getValue());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
