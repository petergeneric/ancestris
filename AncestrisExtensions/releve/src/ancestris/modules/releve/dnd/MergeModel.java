package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.Source;
import genj.gedcom.time.PointInTime;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

public abstract class MergeModel extends AbstractTableModel implements java.lang.Comparable {

    static int minParentYearOld = 15;
    static int indiMaxYearOld = 100;
    static int aboutYear = 5;

    private int nbMatch = 0;
    private int nbMatchMax = 0;

    protected class MergeRow {
        RowType rowType;
        String label;
        Object entityValue;
        Object recordValue;
        Object[] entityChoice;
        Object[] recordChoice;
        boolean merge;
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
    HashMap<RowType, MergeRow> dataMap = new HashMap<RowType, MergeRow>();
    List<MergeRow> dataList = new ArrayList<MergeRow>();

    /**
     * model factory
     *
     * @param entity
     * @param record
     * @return
     */
    static protected List<MergeModel> createMergeModel(Record record, Gedcom gedcom, Entity entity) throws Exception {
        List<MergeModel> models = new ArrayList<MergeModel>();

        if( record instanceof RecordBirth) {
            RecordBirth recordBirth = (RecordBirth)record;
            if ( entity instanceof Fam ) {
                // l'entité selectionnée est une famille
                Fam family = (Fam) entity;
                // j'ajoute un nouvel individu
                models.add(new MergeModelBirth(recordBirth, gedcom, null, family));
                // je recherche les enfants de la famille compatibles avec le releve
                List<Indi> sameChildren = MergeModel.findSameChild(record, gedcom, family);
                // j'ajoute les enfants compatibles
                for(Indi samedIndi : sameChildren) {
                    models.add(new MergeModelBirth(recordBirth, gedcom, samedIndi, samedIndi.getFamilyWhereBiologicalChild()));
                }
            } else if ( entity instanceof Indi ) {
                // l'entité selectionnée est un individu
                Indi selectedIndi = (Indi) entity;

                List<Fam> families = findParentFamily(recordBirth, gedcom, null);

                // j'ajoute l'individu selectionné par dnd 
                if (selectedIndi.getFamilyWhereBiologicalChild() != null ) {
                    models.add(new MergeModelBirth(recordBirth, gedcom, selectedIndi, selectedIndi.getFamilyWhereBiologicalChild()));
                } else {
                    models.add(new MergeModelBirth(recordBirth, gedcom, selectedIndi ,(Fam) null));
                    // j'ajoute l'individu selectionné par dnd avec les familles compatibles
                    for(Fam family : families) {
                        models.add(new MergeModelBirth(recordBirth, gedcom, selectedIndi, family));
                    }
                }
                
                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                // en excluant l'individu selectionne s'il a deja une famille
                List<Indi> sameIndis ;
                if ( selectedIndi.getFamilyWhereBiologicalChild() != null ) {
                    sameIndis = MergeModel.findIndiWithCompatibleBirth(record, gedcom, selectedIndi);
                } else {
                    sameIndis = MergeModel.findIndiWithCompatibleBirth(record, gedcom, null);
                }
                // j'ajoute les individus compatibles
                for(Indi samedIndi : sameIndis) {
                    // j'ajoute les familles compatibles
                    Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild() ;
                    if ( sameIndiFamily != null) {
                        models.add(new MergeModelBirth(recordBirth, gedcom, samedIndi, sameIndiFamily));
                    } else {
                        for(Fam family : families) {
                            models.add(new MergeModelBirth(recordBirth, gedcom, samedIndi, family));
                        }
                    }
                }
                

            } else {
                // j'ajoute un nouvel individu , sans famille
                models.add(new MergeModelBirth((RecordBirth) record, gedcom));
                // je recupere les individus compatibles avec le relevé (qui portent le meme nom que le nom qui est dans le
                // releve et avec les dates de naissance compatibles et les parents compatibles)
                List<Indi> sameIndis = MergeModel.findIndiWithCompatibleBirth(record, gedcom, null);
                List<Fam> families = findParentFamily(recordBirth, gedcom, null);

                // j'ajoute les individus compatibles
                for(Indi samedIndi : sameIndis) {
                    Fam sameIndiFamily = samedIndi.getFamilyWhereBiologicalChild() ;
                    if ( sameIndiFamily != null) {
                        // j'ajoute l'individus compatible avec sa famille
                        models.add(new MergeModelBirth(recordBirth, gedcom, samedIndi, sameIndiFamily));
                    } else {
                        // j'ajoute l'individus compatible sans famille
                        models.add(new MergeModelBirth(recordBirth, gedcom, samedIndi, (Fam) null));
                        // j'ajoute l'individus compatible avec les familles compatibles
                        for(Fam family : families) {
                            models.add(new MergeModelBirth(recordBirth, gedcom, samedIndi, family));
                        }
                    }
                }

                // j'ajoute un nouvel individu avec les familles compatibles
                for(Fam family : families) {
                    models.add(new MergeModelBirth(recordBirth, gedcom, null, family));
                }

                // j'ajoute un nouvel individu avec les couples qui ne sont pas des familles
                // mais qui pourraient être ses parents
                List<Indi> fathers = new ArrayList<Indi>();
                List<Indi> mothers = new ArrayList<Indi>();
                findCompatibleFatherMother(recordBirth, gedcom, families, fathers, mothers);
                for(Indi father : fathers) {
                    for(Indi mother : mothers) {
                        models.add(new MergeModelBirth(recordBirth, gedcom, father, mother));
                    }
                }
            }
           
        } else {
             models = new ArrayList<MergeModel>();
        }

        // je trie les modeles par ordre décroissant du nombre de champs egaux entre le relevé et l'entité
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
     * ajoute une ligne dans le modele
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
        mergeRow.recordChoice = null;
        mergeRow.entityValue = entityValue;
        mergeRow.entityChoice = null;
        mergeRow.entityObject = entity;
        if ( recordValue.isEmpty() ) {
            mergeRow.merge = false;
            mergeRow.compareResult = !recordValue.equals(entityValue) ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
        } else {
            mergeRow.merge = !recordValue.equals(entityValue);
            mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
        }

        // j'incremente le compteur de champs egaux
        if ( mergeRow.compareResult == CompareResult.EQUAL && !recordValue.isEmpty())  {
            nbMatch++;
        }
        nbMatchMax++;

    }

    /**
     * ajoute une ligne dans le modele
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
        mergeRow.entityChoice = null;
        mergeRow.recordChoice = null;

        // je compare les valeurs par defaut du releve et de l'entite
        if ( recordValue == null ) {
             mergeRow.merge = false;
             mergeRow.compareResult = CompareResult.EQUAL;
        } else if ( entityValue == null ) {
             mergeRow.merge = true;
             mergeRow.compareResult = CompareResult.COMPATIBLE;
        } else {
            switch (rowType) {
                case IndiBirthDate:
                case IndiFatherBirthDate:
                case IndiMotherBirthDate:
                case IndiDeathDate:
                case IndiFatherDeathDate:
                case IndiMotherDeathDate:
                    if ( recordValue.getDisplayValue().equals(entityValue.getDisplayValue()) )  {
                        // les valeurs sont egales, pas besoin de merger
                        mergeRow.merge = false;
                        mergeRow.compareResult = CompareResult.EQUAL;
                    } else {
                        mergeRow.merge = isBestBirthDate(recordValue, entityValue, null);
                        mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    }
                    break;
                default:
                    mergeRow.merge = recordValue.compareTo(entityValue)==0;
                    mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    break;
            }
        }

        // j'incremente le compteur des champs egaux
        if ( mergeRow.compareResult == CompareResult.EQUAL)  {
            nbMatch++;
        }
        nbMatchMax++;

    }

    /**
     * ajoute une ligne dans le modele
     * @param rowType
     * @param label
     * @param recordValues
     * @param entityValues
     */
    void addRow(RowType rowType, Object[] recordValues, Object[] entityValues) {
        Object defaultRecordValue;
        Object defaultEntityValue;
        if (recordValues != null) {
            defaultRecordValue = recordValues.length > 0 ? recordValues[0] : null;
            defaultEntityValue = entityValues.length > 0 ? entityValues[0] : null;
            // je verifie si une source du releve correspond a une source de l'entité
            for (Object recordValue : recordValues) {
                for (Object entityValue : entityValues) {
                    if (recordValue.equals(entityValue)) {
                        defaultEntityValue = entityValue;
                        defaultRecordValue = recordValue;
                    }
                }
            }
        } else {
            defaultRecordValue = null;
            defaultEntityValue = entityValues.length > 0 ? entityValues[0] : null;
        }
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.entityValue = defaultEntityValue;
        mergeRow.recordValue = defaultRecordValue;
        mergeRow.entityChoice = entityValues;
        mergeRow.recordChoice = recordValues;
        if (defaultRecordValue == null) {
            mergeRow.merge = false;
            mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
        } else if (defaultEntityValue == null) {
            mergeRow.merge = true;
            mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
        } else if (defaultRecordValue instanceof Property) {
            mergeRow.merge = ((Property) defaultRecordValue).compareTo((Property) defaultEntityValue) != 0;
            mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
        } else {
            mergeRow.merge = !defaultRecordValue.equals(defaultEntityValue);
            mergeRow.compareResult = mergeRow.merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
        }

        // j'incremente le compteur de champs egaux
        if ( mergeRow.compareResult == CompareResult.EQUAL)  {
            nbMatch++;
        }
        nbMatchMax++;

    }

    /**
     * ajoute une ligne dans le modele
     * @param rowType
     * @param label
     * @param recordValues
     * @param entityValues
     */
    void addRow(RowType rowType, Fam family) {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.rowType = rowType;
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.recordValue = "";
        mergeRow.entityValue = family;
        mergeRow.recordChoice = null;
        mergeRow.entityChoice = null;
        mergeRow.merge = true;
        mergeRow.compareResult = CompareResult.COMPATIBLE;
        mergeRow.entityObject = family;
    }

    /**
     * ajoute une ligne dans le modele
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
        mergeRow.entityChoice = null;
        mergeRow.recordChoice = null;
        mergeRow.merge = false;
        mergeRow.compareResult = CompareResult.NOT_APPLICABLE;
    }

    protected MergeRow getRow(RowType rowType) {
        return dataMap.get(rowType);
    }

    protected int getNbMatch() {
        return nbMatch;
    }
    
    protected int getNbMatchMax() {
        return nbMatchMax;
    }

    /**
     * compare le nombre de champs qui correspondent
     * @param object
     * @return
     */
    @Override
    public int compareTo(Object object) {
        if ( !(object instanceof MergeModel)) {
            return 1;
        }
        int nombre1 = ((MergeModel) object).nbMatch;
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
    protected abstract void copyRecordToEntity();
    protected abstract String getTitle();
    protected abstract Entity getSelectedEntity();

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
            case 1:
                return dataList.get(row).recordChoice != null;
            case 2:
                return true;
            case 3:
                return dataList.get(row).entityChoice != null;
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

    void check(int rowNum, boolean state) {
        dataList.get(rowNum).merge = state;
    }

    void check(RowType rowType, boolean state) {
        dataMap.get(rowType).merge = state;
    }

    boolean isChecked(RowType rowType) {
        return dataMap.get(rowType).merge;
    }

    protected Object[] getChoice(int row, int col) {
        switch (col) {
            case 0:
                return null;
            case 1:
                return dataList.get(row).recordChoice;
            case 2:
                return null;
            case 3:
                return dataList.get(row).entityChoice;
            default:
                return null;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // utilitaires
    ////////////////////////////////////////////////////////////////////////////



   /**
    * retourne les sources dont le titre correspond à la commune ou au notaire
    *    codecommune nom_commune BMS
    *    codecommune nom_commune Etat civil
    *    codecommune nom_commune Notaire Notaire_nom Notaire_prenom
    * @param gedcom
    * @param record
    * @return
    */
   static protected Source[] findSources(Record record, Gedcom gedcom)  {
        List<Source> matchedSources = new ArrayList<Source>();
        Collection<? extends Entity> sources = gedcom.getEntities("SOUR");

        String cityName = record.getEventPlace().getCityName();
        String cityCode = record.getEventPlace().getCityCode();
        String countyName = record.getEventPlace().getCountyName();
        String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyName, cityCode, cityName);
        Pattern pattern = Pattern.compile(stringPatter);

        for (Entity source : sources) {
            if (pattern.matcher(((Source)source).getTitle()).matches()) {
                matchedSources.add((Source)source);
            }
        }

        if ( matchedSources.isEmpty() ) {
            Source source = new Source("SOUR","");
            source.addSimpleProperty("TITL", String.format("%s %s Etat civil", cityCode, cityName),1);
            matchedSources.add(source);
        }
        return matchedSources.toArray(new Source[matchedSources.size()]);
    }

    /**
     * retourne la liste des individus qui ont le même nom et une date de naissance
     * compatible avec celle du relevé
     * 
     * @param record
     * @param gedcom
     * @return liste des individus
     */
//    static protected List<Entity> findIndiWithCompatibleBirth(Record record, Gedcom gedcom) {
//        return findIndiWithCompatibleBirth(record, gedcom, (Indi) null);
//    }

    /**
     * retourne la liste des individus qui ont le même nom et date de naissance
     * compatible compatible avec celle du relevé
     * mais qui ont un identifiant different de l'individu excludeIndi
     * @param record
     * @param gedcom
     * @param excludeIndi
     * @return liste des individus
     */
    static protected List<Indi> findIndiWithCompatibleBirth(Record record,  Gedcom gedcom, Indi excludeIndi) {
        List<Indi> sameIndis = new ArrayList<Indi>();

        for (Indi indi : gedcom.getIndis()) {

            // individu a exlure
            if (excludeIndi != null && excludeIndi.compareTo(indi) == 0) {
                continue;
            }

            // meme sexe de l'individu
            if (record.getIndiSex().getSex() != FieldSex.UNKNOWN
                    && indi.getSex() != PropertySex.UNKNOWN
                    && record.getIndiSex().getSex() != indi.getSex()) {
                continue;
            }

            // meme nom de l'individu
            if (!record.getIndiLastName().isEmpty()
                    && !isSameName(record.getIndiLastName().toString(), indi.getLastName())) {
                continue;
            }

            // meme prenom de l'individu
            if (!record.getIndiFirstName().isEmpty()
                    && !isSameName(record.getIndiFirstName().toString(), indi.getFirstName())) {
                continue;
            }

            // je recupere la date de naissance
            PropertyDate recordBirthDate;
            if (record.getIndiBirthDate().getPropertyDate() != null
                    && record.getIndiBirthDate().getPropertyDate().isComparable()) {
                recordBirthDate = record.getIndiBirthDate().getPropertyDate();
            } else {
                // si la date de naisance n'est pas renseignée , j'utilise la date
                // du relevé.
                if (record.getEventDateField() != null
                        && record.getEventDateField().isComparable()) {
                    recordBirthDate = record.getEventDateField();
                } else {
                    // j'abandonne s'il n'y a pas de date
                    continue;
                }
            }

            // petit raccourci pour gagner du temps
            PropertyDate indiBirtDate = indi.getBirthDate();

            if (indiBirtDate != null) {
                if (!isCompatible(recordBirthDate, indiBirtDate)) {
                    // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                    continue;
                }
            }


            Fam parentFamily = indi.getFamilyWhereBiologicalChild();
            if (parentFamily != null) {
                PropertyDate marriageDate = parentFamily.getMarriageDate();
                if (marriageDate != null) {
                    // le releve de naissance doit être après la date mariage.
                    if (!isRecordAfterThanDate(recordBirthDate, marriageDate, 0, 0)) {
                        continue;
                    }
                }

                Indi father = parentFamily.getHusband();
                if (father != null) {

                    // meme nom du pere
                    if (!record.getIndiFatherLastName().isEmpty()
                            && !isSameName(record.getIndiFatherLastName().toString(), father.getLastName())) {
                        continue;
                    }
                    //meme prénom du pere
                    if (!record.getIndiFatherFirstName().isEmpty()
                            && !isSameName(record.getIndiFatherFirstName().toString(), father.getFirstName())) {
                        continue;
                    }

                    // le pere doit avoir au moins minParentYearOld
                    if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance
                    if (!isRecordBeforeThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi mother = parentFamily.getWife();
                if (mother != null) {
                    // meme nom de la mere
                    if (!record.getIndiMotherLastName().isEmpty()
                            && !isSameName(record.getIndiMotherLastName().toString(), mother.getLastName())) {
                        continue;
                    }
                    //meme prénom de la mere
                    if (!record.getIndiMotherFirstName().isEmpty()
                            && !isSameName(record.getIndiMotherFirstName().toString(), mother.getFirstName())) {
                        continue;
                    }
                    // la mere doit avoir au moins minParentYearOld
                    if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                    // la mere ne doit pas etre decedee avant la date de naissance
                    if (!isRecordBeforeThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
                        continue;
                    }
                }
            }
            // j'ajoute l'individu dans la liste
            sameIndis.add(indi);

        }
        return sameIndis;
    }


    /**
     * retourne la liste des enfants qui ont le même nom et une date de naissance
     * compatible  avec celle du relevé
     * Retourne une exception si le nom des parents de la famille est different de
     * du nom des parents du releve, ou si leur age ou la date de mariage n'est pas
     * compatible avec la date de naissance.
     * @param birthRecord
     * @param gedcom
     * @param excludeIndi
     * @return liste des enfants
     */
    static protected List<Indi> findSameChild(Record birthRecord, Gedcom gedcom, Fam selectedFamily) throws Exception {
        List<Indi> sameChildren = new ArrayList<Indi>();

        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = null;
        if (birthRecord.getIndiBirthDate().getPropertyDate() != null
                && birthRecord.getIndiBirthDate().getPropertyDate().isComparable()) {
            recordBirthDate = birthRecord.getIndiBirthDate().getPropertyDate();
        } else {
            // si la date de naisance n'est pas renseignée , j'utilise la date
            // du relevé.
            if (birthRecord.getEventDateField() != null
                    && birthRecord.getEventDateField().isComparable()) {
                recordBirthDate = birthRecord.getEventDateField();
            }
        }

        if (selectedFamily != null) {
            PropertyDate marriageDate = selectedFamily.getMarriageDate();
            if (marriageDate != null) {
                // le releve de naissance doit être après la date mariage.
                if (!isRecordAfterThanDate(recordBirthDate, marriageDate, 0, 0)) {
                    throw new Exception("la date de naissance du releve doit être après la date mariage");
                }
            }

            Indi father = selectedFamily.getHusband();
            if (father != null) {

                // meme nom du pere
                if (!birthRecord.getIndiFatherLastName().isEmpty()
                        && !isSameName(birthRecord.getIndiFatherLastName().toString(), father.getLastName())) {
                    throw new Exception("le nom du pere est different");
                }
                //meme prénom du pere
                if (!birthRecord.getIndiFatherFirstName().isEmpty()
                        && !isSameName(birthRecord.getIndiFatherFirstName().toString(), father.getFirstName())) {
                    throw new Exception("le prenom du pere est different");
                }

                // le pere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                    throw new Exception("le pere n'a pas l'age requis");
                }
                // le pere ne doit pas etre decede 9 mois avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
                    throw new Exception("le pere est decede avant la naissance");
                }
            }

            Indi mother = selectedFamily.getWife();
            if (mother != null) {
                // meme nom de la mere
                if (!birthRecord.getIndiMotherLastName().isEmpty()
                        && !isSameName(birthRecord.getIndiMotherLastName().toString(), mother.getLastName())) {
                    throw new Exception("le nom de la mere est different");
                }
                //meme prénom de la mere
                if (!birthRecord.getIndiMotherFirstName().isEmpty()
                        && !isSameName(birthRecord.getIndiMotherFirstName().toString(), mother.getFirstName())) {
                    throw new Exception("le prénom de la mere est different");
                }
                // la mere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                    throw new Exception("la mere n'a pas l'age requis");
                }
                // la mere ne doit pas etre decedee avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
                    throw new Exception("la mere est décédée avant la naissance");
                }
            }

            // je recherche les enfants conpatibles avec le relevé
            for (Indi child : selectedFamily.getChildren()) {

                // meme sexe de l'enfant
                if (birthRecord.getIndiSex().getSex() != FieldSex.UNKNOWN
                        && child.getSex() != PropertySex.UNKNOWN
                        && birthRecord.getIndiSex().getSex() != child.getSex()) {
                    continue;
                }

                // meme nom de l'enfant
                if (!birthRecord.getIndiLastName().isEmpty()
                        && !isSameName(birthRecord.getIndiLastName().toString(), child.getLastName())) {
                    continue;
                }

                // meme prenom de l'enfant
                if (!birthRecord.getIndiFirstName().isEmpty()
                        && !isSameName(birthRecord.getIndiFirstName().toString(), child.getFirstName())) {
                    continue;
                }

                // petit raccourci pour gagner du temps
                PropertyDate indiBirtDate = child.getBirthDate();

                // date de naissance compatible
                if (indiBirtDate != null) {
                    if (!isCompatible(recordBirthDate, indiBirtDate)) {
                        // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                        continue;
                    }
                }

                // j'ajoute l'enfant dans la liste
                sameChildren.add(child);
            }
        }


        return sameChildren;
    }


    /**
     * retourne "true" si la date du releve est egale ou est compatible avec la
     * date de naissance.
     *
     * @param recordDate
     * @param birthDate
     * @return
     */
    static private boolean isCompatible(PropertyDate recordDate, PropertyDate birthDate) {
       boolean result;

        try {
            if (recordDate == null || birthDate == null ) {
                return false;
            }
            if (birthDate.getFormat() == PropertyDate.DATE) {
                result = recordDate.getStart().compareTo(birthDate.getStart()) == 0;
            } else if (birthDate.getFormat() == PropertyDate.BETWEEN_AND || birthDate.getFormat() == PropertyDate.FROM_TO) {
                result = containsRefTime(recordDate.getStart(), birthDate.getStart(), birthDate.getEnd());
            } else if (birthDate.getFormat() == PropertyDate.FROM || birthDate.getFormat() == PropertyDate.AFTER) {
                PointInTime endPit = new PointInTime();
                endPit.set( birthDate.getStart());
                endPit.add(0, 0, indiMaxYearOld);
                result = containsRefTime(recordDate.getStart(), birthDate.getStart(), endPit);
            } else if (birthDate.getFormat() == PropertyDate.TO || birthDate.getFormat() == PropertyDate.BEFORE) {
                PointInTime startPit = new PointInTime();
                startPit.set( birthDate.getStart());
                startPit.add(0, 0, -indiMaxYearOld);
                result = containsRefTime(recordDate.getStart(), startPit, birthDate.getStart());
            } else if (birthDate.getFormat() == PropertyDate.ABOUT || birthDate.getFormat() == PropertyDate.ESTIMATED) {
                PointInTime startPit = new PointInTime();
                startPit.set( birthDate.getStart());
                startPit.add(0, 0, -aboutYear);
                PointInTime endPit = new PointInTime();
                endPit.set( birthDate.getStart());
                endPit.add(0, 0, +aboutYear);
                result = containsRefTime(recordDate.getStart(), startPit, endPit);
            } else {
                result = recordDate.getStart().compareTo(birthDate.getStart()) == 0;
            }
        } catch (GedcomException ex) {
            result = false;
        }
       return result;
    }

    /**
     * retourne true si l'intervalle [start end ] contient refTime
     *
     *                       refTime
     *    --- [****]----- [*****X*****]------[*****] -----
     *        false          true             false
     *
     * @param refTime
     * @param start
     * @param end
     * @return
     */
    static private boolean containsRefTime(final PointInTime refTime, final PointInTime start , final PointInTime end) throws GedcomException {
        int jdRef = refTime.getJulianDay();
        int jdStart = start.getJulianDay();
        int jdEnd   = end.getJulianDay();
        if ( jdStart <= jdRef && jdRef <= jdEnd) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * recherche les familles des parents compatibles avec le releve
     * ou (inlusif) une famille dans tout le gedcom
     * @param record
     * @param record
     * @return
     */
    static protected List<Fam> findParentFamily(RecordBirth record, Gedcom gedcom, Indi selectedIndi) {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        if ( selectedIndi != null) {
            Fam fam = selectedIndi.getFamilyWhereBiologicalChild();
            if (fam!= null ) {
                // l'individu a deja des parents
                parentFamilies.add(fam);
            }
        }

        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getIndiBirthDate().getPropertyDate();
        if (!recordBirthDate.isComparable()) {
            recordBirthDate = record.getEventDateField();
        }

        // 1) je recherche une famille avec un pere et une mere qui portent le même nom
        //     et dont les dates de naissance, de deces et de mariage sont compatibles
        for (Fam fam : gedcom.getFamilies()) {
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();

            if ( husband == null || wife == null) {
                continue;
            }

            // meme nom du pere
            if (!record.getIndiFatherLastName().isEmpty()
                && !isSameName(record.getIndiFatherLastName().toString(), husband.getLastName()) ){
                continue;
            }
            //meme prénom du pere
            if (!record.getIndiFatherFirstName().isEmpty()
                && !isSameName(record.getIndiFatherFirstName().toString(),husband.getFirstName()) ){
                continue;
            }
            // meme nom de la mere
            if (!record.getIndiMotherLastName().isEmpty()
                && !isSameName(record.getIndiMotherLastName().toString(),wife.getLastName()) ){
                continue;
            }
            //meme prénom de la mere
            if (!record.getIndiMotherFirstName().isEmpty()
                && !isSameName(record.getIndiMotherFirstName().toString(),wife.getFirstName()) ){
                continue;
            }

            // le pere doit avoir au moins minParentYearOld
            if (!isRecordAfterThanDate(recordBirthDate, husband.getBirthDate(), 0, minParentYearOld)) {
                continue;
            }
            // la mere doit avoir au moins minParentYearOld
            if (!isRecordAfterThanDate(recordBirthDate, wife.getBirthDate(), 0, minParentYearOld)) {
                continue;
            }

            // le pere ne doit pas etre decede 9 mois avant la date de naissance
            if (!isRecordBeforeThanDate(recordBirthDate, husband.getDeathDate(), 9, 0)) {
                continue;
            }
            // la mere ne doit pas etre decedee avant la date de naissance
            if (!isRecordBeforeThanDate(recordBirthDate, wife.getDeathDate(), 0, 0)) {
                continue;
            }
            
            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà 
            if (!parentFamilies.contains(fam)) {
                parentFamilies.add(fam);
            }
        }
        
        return parentFamilies;
    }


    /**
     * recherche les individuscorrespondant aux parents du releve  mais qui ne
     * sont pas mariés.
     * 
     * @param record
     * @param record
     * @return
     */
    static protected void findCompatibleFatherMother(RecordBirth record, Gedcom gedcom, List<Fam> families, List<Indi> fathers, List<Indi> mothers) {
        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getIndiBirthDate().getPropertyDate();
        if (!recordBirthDate.isComparable()) {
            recordBirthDate = record.getEventDateField();
        }

        Collection entities = gedcom.getIndis();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();

            if (families!= null) {
                boolean found = false;
                for(Fam fam : families) {
                    if( indi.compareTo(fam.getHusband())== 0 || indi.compareTo(fam.getWife())==0 ) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }

            if (indi.getSex() == PropertySex.MALE) {
                // meme nom du pere
                if (!record.getIndiFatherLastName().isEmpty()
                        && !isSameName(record.getIndiFatherLastName().toString(), indi.getLastName())) {
                    continue;
                }

                //meme prénom du pere
                if (!record.getIndiFatherFirstName().isEmpty()
                        && !isSameName(record.getIndiFatherFirstName().toString(), indi.getFirstName())) {
                    continue;
                }

                // le pere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, indi.getBirthDate(), 0, minParentYearOld)) {
                    continue;
                }

                // le pere ne doit pas etre decede 9 mois avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, indi.getDeathDate(), 9, 0)) {
                    continue;
                }

                // TODO le pere ne doit pas etre deja marié avec une autre personne avant la date du releve
                // et avoir avec elle un enfant avant la date du releve et un autre apres la date du releve
                Fam[] fams = indi.getFamiliesWhereSpouse();
                boolean incompatible = false;
                for( Fam fam : fams) {
                    PropertyDate marriageDate = fam.getMarriageDate();
                    Indi [] children = fam.getChildren(true);
                    PropertyDate firtChildBirthDate = null;
                    PropertyDate lastChildBirthDate = null;
                    if ( children.length > 0 ) {
                        firtChildBirthDate = children[0].getBirthDate();
                        lastChildBirthDate = children[children.length-1].getBirthDate();
                    }
                    if ( !isRecordBeforeThanDate(recordBirthDate, marriageDate,0, 0)
                         && !isRecordBeforeThanDate(recordBirthDate, firtChildBirthDate,0, 0)
                         && !isRecordAfterThanDate(recordBirthDate, lastChildBirthDate,0, 0)
                         && !record.getIndiMotherLastName().isEmpty()
                         && !isSameName(record.getIndiMotherLastName().toString(), fam.getWife().getLastName())
                         && !record.getIndiMotherFirstName().isEmpty()
                         && !isSameName(record.getIndiMotherFirstName().toString(), fam.getWife().getFirstName())
                         ) {
                        incompatible = true;
                        break;
                    }
                }
                if (incompatible) {
                    continue;
                }

                fathers.add(indi);

            } else if (indi.getSex() == PropertySex.FEMALE) {

                // meme nom de la mere
                if (!record.getIndiMotherLastName().isEmpty()
                        && !isSameName(record.getIndiMotherLastName().toString(), indi.getLastName())) {
                    continue;
                }
                //meme prénom de la mere
                if (!record.getIndiMotherFirstName().isEmpty()
                        && !isSameName(record.getIndiMotherFirstName().toString(), indi.getFirstName())) {
                    continue;
                }

                // la mere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, indi.getBirthDate(), 0, minParentYearOld)) {
                    continue;
                }

                // la mere ne doit pas etre decedee avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, indi.getDeathDate(), 0, 0)) {
                    continue;
                }

                // TODO la mere ne doit pas etre deja mariée avec une autre personne avant la date du releve
                // et avoir au moins un enfant apres la date du releve
                Fam[] fams = indi.getFamiliesWhereSpouse();
                boolean incompatible = false;
                for( Fam fam : fams) {
                    PropertyDate marriageDate = fam.getMarriageDate();
                    Indi [] children = fam.getChildren(true);
                    PropertyDate firtChildBirthDate = null;
                    PropertyDate lastChildBirthDate = null;
                    if ( children.length > 0 ) {
                        firtChildBirthDate = children[0].getBirthDate();
                        lastChildBirthDate = children[children.length-1].getBirthDate();
                    }
                    if ( !isRecordBeforeThanDate(recordBirthDate, marriageDate,0, 0)
                         && !isRecordBeforeThanDate(recordBirthDate, firtChildBirthDate,0, 0)
                         && !isRecordAfterThanDate(recordBirthDate, lastChildBirthDate,0, 0)
                         && !record.getIndiFatherLastName().isEmpty()
                         && !isSameName(record.getIndiFatherLastName().toString(), fam.getHusband().getLastName())
                         && !record.getIndiFatherFirstName().isEmpty()
                         && !isSameName(record.getIndiFatherFirstName().toString(), fam.getHusband().getFirstName())                         
                         ) {
                        incompatible = true;
                        break;
                    }
                }
                if (incompatible) {
                    continue;
                }
                
                mothers.add(indi);
            }
        }

    }


    static private boolean isSameName(String str1, String str2) {
        DoubleMetaphone dm = new DoubleMetaphone();
        return dm.encode(str1).equals(dm.encode(str2));
        //return str1.equals(str2);
    }
    
    /**
     * retourne true si la date de naissance du parent est inférieure à la date
     * du relevé (diminuée de l'age minimum pour être parent)
     * et si le parent a moins de 100 ans à la date du relevé.
     * Autrement dit :
     *   recordDate - indiMaxYearOld <  parentBirthDate  < recordDate - (minMonthShift + minYearShift)
     *
     *   recordDate-100ans  parentBirthDate    recordDate-minYearShift
     *   ----[}------------------[******]------[******]------------  => true
     *
     *    r-100ans   recordDate-minYearShift   parentBirthDate
     *   ----[}-------- [******]-----------------[******]----------  => false
     *
     *    parentBirthDate   recordDate-100ans  recordDate-minYearShift
     *   ---[******]------------[}---------------- [******]--------  => false
     *
     * @param recordDate  date du relevé
     * @param parentDeathDate  date de naissance du parent
     * @param minDiff
     * @return true si indiFirthDate + minMonthDiff + minYearDiff <= recordBirthDate
     */
     static protected boolean isRecordAfterThanDate(PropertyDate recordDate, PropertyDate parentBirthDate, int minMonthShift, int minYearShift) {
        boolean result;
        if (recordDate == null ) {
            return true;
        } else if (!recordDate.isComparable()) {
            return true;
        }

        if (parentBirthDate == null) {
            return true;
        } else if (!parentBirthDate.isComparable()) {
            return true;
        }

        try {
            int minStart;
            int minEnd;
            int maxStart;
            int maxEnd;
            PointInTime pit = new PointInTime();

            if (recordDate.getFormat() == PropertyDate.DATE) {
                pit.set(recordDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd   = minStart;

                pit.set(recordDate.getStart());
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                maxEnd   = maxStart;
            } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO) {
                pit.set(recordDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                pit.set(recordDate.getEnd());
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                pit.set(recordDate.getStart());
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                pit.set(recordDate.getEnd());
                maxEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.FROM || recordDate.getFormat() == PropertyDate.AFTER) {
                pit.set(recordDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd = Integer.MAX_VALUE;

                pit.set(recordDate.getStart());
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                maxEnd = Integer.MAX_VALUE;
            } else if (recordDate.getFormat() == PropertyDate.TO || recordDate.getFormat() == PropertyDate.BEFORE) {
                minStart = Integer.MIN_VALUE;
                pit.set(recordDate.getStart());
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                maxStart = Integer.MIN_VALUE;
                pit.set(recordDate.getStart());
                maxEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.ABOUT || recordDate.getFormat() == PropertyDate.ESTIMATED) {
                pit.set(recordDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld-aboutYear).getJulianDay();
                pit.set(recordDate.getStart());
                minEnd = pit.add(0, 0, -indiMaxYearOld+aboutYear).getJulianDay();

                pit.set(recordDate.getStart());
                maxStart = pit.add(0, -minMonthShift, -minYearShift-aboutYear).getJulianDay();
                pit.set(recordDate.getStart());
                maxEnd = pit.add(0, -minMonthShift, -minYearShift +aboutYear).getJulianDay();
            } else {
                pit.set(recordDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd   = minStart;

                pit.set(recordDate.getStart());
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                maxEnd   = maxStart;
            }
            
            int birthStart;
            int birthEnd;
            
            if (parentBirthDate.getFormat() == PropertyDate.DATE) {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd   = birthStart;
            } else if (parentBirthDate.getFormat() == PropertyDate.BETWEEN_AND || parentBirthDate.getFormat() == PropertyDate.FROM_TO) {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd   =  parentBirthDate.getEnd().getJulianDay();
            } else if (parentBirthDate.getFormat() == PropertyDate.FROM || parentBirthDate.getFormat() == PropertyDate.AFTER) {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd   =  Integer.MAX_VALUE;
            } else if (parentBirthDate.getFormat() == PropertyDate.TO || parentBirthDate.getFormat() == PropertyDate.BEFORE) {
                birthStart =  Integer.MIN_VALUE;;
                birthEnd   =  parentBirthDate.getStart().getJulianDay();
            } else if (parentBirthDate.getFormat() == PropertyDate.ABOUT || parentBirthDate.getFormat() == PropertyDate.ESTIMATED) {
                 // intervalle [start2 , end2]
                PointInTime startPit = new PointInTime();
                startPit.set(parentBirthDate.getStart());
                birthStart = startPit.add(0, 0, -aboutYear).getJulianDay();
                PointInTime endPit = new PointInTime();
                endPit.set(parentBirthDate.getStart());
                birthEnd = startPit.add(0, 0, +aboutYear).getJulianDay();
            } else {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd   = birthStart;
            }

            if ( birthStart > maxEnd) {
                result = false;
            } else {
                if ( minStart > birthEnd) {
                    result = false;
                } else {
                    result = true;
                }
            }
        } catch (GedcomException ex) {
            result = false;
        }
        return result;
    }


    /**
     * retourne true si la date de deces du parent est superieure à la date
     * du relevé
     * et si le parent est né moins de 100 ans avant la date du relevé.
     * Autrement dit :
     *   deaththDate -100   < recordDate - (minMonthShift + minYearShift) < deaththDate
     *
     *   deathDate-100ans  recordDate-minMonthShift     deathDate
     *   ----[}------------------[******]---------------[******]----  => true
     *
     *    r-100ans   recordDate-minYearShift   parentBirthDate
     *   ----[}-------- [******]-----------------[******]----------  => false
     *
     *    parentBirthDate   recordDate-100ans  recordDate-minYearShift
     *   ---[******]------------[}---------------- [******]--------  => false
     *
     * @param recordDate  date du relevé
     * @param parentBirthDate  date de naissance du parent
     * @param minDiff
     * @return true si indiFirthDate + minMonthDiff + minYearDiff <= recordBirthDate
     */
    static protected boolean isRecordBeforeThanDate(PropertyDate recordDate, PropertyDate parentDeathDate, int minMonthShift, int minYearShift) {
        boolean result;
        if (recordDate == null ) {
            return true;
        } else if (!recordDate.isComparable()) {
            return true;
        }

        if (parentDeathDate == null) {
            return true;
        } else if (!parentDeathDate.isComparable()) {
            return true;
        }

        try {
            int recStart;
            int recEnd;
            
            if (recordDate.getFormat() == PropertyDate.DATE) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                recEnd   = recStart;
            } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                pit.set(recordDate.getEnd());
                recEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.FROM || recordDate.getFormat() == PropertyDate.AFTER) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                recEnd = Integer.MAX_VALUE;
            } else if (recordDate.getFormat() == PropertyDate.TO || recordDate.getFormat() == PropertyDate.BEFORE) {
                PointInTime pit = new PointInTime();
                recStart = Integer.MIN_VALUE;
                pit.set(recordDate.getEnd());
                recEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.ABOUT || recordDate.getFormat() == PropertyDate.ESTIMATED) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recStart = pit.add(0, -minMonthShift, -minYearShift-aboutYear).getJulianDay();
                pit.set(recordDate.getEnd());
                recEnd = pit.add(0, -minMonthShift, -minYearShift +aboutYear).getJulianDay();
            } else {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                recEnd   = recStart;
            }

            int minStart;
            int minEnd;
            int maxStart;
            int maxEnd;
            
            if (parentDeathDate.getFormat() == PropertyDate.DATE) {
                PointInTime pit = new PointInTime();
                pit.set(parentDeathDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd   = minStart;

                maxStart = parentDeathDate.getStart().getJulianDay();
                maxEnd   = maxStart;
            } else if (parentDeathDate.getFormat() == PropertyDate.BETWEEN_AND || parentDeathDate.getFormat() == PropertyDate.FROM_TO) {
                PointInTime pit = new PointInTime();
                pit.set(parentDeathDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                pit.set(parentDeathDate.getEnd());
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                maxStart = parentDeathDate.getStart().getJulianDay();
                maxEnd = parentDeathDate.getEnd().getJulianDay();
            } else if (parentDeathDate.getFormat() == PropertyDate.FROM || parentDeathDate.getFormat() == PropertyDate.AFTER) {
                PointInTime pit = new PointInTime();
                pit.set(parentDeathDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd = Integer.MAX_VALUE;

                maxStart = parentDeathDate.getStart().getJulianDay();
                maxEnd = Integer.MAX_VALUE;
            } else if (parentDeathDate.getFormat() == PropertyDate.TO || parentDeathDate.getFormat() == PropertyDate.BEFORE) {
                PointInTime pit = new PointInTime();
                minStart = Integer.MIN_VALUE;
                pit.set(parentDeathDate.getStart());
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                maxStart = Integer.MIN_VALUE;
                maxEnd = parentDeathDate.getStart().getJulianDay();
            } else if (parentDeathDate.getFormat() == PropertyDate.ABOUT || parentDeathDate.getFormat() == PropertyDate.ESTIMATED) {
                PointInTime pit = new PointInTime();
                pit.set(parentDeathDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld-aboutYear).getJulianDay();
                pit.set(parentDeathDate.getStart());
                minEnd = pit.add(0, 0, -indiMaxYearOld+aboutYear).getJulianDay();

                pit.set(parentDeathDate.getStart());
                maxStart = pit.add(0, 0,-aboutYear).getJulianDay();
                pit.set(parentDeathDate.getStart());
                maxEnd = pit.add(0, 0, +aboutYear).getJulianDay();
            } else {
                PointInTime pit = new PointInTime();
                pit.set(parentDeathDate.getStart());
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd   = minStart;

                maxStart = parentDeathDate.getStart().getJulianDay();
                maxEnd   = maxStart;
            }
            
            if ( recStart > maxEnd) {
                result = false;
            } else {
                if ( minStart > recEnd) {
                    result = false;
                } else {
                    result = true;
                }
            }
        } catch (GedcomException ex) {
            result = false;
        }
        return result;
    }

    /**
     *  retourne vrai si la date de naissance du releve est plus precise
     *  que la date de naissance de l'entité
     *
     *  record      birth
     *  date        date    vrai si record est plus precis que birth
     *  date        BEF     vrai si record est avant birth
     *  date        AFT     vrai si record est apres birth
     *  date        range   vrai si record est dans l'intervalle
     *
     *  BEF         date    faux
     *  BEF         BEF     vrai si record est avant birth
     *  BEF         AFT     vrai si record est apres birth
     *  BEF         range   vrai si record est entièrement dans l'intervalle birth
     *
     *  AFT         date    faux
     *  AFT         BEF     vrai si record est avant birth
     *  AFT         AFT     vrai si record est apres birth
     *  AFT         range   vrai si record est dans l'intervalle
     *
     *  range       date    faux
     *  range       BEF     vrai si l'intervalle de record est avant birth
     *  range       AFT     vrai si l'intervalle de record est apres birth
     *  range       range   vrai si l'intersection des intervalles n'est pas vide

     * @param recordDate date du releve
     * @param birthDate date de naissance
     * @return
     */
    static protected boolean isBestBirthDate(PropertyDate recordDate, PropertyDate birthDate, PropertyDate resultDate) {
        boolean result;
        try {
            if ( !birthDate.isValid() ) {
                result = true;
            } else if (birthDate.getFormat() == PropertyDate.DATE) {
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    // je compare l'année , puis le mois , puis le jour
                    if ( birthDate.getStart().getYear() == PointInTime.UNKNOWN ) {
                        if ( birthDate.getStart().getYear() != PointInTime.UNKNOWN ) {
                           result = true;
                        } else {
                           result = false;
                        }
                    } else if ( birthDate.getStart().getMonth() == PointInTime.UNKNOWN ) {
                        if ( birthDate.getStart().getMonth() != PointInTime.UNKNOWN ) {
                           result = true;
                        } else {
                           result = false;
                        }
                    } else  if ( birthDate.getStart().getDay() == PointInTime.UNKNOWN ) {
                        if ( birthDate.getStart().getDay() != PointInTime.UNKNOWN ) {
                           result = true;
                        } else {
                           result = false;
                        }
                    } else {
                        result = false;
                    }
                    result = true;
                } else {
                    result = false;
                }
            } else {
                int start1;
                int start2;
                int end1;
                int end2;
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = start1;
                } else if (recordDate.getFormat() == PropertyDate.BEFORE || recordDate.getFormat() == PropertyDate.TO) {
                    start1 = Integer.MIN_VALUE;
                    end1 = recordDate.getStart().getJulianDay();
                } else if (recordDate.getFormat() == PropertyDate.AFTER || recordDate.getFormat() == PropertyDate.FROM) {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = Integer.MAX_VALUE;
                } else if (recordDate.getFormat() == PropertyDate.ABOUT || recordDate.getFormat() == PropertyDate.ESTIMATED) {
                    PointInTime startPit = new PointInTime();
                    startPit.set(recordDate.getStart());
                    start1 = startPit.add(0, 0, -aboutYear).getJulianDay();
                    PointInTime endPit = new PointInTime();
                    endPit.set(recordDate.getStart());
                    end1 = startPit.add(0, 0, +aboutYear).getJulianDay();
                } else {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = recordDate.getEnd().getJulianDay();
                }

                if (birthDate.getFormat() == PropertyDate.DATE) {
                    // intervalle [start2 , start2]
                    start2 = birthDate.getStart().getJulianDay();
                    end2 = start2;
                } else if (birthDate.getFormat() == PropertyDate.BEFORE || birthDate.getFormat() == PropertyDate.TO) {
                    // intervalle [start2 - 100 ans , start2]
                    PointInTime startPit = new PointInTime();
                    startPit.set(birthDate.getStart());
                    start2 = Integer.MIN_VALUE; //startPit.add(0, 0, -indiMaxYearOld).getJulianDay();
                    end2 = birthDate.getStart().getJulianDay();
                } else if (birthDate.getFormat() == PropertyDate.AFTER || birthDate.getFormat() == PropertyDate.FROM) {
                    // intervalle [start2 , start2 + 100 ans]
                    start2 = birthDate.getStart().getJulianDay();
                    PointInTime startPit = new PointInTime();
                    startPit.set(birthDate.getStart());
                    end2 = Integer.MAX_VALUE; //startPit.add(0, 0, +indiMaxYearOld).getJulianDay();
                } else if (birthDate.getFormat() == PropertyDate.ABOUT || birthDate.getFormat() == PropertyDate.ESTIMATED) {
                    // intervalle [start2 , end2]
                    PointInTime startPit = new PointInTime();
                    startPit.set(birthDate.getStart());
                    start2 = startPit.add(0, 0, -aboutYear).getJulianDay();
                    PointInTime endPit = new PointInTime();
                    endPit.set(birthDate.getStart());
                    end2 = startPit.add(0, 0, +aboutYear).getJulianDay();
                } else {
                    start2 = birthDate.getStart().getJulianDay();
                    end2 = birthDate.getEnd().getJulianDay();
                }

                // je verifie si l'intervalle 1 est inclus dans l'intervalle 2
                if (start1 >= start2  && end1 <= end2 ) {
                    result = true;
                    // je renseigne la date resultat
                    if (resultDate != null) {
//                        if (startMax == Integer.MIN_VALUE && endMin == Integer.MAX_VALUE) {
//                            result = false;
//                        } else if (startMax == Integer.MIN_VALUE) {
//                            resultDate.setFormat(PropertyDate.BEFORE);
//                            resultDate.getStart().set(pitEnd);
//                        } else if (endMin == Integer.MAX_VALUE) {
//                            resultDate.setFormat(PropertyDate.AFTER);
//                            resultDate.getStart().set(pitStart);
//                        } else {
//                            resultDate.setFormat(PropertyDate.BETWEEN_AND);
//                            resultDate.getStart().set(pitStart);
//                            resultDate.getStart().set(pitEnd);
//                        }
                    }
                } else {
                    // l'intersection entre les intervalles est nulle
                    result = false;
                }
            }
        } catch (GedcomException ex) {
            result = false;
        }

        return result;
    }



    /**
     * retourne la profession d'un individu a une date donnée
     * @param indi
     * @param occupation
     * @param occupationDate
     * @return occution property or null
     */
    static protected Property findOccupation(final Indi indi, final String occupationName, final PropertyDate occupationDate) {
        Property occupationProperty = null;

        for(Property occu : indi.getProperties("OCCU")){
            if( occupationName.equals(occu.getValue())) {
                for( Property occuDate :  occu.getProperties("DATE")) {
                    if ( occupationDate.compareTo((PropertyDate)occuDate) == 0 ) {
                        // TODO compare des dates range
                        occupationProperty = occu;
                        break;
                    } else if ( ! occuDate.isValid() ) {
                        occupationProperty = occuDate;
                    }
                }
            }
        }
        return occupationProperty;
    }

    static protected void addOccupation(Indi indi, String occupation, PropertyDate occupationDate, Record record ) {
        Property occupationProperty = findOccupation(indi, occupation, occupationDate);
        if (occupationProperty == null) {
            // j'ajoute la profession et la date si elle n'existait pas deja
            occupationProperty = indi.addProperty("OCCU", occupation);
            PropertyDate date = (PropertyDate) occupationProperty.addProperty("DATE", "");
            date.setValue(occupationDate.getValue());
            PropertyPlace place = (PropertyPlace) occupationProperty.addProperty("PLAC", "");
            place.setValue(record.getEventPlace().toString());
            String noteText ;
            if ( record instanceof RecordBirth) {
                noteText = MessageFormat.format("Profession indiquée dans l''acte de naissance de {0} {1} le {2} ( {3} ) ",
                        record.getIndiFirstName().toString(),
                        record.getIndiLastName().toString(),
                        record.getEventDateString(),
                        record.getEventPlace().getCityName()
                        );
            } else if ( record instanceof RecordMarriage) {
                noteText = MessageFormat.format("Profession indiquée dans l'acte de maraige de {0} {1} et {2} {3} le {4} ( {5} ) ",
                        record.getIndiFirstName().toString(),
                        record.getIndiLastName().toString(),
                        record.getWifeFirstName().toString(),
                        record.getWifeLastName().toString(),
                        record.getEventDateString(),
                        record.getEventPlace().getCityName()
                        );
            } else if ( record instanceof RecordDeath) {
                noteText = MessageFormat.format("Profession indiquée dans l'acte de décès de {0} {1} le {1} ( {2} ) ",
                        record.getIndiFirstName().toString(),
                        record.getIndiLastName().toString(),
                        record.getEventDateString(),
                        record.getEventPlace().getCityName()
                        );
            }  else {
                 noteText = MessageFormat.format("Profession indiquée dans l'acte {0} entre {1} {2} et {3} {4} le {5} ( {6}, {7}) ",
                        record.getEventType().toString(),
                        record.getIndiFirstName().toString(),
                        record.getIndiLastName().toString(),
                        record.getWifeFirstName().toString(),
                        record.getWifeLastName().toString(),
                        record.getEventDateString(),
                        record.getEventPlace().getCityName(),
                        record.getNotary().toString()
                        );
            }
            occupationProperty.addProperty("NOTE", noteText);
        }
    }

    String getRowTypeLabel(RowType rowType) {
        return NbBundle.getMessage(MergeModel.class, "MergeModel."+rowType.toString());
    }


    PropertyDate getIndiFatherBirthDate(Record record) {
        PropertyDate recordBirthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateField() : record.getIndiBirthDate().getPropertyDate();

        PropertyDate indiFatherBirthDate = new PropertyDate();
        indiFatherBirthDate.setValue(recordBirthDate.getValue());
        // le pere doit être né avant minParentYearOld avant la naissance
        indiFatherBirthDate.setValue(String.format("BEF %d", indiFatherBirthDate.getStart().add(0, 0, -minParentYearOld).getYear()));
        return indiFatherBirthDate;
    }

    PropertyDate getIndiFatherDeathDate(Record record) {
        PropertyDate recordBirthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateField() : record.getIndiBirthDate().getPropertyDate();

        PropertyDate indiFatherDeathDate = new PropertyDate();
        indiFatherDeathDate.setValue(recordBirthDate.getValue());
        // le pere doit être decede apres 9 mois avant la naissance
        indiFatherDeathDate.setValue(String.format("AFT %d", indiFatherDeathDate.getStart().add(0, -9, 0).getYear()));
        return indiFatherDeathDate;
    }

    PropertyDate getIndiMotherBirthDate(Record record) {
        PropertyDate recordBirthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateField() : record.getIndiBirthDate().getPropertyDate();

        PropertyDate indiMotherBirthDate = new PropertyDate();
        indiMotherBirthDate.setValue(recordBirthDate.getValue());
        // la mere doit être nee avant  minParentYearOld  avant la naissance
        indiMotherBirthDate.setValue(String.format("BEF %d", indiMotherBirthDate.getStart().add(0, 0, -minParentYearOld).getYear()));
        return indiMotherBirthDate;
    }


    PropertyDate getIndiMotherDeathDate(Record record) {
        PropertyDate recordBirthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateField() : record.getIndiBirthDate().getPropertyDate();

        PropertyDate indiMotherDeathDate = new PropertyDate();
        indiMotherDeathDate.setValue(recordBirthDate.getValue());
        // la mere doit être decedee apres la naissance
        indiMotherDeathDate.setValue(String.format("AFT %d", indiMotherDeathDate.getStart().getYear()));
        return indiMotherDeathDate;
    }


    /**
     * concatene plusieurs commentaires dans une chaine
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



    static protected enum RowType {
        Separator,
        Source,
        //  indi ///////////////////////////////////////////////////////////////////
        IndiFirstName,
        IndiLastName,
        IndiSex,
        IndiAge,
        IndiBirthDate,
        IndiDeathDate,
        IndiPlace,
        IndiOccupation,
        IndiComment,
        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        indiMarriedFirstName,
        indiMarriedLastName,
        //indiMarriedSex,
        indiMarriedDead,
        indiMarriedOccupation,
        indiMarriedComment,
        //  indi father ////////////////////////////////////////////////////////////
        IndiFamily,
        IndiFatherEntity,
        IndiMotherEntity,
        IndiFatherFirstName,
        IndiFatherLastName,
        IndiFatherBirthDate,
        IndiFatherDeathDate,
        IndiFatherOccupation,
        IndiFatherComment,
        IndiMotherFirstName,
        IndiMotherLastName,
        IndiMotherBirthDate,
        IndiMotherDeathDate,
        IndiMotherOccupation,
        IndiMotherComment,
        //  wife ///////////////////////////////////////////////////////////////////
        wifeFirstName,
        wifeLastName,
        wifeSex,
        //wifeDead,
        wifeAge,
        wifeBirthDate,
        wifePlace,
        wifeOccupation,
        wifeComment,
        //  wifeMarried ///////////////////////////////////////////////////////////
        wifeMarriedFirstName,
        wifeMarriedLastName,
        //wifeMarriedSex,
        wifeMarriedDead,
        wifeMarriedOccupation,
        wifeMarriedComment,
        //  wifeFather ///////////////////////////////////////////////////////////
        wifeFatherFirstName,
        wifeFatherLastName,
        wifeFatherDead,
        wifeFatherOccupation,
        wifeFatherComment,
        wifeMotherFirstName,
        wifeMotherLastName,
        wifeMotherDead,
        wifeMotherOccupation,
        wifeMotherComment,
        // wintness ///////////////////////////////////////////////////////////////
        witness1FirstName,
        witness1LastName,
        witness1Occupation,
        witness1Comment,
        witness2FirstName,
        witness2LastName,
        witness2Occupation,
        witness2Comment,
        witness3FirstName,
        witness3LastName,
        witness3Occupation,
        witness3Comment,
        witness4FirstName,
        witness4LastName,
        witness4Occupation,
        witness4Comment
    }

}
