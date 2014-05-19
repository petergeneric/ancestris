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
import org.openide.util.NbBundle;

/**
 *
 * @author Michel
 */
public class MergeModelMarriage extends MergeModel {

    private Fam currentFamily;
    
    /**
     * model factory
     * cree un liste contenant un modele comparant le releve et l'entité
     * selectionnée dans le gedcom.
     * Si selectedEntity = null, la liste contient les modeles comparant le relevé
     * avec les entités du gedcom dont les noms, prenoms, et dates de naissance,
     * mariage et décès sont compatibles avec le relevé.
     * @param mergeRecord   releve
     * @param gedcom
     * @param selectedEntity entité sélectionnée dans le gedcom
     * @return
     */
    static protected List<MergeModel> createMergeModelMarriage (MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity, boolean showNewParents) throws Exception {
        List<MergeModel> models = new ArrayList<MergeModel>();
        if (selectedEntity instanceof Fam) {
            // 2.1) Record Marriage : l'entité selectionnée est une famille
            Fam selectedFamily = (Fam) selectedEntity;

            // j'ajoute un modele avec la famille selectionne
            models.add(new MergeModelMarriage(mergeRecord, gedcom, selectedFamily));

        } else if (selectedEntity instanceof Indi) {
            // 2.2) Record Marriage : l'entité selectionnée est un individu
            Indi selectedIndi = (Indi) selectedEntity;

            // je cherche les familles avec l'individu selectionné
            Fam[] families = selectedIndi.getFamiliesWhereSpouse();
            // j'ajoute les familles compatibles
            for (Fam family : families) {
                models.add(new MergeModelMarriage(mergeRecord, gedcom, family));
            }

            if (showNewParents) {
                // j'ajoute les parents possibles non maries entre eux
                List<Indi> husbands = new ArrayList<Indi>();
                List<Indi> wifes = new ArrayList<Indi>();
                if (selectedIndi.getSex() == PropertySex.MALE) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, selectedIndi, (Indi) null));
                    husbands.add(selectedIndi);
                } else if (selectedIndi.getSex() == PropertySex.FEMALE) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, (Indi) null, selectedIndi));
                    wifes.add(selectedIndi);
                }
                MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(mergeRecord, gedcom, Arrays.asList(families), husbands, wifes);
                for (Indi husband : husbands) {
                    for (Indi wife : wifes) {
                        //TODO  rechercher la famille de l'epoux et la famille de l'epouse et la prendre en compte si elle existe
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, wife));
                    }
                }
            }
        } else {
            // 2.3) Record Marriage : pas d'entité selectionnee

            // j'ajoute une nouvelle famille
            models.add(new MergeModelMarriage(mergeRecord, gedcom));

            // je recherche les familles compatibles
            List<Fam> families = MergeQuery.findFamilyCompatibleWithMarriageRecord(mergeRecord, gedcom, null);
            // j'ajoute les familles compatibles
            for (Fam family : families) {
                models.add(new MergeModelMarriage(mergeRecord, gedcom, family));
            }

            // je recherche les individus compatibles avec l'epoux et l'epouse du releve
            List<Indi> husbands = new ArrayList<Indi>();
            List<Indi> wifes = new ArrayList<Indi>();
            MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(mergeRecord, gedcom, families, husbands, wifes);
            for (Indi husband : husbands) {
                for (Indi wife : wifes) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, wife));
                }
                models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, (Indi) null));
            }
            for (Indi wife : wifes) {
                models.add(new MergeModelMarriage(mergeRecord, gedcom, (Indi) null, wife));
            }

            // je recherche les familles des parents compatibles qui ne sont pas
            // dans les modeles precedents
            if (showNewParents
                    || (showNewParents
                    && !mergeRecord.getIndi().getFatherFirstName().isEmpty()
                    && !mergeRecord.getIndi().getMotherFirstName().isEmpty()
                    && !mergeRecord.getIndi().getMotherLastName().isEmpty())) {

                List<Fam> husbandFamilies = new ArrayList<Fam>();
                List<Fam> wifeFamilies = new ArrayList<Fam>();

                for (Fam husbandFamily : MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeRecord.MergeParticipantType.participant1,  gedcom)) {
                    Indi[] children = husbandFamily.getChildren();

                    boolean foundHusband = false;

                    for (int i = 0; i < children.length; i++) {
                        // l'enfant ne doit pas être dans husbands déjà retenus
                        if (husbands.contains(children[i])) {
                            foundHusband = true;
                        }
                        // l'enfant ne doit pas être un epoux dans une famile déjà retenue
                        for (Fam family : families) {
                            if (family.getHusband() != null) {
                                if (family.getHusband().equals(children[i])) {
                                    foundHusband = true;
                                }
                            }
                        }
                    }
                    if (!foundHusband) {
                        husbandFamilies.add(husbandFamily);
                    }
                }

                for (Fam wifeFamily : MergeQuery.findFamilyCompatibleWithWifeParents(mergeRecord, gedcom)) {
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
                    if (!foundWife) {
                        wifeFamilies.add(wifeFamily);
                    }
                }

                for (Fam husbandFamily : husbandFamilies) {
                    for (Fam wifeFamily : wifeFamilies) {
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, husbandFamily, wifeFamily));
                    }
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, husbandFamily, (Fam) null));
                }
                for (Fam wifeFamily : wifeFamilies) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, (Fam) null, wifeFamily));
                }

                // j'ajoute les combinaisons entre les epoux précedents et les familles
                for (Indi husband : husbands) {
                    for (Fam wifeFamily : wifeFamilies) {
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, wifeFamily));
                    }
                }
                for (Indi wife : wifes) {
                    for (Fam husbandFamily : husbandFamilies) {
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, husbandFamily, wife));
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
    MergeModelMarriage(MergeRecord record, Gedcom gedcom) throws Exception {
        super(record, gedcom);
        this.currentFamily = null;

        addRowSource();
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

    MergeModelMarriage(MergeRecord record, Gedcom gedcom, Indi husband, Indi wife) throws Exception {
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

    MergeModelMarriage(MergeRecord record, Gedcom gedcom, Indi husband, Fam wifeParentFamily) throws Exception {
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

    MergeModelMarriage(MergeRecord record, Gedcom gedcom, Fam husbandParentFamily, Indi wife) throws Exception {
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

    MergeModelMarriage(MergeRecord record, Gedcom gedcom, Fam husbandParentFamily, Fam wifeParentFamily) throws Exception {
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
            // je recupere le mariage existant
            Property marriageProperty = currentFamily.getProperty("MARR");

            // j'affiche l'identifiant de la famille
            addRow(RowType.MarriageFamily, record, currentFamily);
            // j'affiche la date de l'acte de mariage
            addRow(RowType.EventDate, record.getEventDate(), currentFamily.getMarriageDate());
            // j'affiche le lieu de l'acte de mariage
            addRow(RowType.EventPlace, record.getEventPlace(), marriageProperty!= null ? marriageProperty.getValue(new TagPath("MARR:PLAC"), "") : "");
            // j'affiche le commentaire
            addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), marriageProperty!= null ? marriageProperty.getValue(new TagPath("MARR:NOTE"), "") : "");
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
            // j'affiche un separateur
            addRowSeparator();
        }
    }

    /**
     * affiche les informations de l'epoux et de ses parents
     * @param husband
     */
    private void addRowHusband(Indi husband) throws Exception {
        if (husband != null) {
            // j'affiche les informations de l'epoux
            addRow(RowType.IndiLastName,   record.getIndi().getLastName(),  husband.getLastName(), husband);
            addRow(RowType.IndiFirstName,  record.getIndi().getFirstName(), husband.getFirstName());
            addRow(RowType.IndiBirthDate,  record.getIndi().getBirthDate(), husband.getBirthDate(false));
            addRow(RowType.IndiBirthPlace,      record.getIndi().getBirthPlace(),     husband.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.IndiOccupation, record.getIndi().getOccupationWithDate(),  MergeQuery.findOccupation(husband, record.getEventDate()));
            addRow(RowType.IndiDeathDate, record.getIndi().getDeathDate(), husband.getDeathDate());            
        } else {
            // j'affiche les informations de l'epoux
            addRow(RowType.IndiLastName,   record.getIndi().getLastName(),  "", null);
            addRow(RowType.IndiFirstName,  record.getIndi().getFirstName(), "");
            addRow(RowType.IndiBirthDate,  record.getIndi().getBirthDate(), null);
            addRow(RowType.IndiBirthPlace,      record.getIndi().getBirthPlace(),     "");
            addRow(RowType.IndiOccupation, record.getIndi().getOccupationWithDate(), "");
            addRow(RowType.IndiDeathDate, record.getIndi().getDeathDate(), null);            
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
            addRow(RowType.IndiParentMarriageDate, record.getIndi().getParentMarriageDate(),  husbandFamily != null ? husbandFamily.getMarriageDate() : null );
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
            addRow(RowType.WifePlace,      record.getWife().getBirthPlace(),      wife.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.WifeOccupation, record.getWife().getOccupationWithDate(), MergeQuery.findOccupation(wife, record.getEventDate()));
            addRow(RowType.WifeDeathDate,  record.getWife().getDeathDate(), wife.getDeathDate());            
        } else {
            addRow(RowType.WifeLastName,   record.getWife().getLastName(), "", null);
            addRow(RowType.WifeFirstName,  record.getWife().getFirstName(), "");
            addRow(RowType.WifeBirthDate,  record.getWife().getBirthDate(), null);
            addRow(RowType.WifePlace,      record.getWife().getBirthPlace(), "");
            addRow(RowType.WifeOccupation, record.getWife().getOccupationWithDate(), "");
            addRow(RowType.WifeDeathDate,  record.getWife().getDeathDate(), null);            
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
            addRow(RowType.WifeParentMarriageDate, record.getWife().getParentMarriageDate(), wifeFamily != null ? wifeFamily.getMarriageDate() : null);
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

        //PropertyDate eventDate = (PropertyDate) getRow(RowType.EventDate).recordValue;

        Indi husband = (Indi) getRow(RowType.IndiLastName).entityObject;
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

        // je copie la profession de l'epoux
        if (isChecked(RowType.IndiOccupation)) {
            copyOccupation(husband, record.getIndi().getOccupation(), record.getIndi().getResidence(), true, record);
        }

        //je copie la date de décès de l'epoux
        if (isChecked(RowType.IndiDeathDate)) {
            copyDeathDate(husband, getRow(RowType.IndiDeathDate), "", record);
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
                copyMarriageDate(parentfamily, getRow(RowType.IndiParentMarriageDate), record );
            }
            

            // je copie le nom et le prenom du pere de l'epoux
            Indi father = (Indi) getRow(RowType.IndiFatherLastName).entityObject;
            if (father == null) {
                // je cree le pere
                father = (Indi) gedcom.createEntity(Gedcom.INDI);
                father.setName(record.getIndi().getFatherFirstName(), record.getIndi().getFatherLastName());
                father.setSex(PropertySex.MALE);
                parentfamily.setHusband(father);
            } else {
                if (isChecked(RowType.IndiFatherFirstName)) {
                    father.setName(record.getIndi().getFatherFirstName(), father.getLastName());
                }
                if (isChecked(RowType.IndiFatherLastName)) {
                    father.setName(father.getFirstName(), record.getIndi().getFatherLastName());
                }
            }

            // je copie la date de naissance du pere de l'epoux
            if (isChecked(RowType.IndiFatherBirthDate)) {
                copyBirthDate(father, getRow(RowType.IndiFatherBirthDate), "", record);
            }

            //je copie la date de décès du pere de l'epoux
            if (isChecked(RowType.IndiFatherDeathDate)) {
                copyDeathDate(father, getRow(RowType.IndiFatherDeathDate), "", record);
            }

            // je copie la profession du pere
            if (isChecked(RowType.IndiFatherOccupation)) {
                copyOccupation(father, record.getIndi().getFatherOccupation(), record.getIndi().getFatherResidence(), true, record);
            }

            // je copie le nom et le prenom de la mere de l'epoux
            Indi mother = (Indi) getRow(RowType.IndiMotherLastName).entityObject;
            if (mother == null) {
                // je cree le pere
                mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                mother.setName(record.getIndi().getMotherFirstName(), record.getIndi().getMotherLastName());
                mother.setSex(PropertySex.FEMALE);
                parentfamily.setWife(mother);
            } else {
                if (isChecked(RowType.IndiMotherFirstName)) {
                    mother.setName(record.getIndi().getMotherFirstName(), mother.getLastName());
                }
                if (isChecked(RowType.IndiMotherLastName)) {
                    mother.setName(mother.getFirstName(), record.getIndi().getMotherLastName());
                }
            }

            // je copie la date de naissance de la mere de l'epoux
            if (isChecked(RowType.IndiMotherBirthDate)) {
                copyBirthDate(mother, getRow(RowType.IndiMotherBirthDate), "", record);
            }

            // je copie la date de décès de la mere de l'epoux
            if (isChecked(RowType.IndiMotherDeathDate)) {
                copyDeathDate(mother, getRow(RowType.IndiMotherDeathDate), "", record);
            }

            // je copie la profession de la mere de l'epoux
            if (isChecked(RowType.IndiMotherOccupation) ) {
                copyOccupation(mother, record.getIndi().getMotherOccupation(), record.getIndi().getMotherResidence(), true, record);
            }

        } // parents de l'epoux


        ///////////////////////////////////////////////////////////
        // wife
        ///////////////////////////////////////////////////////////
        Indi wife = (Indi) getRow(RowType.WifeLastName).entityObject;
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

        //je copie la date de décès de l'epouse 
        if (isChecked(RowType.WifeDeathDate)) {
            copyDeathDate(wife, getRow(RowType.WifeDeathDate), "", record);
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
                copyMarriageDate(parentfamily, getRow(RowType.WifeParentMarriageDate), record );
            }


            // je copie le nom et le prenom du pere de l'epouse
            Indi father = (Indi) getRow(RowType.WifeFatherLastName).entityObject;
            if (father == null) {
                // je cree le pere
                father = (Indi) gedcom.createEntity(Gedcom.INDI);
                father.setName(record.getWife().getFatherFirstName(), record.getWife().getFatherLastName());
                father.setSex(PropertySex.MALE);
                parentfamily.setHusband(father);
            } else {
                if (isChecked(RowType.WifeFatherFirstName)) {
                    father.setName(record.getWife().getFatherFirstName(), father.getLastName());
                }
                if (isChecked(RowType.WifeFatherLastName)) {
                    father.setName(father.getFirstName(), record.getWife().getFatherLastName());
                }
            }

            // je copie la date de naissance du pere de l'epouse
            if (isChecked(RowType.WifeFatherBirthDate)) {
                copyBirthDate(father, getRow(RowType.WifeFatherBirthDate), "", record);
            }

            //je copie la date de décès du pere de l'epouse
            if (isChecked(RowType.WifeFatherDeathDate)) {
                copyDeathDate(father, getRow(RowType.WifeFatherDeathDate), "", record);
            }

            // je copie la profession du pere de l'epouse
            if (isChecked(RowType.WifeFatherOccupation) ) {
                copyOccupation(father, record.getWife().getFatherOccupation(), record.getWife().getFatherResidence(), true, record);
            }

            // je copie le nom et le prenom de la mere de l'epouse
            Indi mother = (Indi) getRow(RowType.WifeMotherLastName).entityObject;
            if (mother == null) {
                // je cree le pere
                mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                mother.setName(record.getWife().getMotherFirstName(), record.getWife().getMotherLastName());
                mother.setSex(PropertySex.FEMALE);
                parentfamily.setWife(mother);
            } else {
                if (isChecked(RowType.WifeMotherFirstName)) {
                    mother.setName(record.getWife().getMotherFirstName(), mother.getLastName());
                }
                if (isChecked(RowType.WifeMotherLastName)) {
                    mother.setName(mother.getFirstName(), record.getWife().getMotherLastName());
                }
            }

            // je copie la date de naissance de la mere e l'epouse
            if (isChecked(RowType.WifeMotherBirthDate)) {
                copyBirthDate(mother, getRow(RowType.WifeMotherBirthDate), "", record);
            }

            // je copie la date de décès de la mere de l'epouse
            if (isChecked(RowType.WifeMotherDeathDate)) {
                copyDeathDate(mother, getRow(RowType.WifeMotherDeathDate), "", record);
            }

            // je copie la profession de la mere de l'epouse
            if (isChecked(RowType.WifeMotherOccupation) ) {
                copyOccupation(mother, record.getWife().getMotherOccupation(), record.getWife().getMotherResidence(), true, record);
            }

        } // parents de l'epouse



        ///////////////////////////////////////////////////////////
        // family & marriage
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

        // je crée la propriété MARR
        Property marriageProperty = currentFamily.getProperty("MARR");
        if (marriageProperty == null) {
            marriageProperty = currentFamily.addProperty("MARR", "");
        }

        // je copie la source du releve de mariage 
        if (isChecked(RowType.EventSource) || isChecked(RowType.EventPage)) {
            copySource((Source) getRow(RowType.EventSource).entityObject, marriageProperty, isChecked(RowType.EventPage), record);
        }

        // je copie la date de mariage
        if (isChecked(RowType.EventDate)) {
            // j'ajoute (ou remplace ) la date de la naissance
            PropertyDate propertyDate = currentFamily.getMarriageDate(true);
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
            String comment = record.getEventComment(showFrenchCalendarDate);
            if (!comment.isEmpty()) {
                if (!value.isEmpty()) {
                    comment += "\n";
                }
                comment += value;
                propertyNote.setValue(comment);
            }
        }

        return currentFamily;

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
            return currentFamily.getProperty("MARR");
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
        return MessageFormat.format(NbBundle.getMessage(MergeDialog.class, "MergeModel.title.marriage"), husband, wife);
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
                indiParents += " x ";
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
                wifeParents += " x ";
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

            summary = husband + " x " + wife + "; ";
            summary += indiParents;
            summary += "; ";
            summary += wifeParents;

        } else {
            summary =  "Modifier le mariage" + " ";
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
