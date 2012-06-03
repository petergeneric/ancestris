package ancestris.modules.releve.dnd;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import java.text.MessageFormat;
import java.util.ArrayList;
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
        //TODO  ajouter Tooltip

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
    static protected List<MergeModel> createMergeModel(MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity) throws Exception {
        List<MergeModel> models = new ArrayList<MergeModel>();
        if( mergeRecord.getType() ==  MergeRecord.RecordType.Birth) {
            if ( selectedEntity instanceof Fam ) {
                // l'entité selectionnée est une famille
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
                // l'entité selectionnée est un individu
                Indi selectedIndi = (Indi) selectedEntity;

                // je cherche les familles compatibles avec le releve de naissance
                List<Fam> families = MergeQuery.findFamilyCompatibleWithBirthRecord(mergeRecord, gedcom, null);

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
                    sameIndis = MergeQuery.findIndiCompatibleWithBirthRecord(mergeRecord, gedcom, selectedIndi);
                } else {
                    // l'individu n'est pas lié a une famille précise, je l'inclue dans la recherche
                    sameIndis = MergeQuery.findIndiCompatibleWithBirthRecord(mergeRecord, gedcom, null);
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
                // pas d'entité selectionnee

                // j'ajoute un nouvel individu , sans famille associée
                models.add(new MergeModelBirth(mergeRecord, gedcom));

                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                List<Indi> sameIndis = MergeQuery.findIndiCompatibleWithBirthRecord(mergeRecord, gedcom, null);

                // je cherche les familles compatibles avec le releve de naissance
                List<Fam> families = MergeQuery.findFamilyCompatibleWithBirthRecord(mergeRecord, gedcom, null);

                // j'ajoute les individus compatibles
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

                // j'ajoute un nouvel individu avec les familles compatibles
                for(Fam family : families) {
                    models.add(new MergeModelBirth(mergeRecord, gedcom, null, family));
                }

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
           
        } else  if( mergeRecord.getType() ==  MergeRecord.RecordType.Marriage) {
            if ( selectedEntity instanceof Fam ) {
                // l'entité selectionnée est une famille
                Fam selectedFamily = (Fam) selectedEntity;

                // j'ajoute un modele avec la famille selectionne
                models.add(new MergeModelMarriage(mergeRecord, gedcom, selectedFamily));

            } if ( selectedEntity instanceof Indi ) {
                Indi selectedIndi = (Indi) selectedEntity;

                // l'entité selectionnée est un individu

                // je cherche les familles avec l'individu selectionné
                Fam[] families = selectedIndi.getFamiliesWhereSpouse();
                // j'ajoute les familles compatibles
                for(Fam family : families) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, family));
                }


            } else {
                // pas d'entité selectionnee

                // j'ajoute une nouvelle famille
                models.add(new MergeModelMarriage(mergeRecord, gedcom));

                // je recherche les familles compatibles
                List<Fam> families = MergeQuery.findFamilyCompatibleWithMarriageRecord(mergeRecord, gedcom, null);
                // j'ajoute les familles compatibles
                for(Fam family : families) {
                    models.add(new MergeModelMarriage(mergeRecord, gedcom, family));
                }

                // je recherche les individus compatibles
                List<Indi> husbands = new ArrayList<Indi>();
                List<Indi> wifes = new ArrayList<Indi>();
                MergeQuery.findHusbanWifeCompatibleWithMarriageRecord(mergeRecord, gedcom, families, husbands, wifes);
                for(Indi husband : husbands) {
                    for(Indi wife : wifes) {
                        models.add(new MergeModelMarriage(mergeRecord, gedcom, husband, wife));
                    }
                }             
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
                mergeRow.compareResult = !recordValue.equals(entityValue) ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            } else {
                mergeRow.merge = !recordValue.equals(entityValue);
                mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            }
        } else {
            // ligne parent NOT_APPLICABLE
            mergeRow.merge = false;
            mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        }


        // j'incremente le compteur de champs egaux
        if ( mergeRow.compareResult == CompareResult.EQUAL && !recordValue.isEmpty())  {
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
            if (recordValue == null) {
                mergeRow.merge = false;
                mergeRow.compareResult = CompareResult.EQUAL;
            } else if (entityValue == null) {
                // j'active Merge seulement si la date du releve est comparable
                mergeRow.merge = recordValue.isComparable();
                mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            } else {
                switch (rowType) {
                    case EventDate:

                    case IndiBirthDate:
                    case IndiFatherBirthDate:
                    case IndiMotherBirthDate:
                    case IndiDeathDate:
                    case IndiFatherDeathDate:
                    case IndiMotherDeathDate:
                    case IndiParentMarriageDate:

                    case WifeBirthDate:
                    case WifeFatherBirthDate:
                    case WifeMotherBirthDate:
                    case WifeDeathDate:
                    case WifeFatherDeathDate:
                    case WifeMotherDeathDate:
                    case WifeParentMarriageDate:

                        if (recordValue.getValue().equals(entityValue.getValue())) {
                            // les valeurs sont egales, pas besoin de merger
                            mergeRow.merge = false;
                            mergeRow.compareResult = CompareResult.EQUAL;
                        } else {
                            // la valeur ne sont pas egales
                            // je verifie si la date du releve est plus précise
                            mergeRow.merge = MergeQuery.isBestBirthDate(recordValue, entityValue);
                            // TODO traiter le cas ou les dates sont incompatibles
                            mergeRow.compareResult = CompareResult.COMPATIBLE;
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
        if ( mergeRow.compareResult == CompareResult.EQUAL)  {
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
    void addRow(RowType rowType, String recordSourceName, Source source, Property eventProperty) {
         MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.recordValue = recordSourceName;

        if (isRowParentApplicable(rowType)) {

            if (source != null) {
                // je recherche les sources de l'envenement
                boolean found = false;
                if (eventProperty != null) {
                    Property[] sourceProperties = eventProperty.getProperties("SOUR", false);
                    for (int i = 0; i < sourceProperties.length; i++) {
                        Source birthSource = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
                        if (source.compareTo(birthSource) == 0) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    mergeRow.entityValue = source;
                    mergeRow.entityObject = source;
                    mergeRow.merge = false;
                    mergeRow.compareResult = CompareResult.EQUAL;
                } else {
                    mergeRow.entityValue = null;
                    mergeRow.entityObject = source;
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
                if ((rowType == rowType.IndiParentFamily && record.getIndiFatherLastName().isEmpty() && record.getIndiMotherLastName().isEmpty())
                        || (rowType == rowType.WifeParentFamily && record.getWifeFatherLastName().isEmpty() && record.getWifeMotherLastName().isEmpty())
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
    protected abstract Entity getSelectedEntity();
    protected abstract Gedcom getGedcom();

    /**
     * i
     */
    private String[] columnNames = {
        "",
        NbBundle.getMessage(MergeModel.class, "MergePanel.title.recordColumn"),
        "=>",
        NbBundle.getMessage(MergeModel.class, "MergePanel.title.indiColumn"),
        "Entité"
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
    public Class getColumnClass(int col) {
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
                dataList.get(row).merge = (Boolean) value;
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
        dataList.get(rowNum).merge = state;


        // je met a jour les lignes filles
        RowType currentRowType = dataList.get(rowNum).rowType;
        for(int i =0 ; i < dataList.size();i++) {
            MergeRow mergeRow = dataList.get(i);
            if (getRowParent(mergeRow.rowType).rowType == currentRowType ) {
                if (state == true) {
                    // je restaure l'eata initial
                    mergeRow.merge =  mergeRow.merge_initial;
                } else {
                    mergeRow.merge =  false;
                }
            }
        }
    }


    /**
     * coche ou décoche une ligne en fonction de sont type
     * @param rowNum
     * @param state
     */
    void check(RowType rowType, boolean state) {
        dataMap.get(rowType).merge = state;
    }

    /**
     * retourne l'etat coché ou décoché d'une ligne
     * @param rowType
     * @return
     */
    boolean isChecked(RowType rowType) {
        return dataMap.get(rowType).merge;
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
        Property propertyPlace = eventProperty.getProperty("PLAC");
        if (propertyPlace == null) {
            // je cree le lieu .
            propertyPlace = eventProperty.addProperty("PLAC", "");
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
                    PropertyXRef sourcexref = (PropertyXRef) eventProperty.addProperty("SOUR", "@" + source.getId() + "@");
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
                PropertyXRef sourcexref = (PropertyXRef) eventProperty.addProperty("SOUR", "@" + newSource.getId() + "@");
                sourcexref.link();
            } catch (GedcomException ex) {
                throw new Exception(String.format("Link source=%s error=% ", source.getTitle(), ex.getMessage()));
            }
        }
    }

    /**
     * ajoute la date et le lieu de naissance et une note pour indiquer la source
     * de la naissance  dans la propriete BIRT d'un individu
     * @param indi            individu
     * @param birthDate       date de naissance
     * @param occupationDate  date du releve
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
                noteText = MessageFormat.format("Naissance indiquée dans l''acte de naissance de {0} {1} le {2} ( {3} ) ",
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Marriage:
                noteText = MessageFormat.format("Naissance indiquée dans l''acte de mariage de {0} {1} et {2} {3} le {4} ( {5} ) ",
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Death:
                noteText = MessageFormat.format("Naissance indiquée dans l''acte de décès de {0} {1} le {1} ( {2} ) ",
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            default:
                noteText = MessageFormat.format("Naissance indiquée dans l''acte {0} entre {1} {2} et {3} {4} le {5} ( {6}, {7}) ",
                    record.getEventType(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName(),
                    record.getNotary()
                    );
        
        }
        birthProperty.addProperty("NOTE", noteText);
    }

    /**
     * ajoute la date de marriage et une note pour indiquer la source dans
     * la propriete MARR d'une famille
     * de de cette date .
     * @param family            marriage
     * @param birthDate         date de naissance
     * @param occupationDate    date du releve
     * @param record            releve servant a renseigner la note 
     */
    static protected void copyMarriageDate(Fam family, PropertyDate birthDate, MergeRecord record ) {
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
        propertyDate.setValue(birthDate.getValue());

        // j'ajoute une note indiquant l'origine de la date de naissance
        String noteText ;
        switch ( record.getType()) {
            case Birth:
                noteText = MessageFormat.format("Date de mariage déduite de l''acte de naissance de {0} {1} le {2} ( {3} ) ",
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
               break;
            case Marriage:
             noteText = MessageFormat.format("Date de mariage déduite de l''acte de mariage de {0} {1} et {2} {3} le {4} ( {5} ) ",
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            case Death:
            noteText = MessageFormat.format("Date de mariage déduite de l''acte de décès de {0} {1} le {1} ( {2} ) ",
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName()
                    );
                break;
            default:
             noteText = MessageFormat.format("Date de mariage déduite de l''acte {0} entre {1} {2} et {3} {4} le {5} ( {6}, {7}) ",
                    record.getEventType(),
                    record.getIndiFirstName(),
                    record.getIndiLastName(),
                    record.getWifeFirstName(),
                    record.getWifeLastName(),
                    record.getEventDateDDMMYYYY(),
                    record.getEventPlaceCityName(),
                    record.getNotary()
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
    static protected void copyOccupation(Indi indi, String occupation, MergeRecord record ) {
        PropertyDate occupationDate = record.getEventDate();
        // je cherche si l'individu a deja un tag OCCU a la meme date
        Property occupationProperty = MergeQuery.findOccupation(indi, occupation, occupationDate);
        if (occupationProperty == null) {
            // j'ajoute la profession 
            occupationProperty = indi.addProperty("OCCU", "");
            occupationProperty.setValue(occupation);
            // j'ajoute la date
            PropertyDate date = (PropertyDate) occupationProperty.addProperty("DATE", "");
            date.setValue(occupationDate.getValue());
            // j'ajoute le lieu
            if (!record.getEventPlace().isEmpty()) {
                PropertyPlace place = (PropertyPlace) occupationProperty.addProperty("PLAC", "");
                place.setValue(record.getEventPlace());
            }
            
            // j'ajoute une note indiqunt la source
            String noteText ;
            switch ( record.getType()) {
                case Birth:
                noteText = MessageFormat.format("Profession indiquée dans l''acte de naissance de {0} {1} le {2} ( {3} ) ",
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName()
                        );
                    break;
                case Marriage:
                    noteText = MessageFormat.format("Profession indiquée dans l''acte de mariage de {0} {1} et {2} {3} le {4} ( {5} ) ",
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getWifeFirstName(),
                        record.getWifeLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName()
                        );
                    break;
                case Death:
                    noteText = MessageFormat.format("Profession indiquée dans l''acte de décès de {0} {1} le {1} ( {2} ) ",
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName()
                        );
                    break;
                default:
                    noteText = MessageFormat.format("Profession indiquée dans l''acte {0} entre {1} {2} et {3} {4} le {5} ( {6}, {7}) ",
                        record.getEventType(),
                        record.getIndiFirstName(),
                        record.getIndiLastName(),
                        record.getWifeFirstName(),
                        record.getWifeLastName(),
                        record.getEventDateDDMMYYYY(),
                        record.getEventPlaceCityName(),
                        record.getNotary()
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
        MergeRow parent = getRowParent(rowType);
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
        if(getRowParent(rowType)!= null) {
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
    MergeRow getRowParent(RowType rowType) {
        MergeRow parent;
        switch (rowType) {
            //case IndiParentFamily:
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
        EventDate,
        EventPlace,
        EventComment,
        MarriageFamily,
        //  indi ///////////////////////////////////////////////////////////////////
        IndiFirstName,
        IndiLastName,
        IndiSex,
        IndiAge,
        IndiBirthDate,
        IndiDeathDate,
        IndiPlace,
        IndiOccupation,
        //IndiComment,
        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        indiMarriedFirstName,
        indiMarriedLastName,
        indiMarriedDead,
        indiMarriedOccupation,
        //indiMarriedComment,
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
        WifeComment,
        //  wifeMarried ///////////////////////////////////////////////////////////
        wifeMarriedFirstName,
        wifeMarriedLastName,
        wifeMarriedDead,
        wifeMarriedOccupation,
        wifeMarriedComment,
        //  wifeFather ///////////////////////////////////////////////////////////
        WifeParentFamily,
        WifeParentMarriageDate,
        WifeFatherFirstName,
        WifeFatherLastName,
        WifeFatherBirthDate,
        WifeFatherDeathDate,
        WifeFatherOccupation,
        WifeFatherComment,
        WifeMotherFirstName,
        WifeMotherLastName,
        WifeMotherBirthDate,
        WifeMotherDeathDate,
        WifeMotherOccupation,
        WifeMotherComment,
    }

}
