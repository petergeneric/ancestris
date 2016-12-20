package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.MergeRecord.MergeParticipantType;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
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
        super(record, participantType, indi, gedcom);                
        addRowSource();
        if( marriedFamily != null && mainParticipant.getSex()== PropertySex.FEMALE) {
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

        if (participantType == MergeParticipantType.participant1) {
            // participant 1
            addRowEvent(currentIndi, record.getEventTag());
//               // je recherche la source de l'évènement deja existant
//               PropertyEvent eventProperty = MergeQuery.findPropertyEvent(currentIndi,record.getEventType(),record.getEventDate());
//               // j'affiche la date, le lieu et les commentaires de l'évènement
//               addRow(RowType.EventType, record.getEventType(), eventProperty, currentIndi);
//               addRow(RowType.EventDate, record.getEventDate(), eventProperty != null ? (PropertyDate) eventProperty.getProperty("DATE") : null);
//               addRow(RowType.EventPlace, record.getEventPlace(), eventProperty != null ? eventProperty.getPropertyValue("PLAC") : "");
//               addRow(RowType.EventComment, record.getEventComment(showFrenchCalendarDate), eventProperty != null ? eventProperty.getPropertyValue("NOTE") : "");
            addRowSeparator();
        } else {
               // participant 2
            // rien à faire
        }
    }

    private void addRowIndi(Indi currentIndi) throws Exception {

       if (currentIndi != null) {
            // j'affiche les informations de l'individu
            addRow(RowType.IndiLastName, mainParticipant.getLastName(), currentIndi.getLastName(), currentIndi);
            addRow(RowType.IndiFirstName, mainParticipant.getFirstName(), currentIndi.getFirstName());
            addRow(RowType.IndiSex, mainParticipant.getSexString(), currentIndi.getPropertyValue("SEX"));
            addRow(RowType.IndiBirthDate, mainParticipant.getBirthDate() , currentIndi.getBirthDate());
            addRow(RowType.IndiBirthPlace, mainParticipant.getBirthPlace(), currentIndi.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.IndiDeathDate, mainParticipant.getDeathDate() , currentIndi.getDeathDate());
            addRow(RowType.IndiOccupation, mainParticipant.getOccupationWithDate(), MergeQuery.findOccupation(currentIndi, record.getEventDate()));

        } else {
            // selectedIndi est nul
            addRow(RowType.IndiLastName, mainParticipant.getLastName(), "");
            addRow(RowType.IndiFirstName, mainParticipant.getFirstName(), "");
            addRow(RowType.IndiSex, mainParticipant.getSexString(), "");
            addRow(RowType.IndiBirthDate, mainParticipant.getBirthDate() , null);
            addRow(RowType.IndiBirthPlace, mainParticipant.getBirthPlace(), "");
            addRow(RowType.IndiDeathDate, mainParticipant.getDeathDate() , null);
            addRow(RowType.IndiOccupation, mainParticipant.getOccupationWithDate(), "");
        }
    }

    private void addRowParents( Fam fam) throws Exception {
        addRowSeparator();
        addRow(RowType.IndiParentFamily, fam);
        if (fam != null) {
            addRow(RowType.IndiParentMarriageDate, mainParticipant.getParentMarriageDate(), fam.getMarriageDate());
            addRowFather(fam.getHusband());
            addRowMother(fam.getWife());
        } else {
            addRow(RowType.IndiParentMarriageDate, mainParticipant.getParentMarriageDate(), null);
            addRowFather(null);
            addRowMother(null);
        }
    }

    private void addRowFather( Indi father ) throws Exception {
        if (father != null) {
            addRow(RowType.IndiFatherLastName, mainParticipant.getFatherLastName(), father.getLastName(), father);
            addRow(RowType.IndiFatherFirstName, mainParticipant.getFatherFirstName(), father.getFirstName());
            addRow(RowType.IndiFatherBirthDate, mainParticipant.getFatherBirthDate(), father.getBirthDate());
            addRow(RowType.IndiFatherDeathDate, mainParticipant.getFatherDeathDate(), father.getDeathDate());
            addRow(RowType.IndiFatherOccupation, mainParticipant.getFatherOccupationWithDate(), MergeQuery.findOccupation(father, record.getEventDate()));
        } else {
            addRow(RowType.IndiFatherLastName, mainParticipant.getFatherLastName(), "");
            addRow(RowType.IndiFatherFirstName, mainParticipant.getFatherFirstName(), "");
            addRow(RowType.IndiFatherBirthDate, mainParticipant.getFatherBirthDate(), null);
            addRow(RowType.IndiFatherDeathDate, mainParticipant.getFatherDeathDate(), null);
            addRow(RowType.IndiFatherOccupation, mainParticipant.getFatherOccupationWithDate(), "");
        }
    }

    private void addRowMother( Indi mother ) throws Exception {
        if (mother != null) {
            addRow(RowType.IndiMotherLastName, mainParticipant.getMotherLastName(), mother.getLastName(), mother);
            addRow(RowType.IndiMotherFirstName, mainParticipant.getMotherFirstName(), mother.getFirstName());
            addRow(RowType.IndiMotherBirthDate, mainParticipant.getMotherBirthDate(), mother.getBirthDate());
            addRow(RowType.IndiMotherDeathDate, mainParticipant.getMotherDeathDate(), mother.getDeathDate());
            addRow(RowType.IndiMotherOccupation, mainParticipant.getMotherOccupationWithDate(), MergeQuery.findOccupation(mother, record.getEventDate()));
        } else {
            addRow(RowType.IndiMotherLastName, mainParticipant.getMotherLastName(), "");
            addRow(RowType.IndiMotherFirstName, mainParticipant.getMotherFirstName(), "");
            addRow(RowType.IndiMotherBirthDate, mainParticipant.getMotherBirthDate(), null);
            addRow(RowType.IndiMotherDeathDate, mainParticipant.getMotherDeathDate(), null);
            addRow(RowType.IndiMotherOccupation, mainParticipant.getMotherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations sur l'ex conjoint
     * @param marriedFamily
     * @throws Exception
     */
    private void addRowMarried(Fam marriedFamily ) throws Exception {
        if (! mainParticipant.getMarriedLastName().isEmpty() || ! mainParticipant.getMarriedFirstName().isEmpty()) {
            // j'affiche un separateur
            addRowSeparator();
            addRow(RowType.IndiMarriedFamily, marriedFamily);

            if (marriedFamily != null) {
                addRow(RowType.IndiMarriedMarriageDate, mainParticipant.getMarriedMarriageDate(), marriedFamily.getMarriageDate());

                Indi married;
                if ( mainParticipant.getSex() == PropertySex.MALE) {
                    married = marriedFamily.getWife();
                } else {
                    married = marriedFamily.getHusband();
                }
                addRow(RowType.IndiMarriedLastName, mainParticipant.getMarriedLastName(), married.getLastName(), married);
                addRow(RowType.IndiMarriedFirstName, mainParticipant.getMarriedFirstName(), married.getFirstName());
                addRow(RowType.IndiMarriedBirthDate, mainParticipant.getMarriedBirthDate(), married.getBirthDate());
                addRow(RowType.IndiMarriedDeathDate, mainParticipant.getMarriedDeathDate(), married.getDeathDate());
                addRow(RowType.IndiMarriedOccupation, mainParticipant.getMarriedOccupationWithDate(), MergeQuery.findOccupation(married, record.getEventDate()));
            } else {
                addRow(RowType.IndiMarriedMarriageDate, mainParticipant.getMarriedMarriageDate(),null);
                addRow(RowType.IndiMarriedLastName, mainParticipant.getMarriedLastName(), "", null);
                addRow(RowType.IndiMarriedFirstName, mainParticipant.getMarriedFirstName(), "");
                addRow(RowType.IndiMarriedBirthDate, mainParticipant.getMarriedBirthDate(), null);
                addRow(RowType.IndiMarriedDeathDate, mainParticipant.getMarriedDeathDate(), null);
                addRow(RowType.IndiMarriedOccupation, mainParticipant.getMarriedOccupationWithDate(), "");
            }            
        }
    }

     /**
     * retourne l'individu proposé dans le modele
     * @return 
     */
    @Override
    protected Entity getProposedEntity() {
        Entity indi = getEntityObject(RowType.IndiLastName);
        if (indi != null)  {
            return indi;
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
        return getEntityObject(RowType.IndiLastName);                 
    }
    
    /**
     * copie les données du relevé dans l'entité
     */
    @Override
    protected Property copyRecordToEntity() throws Exception {
        
        Property resultProperty;
        
        Indi currentIndi = (Indi) getSelectedEntity();
        if (currentIndi == null) {
            currentIndi = (Indi) gedcom.createEntity(Gedcom.INDI);
            currentIndi.setName(mainParticipant.getFirstName(), mainParticipant.getLastName());
            currentIndi.setSex(mainParticipant.getSex());
        } else {
            // je copie le nom du releve dans l'individu
            if (isChecked(RowType.IndiLastName)) {
                currentIndi.setName(currentIndi.getFirstName(), mainParticipant.getLastName());
            }

            // je copie le prénom du releve dans l'individu
            if (isChecked(RowType.IndiFirstName)) {
                currentIndi.setName(mainParticipant.getFirstName(), currentIndi.getLastName());
            }

            // je copie le sex du releve dans l'individu
            if (isChecked(RowType.IndiSex)) {
                currentIndi.setSex(mainParticipant.getSex());
            }
        }
        
        resultProperty = currentIndi;

        // je cree la propriete de naissance si elle n'existait pas
        if (isChecked(RowType.IndiBirthDate) || isChecked(RowType.IndiBirthPlace)) {
            // je copie la date et le lieu de naissance du releve dans l'individu
            if (isChecked(RowType.IndiBirthDate)) {
                // j'ajoute (ou remplace) la date de la naissance (le lieu de naissance n'est pas connu)
                copyBirthDate(currentIndi, getRow(RowType.IndiBirthDate), mainParticipant.getBirthPlace(), record);
            }
        }
        
        // je copie la date de décès de l'individu
        if (isChecked(RowType.IndiDeathDate)) {
            copyDeathDate(currentIndi, getRow(RowType.IndiDeathDate), record);
        }

        // je copie la profession de l'individu
        if (isChecked(RowType.IndiOccupation)) {
            copyOccupation(currentIndi, mainParticipant.getOccupation(), mainParticipant.getResidence(), true, record);
        }

        // je copie l'evenement uniquement pour le participant1
        if (participantType == MergeParticipantType.participant1) {
            resultProperty = copyEvent(currentIndi);
        }

        // je copie les données du conjoint
        copyIndiMarried(mainParticipant, currentIndi);
        
        // je copie les données des parents
        if (isChecked(RowType.IndiParentFamily)) {
            // je copie la famille des parents
            Fam family = (Fam) getEntityObject(RowType.IndiParentFamily);
            if (family == null) {
                // je cree la famille
                family = (Fam) gedcom.createEntity(Gedcom.FAM);
            }

            // j'ajoute l'enfant dans la famille s'il n'est pas déjà dans la famille
            if (!currentIndi.isDescendantOf(family)) {
                family.addChild(currentIndi);
            }

            // je copie la date du mariage et une note indiquant l'origine de cette date
            if (isChecked(RowType.IndiParentMarriageDate)) {
                copyMarriageDate(family, getRow(RowType.IndiParentMarriageDate), record );
            }

            // je copie les parents
            copyIndiFather(mainParticipant, family.getHusband(), family);
            copyIndiMother(mainParticipant, family.getWife(), family);

        }

        // je retourne la propriete pour faire une association entre les particpants
        if (participantType == MergeParticipantType.participant1) {
            return resultProperty;
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
                summary = "Modifier " + getSelectedEntity().toString(true) + ", ";

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
