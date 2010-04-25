package genjreports.geneanet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * 
 */

/**
 *  parses an xml file with the following structure : <br/>
 * <country name="" code=""><br/>
 * 		<region name="" code=""> <br/>
 * 			<subregion name="" code="" /> <br/>
 * 		</region> <br/>
 * </country>
 * 
 * @author Yann L'Henoret <yann.lhenoret@gmail.com>
 * @version 0.1
 * 

 * 
 */
   public class LocationParser
    {
    	/**
    	 * the xml file name
    	 */
	   private static final String xmlFileName = "geneanet_location.xml";
    	private static LocationParser locationParser;
    	
    	private static final String COUNTRY_TAG = "country";
    	private static final String REGION_TAG = "region";
    	private static final String SUBREGION_TAG = "subregion";
    	
    	private static final String NAME_ATTRIBUTE = "name";
    	private static final String CODE_ATTRIBUTE = "code";

    	private LocationParser(){}
    	
    	/**
    	 * the geneanet location information as a java set of Lists
    	 */
    	private static GeneanetLocationElement tree;
    	
    	public static GeneanetLocationElement getTree(){
    		if(tree==null){
    			tree = new GeneanetLocationElement("PLANET","planet");
    			parse();
    		}
    		
    		return tree;
    	}
    	
    	
    	private static void parse(){
	
    		try {
                // loading of the document -------------------------------
                DocumentBuilderFactory factory = 
                                        DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                
                InputStream fis = LocationParser.class.getResourceAsStream(xmlFileName);
                
                Document doc = builder.parse(fis);

                List countries = new ArrayList();
	              List regions = null;
	              List subRegions = null;
	              GeneanetLocationElement geneanetCountry = null;
	              GeneanetLocationElement geneanetRegion = null;
	              GeneanetLocationElement geneanetSubRegion = null;

                Element planet = doc.getDocumentElement();
                
                // for each country
                NodeList countriesNodeList = planet.getElementsByTagName("country");
                for(int i=0;i<countriesNodeList.getLength();i++){
                	Element countryNode = (Element)countriesNodeList.item(i);
                	geneanetCountry = new GeneanetLocationElement(countryNode.getAttribute("name"),countryNode.getAttribute("code"));

                	// for each region
                	NodeList regionsNodeList = countryNode.getElementsByTagName("region");
                	regions = new ArrayList();
                	for(int j=0;j<regionsNodeList.getLength();j++){
                		Element regionNode = (Element)regionsNodeList.item(j);
                		NamedNodeMap regionAttributes = regionNode.getAttributes();
                		geneanetRegion = new GeneanetLocationElement(regionNode.getAttribute("name"),regionNode.getAttribute("code"));

                		// for each subregion
                		NodeList subRegionNodeList = regionNode.getElementsByTagName("subregion");
                		subRegions = new ArrayList();
                    	for(int k=0;k<subRegionNodeList.getLength();k++){
                    		Element subRegionNode = (Element)subRegionNodeList.item(k);
                    		if(subRegionNode.hasAttribute("name") && subRegionNode.hasAttribute("code")){
	                    		geneanetSubRegion = new GeneanetLocationElement(subRegionNode.getAttribute("name"),subRegionNode.getAttribute("code"));
	                    		subRegions.add(geneanetSubRegion);
                    		}
                    	}
                    	geneanetRegion.setSubElementList(subRegions);
                    	regions.add(geneanetRegion);
                	}
                	geneanetCountry.setSubElementList(regions);
                	countries.add(geneanetCountry);
                }
              tree.setSubElementList(countries);
            } catch(Exception e) {
                e.printStackTrace();
                //TODO some log to write
            }
    	}
	
    }
