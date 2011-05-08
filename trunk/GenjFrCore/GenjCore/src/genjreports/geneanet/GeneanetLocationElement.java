package genjreports.geneanet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 
 */

/**
 * @author Yann L'Henoret <yann.lhenoret@gmail.com>
 * @version 0.1
 *
 */
public class GeneanetLocationElement {

    	private String code;
    	/** the name in lower case and whithout any special character*/
    	private List nameList = null;
    	private List subElementList = new ArrayList();
    	/**
    	 * 
    	 * @param name
    	 * @param code
    	 */
    	public GeneanetLocationElement(String names,String code){
    		nameList = new ArrayList();
    		// extract all the "," separated names and save them in a List
    		List nameListTemp = new ArrayList((Collection)Arrays.asList(names.split(",")));
    		String name = null;
    		Iterator it = nameListTemp.iterator();
    		while(it.hasNext()){
    			name = (String)it.next();
    			nameList.add(simplify(name.toLowerCase()));
    		}
    		this.code = code;
    	}
		/**
		 * @return the code
		 */
		public String getCode() {
			return code;
		}
		/**
		 * @param code the code to set
		 */
		public void setCode(String code) {
			this.code = code;
		}
		/**
		 * the name List in lower case and whithout any special character
		 * @return the name
		 */
		public List getNameList() {
			return nameList;
		}
		
		/**
		 * the main name in lower case and whithout any special character
		 * @return the name
		 */
		public String getMainName() {
			return (String)nameList.get(0);
		}
		
		/**
		 * @param name the name to set
		 */
		public void setNameList(List names) {
			this.nameList = names;
		}
		/**
		 * @return the subElementList
		 */
		public List getSubElementList() {
			return subElementList;
		}
		/**
		 * @param subElementList the subElementList to set
		 */
		public void setSubElementList(List subElementList) {
			this.subElementList = subElementList;
		}

		/**
		 * looks for an element in the sub list with a name matching the one given as parameter
		 * @param name the subElement's name
		 * @return the subElement if there is one with the <b>name</b>, null otherwise.
		 */
		public GeneanetLocationElement getSubElementByName(String name){
			// if no subelement, there can't be any matching
			if(this.subElementList.isEmpty()){
				return null;
			}

			Iterator it = this.subElementList.iterator();
			GeneanetLocationElement element;
			String currentName = null;
			Iterator it2 = null;

			// tries each name in the nameList
			while(it.hasNext()){
				element = (GeneanetLocationElement)it.next();
				it2 = element.getNameList().iterator();
				while(it2.hasNext()){
					currentName = (String)it2.next();
					if(equalsMoreOrLess(currentName,name)){
						return element;
					}
				}
			}
			return null;
		}

		/**
		 * looks for an element in the sub-sub list with a name matching the one given as parameter
		 * @param name the subElement's name
		 * @return the subElement if there is one with the <b>name</b>, null otherwise.
		 */
		public GeneanetLocationElement getSubSubElementByName(String name){
			// if no subelement, there can't be any matching
			if(this.subElementList.isEmpty()){
				return null;
			}

			Iterator it = this.subElementList.iterator();
			GeneanetLocationElement subElement;
			GeneanetLocationElement subSubElement;
			String currentName = null;
			Iterator it2 = null;

			// tries each name in the nameList
			while(it.hasNext()){

				subElement = (GeneanetLocationElement)it.next();
				if((subSubElement = getSubElementByName(name))!=null){
					return subSubElement;
				}
				
			}
			return null;
		}
		
		/**
		 * looks for an element in the sub list with a sub element whose name matches the one given as parameter
		 * @param name the subSubElement's name
		 * @return the subElement if there is one with a subSubElement named by the <b>name</b>, null otherwise.
		 */
		public GeneanetLocationElement getSubElementByNameOfSubSubElement(String name){
			// if no subelement, there can't be any matching
			if(this.subElementList.isEmpty()){
				return null;
			}

			Iterator it = this.subElementList.iterator();
			GeneanetLocationElement subElement;
			String currentName = null;
			Iterator it2 = null;

			// tries each name in the nameList
			while(it.hasNext()){

				subElement = (GeneanetLocationElement)it.next();
				if(getSubElementByName(name)!=null){
					return subElement;
				}
			}
			return null;
		}

		/**
		 * looks for an element in the sub list with a name matching the one given as parameter
		 * @param name the subElement's name
		 * @return the subElement if there is one with the <b>name</b>, null otherwise.
		 */
		public GeneanetLocationElement getSubElementByCode(String code){
			// if no subelement, there can't be any matching
			if(this.subElementList.isEmpty()){
				return null;
			}

			Iterator it = this.subElementList.iterator();
			GeneanetLocationElement element;

			while(it.hasNext()){
				element = (GeneanetLocationElement)it.next();
				if(element.getCode().equals(code)){
					return element;
				}
			}
			return null;
		}
		


	/**
	 * compares 2 strings after simplification (erasing of special characters, transforming to lower case)
	 * @param chaine1 the first string
	 * @param chaine2 the second string is simplified before comparison
	 * @return true if the 2 strings are equals (after simplification), false otherwise.
	 */
		private boolean equalsMoreOrLess(String chaine1, String chaine2){
			//chaine1 = simplify(chaine1.toLowerCase());
			chaine1 = chaine1.toLowerCase();
			chaine2 = simplify(chaine2.toLowerCase());

			if(chaine1.equals(chaine2)){
				return true; 
			}else return false;
		}
		
		/**
		 * "simplifies" the string by removing some of its special characters
		 * @param entry the string to simplify
		 * @return
		 */
		private String simplify(String entry){
			entry.replaceAll("-", " ");
			entry.replaceAll("à", "a");
			entry.replaceAll("â", "a");
			entry.replaceAll("é", "e");
			entry.replaceAll("è", "e");
			entry.replaceAll("ê", "e");
			entry.replaceAll("î", "i");
			entry.replaceAll("ï", "i");
			entry.replaceAll("ô", "o");
			entry.replaceAll("õ", "o");
			entry.replaceAll("ù", "u");

			return entry;
		}
}
