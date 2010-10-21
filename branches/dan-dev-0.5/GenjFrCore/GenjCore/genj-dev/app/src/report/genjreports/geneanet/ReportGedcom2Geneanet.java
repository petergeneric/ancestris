package genjreports.geneanet;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.fo.Document;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * GenJ - Report<br>
 * 
 * information taken from the web site : www.geneanet.com :<br>
 * Le fichier GeneaNet a une structure très simple. C'est un fichier texte, dont voici un exemple:<br>
 * CHARRON;;1688;1731;2;MEURSAC;F17;PCH;FRA;A<br>
 * CHOTARD;;1703;1797;4;TESSON;F17;PCH;FRA;A<br>
 * 
 * 
 * Chaque champ est séparé par un point-virgule. Chaque ligne est sur le modèle suivant:<br>
 * name;info;begin;end;nbindi;place;subregion;region;country;type<br>
 * 
 * <b>NAME</b>	est un nom de famille (�ventuellement plusieurs noms d'orthographe proche séparés par des virgules).<br>
 * <b>INFO</b>	est une information complémentaire pour identifier la famille (comme le métier ou les titres).<br>
 * <b>BEGIN - END</b>	sont les années de début et de fin de la période pour laquelle il y a des informations sur le nom<br>
 * <b>NBINDI</b>	Nombre d'individus<br>
 * <b>TOWN</b> 	est le lieu géographique où a vécu la famille. Pour une même entité géographique (SUBREGION/REGION), vous pouvez mettre plusieurs lieux.<br>
 * <b>SUBREGION</b> et <b>REGION</b>	correspondent aux régions administratives à l'intérieur d'un pays.<br>
 * <b>COUNTRY</b>	est le pays. Il est obligatoire. Le code UND signifie que le pays est non déterminé.<br>
 * <b>TYPE</b> 	indique le type de la source:<br>
 * A - Généalogies ascendantes<br>
 * D - Généalogies descendantes<br>
 * F - Généalogies complètes<br>
 * L - les familles �tudi�es par les généalogistes (A, D ou F)<br>
 * P - Généalogies imprimées<br>
 * R - Les sources originelles (registres paroissiaux, notariés, ...)<br>
 * 
 * 
 * TO DO : 
 * 	gérer la date limite inférieure
 * 
 * 
 * @author Yann L'Henoret <yann.lhenoret@gmail.com>
 * @version 0.6
 */
public class ReportGedcom2Geneanet extends Report {

    /** Map with Location elements codes as keys and index of the corresponding element in gedcom file as value <br/>
     * ex : PLAC tag : Town,ZIP code,Region,Country<br/>
     * => key = Location.Region ; value = Integer(2) */
    private Map locationTranslationTable = new HashMap<String, Integer>();

    private ArrayList gedcomPlaceFormatList = null;

    
    // the options for the type of sources 
    private String[] arrayTypes = {"W","A","D","F","L","P","R"};

    public int sourceType = 0;
    public String sourceTypes[] = {
    		   translate("type_W"), // no defined sources
    		   translate("type_A"), // Ascending genealogies
    		   translate("type_D"), // Descending genealogies
    		   translate("type_F"), // Complete genealogies
    		   translate("type_L"), // Genealogists studied families (A, D or F)
    		   translate("type_P"), // Printed genealogies
    		   translate("type_R") // The original sources (parochial registers, notariés, ...)
    		  };


    /** the superior year limit*/
    public int superiorYearLimit = 0;


    private static final TagPath OCCUPATION_TAG = new TagPath("INDI:OCCU");
    private static final TagPath TITLE_TAG = new TagPath("INDI:TITL");

    /** the name of the gedcom file */
    private String gedcomName = null;

    /** the map with the family name as key and a geneanetIndexLinesMap2 as value*/
    private Map geneanetIndexLinesMap1 = new HashMap();
    
    /** options used in the first window opened when the report is launched*/
	  public class Config {
		  public int townIndex = 0;
		  public String townIndexs[];
		  
		  public int townIdIndex = 0;
		  public String townIdIndexs[];
		  
		  public int subRegionIndex = 0;
		  public String subRegionIndexs[] ;
		  
		  public int regionIndex = 0;
		  public String regionIndexs[] ;
		  
		  public int countryIndex = 0;
		  public String countryIndexs[];
	     }
	  public Config conf = new Config();


    /* ---------------------------- */
    /**
     * This method actually starts this report
     */
    public Document start(Gedcom gedcom) {

    	gedcomName = gedcom.getName();

    	// initialization of the place index depending on the place format
    	if(!initializeLocationIndex(gedcom)){return null;}

    	if(Location.existPLACTag){
    		Location.initializeLocationElementIndex(locationTranslationTable);
    	}

        // what to analyze
        Collection indis = gedcom.getEntities(Gedcom.INDI);
        for (Iterator it = indis.iterator(); it.hasNext();) {
            analyze(  (Indi) it.next());
        }

        return generateOutput();

        // write main file out

    }

    /* ---------------------------- */
    /**
     * generates the text output 
     * @param geneanetList the list of Geneanet lines
     */
    private Document generateOutput(){
    	GeneanetIndexLine line;
    	Map map2;
    	Document doc = new Document(getName());
    	doc.startTable("genj:csv=true,width=100%");

    	generateHeader(doc);
    	Object[] keys = geneanetIndexLinesMap1.keySet().toArray();
    	Arrays.sort(keys);
    	for(int i=0;i<keys.length;i++){

    	      map2 = (Map)geneanetIndexLinesMap1.get(keys[i]);

    	      for (Iterator i2 = map2.keySet().iterator(); i2.hasNext(); ) {

    	    	   line = (GeneanetIndexLine)map2.get((String)i2.next());
    	    	  
    	    	   // write in the file
    	    		doc.nextTableRow();
    	    		doc.nextTableCell();
    	    		doc.addText(line.toString());
    	      }
    	}


    		// write in console
    		//println(geneanet.toString());

        return doc;
    }

    /* ---------------------------- */
    /**
     * analyzes the indi
     * @param indi the indi to analyze
     */
    private void analyze(Indi indi){
    	 PropertyPlace place = null;
    	 PropertyDate date = null;

    	 // consider non-private indis only
    	 if (indi.isPrivate()) return;

    	// consider non-empty last names only
        String name = indi.getLastName();
        if (name.length()==0)
          return;
        name = name.trim();

        // loop over places
       try{
	        for (Iterator places = indi.getProperties(PropertyPlace.class).iterator(); places.hasNext(); ) {
	          // Get place
	          place = (PropertyPlace)places.next();

	          // consider non-empty and non private places only
	          if (place.toString().replaceAll(",", "").trim().length()==0
	        		 || place.isPrivate()
	        		  ) continue;

	          int start;
	          int startYear = Integer.MAX_VALUE;
	          int end;
	          int endYear = Integer.MIN_VALUE;
	          
	          // loop over all dates in place to find the lowest and the highest
	          for (Iterator dates = place.getParent().getProperties(PropertyDate.class).iterator(); dates.hasNext(); ) {
	            // consider valid and non private dates only
	            date = (PropertyDate)dates.next();
	            if (!date.isValid() || date.isPrivate()) continue;

	            // compute first and last year

	            start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
	            end = date.isRange() ? date.getEnd().getPointInTime(PointInTime.GREGORIAN).getYear() : start;
	            if (start>end) continue;

	            // year limit is only taken into account if diffent from 0
	            if(superiorYearLimit!=0 && start>superiorYearLimit){
	            	continue;
	            }

	            startYear = Math.min(startYear, start);
	            endYear = Math.max(endYear, end);
	          }

	          // if there is no date, the place is not kept
	          if(startYear==Integer.MAX_VALUE)continue;

	          // info
	          String info = "";
	          if(indi.getProperty(TITLE_TAG)!=null){
	        	  info = indi.getProperty(TITLE_TAG).getValue();
	          }
	          else if(indi.getProperty(OCCUPATION_TAG)!=null){
	        	  info = indi.getProperty(OCCUPATION_TAG).getValue();
	          }

	          Map locationAsKeyMap = (Map)lookup(geneanetIndexLinesMap1, name, HashMap.class);
	          GeneanetIndexLine line = (GeneanetIndexLine)lookup(locationAsKeyMap, Location.buildLocationKey(place), GeneanetIndexLine.class);
	          line.add(name, info, startYear, endYear, new Location(place), arrayTypes[sourceType]);
	        }

	    } catch (Throwable t) {
	        LOG.warning("indi : "+indi.getName()+" "+indi.getFirstName()+" "+t.toString());
	    }
    }

    /* ---------------------------- */
    /**
     * Lookup an object in a map with a default class
     */
    private Object lookup(Map<String, Object> index, String key, Class fallback) {
      // look up and create lazily if necessary
      Object result = index.get(key);
      if (result==null) {
        try {
          result = fallback.newInstance();
        } catch (Throwable t) {
          t.printStackTrace();
          throw new IllegalArgumentException("can't instantiate fallback "+fallback);
        }
        index.put(key, result);
      }
      // done
      return result;
    }
    
    /* ---------------------------- */
    /**
     * initializes the location elements index
     * @param gedcom
     */
    private boolean initializeLocationIndex(Gedcom gedcom) {

    	// if no PLAC tag found
        if (gedcom.getPlaceFormat() == "") {

        	Location.existPLACTag = false;

        	return true;
          } 
          // if PLAC tag found
          else {

        	  String chosenOrderText = null;
          	// initializes the List containing the gedcom PLAC tag elements
          	gedcomPlaceFormatList = new ArrayList(Arrays.asList(gedcom.getPlaceFormat().split("\\,"))); // list used for selection only

          	Location.existPLACTag = true;
//              List choices = (ArrayList)gedcomPlaceFormatList.clone();
//              String locationElement = null;

              String[] locationJuridictionArray = gedcom.getPlaceFormat().split("\\,");
              conf.townIndexs = locationJuridictionArray;

              // for the town id, add a choice "nothing" to let the user choose not to use any town id
              List townIdJuridictionList = (ArrayList)gedcomPlaceFormatList.clone();
              townIdJuridictionList.add(translate("nothing"));
              
              String[] townIdJuridictionArray = (String[]) townIdJuridictionList.toArray(new String[townIdJuridictionList.size()]);//String[] townIdJuridictionList.toArray();

              conf.townIdIndexs = townIdJuridictionArray;
              conf.subRegionIndexs = locationJuridictionArray;
              conf.regionIndexs = locationJuridictionArray;
              conf.countryIndexs = locationJuridictionArray;
              
              if (!getOptionsFromUser(translate("placeFormatAsking"), conf)) return false;

              locationTranslationTable.put(Location.TOWN_CODE,new Integer(conf.townIndex));

              locationTranslationTable.put(Location.TOWN_ID_CODE, new Integer(conf.townIdIndex));

              locationTranslationTable.put(Location.SUBREGION_CODE, new Integer(conf.subRegionIndex));

              locationTranslationTable.put(Location.REGION_CODE, new Integer(conf.regionIndex));

              locationTranslationTable.put(Location.COUNTRY_CODE, new Integer(conf.countryIndex));
  
          }

        return true;
        // done
      }


    /* ---------------------------- */
    /**
     * generates the text output 
     * @param geneanetList the list of Geneanet lines
     */
    private Document generateOutput(List geneanetList){
    	GeneanetIndexLine geneanet;
    	Iterator it = geneanetList.iterator();

    	Document doc = new Document(getName());
    	doc.startTable("genj:csv=true,width=100%");

    	generateHeader(doc);
    	
    	while(it.hasNext()){
    		geneanet = (GeneanetIndexLine)it.next();
    		// write in the file
    		doc.nextTableRow();
    		doc.nextTableCell();
    		doc.addText(geneanet.toString());

    		// write in console
    		//println(geneanet.toString());
    	}
        return doc;
    }
    
    /**
     * generates the header with parameters chosen by the user (juridiction associations, location events...)
     * @param doc
     */
    private void generateHeader(Document doc){
    	doc.nextTableRow();
		doc.nextTableCell();
		doc.addText("# ---------------------------------------------------");
		
		doc.nextTableRow();
		doc.nextTableCell();
		doc.addText("# "+translate("gedcomFile",gedcomName));

		if(Location.existPLACTag){
			doc.nextTableRow();
			doc.nextTableCell();
			doc.addText("# "+translate("placeFormatAsking")+" : ");
			doc.nextTableRow();
			doc.nextTableCell();
			doc.addText("# Geneanet "+translate("town")+" : "+gedcomPlaceFormatList.get(((Integer)locationTranslationTable.get(Location.TOWN_CODE)).intValue()));
			doc.nextTableRow();
			doc.nextTableCell();
			String townIdElement;
			try{
				townIdElement = (String)gedcomPlaceFormatList.get(((Integer)locationTranslationTable.get(Location.TOWN_ID_CODE)).intValue());
			}
			catch(IndexOutOfBoundsException e){
				townIdElement = translate("nothing");
			}
			
			doc.addText("# Geneanet "+translate("townId")+" : "+townIdElement);
			doc.nextTableRow();
			doc.nextTableCell();
			doc.addText("# Geneanet "+translate("subregion")+" : "+gedcomPlaceFormatList.get(((Integer)locationTranslationTable.get(Location.SUBREGION_CODE)).intValue()));
			doc.nextTableRow();
			doc.nextTableCell();
			doc.addText("# Geneanet "+translate("region")+" : "+gedcomPlaceFormatList.get(((Integer)locationTranslationTable.get(Location.REGION_CODE)).intValue()));
			doc.nextTableRow();
			doc.nextTableCell();
			doc.addText("# Geneanet "+translate("country")+" : "+gedcomPlaceFormatList.get(((Integer)locationTranslationTable.get(Location.COUNTRY_CODE)).intValue()));
		}

		doc.nextTableRow();
		doc.nextTableCell();
		doc.addText("# "+translate("superiorYearLimit")+" : "+(superiorYearLimit!=0?Integer.toString(superiorYearLimit):translate("noYearLimit")));

		doc.nextTableRow();
		doc.nextTableCell();
		doc.addText("# ---------------------------------------------------");

    }


    /* ---------------------------- */
    /**
     * gets the <b>INFO</b> data from the current list of indis
     * @param indiList the list of Indi objects from which to get the data
     * @param geneanet the current Geneanet object to fill
     * @TODO taking into account the indis that have several occupations
     */
    private void getInfo(List indiList,GeneanetIndexLine geneanet){
    	String occupation = null;
    	String title = null;
		boolean isOccupationStillFillable = true;
		boolean isTitleStillFillable = true;

    	Iterator it = indiList.iterator();
    	Indi indi;
    	while (it.hasNext()){
			indi = (Indi)it.next();

			//if there is not already more than one title found
			if(isTitleStillFillable){
				// if the current Indi has a title
				if(indi.getProperty(TITLE_TAG)!=null){
					if(title!=null){isTitleStillFillable = false;}
					else{title=indi.getProperty(TITLE_TAG).getValue();}
				}

				// if there is not already more than one occupation found and the current Indi has an occupation
				else if(isOccupationStillFillable && indi.getProperty(OCCUPATION_TAG)!=null){
					if(occupation!=null){isOccupationStillFillable = false;}
					else{occupation=indi.getProperty(OCCUPATION_TAG).getValue();}
				}
			}
		}

    	if(isTitleStillFillable){
    		if(title!=null){
    			geneanet.setInfo(title);
    		}
    		else if(isOccupationStillFillable && occupation!=null){
    			geneanet.setInfo(occupation);
    		}
    	}
    	else{geneanet.setInfo("");}
    	
		if(isOccupationStillFillable && occupation!=null){
			geneanet.setInfo(occupation);
		}
    }

}