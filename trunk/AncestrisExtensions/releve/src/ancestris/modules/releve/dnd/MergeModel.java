package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.MergeRecord.MergeParticipant;
import ancestris.modules.releve.dnd.MergeRecord.MergeParticipantType;
import ancestris.modules.releve.dnd.MergeRecord.RecordType;
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
import genj.gedcom.TagPath;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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
        CompareResult compareResult;
        Entity entityObject = null;

        @Override
        public String toString() {
            return rowType.name() + " " + recordValue + " " + entityValue;
        }
    }

    protected enum CompareResult {

        EQUAL,
        COMPATIBLE,
        CONFLIT,
        NOT_APPLICABLE
    }
    protected MergeRecord record;
    protected Gedcom gedcom;
    // type de participant 
    protected MergeParticipantType participantType;
    protected MergeParticipant participant;
    protected boolean showFrenchCalendarDate = true;
    private EnumMap<RowType, MergeRow> dataMap = new EnumMap<RowType, MergeRow>(RowType.class);
    private List<MergeRow> dataList = new ArrayList<MergeRow>();
    private int nbMatch = 0;
    private int nbMatchMax = 0;

    static protected List<MergeModel> createMergeModel(MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity) throws Exception {
        return createMergeModel(mergeRecord, gedcom, selectedEntity, false);
    }

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
    static protected List<MergeModel> createMergeModel(MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity, boolean showNewParents) throws Exception {
        List<MergeModel> models;
        if (mergeRecord.getType() == MergeRecord.RecordType.Birth) {
            models = MergeModelBirth.createMergeModelBirth(mergeRecord, gedcom, selectedEntity, showNewParents);
        } else if (mergeRecord.getType() == MergeRecord.RecordType.Marriage) {
            models = MergeModelMarriage.createMergeModelMarriage(mergeRecord, gedcom, selectedEntity, showNewParents);
        } else if (mergeRecord.getType() == MergeRecord.RecordType.Death) {
            models = MergeModelDeath.createMergeModelDeath(mergeRecord, gedcom, selectedEntity, showNewParents);
        } else if (mergeRecord.getType() == MergeRecord.RecordType.Misc) {
            if (mergeRecord.getEventTypeTag() == MergeRecord.EventTypeTag.MARC) {
                // Contrat de mariage
                models = MergeModelMiscMarc.createMergeModelMiscMarc(mergeRecord, gedcom, selectedEntity, showNewParents);
            } else if (mergeRecord.getEventTypeTag() == MergeRecord.EventTypeTag.WILL) {
                // Testament
                models = MergeModelMiscWill.createMergeModelMiscWill(mergeRecord, gedcom, selectedEntity, showNewParents);
            } else {
                // Autre evenement (quittance, obligation, emancipation, enregistrement, insinuation ...;
                models = MergeModelMiscOther.createMergeModelMiscOther(mergeRecord, gedcom, selectedEntity, showNewParents);
            }
        } else {
            // je retourne une liste de modeles vide.
            models = new ArrayList<MergeModel>();
        }

        // je trie les modeles par ordre décroissant du nombre de champs egaux entre le relevé et l'entité du gedcom
        Collections.sort(models);

        return models;
    }

    ///////////////////////////////////////////////////////////////////////////
    // constructeurs
    ///////////////////////////////////////////////////////////////////////////
    MergeModel(MergeRecord record, Gedcom gedcom) {
        this(record, MergeParticipantType.participant1, gedcom);
    }

    MergeModel(MergeRecord record, MergeParticipantType participantType, Gedcom gedcom) {
        this.record = record;
        this.gedcom = gedcom;
        this.participantType = participantType;
        this.participant = record.getParticipant(participantType);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ajout de lignes dans le modele
    ///////////////////////////////////////////////////////////////////////////
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
        if (isRowParentApplicable(rowType)) {
            if (recordValue.isEmpty()) {
                mergeRow.merge = false;
                mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                if (rowType == RowType.IndiLastName
                        || rowType == RowType.IndiMarriedLastName
                        || rowType == RowType.IndiFatherLastName
                        || rowType == RowType.IndiMotherLastName
                        || rowType == RowType.WifeLastName
                        || rowType == RowType.WifeMarriedLastName
                        || rowType == RowType.WifeFatherLastName
                        || rowType == RowType.WifeMotherLastName) {
                    if (entityValue.isEmpty()) {
                        mergeRow.merge = !recordValue.equals(entityValue);
                        mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    } else {
                        mergeRow.merge = false;
                        // il suffit que l'un des noms soit identique 
                        String[] names1 = recordValue.split(",");
                        String[] names2 = entityValue.split(",");
                        boolean result = false;
                        for (String name1 : names1) {
                            for (String name2 : names2) {
                                result |= name1.trim().equals(name2.trim());
                            }
                        }
                        mergeRow.compareResult = !result ? CompareResult.CONFLIT : CompareResult.EQUAL;
                    }

                } else if (rowType == RowType.IndiFirstName
                        || rowType == RowType.IndiMarriedFirstName
                        || rowType == RowType.IndiFatherFirstName
                        || rowType == RowType.IndiMotherFirstName
                        || rowType == RowType.WifeFirstName
                        || rowType == RowType.WifeMarriedFirstName
                        || rowType == RowType.WifeFatherFirstName
                        || rowType == RowType.WifeMotherFirstName) {
                    if (entityValue.isEmpty()) {
                        mergeRow.merge = !recordValue.equals(entityValue);
                        mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    } else {
                        mergeRow.merge = false;
                        mergeRow.compareResult = !recordValue.equals(entityValue) ? CompareResult.CONFLIT : CompareResult.EQUAL;
                    }

                } else if (rowType == RowType.EventComment) {
                    // merge actif si le commentaire existant dans l'entité ne contient pas deja le commentaire du relevé.
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
        if ((mergeRow.compareResult == CompareResult.EQUAL || mergeRow.compareResult == CompareResult.CONFLIT)
                && !recordValue.isEmpty() && !entityValue.isEmpty()) {
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
        // je clone la valeur , y compris la phase
        PropertyDate cloneDate = new PropertyDate();
        cloneDate.setValue(recordValue.getFormat(), recordValue.getStart(), recordValue.getEnd(), recordValue.getPhrase());
        mergeRow.recordValue = cloneDate;

        if (isRowParentApplicable(rowType)) {
            // je compare les valeurs par defaut du releve et de l'entite
            if (recordValue == null || (recordValue != null && !recordValue.isComparable())) {
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
                    case IndiMarriedMarriageDate:
                    case IndiMarriedBirthDate:
                    case IndiMarriedDeathDate:
                    case IndiFatherBirthDate:
                    case IndiMotherBirthDate:
                    case IndiFatherDeathDate:
                    case IndiMotherDeathDate:
                    case IndiParentMarriageDate:

                    case WifeBirthDate:
                    case WifeDeathDate:
                    case WifeMarriedMarriageDate:
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
                            if (bestDate == null) {
                                mergeRow.merge = false;
                                mergeRow.compareResult = CompareResult.CONFLIT;
                            } else if (bestDate == entityValue) {
                                mergeRow.merge = false;
                                mergeRow.compareResult = MergeQuery.isCompatible(recordValue, entityValue) ? CompareResult.COMPATIBLE : CompareResult.CONFLIT;
                            } else {
                                // je propose une date plus precise que celle du releve
                                cloneDate.setValue(bestDate.getFormat(), bestDate.getStart(), bestDate.getEnd(), bestDate.getPhrase());
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
        if (mergeRow.compareResult == CompareResult.EQUAL) {
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
    void addRowSource(RowType rowType, String recordSourceTitle, Property entityEventProperty) {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.recordValue = recordSourceTitle;

        //MergeQuery.getSourceTitle(entitySourceProperty);
        //Property entitySourceProperty = MergeQuery.getEntitySourceProperty(record, );

//        if ( recordSourceTitle.isEmpty()) {
//            if (record.getType() == RecordType.Misc) {
//                if (!record.getNotary().isEmpty()) {
//                    recordSourceTitle = String.format("Notaire %s", record.getNotary());
//                } else {
//                    recordSourceTitle = "";
//                }
//            } else {
//                String cityName = record.getEventPlaceCityName();
//
//                if (record.getEventDate().getStart().getYear() <= 1792) {
//                    recordSourceTitle = String.format("BMS %s", cityName);
//                } else {
//                    recordSourceTitle = String.format("Etat civil %s", cityName);
//                }
//            }
//        }

        Source entityEventSource = null;
        String entityEventPage = null;

        if (entityEventProperty != null) {
            Property[] sourceProperties = entityEventProperty.getProperties("SOUR", false);
            for (int i = 0; i < sourceProperties.length; i++) {
                // remarque : verification de classe PropertySource avant de faire le cast en PropertySource pour eliminer
                // les cas anormaux , par exemple une source "multiline"
                if (sourceProperties[i] instanceof PropertySource) {
                    Source source = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
                    if (recordSourceTitle.compareTo(source.getTitle()) == 0) {
                        entityEventSource = source;
                        // je verifie si elle contient le meme numero de page
                        for (Property pageProperty : sourceProperties[i].getProperties("PAGE")) {
                            if (record.getEventPage().equals(pageProperty.getValue())) {
                                entityEventPage = pageProperty.getValue();
                                break;
                            }
                        }
                    }
                }
                if (entityEventSource != null && entityEventPage != null) {
                    break;
                }
            }
            
        } 
//
//            // je verifie si la source existe deja dans le gedcom
//            String cityName = record.getEventPlaceCityName();
//            String cityCode = record.getEventPlaceCityCode();
//            String countyName = record.getEventPlaceCountyName();
//            //String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyName, cityCode, cityName);
//            String stringPatter = String.format("(?:BMS|Etat\\scivil)(?:\\s++)%s", cityName);
//            Pattern pattern = Pattern.compile(stringPatter);
//            Collection<? extends Entity> sources = gedcom.getEntities("SOUR");
//            for (Entity gedComSource : sources) {
//                if (pattern.matcher(((Source) gedComSource).getTitle()).matches()) {
//                    source = (Source) gedComSource;
//                }
//            }

      
        if (isRowParentApplicable(rowType)) {
            if (entityEventSource != null) {
                mergeRow.entityValue = entityEventSource;
                mergeRow.entityObject = entityEventSource;
                mergeRow.merge = false;
                mergeRow.compareResult = CompareResult.EQUAL;
            } else {
                 // je cherche une source dans le gedcom
                Source newSource = null;
                if ( ! recordSourceTitle.isEmpty()) {
                    Collection<? extends Entity> sources = gedcom.getEntities("SOUR");
                    for (Entity gedComSource : sources) {
                        if (((Source) gedComSource).getTitle().contains(recordSourceTitle)) {
                            newSource = (Source) gedComSource;
                        }
                    }
                }

                mergeRow.entityValue = entityEventSource;
                mergeRow.entityObject = newSource;
                mergeRow.merge = !recordSourceTitle.isEmpty();
                mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            }
        } else {
            // ligne parent NOT_APPLICABLE
            mergeRow.merge = false;
            mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        }
        mergeRow.merge_initial = mergeRow.merge;

        addRow(RowType.EventPage, record.getEventPage(), entityEventPage);
    }

    /**
     * ajoute une ligne dans le modele pour comparer le type d'evenement
     * @param rowType
     * @param label
     * @param recordValues
     * @param entityValues
     */
    void addRow(RowType rowType, String recordEventType, Property eventProperty) {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.recordValue = recordEventType;
        mergeRow.entityValue = eventProperty;
        mergeRow.entityObject = null;

        if (isRowParentApplicable(rowType)) {

            if (eventProperty != null) {
                if (eventProperty.getPropertyValue("TYPE").equals(recordEventType)) {
                    mergeRow.merge = false;
                    mergeRow.compareResult = CompareResult.EQUAL;
                } else {
                    mergeRow.merge = true;
                    mergeRow.compareResult = CompareResult.COMPATIBLE;
                }
            } else {
                mergeRow.merge = !recordEventType.isEmpty();
                mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.NOT_APPLICABLE;
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
                if ((rowType == RowType.IndiParentFamily && record.getIndi().getFatherLastName().isEmpty() && record.getIndi().getMotherLastName().isEmpty())
                        || (rowType == RowType.WifeParentFamily && record.getWife().getFatherLastName().isEmpty() && record.getWife().getMotherLastName().isEmpty())) {
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
        if (mergeRow.compareResult == CompareResult.EQUAL) {
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
    void addRowSeparator() {
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

    ///////////////////////////////////////////////////////////////////////////
    // accesseurs
    ///////////////////////////////////////////////////////////////////////////
    public MergeParticipantType getParticipantType() {
        return participantType;
    }

    /**
     * retoune le gedcom associé au modele
     * @return gedcom
     */
    protected Gedcom getGedcom() {
        return gedcom;
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
     * retourne le nombre de champs egaux entre le releve et l'entité
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
     * pour savoir quel est le modele qui contient l'entité la plus proche du relevé.
     * @param object
     * @return
     */
    @Override
    public int compareTo(MergeModel object) {
        if (!(object instanceof MergeModel)) {
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
    protected abstract Property copyRecordToEntity() throws Exception;

    protected abstract String getTitle();

    public abstract String getSummary(Entity selectedEntity);

    protected abstract Entity getSelectedEntity();

    /**
     * cree une association entre associatedProperty et l'entité séelctionné dans 
     * le modele
     * @param associatedProperty1
     * @throws Exception
     */
    protected void copyAssociation(Property associatedProperty1, Property associatedProperty2) throws Exception {
        PropertyXRef asso = (PropertyXRef) associatedProperty2.addProperty("ASSO", '@' + associatedProperty1.getEntity().getId() + '@');
        TagPath anchor = associatedProperty1.getPath(true);

        asso.addProperty("RELA", anchor == null ? "Présent" : "Présent" + '@' + anchor.toString());

        // je cree le lien à l'autre extermite de l'association
        try {
            asso.link();
        } catch (GedcomException e) {
            associatedProperty1.delProperty(asso);
            throw e;
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // focntions de gestion de la JTable
    ///////////////////////////////////////////////////////////////////////////
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
     * coche ou décoche une ligne en fonction de son numero d'ordre
     * @param rowNum
     * @param state
     */
    void check(int rowNum, boolean state) {
        check(dataList.get(rowNum).rowType, state);
    }

    /**
     * coche ou décoche une ligne en fonction de son type
     * @param rowType 
     * @param state
     */
    void check(RowType rowType, boolean state) {
        dataMap.get(rowType).merge = state;
        fireTableCellUpdated(dataList.indexOf(dataMap.get(rowType)), 2);
        fireTableCellUpdated(dataList.indexOf(dataMap.get(rowType)), 3);

        // je mets a jour les lignes filles
        for (int i = 0; i < dataList.size(); i++) {
            MergeRow mergeRow = dataList.get(i);
            MergeRow parentRow = getParentRow(mergeRow.rowType);
            if (parentRow != null && parentRow.rowType == rowType) {
                if (state == true) {
                    // je restaure l'etat initial de la ligne fille
                    mergeRow.merge = mergeRow.merge_initial;
                } else {
                    mergeRow.merge = false;
                }
                fireTableCellUpdated(i, 2);
                fireTableCellUpdated(i, 3);
            }
        }

        // je mets a jour la ligne parent
        // seulement si elle était décochée et que la fille vient d'etre cochee
        MergeRow parentRow = getParentRow(rowType);
        if (state == true && parentRow != null) {
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
        if (mergeRow == null) {
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
                Source eventSource = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
                if (source.compareTo(eventSource) == 0) {
                    found = true;
                    // je memorise le lien vers la source pour ajouter la page  
                    sourcexref = (PropertyXRef) sourceProperties[i];
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
        if (sourcexref != null && !record.getEventPage().isEmpty()) {
            String value = record.getEventPage();
            sourcexref.addProperty("PAGE", value);
        }

    }

    /**
     * ajoute la date et le lieu de naissance et une note pour indiquer la source
     * de la naissance dans la propriete BIRT d'un individu
     *
     * @param indi      individu
     * @param birthDate date de naissance
     * @param place     lieu de naissance
     * @param record    releve servant a renseigner la note
     */
    protected void copyBirthDate(Indi indi, MergeRow mergeRow, String place, MergeRecord record) {
        Property birthProperty = indi.getProperty("BIRT");
        if (birthProperty == null) {
            birthProperty = indi.addProperty("BIRT", "");
        }
        // j'ajoute (ou remplace ) la date de la naissance
        PropertyDate propertyDate = indi.getBirthDate(true);
        PropertyDate birthDate = (PropertyDate) mergeRow.recordValue;
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
        copyReferenceNote(birthProperty, record);
    }

    /**
     * ajoute la date et le lieu de naissance et une note pour indiquer la source
     * de la naissance  dans la propriete DEATH d'un individu
     *
     * @param indi      individu
     * @param deathDate date de naissance
     * @param place     lieu de naissance
     * @param record    releve servant a renseigner la note
     */
    protected void copyDeathDate(Indi indi, MergeRow mergeRow, String place, MergeRecord record) {
        Property deathProperty = indi.getProperty("DEAT");
        if (deathProperty == null) {
            deathProperty = indi.addProperty("DEAT", "");
        }
        // j'ajoute (ou remplace ) la date de la deces
        PropertyDate propertyDate = indi.getDeathDate(true);
        PropertyDate deathDate = (PropertyDate) mergeRow.recordValue;
        propertyDate.setValue(deathDate.getValue());

        // j'ajoute le lieu
        if (!place.isEmpty()) {
            PropertyPlace propertyPlace = (PropertyPlace) deathProperty.getProperty("PLAC");
            if (propertyPlace == null) {
                propertyPlace = (PropertyPlace) deathProperty.addProperty("PLAC", "");
            }
            propertyPlace.setValue(place);
        }

        // j'ajoute une note indiquant l'origine de la date du deces
        copyReferenceNote(deathProperty, record);
    }

    /**
     * ajoute la date de marriage et une note pour indiquer la source dans
     * la propriete MARR d'une famille
     * 
     * @param family            famille de mariés
     * @param marriageDate      date de marriage
     * @param occupationDate    date du releve
     * @param record            releve servant a renseigner la note 
     */
    protected void copyMarriageDate(Fam family, MergeRow mergeRow, MergeRecord record) {
        // j'ajoute (ou remplace) la date du mariage des parents
        // je crée la propriété MARR
        Property marriageProperty = family.getProperty("MARR");
        if (marriageProperty == null) {
            marriageProperty = family.addProperty("MARR", "");
        }
        // j'ajoute (ou remplace ) la date de la naissance
        PropertyDate propertyDate = (PropertyDate) marriageProperty.getProperty("DATE", false);
        if (propertyDate == null) {
            propertyDate = (PropertyDate) marriageProperty.addProperty("DATE", "");
        }
        PropertyDate marriageDate = (PropertyDate) mergeRow.recordValue;
        propertyDate.setValue(marriageDate.getValue());

        // j'ajoute une note indiquant l'origine de la date de naissance
        copyReferenceNote(marriageProperty, record);
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
    protected void copyOccupation(Indi indi, String occupation, String residence, boolean createResidence, MergeRecord record) throws GedcomException {
        PropertyDate occupationDate = record.getEventDate();
        // je cherche si l'individu a deja un tag OCCU a la meme date
        Property occupationProperty = null;
        // j'ajoute la profession ou la residence
        if (!occupation.isEmpty()) {
            occupationProperty = indi.addProperty("OCCU", "", getPropertyBestPosition(indi, occupationDate));
            occupationProperty.setValue(occupation);
        } else if (createResidence && !residence.isEmpty()) {
            occupationProperty = indi.addProperty("RESI", "", getPropertyBestPosition(indi, occupationDate));
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
            copyReferenceNote(occupationProperty, record);
        }
    }

    /**
     * ajoute une NOTE a un evenement 
     * La note contient contenant les informations de reference du relevé
     * a une propriete.
     * @param eventProperty
     * @param record
     */
    protected void copyReferenceNote(Property eventProperty, MergeRecord mergeRecord) {
        String noteText;
        PropertyDate propertyDate = (PropertyDate) eventProperty.getProperty("DATE");

        String targetTagName = eventProperty.getTag();
        if (targetTagName.equals("BIRT")) {
            noteText = "Date de naissance {0} déduite de";
        } else if (targetTagName.equals("DEAT")) {
            noteText = "Date de décès {0} déduite de";
        } else if (targetTagName.equals("MARR")) {
            noteText = "Date de mariage {0} déduite de";
        } else if (targetTagName.equals("OCCU")) {
            noteText = "Profession indiquée dans";
        } else if (targetTagName.equals("RESI")) {
            noteText = "Domicile indiqué dans";
        } else {
            noteText = "Information indiquée dans";
        }

        switch (record.getType()) {
            case Birth:
                noteText = MessageFormat.format(noteText + " " + "l''acte de naissance de {1} {2} le {3} ({4})",
                        propertyDate.getDisplayValue(),
                        record.getIndi().getFirstName(),
                        record.getIndi().getLastName(),
                        record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                        record.getEventPlaceCityName());
                break;
            case Marriage:
                noteText = MessageFormat.format(noteText + " " + "l''acte de mariage de {1} {2} et {3} {4} le {5} ({6})",
                        propertyDate.getDisplayValue(),
                        record.getIndi().getFirstName(),
                        record.getIndi().getLastName(),
                        record.getWife().getFirstName(),
                        record.getWife().getLastName(),
                        record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                        record.getEventPlaceCityName());
                break;
            case Death:
                noteText = MessageFormat.format(noteText + " " + "l''acte de décès de {1} {2} le {3} ({4})",
                        propertyDate.getDisplayValue(),
                        record.getIndi().getFirstName(),
                        record.getIndi().getLastName(),
                        record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                        record.getEventPlaceCityName());
                break;
            default:
                switch (record.getEventTypeTag()) {
                    case WILL:
                        noteText = MessageFormat.format(noteText + " " + "l''acte de {1} de {2} {3} le {4} ({5})",
                                propertyDate.getDisplayValue(),
                                record.getEventType(),
                                record.getIndi().getFirstName(),
                                record.getIndi().getLastName(),
                                record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                record.getEventPlaceCityName() + (record.getNotary().isEmpty() ? "" : ", " + record.getNotary()));
                        break;
                    case MARC:
                        noteText = MessageFormat.format(noteText + " " + "l''acte de contrat de mariage entre {2} {3} et {4} {5} le {6} ({7})",
                                propertyDate.getDisplayValue(),
                                record.getEventType(),
                                record.getIndi().getFirstName(),
                                record.getIndi().getLastName(),
                                record.getWife().getFirstName(),
                                record.getWife().getLastName(),
                                record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                record.getEventPlaceCityName() + (record.getNotary().isEmpty() ? "" : ", " + record.getNotary()));
                        break;
                    default:
                        noteText = MessageFormat.format(noteText + " " + "l''acte ''{1}'' entre {2} {3} et {4} {5} le {6} ({7})",
                                propertyDate.getDisplayValue(),
                                record.getEventType(),
                                record.getIndi().getFirstName(),
                                record.getIndi().getLastName(),
                                record.getWife().getFirstName(),
                                record.getWife().getLastName(),
                                record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                record.getEventPlaceCityName() + (record.getNotary().isEmpty() ? "" : ", " + record.getNotary()));
                        break;
                }
                break;
        }

        // je recherche une NOTE deja presente dans la propriete qui
        // contiendrait deja le texte à ajouter
        Property[] notes = eventProperty.getProperties("NOTE");
        boolean found = false;
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].getValue().contains(noteText)) {
                found = true;
                break;
            }
        }

        // J'ajoute le commentaire si aucune note existe deja avec le texte
        if (!found) {
            Property propertyNote = eventProperty.getProperty("NOTE");
            if (propertyNote == null) {
                // je cree une note .
                propertyNote = eventProperty.addProperty("NOTE", "");
            }

            // j'ajoute le commentaire a la fin de la note existante.
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
     * retourne la position du propriété
     *  - apres BIRT
     *  - avant DEATH
     *
     * @param property
     * @param propertyDate
     */
    static protected int getPropertyBestPosition(Property property, PropertyDate propertyDate) {
        int birthPosition = -1;
        int position = 0;
        int resultPosition = 0;
        for (Property child : property.getProperties()) {
            if (child.getTag().equals("BIRT")) {
                birthPosition = position;
                resultPosition = birthPosition + 1;
            } else if (child.getTag().equals("DEAT")) {
                // rien à faire
            } else {
                Property[] childDates = child.getProperties("DATE");
                if (childDates.length > 0) {
                    try {
                        if (!MergeQuery.isRecordBeforeThanDate(propertyDate, (PropertyDate) childDates[0], 0, 0)) {
                            resultPosition = position + 1;
                        }
                    } catch (GedcomException ex) {
                        // rien a faire
                    }
                }
            }

            position++;
        }

        return resultPosition;
    }

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
        if (parent == null) {
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
        if (getParentRow(rowType) != null) {
            label = "  ";
        }
        label += NbBundle.getMessage(MergeModel.class, "MergeModel." + rowType.toString());
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
                parent = getRow(RowType.IndiParentFamily);
                break;

            case IndiMarriedFirstName:
            case IndiMarriedLastName:
            case IndiMarriedBirthDate:
            case IndiMarriedDeathDate:
            case IndiMarriedOccupation:
            case IndiMarriedMarriageDate:
                parent = getRow(RowType.IndiMarriedFamily);
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
                parent = getRow(RowType.WifeParentFamily);
                break;

            case WifeMarriedFirstName:
            case WifeMarriedLastName:
            case WifeMarriedBirthDate:
            case WifeMarriedDeathDate:
            case WifeMarriedOccupation:
            case WifeMarriedMarriageDate:
                parent = getRow(RowType.WifeMarriedFamily);
                break;

            case EventPage:
                parent = getRow(RowType.EventSource);
                break;

            default:
                parent = null;

        }
        return parent;
    }

    /**
     * liste des types de ligne
     */
    static protected enum RowType {

        Separator,
        EventType,
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
