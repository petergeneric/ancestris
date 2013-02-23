package ancestris.modules.releve.dnd;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 * cette classe gère les lignes affichées dans la fenetre du comparateur
 * @author Michel
 */
public abstract class MergeModel extends AbstractTableModel implements java.lang.Comparable<MergeModel> {

    protected class MergeRow {
        RowType rowType;
        String label;
        Object recordValue;
        Object entityValue;
        boolean merge;
        boolean merge_initial;
        CompareResult compareResult ;
        Entity entityObject = null;
        
        @Override
        public String toString() {
            return rowType.name() + " "+ recordValue + " " + entityValue;
        }
    }
    protected enum CompareResult {
        EQUAL,
        COMPATIBLE,
        CONFLIT,
        NOT_APPLICABLE
    }

    private EnumMap<RowType, MergeRow> dataMap = new EnumMap<RowType, MergeRow>(RowType.class);
    private List<MergeRow> dataList = new ArrayList<MergeRow>();
    private int nbMatch = 0;
    private int nbMatchMax = 0;


    static protected List<MergeModel> createMergeModel(MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity) throws Exception {
        return createMergeModel(mergeRecord, gedcom, selectedEntity, false);
    }

    /**
     * model factory
     * cree un model qui compare un releve et une entité selectionnée dans le
     * gedcom.
     * Si selectedEntity = null , le modele recherche les entités compatibles dans
     * le gedcom.
     * @param mergeRecord   releve
     * @param gedcom
     * @param selectedEntity
     * @return
     */
    static protected List<MergeModel> createMergeModel(MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity, boolean showNewParents) throws Exception {
        List<MergeModel> models = new ArrayList<MergeModel>();
        if( mergeRecord.getType() ==  MergeRecord.RecordType.Birth) {
            if ( selectedEntity instanceof Fam ) {
                // 1.1) Record Birth : l'entité selectionnée est une famille
                Fam family = (Fam) selectedEntity;
                // j'ajoute un nouvel individu
                models.add(new MergeModelBirth(mergeRecord, gedcom, null, family));
                // je recherche les enfants de la famille sélectionnée compatibles avec le releve
                List<Indi> sameChildren = MergeQuery.findSameChild(mergeRecord, gedcom, family);
                // j'ajoute les enfants compatibles
                for(Indi samedIndi : sameChildren) {
                    models.add(new MergeModelBirth(mergeRecord, gedcom, samedIndi, samedIndi.getFamilyWhereBiologicalChild()));
                }
            } else if ( selectedEntity instanceof Indi ) {
                // 1.2) Record Birth : l'entité selectionnée est un individu
                Indi selectedIndi = (Indi) selectedEntity;

                // je cherche les familles compatibles avec le releve de naissance
                List<Fam> families = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);

                // j'ajoute l'individu selectionné par dnd 
                if (selectedIndi.getFamilyWhereBiologicalChild() != null ) {
                    // j'ajoute l'individu selectionné par dnd
                    models.add(new MergeModelBirth(mergeRecord, gedcom, selectedIndi, selectedIndi.getFamilyWhereBiologicalChild()));
                } else {
                    models.add(new MergeModelBirth(mergeRecord, gedcom, selectedIndi ,(Fam) null));
                    // j'ajoute l'individu selectionné par dnd avec les familles compatibles
                    for(Fam family : families) {
                        models.add(new MergeModelBirth(mergeRecord, gedcom, selectedIndi, family));
                    }
                }
                
                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                // en excluant l'individu selectionne s'il a deja une famille
                List<Indi> sameIndis ;
                if ( selectedIndi.getFamilyWhereBiologicalChild() != null ) {
                    // l'individu est lié a une famille précise, je l'exclue de la recherche
                    sameIndis = MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, selectedIndi);
                } else {
                    // l'individu n'est pas lié a une famille précise, je l'inclue dans la recherche
                    sameIndis = MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, null);
                }
                // j'ajoute les individus compatibles
                for(Indi samedIndi : sameIndis) {
                    // j'ajoute les familles compatibles
                    Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild() ;
                    if ( sameIndiFamily != null) {
                        models.add(new MergeModelBirth(mergeRecord, gedcom, samedIndi, sameIndiFamily));
                    } else {
                        for(Fam family : families) {
                            models.add(new MergeModelBirth(mergeRecord, gedcom, samedIndi, family));
                        }
                    }
                }
              
            } else {
                // 1.3) Record Birth : pas d'entité selectionnee

                // j'ajoute un nouvel individu , sans famille associée
                models.add(new MergeModelBirth(mergeRecord, gedcom));

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, null);

                // je cherche les familles compatibles avec le releve de naissance
                List<Fam> families = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);

                // j'ajoute un nouvel individu avec les familles compatibles
                for(Fam family : families) {
                    models.add(new MergeModelBirth(mergeRecord, gedcom, null, family));
                }

                // j'ajoute les individus compatibles avec la famille de chacun
                for(Indi samedIndi : sameIndis) {
                    Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild() ;
                    if ( sameIndiFamily != null) {
                        // j'ajoute l'individus compatible avec sa famille
                        models.add(new MergeModelBirth(mergeRecord, gedcom, samedIndi, sameIndiFamily));
                    } else {
                        // j'ajoute l'individus compatible sans famille
                        models.add(new MergeModelBirth(mergeRecord, gedcom, samedIndi, (Fam) null));
                        // j'ajoute l'individus compatible avec les familles compatibles
                        for(Fam family : families) {
                            models.add(new MergeModelBirth(mergeRecord, gedcom, samedIndi, family));
                        }
                    }
                }

                if (showNewParents) {
                    // j'ajoute un nouvel individu avec les couples qui ne sont pas des familles
                    // mais qui pourraient être ses parents
                    List<Indi> fathers = new ArrayList<Indi>();
                    List<Indi> mothers = new ArrayList<Indi>();
                    MergeQuery.findFatherMotherCompatibleWithBirthRecord(mergeRecord, gedcom, families, fathers, mothers);
                    for(Indi father : fathers) {
                        for(Indi mother : mothers) {
                            models.add(new MergeModelBirth(mergeRecord, gedcom, father, mother));
                        }
                    }
                }
            }
           
        } else  if( mergeRecord.getType() ==  MergeRecord.RecordType.Marriage) {
            if ( selectedEntity instanceof Fam ) {
                // 2.1) Record Marriage : l'entité selectionnée est une famille
                Fam selectedFamily = (Fam) selectedEntity;

                // j'ajoute un modele avec la famille selectionne
                models.add(new MergeModelMarriage(mergeRecord, gedcom, selectedFamily));

            } else if ( selectedEntity instanceof Indi ) {
                // 2.2) Record Marriage : l'entité selectionnée est un individu
                Indi selectedIndi = (Indi) selectedEntity;

                // je cherche les familles avec l'individu selectionné
                Fam[] families = selectedIndi.getFamiliesWhereSpouse();
                // j'ajoute les familles compatibles
                for(Fam family : families) {
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
                for(Fam family : families) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, family));
                }

                // je recherche les individus compatibles avec l'epoux et l'epouse du releve
                List<Indi> husbands = new ArrayList<Indi>();
                List<Indi> wifes = new ArrayList<Indi>();
                MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(mergeRecord, gedcom, families, husbands, wifes);
                for(Indi husband : husbands) {
                    for(Indi wife : wifes) {
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, wife));
                    }
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, (Indi)null));
                }
                for(Indi wife : wifes) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, (Indi)null, wife));
                }

                // je recherche les familles des parents compatibles qui ne sont pas
                // dans les modeles precedents
                if (showNewParents ||
                    (showNewParents
                       && !mergeRecord.getIndiFatherFirstName().isEmpty()
                       && !mergeRecord.getIndiMotherFirstName().isEmpty()
                       && !mergeRecord.getIndiMotherLastName().isEmpty()
                       
                       ) ) {

                    List<Fam> husbandFamilies = new ArrayList<Fam>();
                    List<Fam> wifeFamilies = new ArrayList<Fam>();

                    for (Fam husbandFamily : MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom) ) {
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
                            models.add(new MergeModelMarriage(mergeRecord, gedcom, husbandFamily, wifeFamily));
                        }
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, husbandFamily, (Fam)null));
                    }
                    for(Fam wifeFamily : wifeFamilies) {
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, (Fam)null, wifeFamily));
                    }

                    // j'ajoute les combinaisons entre les epoux précedents et les familles 
                     for(Indi husband : husbands) {
                        for(Fam wifeFamily : wifeFamilies) {
                            models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, wifeFamily));
                        }
                    }
                    for(Indi wife : wifes) {
                        for(Fam husbandFamily : husbandFamilies) {
                            models.add(new MergeModelMarriage(mergeRecord, gedcom, husbandFamily, wife));
                        }
                    }
                }
            }
            
        } else if( mergeRecord.getType() ==  MergeRecord.RecordType.Death) {
            if ( selectedEntity instanceof Fam ) {
                // 1.1) Record Death : l'entité selectionnée est une famille
                Fam family = (Fam) selectedEntity;
                // j'ajoute un nouvel individu
                models.add(new MergeModelDeath(mergeRecord, gedcom, null, family));
                // je recherche les enfants de la famille sélectionnée compatibles avec le releve
                List<Indi> sameChildren = MergeQuery.findSameChild(mergeRecord, gedcom, family);
                // j'ajoute les enfants compatibles
                for(Indi samedIndi : sameChildren) {
                    models.add(new MergeModelDeath(mergeRecord, gedcom, samedIndi, samedIndi.getFamilyWhereBiologicalChild()));
                }
            } else if ( selectedEntity instanceof Indi ) {
                // 1.2) Record Death : l'entité selectionnée est un individu
                Indi selectedIndi = (Indi) selectedEntity;

                // je recherche la famille avec l'ex conjoint
                List<Fam> marriedFamilies = MergeQuery.findFamilyCompatibleWithMarried(mergeRecord, gedcom);

                // je cherche les familles des parents compatibles avec le releve
                List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);

                 if ( !marriedFamilies.isEmpty()) {
                    for(Fam family : marriedFamilies) {
                        if (mergeRecord.getIndiSex() == PropertySex.MALE) {
                            Indi husband = family.getHusband();
                            if ( selectedIndi.compareTo(husband)==0) {
                                Fam husbandParentFamily = husband.getFamilyWhereBiologicalChild();
                                models.add(new MergeModelDeath(mergeRecord, gedcom, husband, family, husbandParentFamily ));

                                if ( husbandParentFamily == null ) {
                                    // j'ajoute un nouvel individu avec les familles compatibles
                                    for(Fam parentFamily : parentFamilies) {
                                        models.add(new MergeModelDeath(mergeRecord, gedcom, husband, family, parentFamily ));
                                    }
                                }
                            }
                        } else {
                            Indi wife = family.getWife();
                            if ( selectedIndi.compareTo(wife)==0) {
                                Fam wifeParentFamily = wife.getFamilyWhereBiologicalChild();
                                models.add(new MergeModelDeath(mergeRecord, gedcom, wife, family, wifeParentFamily));

                                if ( wifeParentFamily == null ) {
                                    // j'ajoute un nouvel individu avec les familles compatibles
                                    for(Fam parentFamily : parentFamilies) {
                                        models.add(new MergeModelDeath(mergeRecord, gedcom, wife, family, parentFamily ));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // l'individu n'a pas d'ex conjoint

                    // je cherche les familles compatibles avec le releve de deces
                    List<Fam> families = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);

                    // j'ajoute l'individu selectionné par dnd
                    if (selectedIndi.getFamilyWhereBiologicalChild() != null ) {
                        // j'ajoute l'individu selectionné par dnd
                        models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, selectedIndi.getFamilyWhereBiologicalChild()));
                    } else {
                        models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi ,(Fam) null));
                        // j'ajoute l'individu selectionné par dnd avec les familles compatibles
                        for(Fam family : families) {
                            models.add(new MergeModelDeath(mergeRecord, gedcom, selectedIndi, family));
                        }
                    }

                    // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                    // releve et avec les dates de naissance compatibles et les parents compatibles)
                    // en excluant l'individu selectionne s'il a deja une famille
                    List<Indi> sameIndis ;
                    if ( selectedIndi.getFamilyWhereBiologicalChild() != null ) {
                        // l'individu est lié a une famille précise, je l'exclue de la recherche
                        sameIndis = MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, selectedIndi);
                    } else {
                        // l'individu n'est pas lié a une famille précise, je l'inclue dans la recherche
                        sameIndis = MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, null);
                    }
                    // j'ajoute les individus compatibles
                    for(Indi samedIndi : sameIndis) {
                        // j'ajoute les familles compatibles
                        Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild() ;
                        if ( sameIndiFamily != null) {
                            models.add(new MergeModelDeath(mergeRecord, gedcom, samedIndi, sameIndiFamily));
                        } else {
                            for(Fam family : families) {
                                models.add(new MergeModelDeath(mergeRecord, gedcom, samedIndi, family));
                            }
                        }
                    }
                }

            } else {
                // 1.3) Record Death : pas d'entité selectionnee

                // j'ajoute un nouvel individu , sans famille associée
                models.add(new MergeModelDeath(mergeRecord, gedcom));

                // je recherche la famille avec l'ex conjoint
                List<Fam> marriedFamilies = MergeQuery.findFamilyCompatibleWithMarried(mergeRecord, gedcom);

                // je cherche les familles des parents compatibles avec le releve
                List<Fam> parentFamilies = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);

                if ( !marriedFamilies.isEmpty()) {
                    for(Fam family : marriedFamilies) {
                        if (mergeRecord.getIndiSex() == PropertySex.MALE) {
                            Indi husband = family.getHusband();
                            Fam husbandParentFamily = husband.getFamilyWhereBiologicalChild();
                            models.add(new MergeModelDeath(mergeRecord, gedcom, husband, family, husbandParentFamily ));

                            if ( husbandParentFamily == null ) {
                                // j'ajoute un nouvel individu avec les familles compatibles
                                for(Fam parentFamily : parentFamilies) {
                                    models.add(new MergeModelDeath(mergeRecord, gedcom, husband, family, parentFamily ));
                                }
                            }
                        } else {
                            Indi wife = family.getWife();
                            Fam wifeParentFamily = wife.getFamilyWhereBiologicalChild();
                            models.add(new MergeModelDeath(mergeRecord, gedcom, wife, family, wifeParentFamily));

                            if ( wifeParentFamily == null ) {
                                // j'ajoute un nouvel individu avec les familles compatibles
                                for(Fam parentFamily : parentFamilies) {
                                    models.add(new MergeModelDeath(mergeRecord, gedcom, wife, family, parentFamily ));
                                }
                            }
                        }
                    }

                } else {
                    // il n'y a pas de famille pour de l'ex conjoint
                    
                    // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                    // releve et avec les dates de naissance compatibles et les parents compatibles)
                    List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, null);

                    // j'ajoute un nouvel individu avec les familles compatibles
                    for(Fam family : parentFamilies) {
                        models.add(new MergeModelDeath(mergeRecord, gedcom, null, family));
                    }

                    // j'ajoute les individus compatibles avec la famille de chacun
                    for(Indi samedIndi : sameIndis) {
                        Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild() ;
                        if ( sameIndiFamily != null) {
                            // j'ajoute l'individus compatible avec sa famille
                            models.add(new MergeModelDeath(mergeRecord, gedcom, samedIndi, sameIndiFamily));
                        } else {
                            // j'ajoute l'individus compatible sans famille
                            models.add(new MergeModelDeath(mergeRecord, gedcom, samedIndi, (Fam) null));
                            // j'ajoute l'individus compatible avec les familles compatibles
                            for(Fam family : parentFamilies) {
                                models.add(new MergeModelDeath(mergeRecord, gedcom, samedIndi, family));
                            }
                        }
                    }
                }

                if (showNewParents) {
                    // j'ajoute un nouvel individu avec les couples qui ne sont pas des familles
                    // mais qui pourraient être ses parents
                    List<Indi> fathers = new ArrayList<Indi>();
                    List<Indi> mothers = new ArrayList<Indi>();
                    MergeQuery.findFatherMotherCompatibleWithBirthRecord(mergeRecord, gedcom, parentFamilies, fathers, mothers);
                    for(Indi father : fathers) {
                        for(Indi mother : mothers) {
                            models.add(new MergeModelDeath(mergeRecord, gedcom, father, mother));
                        }
                    }
                }

            }
        } else  if( mergeRecord.getType() ==  MergeRecord.RecordType.Misc ) {
            if ( mergeRecord.getEventTypeTag()== MergeRecord.EventTypeTag.MARC ) {
                // 4.1) Record Misc : Contrat de mariage
                models = MergeModelMiscMarc.createMiscMergeModel(mergeRecord, gedcom, selectedEntity, showNewParents);
            } else if ( mergeRecord.getEventTypeTag()== MergeRecord.EventTypeTag.WILL ) {
                // 4.2) Record Misc : Testament
                
            } else {
                // 4.2) Record Misc : autre (quittance, ...;

            }


        } else {
             models = new ArrayList<MergeModel>();
        }

        // je trie les modeles par ordre décroissant du nombre de champs egaux entre le relevé et l'entité du gedcom
        Collections.sort(models);

        return models;
    }

    /**
     * ajoute une ligne dans le modele
     * @param rowType
     * @param label
     * @param recordValue
     * @param entityValue
     */
    void addRow(RowType rowType, String recordValue, String entityValue) {
        addRow(rowType, recordValue, entityValue, null);
    }


    /**
     * ajoute une ligne dans le modele pour comparer un champ de type String
     * (nom , prénom, commentaire )
     * @param rowType
     * @param label
     * @param recordValue
     * @param entityValue
     */
    void addRow(RowType rowType, String recordValue, String entityValue, Entity entity) {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.recordValue = recordValue;
        mergeRow.entityValue = entityValue;
        mergeRow.entityObject = entity;
        if ( isRowParentApplicable(rowType)) {
            if ( recordValue.isEmpty() ) {
                mergeRow.merge = false;
                mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                if ( rowType== RowType.IndiFirstName || rowType == RowType.IndiLastName
                     || rowType== RowType.IndiFatherFirstName || rowType == RowType.IndiFatherLastName
                     || rowType== RowType.IndiMotherFirstName || rowType == RowType.IndiMotherLastName
                     || rowType== RowType.WifeFirstName || rowType == RowType.WifeLastName
                     || rowType== RowType.WifeFatherFirstName || rowType == RowType.WifeFatherLastName
                     || rowType== RowType.WifeMotherFirstName || rowType == RowType.WifeMotherLastName
                   ) {
                    if ( entityValue.isEmpty()) {
                        mergeRow.merge = !recordValue.equals(entityValue);
                        mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    } else {
                        mergeRow.merge = false;
                        mergeRow.compareResult = !recordValue.equals(entityValue) ? CompareResult.CONFLIT : CompareResult.EQUAL;
                    }

                } else if (rowType== RowType.EventComment) {
                    // merge actif si le commenatire existant dans l'entité ne contient pas deja le commentaire du relevé.
                    mergeRow.merge = !entityValue.contains(recordValue);
                    mergeRow.compareResult = !entityValue.equals(recordValue) ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                } else {
                    mergeRow.merge = !recordValue.equals(entityValue);
                    mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                }
            }
        } else {
            // ligne parent NOT_APPLICABLE
            mergeRow.merge = false;
            mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        }


        // j'incremente le compteur de champs egaux
        if ( ( mergeRow.compareResult == CompareResult.EQUAL ||  mergeRow.compareResult == CompareResult.CONFLIT)
                && !recordValue.isEmpty() && !entityValue.isEmpty())  {
            nbMatch++;
        }
        nbMatchMax++;
        mergeRow.merge_initial = mergeRow.merge;

    }

    /**
     * ajoute une ligne dans le modele pour comparer un champ date
     * @param rowType
     * @param label
     * @param recordValue
     * @param entityValue
     */
    void addRow(RowType rowType, PropertyDate recordValue, PropertyDate entityValue) {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.entityValue = entityValue;
        mergeRow.recordValue = recordValue;
        
        if (isRowParentApplicable(rowType)) {

            // je compare les valeurs par defaut du releve et de l'entite
            if (recordValue == null || (recordValue != null && !recordValue.isComparable()) ) {
                mergeRow.merge = false;
                mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
            } else if (entityValue == null || (entityValue != null && !entityValue.isComparable())) {
                // j'active Merge seulement si la date du releve est comparable
                mergeRow.merge = recordValue.isComparable();
                mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            } else {
                switch (rowType) {
                    case EventDate:

                    case IndiBirthDate:
                    case IndiDeathDate:
                    case IndiMarriedBirthDate:
                    case IndiMarriedDeathDate:
                    case IndiFatherBirthDate:
                    case IndiMotherBirthDate:
                    case IndiFatherDeathDate:
                    case IndiMotherDeathDate:
                    case IndiParentMarriageDate:

                    case WifeBirthDate:
                    case WifeDeathDate:
                    case WifeMarriedBirthDate:
                    case WifeMarriedDeathDate:
                    case WifeFatherBirthDate:
                    case WifeMotherBirthDate:
                    case WifeFatherDeathDate:
                    case WifeMotherDeathDate:
                    case WifeParentMarriageDate:

                        if (recordValue.getValue().equals(entityValue.getValue())) {
                            // les valeurs sont egales, pas besoin de merger
                            mergeRow.merge = false;
                            mergeRow.compareResult = CompareResult.EQUAL;
                        } else {
                            PropertyDate bestDate = MergeQuery.getMostAccurateDate(recordValue, entityValue);
                            if( bestDate == null) {
                                mergeRow.merge = false;
                                mergeRow.compareResult = CompareResult.CONFLIT;
                            } else if (bestDate == entityValue ) {
                                mergeRow.merge = false;
                                mergeRow.compareResult = CompareResult.COMPATIBLE;
                            } else {
                                // je propose une date plus precise que celle du releve
                                recordValue.setValue(bestDate.getFormat(), bestDate.getStart(), bestDate.getEnd(), bestDate.getPhrase());
                                mergeRow.merge = true;
                                mergeRow.compareResult = CompareResult.COMPATIBLE;
                            } 
                        }

                        break;
                    default:
                        mergeRow.merge = recordValue.compareTo(entityValue) == 0;
                        mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                        break;
                }
            }
        } else {
            // ligne parent NOT_APPLICABLE
            mergeRow.merge = false;
            mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        }

        // j'incremente le compteur des champs egaux
        if ( mergeRow.compareResult == CompareResult.EQUAL )  {
            nbMatch++;
        }
        nbMatchMax++;
        mergeRow.merge_initial = mergeRow.merge;
    }

    /**
     * ajoute une ligne dans le modele pour comparer la source du releve
     * @param rowType
     * @param label
     * @param recordValues
     * @param entityValues
     */
    void addRow(RowType rowType, String recordSourceName, Property sourceProperty, Source recordSource) {
         MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.recordValue = recordSourceName;

        if (isRowParentApplicable(rowType)) {

            if (recordSource != null) {
                if (sourceProperty != null && sourceProperty.getPropertyValue("TITL").equals(recordSourceName)) {
                    mergeRow.entityValue = recordSource;
                    mergeRow.entityObject = recordSource;
                    mergeRow.merge = false;
                    mergeRow.compareResult = CompareResult.EQUAL;
                } else {
                    mergeRow.entityValue = null;
                    mergeRow.entityObject = recordSource;
                    mergeRow.merge = true;
                    mergeRow.compareResult = CompareResult.COMPATIBLE;
                }
            } else {
                mergeRow.entityValue = null;
                mergeRow.entityObject = null;
                mergeRow.merge = !recordSourceName.isEmpty();
                mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            }
        } else {
            // ligne parent NOT_APPLICABLE
            mergeRow.merge = false;
            mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        }
        mergeRow.merge_initial = mergeRow.merge;
    }

    /**
     * ajoute une ligne dans le modele pour comparer la famille
     * @param rowType
     * @param label
     * @param recordValues
     * @param entityValues
     */
    void addRow(RowType rowType, MergeRecord record, Fam family) {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.recordValue = null;
        mergeRow.entityValue = family;
        mergeRow.entityObject = family;

        if (isRowParentApplicable(rowType)) {

            if (family != null) {
                mergeRow.merge = true;
                mergeRow.compareResult = CompareResult.EQUAL;
            } else {
                if ((rowType == RowType.IndiParentFamily && record.getIndiFatherLastName().isEmpty() && record.getIndiMotherLastName().isEmpty())
                        || (rowType == RowType.WifeParentFamily && record.getWifeFatherLastName().isEmpty() && record.getWifeMotherLastName().isEmpty())
                        ) {
                    // j'interdis la creation d'un nouvelle famille si le nom du pere et de la mere sont vide.
                    mergeRow.merge = false;
                    mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
                } else {
                    mergeRow.merge = true;
                    mergeRow.compareResult = CompareResult.COMPATIBLE;
                }
            }
        } else {
            // ligne parent NOT_APPLICABLE
            mergeRow.merge = false;
            mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        }

         // j'incremente le compteur des champs egaux
        if ( mergeRow.compareResult == CompareResult.EQUAL)  {
            nbMatch++;
        }
        nbMatchMax++;
        mergeRow.merge_initial = mergeRow.merge;

    }

    /**
     * ajoute une ligne dans le modele servant de separateur
     * @param rowType
     * @param label
     * @param recordValues
     * @param entityValues
     */
     void addRowSeparator () {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(RowType.Separator, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = RowType.Separator;
        mergeRow.label = "";
        mergeRow.entityValue = null;
        mergeRow.recordValue = null;
        mergeRow.merge = false;
        mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        mergeRow.merge_initial = mergeRow.merge;
    }

    /**
     * retourne une ligne en fonction du type
     * @param rowType
     * @return
     */
    protected MergeRow getRow(RowType rowType) {
        return dataMap.get(rowType);
    }

    /**
     * retourne une ligne en fonction numero 
     * @param row
     * @return
     */
    MergeRow getRow(int row) {
        return dataList.get(row);
    }


    /**
     * retourne le resultat de comparaison de la ligne
     * @param row
     * @return
     */
    protected CompareResult getCompareResult(int row) {
        return dataList.get(row).compareResult;
    }


    /**
     * retourne le nombre de chmaps egaux entre le releve et l'entité
     * @param rowType
     * @return
     */
    protected int getNbMatch() {
        return nbMatch;
    }

    /**
     * nombre total de champs comparables
     * @return
     */
    protected int getNbMatchMax() {
        return nbMatchMax;
    }

    /**
     * compare le nombre de champs egaux du modele avec celui d'un autre modele
     * pour savoir quel est le modele qui contient l'entité la plu proche du relevé.
     * @param object
     * @return
     */
    @Override
    public int compareTo(MergeModel object) {
        if ( !(object instanceof MergeModel)) {
            return 1;
        }
        int nombre1 = object.nbMatch;
        int nombre2 = this.nbMatch;
        if (nombre2 > nombre1) {
            return -1;
        } else if (nombre1 == nombre2) {
            return 0;
        } else {
            return 1;
        }
    }
    
    // methodes abstraites
    protected abstract void copyRecordToEntity() throws Exception;
    protected abstract String getTitle();
    public abstract String getSummary(Entity selectedEntity);
    protected abstract Entity getSelectedEntity();
    protected abstract Gedcom getGedcom();

    /**
     * i
     */
    private String[] columnNames = {
        "",
        NbBundle.getMessage(MergeModel.class, "MergePanel.title.recordColumn"),
        "=>",
        NbBundle.getMessage(MergeModel.class, "MergePanel.title.gedcomColumn"),
        "Identifiant"
    };
    private Class[] columnClass = {String.class, Object.class, Boolean.class, Object.class, Entity.class};

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return dataList.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return columnClass[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return dataList.get(row).label;
            case 1:
                return dataList.get(row).recordValue;
            case 2:
                return dataList.get(row).merge;
            case 3:
                return dataList.get(row).entityValue;
            case 4:
                return dataList.get(row).entityObject;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case 2:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 1:
                dataList.get(row).recordValue = value;
                break;
            case 2:
                check(row, (Boolean) value);
                break;
            case 3:
                dataList.get(row).recordValue = value;
                break;
            default:
                break;
        }
        fireTableCellUpdated(row, col);
    }

    /**
     * coche ou décoche une ligne en focntion de son numero d'ordre
     * @param rowNum
     * @param state
     */
    void check(int rowNum, boolean state) {
        check(dataList.get(rowNum).rowType, state);
    }


    /**
     * coche ou décoche une ligne en fonction de sont type
     * @param rowNum
     * @param state
     */
    void check(RowType rowType, boolean state) {
        dataMap.get(rowType).merge = state;
        fireTableCellUpdated(dataList.indexOf(dataMap.get(rowType)), 2);
        fireTableCellUpdated(dataList.indexOf(dataMap.get(rowType)), 3);

        // je mets a jour les lignes filles
        for(int i =0 ; i < dataList.size();i++) {
            MergeRow mergeRow = dataList.get(i);
            MergeRow parentRow = getParentRow(mergeRow.rowType);
            if (parentRow!= null && parentRow.rowType == rowType ) {
                if (state == true) {
                    // je restaure l'etat initial de la ligne fille
                    mergeRow.merge =  mergeRow.merge_initial;
                } else {
                    mergeRow.merge =  false;
                }
                fireTableCellUpdated(i, 2);
                fireTableCellUpdated(i, 3);
            }
        }

        // je mets a jour la ligne parent
        // seulement si elle était décochée et que la fille vient d'etre cochee
        MergeRow parentRow = getParentRow(rowType);
        if (state == true && parentRow!= null ) {
            parentRow.merge = true;
            fireTableCellUpdated(dataList.indexOf(parentRow), 2);
            fireTableCellUpdated(dataList.indexOf(parentRow), 3);
        }
    }

    /**
     * retourne l'etat coché ou décoché d'une ligne
     * @param rowType
     * @return
     */
    boolean isChecked(RowType rowType) {
        MergeRow mergeRow = dataMap.get(rowType);
        if ( mergeRow == null) {
            return false;
        } else {
            return dataMap.get(rowType).merge;
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // utilitaires
    ////////////////////////////////////////////////////////////////////////////

    /**
     * ajoute un lieu a une propriete
     * @param place
     * @param eventProperty
     */
    static protected void copyPlace(String place, Property eventProperty) {
        PropertyPlace propertyPlace = (PropertyPlace) eventProperty.getProperty("PLAC");
        if (propertyPlace == null) {
            // je cree le lieu .
            propertyPlace = (PropertyPlace) eventProperty.addProperty("PLAC", "");
        }
        propertyPlace.setValue(place);
    }

    /**
     * ajoute une source a une propriete
     * @param source
     * @param eventProperty
     * @param record
     * @throws Exception
     */
    static protected void copySource(Source source, Property eventProperty, MergeRecord record) throws Exception {
        PropertyXRef sourcexref = null;
        if (source != null) {
            // je verifie si la source est déjà associée à la naissance
            boolean found = false;
            // je copie les sources de l'entité
            Property[] sourceProperties = eventProperty.getProperties("SOUR", false);
            for (int i = 0; i < sourceProperties.length; i++) {
                Source birthSource = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
                if (source.compareTo(birthSource) == 0) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                try {
                    // je relie la reference de la source du releve à la propriété de naissance
                    sourcexref = (PropertyXRef) eventProperty.addProperty("SOUR", "@" + source.getId() + "@");
                    sourcexref.link();
                } catch (GedcomException ex) {
                    throw new Exception(String.format("Link source=%s error=% ", source.getTitle(), ex.getMessage()));
                }
            }
        } else {
            // je cree une nouvelle source et je la relie à l'entité
            Source newSource = (Source) eventProperty.getGedcom().createEntity(Gedcom.SOUR);
            newSource.addProperty("TITL", record.getEventSource());
            try {
                // je relie la source du releve à l'entité
                sourcexref = (PropertyXRef) eventProperty.addProperty("SOUR", "@" + newSource.getId() + "@");
                sourcexref.link();
            } catch (GedcomException ex) {
                throw new Exception(String.format("Link source=%s error=% ", source.getTitle(), ex.getMessage()));
            }
        }

        // j'ajoute la page
        if (sourcexref!=null && !record.getEventPage().isEmpty())  {
            String value = record.getEventPage();
            sourcexref.addProperty("PAGE", value);
        }

    }

    /**
     * ajoute la date et le lieu de naissance et une note pour indiquer la source
     * de la naissance  dans la propriete BIRT d'un individu
     * @param indi      individu
     * @param birthDate date de naissance
     * @param place     lieu de naissance
     * @param record    releve servant a renseigner la note
     */
    static protected void copyBirthDate(Indi indi, PropertyDate birthDate, String place, MergeRecord record ) {
        Property birthProperty = indi.getProperty("BIRT");
        if (birthProperty == null) {
            birthProperty = indi.addProperty("BIRT", "");
        }
        // j'ajoute (ou remplace ) la date de la naissance
        PropertyDate propertyDate = (PropertyDate) birthProperty.getProperty("DATE");
        if (propertyDate == null) {
            propertyDate = (PropertyDate) birthProperty.addProperty("DATE", "");
        }
        propertyDate.setValue(birthDate.getValue());

        // j'ajoute le lieu
        if (!place.isEmpty()) {
            PropertyPlace propertyPlace = (PropertyPlace) birthProperty.getProperty("PLAC");
            if (propertyPlace == null) {
                propertyPlace = (PropertyPlace) birthProperty.addProperty("PLAC", "");
            }
            propertyPlace.setValue(place);
        }

        // j'ajoute une note indiquant l'origine de la date de naissance
        String noteText ;
        switch ( record.getType()) {
            case Birth:
                noteText = MessageFormat.format("Naissance {0} déduite de l''acte de naissance de {1} {2} le {3} ({4})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Marriage:
                noteText = MessageFormat.format("Naissance {0} déduite de l''acte de mariage de {1} {2} et {3} {4} le {5} ({6})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Death:
                noteText = MessageFormat.format("Naissance {0} déduite de l''acte de décès de {1} {2} le {3} ({4})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            default:
                noteText = MessageFormat.format("Naissance {0} déduite de l''acte {1} entre {2} {3} et {4} {5} le {6} ({7})",
                    propertyDate.getDisplayValue(),
                    record.getEventType(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()+ (record.getNotary().isEmpty() ? "" : ", "+ record.getNotary() )
                    );
        
        }
        
        Property[] notes = birthProperty.getProperties("NOTE");
        boolean found = false;
        for( int i=0; i < notes.length ; i++ ) {
            if( notes[i].getValue().contains(noteText)) {
                found = true;
                break;
            }
        }

        if (!found) {
            Property propertyNote = birthProperty.getProperty("NOTE");
            if (propertyNote == null) {
                // je cree une note .
                propertyNote = birthProperty.addProperty("NOTE", "");
            }

            // j'ajoute le commentaire du deces a la fin de la note existante.
            String value = propertyNote.getValue();
            if (!noteText.isEmpty()) {
                if (!value.isEmpty()) {
                    value += "\n";
                }
                value += noteText;
                propertyNote.setValue(value);
            }
        }
    }

    /**
     * ajoute la date et le lieu de naissance et une note pour indiquer la source
     * de la naissance  dans la propriete BIRT d'un individu
     * @param indi      individu
     * @param deathDate date de naissance
     * @param place     lieu de naissance
     * @param record    releve servant a renseigner la note
     */
    static protected void copyDeathDate(Indi indi, PropertyDate deathDate, String place, MergeRecord record ) {
        Property deathProperty = indi.getProperty("DEAT");
        if (deathProperty == null) {
            deathProperty = indi.addProperty("DEAT", "");
        }
        // j'ajoute (ou remplace ) la date de la naissance
        PropertyDate propertyDate = (PropertyDate) deathProperty.getProperty("DATE");
        if (propertyDate == null) {
            propertyDate = (PropertyDate) deathProperty.addProperty("DATE", "");
        }
        propertyDate.setValue(deathDate.getValue());

        // j'ajoute le lieu
        if (!place.isEmpty()) {
            PropertyPlace propertyPlace = (PropertyPlace) deathProperty.getProperty("PLAC");
            if (propertyPlace == null) {
                propertyPlace = (PropertyPlace) deathProperty.addProperty("PLAC", "");
            }
            propertyPlace.setValue(place);
        }

        // j'ajoute une note indiquant l'origine de la date de naissance
        String noteText ;
        switch ( record.getType()) {
            case Birth:
                noteText = MessageFormat.format("Date de décès {0} déduite de l''acte de naissance de {1} {2} le {3} ({4})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Marriage:
                noteText = MessageFormat.format("Date de décès {0} déduite de l''acte de mariage de {1} {2} et {3} {4} le {5} ({6})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Death:
                noteText = MessageFormat.format("Date de décès {0} déduite de l''acte de décès de {1} {2} le {3} ({4})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            default:
                noteText = MessageFormat.format("Date de décès {0} déduite de l''acte {1} entre {2} {3} et {4} {5} le {6} ({7})",
                    propertyDate.getDisplayValue(),
                    record.getEventType(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()+ (record.getNotary().isEmpty() ? "" : ", "+ record.getNotary() )
                    );

        }
        
        Property[] notes = deathProperty.getProperties("NOTE");
        boolean found = false;
        for( int i=0; i < notes.length ; i++ ) {
            if( notes[i].getValue().contains(noteText)) {
                found = true;
                break;
            }
        }

        if (!found) {            
            Property propertyNote = deathProperty.getProperty("NOTE");
            if (propertyNote == null) {
                // je cree une note .
                propertyNote = deathProperty.addProperty("NOTE", "");
            }

            // j'ajoute le commentaire du deces a la fin de la note existante.
            String value = propertyNote.getValue();
            if (!noteText.isEmpty()) {
                if (!value.isEmpty()) {
                    value += "\n";
                }
                value += noteText;
                propertyNote.setValue(value);
            }
        }
    }


    /**
     * ajoute la date de marriage et une note pour indiquer la source dans
     * la propriete MARR d'une famille
     * de de cette date .
     * @param family            famille de mariés
     * @param marriageDate      date de marriage
     * @param occupationDate    date du releve
     * @param record            releve servant a renseigner la note 
     */
    static protected void copyMarriageDate(Fam family, PropertyDate marriageDate, MergeRecord record ) {
        // j'ajoute (ou remplace) la date du mariage des parents
        // je crée la propriété MARR
        Property marriageProperty = family.getProperty("MARR");
        if (marriageProperty == null) {
            marriageProperty = family.addProperty("MARR", "");
        }
        // j'ajoute (ou remplace ) la date de la naissance
        PropertyDate propertyDate = (PropertyDate) marriageProperty.getProperty("DATE");
        if (propertyDate == null) {
            propertyDate = (PropertyDate) marriageProperty.addProperty("DATE", "");
        }
        propertyDate.setValue(marriageDate.getValue());

        // j'ajoute une note indiquant l'origine de la date de naissance
        String noteText ;
        switch ( record.getType()) {
            case Birth:
                noteText = MessageFormat.format("Date de mariage {0} déduite de l''acte de naissance de {1} {2} le {3} ({4})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
               break;
            case Marriage:
             noteText = MessageFormat.format("Date de mariage {0} déduite de l''acte de mariage de {1} {2} et {3} {4} le {5} ({6})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Death:
            noteText = MessageFormat.format("Date de mariage {0} déduite de l''acte de décès de {1} {2} le {3} ({4})",
                    propertyDate.getDisplayValue(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            default:
             noteText = MessageFormat.format("Date de mariage {0} déduite de l''acte {1} entre {2} {3} et {4} {5} le {6} ({7})",
                    propertyDate.getDisplayValue(),
                    record.getEventType(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()+ (record.getNotary().isEmpty() ? "" : ", "+ record.getNotary() )
                    );
        }
        Property[] notes = marriageProperty.getProperties("NOTE");
        boolean found = false;
        for( int i=0; i < notes.length ; i++ ) {
            if( notes[i].getValue().contains(noteText)) {
                found = true;
                break;
            }
        }
        if (!found) {
            if ( notes.length > 0 ) {
                notes[0].setValue(notes[0].getValue()+ "\n" +noteText );
            } else {
                marriageProperty.addProperty("NOTE",noteText);
            }
        } 
    }

    /**
     * ajoute la profession a un individu
     * Si la profession existe deja a la meme date, l'invidu n'est pas modifié.
     * 
     * @param indi            individu
     * @param occupation      profession
     * @param occupationDate  date du releve
     * @param record    releve servant a renseigner la note de la profession
     */
    static protected void copyOccupation(Indi indi, String occupation, String residence, MergeRecord record ) {
        PropertyDate occupationDate = record.getEventDate();
        // je cherche si l'individu a deja un tag OCCU a la meme date
        Property occupationProperty = null;
        String occupationLabel ="";
        // j'ajoute la profession ou la residence
        if( !occupation.isEmpty()) {
            occupationProperty = indi.addProperty("OCCU", "");
            occupationProperty.setValue(occupation);
            occupationLabel ="Profession indiquée";
        } else if (!residence.isEmpty()) {
            occupationProperty = indi.addProperty("RESI", "");
            occupationLabel ="Domicile indiqué";
        }

        if (occupationProperty != null) {
            // j'ajoute la date
            PropertyDate date = (PropertyDate) occupationProperty.addProperty("DATE", "");
            date.setValue(occupationDate.getValue());
            // j'ajoute le lieu
            if (!residence.isEmpty()) {
                PropertyPlace place = (PropertyPlace) occupationProperty.addProperty("PLAC", "");
                place.setValue(residence);
            }
            
            // j'ajoute une note indiquant la source
            String noteText ;
            switch ( record.getType()) {
                case Birth:
                noteText = MessageFormat.format(occupationLabel + " dans l''acte de naissance de {0} {1} le {2} ({3})",
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName()
                        );
                    break;
                case Marriage:
                    noteText = MessageFormat.format(occupationLabel + " dans l''acte de mariage de {0} {1} et {2} {3} le {4} ({5})",
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getWifeFirstName(),
                        record.getWifeLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName()
                        );
                    break;
                case Death:
                    noteText = MessageFormat.format(occupationLabel + " dans l''acte de décès de {0} {1} le {2} ({3})",
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName()
                        );
                    break;
                default:
                    noteText = MessageFormat.format(occupationLabel + " dans l''acte {0} entre {1} {2} et {3} {4} le {5} ({6})",
                        record.getEventType(),
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getWifeFirstName(),
                        record.getWifeLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName()+ (record.getNotary().isEmpty() ? "" : ", "+ record.getNotary() )
                        );
            }
            occupationProperty.addProperty("NOTE", noteText);
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // utilitaires
    ///////////////////////////////////////////////////////////////////////////

    /**
     * concatene plusieurs commentaires dans une chaine , séparés par une virgule
     */
    public String appendValue(String value, String... otherValues) {
        int fieldSize = value.length();
        StringBuilder sb = new StringBuilder();
        sb.append(value.trim());
        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValue.trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (fieldSize > 0) {
                    sb.append(", ");
                }
                sb.append(otherValue.trim());
                fieldSize += otherValue.length();
            }
        }

        return sb.toString();
    }

    /**
     * retourne false si la ligne parent existe et est NON_APPLICABLE
     * sinon retoune true
     * @param rowType
     * @return
     */
    private boolean isRowParentApplicable(RowType rowType) {
        MergeRow parent = getParentRow(rowType);
        if (parent== null) {
            return true;
        } else {
            return parent.compareResult != CompareResult.NOT_APPLICABLE;
        }
    }

    /**
     * retourne le libellé d'un type de ligne donnée
     * le libellé est indenté si la ligne a une ligne parent
     * @param rowType
     * @return
     */
    private String getRowTypeLabel(RowType rowType) {
        String label = "";
        if(getParentRow(rowType)!= null) {
            label = "  ";
        }
        label += NbBundle.getMessage(MergeModel.class, "MergeModel."+rowType.toString());
        return label;
    }

    /**
     * retourne la ligne parent d'une ligne donnée
     * @param rowType
     * @return
     */
    MergeRow getParentRow(RowType rowType) {
        MergeRow parent;
        switch (rowType) {
            case IndiParentMarriageDate:
            case IndiFatherLastName:
            case IndiFatherFirstName:
            case IndiFatherBirthDate:
            case IndiFatherDeathDate:
            case IndiFatherOccupation:
            case IndiMotherFirstName:
            case IndiMotherLastName:
            case IndiMotherBirthDate:
            case IndiMotherDeathDate:
            case IndiMotherOccupation:
                parent= getRow(RowType.IndiParentFamily);
                break;

            case IndiMarriedFirstName:
            case IndiMarriedLastName:
            case IndiMarriedBirthDate:
            case IndiMarriedDeathDate:
            case IndiMarriedOccupation:
            case IndiMarriedMarriageDate:
                parent= getRow(RowType.IndiMarriedFamily);
                break;

            case WifeParentMarriageDate:
            case WifeFatherFirstName:
            case WifeFatherLastName:
            case WifeFatherBirthDate:
            case WifeFatherDeathDate:
            case WifeFatherOccupation:
            case WifeMotherFirstName:
            case WifeMotherLastName:
            case WifeMotherBirthDate:
            case WifeMotherDeathDate:
            case WifeMotherOccupation:
                parent= getRow(RowType.WifeParentFamily);
                break;

            case WifeMarriedFirstName:
            case WifeMarriedLastName:
            case WifeMarriedBirthDate:
            case WifeMarriedDeathDate:
            case WifeMarriedOccupation:
            case WifeMarriedMarriageDate:
                parent= getRow(RowType.WifeMarriedFamily);
                break;

            case EventPage:
                parent= getRow(RowType.EventSource);
                break;

            default:
                parent= null;

        }
        return parent;
    }

    /**
     * liste des types de ligne
     */
    static protected enum RowType {
        Separator,
        EventSource,
        EventPage,
        EventDate,
        EventPlace,
        EventComment,
        MarriageFamily,
        MarriageDate,
        //  indi ///////////////////////////////////////////////////////////////////
        IndiFirstName,
        IndiLastName,
        IndiSex,
        IndiAge,
        IndiBirthDate,
        IndiDeathDate,
        IndiBirthPlace,
        IndiResidence,
        IndiOccupation,
        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        IndiMarriedFamily,
        IndiMarriedMarriageDate,
        IndiMarriedFirstName,
        IndiMarriedLastName,
        IndiMarriedBirthDate,
        IndiMarriedDeathDate,
        IndiMarriedOccupation,
        //  indi father ////////////////////////////////////////////////////////////
        IndiParentFamily,
        IndiParentMarriageDate,
        IndiFatherFirstName,
        IndiFatherLastName,
        IndiFatherBirthDate,
        IndiFatherDeathDate,
        IndiFatherOccupation,
        IndiMotherFirstName,
        IndiMotherLastName,
        IndiMotherBirthDate,
        IndiMotherDeathDate,
        IndiMotherOccupation,
        //  wife ///////////////////////////////////////////////////////////////////
        WifeFirstName,
        WifeLastName,
        WifeSex,
        //wifeDead,
        wifeAge,
        WifeBirthDate,
        WifeDeathDate,
        WifePlace,
        WifeOccupation,
        //  wifeMarried ///////////////////////////////////////////////////////////
        WifeMarriedFamily,
        WifeMarriedMarriageDate,
        WifeMarriedFirstName,
        WifeMarriedLastName,
        WifeMarriedBirthDate,
        WifeMarriedDeathDate,
        WifeMarriedOccupation,
        //  wifeFather ///////////////////////////////////////////////////////////
        WifeParentFamily,
        WifeParentMarriageDate,
        WifeFatherFirstName,
        WifeFatherLastName,
        WifeFatherBirthDate,
        WifeFatherDeathDate,
        WifeFatherOccupation,
        WifeMotherFirstName,
        WifeMotherLastName,
        WifeMotherBirthDate,
        WifeMotherDeathDate,
        WifeMotherOccupation,
    }

}
