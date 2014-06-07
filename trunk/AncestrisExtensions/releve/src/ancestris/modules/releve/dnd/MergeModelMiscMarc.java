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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Michel
 */
public class MergeModelMiscMarc extends MergeModel {

    private Fam currentFamily;

    static public List<MergeModel> createMergeModelMiscMarc(MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity, boolean showNewParents) throws Exception {
        List<MergeModel> models = new ArrayList<MergeModel>();
        if ( selectedEntity instanceof Fam ) {
            // 4.1) Record Misc : l'entité selectionnée est une famille
            Fam selectedFamily = (Fam) selectedEntity;

            // j'ajoute un modele avec la famille selectionne
            models.add(new MergeModelMiscMarc(mergeRecord, gedcom, selectedFamily));

        } else if ( selectedEntity instanceof Indi ) {
            // 4.2) Record Misc : l'entité selectionnée est un individu
            Indi selectedIndi = (Indi) selectedEntity;

            // je cherche les familles avec l'individu selectionné
            Fam[] families = selectedIndi.getFamiliesWhereSpouse();
            // j'ajoute les familles compatibles
            for(Fam family : families) {
                models.add(new MergeModelMiscMarc(mergeRecord, gedcom, family));
            }

            if (showNewParents) {
                // j'ajoute les parents possibles non maries entre eux
                List<Indi> husbands = new ArrayList<Indi>();
                List<Indi> wifes = new ArrayList<Indi>();
                if (selectedIndi.getSex() == PropertySex.MALE) {
                    models.add(new MergeModelMiscMarc(mergeRecord, gedcom, selectedIndi, (Indi) null));
                    husbands.add(selectedIndi);
                } else if (selectedIndi.getSex() == PropertySex.FEMALE) {
                    models.add(new MergeModelMiscMarc(mergeRecord, gedcom, (Indi) null, selectedIndi));
                    wifes.add(selectedIndi);
                }
                MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(mergeRecord, gedcom, Arrays.asList(families), husbands, wifes);
                for (Indi husband : husbands) {
                    for (Indi wife : wifes) {
                        //TODO  rechercher la famille de l'epoux et la famille de l'epouse et la prendre en compte si elle existe
                        models.add(new MergeModelMiscMarc(mergeRecord, gedcom, husband, wife));
                    }
                }
            }
        } else {
            // 4.3) Record Misc : pas d'entité selectionnee

            // j'ajoute une nouvelle famille
            models.add(new MergeModelMiscMarc(mergeRecord, gedcom));

            // je recherche les familles compatibles
            List<Fam> families = MergeQuery.findFamilyCompatibleWithMarriageRecord(mergeRecord, gedcom, null);
            // j'ajoute les familles compatibles
            for(Fam family : families) {
                models.add(new MergeModelMiscMarc(mergeRecord, gedcom, family));
            }

            // je recherche les individus compatibles avec l'epoux et l'epouse du releve
            List<Indi> husbands = new ArrayList<Indi>();
            List<Indi> wifes = new ArrayList<Indi>();
            MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(mergeRecord, gedcom, families, husbands, wifes);
            for(Indi husband : husbands) {
                for(Indi wife : wifes) {
                    models.add(new MergeModelMiscMarc(mergeRecord, gedcom, husband, wife));
                }
                models.add(new MergeModelMiscMarc(mergeRecord, gedcom, husband, (Indi)null));
            }
            for(Indi wife : wifes) {
                models.add(new MergeModelMiscMarc(mergeRecord, gedcom, (Indi)null, wife));
            }

            // je recherche les familles des parents compatibles qui ne sont pas
            // dans les modeles precedents
            if (showNewParents ||
                (showNewParents
                   && !mergeRecord.getIndi().getFatherFirstName().isEmpty()
                   && !mergeRecord.getIndi().getMotherFirstName().isEmpty()
                   && !mergeRecord.getIndi().getMotherLastName().isEmpty()

                   ) ) {

                List<Fam> husbandFamilies = new ArrayList<Fam>();
                List<Fam> wifeFamilies = new ArrayList<Fam>();

                for (Fam husbandFamily : MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom) ) {
                    Indi[] children = husbandFamily.getChildren();

                    boolean foundHusband = false;

                    for (int i = 0; i < children.length; i++) {
                        // l'enfant ne doit pas être dans husbands déjà retenus
                        if (husbands.contains(children[i])) {
                            foundHusband = true;
                        }
                        // l'enfant ne doit pas être un epoux dans une famile déjà retenue
                        for (Fam family : families) {
                            if (family.getHusband()!=null) {
                                if (family.getHusband().equals(children[i])) {
                                    foundHusband = true;
                                }
                            }
                        }
                    }
                    if (!foundHusband ) {
                       husbandFamilies.add(husbandFamily);
                    }
                }

                for (Fam wifeFamily : MergeQuery.findFamilyCompatibleWithWifeParents(mergeRecord, gedcom) ) {
                    Indi[] children = wifeFamily.getChildren();

                    boolean foundWife = false;

                    for (int i = 0; i < children.length; i++) {
                        // l'enfant ne doit pas être dans husbands
                        if (wifes.contains(children[i])) {
                            foundWife = true;
                        }
                        // l'enfant ne doit pas être un epoux dans une famile
                        for (Fam family : families) {
                            if (family.getWife() != null) {
                                if (family.getWife().equals(children[i])) {
                                    foundWife = true;
                                }
                            }
                        }
                    }
                    if (!foundWife ) {
                       wifeFamilies.add(wifeFamily);
                    }
                }

                for(Fam husbandFamily : husbandFamilies) {
                    for(Fam wifeFamily : wifeFamilies) {
                        models.add(new MergeModelMiscMarc(mergeRecord, gedcom, husbandFamily, wifeFamily));
                    }
                    models.add(new MergeModelMiscMarc(mergeRecord, gedcom, husbandFamily, (Fam)null));
                }
                for(Fam wifeFamily : wifeFamilies) {
                    models.add(new MergeModelMiscMarc(mergeRecord, gedcom, (Fam)null, wifeFamily));
                }

                // j'ajoute les combinaisons entre les epoux précedents et les familles
                 for(Indi husband : husbands) {
                    for(Fam wifeFamily : wifeFamilies) {
                        models.add(new MergeModelMiscMarc(mergeRecord, gedcom, husband, wifeFamily));
                    }
                }
                for(Indi wife : wifes) {
                    for(Fam husbandFamily : husbandFamilies) {
                        models.add(new MergeModelMiscMarc(mergeRecord, gedcom, husbandFamily, wife));
                    }
                }
            }
        }
        return models;
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    MergeModelMiscMarc(MergeRecord record, Gedcom gedcom) throws Exception {
        super(record, gedcom);
        this.currentFamily = null;
        addRowSource();
        addRowFamily();
        addRowHusband(null);
        addRowHusbandFamily(null);
        addRowSeparator();
        addRowWife(null);
        addRowWifeFamily(null);
    }

    MergeModelMiscMarc(MergeRecord record, Gedcom gedcom, Fam selectedFamily) throws Exception {
        super(record, gedcom);
        this.currentFamily = selectedFamily;
        addRowSource();
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

    MergeModelMiscMarc(MergeRecord record, Gedcom gedcom, Indi husband, Indi wife) throws Exception {
        super(record, gedcom);
        this.currentFamily = null;
        addRowSource();
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

    MergeModelMiscMarc(MergeRecord record, Gedcom gedcom, Indi husband, Fam wifeParentFamily) throws Exception {
        super(record, gedcom);
        this.currentFamily = null;
        addRowSource();
        addRowFamily();
        addRowHusband(husband);
        if ( husband!= null ) {
            addRowHusbandFamily(husband.getFamilyWhereBiologicalChild());
        } else {
            addRowHusbandFamily(null);
        }
        addRowSeparator();
        addRowWife(null);
        addRowWifeFamily(wifeParentFamily);
    }

    MergeModelMiscMarc(MergeRecord record, Gedcom gedcom, Fam husbandParentFamily, Indi wife) throws Exception {
        super(record, gedcom);
        this.currentFamily = null;
        addRowSource();
        addRowFamily();
        addRowHusband(null);
        addRowHusbandFamily(husbandParentFamily);

        addRowSeparator();
        addRowWife(wife);
        if ( wife!= null) {
            addRowWifeFamily(wife.getFamilyWhereBiologicalChild());
        } else {
            addRowWifeFamily(null);
        }
    }

    MergeModelMiscMarc(MergeRecord record, Gedcom gedcom, Fam husbandParentFamily, Fam wifeParentFamily) throws Exception {
        super(record, gedcom);
        this.currentFamily = null;
        addRowSource();
        addRowFamily();
        addRowHusband(null);
        addRowHusbandFamily(husbandParentFamily);
        addRowSeparator();
        addRowWife(null);
        addRowWifeFamily(wifeParentFamily);
    }
    
    private void addRowFamily() {
        if (currentFamily != null) {
            // je recupere le contrat de mariage existant
            Property marcProperty = currentFamily.getProperty(record.getTag());

            // j'affiche l'identifiant de la famille
            addRow(RowType.MarriageFamily, record, currentFamily);
            // j'affiche la date du contrat de mariage
            addRow(RowType.EventDate, record.getEventDate(), marcProperty!= null ? (PropertyDate) marcProperty.getProperty("DATE") : null);
            // j'affiche le lieu du contrat de mariage
            addRow(RowType.EventPlace, record.getEventPlace(), marcProperty!= null ? marcProperty.getValue(new TagPath("MARC:PLAC"), "") : "");
            // j'affiche le commentaire
            addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), marcProperty!= null ? marcProperty.getValue(new TagPath("MARC:NOTE"), "") : "");

            // j'affiche la date de l'acte de mariage
            addRow(RowType.MarriageDate, record.calculateMariageDateFromMarc(record.getEventDate()), currentFamily.getMarriageDate());
            // j'affiche un separateur
            addRowSeparator();
        } else {
            // selectedFamily est nul
            
            // j'affiche l'identifiant de la famille
            addRow(RowType.MarriageFamily, record, null);
            // j'affiche la date du mariage
            addRow(RowType.EventDate, record.getEventDate(), null);
            // j'affiche le lieu de l'acte de mariage
            addRow(RowType.EventPlace, record.getEventPlace(), "");
            // j'affiche le commentaire
            addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), "");
            // j'affiche la date de l'acte de mariage
            addRow(RowType.MarriageDate, record.calculateMariageDateFromMarc(record.getEventDate()), null);
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
            addRow(RowType.IndiLastName,   record.getIndi().getLastName(),  husband.getLastName(), husband);
            addRow(RowType.IndiFirstName,  record.getIndi().getFirstName(), husband.getFirstName());
            addRow(RowType.IndiBirthDate,  record.getIndi().getBirthDate(), husband.getBirthDate(false));
            addRow(RowType.IndiBirthPlace, record.getIndi().getBirthPlace(),     husband.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.IndiOccupation, record.getIndi().getOccupationWithDate(),  MergeQuery.findOccupation(husband, record.getEventDate()));
        } else {
            // j'affiche les informations de l'epoux
            addRow(RowType.IndiLastName,   record.getIndi().getLastName(),  "", null);
            addRow(RowType.IndiFirstName,  record.getIndi().getFirstName(), "");
            addRow(RowType.IndiBirthDate,  record.getIndi().getBirthDate(), null);
            addRow(RowType.IndiBirthPlace, record.getIndi().getBirthPlace(),     "");
            addRow(RowType.IndiOccupation, record.getIndi().getOccupationWithDate(), "");
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
            addRow(RowType.IndiParentMarriageDate, record.getIndi().getParentMarriageDate(), husbandFamily.getMarriageDate());
            addRowHusbandFather( husbandFamily.getHusband());
            addRowHusbandMother( husbandFamily.getWife());
        } else {
            addRow(RowType.IndiParentFamily, record, null);
            // je recherche une estimation de la date de mariage des parents a partir du relevé
            addRow(RowType.IndiParentMarriageDate, record.getIndi().getParentMarriageDate(), null);
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
            addRow(RowType.WifeLastName,   record.getWife().getLastName(), wife.getLastName(), wife);
            addRow(RowType.WifeFirstName,  record.getWife().getFirstName(), wife.getFirstName());
            addRow(RowType.WifeBirthDate,  record.getWife().getBirthDate(), wife.getBirthDate(false));
            addRow(RowType.WifePlace,      record.getWife().getBirthPlace(), wife.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.WifeOccupation, record.getWife().getOccupationWithDate(), MergeQuery.findOccupation(wife, record.getEventDate()));
        } else {
            addRow(RowType.WifeLastName,   record.getWife().getLastName(), "", null);
            addRow(RowType.WifeFirstName,  record.getWife().getFirstName(), "");
            addRow(RowType.WifeBirthDate,  record.getWife().getBirthDate(), null);
            addRow(RowType.WifePlace,      record.getWife().getBirthPlace(), "");
            addRow(RowType.WifeOccupation, record.getWife().getOccupationWithDate(), "");
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
            addRow(RowType.WifeParentMarriageDate, record.getWife().getParentMarriageDate(), wifeFamily.getMarriageDate());
            addRowWifeFather( wifeFamily.getHusband());
            addRowWifeMother( wifeFamily.getWife());
        } else {
            addRow(RowType.WifeParentFamily, record, null);
            // j'affiche une estimation de la date de mariage des parents a partir du relevé
            addRow(RowType.WifeParentMarriageDate, record.getWife().getParentMarriageDate(), null);
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
            addRow(RowType.IndiFatherLastName,  record.getIndi().getFatherLastName(), husbandFather.getLastName(), husbandFather);
            addRow(RowType.IndiFatherFirstName, record.getIndi().getFatherFirstName(), husbandFather.getFirstName());
            addRow(RowType.IndiFatherBirthDate, record.getIndi().getFatherBirthDate(), husbandFather.getBirthDate());
            addRow(RowType.IndiFatherDeathDate, record.getIndi().getFatherDeathDate(), husbandFather.getDeathDate());
            addRow(RowType.IndiFatherOccupation, record.getIndi().getFatherOccupationWithDate(),  MergeQuery.findOccupation(husbandFather, record.getEventDate()));
        } else {
            addRow(RowType.IndiFatherLastName,  record.getIndi().getFatherLastName(), "", null);
            addRow(RowType.IndiFatherFirstName, record.getIndi().getFatherFirstName(), "");
            addRow(RowType.IndiFatherBirthDate, record.getIndi().getFatherBirthDate(), null);
            addRow(RowType.IndiFatherDeathDate, record.getIndi().getFatherDeathDate(), null);
            addRow(RowType.IndiFatherOccupation, record.getIndi().getFatherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations de la mere de l'epoux
     * @param wife
     */
    private void addRowHusbandMother(Indi husbandMother) throws Exception {
        if (husbandMother != null) {
            addRow(RowType.IndiMotherLastName, record.getIndi().getMotherLastName(), husbandMother.getLastName(), husbandMother);
            addRow(RowType.IndiMotherFirstName, record.getIndi().getMotherFirstName(), husbandMother.getFirstName());
            addRow(RowType.IndiMotherBirthDate, record.getIndi().getMotherBirthDate(), husbandMother.getBirthDate(false));
            addRow(RowType.IndiMotherDeathDate, record.getIndi().getMotherDeathDate(), husbandMother.getDeathDate(false));
            addRow(RowType.IndiMotherOccupation, record.getIndi().getMotherOccupationWithDate(),  MergeQuery.findOccupation(husbandMother, record.getEventDate()));
        } else {
            addRow(RowType.IndiMotherLastName, record.getIndi().getMotherLastName(), "", null);
            addRow(RowType.IndiMotherFirstName, record.getIndi().getMotherFirstName(), "");
            addRow(RowType.IndiMotherBirthDate, record.getIndi().getMotherBirthDate(), null);
            addRow(RowType.IndiMotherDeathDate, record.getIndi().getMotherDeathDate(), null);
            addRow(RowType.IndiMotherOccupation, record.getIndi().getMotherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations du pere de l'epouse
     * @param wife
     */
    private void addRowWifeFather(Indi wifeFather) throws Exception {
        if (wifeFather != null) {
            addRow(RowType.WifeFatherLastName,  record.getWife().getFatherLastName(), wifeFather.getLastName(), wifeFather);
            addRow(RowType.WifeFatherFirstName, record.getWife().getFatherFirstName(), wifeFather.getFirstName());
            addRow(RowType.WifeFatherBirthDate, record.getWife().getFatherBirthDate(), wifeFather.getBirthDate());
            addRow(RowType.WifeFatherDeathDate, record.getWife().getFatherDeathDate(), wifeFather.getDeathDate());
            addRow(RowType.WifeFatherOccupation, record.getWife().getFatherOccupationWithDate(), MergeQuery.findOccupation(wifeFather, record.getEventDate()));
        } else {
            addRow(RowType.WifeFatherLastName,  record.getWife().getFatherLastName(), "", null);
            addRow(RowType.WifeFatherFirstName, record.getWife().getFatherFirstName(), "");
            addRow(RowType.WifeFatherBirthDate, record.getWife().getFatherBirthDate(), null);
            addRow(RowType.WifeFatherDeathDate, record.getWife().getFatherDeathDate(), null);
            addRow(RowType.WifeFatherOccupation, record.getWife().getFatherOccupationWithDate(), "");
        }
    }

     /**
     * affiche les informations de la mere de l'epouses
     * @param wife
     */
    private void addRowWifeMother(Indi wifeMother) throws Exception {
        if (wifeMother != null) {
            addRow(RowType.WifeMotherLastName,  record.getWife().getMotherLastName(), wifeMother.getLastName(), wifeMother);
            addRow(RowType.WifeMotherFirstName, record.getWife().getMotherFirstName(), wifeMother.getFirstName());
            addRow(RowType.WifeMotherBirthDate, record.getWife().getMotherBirthDate(), wifeMother.getBirthDate(false));
            addRow(RowType.WifeMotherDeathDate, record.getWife().getMotherDeathDate(), wifeMother.getDeathDate(false));
            addRow(RowType.WifeMotherOccupation, record.getWife().getMotherOccupationWithDate(), MergeQuery.findOccupation(wifeMother, record.getEventDate()));
        } else {
            addRow(RowType.WifeMotherLastName,  record.getWife().getMotherLastName(), "", null);
            addRow(RowType.WifeMotherFirstName, record.getWife().getMotherFirstName(), "");
            addRow(RowType.WifeMotherBirthDate, record.getWife().getMotherBirthDate(), null);
            addRow(RowType.WifeMotherDeathDate, record.getWife().getMotherDeathDate(), null);
            addRow(RowType.WifeMotherOccupation, record.getWife().getMotherOccupationWithDate(), "");
        }
    }

    /**
     * copie les données du relevé dans l'entité
     */
    @Override
    protected Property copyRecordToEntity() throws Exception {

        Property resultProperty;
        
        Indi husband = (Indi) getEntityObject(RowType.IndiLastName);
        if (husband == null) {
            // je cree l'individu
            husband = (Indi) gedcom.createEntity(Gedcom.INDI);
            husband.setName(record.getIndi().getFirstName(), record.getIndi().getLastName());
            husband.setSex(PropertySex.MALE);
        } else {
            // je copie le nom de l'epoux
            if (isChecked(RowType.IndiLastName)) {
                husband.setName(husband.getFirstName(), record.getIndi().getLastName());
            }

            // je copie le prénom de l'epoux
            if (isChecked(RowType.IndiFirstName)) {
                husband.setName(record.getIndi().getFirstName(), husband.getLastName());
            }
        }
        
        // je copie la date, le lieu et commentaire de naissance de l'epoux
        if (isChecked(RowType.IndiBirthDate)) {
            copyBirthDate(husband, getRow(RowType.IndiBirthDate), record.getIndi().getBirthPlace(), record);
        }

        // je copie la profession ou la residence de l'epoux
        if (isChecked(RowType.IndiOccupation) ) {
            copyOccupation(husband, record.getIndi().getOccupation(), record.getIndi().getResidence(), true, record);
        }

        // je copie les données des parents de l'epoux
        if (isChecked(RowType.IndiParentFamily)) {
            // je copie la famille des parents
            Fam parentfamily = (Fam) getEntityObject(RowType.IndiParentFamily);
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
                copyMarriageDate(parentfamily, getRow(RowType.IndiParentMarriageDate), record );
            }
            
            copyIndiFather(record.getIndi(), (Indi) getEntityObject(RowType.IndiFatherLastName), parentfamily);
//            // je copie le nom et le prenom du pere de l'epoux
//            Indi father = (Indi) getRow(RowType.IndiFatherLastName).entityObject;
//            if (father == null) {
//                // je cree le pere
//                father = (Indi) gedcom.createEntity(Gedcom.INDI);
//                father.setName(record.getIndi().getFatherFirstName(), record.getIndi().getFatherLastName());
//                father.setSex(PropertySex.MALE);
//                parentfamily.setHusband(father);
//            } else {
//                if (isChecked(RowType.IndiFatherFirstName)) {
//                    father.setName(record.getIndi().getFatherFirstName(), father.getLastName());
//                }
//                if (isChecked(RowType.IndiFatherLastName)) {
//                    father.setName(father.getFirstName(), record.getIndi().getFatherLastName());
//                }
//            }
//
//            // je copie la date de naissance du pere de l'epoux
//            if (isChecked(RowType.IndiFatherBirthDate)) {
//                copyBirthDate(father, getRow(RowType.IndiFatherBirthDate), "", record);
//            }
//
//            //je copie la date de décès du pere de l'epoux
//            if (isChecked(RowType.IndiFatherDeathDate)) {
//                copyDeathDate(father, getRow(RowType.IndiFatherDeathDate), "", record);
//            }
//
//            // je copie la profession du pere
//            if (isChecked(RowType.IndiFatherOccupation)) {
//                copyOccupation(father, record.getIndi().getFatherOccupation(), record.getIndi().getFatherResidence(), true, record);
//            }

            copyIndiMother(record.getIndi(), (Indi) getEntityObject(RowType.IndiMotherLastName), parentfamily);
//            // je copie le nom et le prenom de la mere de l'epoux
//            Indi mother = (Indi) getRow(RowType.IndiMotherLastName).entityObject;
//            if (mother == null) {
//                // je cree le pere
//                mother = (Indi) gedcom.createEntity(Gedcom.INDI);
//                mother.setName(record.getIndi().getMotherFirstName(), record.getIndi().getMotherLastName());
//                mother.setSex(PropertySex.FEMALE);
//                parentfamily.setWife(mother);
//            } else {
//                if (isChecked(RowType.IndiMotherFirstName)) {
//                    mother.setName(record.getIndi().getMotherFirstName(), mother.getLastName());
//                }
//                if (isChecked(RowType.IndiMotherLastName)) {
//                    mother.setName(mother.getFirstName(), record.getIndi().getMotherLastName());
//                }
//            }
//
//            // je copie la date de naissance de la mere de l'epoux
//            if (isChecked(RowType.IndiMotherBirthDate)) {
//                copyBirthDate(mother, getRow(RowType.IndiMotherBirthDate), "", record);
//            }
//
//            // je copie la date de décès de la mere de l'epoux
//            if (isChecked(RowType.IndiMotherDeathDate)) {
//                copyDeathDate(mother, getRow(RowType.IndiMotherDeathDate), "", record);
//            }
//
//            // je copie la profession de la mere de l'epoux
//            if (isChecked(RowType.IndiMotherOccupation) ) {
//                copyOccupation(mother, record.getIndi().getMotherOccupation(), record.getIndi().getMotherResidence(), true, record);
//            }

        } // parents de l'epoux


        ///////////////////////////////////////////////////////////
        // wife
        ///////////////////////////////////////////////////////////
        Indi wife = (Indi) getEntityObject(RowType.WifeLastName);
        if (wife == null) {
            // je cree l'indivis
            wife = (Indi) gedcom.createEntity(Gedcom.INDI);
            wife.setName(record.getWife().getFirstName(), record.getWife().getLastName());
            wife.setSex(PropertySex.FEMALE);
        } else {
            // je copie le nom de l'epouse
            if (isChecked(RowType.WifeLastName)) {
                wife.setName(wife.getFirstName(), record.getWife().getLastName());
            }

            // je copie le prénom de l'epouse
            if (isChecked(RowType.WifeFirstName)) {
                wife.setName(record.getWife().getFirstName(), wife.getLastName());
            }
        }

        // je copie la date, le lieu et le commentaire de naissance de l'epouse
        if (isChecked(RowType.WifeBirthDate)) {
            copyBirthDate(wife, getRow(RowType.WifeBirthDate), record.getWife().getBirthPlace(), record);
        }

        // je copie la profession de l'epouse
        if (isChecked(RowType.WifeOccupation)) {
            copyOccupation(wife, record.getWife().getOccupation(), record.getWife().getResidence(), true, record);
        }


        // je copie les données des parents de l'epouse
        if (isChecked(RowType.WifeParentFamily)) {
            // je copie la famille des parents
            Fam parentfamily = (Fam) getEntityObject(RowType.WifeParentFamily);
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
                copyMarriageDate(parentfamily, getRow(RowType.WifeParentMarriageDate), record );
            }

            copyWifeFather(record.getWife(), (Indi) getEntityObject(RowType.WifeFatherLastName), parentfamily);
//            // je copie le nom et le prenom du pere de l'epouse
//            Indi father = (Indi) getRow(RowType.WifeFatherLastName).entityObject;
//            if (father == null) {
//                // je cree le pere
//                father = (Indi) gedcom.createEntity(Gedcom.INDI);
//                father.setName(record.getWife().getFatherFirstName(), record.getWife().getFatherLastName());
//                father.setSex(PropertySex.MALE);
//                parentfamily.setHusband(father);
//            } else {
//                if (isChecked(RowType.WifeFatherFirstName)) {
//                    father.setName(record.getWife().getFatherFirstName(), father.getLastName());
//                }
//                if (isChecked(RowType.WifeFatherLastName)) {
//                    father.setName(father.getFirstName(), record.getWife().getFatherLastName());
//                }
//            }
//
//            // je copie la date de naissance du pere de l'epouse
//            if (isChecked(RowType.WifeFatherBirthDate)) {
//                copyBirthDate(father, getRow(RowType.WifeFatherBirthDate), "", record);
//            }
//
//            //je copie la date de décès du pere de l'epouse
//            if (isChecked(RowType.WifeFatherDeathDate)) {
//                copyDeathDate(father, getRow(RowType.WifeFatherDeathDate), "", record);
//            }
//
//            // je copie la profession du pere de l'epouse
//            if (isChecked(RowType.WifeFatherOccupation) ) {
//                copyOccupation(father, record.getWife().getFatherOccupation(), record.getWife().getFatherResidence(), true, record);
//            }

            copyWifeMother(record.getWife(), (Indi) getEntityObject(RowType.WifeMotherLastName), parentfamily);
//            // je copie le nom et le prenom de la mere de l'epouse
//            Indi mother = (Indi) getRow(RowType.WifeMotherLastName).entityObject;
//            if (mother == null) {
//                // je cree le pere
//                mother = (Indi) gedcom.createEntity(Gedcom.INDI);
//                mother.setName(record.getWife().getMotherFirstName(), record.getWife().getMotherLastName());
//                mother.setSex(PropertySex.FEMALE);
//                parentfamily.setWife(mother);
//            } else {
//                if (isChecked(RowType.WifeMotherFirstName)) {
//                    mother.setName(record.getWife().getMotherFirstName(), mother.getLastName());
//                }
//                if (isChecked(RowType.WifeMotherLastName)) {
//                    mother.setName(mother.getFirstName(), record.getWife().getMotherLastName());
//                }
//            }
//
//            // je copie la date de naissance de la mere e l'epouse
//            if (isChecked(RowType.WifeMotherBirthDate)) {
//                copyBirthDate(mother, getRow(RowType.WifeMotherBirthDate), "", record);
//            }
//
//            // je copie la date de décès de la mere e l'epouse
//            if (isChecked(RowType.WifeMotherDeathDate)) {
//                copyDeathDate(mother, getRow(RowType.WifeMotherDeathDate), "", record);
//            }
//
//            // je copie la profession de la mere de l'epouse
//            if (isChecked(RowType.WifeMotherOccupation) ) {
//                copyOccupation(mother, record.getWife().getMotherOccupation(), record.getWife().getMotherResidence(), true, record);
//            }

        } // parents de l'epouse



        ///////////////////////////////////////////////////////////
        // family & contrat de mariage & mariage
        ///////////////////////////////////////////////////////////

        //je cree la famille si necessaire
        if (currentFamily == null) {
            currentFamily = (Fam) gedcom.createEntity(Gedcom.FAM);
            // je lie le mari et la femme a la famille
            currentFamily.setHusband(husband);
            currentFamily.setWife(wife);
        } else {
            // j'ajoute les epoux si l'un d'eux est nouveau
            if (currentFamily.getHusband() != husband) {
                currentFamily.setHusband(husband);
            }
            if (currentFamily.getWife() != wife) {
                currentFamily.setWife(wife);
            }
        }
        
        resultProperty = currentFamily;


        if( ! record.isInsinuation() ) {
            // je crée la propriété MARC (contrat de mariage)
            Property marcProperty = currentFamily.getProperty(record.getTag());
            if (marcProperty == null) {
                marcProperty = currentFamily.addProperty(record.getTag(), "");
            }
            
            resultProperty = marcProperty;

            // je copie la date de contrat de mariage
            if (isChecked(RowType.EventDate)) {
                // je cherhche la date contrat de mariage si elle existe, meme si elle n'est pas valide
                PropertyDate propertyDate = (PropertyDate) marcProperty.getProperty("DATE",false);
                if (propertyDate == null) {
                    propertyDate = (PropertyDate) marcProperty.addProperty("DATE", "");
                }
                propertyDate.setValue(record.getEventDate().getValue());
            }

            // je copie la source du releve de contrat de mariage
            if (isChecked(RowType.EventSource) || isChecked(RowType.EventPage)) {
                copySource((Source) getEntityObject(RowType.EventSource), marcProperty, isChecked(RowType.EventPage), record);
            }

            // je copie le lieu du contrat de mariage
            if (isChecked(RowType.EventPlace)) {
                Property propertyPlace = marcProperty.getProperty("PLAC");
                if (propertyPlace == null) {
                    // je cree le lieu .
                    propertyPlace = marcProperty.addProperty("PLAC", "");
                }
                propertyPlace.setValue(record.getEventPlace());
            }

            // je copie le commentaire du contrat de mariage.
            if (isChecked(RowType.EventComment)) {
                Property propertyNote = marcProperty.getProperty("NOTE");
                if (propertyNote == null) {
                    // je cree une note .
                    propertyNote = marcProperty.addProperty("NOTE", "");
                }

                // j'ajoute le commentaire du contrat de mariage au debut de la note existante.
                String value = propertyNote.getValue();
                String comment = record.getEventComment(showFrenchCalendarDate);
                if (!comment.isEmpty()) {
                    if (!value.isEmpty()) {
                        comment += "\n";
                    }
                    comment += value;
                    propertyNote.setValue(comment);
                }
            }
        } else {
            // c'est une insinuation

            // je crée la propriété MARC (contrat de mariage)
            Property marcProperty = currentFamily.getProperty(record.getTag());
            if (marcProperty == null) {
                marcProperty = currentFamily.addProperty(record.getTag(), "");
            }

            // je copie la date de contrat de mariage
            if (isChecked(RowType.EventDate)) {
                // je cherhche la date contrat de mariage si elle existe, meme si elle n'est pas valide
                PropertyDate propertyDate = (PropertyDate) marcProperty.getProperty("DATE",false);
                if (propertyDate == null) {
                    propertyDate = (PropertyDate) marcProperty.addProperty("DATE", "");
                }
                propertyDate.setValue(record.getEventDate().getValue());
            }

            // je copie le commentaire du contrat de mariage.
            if (isChecked(RowType.EventComment)) {
                Property propertyNote = marcProperty.getProperty("NOTE");
                if (propertyNote == null) {
                    // je cree une note .
                    propertyNote = marcProperty.addProperty("NOTE", "");
                }

                // j'ajoute le commentaire du contrat de mariage au debut de la note existante.
                String value = propertyNote.getValue();
                String comment = record.makeInsinuationReferenceComment(showFrenchCalendarDate);
                if (!comment.isEmpty()) {
                    if (!value.isEmpty()) {
                        comment += "\n";
                    }
                    comment += value;
                    propertyNote.setValue(comment);
                }
            }

            // je crée la propriété EVEN (contrat de mariage)
            Property insinuationProperty = currentFamily.getProperty("EVEN");

            if (insinuationProperty == null ) {
                insinuationProperty = currentFamily.addProperty("EVEN", "", currentFamily.getPropertyPosition(marcProperty));
            } else {
                // je cree un nouveau tag EVEN si la date est différente
                PropertyDate propertyDate = (PropertyDate) insinuationProperty.getProperty("DATE",false);
                if (propertyDate == null) {
                    insinuationProperty = currentFamily.addProperty("EVEN", "", currentFamily.getPropertyPosition(marcProperty));
                } else if ( ! insinuationProperty.equals(record.getEventDate()) ) {
                    insinuationProperty = currentFamily.addProperty("EVEN", "", currentFamily.getPropertyPosition(marcProperty));
                }
            }
            
            resultProperty = insinuationProperty;

            // je copie la date de l'insinuation
            if (isChecked(RowType.EventDate)) {
                // je cherche la date d'insinuation si elle existe, meme si elle n'est pas valide
                PropertyDate propertyDate = (PropertyDate) insinuationProperty.getProperty("DATE",false);
                if (propertyDate == null) {
                    propertyDate = (PropertyDate) insinuationProperty.addProperty("DATE", "");
                }
                propertyDate.setValue(record.getInsinuationDate().getValue());
            }

            // je copie la source du releve de l'insinuation
            if (isChecked(RowType.EventSource) || isChecked(RowType.EventPage)) {
                copySource((Source) getEntityObject(RowType.EventSource), insinuationProperty, isChecked(RowType.EventPage), record);
            }

            // je copie le lieu de l'insinuation
            if (isChecked(RowType.EventPlace)) {
                Property propertyPlace = insinuationProperty.getProperty("PLAC");
                if (propertyPlace == null) {
                    // je cree le lieu .
                    propertyPlace = insinuationProperty.addProperty("PLAC", "");
                }
                propertyPlace.setValue(record.getEventPlace());
            }

            // je copie le commentaire de l'insinuation
            if (isChecked(RowType.EventComment)) {
                Property propertyNote = insinuationProperty.getProperty("NOTE");
                if (propertyNote == null) {
                    // je cree une note .
                    propertyNote = insinuationProperty.addProperty("NOTE", "");
                }

                // j'ajoute le commentaire de l'insinuation au debut de la note existante.
                String value = propertyNote.getValue();
                String comment = record.getEventComment(showFrenchCalendarDate);
                if (!comment.isEmpty()) {
                    if (!value.isEmpty()) {
                        comment += "\n";
                    }
                    comment += value;
                    propertyNote.setValue(comment);
                }
            }


            

        }
        // je crée la propriété MARR (mariage) si elle n'existe pas
        // ou je mets à jour la date 
        PropertyDate recordDate = record.getEventDate();
        PropertyDate mariageDate = currentFamily.getMarriageDate(); 
        PropertyDate mergeDate ;
        if (mariageDate != null) {
            mergeDate = MergeQuery.getMostAccurateDate(recordDate, mariageDate);
        } else {
            mergeDate = recordDate;
        }
         
        if ( mergeDate != null && mergeDate != mariageDate) {
            // je copie la date du mariage estimée a partir de la date du contrat de mariage
            copyMarriageDate(currentFamily, getRow(RowType.MarriageDate), record );            
        }

        return resultProperty;
    }

    /**
     * retourne l'individu proposé dans le modele
     * @return 
     */
    @Override
    protected Entity getProposedEntity() {
        if (currentFamily != null)  {
            return currentFamily;
        } else {
            Entity husband = getEntityObject(RowType.IndiLastName);
            if (husband != null) {
                return husband;
            } else {
                Entity wife = getEntityObject(RowType.WifeLastName);
                if (wife != null) {
                    return wife;
                } else {
                    Entity husbandParentFamily = getEntityObject(RowType.IndiParentFamily);
                     if (husbandParentFamily != null) {
                        return husbandParentFamily;
                    } else {
                        Entity wifeParentFamily = getEntityObject(RowType.WifeParentFamily);
                        if (wifeParentFamily != null) {
                           return wifeParentFamily;
                       } else {
                           return null;
                       }
                    }
                }
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
     * retourne la propriété concernée par l'acte
     * @return propriété concernée par l'acte
     */
    @Override
    protected Property getSelectedProperty() {
        if (currentFamily != null) {
           return currentFamily.getProperty(record.getTag()); 
        } else {
            return null;
        }
    }
    

    /**
     * retourne les noms des epoux pour constituer le titre de la fenetre principale
     * @return
     */
    @Override
    protected String getTitle() {
        String husband = record.getIndi().getFirstName() + " "+ record.getIndi().getLastName();
        String wife = record.getWife().getFirstName() + " "+ record.getWife().getLastName()+ " " + record.getEventDate().getDisplayValue();
        return MessageFormat.format("{0}: {1} x {2}", record.getEventType(), husband, wife);
    }

    /**
     * retourne un resumé du modele
     * Cette chaine sert de commentaire dans la liste des modeles
     * @return
     */
    @Override
    public String getSummary(Entity selectedEntity) {

        String summary;

        if ( currentFamily == null ) {
            String husband;
            String wife;
            String indiParents;
            String wifeParents;

            // j'affiche l'epoux
            if (getEntityObject(RowType.IndiLastName) == null ) {
                husband = "Nouvel époux";
            } else {
                husband = getEntityObject(RowType.IndiLastName).toString(true);
            }
            if (getEntityObject(RowType.WifeLastName) == null ) {
                wife = "Nouvelle épouse";
            } else {
                wife = getEntityObject(RowType.WifeLastName).toString(true);
            }
            // j'afficje les parents
            if (getEntityObject(RowType.IndiParentFamily) == null ) {
                if ( getEntityObject(RowType.IndiFatherLastName) == null ) {
                    indiParents = "Nouveau père";
                } else {
                    indiParents = ((Indi)getEntityObject(RowType.IndiFatherLastName)).getDisplayValue();
                }
                indiParents += " x ";
                if ( getEntityObject(RowType.IndiFatherLastName) == null ) {
                    indiParents += "Nouvelle mère";
                } else {
                    indiParents += ((Indi)getEntityObject(RowType.IndiMotherLastName)).getDisplayValue();
                }
            }  else {
                // j'affiche la famille des parents de l'epoux
                indiParents = ((Fam)getEntityObject(RowType.IndiParentFamily)).toString(false);
            }

            
            if (getEntityObject(RowType.WifeParentFamily) == null ) {
                if ( getEntityObject(RowType.WifeFatherLastName) == null ) {
                    wifeParents = "Nouveau père";
                } else {
                    wifeParents = ((Indi)getEntityObject(RowType.WifeFatherLastName)).getDisplayValue();
                }
                wifeParents += " x ";
                if ( getEntityObject(RowType.WifeFatherLastName) == null ) {
                    wifeParents += "Nouvelle mère";
                } else {
                    wifeParents += ((Indi)getEntityObject(RowType.WifeMotherLastName)).getDisplayValue();
                }
            }  else {
                // j'affiche la famille des parents de l'epoux
                wifeParents = ((Fam)getEntityObject(RowType.WifeParentFamily)).toString(false);
            }

            //summary = MessageFormat.format(summaryFormat, spouses, indiParents, wifeParents);

            summary = husband + " x " + wife + "; ";
            summary += indiParents;
            summary += "; ";
            summary += wifeParents;

        } else {
            
            if( getRow(RowType.EventDate).entityValue == null) {
                summary = "Ajouter: " + record.getEventType()+ " de ";
            } else {
                summary = "Modifier: " + record.getEventType() + " de ";
            }
           
            if (currentFamily.getHusband() != null) {
                summary += currentFamily.getHusband().toString(false);
            } else {
                summary += "---";
            }
            summary += " x ";
            if (currentFamily.getWife() != null) {
                summary += currentFamily.getWife().toString(false);
            } else {
                summary += "---";
            }
        }
        return summary;
    }

}
