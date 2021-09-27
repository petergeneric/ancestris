package ancestris.reports.gedcomstatistics;
/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.report.Report;
import genj.util.ReferenceSet;
import org.openide.util.lookup.ServiceProvider;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportGedcomStatistics.java,v 1.73 2010-01-24 16:08:39 nmeier Exp $
 * @author Francois Massonneau <francois@ancestris.org>
 * @author Carsten Muessig <carsten.muessig@gmx.net>
 * @version 2.2
 */
@SuppressWarnings("unchecked")
@ServiceProvider(service=Report.class)
public class ReportGedcomStatistics extends Report {

    /** if individuals should be analyzed */
    public boolean analyzeIndividuals = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportAgeToIndis = true;

    /** if families should be analyzed */
    public boolean analyzeFamilies = true;
    /** whether individuals with min. / max. marriage age should be reported */
    public boolean reportIndisToMarriageAge = true;
    /** whether indis with min/max age at child birth should be reported */
    public int reportFamsToChildren = 1;
    public String[] reportFamsToChildrens = { translate("choice.all"), translate("choice.minmax"), translate("choice.none")};
    /** whether individuals with min. / max. age at child birth should be reported */
    public boolean reportIndisToChildBirth = true;

    /** whether the surnames should be analyzed */
    public boolean analyzeLastNames = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportAgeToLastNames = true;
    /** whether indis with min./max. marriage should be reported */
    public boolean reportLastNamesToMarriageAge = true;
    /** whether indis with min./max. children should be reported */
    public int reportLastNamesToChildren = 2;
    public String[] reportLastNamesToChildrens = { translate("choice.all"), translate("choice.minmax"), translate("choice.none")};
    /** whether indis with min./max. ages at child births should be reported */
    public boolean reportLastNamesToChildBirths = true;
    /** whether we sort last names by name or frequency */
    public boolean sortLastNamesByName = true;

    /** whether occupatoins should be analyzed */
    public boolean analyzeOccupations = true;
    /** whether the occupations should be sorted by name or frequency */
    public boolean sortOccupationsByName = true;
    /** whether individuals with occucaptions should be reported */
    public boolean reportIndisToOccupations = true;

    /** if birth places should be analyzed */
    public boolean analyzeBirthPlaces = true;
    /** whether indis to birthplaces should be reported */
    public boolean reportIndisToBirthPlaces = true;
    /** whether we sort birth places by name or freqeuncy */
    public boolean sortBirthPlacesByName = true;

    /** if baptism places should be analyzed */
    public boolean analyzeBaptismPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToBaptismPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortBaptismPlacesByName = true;

    /** if marriage places should be analyzed */
    public boolean analyzeMarriagePlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToMarriagePlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortMarriagePlacesByName = true;

    /** if emigration places should be analyzed */
    public boolean analyzeEmigrationPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToEmigrationPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortEmigrationPlacesByName = true;

    /** if immigration places should be analyzed */
    public boolean analyzeImmigrationPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToImmigrationPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortImmigrationPlacesByName = true;

    /** if naturalization places should be analyzed */
    public boolean analyzeNaturalizationPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToNaturalizationPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortNaturalizationPlacesByName = true;

    /** if death places should be analyzed */
    public boolean analyzeDeathPlaces = true;
    /** whether indis to deathplaces should be reported */
    public boolean reportIndisToDeathPlaces = true;
    /** whether we sort death places by name or freqeuncy */
    public boolean sortDeathPlacesByName = true;

    /** to store data about individuals
     * (all, males, females, unknown gender, husbands, wifes)
     */
    private static class StatisticsIndividuals {
        /** which places the statistic is about (ALL|MALE|FEMALE|UNKNOWN) */
        int which = -1;
        /** number of individuals */
        int number = 0;
        /** individuals sorted by age */
        ReferenceSet age = new ReferenceSet();
        /** individuals sorted by age at child births */
        ReferenceSet childBirthAge = new ReferenceSet();
        /** min. age of individuals at child birth */
        int minChildBirthAge = Integer.MAX_VALUE;
        /** max. age of individuals at child birth */
        int maxChildBirthAge = Integer.MIN_VALUE;
        /** number of childbirths */
        int childBirthNumber = 0;
        /** age of individuals at child birth added up */
        int sumChildBirthAge = 0;
        /** min. age of individuals */
        int minAge = Integer.MAX_VALUE;
        /** min. age of individuals */
        int maxAge = Integer.MIN_VALUE;
        /** age of individuals added up */
        int sumAge = 0;
    }

    /** to store data about last names */
    private static class StatisticsLastNames {

        /** individiuals sorted by last names */
        ReferenceSet lastNamesIndis = new ReferenceSet();
        /** statistics of all, males, females, unknown gender sorted by last names */
        ReferenceSet lastNamesStatistic = new ReferenceSet();
    }

    /** to store data about occupations */
    private static class StatisticsOccupations {

        /** number of all individuals */
        int numberIndis = 0;
        /** individiuals sorted by occupations */
        ReferenceSet occupations = new ReferenceSet();
    }

    /** to store data about families */
    private static class StatisticsFamilies {
        /** statistics of husbands */
        StatisticsIndividuals husbands = new StatisticsIndividuals();
        /** statistics of wifes */
        StatisticsIndividuals wifes = new StatisticsIndividuals();
        /** number of families */
        int number = 0;
        /** number of families with children */
        int withChildren = 0;
        /** families sorted by number of children */
        ReferenceSet children = new ReferenceSet();
        /** min. number of children */
        int minChildren = 999;
        /** max. number of children */
        int maxChildren = 0;
        /** number of children added up */
        int sumChildren = 0;
    }

    /** to store data about places */
    private static class StatisticsPlaces {
        /** which places the statistic is about (BIRTH|BAPTISM|MARRIAGE|EMIGRATION|IMMIGRATION|NATURALIZATION|DEATH) */
        int which = -1;
        /** entities with known places */
        int entitiesWithKnownPlaces = 0;
        /** places sorted by name */
        ReferenceSet places = new ReferenceSet();
    }

    // constants for statistics of individuals
    private static final int ALL = 1;
    private static final int MALES = 2;
    private static final int FEMALES = 3;
    private static final int UNKNOWN = 4;

    // constants for analyze, report and print methods
    private static final int INDIS = 5;
    private static final int CHILDBIRTH = 6;

    // constants for statistics of places
    private static final int BIRTH = 7;
    private static final int BAPTISM = 8;
    private static final int MARRIAGE = 9;
    private static final int EMIGRATION = 10;
    private static final int IMMIGRATION = 11;
    private static final int NATURALIZATION = 12;
    private static final int DEATH = 13;

    /**
     * This method actually starts this report
     */
    public void start(Gedcom gedcom) {

        // stop report when no output categories choosen
        if((analyzeIndividuals==false)&&(analyzeLastNames==false)&&
        (analyzeOccupations==false)&&(analyzeFamilies==false)&&
        (analyzeBirthPlaces==false)&&(analyzeBaptismPlaces==false)&&
        (analyzeMarriagePlaces==false)&&(analyzeEmigrationPlaces==false)&&
        (analyzeImmigrationPlaces==false)&&(analyzeNaturalizationPlaces==false)&&
        (analyzeDeathPlaces==false))
            return;

        // what to analyze
        Entity[] indis = gedcom.getEntities(Gedcom.INDI, "");
        Entity[] fams = gedcom.getEntities(Gedcom.FAM,"");

        // where to write the statistic data
        StatisticsIndividuals all=null, males=null, females=null, unknown=null;
        StatisticsLastNames lastNames = null;
        StatisticsOccupations occupations = null;
        StatisticsFamilies families=null;
        StatisticsPlaces births=null, baptisms=null, marriages=null, emigrations=null, immigrations=null, naturalizations=null, deaths=null;

        // now do the desired analyzes
        if(analyzeIndividuals) {
            all = new StatisticsIndividuals();
            all.which = ALL;
            males = new StatisticsIndividuals();
            males.which= MALES;
            females = new StatisticsIndividuals();
            females.which= FEMALES;
            unknown = new StatisticsIndividuals();
            unknown.which= UNKNOWN;
            analyzeIndividuals(indis, all, males, females, unknown);
        }

        if(analyzeFamilies) {
            families = new StatisticsFamilies();
            families.number = fams.length;
            analyzeFamilies(fams, null, families);
        }

        if(analyzeLastNames) {
            lastNames = new StatisticsLastNames();
            analyzeLastNames(indis, lastNames);
        }

        if(analyzeOccupations) {
            occupations = new StatisticsOccupations();
            analyzeOccupations(indis, occupations);
        }

        if(analyzeBirthPlaces) {
            births = new StatisticsPlaces();
            births.which = BIRTH;
            analyzePlaces(indis, births);
        }

        if(analyzeBaptismPlaces) {
            baptisms = new StatisticsPlaces();
            baptisms.which = BAPTISM;
            analyzePlaces(indis, baptisms);
        }

        if(analyzeMarriagePlaces) {
            marriages = new StatisticsPlaces();
            marriages.which = MARRIAGE;
            analyzePlaces(fams, marriages);
        }

        if(analyzeEmigrationPlaces) {
            emigrations = new StatisticsPlaces();
            emigrations.which = EMIGRATION;
            analyzePlaces(indis, emigrations);
        }

        if(analyzeImmigrationPlaces) {
            immigrations = new StatisticsPlaces();
            immigrations.which = IMMIGRATION;
            analyzePlaces(indis, immigrations);
        }

        if(analyzeNaturalizationPlaces) {
            naturalizations = new StatisticsPlaces();
            naturalizations.which = NATURALIZATION;
            analyzePlaces(indis, naturalizations);
        }

        if(analyzeDeathPlaces) {
            deaths = new StatisticsPlaces();
            deaths.which = DEATH;
            analyzePlaces(indis, deaths);
        }

        // generate output
        println(translate("header",gedcom.getName()));
        println();

        if(analyzeIndividuals) {
            int i;
            if(reportAgeToIndis)
                i=1;
            else
                i=3;
            reportIndividuals(i, null, 0, all, males, females, unknown);
        }

        if(analyzeFamilies)
            reportFamilies(families, reportFamsToChildren, reportIndisToChildBirth, false);

        if(analyzeLastNames)
            reportLastNames(lastNames,  sortLastNamesByName? gedcom.getCollator() : null, indis.length);

        if(analyzeOccupations)
            reportOccupations(occupations, sortOccupationsByName? gedcom.getCollator() : null);

        if(analyzeBirthPlaces) {
            println(translate("birthPlaces")+": "+new Integer(births.places.getKeys().size()));
            reportPlaces(reportIndisToBirthPlaces, sortBirthPlacesByName ? gedcom.getCollator() : null, births);
        }

        if(analyzeBaptismPlaces) {
            println(translate("baptismPlaces")+": "+new Integer(baptisms.places.getKeys().size()));
            reportPlaces(reportIndisToBaptismPlaces, sortBaptismPlacesByName ? gedcom.getCollator() : null, baptisms);
        }

        if(analyzeMarriagePlaces) {
            println(translate("marriagePlaces")+": "+new Integer(marriages.places.getKeys().size()));
            reportPlaces(reportIndisToMarriagePlaces, sortMarriagePlacesByName ? gedcom.getCollator() : null, marriages);
        }

        if(analyzeEmigrationPlaces) {
            println(translate("emigrationPlaces")+": "+new Integer(emigrations.places.getKeys().size()));
            reportPlaces(reportIndisToEmigrationPlaces, sortEmigrationPlacesByName ? gedcom.getCollator() : null, emigrations);
        }

        if(analyzeImmigrationPlaces) {
            println(translate("immigrationPlaces")+": "+new Integer(immigrations.places.getKeys().size()));
            reportPlaces(reportIndisToImmigrationPlaces, sortImmigrationPlacesByName ? gedcom.getCollator() : null, immigrations);
        }

        if(analyzeNaturalizationPlaces) {
            println(translate("naturalizationPlaces")+": "+new Integer(naturalizations.places.getKeys().size()));
            reportPlaces(reportIndisToNaturalizationPlaces, sortNaturalizationPlacesByName ? gedcom.getCollator() : null, naturalizations);
        }

        if(analyzeDeathPlaces) {
            println(translate("deathPlaces")+": "+new Integer(deaths.places.getKeys().size()));
            reportPlaces(reportIndisToDeathPlaces, sortDeathPlacesByName ? gedcom.getCollator() : null, deaths);
        }

    }

    /** Rounds a number to a specified number digits in the fraction portion
     * @param number number to round
     * @param digits number of digits allowed in the fraction portion
     * @return the rounded number
     */
    private double roundNumber(double number, int digits) {
        if((Double.isNaN(number))||(Double.isInfinite(number)))
            return 0.0;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(digits);
        nf.setMaximumFractionDigits(digits);
        nf.setGroupingUsed(false);

        return Double.parseDouble(nf.format(number).replace(',','.'));
    }

    /**
     * @param e entities to analyze
     * @param places to store results
     */
    private void analyzePlaces(Entity[] e, StatisticsPlaces places) {

        Property prop;
        Property[] props;
        String place;

        for(int i=0;i<e.length;i++) {

            prop = null;
            props = null;

            switch(places.which) {

                case BIRTH:
                    props = new Property[1];
                    props[0] = e[i].getProperty(new TagPath("INDI:BIRT:PLAC"));
                    break;

                case BAPTISM:
                    ArrayList<Property> baps = new ArrayList<Property>();
                    prop = e[i].getProperty("BAPM");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:BAPM:PLAC"));
                        baps.add(prop);
                    }
                    prop = e[i].getProperty("BAPL");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:BAPL:PLAC"));
                        baps.add(prop);
                    }
                    prop = e[i].getProperty("CHR");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:CHR:PLAC"));
                        baps.add(prop);
                    }
                    prop = e[i].getProperty("CHRA");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:CHRA:PLAC"));
                        baps.add(prop);
                    }
                    props = baps.toArray(new Property[baps.size()]);
                    break;

                case EMIGRATION:
                    prop = e[i].getProperty("EMIG");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("INDI:EMIG:PLAC"));
                    break;

                case IMMIGRATION:
                    prop = e[i].getProperty("IMMI");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("INDI:IMMI:PLAC"));
                    break;

                case NATURALIZATION:
                    prop = e[i].getProperty("NATU");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("INDI:NATU:PLAC"));
                    break;

                case MARRIAGE:
                    prop = e[i].getProperty("MARR");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("FAM:MARR:PLAC"));
                    break;

                case DEATH:
                    props = new Property[1];
                    prop = e[i].getProperty("DEAT");
                    if (prop!=null)
                        props[0] = e[i].getProperty(new TagPath("INDI:DEAT:PLAC"));
                    break;

            }

            if (props!=null && props.length>0) {
                for(int j=0;j<props.length;j++) {
                    if(props[j]!=null) {
                        place = props[j].getValue();
                        if (place.length()>0) {
                            if(places.places.add(place, e[i]))
                                places.entitiesWithKnownPlaces++;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param indi individual to analyze
     * @param age of indi
     * @param single statistic for the single indi
     * @param all to store results of all individuals
     * @param which info to analyze
     */
    private void analyzeAge(Indi indi, Delta age, StatisticsIndividuals single, StatisticsIndividuals all, int which) {

        if(age==null)
            return;
        int a = age.getYears()*360+age.getMonths()*30+age.getDays();

        switch(which) {
            case INDIS:
            case MARRIAGE:

                if(all!=null) {
                    all.age.add(new Integer(a),indi);
                    all.sumAge=all.sumAge+a;
                    if(a>all.maxAge)
                        all.maxAge=a;

                    if(a<all.minAge)
                        all.minAge=a;
                }

                single.age.add(new Integer(a),indi);
                single.sumAge=single.sumAge+a;

                if(a>single.maxAge)
                    single.maxAge=a;

                if(a<single.minAge)
                    single.minAge=a;
                break;

            case CHILDBIRTH:

                single.childBirthNumber++;
                single.sumChildBirthAge = single.sumChildBirthAge + a;
                single.childBirthAge.add(new Integer(a), indi);

                if(a < single.minChildBirthAge)
                    single.minChildBirthAge = a;
                if(a > single.maxChildBirthAge)
                    single.maxChildBirthAge = a;
                break;
        }
    }

    /**
     * @param all to store results for all
     * @param males to store results for males
     * @param females to store results for females
     * @param unknown to store results for unknown
     * @param e array with individuals
     */
    private void analyzeIndividuals(Entity[] e,StatisticsIndividuals all,StatisticsIndividuals males,StatisticsIndividuals females,StatisticsIndividuals unknown) {

        for(int i=0;i<e.length;i++) {

            Delta age = null;
            Indi indi = (Indi)e[i];

            all.number++;

            if(indi.getDeathDate()!=null)
                age = indi.getAge(indi.getDeathDate().getStart());

            switch (indi.getSex()) {

                case PropertySex.MALE:
                    males.number++;
                    analyzeAge(indi, age, males, all, INDIS);
                    break;

                case PropertySex.FEMALE:
                    females.number++;
                    analyzeAge(indi, age, females, all, INDIS);
                    break;

                default:
                    unknown.number++;
                    analyzeAge(indi, age, unknown, all, INDIS);
                    break;
            }
        }
    }

    /** @param e the individuals
     * @param lastNames to store the results */
    private void analyzeLastNames(Entity[] e, StatisticsLastNames lastNames) {

        // sort indis by name
        for(int i=0;i<e.length;i++)
            lastNames.lastNamesIndis.add(((Indi)e[i]).getLastName(), (Indi)e[i]);

        // analyze all individuals with same name and store the result
        Iterator it = lastNames.lastNamesIndis.getKeys().iterator();
        ArrayList familiesToLastName = new ArrayList();
        while(it.hasNext()) {
            familiesToLastName.clear();
            String name = (String)it.next();
            // indis with the same last name
            Iterator entities = lastNames.lastNamesIndis.getReferences(name).iterator();
            // find families in which indis with the same last name are involved
            while(entities.hasNext()) {
                Indi indi = (Indi)entities.next();
                if(indi.getNoOfFams() > 0) {
                    Fam[] fams = indi.getFamiliesWhereSpouse();
                    for(int j=0;j<fams.length;j++)
                        familiesToLastName.add(fams[j]);
                }
            }
            // create statistics for individuals with the same last name
            StatisticsFamilies families = new StatisticsFamilies();
            families.number = familiesToLastName.size();
            StatisticsIndividuals all = new StatisticsIndividuals();
            all.which=ALL;
            StatisticsIndividuals males = new StatisticsIndividuals();
            males.which=MALES;
            StatisticsIndividuals females = new StatisticsIndividuals();
            females.which=FEMALES;
            StatisticsIndividuals unknown = new StatisticsIndividuals();
            unknown.which=UNKNOWN;
            // fill the statistics
            analyzeIndividuals((Entity[])lastNames.lastNamesIndis.getReferences(name).toArray(new Entity[0]), all, males, females, unknown);
            analyzeFamilies((Entity[])familiesToLastName.toArray(new Entity[0]), name, families);
            // store the statistics
            lastNames.lastNamesStatistic.add(name, all);
            lastNames.lastNamesStatistic.add(name, males);
            lastNames.lastNamesStatistic.add(name, females);
            lastNames.lastNamesStatistic.add(name, unknown);
            lastNames.lastNamesStatistic.add(name, families);
        }
    }

    /** @param e array with individuals
     * @param occupations to store the results */
    private void analyzeOccupations(Entity[] e, StatisticsOccupations occupations) {

        for(int i=0;i<e.length;i++) {
            occupations.numberIndis++;
            // an individual might have more than one occupation
            Property[] props = e[i].getProperties(new TagPath("INDI:OCCU"));
            if (props!=null) {
                for(int j=0;j<props.length;j++) {
                    String occu = props[j].getValue();
                    if(occu.length()>0)
                        occupations.occupations.add(occu, e[i]);
                }
            }
        }
    }

    /**
     * Persons with the same last name are basically a "family". Therefore this method is also
     * called from analyzeLastNames().
     *
     * @param families to store the result
     * @param lastName null for "real" families or string value for persons with a certain last name
     * @param e the families
     */
    private void analyzeFamilies(Entity[] e, String lastName, StatisticsFamilies families) {

        Delta age;

        for(int i=0;i<e.length;i++) {
            Fam fam = (Fam)e[i];

            // analyze marriage age of husband and wife
            Indi husband=fam.getHusband();
            Indi wife=fam.getWife();
            PropertyDate date = fam.getMarriageDate();

            if(date!=null) {
                if((husband!=null)&&((lastName==null)||husband.getLastName().equals(lastName))){
                    age = husband.getAge(date.getStart());
                    analyzeAge(husband, age, families.husbands, null, MARRIAGE);
                }
                if((wife!=null)&&((lastName==null)||wife.getLastName().equals(lastName))){
                    age= wife.getAge(date.getStart());
                    analyzeAge(wife, age, families.wifes, null, MARRIAGE);
                }
            }

            // analyze ages at child births
            Indi[] children = fam.getChildren();

            for(int j=0;j<children.length;j++) {
                date = children[j].getBirthDate();
                if(date!=null) {
                    if ((husband!=null)&&((lastName==null)||(husband.getLastName().equals(lastName)))) {
                        age = husband.getAge(date.getStart());
                        analyzeAge(husband, age, families.husbands, null, CHILDBIRTH);
                    }
                    if ((wife!=null)&&((lastName==null)||(wife.getLastName().equals(lastName)))) {
                        age = wife.getAge(date.getStart());
                        analyzeAge(wife, age, families.wifes, null, CHILDBIRTH);
                    }
                }
            }

            // analyze number of children
            families.children.add(new Integer(children.length), fam);

            if(children.length > 0)
                families.withChildren++;

            if(children.length>families.maxChildren)
                families.maxChildren=children.length;

            if(children.length<families.minChildren)
                families.minChildren=children.length;
        }
    }

    /**
     * @param ages all ages added up in days
     * @param numAges number of persons added up
     * @return int[] with average age (year, month, day)
     */
    private int[] calculateAverageAge(double ages, double numAges) {
        int[] age = {0, 0, 0};

        // only calculate if paramaters != default or unvalid values
        if((numAges>0)&&(ages!=Integer.MAX_VALUE)&&(ages!=Integer.MIN_VALUE)) {
            age[0] = (int)roundNumber(Math.floor(ages/360/numAges),0);
            ages = ages%(360*numAges);
            age[1] = (int)roundNumber(Math.floor(ages/30/numAges),0);
            ages = ages%(30*numAges);
            age[2] = (int)roundNumber(ages/numAges, 0);
        }
        return age;
    }

    /** Prints min., average, and max. age
     * @param stats to get the values from
     * @param printIndis 1=all, 2=min./avg./max., 3=none
     * @param indent level for indent printing
     * @param which indis to print
     */
    private void printAges(int printIndis, int indent, StatisticsIndividuals stats, int which) {

        int[] age;

        switch(which) {
            case INDIS:
            case MARRIAGE:

                if(stats.age.getKeys().size()>0) {
                    // there are indis to print
                    if(stats.age.getSize()==1) {
                        // we have one indi to print
                        Indi indi = (Indi)stats.age.getReferences((Integer)stats.age.getKeys().get(0)).iterator().next();
                        age = calculateAverageAge(stats.sumAge,1);
                        println(getIndent(indent)+new Delta(age[2], age[1], age[0])+" "+translate("oneIndi"));
                        if(printIndis<3)
                            println(getIndent(indent+1)+displayEntity(indi.getId(), indi.getName()));
                    }
                    else {
                        // we have several indis to print
                        // min. age
                        printMinMaxAge(indent, "minAge", stats.minAge, stats.age.getReferences(new Integer(stats.minAge)));
                        // average age
                        age = calculateAverageAge(stats.sumAge,stats.age.getSize());
                        println(getIndent(indent)+translate("avgAge")+" "+new Delta(age[2], age[1], age[0]));
                        // max. age
                        printMinMaxAge(indent, "maxAge", stats.maxAge, stats.age.getReferences(new Integer(stats.maxAge)));
                    }
                }
                else
                    // no indis found
                    println(getIndent(indent)+translate("noData"));
                break;
            case CHILDBIRTH:
                if(stats.childBirthAge.getKeys().size()>0) {
                    // there are indis to print
                    if(stats.childBirthAge.getSize()==1) {
                        // we have one indi to print
                        Indi indi = (Indi)stats.childBirthAge.getReferences((Integer)stats.childBirthAge.getKeys().get(0)).iterator().next();
                        age = calculateAverageAge(stats.sumChildBirthAge,1);
                        println(getIndent(indent)+new Delta(age[2], age[1], age[0])+" "+translate("oneIndi"));
                        if(printIndis<3)
                            println(getIndent(indent+1)+displayEntity(indi.getId(), indi.getName()));
                    }
                    else{
                        // we have several indis to print
                        // min. age
                        printMinMaxAge(indent, "minAge", stats.minChildBirthAge, stats.childBirthAge.getReferences(new Integer(stats.minChildBirthAge)));
                        // avg age
                        age = calculateAverageAge(stats.sumChildBirthAge,stats.childBirthNumber);
                        println(getIndent(indent)+translate("avgAge")+" "+new Delta(age[2], age[1], age[0]));
                        // max. age
                        printMinMaxAge(indent, "maxAge", stats.maxChildBirthAge, stats.childBirthAge.getReferences(new Integer(stats.maxChildBirthAge)));
                    }
                }
                else
                    // no indis found
                    println(getIndent(indent)+translate("noData"));
                break;
        }
    }

    /**
     * @param prefix e. g. "min. age:"
     * @param age to print
     * @param ages individuals with this age
     * @param indent level for indent printing
     */
    private void printMinMaxAge(int indent, String prefix, int age, Collection c) {

        int[] avg = calculateAverageAge(age,1);

        println(getIndent(indent)+translate(prefix)+" "+new Delta(avg[2], avg[1], avg[0]));
        Iterator it = c.iterator();
        while(it.hasNext()) {
            Indi indi = (Indi)it.next();
            println(getIndent(indent+1)+displayEntity(indi.getId(), indi.getName() ));
        }
    }

    /**
     * prints individuals (all, males, females, unknown gender, wifes, husbands, same last name, ...)
     * @param printIndis which indis should be printed (1=all, 2=min./max. age, 3=none)
     * @param lastName null if all indis of a gedcom file are reported or
     * a string value if indis with same last name should be reported
     * @param numberAllIndis number of inidividuals in the gedcom file (only needed when last names are reported)
     */
    private void reportIndividuals(int printIndis, String lastName, double numberAllIndis, StatisticsIndividuals all, StatisticsIndividuals males, StatisticsIndividuals females, StatisticsIndividuals unknown) {

        int indent;

        if(lastName==null) {
            println(translate("people"));
            println();
            println(getIndent(2)+translate("number",all.number));
            indent=3;
        }
        else {
            println(getIndent(2)+"\""+lastName+"\""+": "+all.number+" ("+roundNumber((double)all.number/numberAllIndis*100, OPTIONS.getPositions())+"%)");
            println(getIndent(3)+translate("ages"));
            println(getIndent(4)+translate("all"));
            indent=5;
        }

        if((lastName==null) || (all.number>0))
            printAges(printIndis, indent, all, INDIS);

        if((lastName==null) || (males.number>0)) {
            println();
            println(getIndent(indent-1)+translate("males", ""+males.number, ""+roundNumber((double)males.number/(double)all.number*100, OPTIONS.getPositions()) ));
            printAges(printIndis, indent, males, INDIS);
        }

        if((lastName==null) || (females.number>0)) {
            println();
            println(getIndent(indent-1)+translate("females", ""+females.number, ""+roundNumber((double)females.number/(double)all.number*100, OPTIONS.getPositions()) ));
            printAges(printIndis, indent, females, INDIS);
        }

        if((lastName==null) || (unknown.number>0)) {
            println();
            println(getIndent(indent-1)+translate("unknown", ""+unknown.number, ""+roundNumber((double)unknown.number/(double)all.number*100, OPTIONS.getPositions()) ));
            printAges(printIndis, indent, unknown, INDIS);
        }

        if(lastName==null) {
            println();
            println();
        }
    }

    /** print children of families
     * @param families data source for printing
     * @param childs print only families with this number of children
     * @param indent level for indent printing
     **/
    private void printChildren(StatisticsFamilies families, int childs, int indent) {
        Iterator it = families.children.getReferences(new Integer(childs)).iterator();
        while(it.hasNext()) {
            Fam fam = (Fam)it.next();
            println(getIndent(indent+2)+displayEntity(fam.getId(), fam.toString()));
        }
    }


    /** prints the output for families ("real" families or persons with same last name)
     *
     * @param families the statistic
     * @param reportIndisToMarriageAge if indis to marriage ages should be printed
     * @param reportIndisToChildBirths if indis to child births should be printed
     * @param reportFamsToChildren which families with children should be reported (1=all, 2=min./max. age, 3=none)
     * @param lastName whether we report "real" families or indis with the same last name
     **/
    private void reportFamilies(StatisticsFamilies families, int reportFamsToChildren, boolean reportIndisToChildBirths, boolean lastName) {

        int i = -1, j = -1, indent = -1;
        if(reportIndisToMarriageAge)
            i=1;
        else
            i=3;

        if(reportIndisToChildBirth)
            j=1;
        else
            j=3;

        if(lastName==false) {
            println(translate("families")+": "+families.number);
            indent = 2;
        }
        else
            indent = 3;

        if(families.number>0) {
            //ages at marriage
            println();
            println(getIndent(indent)+translate("ageAtMarriage"));
            //husbands
            println(getIndent(indent+1)+translate("husbands"));
            printAges(i, indent+2, families.husbands, MARRIAGE);
            // wifes
            println();
            println(getIndent(indent+1)+translate("wifes"));
            printAges(i, indent+2, families.wifes, MARRIAGE);

            //children
            println();
            println(getIndent(indent)+translate("withChildren", ""+families.withChildren, ""+roundNumber((double)families.withChildren/(double)families.number*100,OPTIONS.getPositions()) ));

            switch(reportFamsToChildren) {
                case 0:
                    println(getIndent(indent+1)+translate("avgChildren",""+roundNumber((double)families.withChildren/(double)families.number,OPTIONS.getPositions())));
                    Iterator f = families.children.getKeys().iterator();
                    while(f.hasNext()) {
                        int children = ((Integer)f.next()).intValue();
                        println(getIndent(indent+1)+translate("children")+": "+children);
                        printChildren(families, children, indent);
                    }
                    break;
                case 1:
                    println(getIndent(indent+1)+translate("avgChildren",""+roundNumber((double)families.withChildren/(double)families.number,OPTIONS.getPositions())));
                    println(getIndent(indent+1)+translate("minChildren",families.minChildren));
                    printChildren(families, families.minChildren, indent);
                    println(getIndent(indent+1)+translate("maxChildren",families.maxChildren));
                    printChildren(families, families.maxChildren, indent);
                    break;
                case 2:
                    println(getIndent(indent+1)+translate("minChildren",families.minChildren));
                    println(getIndent(indent+1)+translate("avgChildren",""+roundNumber((double)families.withChildren/(double)families.number,OPTIONS.getPositions())));
                    println(getIndent(indent+1)+translate("maxChildren",families.maxChildren));
                    break;
            }

            //ages at child birth
            println();
            println(getIndent(indent)+translate("agesAtChildBirths"));
            //husbands
            println();
            println(getIndent(indent+1)+translate("husbands"));
            printAges(j, indent+2, families.husbands, CHILDBIRTH);
            //wifes
            println();
            println(getIndent(indent+1)+translate("wifes"));
            printAges(j, indent+2, families.wifes, CHILDBIRTH);
        }

        if(lastName==false) {
            println();
            println();
        }
    }

    /** print the output for playes
     *
     * @param reportIndisToPlaces if indis to places should be reported
     * @param sortPlacesByName if places should be sorted by name
     * @param places our statistic
     */
    private void reportPlaces(boolean reportIndisToPlaces, Comparator sort, StatisticsPlaces places) {

        Iterator p = places.places.getKeys(sort).iterator();
        while(p.hasNext()) {
            String place = (String)p.next();
            int number = places.places.getSize(place);
            println(getIndent(2)+place+": "+number+" ("+roundNumber((double)number/(double)places.entitiesWithKnownPlaces*100, OPTIONS.getPositions())+"%)");
            if(reportIndisToPlaces) {
                Iterator entities = places.places.getReferences(place).iterator();
                while(entities.hasNext()) {
                    if(places.which==MARRIAGE) {
                        Fam fam = (Fam)entities.next();
                        println(getIndent(3)+displayEntity(fam.getId(), fam.toString()));
                    }
                    else {
                        Indi indi = (Indi)entities.next();
                        println(getIndent(3)+displayEntity(indi.getId(), indi.getName()));
                    }
                }
            }
        }
        println();
        println();
    }
    /** print info about indis with the same last name. this method calls reportIndividuals() and reportFamilies().
     *
     * @param lastNames statistical data
     * @param numberAllIndis number of indis in gedcom file
     */
    private void reportLastNames(StatisticsLastNames lastNames, Comparator sort, int numberAllIndis) {

        println();
        println(translate("lastNames", ""+lastNames.lastNamesIndis.getKeys().size(), ""+numberAllIndis ));
        Iterator it = lastNames.lastNamesIndis.getKeys(sort).iterator();
        while(it.hasNext()) {
            String name = (String)it.next();
            StatisticsIndividuals all=null, males=null, females=null, unknown=null;
            StatisticsFamilies families = null;
            Iterator stats = lastNames.lastNamesStatistic.getReferences(name).iterator();
            while(stats.hasNext()) {
                Object stat = stats.next();
                if(stat instanceof StatisticsIndividuals) {
                    switch(((StatisticsIndividuals)stat).which) {
                        case ALL:
                            all=(StatisticsIndividuals)stat;
                            break;
                        case MALES:
                            males=(StatisticsIndividuals)stat;
                            break;
                        case FEMALES:
                            females=(StatisticsIndividuals)stat;
                            break;
                        case UNKNOWN:
                            unknown=(StatisticsIndividuals)stat;
                            break;
                    }
                }
                else
                    families = (StatisticsFamilies)stat;
            }
            int i;
            if(reportAgeToLastNames)
                i=1;
            else
                i=3;
            reportIndividuals(i, name, numberAllIndis, all, males, females, unknown);
            reportFamilies(families, reportFamsToChildren, reportIndisToChildBirth, true);
        }
    }

    /** print info about occupations
     *
     * @param occupations statistic with data
     */
    private void reportOccupations(StatisticsOccupations occupations, Comparator sort) {

        println(translate("occupations"));
        println(getIndent(2)+translate("number", occupations.occupations.getKeys().size()));
        Iterator it = occupations.occupations.getKeys(sort).iterator();
        while(it.hasNext()) {
            String occupation = (String)it.next();
            println(getIndent(3)+translate("occupation", occupation, ""+occupations.occupations.getSize(occupation), ""+roundNumber((double)occupations.occupations.getSize(occupation)/(double)occupations.occupations.getSize()*100, OPTIONS.getPositions()) ));
            if(reportIndisToOccupations) {
                Iterator indis = occupations.occupations.getReferences(occupation).iterator();
                while(indis.hasNext()) {
                    Indi indi = (Indi)indis.next();
                    println(getIndent(4)+displayEntity(indi.getId(), indi.getName()));
                }
            }
        }
        println();
        println();
    }

    private String displayEntity(String id, String name) {
        return "("+id+")\t" + name;
    }

    
    
} //ReportGedcomStatistics