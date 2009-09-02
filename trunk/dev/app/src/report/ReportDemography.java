/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.report.Report;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** A report showing age distribution for males/females. */
public class ReportDemography extends Report {

  /** The maximum age stored in the series. */
  private static final int MAX_AGE = 100;
  
  /** The number of ages stored in a single group. */
  private int ageGroupSize = 10;

  /** The labels shown on the category axis. */
  private String[] categories;

  /** Accessor - age grouping */
  public int getAgeGroupSize() {
    return ageGroupSize;
  }

  /** Accessor - age grouping */
  public void setAgeGroupSize(int set) {
    ageGroupSize = Math.max(1, Math.min(25, set));
  }

  /** n/a */
  public boolean usesStandardOut() {
    return false;
  }

  /** main */
  public void start(Gedcom gedcom) throws Exception {

    createCategories();
    String labelForMen = translate("men");
    String labelForWomen = translate("women");
    String labelForFathers = translate("fathers");
    String labelForMothers = translate("mothers");
    String diagramTitle = translate("title", gedcom.getName());

    IndiAnalyzer[] indiAnalyzers = {
        //------------------------------------------------------
        new IndiAnalyzer(labelForMen, labelForWomen, PropertyAge.getLabelForAge()){

          public void addFact(Indi indi) {
            try { addAge(indi, indi.getDeathDate()); } catch (RuntimeException e) {}
          }
        },

        //------------------------------------------------------
        new IndiAnalyzer(labelForMen, labelForWomen, translate("ageAtFirstMariage")){

          public void addFact(Indi indi) {
            try { addAge(indi, indi.getFamiliesWhereSpouse()[0].getMarriageDate()); } catch (RuntimeException e) {}
          }
        },

        //------------------------------------------------------
        new IndiAnalyzer(labelForFathers, labelForMothers, translate("ageAtParentsDeath")){
          
          public void addFact(Indi indi) {
            try { addAgeForMale  ( indi, indi.getBiologicalFather().getDeathDate() ); } catch (RuntimeException e) {}
            try { addAgeForFemale( indi, indi.getBiologicalMother().getDeathDate() ); } catch (RuntimeException e) {}
          }
        },
        
        //------------------------------------------------------
        new IndiAnalyzer(labelForFathers, labelForMothers, translate("ageAtChildsBirth")){

          public void addFact(Indi indi) {
            try { 
              PropertyDate birthDate = indi.getBirthDate();
              try { addAgeForMale  ( indi.getBiologicalFather(), birthDate ); } catch (RuntimeException e) {}
              addAgeForFemale( indi.getBiologicalMother(), birthDate );
            }
            catch (RuntimeException e) {}
          }},

        //------------------------------------------------------
        new IndiAnalyzer(labelForFathers, labelForMothers, translate("ageOfYoungestChildLeftBehind")){

          public void addFact(Indi indi) {
            try {
              // catch early if this fact missing to prevent needles further actions 
              PropertyDate deathDate = indi.getDeathDate();  
              
              // find youngest child
              Fam[] fams = indi.getFamiliesWhereSpouse();
              Fam fam = fams[fams.length - 1 ];
              Indi[] children = fam.getChildren();
              Indi child = children[children.length - 1]; // NB: ignored whether deceased

              // add the child's age when its parent died
              // it may be the first parent or the second parent
              if ( indi.getSex() == PropertySex.MALE )
                  addAgeForMale(child, deathDate);
                else 
                  addAgeForFemale(child, deathDate);
            }
            catch (RuntimeException e) {}
          }
        },
        //------------------------------------------------------
        new IndiAnalyzer(labelForMen, labelForWomen, translate("yearsSinceDeathOfSpouse")){
          
          public void addFact(Indi indi) {
            try {
              Indi[] partners = indi.getPartners();
              for ( int i=0 ; i<partners.length ; i++ )
                try { addRemainingYears(indi, partners[i].getDeathDate()); } catch (RuntimeException e) {}
            }
            catch (RuntimeException e) {}
          }
        }
        //------------------------------------------------------
    };

    FamAnalyzer[] famAnalyzers = {
        
        //------------------------------------------------------
        new FamAnalyzer(labelForMen, labelForWomen, translate("ageAtDivorce")){
          
          public void addFact(Fam fam) {
            try {
              PropertyDate divorce = fam.getDivorceDate();
              try { addAgeForMale(fam.getHusband(), divorce); } catch (RuntimeException e) {}
              addAgeForFemale(fam.getWife(), divorce);
            }
            catch (RuntimeException e) {}
          }
        },
        
        //------------------------------------------------------
        new FamAnalyzer(labelForMen, labelForWomen, translate("ageOfOldestWhenYoungestWasBorn")){
          
          public void addFact(Fam fam) {
            try { 
              Indi[] children = fam.getChildren();
              if ( 1 >= children.length ) return; // skip children without siblings 
              Indi youngest = children[children.length - 1];  // NB: ignored whether deceased
              Indi oldest = children[0];  // NB: ignored whether deceased

              addAge(oldest, youngest.getBirthDate());
            }
            catch (RuntimeException e) {}
          }
        },

        //------------------------------------------------------
        new FamAnalyzer(labelForFathers, labelForMothers, translate("ageOfYoungestOrphan")){
          
          public void addFact(Fam fam) {
            try {
              Indi[] children = fam.getChildren();
              Indi youngest = children[children.length - 1]; // NB: ignored whether deceased
              
              PropertyDate father = fam.getHusband().getDeathDate();
              PropertyDate mother = fam.getWife().getDeathDate();
              if ( father.getStart().getYear() > mother.getStart().getYear() ) 
                addAgeForMale(youngest, father);
              else  
                addAgeForFemale(youngest, mother);
            }
            catch (RuntimeException e) {}
          }
        }
        //------------------------------------------------------
    };

    gatherData( gedcom,       indiAnalyzers, famAnalyzers);
    showData  ( diagramTitle, indiAnalyzers, famAnalyzers);
  }

  /** Show the gathered data to the user.
   * 
   * @param title title of the diagram
   * @param indiAnalyzers data collected when looping over individuals
   * @param famAnalyzers data collected when looping over families
   */
  private void showData(
      String title, 
      IndiAnalyzer[] indiAnalyzers,
      FamAnalyzer[] famAnalyzers) {
    
    JTabbedPane charts = new JTabbedPane();
    for ( int i=0 ; i<indiAnalyzers.length ; i++ ) {
      charts.addTab( indiAnalyzers[i].getAgeLabel(), indiAnalyzers[i].createChart (title) );
    }
    for ( int i=0 ; i<famAnalyzers.length ; i++ ) {
      charts.addTab( famAnalyzers[i].getAgeLabel(), famAnalyzers[i].createChart (title) );
    }
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.CENTER, charts);
    showComponentToUser(panel);
  }

  /** Gather data by looping over individuals and families.
   * 
   * @param gedcom the database
   * @param indiAnalyzers 
   * @param famAnalyzers
   */
  private void gatherData(
      Gedcom gedcom, 
      IndiAnalyzer[] indiAnalyzers,
      FamAnalyzer[] famAnalyzers) {
    
    // loop over each individual in GedCom
    Iterator indis = gedcom.getEntities( Gedcom.INDI ).iterator();
    while ( indis.hasNext() ) {
      Indi indi = (Indi) indis.next();
      for ( int i=0 ; i<indiAnalyzers.length ; i++ ) {
        indiAnalyzers[i].addFact( indi );
      }
    }

    // loop over each family in GedCom
    Iterator fams = gedcom.getEntities( Gedcom.FAM ).iterator();
    while ( fams.hasNext() ) {
      Fam fam = (Fam) fams.next();
      for ( int i=0 ; i<famAnalyzers.length ; i++ ) {
        famAnalyzers[i].addFact( fam );
      }
    }
  }

  private abstract class FamAnalyzer  extends Analyzer {
    
    /** Creates an analyzer on age distributions for males and females 
     * for a certain type of facts for families.
     * 
     * @param maleLabel label on X-axis for male series
     * @param femaleLabel label on X-axis for female series
     * @param ageLabel label for Y-axis
     */
    public FamAnalyzer (String maleLabel, String femaleLabel, String ageLabel) {
      super(maleLabel, femaleLabel, ageLabel);
    }    

    /** Adds a fact of a family member to the male and/or female series.
     * Called by {gatherData} for each family in the GedCom. 
     * Events or dates might not be available, 
     * so catch RuntimeExceptions to skip these families.
     * 
     * @param fam family for which the fact is calculated
     */
    public abstract void addFact (Fam fam);
  }

  private abstract class IndiAnalyzer  extends Analyzer {
    
    /** Creates an analyzer on age distributions for males and females 
     * for a certain type of facts for individuals.
     * 
     * @param maleLabel label on X-axis for male series
     * @param femaleLabel label on X-axis for female series
     * @param ageLabel label for Y-axis
     */
    public IndiAnalyzer (String maleLabel, String femaleLabel, String ageLabel) {
      super(maleLabel, femaleLabel, ageLabel);
    }    
    
    /** Adds a fact to the male and/or female series.
     * Called by {gatherData} for each individual in the GedCom. 
     * Events or dates might not be available, 
     * so catch RuntimeExceptions to skip these individuals.
     * 
     * @param indi individual for which the fact is calculated
     *             a fact may be its own age at an event, or the age of a relative
     */
    public abstract void addFact (Indi indi);
  }
  
  /** Creates the categories shown on the chart. */
  private void createCategories() {
    categories = new String[MAX_AGE/ageGroupSize + 1];
    categories[0] = MAX_AGE + "+";
    for (int i=1;i<categories.length;i++) {
      if (ageGroupSize<5 && i%Math.ceil(5F/ageGroupSize)!=0)
        categories[i] = "";
      else
        categories[i] = (MAX_AGE - (i*ageGroupSize)) + "+";
    }
  }


  /** Analyzes how ages are distributed for males and females */
  private abstract class Analyzer {

    private IndexedSeries males;
    private IndexedSeries females;
    private String ageLabel;

    /** Creates an analyzer on age distributions for males and females 
     * for a certain type of facts.
     * 
     * @param maleLabel label on X-axis for male series
     * @param femaleLabel label on X-axis for female series
     * @param ageLabel label for Y-axis
     */
    public Analyzer (String maleLabel, String femaleLabel, String ageLabel) {

      this.ageLabel = ageLabel;
      males = new IndexedSeries(maleLabel, categories.length);
      females = new IndexedSeries(femaleLabel, categories.length);
    }

    /** Creates a chart from the series.
     * 
     * @param title title for the chart
     * @return a chart with age as Y-axis and counts for male and female on the X-axis
     */
    public Chart createChart (String title) {
      // + we're using a custom format so that the male series' negative
      //   values show up as a positive ones.
      // + isStacked makes sure the bars for the series are stacked instead
      //   of being side by side
      // + isVertical makes the main axis for the categories go from top
      //   to bottom
      IndexedSeries[] nestedSeries = new IndexedSeries[]{ males, females};
      DecimalFormat decimalFormat = new DecimalFormat("#; #");
      return new Chart(title, ageLabel, nestedSeries, categories, decimalFormat, true, true);
    }

    /** Calculates the group in the series of categories.
     * 
     * @param firstEvent
     * @param lastEvent
     * @return years between {firstEvent} and {lastEvent} divided by {ageGroupSize}
     */
    private int calculateGroup(PropertyDate firstEvent, PropertyDate lastEvent) {
      
      if ( ! firstEvent.isValid() || ! lastEvent.isValid() )
        throw new IllegalArgumentException();

      try {
        if ( firstEvent.getStart().getJulianDay() > lastEvent.getStart().getJulianDay() )
          throw new IllegalArgumentException();
      } catch (GedcomException e) {
        throw new IllegalArgumentException();
      }
        
      int years = Delta.get( firstEvent.getStart(), lastEvent.getStart() ).getYears();
      return years>=MAX_AGE ? 0 : (MAX_AGE-years-1)/ageGroupSize + 1;
    }

    /** Adds the age of {individual} at {event} to the male series.
     * 
     * @param indi the individual for which to calculate the age
     * @param event the date of some event
     */
    protected void addAgeForMale( Indi individual, PropertyDate event ) {
       // For the male series we decrease the number of individuals.
       // That's how we get the male bars on the left of the axis.
      males.dec( calculateGroup( individual.getBirthDate(), event) );
    }
    
    /** Adds the age of {individual} at {event} to the female series.
     * 
     * @param individual the individual for which to calculate the age
     * @param event the date of some event
     */
    protected void addAgeForFemale( Indi individual, PropertyDate event ) {
       // For the female series we increase the number of individuals. 
       // That's how we get the females on the right of the axis.
      females.inc( calculateGroup( individual.getBirthDate(), event) );
    }
    
    /** Adds the age of {individual} at {event} to the series of {individual}'s sex.
     * 
     * @param individual the individual for which to calculate the age
     * @param event the date of some event
     */
    protected void addAge( Indi individual, PropertyDate event ) {
      if (individual.getSex() == PropertySex.MALE)
        addAgeForMale(individual, event);
      else
        addAgeForFemale(individual, event);
    }
    
    /** Adds the years that {individual} lived after {event} to the series of {individual}'s sex.
     * 
     * @param individual the individual for which to calculate the remaining years
     * @param event the date of some event
     */
    protected void addRemainingYears( Indi individual, PropertyDate event ) {
      int group = calculateGroup( event, individual.getDeathDate());
      // for the male series we decrease the number of individuals
      // and for females we increase. That's how we get the male
      // bars on the left and the females on the right of the axis.
      if (individual.getSex() == PropertySex.MALE)
        males.dec( group);
      else
        females.inc( group);
    }
    
    public String getAgeLabel() {
      return ageLabel;
    }
  }
} 
