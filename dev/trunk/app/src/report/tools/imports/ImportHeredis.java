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

import genj.io.PropertyReader;
import genj.report.Report;
import genj.util.Resources;
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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The import function for Heredis originated Gedcom files
 */
public class ImportHeredis {

	static final String typerepo = "REPO"; // Debut de la cle REPO dans le
	// gedcom
	static String EOL = System.getProperty("line.separator");


//	static Pattern pattern = Pattern.compile("^1 REPO (.*)");
	static final String INDI_TAG_YES = "BIRT|CHR|" + "DEAT|BURI|CREM|"
			+ "ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|"
			+ "CENS|PROB|WILL|" + "GRAD|RETI|";
	static final String FAM_TAG_YES = "ANUL|CENS|DIV|DIVF|"
			+ "ENGA|MARR|MARB|MARC|" + "MARL|MARS";
	static final String GEDCOM_TAG = "ABBR|ADDR|ADR1|ADR2|ADOP|AFN|AGE|AGNC|ALIA|ANCE|ANCI|ANUL|ASSO|AUTH|"
			+ "BAPL|BAPM|BARM|BASM|BIRT|BLES|BLOB|BURI|CALN|CAST|CAUS|CENS|CHAN|CHAR|"
			+ "CHIL|CHR|CHRA|CITY|CONC|CONF|CONL|CONT|COPR|CORP|CREM|CTRY|DATA|DATE|"
			+ "DEAT|DESC|DESI|DEST|DIV|DIVF|DSCR|EDUC|EMIG|ENDL|ENGA|EVEN|FAM|FAMC|"
			+ "FAMF|FAMS|FCOM|FILE|FORM|GEDC|GIVN|GRAD|HEAD|HUSB|IDNO|IMMI|INDI|LANG|"
			+ "LEGA|MARB|MARC|MARL|MARR|MARS|MEDI|NAME|NATI|NATU|NCHI|NICK|NMR|NOTE|"
			+ "NPFX|NSFX|OBJE|OCCU|ORDI|ORDN|PAGE|PEDI|PHON|PLAC|POST|PROB|PROP|PUBL|"
			+ "QUAY|REFN|RELA|RELI|REPO|RESI|RESN|RETI|RFN|RIN|ROLE|SEX|SLGC|SLGS|"
			+ "SOUR|SPFX|SSN|STAE|STAT|SUBM|SUBN|SURN|TEMP|TEXT|TIME|TITL|TRLR|TYPE|"
			+ "VERS|WIFE|WILL";

	static Pattern tag_y = Pattern.compile("(" + INDI_TAG_YES
			+ FAM_TAG_YES + ")");
	static Pattern tag_valid = Pattern.compile("(" + GEDCOM_TAG + ")");
	static Pattern gedcom_line = Pattern.compile("^(\\d) (_*\\w+)(.*)");
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
	private File fileIn = null;
	private File fileOut = null;
	private String inputEncoding;

	Translate t = new Translate(this);
	/**
	 * Constructor
	 */
	public ImportHeredis(Report report, File fileIn) {


		this.report = report;
		this.fileIn = fileIn;
		fileOut = report.getFileFromUser(
				t.translate("fileout.dlg.title"), Action2.TXT_OK, true,
				"ged");
		if (fileOut == null)
			return;
		if (!fileOut.getName().toLowerCase().endsWith(".ged")) {
			fileOut = new File(fileOut.getAbsolutePath()+".ged");
		}
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

	private GedcomFileReader getReader() throws Exception {
		return new GedcomFileReader(fileIn);
	}

	private GedcomFileWriter getWriter() throws Exception {
		GedcomFileWriter output = new GedcomFileWriter(fileOut, getEOL(fileIn));
		return output;
	}

	public boolean run() {
		// import function

		if (fileOut == null)
			return false;

		// on fait une premiere passe sur le fichier pour creer les repos
		try {
			EOL = getEOL(fileIn);
			GedcomFileReader input = getReader();
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) {
					if ((input.getLevel() == 1) && input.getTag().equals("REPO")) {
						if (!hashrepo.containsKey(input.getValue())) {
							clerepo++;
							hashrepo.put(input.getValue(), clerepo);
							sb.append("0 @" + typerepo + clerepo + "@ REPO"
									+ EOL);
							sb.append("1 NAME " + input.getValue() + EOL);
						}
					}
				}
			} finally {
				input.close();
			}
		} catch (FileNotFoundException e1) {
			report.println(t.translate("file.not.found", fileIn.getName()));
			return false;
		} catch (IOException ex) {
			report.println(t.translate("file.read.error", fileIn.getName()));
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			report.println(t.translate("error.unknown"));
			return false;
		}

		// maintenant on effectue toutes les transformations
		GedcomFileWriter output = null;
		try {
			output = getWriter();
		} catch (IOException e1) {
			report.println(t.translate("file.create.error",fileOut.getName()));
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			report.println(t.translate("error.unknown"));
			return false;
		}
		try {
			GedcomFileReader input = getReader();
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					if ((input.getLevel() == 1) && input.getTag().equals("REPO")) {
						if (hashrepo.containsKey(input.getValue())) {
							output.writeLine(1,"REPO","@" + typerepo
									+ hashrepo.get(input.getValue()) + "@");
							report.println(line);
							report.println("==> "+t.translate("corrected"));
						}
						continue;
					}
					if (input.getTag().equals("TRLR")) {
						output.write(sb.toString());
						output.writeLine(0,"TRLR",null);
						continue;
					}
					// on ajoute des Y si necessaire
					{
						Matcher matcher = tag_y.matcher(input.getTag());
						if (matcher.matches()) {
							if (input.getValue().length() != 0)
								output.writeln(line);
							else {
								int level = input.getLevel();
								String temp = input.guessNextLine();
								if ((temp != null) && (input.getLevel() == level+1)) {
									input.reset();
									output.writeln(line);
								} else {
									input.reset();
									String result = output.writeLine(input.getLevel(),input.getTag(),"Y");
									report.println(line);
									report.println("==> "+ result);
								}
							}
							continue;
						}
					}
						// C'est un tag perso: on ecrit telque
						if (input.getTag().startsWith("_")) {
							output.write(line + EOL);
							continue;
						}
						// le tag n'est pas valide: on le prefixe par _
						if (!tag_valid.matcher(input.getTag()).matches()) {
							String result = output.writeLine(input.getLevel(),"_"+ input.getTag(),input.getValue());
							report.println(line);
							report.println("==> "+ result);
							continue;
						}
					// calendrier repub
						// C'est un tag DATE: on transforme les dates rep
						if (input.getTag().equals("DATE")) {
							String newValue = frenchCalCheck(input.getValue());
							if (newValue != null) {
								String result = output.writeLine(input.getLevel(),input.getTag(),newValue);
								report.println(line);
								report.println("==> "+ result);
								continue;
								
							}
						}
					output.write(line + EOL);
				}
			} finally {
				input.close();
				output.close();
			}

	} catch (FileNotFoundException e1) {
		report.println(t.translate("file.not.found", fileIn.getName()));
		return false;
	} catch (IOException ex) {
		report.println(t.translate("file.read.error", fileIn.getName()));
		return false;
	} catch (Exception e) {
		e.printStackTrace();
		report.println(t.translate("error.unknown"));
		return false;
	}

	report.println(t.translate("end",fileOut));
	return true;
	
	
	
	}

String frenchCalCheck(String in){
	final Pattern french_cal = Pattern.compile("(@#DFRENCH R@ )(.*)");
	final  Pattern date_value = Pattern.compile("(FROM|BEF|AFT|BET|INT|TO) (.*)");
	final  Pattern date_range = Pattern.compile("(FROM|BEF|AFT|BET|INT|TO) (.*) (TO|AND) (.*)");

	String result = "";
	Matcher matcher = french_cal.matcher(in);
	if (matcher.matches() && (matcher.groupCount() > 1)) {
		// C'est un cal republicain, on essaie d'interpreter
		String date_parameter=matcher.group(3);
		Matcher m1 = date_range.matcher(date_parameter);
		if (m1.matches()) {
			result += m1.group(1) + " @#DFRENCH R@ "
					+ convDateFormat(m1.group(2));
			result += " " + m1.group(3)
					+ " @#DFRENCH R@ "
					+ convDateFormat(m1.group(4));
			return result;
		}
		
		m1 = date_value.matcher(date_parameter);
		if (m1.matches()) {
			result += m1.group(1) + " @#DFRENCH R@ "
					+ convDateFormat(m1.group(2));
			return result;
		}
		result += "@#DFRENCH R@ "
			+ convDateFormat(matcher.group(3));
		return result;
	} else 
		return null;
}

	
	
	
	static private String convDateFormat(String from) {
		final Hashtable<String, String> repmonconvtable = new Hashtable<String, String>() {
			{
				put("I", "1");
				put("II", "2");
				put("III", "3");
				put("IV", "4");
				put("V", "5");
				put("VI", "6");
				put("VII", "7");
				put("VIII", "8");
				put("IX", "9");
				put("X", "10");
				put("XI", "11");
				put("XII", "12");
			}
		};
		final  Pattern french_date = Pattern.compile("(.*) an (\\w*)(.*)");
		Matcher m = french_date.matcher(from);
		if (m.matches() && m.groupCount() > 2) {
			String result = m.group(1) + " " + repmonconvtable.get(m.group(2));
			if (m.groupCount() > 3)
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

		return eolMark;
	}

	private class GedcomFileReader extends PropertyReader {
		  private int saved_level;
		  private String saved_tag;
		  private String saved_xref;
		  private String saved_value;
		  
		  private String tags[] = new String[10];
		  
		public GedcomFileReader(File filein)
				throws UnsupportedEncodingException, FileNotFoundException {
			super(new InputStreamReader(new FileInputStream(fileIn), "LATIN1"),
					null, false);
			// TODO Auto-generated constructor stub
		}

		public String getValue() {
			return value;
		}

		public int getLevel() {
			return level;
		}

		public String readLine() throws IOException {
			readLine(false);
			String result = line;
			line = null;
			return result;
		}

		public String guessNextLine() throws IOException{
saved_level = level;
saved_tag = tag;
saved_xref = xref;
saved_value = value;
			readLine(false);
			return line;
		}
		public boolean readLine(boolean consume) throws IOException {
			boolean b = super.readLine(consume);
			tags[level]=tag;
			return b;
		}

		public void reset(){
			level = saved_level;
			tag = saved_tag;
			xref = saved_xref;
			value = saved_value;
		}
		
		void close() throws IOException {
			in.close();
		}

		String getTag() {
			return tag;
		}

		String getLine() {
			return "" + level + " " + tag + " " + value;
		}
	}

	private class GedcomFileWriter extends BufferedWriter {
		String EOL = System.getProperty("line.separator");

		public GedcomFileWriter(File filein, String eol)
				throws UnsupportedEncodingException, FileNotFoundException {
			super(new OutputStreamWriter(new FileOutputStream(fileOut),
					"LATIN1"));
			EOL = eol;
		}

		void writeln(String line) throws IOException { 
			write(line);
			write(EOL);
		}
		String writeLine(int level, String tag, String value) throws IOException {
			
			String result = Integer.toString(level)+" "+tag;

			// Value
			if (value != null && value.length() > 0) {
				result += " "+value;
			}
			write(result+EOL);
			return result;
		}
	}
}
