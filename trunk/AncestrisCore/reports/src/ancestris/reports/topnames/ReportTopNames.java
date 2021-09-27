package ancestris.reports.topnames;
import java.util.ArrayList;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 */
@ServiceProvider(service=Report.class)
public class ReportTopNames extends Report {
	 
	public boolean showAllNames = true;
	
	//translate strings for output  
	private String textTitle = translate("title");
	private String textFileName = translate("filename");
	private String textDate = translate("date");
	private String textGivenNames = translate("givennames");
	private String textSurnames = translate("surnames");
	

	
	
	//entry point
	public void start(Gedcom gedcom) {
		  	
		//show report header
		displayHeader(gedcom.getName());
		outputReport(gedcom);
		 
	}
	
	  
	
	//display report header
	public void displayHeader(String strSubject) {
  
		  int loop;
		  String strULine="";
			
		  //print report title
		  println(textTitle);
		  for(loop=0 ;loop<textTitle.length(); loop++)
			  strULine += "=";
		  println(strULine);
		  println(); 
		  println(textFileName + ": " + strSubject);
		  println(textDate + ": " + PointInTime.getNow().toString());
		  println();
		  
	}	
	
	
	
	//output the report
	public void outputReport(Gedcom gedcom) {
  
		class objRec{
			//constructor
			public objRec(String str, int i) {
				strName = str;
				count = i;
			}
			//method
			public void inccounter() {
				count +=1;	
			}
			//fields
			String strName;
			int count;	
		}
		
		//variables
		ArrayList<objRec> alGiven = new ArrayList<objRec>();
		ArrayList<objRec> alSurname = new ArrayList<objRec>();
		objRec objTemp;
		Indi person;
		Entity[] individuals;
		int loop, loop2, firstSpace, position, maxNames, numIndis;
		String strGiven, strSurname, strOutput, strULine;
		boolean flagFound, flagSwapped;
		float percent;
	
		//grab all
		individuals = gedcom.getEntities(Gedcom.INDI,"");
		//number in file
		numIndis = individuals.length;
		
		for(loop=0; loop<individuals.length; loop++) {
	        
			//report on each
			person = (Indi)individuals[loop];      
			strGiven = person.getFirstName();
			strSurname = person.getLastName();
			
			//consider only very first name i.e. not middle names or initials
			firstSpace = strGiven.indexOf(" ");
			if(firstSpace !=-1) strGiven = strGiven.substring(0, firstSpace);


			//given name:
			//if this is the first object, add it to the list
			//otherwise iterate list for name match
			if(alGiven.isEmpty()) {
				objTemp = new objRec(strGiven, 1);
				alGiven.add(objTemp);
			} else {
	 
				flagFound=false;
				
				for(loop2=0; loop2<alGiven.size(); loop2++) {
					//if already in list update count
					if(alGiven.get(loop2).strName.equals(strGiven)) {
						alGiven.get(loop2).inccounter();
						flagFound = true;
					}
				}//loop2
				
				//if not found in list
				if(!flagFound) {
					//create a new record with a count of 1
					objTemp = new objRec(strGiven, 1);
					alGiven.add(objTemp);
				}
	
			}//else
		        	

			//surname:
			//if this is the first object, add it to the list
			//otherwise iterate list for name match
			if(alSurname.isEmpty()) {
				objTemp = new objRec(strSurname, 1);
				alSurname.add(objTemp);
			} else {
	 
				flagFound=false;
				
				for(loop2=0; loop2<alSurname.size(); loop2++) {
					//if already in list update count
					if(alSurname.get(loop2).strName.equals(strSurname)) {
						alSurname.get(loop2).inccounter();
						flagFound = true;
					}
				}//loop2
				
				//if not found in list
				if(!flagFound) {
					//create a new record with a count of 1
					objTemp = new objRec(strSurname, 1);
					alSurname.add(objTemp);
				}
		
			}//else
			
	    }//loop				
			
	
		//sort given name list on count
		do { 
			flagSwapped = false;
			for(loop=1; loop<alGiven.size(); loop++) {
				if((alGiven.get(loop).count > alGiven.get(loop-1).count)) {
					//bubble up
					//save obj higher in list
					objTemp = alGiven.get(loop-1);
					//swap
					alGiven.set(loop-1, alGiven.get(loop));
					alGiven.set(loop, objTemp);
					//note swap
					flagSwapped = true;
					//break out
					//break;
				}
			}
		}while(flagSwapped);
		
		//sort surname
		do { 
			flagSwapped = false;
			for(loop=1; loop<alSurname.size(); loop++) {
				if((alSurname.get(loop).count > alSurname.get(loop-1).count)) {
					//bubble up
					//save obj higher in list
					objTemp = alSurname.get(loop-1);
					//swap
					alSurname.set(loop-1, alSurname.get(loop));
					alSurname.set(loop, objTemp);
					//note swap
					flagSwapped = true;
					//break;
				}
			}
		}while(flagSwapped);		
		
		
		
		//display given names
		//label
		println(textGivenNames);
		strULine="";
		for(loop=0 ;loop<textGivenNames.length(); loop++)
		  strULine += "-";
		println(strULine);
		
		if(showAllNames) maxNames = alGiven.size();
		else {
			if(alGiven.size() < 20) maxNames = alGiven.size(); 
				else maxNames = 20;	
		}
		for(loop=0; loop<maxNames; loop++) {
			percent = (float)alGiven.get(loop).count/numIndis*100;
			//long ugly statement...
			println(align(alGiven.get(loop).strName, 20, 3) + align(Integer.toString(alGiven.get(loop).count),6,2) + "  -  " + Float.toString(percent).substring(0,4) + "%") ;					
		}
		
		//display surnames
		//label
		println();
		println(textSurnames);
		strULine="";
		for(loop=0 ;loop<textSurnames.length(); loop++)
			  strULine += "-";
		println(strULine);		

		if(showAllNames) maxNames = alSurname.size();
		else {
			if(alSurname.size() < 20) maxNames = alSurname.size(); 
				else maxNames = 20;	
		}		
		for(loop=0; loop<maxNames; loop++) {
			percent = (float)alSurname.get(loop).count/numIndis*100;
			println(align(alSurname.get(loop).strName, 20, 3) + align(Integer.toString(alSurname.get(loop).count),6,2) + "  -  " + Float.toString(percent).substring(0,4) + "%") ;					
		}		
		
	}
}
 