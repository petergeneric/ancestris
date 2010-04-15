/**
 * 
 */
package genjreports.geneanet;

import genj.gedcom.time.PointInTime;


/**
 * A geneanet line, (ie) a family name associated with its living place during a certain period.
 */
public class GeneanetIndexLine implements Comparable  {
	private String name = "";
	private String info = "";
	private int beginningDate = Integer.MAX_VALUE;
	private int endDate = Integer.MIN_VALUE;
	private int nbIndi = 0;

	private String place = "";
	private String subRegionCode = "";
	private String regionCode = "";
	private String countryCode = "";
	private String type = "";

	 /* ---------------------------- */

	public String toString(){

		StringBuffer sb = new StringBuffer();
		sb = new StringBuffer();
		sb.append(name);
		sb.append(";");
		sb.append(info);
		sb.append(";");
		sb.append((beginningDate==Integer.MAX_VALUE?"":String.valueOf(beginningDate)));
		sb.append(";");
		sb.append((endDate==Integer.MIN_VALUE?"":String.valueOf(endDate)));
		sb.append(";");
		sb.append(nbIndi);
		sb.append(";");
		sb.append(place);
		sb.append(";");
		sb.append(subRegionCode);
		sb.append(";");
		sb.append(regionCode);
		sb.append(";");
		sb.append(countryCode);
		sb.append(";");
		sb.append(type);

		return sb.toString();
	}

	 /* ---------------------------- */
/**
 * compares the current geneanetIndex with the parameter.<br/>
 * The compared attributes are the <b>name</b> and the <b>place</b>.
 * @param otherGeneanetIndexLine
 * @return
 */
	public int compareTo(Object otherGeneanetIndexLine){

		if(!otherGeneanetIndexLine.getClass().equals(GeneanetIndexLine.class))
			return 0;

		GeneanetIndexLine otherGeneanetIndex = (GeneanetIndexLine)otherGeneanetIndexLine;
		int res = this.name.compareTo(otherGeneanetIndex.name);

		if(res ==0)
			res = this.place.compareTo(otherGeneanetIndex.place);

		return res;
	}


	public void add(String name, String info, int start, int end, Location location, String type) {

		if(this.name.length()==0){
			this.name = name;
		}

		// check for valid year - this might still be UNKNOWN even though a date was valid
	      if (start!=PointInTime.UNKNOWN)
	        this.beginningDate = Math.min(beginningDate, start);
	      if (end!=PointInTime.UNKNOWN)
	        this.endDate = Math.max(endDate, end);

	      this.nbIndi+=1;

	      if(place.length()==0){
	    	  Location  geneanetLocation = location.fromGedcom2Geneanet();
	    	  this.place = geneanetLocation.getPlace();
	    	  this.subRegionCode = geneanetLocation.getSubRegionCode();
	    	  this.regionCode = geneanetLocation.getRegionCode();
	    	  this.countryCode = geneanetLocation.getCountryCode();
	      }

	      if(this.info.length()==0){
	    	  this.info = info;
	      }
	      else if(info.length()!=0){
	    	  this.info = "";
	      }

	      if(this.type.length()==0){
	    	  this.type = type;
	      }
	}


	/* -------------------*/
	/* GETTERS ET SETTETS */
	/* -------------------*/
	
	
/**
 * @return the beginningDate
 */
public int getBeginningDate() {
	return beginningDate;
}

/**
 * @param beginningDate the beginningDate to set
 */
public void setBeginningDate(int beginningDate) {
	this.beginningDate = beginningDate;
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
public void setCountryCode(String country) {
	this.countryCode = country;
}

/**
 * @return the endDate
 */
public int getEndDate() {
	return endDate;
}

/**
 * @param endDate the endDate to set
 */
public void setEndDate(int endDate) {
	this.endDate = endDate;
}

/**
 * @return the info
 */
public String getInfo() {
	return info;
}

/**
 * @param info the info to set
 */
public void setInfo(String info) {
	this.info = info;
}

/**
 * @return the name
 */
public String getName() {
	return name;
}

/**
 * @param name the name to set
 */
public void setName(String name) {
	this.name = name;
}

/**
 * @return the nbIndi
 */
public int getNbIndi() {
	return nbIndi;
}

/**
 * @param nbIndi the nbIndi to set
 */
public void setNbIndi(int nbIndi) {
	this.nbIndi = nbIndi;
}

/**
 * @return the place
 */
public String getPlace() {
	return place;
}

/**
 * @param place the place to set
 */
public void setPlace(String place) {
	this.place = place;
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
public void setRegionCode(String region) {
	this.regionCode = region;
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
public void setSubRegionCode(String subRegion) {
	this.subRegionCode = subRegion;
}

/**
 * @return the type
 */
public String getType() {
	return type;
}

/**
 * @param type the type to set
 */
public void setType(String type) {
	this.type = type;
}

}