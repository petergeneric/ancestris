import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.TagPath;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

/**


*/
public class ReportContemporaries extends Report {
	 
	// check all relevant tags by default	
	public int optionLifeSpan = 70;
  
	//translate strings for output  
	private String textTitle = translate("title");
	private String textDate = translate("date"); 
	private String textSelect = translate("select");
	private String textNothing = translate("nothing");
	private String textNoDates = translate("nodates");
	private String textContemps = translate("contemps");
	
  public void start(Indi indi) {
	  
	  //running for current person - no selection required
	  
	  //show report header
	  displayHeader();
	  
	  //do report
	  doReport(indi);  
  }
    
  
  public void start(Gedcom gedcom) {
	  
	  Entity ent;
	  
	  //variables
	  Indi person;
	  
	  //choose person to report on
	  ent = getEntityFromUser(textSelect + " ", gedcom, "INDI");
	  if(ent==null) return;
	  
	  person = (Indi)ent;
	  
	  //show report header
	  displayHeader();
	  
	  //do the report
	  doReport(person);	 
  }
    
    
  public void doReport(Indi indi) {
	
	  int subjectDOBYear, subjectDODYear, loop;
	  int tempDOBYear, tempDODYear;
	  Entity[] individuals;
	  String strSpan="";
	  
	  //subjects life span  
	  subjectDOBYear = getYear(indi.getBirthDate());
	  subjectDODYear = getYear(indi.getDeathDate());

	  //no birth or death year?
	  if((subjectDOBYear ==-1) && (subjectDODYear ==-1)) { 
		  println(textNothing);
		  println(textNoDates);
		  return;	  
	  } 
	  
	  //no birth year?
	  if(subjectDOBYear ==-1) {
		  subjectDOBYear = subjectDODYear - optionLifeSpan;
		  strSpan=subjectDOBYear + "(est) - ";
	  }
	  else strSpan=subjectDOBYear + " - ";

	  //no death year?
	  if(subjectDODYear ==-1) {
		  subjectDODYear = subjectDOBYear + optionLifeSpan;
		  //if calculated date is in the future leave blank!
		  if (subjectDODYear < PointInTime.getNow().getYear()) 
			  strSpan = strSpan + subjectDODYear + "(est)";
	  }
	  else strSpan= strSpan + subjectDODYear;	  
	  
	  //display
	  println(textContemps + " " + indi.getName() + " " + strSpan);
	  println();
	  
	  //list anyone inn the file who was born or died during the life span of the subject
	  individuals = indi.getGedcom().getEntities(Gedcom.INDI,"");
      
	  for(loop=0; loop<individuals.length; loop++) {	  
	  
		  //don't check self ;)
		  if((Indi)(individuals[loop]) != indi ) {

		  	  //get birth
			  tempDOBYear = getYear(((Indi)individuals[loop]).getBirthDate());
			  //get death
			  tempDODYear = getYear(((Indi)individuals[loop]).getDeathDate());
			  
			  //no birth or death year?
			  if((tempDOBYear ==-1) && (tempDODYear ==-1)) { 
				  //println("No suitable birth/death dates for indi.");
			  } else {
	  
				  //no birth year?
				  if(tempDOBYear ==-1) {
					  tempDOBYear = tempDODYear - optionLifeSpan;
					  strSpan=tempDOBYear + "(est) - ";
				  }
				  else strSpan=tempDOBYear + " - ";
		  
				  //no death year?
				  if(tempDODYear ==-1) {
					  tempDODYear = tempDOBYear + optionLifeSpan;
					  //if calculated date is in the future leave blank!
					  if (tempDODYear < PointInTime.getNow().getYear()) 
						  strSpan = strSpan + tempDODYear + "(est)";
				  }
				  else strSpan= strSpan + tempDODYear;	  
				  		  
				  //if died before subject born or born after subject died non-contemp
				  if( (tempDODYear < subjectDOBYear) || (tempDOBYear > subjectDODYear) ) {
					 
				  }
				  else {
					  strSpan = ((Indi)(individuals[loop])).getName() + " " + strSpan; 
					  println(strSpan);
				  }
			  
			  }
	  	}  
	  }// end for loop
  }
  
    
  public int getYear(PropertyDate someDate) {
	  
 	  String strYear;
	  
	  //check for null, invalid or range-type birth date
	  if ((someDate==null) || (!someDate.isValid()) || (someDate.isRange())) 
		  return -1;
	  
	  //get year of time of birth
	  strYear = (someDate.getDisplayValue().trim());
	  strYear = strYear.substring(strYear.length()-4);
	  return Integer.parseInt(strYear);
  }
  
    
  public void displayHeader() {  
	  //print report title
	  println(align(textTitle, 80, 1));
	  println(textDate + ": " + PointInTime.getNow().toString());	 
	  println();
  }
} 