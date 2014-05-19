package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.MergeRecord.MergeParticipantType;
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
import org.openide.util.NbBundle;

/**
 *
 */
class MergeModelMiscOther extends MergeModel {

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
    static protected List<MergeModel> createMergeModelMiscOther (MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity, boolean showNewParents) throws Exception {
        List<MergeModel> models = new ArrayList<MergeModel>();

        // je recherche les propositions concernant le participant 1
        if (selectedEntity instanceof Fam) {
            // 1) l'entité selectionnée est une famille pour le participant 1
            Fam family = (Fam) selectedEntity;
            // j'ajoute un nouvel individu
            models.add(new MergeModelMiscOther(mergeRecord, gedcom, null, family));
            // je recherche les enfants de la famille sélectionnée compatibles avec le releve
            List<Indi> sameChildren = MergeQuery.findSameChild(mergeRecord, gedcom, family);
            // j'ajoute les enfants compatibles
            for (Indi samedIndi : sameChildren) {
                models.add(new MergeModelMiscOther(mergeRecord, gedcom, samedIndi, samedIndi.getFamilyWhereBiologicalChild()));
            }
        } else if (selectedEntity instanceof Indi) {
            // 2) l'entité selectionnée est un individu pour le participant 1
            Indi selectedIndi = (Indi) selectedEntity;

            // je recherche la famille avec l'ex conjoint
            List<Fam> marriedFamilies = MergeQuery.findFamilyCompatibleWithParticipantMarried(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            // je cherche les familles des parents compatibles avec le releve
            List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            if (!marriedFamilies.isEmpty()) {
                for (Fam family : marriedFamilies) {
                    if (mergeRecord.getIndi().getSex() == PropertySex.MALE) {
                        Indi husband = family.getHusband();
                        if (selectedIndi.compareTo(husband) == 0) {
                            Fam husbandParentFamily = husband.getFamilyWhereBiologicalChild();
                            models.add(new MergeModelMiscOther(mergeRecord, gedcom, husband, family, husbandParentFamily));

                            if (husbandParentFamily == null) {
                                // j'ajoute un nouvel individu avec les familles compatibles
                                for (Fam parentFamily : parentFamilies) {
                                    models.add(new MergeModelMiscOther(mergeRecord, gedcom, husband, family, parentFamily));
                                }
                            }
                        }
                    } else {
                        Indi wife = family.getWife();
                        if (selectedIndi.compareTo(wife) == 0) {
                            Fam wifeParentFamily = wife.getFamilyWhereBiologicalChild();
                            models.add(new MergeModelMiscOther(mergeRecord, gedcom, wife, family, wifeParentFamily));

                            if (wifeParentFamily == null) {
                                // j'ajoute un nouvel individu avec les familles compatibles
                                for (Fam parentFamily : parentFamilies) {
                                    models.add(new MergeModelMiscOther(mergeRecord, gedcom, wife, family, parentFamily));
                                }
                            }
                        }
                    }
                }
            } else {
                // l'individu n'a pas d'ex conjoint

                // je cherche les familles compatibles avec le releve de deces
                List<Fam> families = MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

                // j'ajoute l'individu selectionné par dnd
                if (selectedIndi.getFamilyWhereBiologicalChild() != null) {
                    // j'ajoute l'individu selectionné par dnd
                    models.add(new MergeModelMiscOther(mergeRecord, gedcom, selectedIndi, selectedIndi.getFamilyWhereBiologicalChild()));
                } else {
                    models.add(new MergeModelMiscOther(mergeRecord, gedcom, selectedIndi, (Fam) null));
                    // j'ajoute l'individu selectionné par dnd avec les familles compatibles
                    for (Fam family : families) {
                        models.add(new MergeModelMiscOther(mergeRecord, gedcom, selectedIndi, family));
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
                for (Indi samedIndi : sameIndis) {
                    // j'ajoute les familles compatibles
                    Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild();
                    if (sameIndiFamily != null) {
                        models.add(new MergeModelMiscOther(mergeRecord, gedcom, samedIndi, sameIndiFamily));
                    } else {
                        for (Fam family : families) {
                            models.add(new MergeModelMiscOther(mergeRecord, gedcom, samedIndi, family));
                        }
                    }
                }
            }

        } else {
            // 3) pas d'entité selectionnee pour le participant 1

            // j'ajoute un nouvel individu , sans famille associée
            models.add(new MergeModelMiscOther(mergeRecord, gedcom));

            // je recherche la famille avec l'ex conjoint
            List<Fam> marriedFamilies = MergeQuery.findFamilyCompatibleWithParticipantMarried(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            // je cherche les familles des parents compatibles avec le releve
            List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeRecord.MergeParticipantType.participant1, gedcom);

            if (!marriedFamilies.isEmpty()) {
                for (Fam family : marriedFamilies) {
                    if (mergeRecord.getIndi().getSex() == PropertySex.MALE) {
                        Indi husband = family.getHusband();
                        Fam husbandParentFamily = husband.getFamilyWhereBiologicalChild();
                        models.add(new MergeModelMiscOther(mergeRecord, gedcom, husband, family, husbandParentFamily));

                        if (husbandParentFamily == null) {
                            // j'ajoute un nouvel individu avec les familles compatibles
                            for (Fam parentFamily : parentFamilies) {
                                models.add(new MergeModelMiscOther(mergeRecord, gedcom, husband, family, parentFamily));
                            }
                        }
                    } else {
                        Indi wife = family.getWife();
                        Fam wifeParentFamily = wife.getFamilyWhereBiologicalChild();
                        models.add(new MergeModelMiscOther(mergeRecord, gedcom, wife, family, wifeParentFamily));

                        if (wifeParentFamily == null) {
                            // j'ajoute un nouvel individu avec les familles compatibles
                            for (Fam parentFamily : parentFamilies) {
                                models.add(new MergeModelMiscOther(mergeRecord, gedcom, wife, family, parentFamily));
                            }
                        }
                    }
                }

            } else {
                // il n'y a pas de famille pour de l'ex conjoint

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithParticipant(mergeRecord, MergeParticipantType.participant1, gedcom, null);

                // j'ajoute un nouvel individu avec les familles compatibles
                for (Fam family : parentFamilies) {
                    models.add(new MergeModelMiscOther(mergeRecord, gedcom, null, family));
                }

                // j'ajoute les individus compatibles avec la famille de chacun
                for (Indi samedIndi : sameIndis) {
                    Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild();
                    if (sameIndiFamily != null) {
                        // j'ajoute l'individus compatible avec sa famille
                        models.add(new MergeModelMiscOther(mergeRecord, gedcom, samedIndi, sameIndiFamily));
                    } else {
                        // j'ajoute l'individus compatible sans famille
                        models.add(new MergeModelMiscOther(mergeRecord, gedcom, samedIndi, (Fam) null));
                        // j'ajoute l'individus compatible avec les familles compatibles
                        for (Fam family : parentFamilies) {
                            models.add(new MergeModelMiscOther(mergeRecord, gedcom, samedIndi, family));
                        }
                    }
                }
            }

//            if (showNewParents) {
//                // j'ajoute un nouvel individu en formant des couples qui ne sont pas des familles
//                // mais qui pourraient être ses parents
//                List<Indi> fathers = new ArrayList<Indi>();
//                List<Indi> mothers = new ArrayList<Indi>();
//                MergeQuery.findFatherMotherCompatibleWithBirthParticipant(mergeRecord, gedcom, parentFamilies, fathers, mothers);
//                for (Indi father : fathers) {
//                    for (Indi mother : mothers) {
//                        models.add(new MergeModelMiscOther(mergeRecord, gedcom, father, mother));
//                    }
//                }
//            }

        }

        // 4)  pas d'entité selectionnee pour le participant 2
        // je recherche les propositions concernant le participant 2
        if (!mergeRecord.getWife().getLastName().isEmpty() || !mergeRecord.getWife().getFirstName().isEmpty()) {

            // j'ajoute un nouvel individu , sans famille associée
            models.add(new MergeModelMiscOther(mergeRecord, MergeParticipantType.participant2,gedcom));

            // je recherche la famille avec l'ex conjoint
            List<Fam> marriedFamilies = MergeQuery.findFamilyCompatibleWithParticipantMarried(mergeRecord, MergeParticipantType.participant2, gedcom);

            // je cherche les familles des parents de participant2 compatibles avec le releve
            List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithParticipantParents(mergeRecord, MergeParticipantType.participant2, gedcom);

            if (!marriedFamilies.isEmpty()) {
                for (Fam family : marriedFamilies) {
                    Indi husband = family.getHusband();
                    Fam husbandParentFamily = husband.getFamilyWhereBiologicalChild();
                    models.add(new MergeModelMiscOther(mergeRecord, MergeParticipantType.participant2, gedcom, husband, family, husbandParentFamily));

                    if (husbandParentFamily == null) {
                        // j'ajoute un nouvel individu avec les familles compatibles
                        for (Fam parentFamily : parentFamilies) {
                            models.add(new MergeModelMiscOther(mergeRecord, MergeParticipantType.participant2, gedcom, husband, family, parentFamily));
                        }
                    }
                    
                }

            } else {
                // participant 2 : il n'y a pas de famille pour de l'ex conjoint

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithParticipant(mergeRecord, MergeParticipantType.participant2, gedcom, null);

                // j'ajoute un nouvel individu avec les familles compatibles
                for (Fam family : parentFamilies) {
                    models.add(new MergeModelMiscOther(mergeRecord, MergeParticipantType.participant2, gedcom, null, family));
                }

                // j'ajoute les individus compatibles avec la famille de chacun
                for (Indi samedIndi : sameIndis) {
                    Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild();
                    if (sameIndiFamily != null) {
                        // j'ajoute l'individus compatible avec sa famille
                        models.add(new MergeModelMiscOther(mergeRecord, MergeParticipantType.participant2, gedcom, samedIndi, sameIndiFamily));
                    } else {
                        // j'ajoute l'individus compatible sans famille
                        models.add(new MergeModelMiscOther(mergeRecord, MergeParticipantType.participant2, gedcom, samedIndi, (Fam) null));
                        // j'ajoute l'individus compatible avec les familles compatibles
                        for (Fam family : parentFamilies) {
                            models.add(new MergeModelMiscOther(mergeRecord, MergeParticipantType.participant2, gedcom, samedIndi, family));
                        }
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
    protected MergeModelMiscOther(MergeRecord record, Gedcom gedcom) throws Exception {
        this(record, MergeRecord.MergeParticipantType.participant1, gedcom);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve
     *
     * @param indi
     * @param record
     */
    protected MergeModelMiscOther(MergeRecord record, Gedcom gedcom, Indi indi, Fam parentfam) throws Exception {
        this(record, MergeRecord.MergeParticipantType.participant1, gedcom, indi, parentfam);
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
    protected MergeModelMiscOther( MergeRecord record, Gedcom gedcom, Indi indi, Fam marriedFamily, Fam parentFamily) throws Exception {
        this(record, MergeRecord.MergeParticipantType.participant1, gedcom, indi, marriedFamily, parentFamily);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    protected MergeModelMiscOther(MergeRecord record, MergeParticipantType participantType, Gedcom gedcom) throws Exception {
        this(record, participantType, gedcom, null, null, null);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve
     *
     * @param indi
     * @param record
     */
    protected MergeModelMiscOther(MergeRecord record, MergeParticipantType participantType, Gedcom gedcom, Indi indi, Fam parentfam) throws Exception {
        this(record, participantType, gedcom, indi, null, parentfam);        
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
    protected MergeModelMiscOther(MergeRecord record, MergeParticipantType participantType, Gedcom gedcom, Indi indi, Fam marriedFamily, Fam parentFamily) throws Exception {
        super(record, participantType, gedcom);
        addRowSource();
        if( marriedFamily != null && participant.getSex()== PropertySex.FEMALE) {
            // l'epouse est affichée en premier
            addRowEvent(indi);
            addRowIndi(marriedFamily.getWife());
            addRowMarried(marriedFamily);
            addRowParents(parentFamily);
        } else {
            addRowEvent(indi);
            addRowIndi(indi);
            addRowMarried(marriedFamily);
            addRowParents(parentFamily);
        }
    }
    
    /**
     * ajoute l'evenement s'il s'agit du participant 1
     * @param currentIndi
     * @throws Exception
     */
    private void addRowEvent(Indi currentIndi) throws Exception {

       if (currentIndi != null) {
           if (participantType == MergeParticipantType.participant1) {
               // participant 1
               // je recherche la source de l'évènement deja existant
               Property eventProperty = MergeQuery.findPropertyEvent(currentIndi,record.getEventType(),record.getEventDate());
               // j'affiche la date, le lieu et les commentaires de l'évènement
               addRow(RowType.EventType, record.getEventType(), eventProperty);
               addRow(RowType.EventDate, record.getEventDate(), eventProperty != null ? (PropertyDate) eventProperty.getProperty("DATE") : null);
               addRow(RowType.EventPlace, record.getEventPlace(), eventProperty != null ? eventProperty.getPropertyValue("PLAC") : "");
               addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), eventProperty != null ? eventProperty.getPropertyValue("NOTE") : "");
               addRowSeparator();
           } else {
               // participant 2
               // rien à faire
           }

        } else {
            // selectedIndi est nul

            if (participantType == MergeParticipantType.participant1) {
               // j'affiche la date, le lieu et les commentaires de l'évènement
               addRow(RowType.EventDate, record.getEventDate(), null);
               addRow(RowType.EventPlace, record.getEventPlace(), "");
               addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), "");
               addRowSeparator();
            } else {
               // participant 2
               // rien à faire
           }
        }
    }

    private void addRowIndi(Indi currentIndi) throws Exception {

       if (currentIndi != null) {
            // j'affiche les informations de l'individu
            addRow(RowType.IndiLastName, participant.getLastName(), currentIndi.getLastName(), currentIndi);
            addRow(RowType.IndiFirstName, participant.getFirstName(), currentIndi.getFirstName());
            addRow(RowType.IndiSex, participant.getSexString(), currentIndi.getPropertyValue("SEX"));
            addRow(RowType.IndiBirthDate, participant.getBirthDate() , currentIndi.getBirthDate());
            addRow(RowType.IndiBirthPlace, participant.getBirthPlace(), currentIndi.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.IndiDeathDate, participant.getDeathDate() , currentIndi.getDeathDate());
            addRow(RowType.IndiOccupation, participant.getOccupationWithDate(), MergeQuery.findOccupation(currentIndi, record.getEventDate()));

        } else {
            // selectedIndi est nul
            addRow(RowType.IndiLastName, participant.getLastName(), "");
            addRow(RowType.IndiFirstName, participant.getFirstName(), "");
            addRow(RowType.IndiSex, participant.getSexString(), "");
            addRow(RowType.IndiBirthDate, participant.getBirthDate() , null);
            addRow(RowType.IndiBirthPlace, participant.getBirthPlace(), "");
            addRow(RowType.IndiDeathDate, participant.getDeathDate() , null);
            addRow(RowType.IndiOccupation, participant.getOccupationWithDate(), "");
        }
    }

    private void addRowParents( Fam fam) throws Exception {
        addRowSeparator();
        addRow(RowType.IndiParentFamily, record, fam);
        if (fam != null) {
            addRow(RowType.IndiParentMarriageDate, participant.getParentMarriageDate(), fam.getMarriageDate());
            addRowFather(fam.getHusband());
            addRowMother(fam.getWife());
        } else {
            addRow(RowType.IndiParentMarriageDate, participant.getParentMarriageDate(), null);
            addRowFather(null);
            addRowMother(null);
        }
    }

    private void addRowFather( Indi father ) throws Exception {
        if (father != null) {
            addRow(RowType.IndiFatherLastName, participant.getFatherLastName(), father.getLastName(), father);
            addRow(RowType.IndiFatherFirstName, participant.getFatherFirstName(), father.getFirstName());
            addRow(RowType.IndiFatherBirthDate, participant.getFatherBirthDate(), father.getBirthDate());
            addRow(RowType.IndiFatherDeathDate, participant.getFatherDeathDate(), father.getDeathDate());
            addRow(RowType.IndiFatherOccupation, participant.getFatherOccupationWithDate(), MergeQuery.findOccupation(father, record.getEventDate()));
        } else {
            addRow(RowType.IndiFatherLastName, participant.getFatherLastName(), "");
            addRow(RowType.IndiFatherFirstName, participant.getFatherFirstName(), "");
            addRow(RowType.IndiFatherBirthDate, participant.getFatherBirthDate(), null);
            addRow(RowType.IndiFatherDeathDate, participant.getFatherDeathDate(), null);
            addRow(RowType.IndiFatherOccupation, participant.getFatherOccupationWithDate(), "");
        }
    }

    private void addRowMother( Indi mother ) throws Exception {
        if (mother != null) {
            addRow(RowType.IndiMotherLastName, participant.getMotherLastName(), mother.getLastName(), mother);
            addRow(RowType.IndiMotherFirstName, participant.getMotherFirstName(), mother.getFirstName());
            addRow(RowType.IndiMotherBirthDate, participant.getMotherBirthDate(), mother.getBirthDate());
            addRow(RowType.IndiMotherDeathDate, participant.getMotherDeathDate(), mother.getDeathDate());
            addRow(RowType.IndiMotherOccupation, participant.getMotherOccupationWithDate(), MergeQuery.findOccupation(mother, record.getEventDate()));
        } else {
            addRow(RowType.IndiMotherLastName, participant.getMotherLastName(), "");
            addRow(RowType.IndiMotherFirstName, participant.getMotherFirstName(), "");
            addRow(RowType.IndiMotherBirthDate, participant.getMotherBirthDate(), null);
            addRow(RowType.IndiMotherDeathDate, participant.getMotherDeathDate(), null);
            addRow(RowType.IndiMotherOccupation, participant.getMotherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations sur l'ex conjoint
     * @param marriedFamily
     * @throws Exception
     */
    private void addRowMarried(Fam marriedFamily ) throws Exception {
        if (! participant.getMarriedLastName().isEmpty() || ! participant.getMarriedFirstName().isEmpty()) {
            // j'affiche un separateur
            addRowSeparator();
            addRow(RowType.IndiMarriedFamily, record, marriedFamily);

            if (marriedFamily != null) {
                addRow(RowType.IndiMarriedMarriageDate, participant.getMarriedMarriageDate(), marriedFamily.getMarriageDate());

                Indi married;
                if ( participant.getSex() == PropertySex.MALE) {
                    married = marriedFamily.getWife();
                } else {
                    married = marriedFamily.getHusband();
                }
                addRow(RowType.IndiMarriedLastName, participant.getMarriedLastName(), married.getLastName(), married);
                addRow(RowType.IndiMarriedFirstName, participant.getMarriedFirstName(), married.getFirstName());
                addRow(RowType.IndiMarriedBirthDate, participant.getMarriedBirthDate(), married.getBirthDate());
                addRow(RowType.IndiMarriedDeathDate, participant.getMarriedDeathDate(), married.getDeathDate());
                addRow(RowType.IndiMarriedOccupation, participant.getMarriedOccupationWithDate(), MergeQuery.findOccupation(married, record.getEventDate()));
            } else {
                addRow(RowType.IndiMarriedMarriageDate, participant.getMarriedMarriageDate(),null);
                addRow(RowType.IndiMarriedLastName, participant.getMarriedLastName(), "", null);
                addRow(RowType.IndiMarriedFirstName, participant.getMarriedFirstName(), "");
                addRow(RowType.IndiMarriedBirthDate, participant.getMarriedBirthDate(), null);
                addRow(RowType.IndiMarriedDeathDate, participant.getMarriedDeathDate(), null);
                addRow(RowType.IndiMarriedOccupation, participant.getMarriedOccupationWithDate(), "");
            }            
        }
    }

    /**
     * retoune l'individu selectionné
     * @return individu selectionné ou null si c'est un nouvel individu
     */
    @Override
    protected Entity getSelectedEntity() {
        if (participantType == MergeParticipantType.participant1) {
            MergeRow mergeRow = getRow(RowType.IndiLastName);
            if ( mergeRow != null ) {
                return mergeRow.entityObject;
            } else {
                return null;
            }
        } else {
            MergeRow mergeRow = getRow(RowType.IndiLastName);
            if ( mergeRow != null ) {
                return mergeRow.entityObject;
            } else {
                return null;
            }
        }
        
    }
    
    /**
     * retourne la propriété concernée par l'acte
     * @return propriété concernée par l'acte
     */
    @Override
    protected Property getSelectedProperty() {
        if (participantType == MergeParticipantType.participant1 && getSelectedEntity()!= null) {
            return MergeQuery.findPropertyEvent(getSelectedEntity(),record.getEventType(),record.getEventDate());
        } else {
            return null;
        }
    }        

    /**
     * copie les données du relevé dans l'entité
     */
    @Override
    protected Property copyRecordToEntity() throws Exception {
        Indi currentIndi = (Indi) getSelectedEntity();
        if (currentIndi == null) {
            currentIndi = (Indi) gedcom.createEntity(Gedcom.INDI);
            currentIndi.setName(participant.getFirstName(), participant.getLastName());
            currentIndi.setSex(participant.getSex());
        } else {
            // je copie le nom du releve dans l'individu
            if (isChecked(RowType.IndiLastName)) {
                currentIndi.setName(currentIndi.getFirstName(), participant.getLastName());
            }

            // je copie le prénom du releve dans l'individu
            if (isChecked(RowType.IndiFirstName)) {
                currentIndi.setName(participant.getFirstName(), currentIndi.getLastName());
            }

            // je copie le sex du releve dans l'individu
            if (isChecked(RowType.IndiSex)) {
                currentIndi.setSex(participant.getSex());
            }
        }

        // je cree la propriete de naissance si elle n'existait pas
        if (isChecked(RowType.IndiBirthDate) || isChecked(RowType.IndiBirthPlace) || isChecked(RowType.EventSource) || isChecked(RowType.EventComment)) {
            Property birthProperty = currentIndi.getProperty("BIRT");
            if (birthProperty == null) {
                birthProperty = currentIndi.addProperty("BIRT", "");
            }

            // je copie la date de naissance du releve dans l'individu
            if (isChecked(RowType.IndiBirthDate)) {
                // j'ajoute (ou remplace) la date de la naissance (le lieu de naissance n'est pas connu)
                copyBirthDate(currentIndi, getRow(RowType.IndiBirthDate), "", record);
            }

            // je copie le lieu de naissance
            if (isChecked(RowType.IndiBirthPlace)) {
                copyPlace(participant.getBirthPlace(), birthProperty);
            }
        }

        // je copie la profession de l'individu
        if (isChecked(RowType.IndiOccupation)) {
            copyOccupation(currentIndi, participant.getOccupation(), participant.getResidence(), true, record);
        }

        // je copie l'evenement
        Property eventProperty = null;
        if ( getRow(RowType.EventType)!= null ) {
            eventProperty = (Property)getRow(RowType.EventType).entityValue;
        }
        if (isChecked(RowType.EventType) || isChecked(RowType.EventDate) || isChecked(RowType.EventPlace) || isChecked(RowType.EventSource) || isChecked(RowType.EventComment)) {
            // je cree la propriete de l'évènement si elle n'existait pas
            if (eventProperty == null) {
                eventProperty = currentIndi.addProperty("EVEN", "", getPropertyBestPosition(currentIndi, record.getEventDate()));
                eventProperty.addProperty("TYPE", record.getEventType());
            }

            // je copie la date de l'evenement du releve dans l'individu
            if (isChecked(RowType.EventDate)) {
                // j'ajoute (ou remplace) la date de la naissance
                PropertyDate propertyDate = (PropertyDate) eventProperty.getProperty("DATE",false);
                if (propertyDate == null) {
                    propertyDate = (PropertyDate) eventProperty.addProperty("DATE", "");
                }
                if( record.isInsinuation()) {
                    propertyDate.setValue(record.getInsinuationDate().getValue());
                } else {
                    propertyDate.setValue(record.getEventDate().getValue());
                }
            }

            // je copie le lieu de l'evenement
            if (isChecked(RowType.EventPlace)) {
                copyPlace(record.getEventPlace(),  eventProperty);
            }

            // je copie la source de l'evenement
            if (isChecked(RowType.EventSource) || isChecked(RowType.EventPage)) {
                copySource((Source) getRow(RowType.EventSource).entityObject, eventProperty, isChecked(RowType.EventPage), record);
            }

            // je copie le commentaire de l'evenement
            if (isChecked(RowType.EventComment)) {
                Property propertyNote = eventProperty.getProperty("NOTE");
                if (propertyNote == null) {
                    // je cree une note .
                    propertyNote = eventProperty.addProperty("NOTE", "");
                }

                // j'ajoute le commentaire general au debut de la note existante.
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

        // je copie les données du conjoint
        if (isChecked(RowType.IndiMarriedFamily)) {
            Indi exSpouse = (Indi) getRow(RowType.IndiMarriedLastName).entityObject;
            if (exSpouse == null) {
                // je cree l'individu
                exSpouse = (Indi) gedcom.createEntity(Gedcom.INDI);
                exSpouse.setName(participant.getMarriedFirstName(), participant.getMarriedLastName());
                exSpouse.setSex(currentIndi.getSex()==PropertySex.MALE ? PropertySex.FEMALE : PropertySex.MALE);
            } else {
                // je copie le nom de l'ex conjoint
                if (isChecked(RowType.IndiMarriedLastName)) {
                    exSpouse.setName(exSpouse.getFirstName(), participant.getMarriedLastName());
                }

                // je copie le prénom de l'ex conjoint
                if (isChecked(RowType.IndiMarriedFirstName)) {
                    exSpouse.setName(participant.getMarriedFirstName(), exSpouse.getLastName());
                }
            }

            // je copie la date, le lieu et commentaire de naissance du conjoint
            if (isChecked(RowType.IndiMarriedBirthDate)) {
                copyBirthDate(exSpouse, getRow(RowType.IndiMarriedBirthDate), "", record);
            }

            // je copie la date, le lieu et commentaire de naissance du conjoint
            if (isChecked(RowType.IndiMarriedDeathDate)) {
                copyDeathDate(exSpouse, getRow(RowType.IndiMarriedDeathDate), "", record);
            }

            // je copie la profession du conjoint
            if (isChecked(RowType.IndiMarriedOccupation)) {
                copyOccupation(exSpouse, participant.getMarriedOccupation(), participant.getMarriedResidence(), true, record);
            }

            // je copie la famille avec le conjoint
            Fam family = (Fam) getRow(RowType.IndiMarriedFamily).entityObject;
            if (family == null) {
                // je cree la famille
                family = (Fam) gedcom.createEntity(Gedcom.FAM);
                // j'ajoute les epoux
                if ( currentIndi.getSex() == PropertySex.MALE) {
                    family.setHusband(currentIndi);
                    family.setWife(exSpouse);
                } else {
                    family.setHusband(exSpouse);
                    family.setWife(currentIndi);
                }
            }

           // je copie la date du mariage avec le conjoint et une note indiquant l'origine de cette date
            if (isChecked(RowType.IndiMarriedMarriageDate)) {
                copyMarriageDate(family, getRow(RowType.IndiMarriedMarriageDate), record );
            }
        }


        // je copie les données des parents
        if (isChecked(RowType.IndiParentFamily)) {
            // je copie la famille des parents
            Fam family = (Fam) getRow(RowType.IndiParentFamily).entityObject;
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

            // je copie les informations du pere
            if ( isChecked(RowType.IndiFatherFirstName) || isChecked(RowType.IndiFatherLastName)
                    || isChecked(RowType.IndiFatherBirthDate) || isChecked(RowType.IndiFatherDeathDate)
                    || isChecked(RowType.IndiFatherOccupation) ){
                
                // je copie le nom et le prenom du pere
                Indi father = family.getHusband();
                if (father == null) {
                    // je cree le pere
                    father = (Indi) gedcom.createEntity(Gedcom.INDI);
                    father.setName(participant.getFatherFirstName(), participant.getFatherLastName());
                    father.setSex(PropertySex.MALE);
                    family.setHusband(father);
                } else {
                    if (isChecked(RowType.IndiFatherFirstName)) {
                        father.setName(participant.getFatherFirstName(), father.getLastName());
                    }
                    if (isChecked(RowType.IndiFatherLastName)) {
                        father.setName(father.getFirstName(), participant.getFatherLastName());
                    }
                }

                // je copie la date de naissance du pere
                if (isChecked(RowType.IndiFatherBirthDate)) {
                    copyBirthDate(father, getRow(RowType.IndiFatherBirthDate), "", record);
                }

                //je copie la date de décès du pere
                if (isChecked(RowType.IndiFatherDeathDate)) {
                    copyDeathDate(father, getRow(RowType.IndiFatherDeathDate), "", record);
                }

                // je copie la profession du pere
                if (isChecked(RowType.IndiFatherOccupation)) {
                    copyOccupation(father, participant.getFatherOccupation(), participant.getFatherResidence(), true, record);
                }
            }

            // je copie les informations de la mere
            if ( isChecked(RowType.IndiMotherFirstName) || isChecked(RowType.IndiMotherLastName)
                    || isChecked(RowType.IndiMotherBirthDate) || isChecked(RowType.IndiMotherDeathDate)
                    || isChecked(RowType.IndiMotherOccupation) ){

                // je copie le nom et le prenom de la mere
                Indi mother = family.getWife();
                if (mother == null ) {
                    // je cree la mere
                    mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                    mother.setName(participant.getMotherFirstName(), participant.getMotherLastName());
                    mother.setSex(PropertySex.FEMALE);
                    family.setWife(mother);
                } else {
                    if (isChecked(RowType.IndiMotherFirstName)) {
                        mother.setName(participant.getMotherFirstName(), mother.getLastName());
                    }
                    if (isChecked(RowType.IndiMotherLastName)) {
                        mother.setName(mother.getFirstName(), participant.getMotherLastName());
                    }
                }

                // je copie la date de naissance de la mere
                if (isChecked(RowType.IndiMotherBirthDate)) {
                    copyBirthDate(mother, getRow(RowType.IndiMotherBirthDate), "", record);
                }

                // je copie la date de décès de la mere
                if (isChecked(RowType.IndiMotherDeathDate)) {
                    copyDeathDate(mother, getRow(RowType.IndiMotherDeathDate), "", record);
                }

                // je met à jour la profession de la mere
                if (isChecked(RowType.IndiMotherOccupation)) {
                    copyOccupation(mother, participant.getMotherOccupation(), participant.getMotherResidence(), true, record);
                }
            }
        }

        // je retourne la propriete pour faire une association entre les particpants
        if (participantType == MergeParticipantType.participant1) {
            return eventProperty;
        } else {
            return currentIndi;
        }
    }

    /**
     * retourne une chaine de caracteres contenant le nom, prénom et la date du relevé
     * Cette chaine sert de titre a la fenetre principale
     * @return
     */
    @Override
    protected String getTitle() {
        String message1 = record.getIndi().getFirstName() + " "+ record.getIndi().getLastName();
        String message2 = record.getWife().getFirstName() + " "+ record.getWife().getLastName()+ " " + record.getEventDate().getDisplayValue();
        return MessageFormat.format(NbBundle.getMessage(MergeDialog.class, "MergeModel.title.misc"), record.getEventType(), message1, message2);
    }

    /**
     * retourne un resumé du modele
     * Cette chaine sert de commentaire dans la liste des modeles
     * @return
     */
    @Override
    public String getSummary(Entity selectedEntity) {
        String summary;
        if ( getSelectedEntity() == null ) {
                summary = "Nouvel individu" + " - ";
                if (getRow(MergeModel.RowType.IndiParentFamily).entityObject != null) {
                    summary += getRow(MergeModel.RowType.IndiParentFamily).entityObject.toString(false);
                } else {
                    if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null
                            || getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                        summary += "Nouveau couple:" + " ";
                        if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiFatherLastName).entityObject.toString(true);
                        } else {
                            summary += "Nouveau père";
                        }
                        summary += " , ";
                        if (getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiMotherLastName).entityObject.toString(true);
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
                summary = "Modifier " + getSelectedEntity().toString(true) + ", ";

                if (getRow(MergeModel.RowType.IndiParentFamily).entityObject != null) {
                    summary += getRow(MergeModel.RowType.IndiParentFamily).entityObject.toString(false);
                } else {
                    if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null
                            || getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                        summary += "Nouveau couple:" + " ";
                        if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiFatherLastName).entityObject.toString(true);
                        } else {
                            summary += "Nouveau père";
                        }
                        summary += ", ";
                        if (getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiMotherLastName).entityObject.toString(true);
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
