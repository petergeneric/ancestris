package ancestris.reports.familytex;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */


import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.TagPath;
import genj.report.Report;
import org.openide.util.lookup.ServiceProvider;

// import tree.ReportGraphicalTree;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * @author Ekran, based on work of Carsten Muessig <carsten.muessig@gmx.net>
 * @version $Revision: 1.8 $
 * @modified by $Author: lukas0815 $, Ekran
 * updated   = $Date: 2009-07-25 20:46:42 $
 */

@ServiceProvider(service=Report.class)
public class ReportFamilyTex extends Report {

    public boolean reportParents = true;
    public boolean reportOtherSpouses = true;
    public boolean reportDetailedChildrenData = true;


    public boolean reportNumberFamilies = true;
    public boolean reportPages = false;
	public boolean reportNumberIndi = true;
	// public boolean reportSymbolFamilies = true;	// not used,
	// public boolean reportSymbolIndi = true;		// not used,
	public boolean reportNoteFam    = true;
	public boolean reportNoteIndi   = false;
	public boolean reportNoteDeath  = true;
	public boolean reportNoteBirth  = true;
	public boolean reportDetailOccupation  = true;
	public boolean reportFamiliyImage  = false;  
	public boolean reportTexHeader  = false;

	public boolean reportsubsection_on_newpage  = false;

    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) {
    	String str = "";


	  // Header for TEX File
	  if (reportTexHeader == true){
		  println("\\documentclass[10pt,a4paper]{article}");
		  println("\\usepackage[T1]{fontenc}");
		  println("\\usepackage[latin1]{inputenc}");
		  println("\\usepackage{float}" );
		  println("%\\usepackage{ngerman} % or use: \\usepackage[francais]{babel}");
		  println("\\usepackage[pdftex]{graphicx}");
		  println("\\DeclareGraphicsExtensions{.jpg,.pdf,.png} % for pdflatex");
		  println("\\usepackage{subfig} % for subfloat");
		  println("\\usepackage{fancybox}");
		  println("\\usepackage[	pdftex,");
		  println("		colorlinks,");
		  println("		linkcolor=black]");
		  println("		{hyperref}");
		  println("\n\\newcommand{\\Bild}[3]{%");
		  println("\\begin{figure}[H]%");
		  println("\\includegraphics[width=120mm]{#2}%");
		  println("\\caption{#3 \\\\ {\\tiny (#2)}}%");
		  println("\\label{pic_#1}%\n\\end{figure}%\n}");
		  println("\n\\newcommand{\\Bildh}[3]{%");
		  println("\\begin{figure}[H]%");
		  println("\\includegraphics[height=160mm]{#2}%");
		  println("\\caption{#3 \\\\ {\\tiny (#2)}}%");
		  println("\\label{pic_#1}%");
		  println("\\end{figure}%");
		  println("}");
		  println("\n%Notes are from 4 sources: Birth, death, persons, families");
		  println("%with the next options you can select how the notes are printed");
		  println("%\\newcommand{ \\NoteBirth }[1]{ \\footnote{ #1 } }");
		  println("\\newcommand{ \\NoteBirth }[1]{, Notiz: #1 }");
		  println("%\\newcommand{ \\NoteBirth }[1]{ \\\\ \\leftskip=12mm Notiz: #1 }");
		  println("\n%\\newcommand{ \\NoteDeath }[1]{ \\footnote{ #1 } }");
		  println("\\newcommand{ \\NoteDeath }[1]{, Notiz: #1  }");
		  println("\n%\\newcommand{ \\NoteIndi  }[1]{ \\footnote{ #1 } }");
		  println("\\newcommand{ \\NoteIndi  }[1]{ \\\\ \\leftskip=12mm Notiz: #1 }");
		  println("\n% \\newcommand{ \\NoteFam   }[1]{ \\\\ \\leftskip=0mm Notiz zur Familie: #1 } \\par");
		  println("\\newcommand{ \\NoteFam   }[1]{ \\footnote{ #1 } }");
		  println("\n%\\newcommand{\\zeile}[2]{\\hspace*{#1}\\begin{minipage}[t]{\\textwidth} #2 \\end{minipage}\\\\}");
		  println("\n\\begin{document}");
		  println("");
		  println("\n\n\\title{Title}");
		  println("\\author{your name \\and your helper}");
		  println("% \\thanks{to all suporter}");
		  println("\\date{ \\today }\n\n\\restylefloat{figure}");
		  println("\n\\maketitle");
		  println("\n\\section{Introduction}\nsome words ...");
		  println("\n\\subsection{Used symbols}");
		  println("The following symbols are used for the events:\\\\");
		  println(TexEncode(OPTIONS.getBirthSymbol()) + " - Birth \\\\");
		  println(TexEncode(OPTIONS.getDeathSymbol()) + " - Death \\\\");
		  println(TexEncode(OPTIONS.getMarriageSymbol()) + " - Marriage \\\\");
		  println(TexEncode(OPTIONS.getOccuSymbol()) + " - Occupation \\\\");
		  println(TexEncode(OPTIONS.getChildOfSymbol()) + " - Child in Family\\\\");
		  println(TexEncode(OPTIONS.getBaptismSymbol()) + " - Baptism \\\\");
		  println("\n\\section{Families}");
		  println("\n\n\\parindent0mm");
		  
	  }
      // include head.tex if exists
	  println("\n\n\\IfFileExists{head}{\\input{head}}\n\n");
	  
      Entity[] fams = gedcom.getEntities(Gedcom.FAM,"");
      for(int i=0; i<fams.length; i++) {
          analyzeFam((Fam)fams[i]);
      }
      
      // include foot.tex if exists
      println("\n\n\\IfFileExists{foot}{\\input{foot}}\n");
      
	  // Footer for TEX File
	  if (reportTexHeader == true){
		  println("\\tableofcontents");
		  println("\\end{document}");
	  }

    }

    /**
     * Main for argument Family
     */
    public void start(Fam fam) {
      analyzeFam(fam);
    }

    private String trim(Object o) {
        if(o == null)
            return "";
        return o.toString();
    }

	/**
	 * Function deletes or modify some characters which cause malfunction of tex
	 */
    private String TexEncode(String str) {
    	// discussed at http://genj.sourceforge.net/forum/viewtopic.php?p=5841
    	// replace characters acording to 
    	// http://www.ctan.org/tex-archive/info/symbols/comprehensive/symbols-a4.pdf
    	// UTF-8 database: http://www.sql-und-xml.de/unicode-database/
    	// Attention: to get a single \ in the tex mode, you must follow 
    	// the string convetions and write \\  ('\' is used as escape for special
    	// characters like \n, \t, \r, ...
    	// within string: \ --> \\
    	//                " --> \"
    	String out;
    	
    	// 
    	// working:
    	// special characters
    	out = str.replaceAll("[\\\\]", "\\\\textbackslash");		// very first becaus all other command add some '\'
    	out = out.replaceAll("[%]", "\\\\%");
    	out = out.replaceAll("[{]", "\\\\{");
    	out = out.replaceAll("[}]", "\\\\}"); 
    	out = out.replaceAll("\\\u005B", "\\\\[");				// [
    	out = out.replaceAll("\\\u005D", "\\\\]");    				// ]
    	// out = out.replaceAll("[\"]", "\\\\grqq{}"); // have to be befor any \"{a} ... 
    	out = out.replaceAll("[_]", "\\\\_");
        out = out.replaceAll("[#]", "\\\\#");
        out = out.replaceAll("[&]", "\\\\&");
        out = out.replaceAll("<", "\\\\textless{}");
    	out = out.replaceAll(">", "\\\\textgreater{}");
    	out = out.replaceAll("\u00A7", "\\\\S");	// §
    	out = out.replaceAll("(\\$)", "\\\\\\$");  // $

        // german
        out = out.replaceAll("\u00DF", "\\\\ss{}");
    	out = out.replaceAll("\u00c4", "\\\\\"{A}");
    	out = out.replaceAll("\u00d6", "\\\\\"{O}");
    	out = out.replaceAll("\u00dc", "\\\\\"{U}");
    	out = out.replaceAll("\u00e4", "\\\\\"{a}");
    	out = out.replaceAll("\u00f6", "\\\\\"{o}");
    	out = out.replaceAll("\u00fc", "\\\\\"{u}");
  	
    	// circumflex, grave, circle
    	out = out.replaceAll("\u00E8", "\\\\`{e}");		// è
    	out = out.replaceAll("\u00e9", "\\\\'{e}");		// é
    	out = out.replaceAll("\u00EA", "\\\\^{e}");	// ê
    	out = out.replaceAll("\u00EB", "\\\\\"{e}");	// ë
    	out = out.replaceAll("\u00C9", "\\\\'{E}");	// É
    	out = out.replaceAll("\u00C8", "\\\\`{E}"); // È
    	out = out.replaceAll("\u00CA", "\\\\^{E}"); // Ê 
    	
       	out = out.replaceAll("\u00E2", "\\\\^{a}");	// â
    	out = out.replaceAll("\u00E1", "\\\\'{a}");	// á
    	out = out.replaceAll("\u00E0", "\\\\`{a}");	// à
    	out = out.replaceAll("\u00C2", "\\\\^{A}");	// Â
    	out = out.replaceAll("\u00E6", "\\\\ae");	// æ
    	out = out.replaceAll("\u00C6", "\\\\AE");	// Æ
    	
    	out = out.replaceAll("\u00E7", "\\\\c{c}");	// ç
    	out = out.replaceAll("\u00C7", "\\\\c{C}");	// Ç
    	
    	out = out.replaceAll("\u0153", "\\\\oe");	// œ
    	out = out.replaceAll("\u0152", "\\\\OE");	// Œ
    		
    	
    	
    	out = out.replaceAll("\u00EF", "\\\\\"{i}");	// ï
    	out = out.replaceAll("\u00ED", "\\\\'{i}");	// í
    	out = out.replaceAll("\u00EC", "\\\\`{i}");	// ì
    	out = out.replaceAll("\u00EE", "\\\\^{i}");	// î
    	out = out.replaceAll("\u00CF", "\\\\\"{I}"); // Ï
    	out = out.replaceAll("\u00CE", "\\\\^{I}");	// Î
    	
    	out = out.replaceAll("\u00F3", "\\\\'{o}");	// ó
    	out = out.replaceAll("\u00F2", "\\\\`{o}");	// ò
    	out = out.replaceAll("\u00F4", "\\\\^{o}");	// ô

    	out = out.replaceAll("\u00FA", "\\\\'{u}");	// ú
    	out = out.replaceAll("\u00F9", "\\\\`{u}");	// ù
    	out = out.replaceAll("\u00FB", "\\\\^{u}");	// û
    	
    	out = out.replaceAll("\u00F1", "\\\\~{n}");	// ñ
    	out = out.replaceAll("\u00D1", "\\\\~{N}");	// Ñ
    	
    	out = out.replaceAll("\u00E5", "\\\\r{a}");	// å
    	out = out.replaceAll("\u00C5", "\\\\r{A}");	// Å
    	
    	// new or untested symbols
    	
    	out = out.replaceAll("\u00B0", "\\\\degree");	// ° (Degree sign)
    	out = out.replaceAll("\\*", "\\\\textasteriskcentered");	// * (asterix)
    	
    	// ToDo:
    	// poland:
    	// l and s    	

    	return out;
    }

	/**
	 * Function prints the command for indent a line
	 */
	private String getIndentTex( int i) {

		// problem: only one line is indented
		String str = "\\leftskip=";
		str = str + (6*(i-1)) + "mm ";

		return str;
	}


	/**
	 * Function prints the command for a note
	 * specify in the header (or tex output) how the note should be shown
	 */
	private String familyNote(Fam f) {
		if(!reportNoteFam) 
		{ 
			return "";
		}
		String str = "";

		for(int n = 0; n < f.getProperties("NOTE").length; n++) {
			str += "\\NoteFam{";
			str += TexEncode(trim(f.getProperties("NOTE")[n]));
			str += "} ";
		}
        return str;
    }

	/**
	 * Function prints the command for a note
	 * specify in the header (or tex output) how the note should be shown
	 */
	private String BirthNote(Indi i) {
		if (!reportNoteBirth)
		{
			return "";
		}
		String str = "";

		// for(int n = 0; n < i.getProperty(new TagPath("INDI:BIRTH:NOTE")).length; n++) { // f.getProperty(new TagPath("FAM:MARR:PLAC"))
			str += trim(i.getProperty(new TagPath("INDI:BIRT:NOTE")));
			if (str.length() <1)
				return "";
			str = "\\NoteBirth{"+ TexEncode(str) +"} ";
		// }

        return str;
    }

	/**
	 * Function prints the command for a note
	 * specify in the header (or tex output) how the note should be shown
	 */
	private String DeathNote(Indi i) {
        if( ! reportNoteDeath) {
			return "";
			}
		String str = "";

		// for(int n = 0; n < i.getProperty(new TagPath("INDI:BIRTH:NOTE")).length; n++) { // f.getProperty(new TagPath("FAM:MARR:PLAC"))
			str += trim(i.getProperty(new TagPath("INDI:DEAT:NOTE")));
			if (str.length() <1)
				return "";
			str = "\\NoteDeath{"+ TexEncode(str) + "} ";
		// }
        return str;
    }

	private String ID_of_Family(Fam f)
	{
		if (reportNumberFamilies==true)
		{
			return TexEncode(f.getId())+" ";
		}
		else
		{
			return "";
		}

	}
	private String Name_of_Husband(Fam f)
	{
		String str="";
	
		str = f.getHusband().getName();
		if (reportNumberIndi==true)
		{
			str += " " + f.getHusband().getId();
		}
		
		return TexEncode(str); 
	}
	
	private String Name_of_Wife(Fam f)
	{
		String str="";
		
		str = f.getWife().getName();
		if (reportNumberIndi==true)
		{
			str += " " + f.getWife().getId();
		}		
		return TexEncode(str); 
	}
	
	private String Name_of_Indi(Indi Indi)
	{
		String str="";

		
		str = Indi.getName();
		if (reportNumberIndi==true)
		{
			str += " " + Indi.getId();
		}		
		return TexEncode(str); 
	}


	/**
	 * Function prints the names of husband and wife
	 */
	private String familyToString(Fam f) {
		Indi husband = f.getHusband(), wife = f.getWife();
		String str = "\\hyperlink{"+f.getId()+"}{"+ ID_of_Family(f);

		if(husband!=null)
			str = str + Name_of_Husband(f);
		if(husband!=null & wife!=null)
			str = str + " " + TexEncode(translate("and")) + " ";
		if(wife!=null)
			str = str + Name_of_Wife(f);

		if (reportPages)
			str = str + "(" + TexEncode(translate("Chap")) +  ". \\ref*{"+f.getId()+"}, S. \\pageref*{"+f.getId()+"})";
		str += "}";
        return str;
    }

	/**
	 * Function prints the names of husband and wife as subsection
	 */
    private String familyToStringSubsection(Fam f) {
    	Indi husband = f.getHusband(), wife = f.getWife();

        String str = "\\leftskip=0mm ";
        
        if (reportsubsection_on_newpage == true) {
        	str += "\\newpage ";
        }
        
        str += "\\subsection{"+ ID_of_Family(f);

        if(husband!=null)
            str = str + Name_of_Husband(f);
        if(husband!=null & wife!=null)
        	str = str + " " + TexEncode(translate("and")) + " ";
        if(wife!=null)
            str = str + Name_of_Wife(f);

        str += "} \n\\hypertarget{"+f.getId()+"}{}\n\\label{"+f.getId()+"}";
        return str;
    }

	/**
	 * Function prints the caption of the picture for each family
	 */

	private String familyImageCaption(Fam f) {
        // ToDo: TexEncode(str)
		String str = "\n";
		Indi husband = f.getHusband(), wife = f.getWife();
		str += "Stammbaum der Famile " + ID_of_Family(f);
		if(husband!=null)
			str = str + Name_of_Husband(f);
		if(husband!=null & wife!=null)
			str = str + " " + translate("and") + " ";
		if(wife!=null)
			str = str + Name_of_Wife(f);

		return str;

	}


	/**
	 * Function prints the picture for each family
	 */
	private String familyImage(Fam f) {
        // ToDo: TexEncode(str)
		// Indi husband = f.getHusband();
		Indi husband = f.getHusband(), wife = f.getWife();
		String str = "\n";

		if(husband!=null){
			str += "\\IfFileExists{"+husband.getId()+".pdf}{"; // Picture for husband
			str += "\\Bild{Bild_"+husband.getId()+"}{"+husband.getId()+".pdf}{" + familyImageCaption(f) + "}}\n";
		}
        return str;
	}

	/**
	 * Function prints the data for family
	 */
    private void analyzeFam(Fam f) {
    	// ToDo: TexEncode(str)
		String str = "";
		println(familyToStringSubsection(f));
		if (reportFamiliyImage == true) {
			println(familyImage(f));
		}

		// str = getIndentTex(1)+familyToString(f); // same as subsection!
        println(str + familyNote(f));

        if( (trim(f.getMarriageDate()).length()>0) || (trim(f.getProperty(new TagPath("FAM:MARR:PLAC"))).length()>0) )
            println(getIndentTex(1)+TexEncode(OPTIONS.getMarriageSymbol()+" "+trim(f.getMarriageDate())+" "+trim(f.getProperty(new TagPath("FAM:MARR:PLAC"))))+"\\par");
        analyzeIndi(f.getHusband(), f);
        analyzeIndi(f.getWife(), f);
        analyzeChildren(f);
    }

	/**
	 * Function prints the datas for a person
	 */
    private void analyzeIndi(Indi indi, Fam f) {
    	// ToDo: TexEncode(str)
        if(indi==null)
            return;

		String str;
		println( getIndentTex(2) + Name_of_Indi(indi) + "\\par");

        // println(str.replaceAll("[_]", " "));
        // println(str);

        if(reportParents) {
          Fam fam = indi.getFamilyWhereBiologicalChild();
            if(fam!=null)
                println(getIndentTex(3)+TexEncode(OPTIONS.getChildOfSymbol()) + " "+familyToString(fam) + "\\par");
        }

        if( (trim(indi.getBirthAsString()).length()>0) || (trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))).length()>0) ) {
            str = OPTIONS.getBirthSymbol()+" "+trim(indi.getBirthAsString())+" "+trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC")));
            str = TexEncode(str);
			println(getIndentTex(3) + str + BirthNote(indi) + "\\par");
			}

        if(indi.getProperty("DEAT")!=null && ( (trim(indi.getDeathAsString()).length()>0) || (trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC"))).length()>0) ) ) {
            str = OPTIONS.getDeathSymbol()+" "+trim(indi.getDeathAsString())+" "+trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC")));
            str = TexEncode(str);
            println(getIndentTex(3) + str + DeathNote(indi) + "\\par");
		}

        if(reportOtherSpouses) {
            Fam[] families = indi.getFamiliesWhereSpouse();
            if(families.length > 1) {
                println(getIndentTex(3)+translate("otherSpouses")+"\\par");
                for(int i=0; i<families.length; i++) {
                    if(families[i]!=f) {
                        // String str = "";
                        str = "";
                        if((trim(families[i].getMarriageDate()).length()>0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length()>0))
                            str = OPTIONS.getMarriageSymbol()+" "+trim(families[i].getMarriageDate())+" "+trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC")))+" ";
                        str = TexEncode(str);
                        println(getIndentTex(4)+str+" "+familyToString(families[i])+"\\par");
                    }
                }
            }
        }
        if (reportDetailOccupation & trim(indi.getProperty(new TagPath("INDI:OCCU"))).length()>0) {
			str = translate("occupation")+": "+ trim(indi.getProperty(new TagPath("INDI:OCCU"))) ;
			str = TexEncode(str);
			println(getIndentTex(3) + str + "\\par");
		}
    }

    
	/**
	 * Function prints the data for the children
	 */
    private void analyzeChildren(Fam f) {

        Indi[] children = f.getChildren();
        Indi child;
        Fam[] families;
        Fam family;
		String str = "";

        if(children.length>0)
            println(getIndentTex(2) + translate("children") + "\\par");
        for(int i=0; i<children.length; i++) {
            child = children[i];
            str = Name_of_Indi(child); // (reportNumberIndi==true?(String)child:(String)child.getName());
            println(getIndentTex(3) + str + "\\par");
            if(reportDetailedChildrenData) {
                if ( (trim(child.getBirthAsString()).length()>0) || (trim(child.getProperty(new TagPath("INDI:BIRT:PLAC"))).length()>0) ) {
                    str = OPTIONS.getBirthSymbol()+" ";
                    str += trim(child.getBirthAsString())+" ";
                    str += trim(child.getProperty(new TagPath("INDI:BIRT:PLAC")));
					str = TexEncode(str);
					println(getIndentTex(4) + str + BirthNote(child) + "\\par");
				}
                printBaptism(child, "BAPM");
                printBaptism(child, "BAPL");
                printBaptism(child, "CHR");
                printBaptism(child, "CHRA");
                families = child.getFamiliesWhereSpouse();
                for(int j=0; j<families.length; j++) {
                    family = families[j];
                    // println(getIndentTex(4)+OPTIONS.getMarriageSymbol()+family+" "+trim(family.getMarriageDate())+" "+trim(family.getProperty(new TagPath("FAM:MARR:PLAC")))+"\\par");

                    str = trim(family.getMarriageDate())+" ";
                    str += trim(family.getProperty(new TagPath("FAM:MARR:PLAC")));
					str = TexEncode(str);
					println(getIndentTex(4) + OPTIONS.getMarriageSymbol() + " " + familyToString(family) + " " + str + "\\par");                    
                }
                if (reportDetailOccupation & trim(child.getProperty(new TagPath("INDI:OCCU"))).length()>0) {
					str = translate("occupation")+": ";
					str += trim(child.getProperty(new TagPath("INDI:OCCU")));
					str = TexEncode(str);
					println(getIndentTex(4) + str + "\\par");
				}
                if(child.getProperty("DEAT")!=null && ( (trim(child.getDeathAsString()).length()>0) || (trim(child.getProperty(new TagPath("INDI:DEAT:PLAC"))).length()>0) ) ) {
                    str = OPTIONS.getDeathSymbol()+" ";
                    str += trim(child.getDeathAsString())+" ";
                    str += trim(child.getProperty(new TagPath("INDI:DEAT:PLAC")));
                    str = TexEncode(str);
					println(getIndentTex(4) + str + DeathNote(child) + "\\par");
				}
            }
        }
    }

	/**
	 * Function prints the infos for Baptism
	 */
    private void printBaptism(Indi indi, String tag) {
    	String str = "";
        if( (indi.getProperty(tag)!=null) 
        	&& (
        			(trim(indi.getProperty(new TagPath("INDI:"+tag+":DATE"))).length()>0) 
        			|| (trim(indi.getProperty(new TagPath("INDI:"+tag+":PLAC"))).length()>0) 
        		) 
        	) {
	        	
	        	str = OPTIONS.getBaptismSymbol() + " (" + tag;
	        	str += "): ";
	        	str += trim(indi.getProperty(new TagPath("INDI:"+tag+":DATE")))+" ";
	        	str += trim(indi.getProperty(new TagPath("INDI:"+tag+":PLAC")));
	        	str = TexEncode(str);
	        	println(getIndentTex(4) + str + "\\par");
	        }
    }

} //ReportFamily
