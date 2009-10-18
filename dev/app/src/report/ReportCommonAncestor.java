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
import java.util.Collections;
import java.util.List;

import tree.Translator;
import tree.graphics.GraphicsOutput;
import tree.graphics.GraphicsOutputFactory;
import tree.graphics.GraphicsRenderer;
import tree.output.RendererFactory;

/**
 * Compute the common ancestor of two individuals and show the descendant chains between the common ancestor and the two selected descendants
 * 
 */
//TODO let the user choose which ancestor to display when there are several
//TODO option landscape or portrait
//TODO write description in .properties
//TODO rewrite title depending on the relationship between indis (cousins, parents...)
//TODO shouldn't we take into account the potential other parents (adoption...)?

/**
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
    
   
    
    private static final int MALE = 1;
    private static final int FEMALE = 2;
    
    private Font boldFontStyle;
    private Font plainFontStyle;
    private Font dateFontStyle;
    private Font titleFontStyle;
    private Font smallFontStyle;
    
    
    /** Whether to use colors (or only black and white). */
    public boolean use_colors = true;
    
    /** Whether to display indi and family ids. */
    public boolean display_ids = true;
	
    public int ufont_name=0;
    public String ufont_names[] = { translate("ufont_name.0"),
				     translate("ufont_name.1"),
				     translate("ufont_name.2"),
				     translate("ufont_name.3"),
				     translate("ufont_name.4"),
				    translate("ufont_name.5")};
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
		Indi indi = indis[0];
		Indi other = indis[1];

		// search the common ancestor
		Indi ancestor = null;
		if(indi.isAncestorOf(other)){
			ancestor = indi;
		}else if (other.isAncestorOf(indi)){
			ancestor = other;
		}else{
			ancestor = getCommonAncestor(indi, other);
		}
		
		// if the common ancestor exists
		if(ancestor!=null){
			List<Step> indiDirectLinks = new ArrayList<Step>();
			indiDirectLinks.add(new Step(getLastFamilyWhereSpouse(indi),indi, indi.getSex()));
			getAncestorListBetween(ancestor, indi, indiDirectLinks);
			Collections.reverse(indiDirectLinks);
			LOG.fine("indi's link number : "+indiDirectLinks.size());
			
			List<Step> otherDirectLinks = new ArrayList<Step>();
		
			otherDirectLinks.add(new Step(getLastFamilyWhereSpouse(other),other, other.getSex()));
			getAncestorListBetween(ancestor, other, otherDirectLinks);
			Collections.reverse(otherDirectLinks);
			LOG.fine("other's link number : "+otherDirectLinks.size());

			GraphicsOutput output = outputs.createOutput(this);
	        if (output == null) // report canceled
	            return;
	        try {
	            output.output(new Renderer(indi, other, indiDirectLinks, otherDirectLinks));
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
	/** finds the most recent family of the given Indi
	 * @param indi the Indi whom family we're looking for
	 * @return his last family
	 */
	private Fam getLastFamilyWhereSpouse(Indi indi){
		Fam[] fams = indi.getFamiliesWhereSpouse();
		if(fams==null || fams.length==0){
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
	private Indi getCommonAncestor(Indi indi, Indi other) {
		// 

		Indi father = indi.getBiologicalFather();
		if (father != null) {
			if (father.isAncestorOf(other))
				return father;
			Indi ancestor = getCommonAncestor(father, other);
			if (ancestor != null)
				return ancestor;
		}
		Indi mother = indi.getBiologicalMother();
		if (mother != null) {
			if (mother.isAncestorOf(other))
				return mother;
			Indi ancestor = getCommonAncestor(mother, other);
			if (ancestor != null)
				return ancestor;
		}
		// none found
		return null;
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
//				System.out.println("the link has no determined sex...");
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

	        private Indi indi;
	        private Indi other;
	        private List<Step> indiDirectLinks;
	        private List<Step> otherDirectLinks;

	       

	        private AffineTransform defaultTransform;

	        private int width;
	        private int height;
	        private double cx;
	        private double cy;

	        /* ----------------- */
	        /**
	         * renderer's constructor. The entry point to the ouput generation
	         * @param indi
	         * @param other
	         * @param indiDirectLinks
	         * @param otherDirectLinks
	         */
	        public Renderer(Indi indi, Indi other, List<Step> indiDirectLinks, List<Step> otherDirectLinks) {
	            this.indi = indi;
	            this.other = other;
	            this.indiDirectLinks = indiDirectLinks;
	            this.otherDirectLinks = otherDirectLinks;

//	            int generations = getGenerationCount(indi, max_generations);
	            width = 3 * FAMILY_WIDTH + FAMILY_WIDTH/2;
	            height = (Math.max(indiDirectLinks.size(), otherDirectLinks.size())) * FAMILY_HEIGH
	            		+ (Math.max(indiDirectLinks.size(), otherDirectLinks.size())+3) * SPACE_BETWEEN_RECTANGLES
	            		+ SPACE_BEFORE_DATE;
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
	            cy += 10;
	            int nbMaxGen = Math.max(indiDirectLinks.size(), otherDirectLinks.size());
	            
	            // the title
	            graphics.setFont(titleFontStyle);
	            centerString(graphics, getTitleLine(indi, other, nbMaxGen), (int)cx, (int)cy+15 );
	            graphics.setFont(plainFontStyle);
	            cy+=SPACE_BETWEEN_TITLE_AND_COMMON_ANCESTOR;
	            
	            //the common ancestor
	            render(graphics, indiDirectLinks.get(0),Position.CENTER);
	            graphics.drawLine((int)cx, (int)cy+FAMILY_HEIGH, (int)cx, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES);
	            if(indiDirectLinks.size()>1){
	            	graphics.drawLine((int)cx-FAMILY_WIDTH, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES, (int)cx, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES);
	            }
	            if(otherDirectLinks.size()>1){
	            	graphics.drawLine((int)cx, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES, (int)cx+FAMILY_WIDTH, (int)cy+FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES);
	            }
	            
	            cy+=SPACE_BETWEEN_RECTANGLES;
	            
	            // the two branches
	            for (int i=1;i<nbMaxGen;i++) {
					cy+=FAMILY_HEIGH+SPACE_BETWEEN_RECTANGLES;
					if(indiDirectLinks.size()>i){
						 graphics.drawLine((int)cx-FAMILY_WIDTH, (int)cy-SPACE_BETWEEN_RECTANGLES, (int)cx-FAMILY_WIDTH, (int)cy);
						render(graphics, indiDirectLinks.get(i),Position.LEFT);
					}
					if(otherDirectLinks.size()>i){
						 graphics.drawLine((int)cx+FAMILY_WIDTH, (int)cy-SPACE_BETWEEN_RECTANGLES, (int)cx+FAMILY_WIDTH, (int)cy);
						render(graphics, otherDirectLinks.get(i),Position.RIGHT);
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
	        private void render(Graphics2D graphics, Step step,Position rightLeft){
	        	
	        	graphics.setPaint(Color.BLACK);
	        	int cxStep=0;
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
	        	
	        	// husband
	        	if(step.getHusband()!=null){
	        		if(step.linkSex==MALE){
		        		graphics.setFont(boldFontStyle);
		        		if(use_colors){
		        			graphics.setPaint(Color.BLUE);
		        		}
		        		centerString(graphics, getNameLine(step.getHusband()), (int)cxStep, (int)cy + SPACE_BETWEEN_LINES);
		        		graphics.setFont(plainFontStyle);
		        		graphics.setPaint(Color.BLACK);
		        	}else{
		        		centerString(graphics, getNameLine(step.getHusband()), (int)cxStep, (int)cy + SPACE_BETWEEN_LINES);
		        	}
	        		graphics.setFont(dateFontStyle);
		        	 centerString(graphics, getDateLine(step.getHusband()), (int)cxStep, (int)cy+SPACE_BETWEEN_LINES*2);
		        	 graphics.setFont(plainFontStyle);
	        	}
	        	 
	        	 // wife
	        	if(step.getWife()!=null){
		        	 if(step.linkSex==FEMALE){
		        		 graphics.setFont(boldFontStyle);
		        		 if(use_colors){
		        			 graphics.setPaint(Color.MAGENTA);
		        		 }
		        		 centerString(graphics, getNameLine(step.getWife()), (int)cxStep, (int)cy + SPACE_BETWEEN_LINES*3);
			        		graphics.setFont(plainFontStyle);
			        		graphics.setPaint(Color.BLACK);
		        	 } else{
		        		 centerString(graphics, getNameLine(step.getWife()), (int)cxStep, (int)cy + SPACE_BETWEEN_LINES*3);
		        	 }
		        	 graphics.setFont(dateFontStyle);
		        	 centerString(graphics, getDateLine(step.getWife()), (int)cxStep, (int)cy + SPACE_BETWEEN_LINES*4);
		        	 graphics.setFont(plainFontStyle);
	        	}
	        	
	        	// Marriage if it does exist
	        	if(step.famWhereSpouse!=null && step.famWhereSpouse.getMarriageDate()!=null){
	        		centerString(graphics, getMariageLine(step),(int)cxStep, (int)cy + SPACE_BETWEEN_LINES*5);
	        	}
	        }
	        
	        /* ----------------- */
	        /**
	         * @param indi
	         * @return
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
	         * @param indi
	         * @return
	         */
	        private String getDateLine(Indi indi){
	        	
	        	return new StringBuffer("("+indi.getBirthDate(true))
				        	.append(" - ")
				        	.append(indi.getDeathDate(true))
				        	.append(")")
				        	.toString();
	        }
	        
	        /* ----------------- */
	        /**
	         * @param step
	         * @return
	         */
	        private String getMariageLine(Step step){
	        	StringBuffer sb = new StringBuffer(translate("marriage.date"))
							        	.append(" ")
							        	.append(step.famWhereSpouse.getMarriageDate(true));
	        	
	        	if(display_ids){
	        		sb.append(" [")
	        		.append(step.famWhereSpouse.getId())
	        		.append("]");
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
