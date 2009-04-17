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
import genj.util.swing.Action2;

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
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The import function for Heredis originated Gedcom files
 */
public class Importer {

	static final String typerepo = "REPO"; // Debut de la cle REPO dans le
	// gedcom
	static String EOL = System.getProperty("line.separator");

	// static Pattern pattern = Pattern.compile("^1 REPO (.*)");
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

	static Pattern tag_y = Pattern.compile("(" + INDI_TAG_YES + FAM_TAG_YES
			+ ")");
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
	private static Hashtable<String, ImportIndi> hashIndis;
	private static Hashtable<String, ImportFam> hashFams;
	private static StringBuilder sb;

	/** our calling report */
	private Report report = null;

	/** our files */
	private File fileIn = null;
	private File fileOut = null;
	private GedcomFileReader input;
	private GedcomFileWriter output;

	Translate t = new Translate(Importer.class);
	protected boolean handleYesTag = true;
	protected boolean handleInvalidTag = true;
	protected boolean handleFrenchRepHeredis = false;
	protected boolean handleRepo = false;
	protected boolean handleMissingEntities = true;

	/**
	 * Constructor
	 */
	public Importer(Report report, File fileIn) {
		this.report = report;
		this.fileIn = fileIn;
		clerepo = 0;
		hashrepo = new Hashtable<String, Integer>();
		hashIndis = new Hashtable<String, ImportIndi>();
		hashFams = new Hashtable<String, ImportFam>();
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

		fileOut = report.getFileFromUser(t.translate("fileout.dlg.title"),
				Action2.TXT_OK, true, "ged");
		if (fileOut == null)
			return false;
		if (!fileOut.getName().toLowerCase().endsWith(".ged")) {
			fileOut = new File(fileOut.getAbsolutePath() + ".ged");
		}
		if (fileOut == null)
			return false;

		// on fait une premiere passe sur le fichier pour creer les repos
		try {
			EOL = getEOL(fileIn);
			input = getReader();
			try {
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((input.getNextLine(true)) != null) {
					if (handleRepo) {
						firstPassRepo();
					}
					if (handleMissingEntities) {
						firstPassMissingEntities();
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
		try {
			output = getWriter();
		} catch (IOException e1) {
			report.println(t.translate("file.create.error", fileOut.getName()));
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			report.println(t.translate("error.unknown"));
			return false;
		}
		try {
			input = getReader();
			try {
				while (input.getNextLine(true) != null) {
					if ((input.getLevel() == 0)
							&& (input.getTag().equals("HEAD"))) {
						output.writeln(input.getLine());
						output.writeLine(1, "NOTE", t.translate("note1", this
								.getClass().getSimpleName()));
						output.writeLine(2, "CONT", t.translate("note2"));
						output.writeLine(2, "CONT", t.translate("note3"));
						continue;
					}
					if (handleRepo)
						if (processRepo())
							continue;
					if (handleYesTag)
						if (processYesTag())
							continue;
					if (handleInvalidTag)
						if (processInvalidTag())
							continue;
					if (handleFrenchRepHeredis)
						if (processFrenchRepHeredis())
							continue;

					if (input.getTag().equals("TRLR")) {
						finalise();
						output.writeLine(0, "TRLR", null);
						continue;
					}
					output.write(input.getLine() + EOL);
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

		report.println(t.translate("end", fileOut));
		return true;
	}

	protected void finalise() throws IOException {
		if (handleRepo) {
			finaliseRepo();
		}
		if (handleMissingEntities){
			finaliseMissingEntities();
		}
	}

private void firstPassMissingEntities(){
	if (input.getTag().equals("INDI")) {
		String xref = "@" + input.getXref() + "@";
		if (!hashIndis.containsKey(xref))
			hashIndis.put(xref, new ImportIndi());
		hashIndis.get(xref).seen = true;
	}
	if (input.getTag().equals("CHIL")) {
		if (!hashIndis.containsKey(input.getValue()))
			hashIndis.put(input.getValue(), new ImportIndi());
	}
	if (input.getTag().equals("FAM")) {
		String xref = "@" + input.getXref() + "@";
		if (!hashFams.containsKey(xref))
			hashFams.put(xref, new ImportFam());
		hashFams.get(xref).seen = true;
	}
	if (input.getTag().equals("FAMS")) {
		if (!hashFams.containsKey(input.getValue()))
			hashFams.put(input.getValue(), new ImportFam());
	}

}
	private void finaliseMissingEntities() throws IOException{
		for (String k : hashIndis.keySet()) {
			if (!hashIndis.get(k).seen) {
				output.writeln("0 " + k + " INDI");
			}
		}
		for (String k : hashFams.keySet()) {
			if (!hashFams.get(k).seen) {
				output.writeln("0 " + k + " FAM");
			}
		}

	}

private void firstPassRepo(){
	if ((input.getLevel() == 1)
			&& input.getTag().equals("REPO")) {
		if (!hashrepo.containsKey(input.getValue())) {
			clerepo++;
			hashrepo.put(input.getValue(), clerepo);
			sb.append("0 @" + typerepo + clerepo + "@ REPO"
					+ EOL);
			sb.append("1 NAME " + input.getValue() + EOL);
		}
	}

}
	private boolean processRepo() throws IOException {
		if ((input.getLevel() == 1) && input.getTag().equals("REPO")) {
			if (hashrepo.containsKey(input.getValue())) {
				output.writeLine(1, "REPO", "@" + typerepo
						+ hashrepo.get(input.getValue()) + "@");
				report.println(input.getLine());
				report.println("==> " + t.translate("corrected"));
			}
			return true;
		}
		return false;

	}
	private void finaliseRepo() throws IOException{
		output.write(sb.toString());
	}

	public boolean processYesTag() throws IOException {
		Matcher matcher = tag_y.matcher(input.getTag());
		if (matcher.matches()) {
			if (input.getValue().length() != 0)
				output.writeln(input.getLine());
			else {
				String tag = input.getTag();
				int level = input.getLevel();
				String line = input.getLine();
				String temp = input.getNextLine(false);
				if ((temp != null) && (input.getLevel() == level + 1)) {
					output.writeln(input.getLine());
				} else {
					String result = output.writeLine(level, tag, "Y");
					report.println(line);
					report.println("==> " + result);
				}
			}
			return true;
		} else
			return false;
	}

	public boolean processInvalidTag() throws IOException {
		// C'est un tag perso: on ecrit telque
		if (input.getTag().startsWith("_")) {
			return false;
		}
		// le tag n'est pas valide: on le prefixe par _
		if (!tag_valid.matcher(input.getTag()).matches()) {
			String result = output.writeLine(input.getLevel(), "_"
					+ input.getTag(), input.getValue());
			report.println(input.getLine());
			report.println("==> " + result);
			return true;
		}
		return false;
	}

	// calendrier repub
	private boolean processFrenchRepHeredis() throws IOException {
		// C'est un tag DATE: on transforme les dates rep
		if (input.getTag().equals("DATE")) {
			String newValue = frenchCalCheck(input.getValue());
			if (newValue != null) {
				String result = output.writeLine(input.getLevel(), input
						.getTag(), newValue);
				report.println(input.getLine());
				report.println("==> " + result);
				return true;

			}
		}
		return false;
	}

	String frenchCalCheck(String in) {
		final Pattern french_cal = Pattern.compile("(@#DFRENCH R@ )(.*)");
		final Pattern date_value = Pattern
				.compile("(FROM|BEF|AFT|BET|INT|TO) (.*)");
		final Pattern date_range = Pattern
				.compile("(FROM|BEF|AFT|BET|INT|TO) (.*) (TO|AND) (.*)");

		String result = "";
		Matcher matcher = french_cal.matcher(in);
		if (matcher.matches() && (matcher.groupCount() > 1)) {
			// C'est un cal republicain, on essaie d'interpreter
			String date_parameter = matcher.group(2);
			Matcher m1 = date_range.matcher(date_parameter);
			if (m1.matches()) {
				result += m1.group(1) + " @#DFRENCH R@ "
						+ convDateFormat(m1.group(2));
				result += " " + m1.group(3) + " @#DFRENCH R@ "
						+ convDateFormat(m1.group(4));
				return result;
			}

			m1 = date_value.matcher(date_parameter);
			if (m1.matches()) {
				result += m1.group(1) + " @#DFRENCH R@ "
						+ convDateFormat(m1.group(2));
				return result;
			}
			result += "@#DFRENCH R@ " + convDateFormat(matcher.group(2));
			return result;
		} else
			return null;
	}

	@SuppressWarnings("serial")
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
		final Pattern french_date = Pattern.compile("(.*) an (\\w*)(.*)");
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
		private String theLine = "";

		public GedcomFileReader(File filein)
				throws UnsupportedEncodingException, FileNotFoundException {
			super(new InputStreamReader(new FileInputStream(fileIn), "LATIN1"),
					null, false);
		}

		public String getValue() {
			return value;
		}

		public String getXref() {
			return xref;
		}

		public int getLevel() {
			return level;
		}

		public String getNextLine(boolean consume) throws IOException {
			readLine(false);
			theLine = line;
			if (consume)
				line = null;
			return theLine;
		}

		void close() throws IOException {
			in.close();
		}

		String getTag() {
			return tag;
		}

		String getLine() {
			return theLine;
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

		String writeLine(int level, String tag, String value)
				throws IOException {

			String result = Integer.toString(level) + " " + tag;

			// Value
			if (value != null && value.length() > 0) {
				result += " " + value;
			}
			write(result + EOL);
			return result;
		}
	}

	private class ImportIndi {
		protected boolean seen = false;
	}

	private class ImportFam {
		protected boolean seen = false;
		protected String husb = "";
		protected String wife = "";
		protected String[] child = new String[10];
	}
}
