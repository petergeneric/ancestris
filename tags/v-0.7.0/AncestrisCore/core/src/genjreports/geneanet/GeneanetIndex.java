/**
 * 
 */
package genjreports.geneanet;


/**
 * A geneanet line, (ie) a family name associated with its living place during a certain period.
 */
public class GeneanetIndex implements Comparable  {
	private String name = "";
	private String info = "";
	private String beginningDate = "";
	private String endDate = "";
	private String nbIndi = "";
	private String place = "";
	private String subRegion = "";
	private String region = "";
	private String country = "";
	private String type = "";
	
	 /* ---------------------------- */
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb = new StringBuffer();
		sb.append(name);
		sb.append(";");
		sb.append(info);
		sb.append(";");
		sb.append(beginningDate);
		sb.append(";");
		sb.append(endDate);
		sb.append(";");
		sb.append(nbIndi);
		sb.append(";");
		sb.append(place);
		sb.append(";");
		sb.append(subRegion);
		sb.append(";");
		sb.append(region);
		sb.append(";");
		sb.append(country);
		sb.append(";");
		sb.append(type);
		return sb.toString();
	}

/**
 * compares the current geneanetIndex with the parameter.<br/>
 * The compared attributes are the <b>name</b> and the <b>place</b>.
 * @param otherGeneanetIndex
 * @return
 */
	public int compareTo(Object geneanetIndex){
		
		if(!geneanetIndex.getClass().equals(GeneanetIndex.class))
			return 0;

		GeneanetIndex otherGeneanetIndex = (GeneanetIndex)geneanetIndex;
		int res = this.name.compareTo(otherGeneanetIndex.name);
		
		if(res ==0)
			res = this.place.compareTo(otherGeneanetIndex.place);
		
		return res;
	}

	
	/* -------------------*/
	/* GETTERS ET SETTETS */
	/* -------------------*/
	
	
/**
 * @return the beginningDate
 */
public String getBeginningDate() {
	return beginningDate;
}

/**
 * @param beginningDate the beginningDate to set
 */
public void setBeginningDate(String beginningDate) {
	this.beginningDate = beginningDate;
}

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
 * @return the endDate
 */
public String getEndDate() {
	return endDate;
}

/**
 * @param endDate the endDate to set
 */
public void setEndDate(String endDate) {
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
public String getNbIndi() {
	return nbIndi;
}

/**
 * @param nbIndi the nbIndi to set
 */
public void setNbIndi(String nbIndi) {
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
