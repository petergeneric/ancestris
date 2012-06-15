package ancestris.modules.releve.dnd;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.text.MessageFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author Michel
 */
public class MergeModelMarriage extends MergeModel {

    private Fam currentFamily;
    private MergeRecord record;
    private Gedcom gedcom;

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    MergeModelMarriage(MergeRecord record, Gedcom gedcom) throws Exception {
        this.record = record;
        this.currentFamily = null;
        this.gedcom = gedcom;
        addRowFamily();
        addRowHusband(null);
        //TODO ajouter l'ex conjoint de l'epoux
        addRowHusbandFamily(null);
        addRowSeparator();
        addRowWife(null);
        //TODO ajouter l'ex conjoint de l'epouse
        addRowWifeFamily(null);
    }

    MergeModelMarriage(MergeRecord record, Gedcom gedcom, Fam selectedFamily) throws Exception {
        this.record = record;
        this.currentFamily = selectedFamily;
        this.gedcom = gedcom;
        addRowFamily();
        if ( selectedFamily != null ) {
            addRowHusband(selectedFamily.getHusband());
            if (selectedFamily.getHusband() != null) {
                addRowHusbandFamily(selectedFamily.getHusband().getFamilyWhereBiologicalChild());
            } else {
                addRowHusbandFamily(null);
            }
            addRowSeparator();
            addRowWife(selectedFamily.getWife());
            if (selectedFamily.getWife() != null) {
                addRowWifeFamily(selectedFamily.getWife().getFamilyWhereBiologicalChild());
            } else {
                addRowWifeFamily(null);
            }
        } else {
            addRowHusband(null);
            addRowHusbandFamily(null);
            addRowSeparator();
            addRowWife(null);
            addRowWifeFamily(null);
        }
    }

    MergeModelMarriage(MergeRecord record, Gedcom gedcom, Indi husband, Indi wife) throws Exception {
        this.record = record;
        this.currentFamily = null;
        this.gedcom = gedcom;
        addRowFamily();
        addRowHusband(husband);
        if ( husband!= null ) {
            addRowHusbandFamily(husband.getFamilyWhereBiologicalChild());
        } else {
            addRowHusbandFamily(null);
        }
        
        addRowSeparator();
        addRowWife(wife);
        if ( wife!= null) {
            addRowWifeFamily(wife.getFamilyWhereBiologicalChild());
        } else {
            addRowWifeFamily(null);
        }
    }

    MergeModelMarriage(MergeRecord record, Gedcom gedcom, Fam husbandParentFamily, Fam wifeParentFamily) throws Exception {
        this.record = record;
        this.currentFamily = null;
        this.gedcom = gedcom;
        addRowFamily();
        addRowHusband(null);
        addRowHusbandFamily(husbandParentFamily);
        addRowSeparator();
        addRowWife(null);
        addRowWifeFamily(wifeParentFamily);
    }

    private void addRowFamily() {
        if (currentFamily != null) {
            // je recupere le mariage existant
            Property marriageProperty = currentFamily.getProperty("MARR");

            // j'affiche la source du mariage
            Property sourceProperty = MergeQuery.findSource(record, gedcom, marriageProperty);
            addRow(RowType.EventSource, record.getEventSource(), MergeQuery.findSourceTitle(sourceProperty, gedcom), MergeQuery.findSource(record, gedcom));
            addRow(RowType.EventPage, record.getEventPage(),  MergeQuery.findSourcePage(record, sourceProperty, gedcom), null);

            // j'affiche un separateur
            addRowSeparator();

            // j'affiche l'identifiant de la famille
            addRow(RowType.MarriageFamily, record, currentFamily);
            // j'affiche la date de l'acte de mariage
            addRow(RowType.EventDate, record.getEventDate(), currentFamily.getMarriageDate());
            // j'affiche le lieu de l'acte de mariage
            addRow(RowType.EventPlace, record.getEventPlace(), marriageProperty!= null ? marriageProperty.getValue(new TagPath("MARR:PLAC"), "") : "");
            // j'affiche le commentaire
            addRow(RowType.EventComment, record.getEventComment(), marriageProperty!= null ? marriageProperty.getValue(new TagPath("MARR:NOTE"), "") : "");
            // j'affiche un separateur
            addRowSeparator();
        } else {
            // selectedFamily est nul
            
            // j'affiche la source 
            addRow(RowType.EventSource, record.getEventSource(),"",  MergeQuery.findSource(record, gedcom));
            addRow(RowType.EventPage,   record.getEventPage(), "", null);
            // j'affiche un separateur
            addRowSeparator();

            // j'affiche l'identifiant de la famille
            addRow(RowType.MarriageFamily, record, null);
            // j'affiche la date du mariage
            addRow(RowType.EventDate, record.getEventDate(), null);
            // j'affiche le lieu de l'acte de mariage
            addRow(RowType.EventPlace, record.getEventPlace(), "");
            // j'affiche le commentaire
            addRow(RowType.EventComment, record.getEventComment(), "");
            // j'affiche un separateur
            addRowSeparator();
        }
    }

    /**
     * affiche les informations de l'epoux et de ses parents
     * @param wife
     */
    private void addRowHusband(Indi husband) throws Exception {
        if (husband != null) {
            // j'affiche les informations de l'epoux
            addRow(RowType.IndiLastName,   record.getIndiLastName(),  husband.getLastName(), husband);
            addRow(RowType.IndiFirstName,  record.getIndiFirstName(), husband.getFirstName());
            addRow(RowType.IndiBirthDate,  record.getIndiBirthDate(), husband.getBirthDate(false));
            addRow(RowType.IndiPlace,      record.getIndiPlace(),     husband.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.IndiOccupation, record.getIndiOccupationWithDate(),  MergeQuery.findOccupation(husband, record.getEventDate()));
        } else {
            // j'affiche les informations de l'epoux
            addRow(RowType.IndiLastName,   record.getIndiLastName(),  "", null);
            addRow(RowType.IndiFirstName,  record.getIndiFirstName(), "");
            addRow(RowType.IndiBirthDate,  record.getIndiBirthDate(), null);
            addRow(RowType.IndiPlace,      record.getIndiPlace(),     "");
            addRow(RowType.IndiOccupation, record.getIndiOccupationWithDate(), "");
        }

    }

    /**
     * affiche les informations de l'epoux 
     * @param wife
     */
    private void addRowHusbandFamily(Fam husbandFamily) throws Exception {
        if (husbandFamily != null) {
            // j'affiche la famille de l'epoux
            addRow(RowType.IndiParentFamily, record, husbandFamily);
            // j'affiche une estimation de la date de mariage des parents a partir du relevé
            addRow(RowType.IndiParentMarriageDate, record.getIndiParentMarriageDate(),  husbandFamily != null ? husbandFamily.getMarriageDate() : null );
            addRowHusbandFather( husbandFamily.getHusband());
            addRowHusbandMother( husbandFamily.getWife());
        } else {
            addRow(RowType.IndiParentFamily, record, null);
            // je recherche une estimation de la date de mariage des parents a partir du relevé
            addRow(RowType.IndiParentMarriageDate, record.getIndiParentMarriageDate(), null);
            addRowHusbandFather( null);
            addRowHusbandMother( null);
        }
    }


    /**
     * affiche les informations de l'epouse
     * @param wife
     */
    private void addRowWife(Indi wife) throws Exception {
        if (wife != null) {
            // j'affiche les informations de l'epouse
            addRow(RowType.WifeLastName,   record.getWifeLastName(), wife.getLastName(), wife);
            addRow(RowType.WifeFirstName,  record.getWifeFirstName(), wife.getFirstName());
            addRow(RowType.WifeBirthDate,  record.getWifeBirthDate(), wife.getBirthDate(false));
            addRow(RowType.WifePlace,      record.getWifePlace(),      wife.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.WifeOccupation, record.getWifeOccupationWithDate(), MergeQuery.findOccupation(wife, record.getEventDate()));
        } else {
            addRow(RowType.WifeLastName,   record.getWifeLastName(), "", null);
            addRow(RowType.WifeFirstName,  record.getWifeFirstName(), "");
            addRow(RowType.WifeBirthDate,  record.getWifeBirthDate(), null);
            addRow(RowType.WifePlace,      record.getWifePlace(), "");
            addRow(RowType.WifeOccupation, record.getWifeOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations des parents de l'epouse
     * @param wife
     */
    private void addRowWifeFamily(Fam wifeFamily) throws Exception {
        if (wifeFamily != null) {
            addRow(RowType.WifeParentFamily, record, wifeFamily);
            // j'affiche une estimation de la date de mariage des parents a partir du relevé
            addRow(RowType.WifeParentMarriageDate, record.getWifeParentMarriageDate(), wifeFamily != null ? wifeFamily.getMarriageDate() : null);
            addRowWifeFather( wifeFamily.getHusband());
            addRowWifeMother( wifeFamily.getWife());
        } else {
            addRow(RowType.WifeParentFamily, record, null);
            // j'affiche une estimation de la date de mariage des parents a partir du relevé
            addRow(RowType.WifeParentMarriageDate, record.getWifeParentMarriageDate(), null);
            addRowWifeFather( null);
            addRowWifeMother( null);
        }
    }

    /**
     * affiche les informations du pere de l'epoux
     * @param wife
     */
    private void addRowHusbandFather(Indi husbandFather) throws Exception {
        if (husbandFather != null) {
            addRow(RowType.IndiFatherLastName,  record.getIndiFatherLastName(), husbandFather.getLastName(), husbandFather);
            addRow(RowType.IndiFatherFirstName, record.getIndiFatherFirstName(), husbandFather.getFirstName());
            addRow(RowType.IndiFatherBirthDate, record.getIndiFatherBirthDate(), husbandFather.getBirthDate());
            addRow(RowType.IndiFatherDeathDate, record.getIndiFatherDeathDate(), husbandFather.getDeathDate());
            addRow(RowType.IndiFatherOccupation, record.getIndiFatherOccupationWithDate(),  MergeQuery.findOccupation(husbandFather, record.getEventDate()));
        } else {
            addRow(RowType.IndiFatherLastName,  record.getIndiFatherLastName(), "", null);
            addRow(RowType.IndiFatherFirstName, record.getIndiFatherFirstName(), "");
            addRow(RowType.IndiFatherBirthDate, record.getIndiFatherBirthDate(), null);
            addRow(RowType.IndiFatherDeathDate, record.getIndiFatherDeathDate(), null);
            addRow(RowType.IndiFatherOccupation, record.getIndiFatherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations de la mere de l'epoux
     * @param wife
     */
    private void addRowHusbandMother(Indi husbandMother) throws Exception {
        if (husbandMother != null) {
            addRow(RowType.IndiMotherLastName, record.getIndiMotherLastName(), husbandMother.getLastName(), husbandMother);
            addRow(RowType.IndiMotherFirstName, record.getIndiMotherFirstName(), husbandMother.getFirstName());
            addRow(RowType.IndiMotherBirthDate, record.getIndiMotherBirthDate(), husbandMother.getBirthDate(false));
            addRow(RowType.IndiMotherDeathDate, record.getIndiMotherDeathDate(), husbandMother.getDeathDate(false));
            addRow(RowType.IndiMotherOccupation, record.getIndiMotherOccupationWithDate(),  MergeQuery.findOccupation(husbandMother, record.getEventDate()));
        } else {
            addRow(RowType.IndiMotherLastName, record.getIndiMotherLastName(), "", null);
            addRow(RowType.IndiMotherFirstName, record.getIndiMotherFirstName(), "");
            addRow(RowType.IndiMotherBirthDate, record.getIndiMotherBirthDate(), null);
            addRow(RowType.IndiMotherDeathDate, record.getIndiMotherDeathDate(), null);
            addRow(RowType.IndiMotherOccupation, record.getIndiMotherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations du pere de l'epouse
     * @param wife
     */
    private void addRowWifeFather(Indi wifeFather) throws Exception {
        if (wifeFather != null) {
            addRow(RowType.WifeFatherLastName,  record.getWifeFatherLastName(), wifeFather.getLastName(), wifeFather);
            addRow(RowType.WifeFatherFirstName, record.getWifeFatherFirstName(), wifeFather.getFirstName());
            addRow(RowType.WifeFatherBirthDate, record.getWifeFatherBirthDate(), wifeFather.getBirthDate());
            addRow(RowType.WifeFatherDeathDate, record.getWifeFatherDeathDate(), wifeFather.getDeathDate());
            addRow(RowType.WifeFatherOccupation, record.getWifeFatherOccupationWithDate(), MergeQuery.findOccupation(wifeFather, record.getEventDate()));
        } else {
            addRow(RowType.WifeFatherLastName,  record.getWifeFatherLastName(), "", null);
            addRow(RowType.WifeFatherFirstName, record.getWifeFatherFirstName(), "");
            addRow(RowType.WifeFatherBirthDate, record.getWifeFatherBirthDate(), null);
            addRow(RowType.WifeFatherDeathDate, record.getWifeFatherDeathDate(), null);
            addRow(RowType.WifeFatherOccupation, record.getWifeFatherOccupationWithDate(), "");
        }
    }

     /**
     * affiche les informations de la mere de l'epouses
     * @param wife
     */
    private void addRowWifeMother(Indi wifeMother) throws Exception {
        if (wifeMother != null) {
            addRow(RowType.WifeMotherLastName,  record.getWifeMotherLastName(), wifeMother.getLastName(), wifeMother);
            addRow(RowType.WifeMotherFirstName, record.getWifeMotherFirstName(), wifeMother.getFirstName());
            addRow(RowType.WifeMotherBirthDate, record.getWifeMotherBirthDate(), wifeMother.getBirthDate(false));
            addRow(RowType.WifeMotherDeathDate, record.getWifeMotherDeathDate(), wifeMother.getDeathDate(false));
            addRow(RowType.WifeMotherOccupation, record.getWifeMotherOccupationWithDate(), MergeQuery.findOccupation(wifeMother, record.getEventDate()));
        } else {
            addRow(RowType.WifeMotherLastName,  record.getWifeMotherLastName(), "", null);
            addRow(RowType.WifeMotherFirstName, record.getWifeMotherFirstName(), "");
            addRow(RowType.WifeMotherBirthDate, record.getWifeMotherBirthDate(), null);
            addRow(RowType.WifeMotherDeathDate, record.getWifeMotherDeathDate(), null);
            addRow(RowType.WifeMotherOccupation, record.getWifeMotherOccupationWithDate(), "");
        }
    }

    /**
     * copie les données du relevé dans l'entité
     */
    @Override
    protected void copyRecordToEntity() throws Exception {

        //PropertyDate eventDate = (PropertyDate) getRow(RowType.EventDate).recordValue;

        Indi husband = (Indi) getRow(RowType.IndiLastName).entityObject;
        if (husband == null) {
            // je cree l'individu
            husband = (Indi) gedcom.createEntity(Gedcom.INDI);
            husband.setName(record.getIndiFirstName(), record.getIndiLastName());
            husband.setSex(PropertySex.MALE);
        } else {
            // je copie le nom de l'epoux
            if (isChecked(RowType.IndiLastName)) {
                husband.setName(husband.getFirstName(), record.getIndiLastName());
            }

            // je copie le prénom de l'epoux
            if (isChecked(RowType.IndiFirstName)) {
                husband.setName(record.getIndiFirstName(), husband.getLastName());
            }
        }

        // je copie la date, le lieu et commentaire de naissance de l'epoux
        if (isChecked(RowType.IndiBirthDate)) {
            copyBirthDate(husband, record.getIndiBirthDate(), record.getIndiPlace(), record);
        }

        // je copie la profession de l'epoux
        if (isChecked(RowType.IndiOccupation) && !record.getIndiOccupation().isEmpty()) {
            copyOccupation(husband, record.getIndiOccupation(), record);
        }

        // je copie les données des parents de l'epoux
        if (isChecked(RowType.IndiParentFamily)) {
            // je copie la famille des parents
            Fam parentfamily = (Fam) getRow(RowType.IndiParentFamily).entityObject;
            if (parentfamily == null) {
                // je cree la famille
                parentfamily = (Fam) gedcom.createEntity(Gedcom.FAM);
            }

            // j'ajoute l'enfant dans la famille si ce n'est pas déja le cas
            if (!husband.isDescendantOf(parentfamily)) {
                parentfamily.addChild(husband);
            }

            // je copie la date du mariage des parents et une note indiquant l'origine de cette date
            if (isChecked(RowType.IndiParentMarriageDate)) {
                copyMarriageDate(parentfamily, record.getIndiParentMarriageDate(), record );
            }
            

            // je copie le nom et le prenom du pere de l'epoux
            Indi father = (Indi) getRow(RowType.IndiFatherLastName).entityObject;
            if (father == null) {
                // je cree le pere
                father = (Indi) gedcom.createEntity(Gedcom.INDI);
                father.setName(record.getIndiFatherFirstName(), record.getIndiFatherLastName());
                father.setSex(PropertySex.MALE);
                parentfamily.setHusband(father);
            } else {
                if (isChecked(RowType.IndiFatherFirstName)) {
                    father.setName(record.getIndiFatherFirstName(), father.getLastName());
                }
                if (isChecked(RowType.IndiFatherLastName)) {
                    father.setName(father.getFirstName(), record.getIndiFatherLastName());
                }
            }

            // je copie la date de naissance du pere de l'epoux
            if (isChecked(RowType.IndiFatherBirthDate)) {
                copyBirthDate(father, record.getIndiFatherBirthDate(), record.getEventPlace(), record);
            }

            //je copie la date de décès du pere de l'epoux
            if (isChecked(RowType.IndiFatherDeathDate)) {
                copyDeathDate(father, record.getIndiFatherDeathDate(), record.getEventPlace(), record);
            }

            // je copie la profession du pere
            if (isChecked(RowType.IndiFatherOccupation)) {
                copyOccupation(father, record.getIndiFatherOccupation(), record);
            }

            // je copie le nom et le prenom de la mere de l'epoux
            Indi mother = (Indi) getRow(RowType.IndiMotherLastName).entityObject;
            if (mother == null) {
                // je cree le pere
                mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                mother.setName(record.getIndiMotherFirstName(), record.getIndiMotherLastName());
                mother.setSex(PropertySex.FEMALE);
                parentfamily.setWife(mother);
            } else {
                if (isChecked(RowType.IndiMotherFirstName)) {
                    mother.setName(record.getIndiMotherFirstName(), mother.getLastName());
                }
                if (isChecked(RowType.IndiMotherLastName)) {
                    mother.setName(mother.getFirstName(), record.getIndiMotherLastName());
                }
            }

            // je copie la date de naissance de la mere de l'epoux
            if (isChecked(RowType.IndiMotherBirthDate)) {
                copyBirthDate(mother, record.getIndiMotherBirthDate(), record.getEventPlace(), record);
            }

            // je copie la date de décès de la mere de l'epoux
            if (isChecked(RowType.IndiMotherDeathDate)) {
                copyDeathDate(mother, record.getIndiMotherDeathDate(), record.getEventPlace(), record);
            }

            // je copie la profession de la mere de l'epoux
            if (isChecked(RowType.IndiMotherOccupation) ) {
                copyOccupation(mother, record.getIndiMotherOccupation(), record);
            }

        } // parents de l'epoux


        ///////////////////////////////////////////////////////////
        // wife
        ///////////////////////////////////////////////////////////
        Indi wife = (Indi) getRow(RowType.WifeLastName).entityObject;
        if (wife == null) {
            // je cree l'indivis
            wife = (Indi) gedcom.createEntity(Gedcom.INDI);
            wife.setName(record.getWifeFirstName(), record.getWifeLastName());
            wife.setSex(PropertySex.FEMALE);
        } else {
            // je copie le nom de l'epouse
            if (isChecked(RowType.WifeLastName)) {
                wife.setName(wife.getFirstName(), record.getWifeLastName());
            }

            // je copie le prénom de l'epouse
            if (isChecked(RowType.WifeFirstName)) {
                wife.setName(record.getWifeFirstName(), wife.getLastName());
            }
        }

        // je copie la date, le lieu et le commentaire de naissance de l'epouse
        if (isChecked(RowType.WifeBirthDate)) {
            copyBirthDate(wife, record.getWifeBirthDate(), record.getWifePlace(), record);
        }

        // je copie la profession de l'epouse
        if (isChecked(RowType.WifeOccupation) && !record.getWifeOccupation().isEmpty()) {
            copyOccupation(wife, record.getWifeOccupation(), record);
        }


        // je copie les données des parents de l'epouse
        if (isChecked(RowType.WifeParentFamily)) {
            // je copie la famille des parents
            Fam parentfamily = (Fam) getRow(RowType.WifeParentFamily).entityObject;
            if (parentfamily == null) {
                // je cree la famille
                parentfamily = (Fam) gedcom.createEntity(Gedcom.FAM);
            }

            // j'ajoute l'enfant dans la famille si ce n'est pas déja le cas
            if (!wife.isDescendantOf(parentfamily)) {
                parentfamily.addChild(wife);
            }

            // je copie la date du mariage des parents et une note indiquant l'origine de cette date
            if (isChecked(RowType.WifeParentMarriageDate)) {
                copyMarriageDate(parentfamily, record.getWifeParentMarriageDate(), record );
            }


            // je copie le nom et le prenom du pere de l'epouse
            Indi father = (Indi) getRow(RowType.WifeFatherLastName).entityObject;
            if (father == null) {
                // je cree le pere
                father = (Indi) gedcom.createEntity(Gedcom.INDI);
                father.setName(record.getWifeFatherFirstName(), record.getWifeFatherLastName());
                father.setSex(PropertySex.MALE);
                parentfamily.setHusband(father);
            } else {
                if (isChecked(RowType.WifeFatherFirstName)) {
                    father.setName(record.getWifeFatherFirstName(), father.getLastName());
                }
                if (isChecked(RowType.WifeFatherLastName)) {
                    father.setName(father.getFirstName(), record.getWifeFatherLastName());
                }
            }

            // je copie la date de naissance du pere de l'epouse
            if (isChecked(RowType.WifeFatherBirthDate)) {
                father.getBirthDate(true).setValue(record.getWifeFatherBirthDate().getValue());
                copyBirthDate(father, record.getWifeMotherBirthDate(), record.getEventPlace(), record);
            }

            //je copie la date de décès du pere de l'epouse
            if (isChecked(RowType.WifeFatherDeathDate)) {
                copyDeathDate(father, record.getWifeFatherDeathDate(), record.getEventPlace(), record);
            }

            // je copie la profession du pere de l'epouse
            if (isChecked(RowType.WifeFatherOccupation) ) {
                copyOccupation(father, record.getWifeFatherOccupation(), record);
            }

            // je copie le nom et le prenom de la mere de l'epouse
            Indi mother = (Indi) getRow(RowType.WifeMotherLastName).entityObject;
            if (mother == null) {
                // je cree le pere
                mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                mother.setName(record.getWifeMotherFirstName(), record.getWifeMotherLastName());
                mother.setSex(PropertySex.FEMALE);
                parentfamily.setWife(mother);
            } else {
                if (isChecked(RowType.WifeMotherFirstName)) {
                    mother.setName(record.getWifeMotherFirstName(), mother.getLastName());
                }
                if (isChecked(RowType.WifeMotherLastName)) {
                    mother.setName(mother.getFirstName(), record.getWifeMotherLastName());
                }
            }

            // je copie la date de naissance de la mere e l'epouse
            if (isChecked(RowType.WifeMotherBirthDate)) {
                copyBirthDate(mother, record.getWifeMotherBirthDate(), record.getEventPlace(), record);
            }

            // je copie la date de décès de la mere e l'epouse
            if (isChecked(RowType.WifeMotherDeathDate)) {
                copyDeathDate(mother, record.getWifeMotherDeathDate(), record.getEventPlace(), record);
            }

            // je copie la profession de la mere de l'epouse
            if (isChecked(RowType.WifeMotherOccupation) ) {
                copyOccupation(mother, record.getWifeMotherOccupation(), record);
            }

        } // parents de l'epouse



        ///////////////////////////////////////////////////////////
        // family & marriage
        ///////////////////////////////////////////////////////////

        //je cree la famille si necessaire
        if (currentFamily == null) {
            currentFamily = (Fam) gedcom.createEntity(Gedcom.FAM);
            // je lie le mari a la famille
            currentFamily.setHusband(husband);
            currentFamily.setWife(wife);
        }

        // je crée la propriété MARR
        Property marriageProperty = currentFamily.getProperty("MARR");
        if (marriageProperty == null) {
            marriageProperty = currentFamily.addProperty("MARR", "");
        }

        // je copie la source du releve de mariage 
        if (isChecked(RowType.EventSource)) {
            copySource((Source) getRow(RowType.EventSource).entityObject, marriageProperty, record);
        }

        // je copie la date de mariage
        if (isChecked(RowType.EventDate)) {
            // j'ajoute (ou remplace ) la date de la naissance
            PropertyDate propertyDate = (PropertyDate) marriageProperty.getProperty("DATE");
            if (propertyDate == null) {
                propertyDate = (PropertyDate) marriageProperty.addProperty("DATE", "");
            }
            propertyDate.setValue(record.getEventDate().getValue());
        }

        // je copie le lieu du mariage
        if (isChecked(RowType.EventPlace)) {
            Property propertyPlace = marriageProperty.getProperty("PLAC");
            if (propertyPlace == null) {
                // je cree le lieu .
                propertyPlace = marriageProperty.addProperty("PLAC", "");
            }
            propertyPlace.setValue(record.getEventPlace());
        }

        // je copie le commentaire du mariage.
        if (isChecked(RowType.EventComment)) {
            Property propertyNote = marriageProperty.getProperty("NOTE");
            if (propertyNote == null) {
                // je cree une note .
                propertyNote = marriageProperty.addProperty("NOTE", "");
            }

            // j'ajoute le commentaire du mariageau debut de la note existante.
            String value = propertyNote.getValue();
            String comment = record.getEventComment();
            if (!comment.isEmpty()) {
                if (!value.isEmpty()) {
                    comment += "\n";
                }
                comment += value;
                propertyNote.setValue(comment);
            }
        }

    }

    
    /**
     * retourne la famille selectionnée
     *
     * @return famille selectionnée ou null si c'est une nouvelle famille
     */@Override
    protected Entity getSelectedEntity() {
        return currentFamily;
    }

    /**
     * retoune le gedcom du modele
     * @return gedcom
     */
    @Override
    protected Gedcom getGedcom() {
        return gedcom;
    }

    /**
     * retourne les noms des epoux pour constituer le titre de la fenetre principale
     * @return
     */
    @Override
    protected String getTitle() {
        String husband = record.getIndiFirstName() + " "+ record.getIndiLastName();
        String wife = record.getWifeFirstName() + " "+ record.getWifeLastName()+ " " + record.getEventDate().getDisplayValue();
        return MessageFormat.format(NbBundle.getMessage(MergeDialog.class, "MergeModel.title.marriage"), husband, wife);
    }

    /**
     * retourne un resumé du modele
     * Cette chaine sert de commentaire dans la liste des modeles
     * @return
     */
    @Override
    protected String getSummary(Entity selectedEntity) {

        String summary;

        if ( currentFamily == null ) {
            String husband;
            String wife;
            String indiParents;
            String wifeParents;

            // j'affiche l'epoux
            if (getRow(RowType.IndiLastName).entityObject == null ) {
                husband = "Nouvel époux";
            } else {
                husband = getRow(RowType.IndiLastName).entityObject.toString(true);
            }
            if (getRow(RowType.WifeLastName).entityObject == null ) {
                wife = "Nouvelle épouse";
            } else {
                wife = getRow(RowType.WifeLastName).entityObject.toString(true);
            }
            // j'afficje les parents
            if (getRow(RowType.IndiParentFamily).entityObject == null ) {
                if ( getRow(RowType.IndiFatherLastName).entityObject == null ) {
                    indiParents = "Nouveau père";
                } else {
                    indiParents = ((Indi)getRow(RowType.IndiFatherLastName).entityObject).getDisplayValue();
                }
                indiParents += "+";
                if ( getRow(RowType.IndiFatherLastName).entityObject == null ) {
                    indiParents += "Nouvelle mère";
                } else {
                    indiParents += ((Indi)getRow(RowType.IndiMotherLastName).entityObject).getDisplayValue();
                }
            }  else {
                // j'affiche la famille des parents de l'epoux
                indiParents = ((Fam)getRow(RowType.IndiParentFamily).entityObject).toString(false);
            }

            
            if (getRow(RowType.WifeParentFamily).entityObject == null ) {
                if ( getRow(RowType.WifeFatherLastName).entityObject == null ) {
                    wifeParents = "Nouveau père";
                } else {
                    wifeParents = ((Indi)getRow(RowType.WifeFatherLastName).entityObject).getDisplayValue();
                }
                wifeParents += "+";
                if ( getRow(RowType.WifeFatherLastName).entityObject == null ) {
                    wifeParents += "Nouvelle mère";
                } else {
                    wifeParents += ((Indi)getRow(RowType.WifeMotherLastName).entityObject).getDisplayValue();
                }
            }  else {
                // j'affiche la famille des parents de l'epoux
                wifeParents = ((Fam)getRow(RowType.WifeParentFamily).entityObject).toString(false);
            }

            //summary = MessageFormat.format(summaryFormat, spouses, indiParents, wifeParents);

            summary = husband + " + " + wife + "; ";
            summary += indiParents;
            summary += "; ";
            summary += wifeParents;

        } else {
            summary =  "Modifier le mariage" + " ";
            summary += currentFamily.getHusband().toString(false);
            summary += " ";
            summary += currentFamily.getWife().toString(false);
        }
        return summary;
    }

}
