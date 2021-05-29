package ancestris.reports;
import genj.gedcom.Indi;
import genj.report.Report;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 */ 

@ServiceProvider(service=Report.class)
public class ReportLinesAscent extends Report {
		 
	//set all true by default	
	public boolean optionMaternal = true;
	public boolean optionPaternal = true;
	
	//translation strings for output  
	private String textMaternal = translate("maternalline");
	private String textPaternal = translate("paternalline");
	private String textTitle = translate("title"); 	


  public void start(Indi indi) {

	  Indi person;
	  int iGen=1;
	  String strTemp, strULine;
	  int iLoop;
	  
	  //why did they run it?!
	  if(!optionMaternal&&!optionPaternal) return;
	  
	  //save the starting person
	  person = indi;

	  if(optionMaternal){
		  strULine="";
		  for(iLoop=0 ;iLoop<textMaternal.length(); iLoop++)
			  strULine += "-";		  
		  println(textMaternal);
		  println(strULine);
		  println(iGen + " " + getIndent(iGen)+indi.getName());
		  iGen++;
		  while((indi=indi.getBiologicalMother())!=null) {
			  strTemp = "";
			  strTemp += getIndent(iGen,2,"") + iGen + " " + getNonNullString(indi.getName());
			  strTemp += " (" + getNonNullString(indi.getBirthAsString()) + " - " + getNonNullString(indi.getDeathAsString()) + ")"; 
			  println(strTemp);
			  iGen++;  
		  }
	  }
	  
	  if(optionPaternal){
		  strULine="";
		  for(iLoop=0 ;iLoop<textPaternal.length(); iLoop++)
			  strULine += "-";
		  println();
		  println();
		  println(textPaternal);
		  println(strULine);
		  //restore starting person
		  indi=person;
		  iGen=1;
		  println(iGen + " " + getIndent(iGen)+indi.getName());
		  iGen++;
		  while((indi=indi.getBiologicalFather())!=null) {
			  strTemp = "";
			  strTemp += getIndent(iGen,2,"") + iGen + " " + getNonNullString(indi.getName());
			  strTemp += " (" + getNonNullString(indi.getBirthAsString()) + " - " + getNonNullString(indi.getDeathAsString()) + ")"; 
			  println(strTemp);  
			  iGen++;  
		  }
	  }	  
  }

  
  private String getNonNullString(String testString) {
	  if(testString==null) return ""; else return testString;  
  }
} 