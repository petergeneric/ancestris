package ancestris.modules.releve.merge;

import static ancestris.modules.releve.merge.MergeLogger.ACCEPT;
import static ancestris.modules.releve.merge.MergeLogger.LOG;
import static ancestris.modules.releve.merge.MergeLogger.REFUSE;
import ancestris.modules.releve.merge.MergeRecord.MergeParticipantType;
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
import java.util.List;
import java.util.logging.Level;
import org.openide.util.NbBundle;

/**
 *
 */
class MergeModelDeath extends MergeModel {

    private Indi currentIndi;

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
    static protected List<MergeModel> createMergeModelDeath (MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity, boolean showNewParents) throws Exception {
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(MergeModelDeath.class.getName(), "createMergeModelDeath", 
                    mergeRecord.getParticipant(MergeParticipantType.participant1).getLastName()
                    + " "
                    + mergeRecord.getParticipant(MergeParticipantType.participant1).getFirstName()
            );
        }
        List<MergeModel> models = new ArrayList<MergeModel>();
        if (selectedEntity instanceof Fam) {
            // 1.1) Record Death : l'entité selectionnée est une famille
            Fam family = (Fam) selectedEntity;
            // j'ajoute un nouvel individu
            models.add(new MergeModelDeath(mergeRecord, gedcom, null, family));
            // je recherche les enfants de la famille sélectionnée compatibles avec le releve
            List<Indi> sameChildren = MergeQuery.findSameChild(mergeRecord, gedcom, family);
            // j'ajoute les enfants compatibles
            for (Indi sameIndi : sameChildren) {
                models.add(new MergeModelDeath(mergeRecord, gedcom, sameIndi, sameIndi.getFamilyWhereBiologicalChild()));
            }
        } else if (selectedEntity instanceof Indi) {
            // 1.2) Record Death : l'entité selectionnée est un individu
            Indi selectedIndi = (Indi) selectedEntity;

            // je recherche la famille avec l'ex conjoint
            List<Fam> marriedFamilies = MergeQuery.findFamilyCompatibleWithParticipantMarried(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            // je cherche les familles des parents compatibles avec le releve
            List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            if (!marriedFamilies.isEmpty()) {
                for (Fam marriedFamily : marriedFamilies) {
                    if (mergeRecord.getIndi().getSex() == PropertySex.MALE) {
                        Indi husband = marriedFamily.getHusband();
                        if (selectedIndi.compareTo(husband) == 0) {
                            Fam husbandParentFamily = husband.getFamilyWhereBiologicalChild();
                            models.add(new MergeModelDeath(mergeRecord, gedcom, husband, marriedFamily, husbandParentFamily));
                            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                LOG.log(ACCEPT, String.format("ACCEPT husband %s with compatible marriedFamily %s and husbandParentFamily %s" , 
                                        husband.getId(),  marriedFamily.getId(), husbandParentFamily.getId() ));
                            }
                            if (husbandParentFamily == null) {
                                // j'ajoute un nouvel individu avec les familles compatibles
                                for (Fam parentFamily : parentFamilies) {
                                    models.add(new MergeModelDeath(mergeRecord, gedcom, husband, marriedFamily, parentFamily));
                                    if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                        LOG.log(ACCEPT, String.format("ACCEPT husband %s with compatible marriedFamily %s and parentFamily %s" , 
                                                husband.getId(),  marriedFamily.getId(), parentFamily.getId() ));
                                    }
                                }
                            }
                        } else {
                            if (LOG.isLoggable(MergeLogger.REFUSE)) {
                                LOG.log(REFUSE, String.format("REFUSE husband %s different from selectedIndi %s" , 
                                        husband.getId(), selectedIndi.getId() ));
                            }                            
                        }
                    } else {
                        Indi wife = marriedFamily.getWife();
                        if (selectedIndi.compareTo(wife) == 0) {
                            Fam wifeParentFamily = wife.getFamilyWhereBiologicalChild();
                            models.add(new MergeModelDeath(mergeRecord, gedcom, wife, marriedFamily, wifeParentFamily));
                            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                LOG.log(ACCEPT, String.format("ACCEPT wife %s with compatible marriedFamily %s and wifeParentFamily %s" , 
                                        wife,  marriedFamily, wifeParentFamily ));
                            }
                            if (wifeParentFamily == null) {
                                // j'ajoute un nouvel individu avec les familles compatibles
                                for (Fam parentFamily : parentFamilies) {
                                    models.add(new MergeModelDeath(mergeRecord, gedcom, wife, marriedFamily, parentFamily));
                                    if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                        LOG.log(ACCEPT, String.format("ACCEPT wife %s with compatible marriedFamily %s and parentFamily %s" , 
                                                wife,  marriedFamily, parentFamily ));
                                    }
                                }
                            }
                        } else {
                            if (LOG.isLoggable(MergeLogger.REFUSE)) {
                                LOG.log(REFUSE, String.format("REFUSE wife %s different from selectedIndi %s",
                                        wife.getId(), selectedIndi.getId() ));
                            }
                        }
                    }
                }
            } else {
                // l'individu n'a pas d'ex conjoint

                // j'ajoute l'individu selectionné par dnd
                if (selectedIndi.getFamilyWhereBiologicalChild() != null) {
                    // j'ajoute l'individu selectionné par dnd
                    models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, selectedIndi.getFamilyWhereBiologicalChild()));
                    if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                        LOG.log(ACCEPT, String.format("ACCEPT selectedIndi %s with compatible FamilyWhereBiologicalChild %s " , 
                                selectedIndi.getId(),  selectedIndi.getFamilyWhereBiologicalChild().getId() ));
                    }
                } else {
                    // je cherche les familles avec selectedIndi
                    Fam[] selectedIndiFamilies = selectedIndi.getFamiliesWhereSpouse();
                    for( Fam fam :selectedIndiFamilies) {
                        // je verifie si le mariage de selectedIndi est avant la date de deces
                        if ( MergeQuery.isRecordBeforeThanDate(fam.getMarriageDate(), mergeRecord.getIndi().getDeathDate(), 0, 0) ) {
                            models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, fam, (Fam) null));
                            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                LOG.log(ACCEPT, String.format("ACCEPT selectedIndi %s with compatible FamiliesWhereSpouse %s ",
                                        selectedIndi.getId(), fam.getId() ));
                            }
                            //  j'ajoute un modéle avec une nouvelle famille si le nom de l'epoux est différent entre le relevé et la famille trouvée
                            if (!mergeRecord.getIndi().getMarriedLastName().isEmpty() || !mergeRecord.getIndi().getMarriedFirstName().isEmpty()) {
                                if (selectedIndi.equals(fam.getHusband())) {
                                    if (!(MergeQuery.isSameLastName(fam.getWife().getLastName(), mergeRecord.getIndi().getMarriedLastName())
                                          && MergeQuery.isSameFirstName(fam.getWife().getFirstName(), mergeRecord.getIndi().getMarriedFirstName()))) {

                                        models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, (Fam) null));
                                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                            LOG.log(ACCEPT, String.format("ACCEPT selectedIndi %s with new family",
                                                    selectedIndi.getId() ));                                            
                                        }
                                    }

                                } else {
                                    if (!(MergeQuery.isSameLastName(fam.getHusband().getLastName(), mergeRecord.getIndi().getMarriedLastName())
                                          && MergeQuery.isSameFirstName(fam.getHusband().getFirstName(), mergeRecord.getIndi().getMarriedFirstName()))) {

                                        models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, (Fam) null));
                                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                            LOG.log(ACCEPT, String.format("ACCEPT selectedIndi %s with fam %s where husband %s different from ex married name%s",
                                                    selectedIndi.getId(), fam.getId(), fam.getHusband().getId(), mergeRecord.getIndi().getMarriedLastName() ));
                                        }
                                    }

                                }
                            }
                        } else {
                            if (LOG.isLoggable(MergeLogger.REFUSE)) {
                                LOG.log(REFUSE, String.format("REFUSE selectedIndi %s marriage %s date %s must be before deathDate %s",
                                        selectedIndi.getId(), fam.getId(), fam.getMarriageDate().getValue() , mergeRecord.getIndi().getDeathDate().getValue() ));
                            }                            
                        }
                    }

                    // j'ajoute l'individu selectionné par dnd avec les familles compatibles
                    for (Fam parentFamily : parentFamilies) {
                        models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, parentFamily));
                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                            LOG.log(ACCEPT, String.format("ACCEPT selectedIndi %s with compatible parentFamily %s ",
                                    selectedIndi.getId(), parentFamily.getId() ));
                        }
                    }
                }

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                // en excluant l'individu selectionne s'il a deja une famille
                List<Indi> sameIndis;
                if (selectedIndi.getFamilyWhereBiologicalChild() != null) {
                    // l'individu est lié a une famille précise, je l'exclue de la recherche
                    sameIndis = MergeQuery.findIndiCompatibleWithParticipant(mergeRecord, MergeParticipantType.participant1, gedcom, selectedIndi);
                } else {
                    // l'individu n'est pas lié a une famille précise, je l'inclue dans la recherche
                    sameIndis = MergeQuery.findIndiCompatibleWithParticipant(mergeRecord, MergeParticipantType.participant1, gedcom, null);
                }
                // j'ajoute les individus compatibles
                for (Indi sameIndi : sameIndis) {
                    // j'ajoute les familles compatibles
                    Fam sameIndiFamily = sameIndi.getFamilyWhereBiologicalChild();
                    if (sameIndiFamily != null) {
                        models.add(new MergeModelDeath(mergeRecord, gedcom, sameIndi, sameIndiFamily));
                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                            LOG.log(ACCEPT, String.format("ACCEPT sameIndi %s with compatible FamilyWhereBiologicalChild %s ",
                                    sameIndi.getId(), sameIndiFamily.getId() ));
                        }

                    } else {
                        if ( parentFamilies.size() > 0 ) {
                            for (Fam parentFamily : parentFamilies) {
                                models.add(new MergeModelDeath(mergeRecord, gedcom, sameIndi, parentFamily));
                                if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                    LOG.log(ACCEPT, String.format("ACCEPT sameIndi %s with compatible parentFamily %s ",
                                            sameIndi.getId(), parentFamily.getId() ));
                                }
                            }
                        } else {
                            // j'ajoute l'individu selectionné sans famille
                            models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, (Fam) null));
                            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                LOG.log(ACCEPT, String.format("ACCEPT selectedIndi %s without family",
                                        sameIndi.getId() ));
                            }
                        }
                    }
                }
            }

        } else {
            // 1.3) Record Death : pas d'entité selectionnee

            // j'ajoute un nouvel individu , sans famille associée
            models.add(new MergeModelDeath(mergeRecord, gedcom));
            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                LOG.log(ACCEPT, String.format("ACCEPT new indi without family" ));
            }

            // je recherche la famille avec l'ex conjoint
            List<Fam> marriedFamilies = MergeQuery.findFamilyCompatibleWithParticipantMarried(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            // je cherche les familles des parents compatibles avec le releve
            List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            if (!marriedFamilies.isEmpty()) {
                for (Fam marriedFamily : marriedFamilies) {
                    if (mergeRecord.getIndi().getSex() == PropertySex.MALE) {
                        Indi husband = marriedFamily.getHusband();
                        Fam husbandParentFamily = husband.getFamilyWhereBiologicalChild();
                        if (husbandParentFamily != null) {
                            models.add(new MergeModelDeath(mergeRecord, gedcom, husband, marriedFamily, husbandParentFamily));
                            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                LOG.log(ACCEPT, String.format("ACCEPT husband %s with compatible marriedFamily %s and husbandParentFamily %s" , 
                                        husband.getId(),  marriedFamily.getId(), husbandParentFamily.getId() ));
                            }
                        } else {     
                            models.add(new MergeModelDeath(mergeRecord, gedcom, husband, marriedFamily, null));
                            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                LOG.log(ACCEPT, String.format("ACCEPT husband %s with compatible marriedFamily %s" , 
                                        husband.getId(),  marriedFamily.getId() ));
                            }
                            // j'ajoute un nouvel individu avec les familles compatibles
                            for (Fam parentFamily : parentFamilies) {
                                models.add(new MergeModelDeath(mergeRecord, gedcom, husband, marriedFamily, parentFamily));
                                if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                    LOG.log(ACCEPT, String.format("ACCEPT husband %s with compatible marriedFamily %s and parentFamily %s",
                                            husband.getId(), marriedFamily.getId(), parentFamily.getId() ));
                                }                            
                            }
                        }
                    } else {
                        Indi wife = marriedFamily.getWife();
                        Fam wifeParentFamily = wife.getFamilyWhereBiologicalChild();
                        models.add(new MergeModelDeath(mergeRecord, gedcom, wife, marriedFamily, wifeParentFamily));
                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                            LOG.log(ACCEPT, String.format("ACCEPT wife %s with compatible marriedFamily %s and wifeParentFamily %s" , 
                                    wife,  marriedFamily, wifeParentFamily ));
                        }
                        if (wifeParentFamily == null) {
                            // j'ajoute un nouvel individu avec les familles compatibles
                            for (Fam parentFamily : parentFamilies) {
                                models.add(new MergeModelDeath(mergeRecord, gedcom, wife, marriedFamily, parentFamily));
                                if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                    LOG.log(ACCEPT, String.format("ACCEPT wife %s with compatible marriedFamily %s and parentFamily %s",
                                            wife, marriedFamily, parentFamily ));
                                }
                            }
                        }
                    }
                }

            } else {
                // il n'y a pas de famille pour de l'ex conjoint

                // j'ajoute un nouvel individu avec les familles compatibles
                for (Fam parentFamily : parentFamilies) {
                    models.add(new MergeModelDeath(mergeRecord, gedcom, null, parentFamily));
                    if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                        LOG.log(ACCEPT, String.format("ACCEPT new indi with parentFamily %s" , 
                                parentFamily.getId() ));
                    }                    
                }

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithParticipant(mergeRecord, MergeParticipantType.participant1, gedcom, null);

                // j'ajoute les individus compatibles avec la famille de chacun
                for (Indi sameIndi : sameIndis) {
                    Fam sameIndiFamily = sameIndi.getFamilyWhereBiologicalChild();
                    if (sameIndiFamily != null) {
                        // j'ajoute l'individus compatible avec sa famille
                        models.add(new MergeModelDeath(mergeRecord, gedcom, sameIndi, sameIndiFamily));
                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                            LOG.log(ACCEPT, String.format("ACCEPT sameIndi %s with compatible FamilyWhereBiologicalChild %s ",
                                    sameIndi.getId(), sameIndiFamily.getId() ));
                        }
                    } else {
                        // j'ajoute l'individus compatible sans famille
                        models.add(new MergeModelDeath(mergeRecord, gedcom, sameIndi, (Fam) null));
                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                            LOG.log(ACCEPT, String.format("ACCEPT sameIndi %s without family",
                                        sameIndi.getId() ));
                        }
                        // j'ajoute l'individus compatible avec les familles compatibles
                        for (Fam parentFamily : parentFamilies) {
                            models.add(new MergeModelDeath(mergeRecord, gedcom, sameIndi, parentFamily));
                            if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                                LOG.log(ACCEPT, String.format("ACCEPT sameIndi %s with compatible parentFamily %s ",
                                        sameIndi.getId(), parentFamily.getId() ));
                            }
                        }
                    }
                }
            }

            if (showNewParents) {
                // j'ajoute un nouvel individu avec les couples qui ne sont pas des familles
                // mais qui pourraient être ses parents
                List<Indi> fathers = new ArrayList<Indi>();
                List<Indi> mothers = new ArrayList<Indi>();
                MergeQuery.findFatherMotherCompatibleWithBirthParticipant(mergeRecord, gedcom, parentFamilies, fathers, mothers);
                for (Indi father : fathers) {
                    for (Indi mother : mothers) {
                        models.add(new MergeModelDeath(mergeRecord, gedcom, father, mother));
                        if (LOG.isLoggable(MergeLogger.ACCEPT)) {
                            LOG.log(ACCEPT, String.format("ACCEPT new indi with new family father %s mother %s ",
                                    father.getId(), mother.getId() ));
                        }                   
                    }
                }
            }

        }
        if (LOG.isLoggable(Level.FINER)){
            LOG.exiting(MergeModelDeath.class.getName(), "createMergeModelDeath");   
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
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom) throws Exception {
        this(record, gedcom, null , null, null);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve
     *
     * @param indi
     * @param record
     */
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom, Indi indi, Fam parentfam) throws Exception {
        this(record, gedcom, indi , null, parentfam);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve
     *
     * @param record
     * @param gedcom
     * @param indi
     * @param marriedFamily famille avec l'ex conjoint
     * @param parentFamily  famille parent de l'individu
     */
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom, Indi indi, Fam marriedFamily, Fam parentFamily) throws Exception {
        super(record, indi, gedcom);
        this.currentIndi = indi;
        addRowSource();                
        addRowIndi();
        addRowMarried(marriedFamily);
        addRowParents(parentFamily);

    }
    
    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve
     *
     * @param indi
     * @param record
     */
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom, Indi father, Indi mother ) throws Exception {
        super(record, null, gedcom);
        this.currentIndi = null;
        
        addRowSource();                
        addRowIndi();
        // j'affiche l'ex conjoint
        addRowMarried(null);
        // j'affiche la famille de l'enfant
        addRowParents(father, mother);

    }
    
    private void addRowIndi() throws Exception {

       if (currentIndi != null) {
            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndi().getLastName(), currentIndi.getLastName(), currentIndi);
            addRow(RowType.IndiFirstName, record.getIndi().getFirstName(), currentIndi.getFirstName());
            addRowSex(RowType.IndiSex, record.getIndi().getSex(), currentIndi.getSex(), currentIndi);
            addRow(RowType.IndiBirthDate, record.getIndi().getBirthDate() , currentIndi.getBirthDate());
            addRow(RowType.IndiBirthPlace, record.getIndi().getBirthPlace(), record.getIndi().getBirthAddress(), 
                    currentIndi.getValue(new TagPath("INDI:BIRT:PLAC"), ""), currentIndi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            addRow(RowType.IndiDeathDate, record.getIndi().getDeathDate() , currentIndi.getDeathDate());
            addRow(RowType.IndiOccupation, record.getIndi().getOccupationWithDate(), MergeQuery.findOccupation(currentIndi, record.getEventDate()));
            addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), currentIndi.getValue(new TagPath("INDI:DEAT:NOTE"), ""));

        } else {
            // selectedIndi est nul
            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndi().getLastName(), "", null);
            addRow(RowType.IndiFirstName, record.getIndi().getFirstName(), "");
            addRowSex(RowType.IndiSex, record.getIndi().getSex(), PropertySex.UNKNOWN, null);
            addRow(RowType.IndiBirthDate, record.getIndi().getBirthDate() , null);
            addRow(RowType.IndiBirthPlace, record.getIndi().getBirthPlace(), record.getIndi().getBirthAddress(), "", "");
            addRow(RowType.IndiDeathDate, record.getIndi().getDeathDate() , null);
            addRow(RowType.IndiOccupation, record.getIndi().getOccupationWithDate(), "");
            addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), "");
        }
    }

    private void addRowParents( Fam fam) throws Exception {
        addRowSeparator();
        addRow(RowType.IndiParentFamily, fam);
        if (fam != null) {
            addRow(RowType.IndiParentMarriageDate, record.getIndi().getParentMarriageDate(), fam.getMarriageDate());

            addRowFather(fam.getHusband());
            addRowMother(fam.getWife());
        } else {
            addRow(RowType.IndiParentMarriageDate, record.getIndi().getParentMarriageDate(), null);
            addRowFather(null);
            addRowMother(null);
        }
    }

    private void addRowParents( Indi father, Indi mother) throws Exception {
        addRowSeparator();
        addRow(RowType.IndiParentFamily, null);
        addRow(RowType.IndiParentMarriageDate, record.getIndi().getParentMarriageDate(), null);
        // j'affiche les parents
        addRowFather(father);
        addRowMother(mother);
    }

    private void addRowFather( Indi father ) throws Exception {
        if (father != null) {
            addRow(RowType.IndiFatherLastName, record.getIndi().getFatherLastName(), father.getLastName(), father);
            addRow(RowType.IndiFatherFirstName, record.getIndi().getFatherFirstName(), father.getFirstName());
            addRow(RowType.IndiFatherBirthDate, record.getIndi().getFatherBirthDate(), father.getBirthDate());
            addRow(RowType.IndiFatherDeathDate, record.getIndi().getFatherDeathDate(), father.getDeathDate());
            addRow(RowType.IndiFatherOccupation, record.getIndi().getFatherOccupationWithDate(), MergeQuery.findOccupation(father, record.getEventDate()));
        } else {
            addRow(RowType.IndiFatherLastName, record.getIndi().getFatherLastName(), "");
            addRow(RowType.IndiFatherFirstName, record.getIndi().getFatherFirstName(), "");
            addRow(RowType.IndiFatherBirthDate, record.getIndi().getFatherBirthDate(), null);
            addRow(RowType.IndiFatherDeathDate, record.getIndi().getFatherDeathDate(), null);
            addRow(RowType.IndiFatherOccupation, record.getIndi().getFatherOccupationWithDate(), "");
        }
    }

    private void addRowMother( Indi mother ) throws Exception {
        if (mother != null) {
            addRow(RowType.IndiMotherLastName, record.getIndi().getMotherLastName(), mother.getLastName(), mother);
            addRow(RowType.IndiMotherFirstName, record.getIndi().getMotherFirstName(), mother.getFirstName());
            addRow(RowType.IndiMotherBirthDate, record.getIndi().getMotherBirthDate(), mother.getBirthDate());
            addRow(RowType.IndiMotherDeathDate, record.getIndi().getMotherDeathDate(), mother.getDeathDate());
            addRow(RowType.IndiMotherOccupation, record.getIndi().getMotherOccupationWithDate(), MergeQuery.findOccupation(mother, record.getEventDate()));
        } else {
            addRow(RowType.IndiMotherLastName, record.getIndi().getMotherLastName(), "");
            addRow(RowType.IndiMotherFirstName, record.getIndi().getMotherFirstName(), "");
            addRow(RowType.IndiMotherBirthDate, record.getIndi().getMotherBirthDate(), null);
            addRow(RowType.IndiMotherDeathDate, record.getIndi().getMotherDeathDate(), null);
            addRow(RowType.IndiMotherOccupation, record.getIndi().getMotherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations sur l'ex conjoint
     * @param marriedFamily
     * @throws Exception
     */
    private void addRowMarried( Fam marriedFamily ) throws Exception {
        if (! record.getIndi().getMarriedLastName().isEmpty()
              || ! record.getIndi().getMarriedFirstName().isEmpty()
              || (marriedFamily != null && marriedFamily.getMarriageDate()!= null) ) {
            
            // j'affiche un separateur
            addRowSeparator();
            addRow(RowType.IndiMarriedFamily, marriedFamily);

            if (marriedFamily != null) {
                addRow(RowType.IndiMarriedMarriageDate, record.getIndi().getMarriedMarriageDate(), marriedFamily.getMarriageDate());

                Indi married;
                if ( currentIndi.getSex() == PropertySex.MALE) {
                    married = marriedFamily.getWife();
                } else {
                    married = marriedFamily.getHusband();
                }
                addRow(RowType.IndiMarriedLastName, record.getIndi().getMarriedLastName(), married.getLastName(), married);
                addRow(RowType.IndiMarriedFirstName, record.getIndi().getMarriedFirstName(), married.getFirstName());
                addRow(RowType.IndiMarriedBirthDate, record.getIndi().getMarriedBirthDate(), married.getBirthDate());
                addRow(RowType.IndiMarriedDeathDate, record.getIndi().getMarriedDeathDate(), married.getDeathDate());
                addRow(RowType.IndiMarriedOccupation, record.getIndi().getMarriedOccupationWithDate(), MergeQuery.findOccupation(married, record.getEventDate()));
            } else {
                addRow(RowType.IndiMarriedMarriageDate, record.getIndi().getMarriedMarriageDate(),null);
                addRow(RowType.IndiMarriedLastName, record.getIndi().getMarriedLastName(), "", null);
                addRow(RowType.IndiMarriedFirstName, record.getIndi().getMarriedFirstName(), "");
                addRow(RowType.IndiMarriedBirthDate, record.getIndi().getMarriedBirthDate(), null);
                addRow(RowType.IndiMarriedDeathDate, record.getIndi().getMarriedDeathDate(), null);
                addRow(RowType.IndiMarriedOccupation, record.getIndi().getMarriedOccupationWithDate(), "");
            }
            
        }
    }
    
    /**
     * retourne l'individu proposé dans le modele
     * @return 
     */
    @Override
    protected Entity getProposedEntity() {
        if (currentIndi != null)  {
            return currentIndi;
        } else {
            Entity parentFamily = getEntityObject(MergeModel.RowType.IndiParentFamily);
            if (parentFamily != null) {
                return parentFamily;
            } else {
                Entity marriedFamily = getEntityObject(RowType.IndiMarriedFamily);
                if (marriedFamily != null) {
                    return marriedFamily;
                } else {
                    return null;
                }
            }

        }
    }

    /**
     * retoune l'individu selectionné
     * @return individu selectionné ou null si c'est un nouvel individu
     */
    @Override
    protected Entity getSelectedEntity() {
        return currentIndi;
    }
    
    /**
     * copie les données du relevé dans l'entité
     */
    @Override
    protected Property copyRecordToEntity() throws Exception {
        Property resultProperty;
        
        if (currentIndi == null) {
            currentIndi = (Indi) gedcom.createEntity(Gedcom.INDI);
            currentIndi.setName(record.getIndi().getFirstName(), record.getIndi().getLastName());
            currentIndi.setSex(record.getIndi().getSex());
        } else {
            // je copie le nom du releve dans l'individu
            if (isChecked(RowType.IndiLastName)) {
                currentIndi.setName(currentIndi.getFirstName(), record.getIndi().getLastName());
            }

            // je copie le prénom du releve dans l'individu
            if (isChecked(RowType.IndiFirstName)) {
                currentIndi.setName(record.getIndi().getFirstName(), currentIndi.getLastName());
            }

            // je copie le sexe du releve dans l'individu
            if (isChecked(RowType.IndiSex)) {
                currentIndi.setSex(record.getIndi().getSex());
            }
        }
        
        resultProperty = currentIndi;

        // je copie la date, le lieu et le commentaire de naissance du releve dans l'individu
        if (isChecked(RowType.IndiBirthDate) || isChecked(RowType.IndiBirthPlace)) {
            copyBirthDate(currentIndi, isChecked(RowType.IndiBirthDate), isChecked(RowType.IndiBirthPlace), 
                    record.getIndi().getBirthDate(), record.getIndi().getBirthPlace(), record.getIndi().getBirthAddress(), record);
        }

        // je copie la profession et la residence de l'individu
        if (isChecked(RowType.IndiOccupation)) {
            copyOccupation(currentIndi, record.getIndi().getOccupation(), record.getIndi().getResidence(), record.getIndi().getAddress(), false, record);
        }

        // je cree la propriete de deces si elle n'existait pas
        if (isChecked(RowType.IndiDeathDate) || isChecked(RowType.EventSource) || isChecked(RowType.EventComment)) {
            Property deathProperty = currentIndi.getProperty("DEAT");
            if (deathProperty == null) {
                deathProperty = currentIndi.addProperty("DEAT", "");
            }
            
            resultProperty = deathProperty;

            // je copie la date de deces du releve dans l'individu
            if (isChecked(RowType.IndiDeathDate)) {
                // j'ajoute (ou remplace) la date de la décès
                PropertyDate propertyDate = currentIndi.getDeathDate(true);
                propertyDate.setValue(record.getIndi().getDeathDate().getValue());
            }

            // je copie le lieu de l'acte de deces
            copyPlace(record.getIndi().getDeathPlace(), record.getIndi().getAddress(), deathProperty);
            
            // je copie la source du deces du releve dans l'individu
            if (isChecked(RowType.EventSource) || isChecked(RowType.EventPage) ) {
                copySource((Source) getEntityObject(RowType.EventSource), deathProperty, isChecked(RowType.EventPage), record);
            }

            // je copie le commentaire du deces
            if (isChecked(RowType.EventComment)) {
                Property propertyNote = deathProperty.getProperty("NOTE");
                if (propertyNote == null) {
                    // je cree une note .
                    propertyNote = deathProperty.addProperty("NOTE", "");
                }

                // j'ajoute le commentaire du deces au debut de la note existante.
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

        // je copie les données de l'ex conjoint
        copyIndiMarried(record.getIndi(), currentIndi);
        
        // je copie les données des parents
        if (isChecked(RowType.IndiParentFamily)) {
            // je copie la famille des parents
            Fam family = (Fam) getEntityObject(RowType.IndiParentFamily);
            if (family == null) {
                // je cree la famille
                family = (Fam) gedcom.createEntity(Gedcom.FAM);
            }

            // j'ajoute l'enfant dans la famille si ce n'est pas déja le cas
            if (!currentIndi.isDescendantOf(family)) {
                family.addChild(currentIndi);
            }

            // je copie la date du mariage et une note indiquant l'origine de cette date
            if (isChecked(RowType.IndiParentMarriageDate)) {                
                copyMarriageDate(family, getRow(RowType.IndiParentMarriageDate), record );
            }

            // je copie les parents
            copyIndiFather(record.getIndi(), family.getHusband(), family);
            copyIndiMother(record.getIndi(), family.getWife(), family);
         
        }
        return resultProperty;
    }

    /**
     * retourne une chaine de caracteres contenant le nom, prénom et la date du relevé
     * Cette chaine sert de titre a la fenetre principale
     * @return
     */
    @Override
    protected String getTitle() {
        String message = record.getIndi().getFirstName() + " "+ record.getIndi().getLastName()+ " " + record.getEventDate().getDisplayValue();
        return MessageFormat.format(NbBundle.getMessage(MergeDialog.class, "MergeModel.title.death"), message);
    }

    /**
     * retourne un resumé du modele
     * Cette chaine sert de commentaire dans la liste des modeles
     * @return
     */
    @Override
    public String getSummary(Entity selectedEntity) {
        String summary;
        if ( currentIndi == null ) {
                summary = "Nouvel individu" + " - ";
                if (getEntityObject(MergeModel.RowType.IndiParentFamily) != null) {
                    summary += getEntityObject(MergeModel.RowType.IndiParentFamily).toString(false);
                } else {
                    if (getEntityObject(MergeModel.RowType.IndiFatherLastName) != null
                            || getEntityObject(MergeModel.RowType.IndiMotherLastName) != null) {
                        summary += "Nouveau couple:" + " ";
                        if (getEntityObject(MergeModel.RowType.IndiFatherLastName) != null) {
                            summary += getEntityObject(MergeModel.RowType.IndiFatherLastName).toString(true);
                        } else {
                            summary += "Nouveau père";
                        }
                        summary += " , ";
                        if (getEntityObject(MergeModel.RowType.IndiMotherLastName) != null) {
                            summary += getEntityObject(MergeModel.RowType.IndiMotherLastName).toString(true);
                        } else {
                            summary += "Nouvelle mère";
                        }
                    } else {
                        if( isChecked(MergeModel.RowType.IndiParentFamily)) {
                            summary += "Nouveaux parents";
                        } else {
                            summary += "Sans parents";
                        }
                    }
                }
        } else {
            if (selectedEntity instanceof Fam) {
                summary = "Nouvel enfant de la famille sélectionnée";
            } else {
                summary = "Modifier " + currentIndi.toString(true) + ", ";

                if (getEntityObject(MergeModel.RowType.IndiParentFamily) != null) {
                    summary += getEntityObject(MergeModel.RowType.IndiParentFamily).toString(false);
                } else {
                    if (getEntityObject(MergeModel.RowType.IndiFatherLastName) != null
                            || getEntityObject(MergeModel.RowType.IndiMotherLastName) != null) {
                        summary += "Nouveau couple:" + " ";
                        if (getEntityObject(MergeModel.RowType.IndiFatherLastName) != null) {
                            summary += getEntityObject(MergeModel.RowType.IndiFatherLastName).toString(true);
                        } else {
                            summary += "Nouveau père";
                        }
                        summary += ", ";
                        if (getEntityObject(MergeModel.RowType.IndiMotherLastName) != null) {
                            summary += getEntityObject(MergeModel.RowType.IndiMotherLastName).toString(true);
                        } else {
                            summary += "Nouvelle mère";
                        }
                    } else {
                        if( isChecked(MergeModel.RowType.IndiParentFamily)) {
                            summary += "Nouveaux parents";
                        } else {
                            summary += "Sans parents";
                        }
                    }
                }
            }
        }
        return summary;
    }

}
