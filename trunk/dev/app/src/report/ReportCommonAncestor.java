/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import genj.report.options.ComponentReport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jfree.util.Log;

import tree.Translator;
import tree.graphics.GraphicsOutput;
import tree.graphics.GraphicsOutputFactory;
import tree.graphics.GraphicsRenderer;
import tree.output.RendererFactory;


//TODO let the user choose which ancestor to display when there are several
//TODO evaluate the optimal box width based on the name lengths
//TODO option landscape or portrait
//TODO rewrite title depending on the relationship between indis (cousins, parents...)
//TODO shouldn't we take into account the potential other parents (adoption...)?
//TODO let the user choose the year number limit for the dates to display
//TODO option : display the generation date or not


/**
 * Computes the common ancestor of two indis and shows the descendant chain between the common ancestor and these two selected indis
 * @author nmeier
 * @author ylhenoret
 *
 */
public class ReportCommonAncestor extends ComponentReport {

    private static final String OUTPUT_CATEGORY = "output";
    
    private static final int FAMILY_WIDTH = 300;
    private static final int FAMILY_HEIGH = 100;
    private static final int SPACE_BETWEEN_RECTANGLES = 20;
    private static final int SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR = 30;
    private static final int SPACE_BETWEEN_LINES = 18;
    private static final int SPACE_BEFORE_DATE = 20;
    private static final int SHADOW_SIZE = 3;
    private static final int SPACE_BETWEEN_BORDER_AND_RECTANGLE = 5;
    private static final int SPACE_BETWEEN_BORDER_AND_TITLE = 25;
    
   
    
    private static final int MALE = 1;
    private static final int FEMALE = 2;
    
    private Font boldFontStyle;
    private Font plainFontStyle;
    private Font dateFontStyle;
    private Font titleFontStyle;
    private Font smallFontStyle;
    
    private final int YEAR_LIMIT_NUMBER = 75;
    private final int YEAR_LIMIT = Calendar.getInstance().get(Calendar.YEAR) - YEAR_LIMIT_NUMBER;
    
    
    /** Whether to use colors (or only black and white). */
    public boolean use_colors = true;
    
    /** Whether to display indi and family ids. */
    public boolean display_ids = true;
    
    /** Whether to display under 75 year dates. */
    public boolean displayRecentYears = true;
    
    /** Whether to display the husband or the wife first in each step bloc. */
   public int husband_or_wife_first = 0;
   public String husband_or_wife_firsts[] = { 
		   translate("wife"),
		   translate("husband")
		     };

   
	/** choose font to use*/
    public int ufont_name=0;
	public String ufont_names[] = { translate("ufont_name.0"),
			translate("ufont_name.1"), translate("ufont_name.2"),
			translate("ufont_name.3"), translate("ufont_name.4"),
			translate("ufont_name.5") };
    private String font_name = "Helvetica";
    
    
    
    /** Generates file or screen output. */
    private GraphicsOutputFactory outputs = new GraphicsOutputFactory();
    
    /** Object used for translating strings. */
    private Translator translator = new Translator(this);;

    /** Draws the tree to an output. */
    private RendererFactory renderers = new RendererFactory(translator);

    
    /** where to position the representation parts */
    private enum Position {LEFT, RIGHT, CENTER};
    
    //----------------
    // METHODS
    //----------------
   
    public ReportCommonAncestor()
    {
        addOptions(outputs, OUTPUT_CATEGORY);
    }
    
    /* ----------------- */
	/**
	 * we're not using the console
	 */
	public boolean usesStandardOut() {
		return false;
	}

	 /* ----------------- */
	/**
	 * special treatment context argument check
	 */
	public String accepts(Object context) {
		// an indi is fine
		if (context instanceof Indi)
			return getName();
		// an array of indis is fine as well
		if (context instanceof Indi[]) {
			Indi[] indis = (Indi[]) context;
			if (indis.length == 2)
				return getName();
		}
		// no go
		return null;
	}

	 /* ----------------- */
	/**
	 * our main method for an argument individual
	 */
	public void start(Indi indi) {
		initStyles();
		
		// ask for other
		Indi other = (Indi) getEntityFromUser(translate("select"), indi.getGedcom(), Gedcom.INDI);

		if (other == null)
			return;
		// continue
		start(new Indi[] { indi, other });
	}

	 /* ----------------- */
	/**
	 * our main method for an argument of a bunch of individuals
	 */
	public void start(Indi[] indis) {

		// first and second indis
		Indi firstSelectedIndi = indis[0];
		Indi secondSelectedIndi = indis[1];
		
		// the list to gather the different ancestors in order to let the user choose which one to display
		Set<Indi> ancestorList = new LinkedHashSet<Indi>();
		
		// search the common ancestor
		Indi ancestor = null;
		if (firstSelectedIndi.isAncestorOf(secondSelectedIndi)){
			ancestor = firstSelectedIndi;
		}else if (secondSelectedIndi.isAncestorOf(firstSelectedIndi)){
			ancestor = secondSelectedIndi;
		}else {
			getCommonAncestor(firstSelectedIndi, secondSelectedIndi, ancestorList);
			getCommonAncestor(secondSelectedIndi, firstSelectedIndi, ancestorList);
			ancestorList = filterAncestors(ancestorList);
//			regroupCoupleMembers(ancestorList);
		}
		
		if (ancestorList.size()==1){
			ancestor = (Indi) ancestorList.toArray()[0];
		}
		// if there is more than one ancestor, let the user choose which one to display
		else if (ancestorList.size()>1) {
			for (Indi indi : ancestorList) {
				Log.info("name : "+indi.getName()+" id : "+indi.getId());
			}
			Log.info("----------------------");

			ancestor = (Indi)getValueFromUser(translate("select_ancestor_in_list"), ancestorList.toArray(), (Indi) ancestorList.toArray()[0]);
		}  


		// if the common ancestor exists
		if (ancestor != null){
			List<Step> firstIndiDirectLinks = new ArrayList<Step>();
			firstIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(firstSelectedIndi),firstSelectedIndi, firstSelectedIndi.getSex()));
			getAncestorListBetween(ancestor, firstSelectedIndi, firstIndiDirectLinks);
			Collections.reverse(firstIndiDirectLinks);
			LOG.fine("indi's link number : "+firstIndiDirectLinks.size());
			
			List<Step> secondIndiDirectLinks = new ArrayList<Step>();
		
			secondIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(secondSelectedIndi),secondSelectedIndi, secondSelectedIndi.getSex()));
			getAncestorListBetween(ancestor, secondSelectedIndi, secondIndiDirectLinks);
			Collections.reverse(secondIndiDirectLinks);
			LOG.fine("other's link number : "+secondIndiDirectLinks.size());

			GraphicsOutput output = outputs.createOutput(this);
	        if (output == null) // report canceled
	            return;
	        try {
	            output.output(new Renderer(firstSelectedIndi, secondSelectedIndi, firstIndiDirectLinks, secondIndiDirectLinks));
	            output.display(this);
	        } catch (IOException e) {
	            println("error");
	        }
		}
		
		// nothing to show?
		else if (ancestor == null) {
			getOptionFromUser(translate("nocommon"), Report.OPTION_OK);
			return;
		}
//
//		// show the result
//		List<Context> list = new ArrayList<Context>();
//		list
//				.add(new ViewContext(indi).setText(translate("result.first",
//						indi)));
//		list.add(new ViewContext(other).setText(translate("result.second",
//				other)));
//		list.add(new ViewContext(ancestor).setText(translate("result.ancestor",
//				ancestor)));
//
//		showAnnotationsToUser(indi.getGedcom(), getName(), list);
	}
	  
	  
	 /* ----------------- */
	/**
	 * init the styles with the selected font
	 */
	private void initStyles(){
		font_name = translate("ufont_name."+ufont_name);
		
		boldFontStyle = new Font(font_name, Font.BOLD, 12);
	    plainFontStyle = new Font(font_name, Font.PLAIN, 12);
	    dateFontStyle = new Font(font_name, Font.PLAIN, 10);
	    titleFontStyle = new Font(font_name, Font.BOLD, 14);
	    smallFontStyle = new Font(font_name, Font.BOLD, 8);
	}
	
	 /* ----------------- */
	/**
	 * @param ancestorList
	 */
	private List<Object> regroupCoupleMembers(List<Indi> ancestorList){
		List<Object> results = new ArrayList<Object>();
		
			for (Indi ancestor : ancestorList) {
				Fam[] families = ancestor.getFamiliesWhereSpouse();
				// for each of the families the ancestor belonged to,
				// look if one of the other ancestors did not belong to it
				for (int i = 0; i < families.length; i++) {
					Indi otherSpouse = families[i].getOtherSpouse(ancestor);
					for (Indi indi : ancestorList) {
						if (otherSpouse.equals(indi) 
								&& !results.contains(families[i])){
							ancestorList.remove(otherSpouse);
							results.add(families[i]);
							break;
						}
					}
				}
			}
			return results;
	}
	
	private Set<Indi> filterAncestors(Set<Indi> ancestorList){
		Set<Indi> filteredList = new LinkedHashSet<Indi>();
		boolean found = false;
		for (Indi ancestor : ancestorList) {
			found = false;
			Fam[] families = ancestor.getFamiliesWhereSpouse();
			// for each of the families the ancestor belonged to,
			// look if one of the other ancestors did not belong to it
			for (int i = 0; i < families.length; i++) {
				Indi otherSpouse = families[i].getOtherSpouse(ancestor);
				if (filteredList.contains(otherSpouse)){
					found = true;
				}
			}
			if(found == false){
				filteredList.add(ancestor);
			}
		}
		return filteredList;
	}
	
	 /* ----------------- */
	/** finds the most recent family of the given Indi
	 * @param indi the Indi whom family we're looking for
	 * @return his last family
	 */
	private Fam getLastFamilyWhereSpouse(Indi indi){
		Fam[] fams = indi.getFamiliesWhereSpouse();
		if (fams==null || fams.length==0){
			return null;
		}
		return fams[fams.length-1];
	}
	
	 /* ----------------- */
	/**
	 * check biological father and mother of "indi" to see of one is "other"'s ancestor.
	 * @param indi
	 * @param other
	 * @return
	 */
	private void getCommonAncestor(Indi firstIndi, Indi secondIndi, Set<Indi> ancestorList) {
		//FIXME non symmetrical algorithm :  

		Indi father = firstIndi.getBiologicalFather();
		if (father != null) {
			if (father.isAncestorOf(secondIndi)){
				ancestorList.add(father);
			}
			else{
				getCommonAncestor(father, secondIndi, ancestorList);
			}
		}
		
		Indi mother = firstIndi.getBiologicalMother();
		if (mother != null) {
			if (mother.isAncestorOf(secondIndi)){
				ancestorList.add(mother);
			} else {
				getCommonAncestor(mother, secondIndi, ancestorList);
			}
			
		}

	}

	 /* ----------------- */
	/**
	 * @param ancestor
	 * @param descendant
	 * @param directLinks
	 */
	private void getAncestorListBetween(Indi ancestor, Indi descendant, List<Step> directLinks) {
		
		Indi link = getParentInDirectLine(ancestor, descendant);

		// while there are links to be added, we keep going
		if(link != null){
			directLinks.add(new Step(descendant.getFamilyWhereBiologicalChild(),link, link.getSex()));
			LOG.fine("found link between indi and ancestor : "+link.getName());
			getAncestorListBetween(ancestor, link, directLinks);
		}

	}
	
	 /* ----------------- */
	/**
	 * @param ancestor
	 * @param child
	 * @return
	 */
	private Indi getParentInDirectLine(Indi ancestor, Indi child) {

		// check his mom/dad
		Indi father = child.getBiologicalFather();
		if (father != null) {

			if (father.isDescendantOf(ancestor) || father.equals(ancestor))
				return father;
		}

		Indi mother = child.getBiologicalMother();
		if (mother != null) {
			if (mother.isDescendantOf(ancestor) || mother.equals(ancestor))
				return mother;
		}

		// this case is never to happen as we checked that there is a link
		// between the child and the ancestor, until one of the parent is the
		// famous ancestor
		return null;
	}


	
	 /* ----------------- */
	/**
	 * a step in the link chain between one of the two Indis chosen by the user and the researched ancestor
	 * @author ylh
	 *
	 */
	private class Step{
		Fam famWhereSpouse;
		int linkSex;
		Indi link;
		
		 /* ----------------- */
		/**
		 * @param famWhereChild
		 * @param link
		 * @param linkSex
		 */
		public Step(Fam famWhereChild, Indi link, int linkSex){
			this.famWhereSpouse = famWhereChild;
			this.link = link;
			this.linkSex = linkSex;
		}
		
		public Indi getLink(){
//			if(linkSex==MALE){
//				return fam.getHusband();
//			}else if(linkSex==FEMALE){
//				return fam.getWife();
//			}
//			else{
//				Log.info("the link has no determined sex...");
//				Log.info("the link has no determined sex...");
//				return null;
//			}
			return link;
		}
		
		 /* ----------------- */
		/**
		 * get the wife of this step : it may be the link or the link's wife, or null if link had no wife
		 * @return
		 */
		public Indi getWife(){
			if(linkSex==FEMALE){
				return link;
			}
			if(famWhereSpouse!=null){
				return famWhereSpouse.getWife();	
			}
			return null;
		}
		
		 /* ----------------- */
		/**
		 * get the husband of this step : it may be the link or the link's husband, or null if link had no husband
		 * @return
		 */
		public Indi getHusband(){
			if(linkSex==MALE){
				return link;
			}
			
			if(famWhereSpouse!=null){
				return famWhereSpouse.getHusband();	
			}
			return null;
		}
	}
	
	 /**
	 * @author Yann
	 *
	 */
	private class Renderer implements GraphicsRenderer {

	        private Indi firstIndi;
	        private Indi secondIndi;
	        private List<Step> firstIndiDirectLinks;
	        private List<Step> secondIndiDirectLinks;

	       

	        private AffineTransform defaultTransform;

	        private int width;
	        private int height;
	        private double cx;
	        private double cy;

	        /* ----------------- */
	        /**
	         * renderer's constructor. The entry point to the ouput generation
	         * @param firstIndi
	         * @param secondIndi
	         * @param indiDirectLinks
	         * @param otherDirectLinks
	         */
	        public Renderer(Indi firstIndi, Indi secondIndi, List<Step> firstIndiDirectLinks, List<Step> secondIndiDirectLinks) {
	            this.firstIndi = firstIndi;
	            this.secondIndi = secondIndi;
	            this.firstIndiDirectLinks = firstIndiDirectLinks;
	            this.secondIndiDirectLinks = secondIndiDirectLinks;

//	            int generations = getGenerationCount(indi, max_generations);
	            width = 3 * FAMILY_WIDTH + FAMILY_WIDTH/2;
	            height = (Math.max(firstIndiDirectLinks.size(), secondIndiDirectLinks.size())) * FAMILY_HEIGH
	            		+ (Math.max(firstIndiDirectLinks.size(), secondIndiDirectLinks.size())+3) * SPACE_BETWEEN_RECTANGLES
	            		+ SPACE_BEFORE_DATE
	            		+ SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR
	            		+ SPACE_BETWEEN_BORDER_AND_RECTANGLE
	            		+ SPACE_BETWEEN_BORDER_AND_TITLE;

	            cx = width / 2;
	        }

	        /* ----------------- */
	        /* (non-Javadoc)
	         * @see tree.graphics.GraphicsRenderer#render(java.awt.Graphics2D)
	         */
	        public void render(Graphics2D graphics) {
	        	cy = 0;
	            graphics.setPaint(Color.BLACK);
	            graphics.setBackground(Color.WHITE);
	            graphics.clearRect(0, 0, getImageWidth(), getImageHeight());
	            graphics.drawRoundRect(SPACE_BETWEEN_BORDER_AND_RECTANGLE, 
	            						SPACE_BETWEEN_BORDER_AND_RECTANGLE, 
	            						getImageWidth()-SPACE_BETWEEN_BORDER_AND_RECTANGLE*2, 
	            						getImageHeight()-SPACE_BETWEEN_BORDER_AND_RECTANGLE*2, 50, 50);
//	            graphics.drawRoundRect(SPACE_BETWEEN_BORDER_AND_RECTANGLE+3, 
//						SPACE_BETWEEN_BORDER_AND_RECTANGLE+3, 
//						getImageWidth()-SPACE_BETWEEN_BORDER_AND_RECTANGLE*2-6, 
//						getImageHeight()-SPACE_BETWEEN_BORDER_AND_RECTANGLE*2-6, 50, 50);
	            graphics.setFont(plainFontStyle);
	            graphics.setStroke(new BasicStroke(2));
	            defaultTransform = new AffineTransform(graphics.getTransform());
	            cy += SPACE_BETWEEN_BORDER_AND_RECTANGLE;
	            int nbMaxGen = Math.max(firstIndiDirectLinks.size(), secondIndiDirectLinks.size());
	            
	            // the title
	            graphics.setFont(titleFontStyle);
	            cy += SPACE_BETWEEN_BORDER_AND_TITLE;
	            centerString(graphics, getTitleLine(firstIndi, secondIndi, nbMaxGen), (int)cx, (int)cy );
	            graphics.setFont(plainFontStyle);
	            cy += SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR;
	            
	            //the common ancestor
	            render(graphics, firstIndiDirectLinks.get(0),Position.CENTER);
	            graphics.drawLine((int)cx, (int)cy+FAMILY_HEIGH, (int)cx, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES);
	            if(firstIndiDirectLinks.size()>1){
	            	graphics.drawLine((int)cx-FAMILY_WIDTH, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES, (int)cx, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES);
	            }
	            if(secondIndiDirectLinks.size()>1){
	            	graphics.drawLine((int)cx, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES, (int)cx+FAMILY_WIDTH, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES);
	            }
	            
	            cy+=SPACE_BETWEEN_RECTANGLES;
	            
	            // the two branches
	            for (int i=1;i<nbMaxGen;i++) {
					cy += FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES;
					if(firstIndiDirectLinks.size()>i){
						 graphics.drawLine((int)cx-FAMILY_WIDTH, (int)cy-SPACE_BETWEEN_RECTANGLES, (int)cx-FAMILY_WIDTH, (int)cy);
						render(graphics, firstIndiDirectLinks.get(i),Position.LEFT);
					}
					if(secondIndiDirectLinks.size()>i){
						 graphics.drawLine((int)cx+FAMILY_WIDTH, (int)cy-SPACE_BETWEEN_RECTANGLES, (int)cx+FAMILY_WIDTH, (int)cy);
						render(graphics, secondIndiDirectLinks.get(i),Position.RIGHT);
					}
				}
	            
	            // date of generation
	            graphics.setFont(smallFontStyle);
	            centerString(graphics, PointInTime.getNow().toString(), (int)cx+FAMILY_WIDTH, (int)cy+FAMILY_HEIGH+SPACE_BEFORE_DATE );
	            
	        }

	        /* ----------------- */
	        /* (non-Javadoc)
	         * @see tree.graphics.GraphicsRenderer#getImageWidth()
	         */
	        public int getImageWidth() {
	            return width;
	        }

	        /* ----------------- */
	        /* (non-Javadoc)
	         * @see tree.graphics.GraphicsRenderer#getImageHeight()
	         */
	        public int getImageHeight() {
	            return height;
	        }

	        /* ----------------- */
	        /**
	         * draw a rectangle<br/> 
	         * write the couple names (and dates) in this rectangle<br/>
	         * put the ascendant name in bold<br/>
	         * @param step a link between the common ancestor and a descendant, with its spouse
	         * @param rightLeft where to position the step
	         */
	        private void render(Graphics2D graphics, Step step, Position rightLeft){
	        	
	        	graphics.setPaint(Color.BLACK);
	        	int cxStep = 0;
	        	if(rightLeft==Position.LEFT){
	        		cxStep = (int)cx-FAMILY_WIDTH;
	        	} else if(rightLeft==Position.RIGHT){
	        		cxStep = (int)cx+FAMILY_WIDTH;
	        	}else{
	        		cxStep = (int)cx;
	        	}

	        	LOG.fine("step.link.getName() "+step.getLink().getName());
	        	
	        	// the rectangle containing one step
	
	        	graphics.setPaint(Color.LIGHT_GRAY);
	        	graphics.fillRect(cxStep-FAMILY_WIDTH/2+SHADOW_SIZE, (int)cy+SHADOW_SIZE, FAMILY_WIDTH+SHADOW_SIZE, FAMILY_HEIGH+SHADOW_SIZE);
	        	
	        	graphics.setPaint(Color.BLACK);
	        	graphics.clearRect(cxStep-FAMILY_WIDTH/2, (int)cy, FAMILY_WIDTH, FAMILY_HEIGH);
	        	graphics.drawRect(cxStep-FAMILY_WIDTH/2, (int)cy, FAMILY_WIDTH, FAMILY_HEIGH);
	        	
	        	
	        	if(husband_or_wife_first == 0){
	        		renderWife(graphics, step, cxStep, (int)cy);
	        		renderHusband(graphics, step, cxStep, (int)cy + SPACE_BETWEEN_LINES*2);
	        	} else {
	        		renderHusband(graphics, step, cxStep, (int)cy);
	        		renderWife(graphics, step, cxStep, (int)cy + SPACE_BETWEEN_LINES*2);
	        	}
	        	
	        	
	        	// Marriage if it does exist
	        	if(step.famWhereSpouse != null 
	        			&& step.famWhereSpouse.getMarriageDate()!=null){

	        		centerString(graphics, getMarriageLine(step),(int)cxStep, (int)cy + SPACE_BETWEEN_LINES*5);
	        	}
	        }

	        /* ------------- */
			/**
			 * render the wife of a step in a bloc
			 * @param graphics
			 * @param step
			 * @param cxStep
			 */
			private void renderWife(Graphics2D graphics, Step step, int cxStep, int cyStep) {
				if(step.getWife() != null){
		        	 if(step.linkSex == FEMALE){
		        		 graphics.setFont(boldFontStyle);
		        		 if(use_colors){
		        			 graphics.setPaint(Color.MAGENTA);
		        		 }
		        		 centerString(graphics, getNameLine(step.getWife()), cxStep, cyStep + SPACE_BETWEEN_LINES);
			        		graphics.setFont(plainFontStyle);
			        		graphics.setPaint(Color.BLACK);
		        	 } else{
		        		 centerString(graphics, getNameLine(step.getWife()), cxStep, cyStep + SPACE_BETWEEN_LINES);
		        	 }
		        	 graphics.setFont(dateFontStyle);
		        	 centerString(graphics, getDateLine(step.getWife()), cxStep, cyStep + SPACE_BETWEEN_LINES*2);
		        	 graphics.setFont(plainFontStyle);
	        	}
			}

			/* ------------- */
			/**
			 * render the husband of a step in a bloc
			 * @param graphics
			 * @param step
			 * @param cxStep
			 */
			private void renderHusband(Graphics2D graphics, Step step, int cxStep, int cyStep) {
				if(step.getHusband() != null){
	        		if(step.linkSex == MALE){
		        		graphics.setFont(boldFontStyle);
		        		if(use_colors){
		        			graphics.setPaint(Color.BLUE);
		        		}
		        		centerString(graphics, getNameLine(step.getHusband()), cxStep, cyStep + SPACE_BETWEEN_LINES);
		        		graphics.setFont(plainFontStyle);
		        		graphics.setPaint(Color.BLACK);
		        	}else{
		        		centerString(graphics, getNameLine(step.getHusband()), cxStep, cyStep + SPACE_BETWEEN_LINES);
		        	}
	        		graphics.setFont(dateFontStyle);
		        	 centerString(graphics, getDateLine(step.getHusband()), cxStep, cyStep + SPACE_BETWEEN_LINES*2);
		        	 graphics.setFont(plainFontStyle);
	        	}
			}
	        
	        /* ----------------- */
	        /**
	         * build the name line for an Indi, (ie) his name and id<br/>
	         * if the option "do not display ids" is selected, only the name is returned
	         * @param indi the indi to build the name line for
	         * @return the name line formatted as follow : "name [indi id]"
	         */
	        private String getNameLine(Indi indi){
	        	StringBuffer sb =  new StringBuffer(indi.getFirstName())
	        				.append(" ")
	        				.append(indi.getLastName());
	        				
		        	if(display_ids){
		        		sb.append(" [")
		        		.append(indi.getId())
		        		.append("]");
		        	}
	        	return sb.toString();
	        }
	        
	        /* ----------------- */
	        /**
	         * build the date line for an Indi, (ie) his birth and death dates<br/>
	         * if the option "do not display recent years" is selected, builds a blank line instead of the dates
	         * @param indi the indi to build the date line for
	         * @return the date line formatted as follow : "(birth date - death date)"
	         */
	        private String getDateLine(Indi indi){
	        	StringBuffer sb =  new StringBuffer();
	        	
	        	if(displayRecentYears 
	        			|| indi.getDeathDate(true).getStart().getYear()<YEAR_LIMIT  
	        			|| indi.getBirthDate(true).getStart().getYear()<YEAR_LIMIT){
	        		sb.append("("+indi.getBirthDate(true))
		        	.append(" - ")
		        	.append(indi.getDeathDate(true))
		        	.append(")");
	        	}

	        	return sb.toString();
	        }
	        
	        /* ----------------- */
	        /**
	       * build the marriage line for a step, (ie) the marriage date<br/>
	         * if the option "do not display recent years" is selected, builds a blank line instead of the whole marriage line
	         * @param step the couple to build the marriage line for
	         * @return the marriage line formatted as follow : "marriage on : marriage date [couple ID]")
	         */
	        private String getMarriageLine(Step step){
	        	StringBuffer sb = new StringBuffer();
	        	
	        	if(displayRecentYears || step.famWhereSpouse.getMarriageDate(true).getStart().getYear()<YEAR_LIMIT){

		        	sb.append(translate("marriage.date"))
								        	.append(" ")
								        	.append(step.famWhereSpouse.getMarriageDate(true));
		        	
		        	if(display_ids){
		        		sb.append(" [")
		        		.append(step.famWhereSpouse.getId())
		        		.append("]");
		        	}
	        	}

				return sb.toString();
	        }
	        
	        /* ----------------- */
	        /**
	         * the title line atop the diagram
	         * @param indi the first indi selected by the user
	         * @param other the second indi selected by the user
	         * @param generationCount the number of generations between the two selected indis
	         * @return
	         */
	        private String getTitleLine(Indi indi, Indi other, int generationCount){
	        	//TODO deal with cases when generationCount =1 or 2 or when one is ascendant from the other
	        	String[] args = {getNameLine(indi), getNameLine(other)};
	        	return translate("title",args);
	        }
	        

	        /* ----------------- */
	        /**
	         * Outputs a centered string.
	         */
	        private void centerString(Graphics2D graphics, String text, int x, int y) {
	            Rectangle2D rect = graphics.getFont().getStringBounds(text,
	                    graphics.getFontRenderContext());
	            int width = (int)rect.getWidth();
	            graphics.drawString(text, x - width/2, y);
	        }

	        /* ----------------- */
	        /**
	         * Returns the number of generations that will be displayed.
	         */
	        private int getGenerationCount(Indi indi, int max) {
	            if (indi == null)
	                return -1;
	            if (max == 0)
	                return 0;
	            Fam family = indi.getFamilyWhereBiologicalChild();
	            if (family != null) {
	                int g1 = getGenerationCount(family.getHusband(), max - 1) + 1;
	                int g2 = getGenerationCount(family.getWife(), max - 1) + 1;
	                if (g2 > g1)
	                    return g2;
	                else
	                    return g1;
	            }
	            return 0;
	        }
	    }
}
