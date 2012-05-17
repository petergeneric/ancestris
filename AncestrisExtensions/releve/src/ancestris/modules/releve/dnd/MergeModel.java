package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.Source;
import genj.gedcom.time.PointInTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

public abstract class MergeModel extends AbstractTableModel {

    static int minParentYearOld = 15;
    static int indiMaxYearOld = 100;
    static int aboutYear = 5;

    protected class MergeRow {

        String label;
        Object entityValue;
        Object recordValue;
        Object[] entityChoice;
        Object[] recordChoice;
        boolean merge;
        int sameValue ;  // 1=sameValue 0=diffrentValue -1= conflit
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
    static protected MergeModel  createMergeModel(Record record, Gedcom gedcom, Entity entity) {
        MergeModel mergeModel = null;
        if( record instanceof RecordBirth) {
            mergeModel = new MergeModelBirth((RecordBirth) record, gedcom, (Indi) entity);
        }

        return mergeModel;
    }

    /**
     * ajoute une ligne dans le modele
     * @param rowType
     * @param label
     * @param recordValue
     * @param entityValue
     */
    void addRow(RowType rowType, String recordValue, String entityValue) {
        MergeRow mergeRow = new MergeRow();
        dataMap.put(rowType, mergeRow);
        dataList.add(mergeRow);
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.entityValue = entityValue;
        mergeRow.recordValue = recordValue;
        mergeRow.entityChoice = null;
        mergeRow.recordChoice = null;
        mergeRow.merge = !recordValue.equals(entityValue);
        mergeRow.sameValue = mergeRow.merge ? 1 :0;
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
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.entityValue = entityValue;
        mergeRow.recordValue = recordValue;
        mergeRow.entityChoice = null;
        mergeRow.recordChoice = null;
        if ( recordValue == null ) {
             mergeRow.merge = false;
             mergeRow.sameValue = 0;
        } else if ( entityValue == null ) {
             mergeRow.merge = true;
             mergeRow.sameValue = 1;
        } else {
            mergeRow.merge = isBestBirthDate(recordValue, entityValue, null);
            mergeRow.sameValue = mergeRow.merge ? 1 :0;
        }
    }

    /**
     * ajoute une ligne dans le modele
     * @param rowType
     * @param label
     * @param recordValues
     * @param entityValues
     */void addRow(RowType rowType, Object[] recordValues, Object[] entityValues) {
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
        mergeRow.label = getRowTypeLabel(rowType);
        mergeRow.entityValue = defaultEntityValue;
        mergeRow.recordValue = defaultRecordValue;
        mergeRow.entityChoice = entityValues;
        mergeRow.recordChoice = recordValues;
        if (defaultRecordValue == null) {
            mergeRow.merge = false;
            mergeRow.sameValue = -2;
        } else if (defaultEntityValue == null) {
            mergeRow.merge = true;
            mergeRow.sameValue = mergeRow.merge ? 1 :0;
        } else if (defaultRecordValue instanceof Property) {
            mergeRow.merge = ((Property) defaultRecordValue).compareTo((Property) defaultEntityValue) != 0;
            mergeRow.sameValue = mergeRow.merge ? 1 :0;
        } else {
            mergeRow.merge = !defaultRecordValue.equals(defaultEntityValue);
            mergeRow.sameValue = mergeRow.merge ? 1 :0;
        }

    }

    protected MergeRow getRow(RowType rowType) {
        return dataMap.get(rowType);
    }

    // methodes abstraites
    protected abstract void copyRecordToEntity();
    protected abstract String getTitle();

    /**
     * i
     */
    private String[] columnNames = {
        "",
        NbBundle.getMessage(MergeModel.class, "MergePanel.title.recordColumn"),
        "=>",
        NbBundle.getMessage(MergeModel.class, "MergePanel.title.indiColumn"),
    };
    private Class[] columnClass = {String.class, Object.class, Boolean.class, Object.class};

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
            case 0:
                break;
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
    static protected List<Entity> findSameIndi(Record record, Gedcom gedcom) {
        return findSameIndi(record, gedcom, null);
    }

    /**
     * retourne la liste des individus qui ont le même nom et date de naissance
     * compatible compatible avec celle du relevé
     * mais qui ont un identifiant different de l'individu excludeIndi
     * @param record
     * @param gedcom
     * @param excludeIndi
     * @return liste des individus
     */
    static protected List<Entity> findSameIndi(Record record,  Gedcom gedcom, Indi excludeIndi) {
        List<Entity> sameIndis = new ArrayList<Entity>();

        for (Indi indi : gedcom.getIndis()) {
            if (record.getIndiFirstName().toString().equals(indi.getFirstName())
                    && record.getIndiLastName().toString().equals(indi.getLastName())
                ) {

                if ( excludeIndi != null && excludeIndi.compareTo(indi)==0 ) {
                    continue;
                }


                // je recupere la date de naissance
                PropertyDate recordBirthDate;
                if ( record.getIndiBirthDate().getPropertyDate() != null
                      && record.getIndiBirthDate().getPropertyDate().isComparable()
                    ) {
                    recordBirthDate = record.getIndiBirthDate().getPropertyDate();
                } else {
                    // si la date de naisance n'est pas renseignée , j'utilise la date
                    // du relevé.
                    if (record.getEventDateField() != null
                          && record.getEventDateField().isComparable()
                        ) {
                        recordBirthDate = record.getEventDateField();
                    } else {
                        // j'abandonne s'il n'y a pas de date
                        continue;
                    }
                }

                // petit raccourci pour gagner du temps
                PropertyDate indiBirtDate = indi.getBirthDate();

                if (indiBirtDate == null && indiBirtDate.isComparable()) {
                    // la date ,l'individu n'est pas valide
                    continue;
                }
                
                if (! isCompatible(recordBirthDate, indiBirtDate)) {
                    // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                    continue;
                }

                Fam parentFamily = indi.getFamilyWhereBiologicalChild() ;
                if ( parentFamily != null ) {
                    PropertyDate marriageDate = parentFamily.getMarriageDate();
                    if( marriageDate != null) {
                        // le releve de naissance doit être après la date mariage.
                        if ( !isRecordAfterThanDate(recordBirthDate, marriageDate, 0, 0 ) ) {
                            continue;
                        }
                    }

                    Indi father = parentFamily.getHusband();
                    if (father != null) {
                        // le pere doit avoir au moins minParentYearOld
                        if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                            continue;
                        }
                        // le pere ne doit pas etre decede 9 mois avant la date de naissance
                        if (!isRecordAfterThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
                            continue;
                        }
                    }

                    Indi mother = parentFamily.getWife();
                    if (mother != null) {
                        // la mere doit avoir au moins minParentYearOld
                        if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                            continue;
                        }
                        // la mere ne doit pas etre decedee avant la date de naissance
                        if (!isRecordAfterThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
                            continue;
                        }
                    }
                }
                // j'ajoute l'individu dans la liste
                sameIndis.add(indi);
            }
        }
        return sameIndis;
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
     * recherche les familles des parents potentielles de l'individu selectionne
     * ou dans tous le gedcom si l'individu selectionne n'a pas de famille
     * @param record
     * @param record
     * @return
     */
    static protected List<Fam> findParentFamily(RecordBirth record, Gedcom gedcom, Indi selectedIndi) {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        if ( selectedIndi != null) {
            Fam[] fams = selectedIndi.getFamiliesWhereChild();
            if (fams.length > 0) {
                // l'individu a deja des parents
                parentFamilies.addAll(Arrays.asList(fams));
            }
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
                && !record.getIndiFatherLastName().toString().equals(husband.getLastName()) ){
                continue;
            }
            //meme prénom du pere
            if (!record.getIndiFatherFirstName().isEmpty()
                && !record.getIndiFatherFirstName().toString().equals(husband.getFirstName()) ){
                continue;
            }
            // meme nom de la mere
            if (!record.getIndiMotherLastName().isEmpty()
                && !record.getIndiMotherLastName().toString().equals(wife.getLastName()) ){
                continue;
            }
            //meme prénom de la mere
            if (!record.getIndiMotherFirstName().isEmpty()
                && !record.getIndiMotherFirstName().toString().equals(wife.getFirstName()) ){
                continue;
            }

            // je controle les dates par rapport à la date de naissance du releve
            PropertyDate recordBirthDate = record.getIndiBirthDate().getPropertyDate();
            if (!recordBirthDate.isComparable()) {
                recordBirthDate = record.getEventDateField();
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
            if (!isRecordAfterThanDate(recordBirthDate, husband.getDeathDate(), 9, 0)) {
                continue;
            }
            // la mere ne doit pas etre decedee avant la date de naissance
            if (!isRecordAfterThanDate(recordBirthDate, wife.getDeathDate(), 0, 0)) {
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
     * retourne true si la date de naissance du parent est inférieure à la date
     * du relevé (diminuée de l'age minimum pour être parent)
     * et si le parent a moins de 100 ans à la date du relevé.
     * Autrement dit :
     *   recordBirthDate - indiMaxYearOld <  indiFirthDate  < recordBirthDate - (minMonthShift + minYearShift)
     * @param recordBirthDate  date du relevé 
     * @param parentBirthDate  date de naissance du parent
     * @param minDiff
     * @return true si indiFirthDate + minMonthDiff + minYearDiff <= recordBirthDate
     */
    static private boolean isRecordAfterThanDate(PropertyDate recordBirthDate, PropertyDate parentBirthDate, int minMonthShift, int minYearShift) {
        boolean result;
        if (recordBirthDate == null ) {
            return true;
        } else if (!recordBirthDate.isComparable()) {
            return true;
        }

        if (parentBirthDate == null) {
            return true;
        } else if (!parentBirthDate.isComparable()) {
            return true;
        }

        PointInTime recordPit = new PointInTime();
        recordPit.set( recordBirthDate.getStart());
        //recordPit.add(0, -minMonthShift, - minYearShift);

        try {
            int minDiffays;
            int maxDiffays;
            if (parentBirthDate.getFormat() == PropertyDate.DATE) {
                minDiffays = parentBirthDate.getStart().compareTo(recordPit);
                maxDiffays = minDiffays;
            } else if (parentBirthDate.getFormat() == PropertyDate.BETWEEN_AND || parentBirthDate.getFormat() == PropertyDate.FROM_TO) {
                minDiffays = getMindiff(recordPit, recordPit, parentBirthDate.getStart(), parentBirthDate.getEnd());
                maxDiffays = getMaxdiff(recordPit, recordPit, parentBirthDate.getStart(), parentBirthDate.getEnd());
            } else if (parentBirthDate.getFormat() == PropertyDate.FROM || parentBirthDate.getFormat() == PropertyDate.AFTER) {
                PointInTime endPit = new PointInTime();
                endPit.set( parentBirthDate.getStart());
                endPit.add(0, 0, indiMaxYearOld);
                minDiffays = getMindiff(recordPit, recordPit, parentBirthDate.getStart(), endPit) ;
                maxDiffays = getMaxdiff(recordPit, recordPit, parentBirthDate.getStart(), endPit) ;
            } else if (parentBirthDate.getFormat() == PropertyDate.TO || parentBirthDate.getFormat() == PropertyDate.BEFORE) {
                PointInTime startPit = new PointInTime();
                startPit.set( parentBirthDate.getStart());
                startPit.add(0, 0, -indiMaxYearOld);
                minDiffays = getMindiff(recordPit, recordPit, startPit, parentBirthDate.getStart());
                maxDiffays = getMaxdiff(recordPit, recordPit, startPit, parentBirthDate.getStart());
            } else if (parentBirthDate.getFormat() == PropertyDate.ABOUT || parentBirthDate.getFormat() == PropertyDate.ESTIMATED) {
                PointInTime startPit = new PointInTime();
                startPit.set( parentBirthDate.getStart());
                startPit.add(0, 0, -aboutYear);
                PointInTime endPit = new PointInTime();
                endPit.set( parentBirthDate.getStart());
                endPit.add(0, 0, +aboutYear);
                minDiffays = getMindiff(recordPit, recordPit, startPit, endPit);
                maxDiffays = getMaxdiff(recordPit, recordPit, startPit, endPit);
            } else {
                minDiffays = recordPit.compareTo(parentBirthDate.getStart());
                maxDiffays = minDiffays;
            }

            if (minDiffays >= -(indiMaxYearOld * 365.25) && maxDiffays <= -(minYearShift * 365.25 + minMonthShift * 3.4) ) {
                result = true;
            } else {
                result = false;
            }
        } catch (GedcomException ex) {
            result = false;
        }
        return result;
    }

    /**
     * retourne le nombre de jour minimal entre les intervalles
     * [start1 end1] et [start2 end2]
     *
     *        s1   e1          s2    e2
     *    --- [****]-----------[*****] -----
     *              --- x>0 -->
     *
     *        s2   e2          s1    e1
     *    --- [****]-----------[*****] -----
     *              <--- x<0 --

     *        s2      s1      e2     e1
     *    ----[*******[*******]******] -----
     *                   x=0
     * @param refTime
     * @param start
     * @param end
     * @return nombre de jours juliens, negatif si [start1 end1] est apres [start2 end2]
     */
    static private int getMindiff(final PointInTime start1, final PointInTime end1 ,  final PointInTime  start2, final PointInTime end2) throws GedcomException {
        int jdStart1 = start1.getJulianDay();
        int jdEnd1   = end1.getJulianDay();
        int jdStart2 = start2.getJulianDay();
        int jdEnd2   = end2.getJulianDay();

        // j'ordonne les dates du premier intervalle
        if ( jdStart1 >  jdEnd1) {
            int temp = jdStart1;
            jdStart1 = jdEnd1;
            jdEnd1 = temp;
        }
         // j'ordonne les dates du deuxième intervalle
        if ( jdStart2 >  jdEnd2) {
            int temp = jdStart2;
            jdStart2 = jdEnd2;
            jdEnd2 = temp;
        }

        if ( jdEnd1 < jdStart2) {
            return jdStart2 - jdEnd1;
        } else if ( jdStart1 > jdEnd2 ) {
            return jdEnd2 -jdStart1;
        } else {
            return 0;
        }
    }


    /**
     * retourne le nombre de jour maximum entre les intervalles [start1 end1]
     * et [start2 end2]
     *
     *          1                 2
     *    --- [****]-----------[*****] -----
     *        -------- x>0 ---------->
     *
     *          2                 1
     *    --- [****]-----------[*****] -----
     *         <------- x<0 ---------

     *              2           1
     *    ------[******[*******]*****] -----
     *          <------- x<0 --------
     * @param refTime
     * @param start
     * @param end
     * @return
     */
    static private int getMaxdiff(final PointInTime start1, final PointInTime end1 ,  final PointInTime  start2, final PointInTime end2) throws GedcomException {
        int jdStart1 = start1.getJulianDay();
        int jdEnd1   = end1.getJulianDay();
        int jdStart2 = start2.getJulianDay();
        int jdEnd2   = end2.getJulianDay();

        // j'ordonne les dates du premier intervalle
        if ( jdStart1 >  jdEnd1) {
            int temp = jdStart1;
            jdStart1 = jdEnd1;
            jdEnd1 = temp;
        }
         // j'ordonne les dates du deuxième intervalle
        if ( jdStart2 >  jdEnd2) {
            int temp = jdStart2;
            jdStart2 = jdEnd2;
            jdEnd2 = temp;
        }

        if ( jdEnd1 < jdStart2) {
            return jdEnd2 - jdStart1;
        } else {
            return jdStart2 -jdEnd1;
        }
    }


    /**
     *  retourne vrai si la date du releve est plus precise que la date de naissance
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
     *  BEF         range   vrai si record est dans l'intervalle birth
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
            if (birthDate.getFormat() == PropertyDate.DATE) {
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    // je retourne le plus precis
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

                // je verifie si [start1 end1] est inclu dans [start2 end2]
//                int startMax;
//                int endMin;
//                PointInTime pitStart;
//                PointInTime pitEnd;
//                if (start1 > start2) {
//                    startMax = start1;
//                    pitStart = recordDate.getStart();
//                } else {
//                    startMax = start2;
//                    pitStart = birthDate.getStart();
//                }
//
//                if (end1 < end2) {
//                    endMin = end1;
//                    pitEnd = recordDate.getEnd();
//                } else {
//                    endMin = end2;
//                    pitEnd = birthDate.getEnd();
//                }

                // je verifie si l'intersection n'est pas nulle
                if (start1 >= start2  && end1 <= end2 ) {
                    //TODO je verifie 
                    // l'intersection entre les intervalles n'est pas nulle
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
    static protected Property findOccupation(final Indi indi, final String occupation, final PropertyDate occupationDate) {
        Property occupationProperty = null;

        for(Property occu : indi.getProperties("OCCU")){
            if( occupation.equals(occu.getValue())) {
                for( Property occuDate :  occu.getProperties("DATE")) {
                    if ( occupationDate.compareTo((PropertyDate)occuDate) == 0 ) {
                        // TODO compare des dates range
                        occupationProperty = occuDate;
                        break;
                    } else if ( ! occuDate.isValid() ) {
                        occupationProperty = occuDate;
                    }
                }
            }
        }
        return occupationProperty;
    }

    static protected void addOccupation(final Indi indi, final String occupation, final PropertyDate occupationDate) {
        Property occupationProperty = findOccupation(indi, occupation, occupationDate);
        if (occupationProperty == null) {
            // j'ajoute la profession et la date si elle n'existait pas deja
            occupationProperty = indi.addProperty("OCCU", occupation);
            PropertyDate occuDate = (PropertyDate) occupationProperty.addProperty("DATE", "");
            occuDate.setValue(occupationDate.getValue());
        }
    }

    String getRowTypeLabel(RowType rowType) {
        return NbBundle.getMessage(MergeModel.class, "MergeModel."+rowType.toString());
    }


    static protected enum RowType {

        Source,
        //  indi ///////////////////////////////////////////////////////////////////
        IndiFirstName,
        IndiLastName,
        IndiSex,
        IndiAge,
        IndiBirthDate,
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
        IndiFatherFirstName,
        IndiFatherLastName,
        IndiFatherBirthDate,
        IndiFatherDead,
        IndiFatherOccupation,
        IndiFatherComment,
        IndiMotherFirstName,
        IndiMotherLastName,
        IndiMotherBirthDate,
        IndiMotherDead,
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
