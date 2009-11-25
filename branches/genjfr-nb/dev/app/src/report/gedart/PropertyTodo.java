package gedart;

import genj.gedcom.Property;

import java.util.List;
import java.util.regex.Pattern;

public class PropertyTodo extends Property {
	private static String todoTag = "NOTE";
	private static String todoStart = "TODO:";

	public static void setTag(String tag, String start){
		todoTag = tag;
		todoStart = start;
	}
	@Override
	public String getTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub

	}

// pour memoire	public String getEntity() {return getEntity().toString();} 
// pour memoirt	public String getValue() {return outputPropertyValue(this));}
// public String getPlace() { getParent().getPropertyDisplayValue(("PLAC"))}
// public String getDate() { getParent().getPropertyDisplayValue(("DATE"))}
//public String getEvent() {Gedcom.getName(this.getParent.getTag())
	@SuppressWarnings("unchecked")
	public static List<PropertyTodo> findTodos1(Property of) {
		return of.findProperties(Pattern.compile(todoTag), Pattern.compile(
				todoStart + ".*", Pattern.DOTALL));
	}


}
/* TODO: autre formattage:
	doc.nextTableCell(FORMAT_HEADER3_TODO);
	doc.addText( Gedcom.getName(parent.getTag()) );
	doc.nextTableCell("number-columns-spanned=5,");
	doc.addText( parent.format(PLACE_AND_DATE_FORMAT) );
	doc.nextParagraph();
	outputPropertyValue(prop,doc);
	doc.nextParagraph();
	doc.addText( outputProperty(prop, prop.getPath().toString() + ":REPO") );
	doc.nextParagraph();
	doc.addText( outputProperty(prop, prop.getPath().toString() + ":NOTE") );

 */

// loop over todos for entity
/*for (int i = 0; i < todos.size(); i++) {
	 Property prop = (Property) todos.get(i);
	 if ((prop instanceof PropertyMultilineValue)) continue;
	 Property parent = prop.getParent();
	 
	 if (parent != null){
		 doc.nextTableRow();
		 if ((parent instanceof Entity)) {
			 doc.nextTableCell();
			 doc.nextTableCell();
			 doc.nextTableCell();
		 } else {
			 doc.addText( Gedcom.getName(parent.getTag()) );
			 doc.nextTableCell();
			 doc.addText( parent.getPropertyDisplayValue("DATE") );
			 doc.nextTableCell();
			 doc.addText( parent.getPropertyDisplayValue("PLAC") );
		 }
		 doc.nextTableCell();
		 doc.addText( prop.getEntity().toString() );
		 doc.nextTableCell();
		 outputPropertyValue(prop, doc);
		 
		 nbTodos++;
	 }
*/