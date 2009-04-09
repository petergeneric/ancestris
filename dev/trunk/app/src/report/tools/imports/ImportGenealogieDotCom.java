/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * sur une base de gedrepohr.pl (Patrick TEXIER) pour la correction des REPO
 * Le reste des traitements par Daniel ANDRE 
 */

package tools.imports;
import genj.report.Report;
import genj.util.swing.Action2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The import function for Heredis originated Gedcom files 
 */
public class ImportGenealogieDotCom {

	static final String typerepo = "REPO"; // Debut de la cle REPO dans le
	// gedcom
	static String EOL = System.getProperty("line.separator");

	private static final Hashtable<String, String> repmonconvtable = new Hashtable<String, String>()
	{{put("I","1");
	put("II","2");
	put("III","3");
	put("IV","4");
	put("V","5");
	put("VI","6");
	put("VII","7");
	put("VIII","8");
	put("IX","9");
	put("X","10");
	put("XI","11");
	put("XII","12");
}};

static Pattern pattern = Pattern.compile("^1 REPO (.*)");
static Pattern tag_y = Pattern.compile("^(\\d) "+
		"(BIRT|CHR|"+
"DEAT|BURI|CREM|"+
  "ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|"+
  "CENS|PROB|WILL|"+
  "GRAD|RETI|"+
  "ANUL|CENS|DIV|DIVF|"+
  "ENGA|MARR|MARB|MARC|"+
  "MARL|MARS"+")(.*)");
static Pattern tag_valid = Pattern.compile(
		"(ABBR|ADDR|ADR1|ADR2|ADOP|AFN|AGE|AGNC|ALIA|ANCE|ANCI|ANUL|ASSO|AUTH|"+
		"BAPL|BAPM|BARM|BASM|BIRT|BLES|BLOB|BURI|CALN|CAST|CAUS|CENS|CHAN|CHAR|"+
		"CHIL|CHR|CHRA|CITY|CONC|CONF|CONL|CONT|COPR|CORP|CREM|CTRY|DATA|DATE|"+
		"DEAT|DESC|DESI|DEST|DIV|DIVF|DSCR|EDUC|EMIG|ENDL|ENGA|EVEN|FAM|FAMC|"+
		"FAMF|FAMS|FCOM|FILE|FORM|GEDC|GIVN|GRAD|HEAD|HUSB|IDNO|IMMI|INDI|LANG|"+
		"LEGA|MARB|MARC|MARL|MARR|MARS|MEDI|NAME|NATI|NATU|NCHI|NICK|NMR|NOTE|"+
		"NPFX|NSFX|OBJE|OCCU|ORDI|ORDN|PAGE|PEDI|PHON|PLAC|POST|PROB|PROP|PUBL|"+
		"QUAY|REFN|RELA|RELI|REPO|RESI|RESN|RETI|RFN|RIN|ROLE|SEX|SLGC|SLGS|"+
		"SOUR|SPFX|SSN|STAE|STAT|SUBM|SUBN|SURN|TEMP|TEXT|TIME|TITL|TRLR|TYPE|"+
		"VERS|WIFE|WILL"+
		")");
static Pattern gedcom_line = Pattern.compile("^(\\d) (_*\\w+)(.*)");
static Pattern french_cal = Pattern.compile("(\\d)( DATE @#DFRENCH R@ )(.*)");
static Pattern date_value = Pattern.compile("(FROM|BEF|AFT|BET|INT|TO) (.*)");
static Pattern date_range= Pattern.compile("(FROM|BEF|AFT|BET|INT|TO) (.*) (TO|AND) (.*)");
static Pattern french_date = Pattern.compile("(.*) an (\\w*)(.*)");
// Traiter FROM ... TO
// BET ... AND
// <date>
// from <date>
// to <date>
// from <date> to <date>
// bef <date>
// aft <date>
// bet <date> and <date>
// int <date>

private static int clerepo;
	private static Hashtable<String, Integer> hashrepo;
	private static StringBuilder sb;
	
	/** our calling report */
  private Report report = null;

  /** our files */
  private File fileIn  = null;
  private File fileOut = null;
private String inputEncoding;

  /**
   * Constructor
   */
  public ImportGenealogieDotCom(Report report, File fileIn) {

    this.report = report;
    this.fileIn = fileIn;
    fileOut = report.getFileFromUser("Veuillez choisir le fichier de sortie", Action2.TXT_OK,true,"ged");
    
    clerepo = 0;
	hashrepo = new Hashtable<String, Integer>();
	sb = new StringBuilder();

    }

 /**
   * Gives back output file name
   */
  public String getOutputName() {
    return fileOut == null ? "none" : fileOut.getName();
    }

  private BufferedReader getReader() throws Exception {
		BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn),"LATIN1"));
		return input; 
  }
  private BufferedWriter getWriter() throws Exception {
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"LATIN1"));
		return output; 
}
 /**
   * Executes import from in file to out file
   */
  public boolean run() {
    // import function

	    if (fileOut == null) return false;

	    // on fait une premiere passe sur le fichier pour creer les repos
	    try {
			EOL = getEOL(fileIn);
			BufferedReader input = getReader();
			try {
				String line = null; // not declared within while loop
				/*
				* readLine is a bit quirky :
				* it returns the content of a line MINUS the newline.
				* it returns null only for the END of the stream.
				* it returns an empty String if two newlines appear in a row.
				*/
				while ((line = input.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.matches()) {
						if (!hashrepo.containsKey(matcher.group(1))) {
							clerepo++;
							hashrepo.put(matcher.group(1), clerepo);
							sb.append("0 @" + typerepo + clerepo + "@ REPO"
									+ EOL);
							sb.append("1 NAME " + matcher.group(1) + EOL);
						}
					}
				}
			} finally {
				input.close();
			}
		} catch (FileNotFoundException e1) {
			report.println("fichier " + fileIn.getName() + " introuvable");
			return false;
		} catch (IOException ex) {
			report.println("Erreur de lecture du fichier " + fileIn.getName());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			report.println("Erreur inconnue");
			return false;
		}

		// maintenant on effectue toutes les transformations
		Writer output = null;
		try {
		    output = getWriter();
		} catch (IOException e1) {
			report.println("Creation du fichier " + fileOut.getName() + " impossible");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			report.println("Erreur inconnue");
			return false;
		}
		try {
			BufferedReader input = getReader();
			try {
				String line = null; // not declared within while loop
				/*
				* readLine is a bit quirky :
				* it returns the content of a line MINUS the newline.
				* it returns null only for the END of the stream.
				* it returns an empty String if two newlines appear in a row.
				*/
				while ((line = input.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.matches()) {
						if (hashrepo.containsKey(matcher.group(1))) {
							output.write("1 REPO @" + typerepo
									+ hashrepo.get(matcher.group(1)) + "@"+EOL);
						}
						continue;
					}
					if (line.matches("^0 TRLR")) {
						output.write(sb.toString());
						output.write("0 TRLR"+EOL);
						continue;
					}
					// on ajoute des Y si necessaire
					{
						matcher = tag_y.matcher(line);
						if (matcher.matches()){
							if (matcher.groupCount() == 4)
								output.write(line+EOL);
							else {
								int level = (new Integer(matcher.group(1))).intValue();
								input.mark(100);
								String temp = input.readLine();
								if ((temp != null) && (temp.startsWith(""+(level+1)+" ")))
									output.write(line+EOL);
								else 
									output.write(matcher.group(1)+" "+matcher.group(2)+" Y"+EOL);			
								input.reset();
							}
						continue;
						}
					}
					{
						matcher = gedcom_line.matcher(line);
						if (matcher.matches() && (matcher.groupCount()>2)){
							// C'est un tag perso: on ecrit telque
							if (matcher.group(2).startsWith("_")){
								output.write(line+EOL);
								continue;
							}
							// le tag n'est pas valide: on le prefixe par _
							if (! tag_valid.matcher(matcher.group(2)).matches()){
								output.write(matcher.group(1)+" _"+matcher.group(2)+matcher.group(3)+EOL);
								continue;
							}
						}
					}
					// calendrier repub
					{
						String date_result = "";
						matcher = french_cal.matcher(line);
						if (matcher.matches()&& (matcher.groupCount()>2)){
							// C'est un cal republicain, on essaie d'interpreter
							date_result += matcher.group(1)+" DATE ";
							Matcher m1 = date_range.matcher(matcher.group(3));
							if (m1.matches()){
								date_result += m1.group(1)+" @#DFRENCH R@ "+convDateFormat(m1.group(2));
								date_result += " "+m1.group(3)+" @#DFRENCH R@ "+convDateFormat(m1.group(4));
								output.write(date_result+EOL);
								continue;
							}
							m1 = date_value.matcher(matcher.group(3));
							if (m1.matches()){
								date_result += m1.group(1)+" @#DFRENCH R@ "+convDateFormat(m1.group(2));
								output.write(date_result+EOL);
								continue;
							}
							date_result += "@#DFRENCH R@ "+convDateFormat(matcher.group(3));
							output.write(date_result+EOL);
							continue;
						}
					}
					output.write(line+EOL);
				}
			} finally {
				input.close();
				output.close();
			}

		} catch (FileNotFoundException e1) {
			report.println("fichier " + fileIn.getName() + " introuvable");
			return false;
		} catch (IOException ex) {
			report.println("Erreur de lecture du fichier " + fileIn.getName());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			report.println("Erreur inconnue");
			return false;
		}
	    return true;
	    }

static private String convDateFormat(String from) {
	Matcher m = french_date.matcher(from); 
	if (m.matches() && m.groupCount()>2) {
		String result = m.group(1)+" "+repmonconvtable.get(m.group(2));
		if (m.groupCount()>3)
			result += m.group(3);
		return result;
	}
	return from;
}

private String getEOL(File input) {
	
	String eolMark = System.getProperty("line.separator");


	try {
		FileReader fr = new FileReader(input);
		char[] buffer = new char[200];
		fr.read(buffer);
		
		String line = new String(buffer);
		if (line.contains("\r\n"))
			eolMark = "\r\n";
		else if (line.contains("\n"))
			eolMark = "\n";
		else if (line.contains("\r"))
			eolMark = "\r";
	} catch (IOException e) {
		e.printStackTrace();
	}

	return eolMark ;
}
}
