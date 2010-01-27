/**
 * 
 */
package geneanet;

import genj.gedcom.PropertyPlace;

import java.util.Map;

/**
 * A geneanet location composed of the town, the subregion, the region and the country
 *
 */
public class Location {
	
	// constants
	public static final String GENEANET_FORMAT = "geneanet";
	public static final String GEDCOM_FORMAT = "gedcom";
	public static final String TOWN = "TOWN";
	public static final String TOWN_ID = "TOWN_ID";
	public static final String COUNTRY = "COUNTRY";
	public static final String REGION = "REGION";
	public static final String SUBREGION = "SUBREGION";
	
	public static final String TOWN_CODE = "P";
	public static final String TOWN_ID_CODE = "Z";
	public static final String SUBREGION_CODE = "S";
	public static final String REGION_CODE = "R";
	public static final String COUNTRY_CODE = "C";

	
	public static final String UNDETERMINED_COUNTRY = "UND";
	
	private static final String SEPARATOR = ";";
	private static final String SEPARATOR_IN_PLACE = ",";

    private static int townIndexInLocationFormat;
    private static int townIdIndexInLocationFormat;
	private static int subRegionIndexInLocationFormat;
	private static int regionIndexInLocationFormat;
	private static int countryIndexInLocationFormat;
	
	public static boolean existPLACTag = false;  // Will be false if no PLAC tag found in header
	
	// attributes
	private String place = "";
	private String town = "";
	private String townId = "";
	private String subRegion = "";
	private String region = "";
	private String country = "";

	private String subRegionCode = "";
	private String regionCode = "";
	private String countryCode = "";


	 /* ---------------------------- */

	public Location(){}

	public Location(PropertyPlace place) {

		if(existPLACTag){
			this.town = place.getJurisdiction(townIndexInLocationFormat);
			if((this.townId = place.getJurisdiction(townIdIndexInLocationFormat))==null){
				this.townId = "";
			}

			this.subRegion = place.getJurisdiction(subRegionIndexInLocationFormat);
			this.region = place.getJurisdiction(regionIndexInLocationFormat);
			this.country = place.getJurisdiction(countryIndexInLocationFormat);
		}
		else{
			this.place = place.toString();
			this.country = UNDETERMINED_COUNTRY;
			this.countryCode = UNDETERMINED_COUNTRY;
		}
	}


	public static void initializeLocationElementIndex(Map locationInformationOrder) {

		townIndexInLocationFormat = ((Integer)locationInformationOrder.get(TOWN_CODE)).intValue();
		townIdIndexInLocationFormat = ((Integer)locationInformationOrder.get(TOWN_ID_CODE)).intValue();
		subRegionIndexInLocationFormat = ((Integer)locationInformationOrder.get(SUBREGION_CODE)).intValue();
		regionIndexInLocationFormat = ((Integer)locationInformationOrder.get(REGION_CODE)).intValue();
		countryIndexInLocationFormat = ((Integer)locationInformationOrder.get(COUNTRY_CODE)).intValue();
	}

	 /* ---------------------------- */

	/**
	 * displays the information pieces : 
	 * town,insee code or zip code,subregion,region,subregion code,region code,country
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(town)
			.append(SEPARATOR_IN_PLACE)
			.append(townId)
			.append(SEPARATOR_IN_PLACE)
			.append(subRegion)
			.append(SEPARATOR_IN_PLACE)
			.append(region)
			.append(SEPARATOR)
			.append(subRegionCode)
			.append(SEPARATOR)
			.append(regionCode)
			.append(SEPARATOR)
			.append(countryCode);
		return sb.toString();
	}

	 /* ---------------------------- */
/**
 * transforms gedcom location data into geneanet location data
 * @return the location in GeneaNet format 
 */
	public Location fromGedcom2Geneanet() {
		Location geneanetLocation = new Location();

		geneanetLocation.setTown(this.town);
		geneanetLocation.setTownId(this.townId);
		geneanetLocation.setSubRegion(this.subRegion);
		geneanetLocation.setRegion(this.region);
		geneanetLocation.setPlace(this.place);


		// if non PLAC tag, only th geneanet town and country will be filled
		if(!existPLACTag){
			geneanetLocation.setCountryCode(UNDETERMINED_COUNTRY);
			geneanetLocation.setRegionCode("");;
			geneanetLocation.setSubRegionCode("");;
		}
		else{
			findCountryCode(geneanetLocation);
			findRegionCode(geneanetLocation);
			findSubRegionCode(geneanetLocation);
		}

		return geneanetLocation;
	}

	/**
	 * @param geneanetLocation
	 * @return
	 */
	private void findSubRegionCode(Location geneanetLocation) {
		GeneanetLocationElement subRegionElement = null;
		
		if(!geneanetLocation.getCountryCode().equals(UNDETERMINED_COUNTRY) 
				&& this.subRegion != null && this.subRegion.length() > 0){
			if((subRegionElement = findSubRegionGeneanetLocationPart(geneanetLocation,this.subRegion))!=null){
				geneanetLocation.setSubRegionCode(subRegionElement.getCode());}
		}
	}
	
	/**
	 * @param geneanetLocation
	 * @return
	 */
	private void findRegionCode(Location geneanetLocation) {
		GeneanetLocationElement regionElement = null;
		if(!geneanetLocation.getCountryCode().equals(UNDETERMINED_COUNTRY) 
				&& this.region != null && this.region.length() > 0){
			if((regionElement = findRegionGeneanetLocationPart(geneanetLocation,this.region))!=null){
				geneanetLocation.setRegionCode(regionElement.getCode());}
		}
	}

/**
 * @param geneanetLocation
 * @param countryElement
 * @return
 */
private void findCountryCode(Location geneanetLocation) {
	GeneanetLocationElement countryElement = null;
	if (this.country != null && this.country.length() > 0) {
		if((countryElement = findCountryGeneanetLocationPart(this.country))!=null){
			geneanetLocation.setCountryCode(countryElement.getCode());
		}
		// if no country code found, fill with GeneaNet UNDETERMINED_COUNTRY code
		else{
			geneanetLocation.setCountryCode(UNDETERMINED_COUNTRY);
		}
	}
	else{
		geneanetLocation.setCountryCode(UNDETERMINED_COUNTRY);
	}
}

	 /* ---------------------------- */
	/**
	 * looks for a country whose name is given as parameter
	 * @param countryLocationName the searched country name
	 * @return the GeneanetLocationElement corresponding to the given country name, null otherwise
	 */
	public GeneanetLocationElement findCountryGeneanetLocationPart(String countryLocationName) {
		GeneanetLocationElement tree = LocationParser.getTree();
		return tree.getSubElementByName(countryLocationName);	
	}
	
	 /* ---------------------------- */
	/**
	 * looks for a region whose name is given as parameter and in the country given as parameter
	 * @param countryLocationCode the country code
	 * @param regionLocationName the region name
	 * @return the GeneanetLocationElement corresponding to the given country code and region name, null otherwise
	 */
	public GeneanetLocationElement findRegionGeneanetLocationPart(Location location, String regionLocationName) {
		GeneanetLocationElement tree = LocationParser.getTree();
		GeneanetLocationElement region;
		
		// try to find the regionCode in the region list under the country code
		if((region = tree.getSubElementByCode(location.getCountryCode()).getSubElementByName(regionLocationName))!=null){
			return region;
		}
		
		// if the first attempt failed, try to get the region code that is parent to the subregion name, under the country code
		if((region = tree.getSubElementByCode(location.getCountryCode()).getSubElementByNameOfSubSubElement(location.getRegion()))!=null){
			return region;
		}

		// if nothing worked, just go to bed :-(
		return null;	
	}
	
	 /* ---------------------------- */
	/**
	 * looks for the subregion in the tree
	 * @param countryLocationCode the country code
	 * @param regionLocationCode the region code
	 * @param subRegionLocationName the subregion name
	 * @return
	 */
	public GeneanetLocationElement findSubRegionGeneanetLocationPart(Location geneaNetLocation,  String subRegionLocationName) {
		GeneanetLocationElement tree = LocationParser.getTree();
		
		return tree.getSubElementByCode(geneaNetLocation.getCountryCode()).getSubElementByCode(geneaNetLocation.getRegionCode()).getSubElementByName(subRegionLocationName);	
	}
	
	
    /* ---------------------------- */
    /**
     * builds a key based on the the location and the subregion
     * @param town the town from which to build the key
     * @return
     */
    public static String buildLocationKey(PropertyPlace location){
    	String key = null;
    	if(Location.existPLACTag){
    		// city__subregion
    		StringBuffer sb =new StringBuffer()
    			.append(location.getJurisdiction(townIndexInLocationFormat))
    			.append("__")
    			.append(location.getJurisdiction(subRegionIndexInLocationFormat));
    		key = sb.toString();
    	}
    	else{
    		key = location.toString();
    	}

    	return key;
    }
	
    
	/**
	 * @return the place information (town,town id,subregion,region)
	 */
	public String getPlace() {
		if(this.place.equals("")){
			StringBuffer placeSB = new StringBuffer(this.town)
			.append(SEPARATOR_IN_PLACE)
			.append(this.townId)
			.append(SEPARATOR_IN_PLACE)
			.append(this.subRegion)
			.append(SEPARATOR_IN_PLACE)
			.append(this.region);

			 this.place = placeSB.toString();
		}
			
		return this.place.toString();
	}

	public void setPlace(String place) {
		this.place = place;
	}
	/* -------------------*/
	/* GETTERS ET SETTETS */
	/* -------------------*/


	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the town
	 */
	public String getTown() {
		return town;
	}

	/**
	 * @param town the town to set
	 */
	public void setTown(String town) {
		this.town = town;
	}

	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * @return the subRegion
	 */
	public String getSubRegion() {
		return subRegion;
	}

	/**
	 * @param subRegion the subRegion to set
	 */
	public void setSubRegion(String subRegion) {
		this.subRegion = subRegion;
	}

	/**
	 * @return the countryCode
	 */
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * @param countryCode the countryCode to set
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	/**
	 * @return the townId
	 */
	public String getTownId() {
		return townId;
	}

	/**
	 * @param townId the townId to set
	 */
	public void setTownId(String townId) {
		this.townId = townId;
	}

	/**
	 * @return the regionCode
	 */
	public String getRegionCode() {
		return regionCode;
	}

	/**
	 * @param regionCode the regionCode to set
	 */
	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	/**
	 * @return the subRegionCode
	 */
	public String getSubRegionCode() {
		return subRegionCode;
	}

	/**
	 * @param subRegionCode the subRegionCode to set
	 */
	public void setSubRegionCode(String subRegionCode) {
		this.subRegionCode = subRegionCode;
	}

}
