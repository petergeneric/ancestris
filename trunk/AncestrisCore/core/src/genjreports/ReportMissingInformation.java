package genjreports;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

/**

 


*/
public class ReportMissingInformation extends Report {
	 
	// check all relevant tags by default	
	public boolean checkBirthDate = true;
	public boolean checkBirthPlace = true;
	public boolean checkBaptismDate = true;
	public boolean checkBaptismPlace = true;
	public boolean checkDeathDate = true;
	public boolean checkDeathPlace = true;
	public boolean checkSex = true;
	public boolean checkGiven = true;
	public boolean checkSurname = true;
  
	//translate strings for output  
	private String textTitle = translate("title");
	private String textSubject = translate("subject"); 
	private String textBirth = translate("birth");
	private String textBaptism = translate("baptism");
	private String textDeath = translate("death");
	private String textDate = translate("date");
	private String textPlace = translate("place");
	private String textSex = translate("sex");
	private String textGiven = translate("given");
	private String textSurname = translate("surname");
	private String textKey = translate("key");

	
	//column widths etc
	private int colName = 30;
	private int colData = 6;
	private int numDataCols = 9;
  
  public void start(Indi indi) {
	  
   	 
	  //show column headers
	  displayHeader(indi.getName());
	  //do report
	  checkIndi(indi);
	  
  }
   
  public void checkIndi(Indi indi) {
  
		
	//vars
	PropertyDate tempDate;
	PropertyPlace tempPlace;
	String strDataRow;
	String strNameID;
	Boolean flagOk1, flagOk2;

	
    //clear any previous data and align
	strNameID = indi.getName() + " " + indi.getId();
    strDataRow = align(strNameID, colName, 3); 

    //NOTE: the order of the following tests corresponds with the display column order
    
 	//check birth date if required
	if(checkBirthDate) {		
		//read date of birth for validity checking
		tempDate = indi.getBirthDate();
		if((tempDate == null) || (!tempDate.isValid())) {
			strDataRow = strDataRow + align("X",colData,1);
		} else { 
			strDataRow = strDataRow + align("ok",colData,1);	
		}
	} else {
		strDataRow = strDataRow + align("-",colData,1);	// not checked
	}
	
	//check place of birth if required
	if(checkBirthPlace) {
		tempPlace = (PropertyPlace)indi.getProperty(new TagPath("INDI:BIRT:PLAC"));
		if((tempPlace == null)){
			strDataRow = strDataRow + align("X",colData,1);
		}else {
			strDataRow = strDataRow + align("ok",colData,1);	
		}
		
	} else {
		strDataRow = strDataRow + align("-",colData,1);	// not checked
	}
	
	//check baptism and christening date if required
	if(checkBaptismDate) {
		//reset flags
		flagOk1 = true;
		flagOk2 = true;
		
		// bapm date...
		tempDate = (PropertyDate)indi.getProperty(new TagPath("INDI:BAPM:DATE"));		
		if((tempDate == null) || (!tempDate.isValid())) {
			flagOk1 = false;
		}
		//now do chr tag
		tempDate = (PropertyDate)indi.getProperty(new TagPath("INDI:CHR:DATE"));
		if((tempDate == null) || (!tempDate.isValid())) {
			flagOk2 =false;
		}	
	
		//if date found on either tag, flag is true
		if(flagOk1 || flagOk2) {
			strDataRow = strDataRow + align("ok",colData,1);
		} else {
		strDataRow = strDataRow + align("X",colData,1);
			
		}
		
	}
	else {
		strDataRow = strDataRow + align("-",colData,1);
	}
	
	
	//baptism place
	if(checkBaptismPlace) {
		
		flagOk1 = true;
		flagOk2 = true;
		
		//check <bapt> 
		tempPlace = (PropertyPlace)indi.getProperty(new TagPath("INDI:BAPM:PLAC"));
		//tempPlace2 = (PropertyPlace)indi.getProperty(new TagPath("INDI:CHR:PLAC"));
		if((tempPlace == null) || (tempPlace.getValue() == "")) {
			flagOk1 = false;
		}

		//check <chr> 
		tempPlace = (PropertyPlace)indi.getProperty(new TagPath("INDI:CHR:PLAC"));
		if((tempPlace == null) || (tempPlace.getValue().length()==0)) {
			flagOk2 = false;
		}		
			
		if(flagOk1 || flagOk2) {	
			strDataRow = strDataRow + align("ok",colData,1);
		} else { 
			strDataRow = strDataRow + align("X",colData,1);	
		}		
	}
	else {
		strDataRow = strDataRow + align("-",colData,1);
	}
	
	
	
	
	//check death date if required
	if(checkDeathDate) {
		
		//reset flags
		
		tempDate = indi.getDeathDate();
		if((indi.getDeathDate() == null) || (!tempDate.isValid())) {
			strDataRow = strDataRow + align("X",colData,1);
		} else {
		strDataRow = strDataRow + align("ok",colData,1);
		}
	} else {
		strDataRow = strDataRow + align("-",colData,1);	
	}
	
	//check place of death if required
	if(checkDeathPlace) {
		tempPlace = (PropertyPlace)indi.getProperty(new TagPath("INDI:DEAT:PLAC"));
		if((tempPlace == null)){
			strDataRow = strDataRow + align("X",colData,1);
		}else {
			strDataRow = strDataRow + align("ok",colData,1);	
		}
		
	} else {
		strDataRow = strDataRow + align("-",colData,1);	// not checked
	}	

	//check gender if required
	if(checkSex) {
		if((indi.getSex() != PropertySex.MALE) && (indi.getSex() != PropertySex.FEMALE)) { 
			strDataRow = strDataRow + align("X",colData,1);
		} else {	
		strDataRow = strDataRow + align("ok",colData,1);
		}
	} else {
		strDataRow = strDataRow + align("-",colData,1);	
	}
	

	//check given/firstname
	// uses extraction from <name> rather than checking <GIVN>
	if(checkGiven) {
		if(indi.getFirstName() == "") {
			strDataRow = strDataRow + align("X",colData,1);
	} else {
		strDataRow = strDataRow + align("ok",colData,1);
	}
	} else {
		strDataRow = strDataRow + align("-",colData,1);	
	}
	
	
	//check surname/family name
	// uses extraction from <name> rather than checking <SURN>
	if(checkSurname) {
		if(indi.getLastName() == "") {
			strDataRow = strDataRow + align("X",colData,1);
	} else {
		strDataRow = strDataRow + align("ok",colData,1);
	} 
	} else {
		strDataRow = strDataRow + align("-",colData,1);	
	}
	
	//display results
	println(strDataRow);
  }
 
  
  
  
  
  
  public void start(Gedcom gedcom) {
	  
	  //variables
	  Entity[] individuals;
	  int loop;
	  Indi person;

	    
	  //show report header
	  displayHeader(gedcom.getName());
	
	  //grab all
	  individuals = gedcom.getEntities(Gedcom.INDI,"");
      
	  for(loop=0; loop<individuals.length; loop++) {
        
      	//report on each
		person = (Indi)individuals[loop];      
      	checkIndi(person);
        	
      }//for loop
	 
  }
  
  

  
  public void displayHeader(String strSubject) {
  
	  String strColHeader1, strColHeader2;
	  String strUnderLine;
	  int loop;	  
	  
	  //print report title
	  println(align(textTitle, (colName + numDataCols*colData), 1));
	  println();
	  
	  println(textSubject + ": " + strSubject);
	  println(textDate + ": " + PointInTime.getNow().toString());
	  println(textKey);
	  println();
	   
	  strUnderLine = "-";
	  for(loop=1; loop<(colName+numDataCols*colData)-1; loop++)
		  strUnderLine += "-";
	  
	  //create column header labels
	  strColHeader1 = align(" ", colName, 1)
	  				+ align(textBirth, colData, 1)
	  				+ align(textBirth, colData, 1)
	  				+ align(textBaptism, colData, 1)
	  				+ align(textBaptism, colData, 1)	  				
	  				+ align(textDeath, colData, 1)
	  				+ align(textDeath, colData, 1);
	  
	  
	  strColHeader2 = align(" ", colName, 1)
	  				+ align(textDate, colData, 1)
	  				+ align(textPlace, colData, 1)
	    			+ align(textDate, colData, 1)
	    			+ align(textPlace, colData, 1)
	    			+ align(textDate, colData, 1)
	    			+ align(textPlace, colData, 1)	    			
	    			+ align(textSex,colData, 1) 
	    			+ align(textGiven,colData,1) 
	    			+ align(textSurname,colData,1);
 
	  //display
	  println(strColHeader1);
	  println(strColHeader2);
	  println(strUnderLine);
  
  }
} 