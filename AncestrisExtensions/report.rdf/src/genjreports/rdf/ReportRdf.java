package genjreports.rdf;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
//import genj.report.BatchCompatible;
import genj.report.Report;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Report.class)
public class ReportRdf extends Report /*implements BatchCompatible */{

	static final String DEFAULT_URI = "http://my.domain.com/gedcom/{0}.html";
	static final String DEFAULT_STYLE_SHEET = "http://www.w3.org/TR/rdf-sparql-XMLres/result-to-html.xsl";

	public static class Queries {
		public String qGedcom = "";
		public String qFam = "";
		public String qIndi = "";
		public String qMedia = "";
		public String qNote = "";
		public String qRepository = "";
		public String qSource = "";
		public String qSubmitter = "";
		public String qRules = "";
	}

	public static class DisplayFormats {
		public String styleSheet = DEFAULT_STYLE_SHEET;
		public String convertedFileName = "gedcom.ttl";
		public boolean reuseConversion = false;
		public String reportFileName = "report.txt";
		public boolean askForOverwrite = true;
	};

	public static class UriFormats {
		
		public String fam = DEFAULT_URI;
		public String indi = DEFAULT_URI;
		public String obje = DEFAULT_URI;
		public String note = DEFAULT_URI;
		public String repo = DEFAULT_URI;
		public String sour = DEFAULT_URI;
		public String subm = DEFAULT_URI;

		private Map<String, String> getURIs() {
			Map<String, String> uris;
			uris = new HashMap<String, String>();
			uris.put("FAM", fam);
			uris.put("INDI", indi);
			uris.put("OBJE", obje);
			uris.put("NOTE", note);
			uris.put("REPO", repo);
			uris.put("SOUR", sour);
			uris.put("SUBM", subm);
			return uris;
		}
	}

	public enum Extension {
		ttl("TURTLE"), n3("N3"), nt("N-TRIPPLE"), rdf("RDF/XML-ABBREV");
		// RDF/XML-ABBREV is less verbose, use RDF/XML for large models
		private final String language;

		private Extension(final String language) {
			this.language = language;
		}

		public String getLanguage() {
			return language;
		}
	}

	public UriFormats uriFormats = new UriFormats();
	public DisplayFormats output = new DisplayFormats();
	public Queries queries = new Queries();

	public void start(final Gedcom gedcom) throws IOException {

		final String query = getQuery(queries.qGedcom, "query.gedcom");
		if (optionsOk(query))
			run("", getModel(gedcom), query);
	}

	public void start(final Entity entity) throws IOException,
			SecurityException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {

		// use reflection to get the query option for the actual type of entity
		final String name = entity.getClass().getSimpleName();
		final String resourceKeyBase = "query." + name.toLowerCase();
		final String value = (String) queries.getClass().getField("q" + name)
				.get(queries);

		final String query = getQuery(value, resourceKeyBase);
		if (optionsOk(query))
			run(entity.getId(), getModel(entity.getGedcom()), String.format(
					query, entity.getId()));
	}

	public void run(final String id, final Model model, final String query)
			throws FileNotFoundException, IOException {

		progress("executing query");
		final ResultSet resultSet = execSelect(assembleQuery(query, model),
				model);
		if (resultSet == null || !resultSet.hasNext()) {
			progress("no results");
			return;
		}
		progress("query completed");

		final String extension = extractExtension(output.reportFileName);
		if (extension.equals("xml")) {
			if (output.styleSheet.trim().length() > 0)
				write(output.reportFileName, ResultSetFormatter.asXMLString(
						resultSet, output.styleSheet));
			else
				write(output.reportFileName, ResultSetFormatter
						.asXMLString(resultSet));
		} else if (extension.equals("txt")) {
			write(output.reportFileName, ResultSetFormatter.asText(resultSet));
		}
	}

	private boolean optionsOk(final String query) {

		boolean ok = true;
		if (output.convertedFileName.trim().length() > 0) {
			final String ext = extractExtension(output.convertedFileName);
			try {
				Extension.valueOf(ext);
			} catch (final IllegalArgumentException e) {
				println(output.convertedFileName
						+ " should have one of the extensions "
						+ Arrays.deepToString(Extension.values()));
				ok = false;
			}
		}
		// TODO if not reuse there should be a convertedFileName
		// [extension of] report output
		if (query.trim().length() == 0) {
			println("no query");
			ok = false;
		}
		if (!query.trim().toLowerCase().startsWith("select ")) {
			println("no valid select query: " + query);
			ok = false;
		}
		// TODO read messages from properties
		return ok;
	}

	private String extractExtension(final String reportFileName) {
		return reportFileName.trim().replaceAll(".*\\.", "").toLowerCase();
	}

	private Model getModel(final Gedcom gedcom) throws FileNotFoundException,
			IOException {

		final Model model;
		final String fileName = output.convertedFileName.trim();
		final String extension = extractExtension(output.convertedFileName);
		final String language = Extension.valueOf(extension).getLanguage();
		if (output.reuseConversion) {
			progress("reading "+fileName);
			model = ModelFactory.createDefaultModel();
			model.read(new FileInputStream(fileName), (String) null, language);
			progress("reading completed");
		} else {
			model = convert(gedcom);
			writeConvertedGedcom(model, fileName);
		}
		// TODO read the other RDF files from the folder and its sub-folders
		return model;
	}

	public InfModel convert(final Gedcom gedcom) throws FileNotFoundException,
			IOException {

		final SemanticGedcomUtil util = new SemanticGedcomUtil();
		progress("converting");
		final Model rawModel = util.toRdf(gedcom, uriFormats.getURIs());
		progress("applying rules");
		final InfModel model = util.getInfModel(getQuery(queries.qRules,
				"query.rules"));
		progress("rules completed");
		return model;
	}

	private void progress(final String string) {

		final DateFormat dateFormat = new SimpleDateFormat(" HH:mm:ss.SSS ");
		final Date date = new Date();
		getOut().println(dateFormat.format(date) + string);
		getOut().flush();
	}

	private void writeConvertedGedcom(final Model model, final String fileName)
			throws FileNotFoundException {
		if (fileName.length() == 0)
			return;
		final String language;
		try {
			language = Extension.valueOf(extractExtension(fileName))
					.getLanguage();
		} catch (final IllegalArgumentException exception) {
			return;
		}
		if (fileName.startsWith("#")) {
			model.write(getOut(), language);
			return;
		}
		final File file = new File(fileName);
		if (!doNotOverwrite(file)) {
			writeProgress(file);
			model.write(new FileOutputStream(file), language);
		}
	}

	private void write(final String fileName, final String content)
			throws IOException {
		if (fileName.startsWith("#")) {
			getOut().println("############################################");
			getOut().println(content);
			getOut().flush();
			return;
		}
		final File file = new File(fileName);
		if (doNotOverwrite(file))
			return;
		writeProgress(file);
		new FileOutputStream(fileName).write(content.getBytes());
	}

	private boolean doNotOverwrite(final File file) {
		if (file.exists() && output.askForOverwrite) {
			final String format = getResources()
					.getString("overwrite.question");
			final String prompt = MessageFormat.format(format, file
					.getAbsoluteFile());
			final int rc = DialogHelper
					.openDialog(getName(), DialogHelper.WARNING_MESSAGE,
							prompt, Action2.yesNo(), null);
			return (rc != 0);
		}
		return false;
	}

	private void writeProgress(final File file) {
		final String format = getResources().getString("progress.writing");
		final String prompt = MessageFormat.format(format, file
				.getAbsoluteFile());
		getOut().println(prompt);
		getOut().flush();
	}

	private ResultSet execSelect(final String query, final Model model) {
		try {
			final QueryExecution queryExecution = QueryExecutionFactory.create(
					query, Syntax.syntaxARQ, model, new QuerySolutionMap());
			final ResultSet resultSet = queryExecution.execSelect();
			progress("query executed");
			flush();
			return resultSet;
		} catch (final QueryParseException exception) {
			println(exception.getMessage());
			println(query);
			flush();
		}
		return null;
	}

	String assembleQuery(final String query, final Model model)
			throws IOException, FileNotFoundException,
			UnsupportedEncodingException {
		final StringBuffer fullQuery = assemblePrefixes(model);
		fullQuery.append(getResources().getString("query.function.prefixes"));
		fullQuery.append(query);
		return fullQuery.toString();
	}

	private String getQuery(final String queryPart, final String resourceKeyBase)
			throws FileNotFoundException, IOException {
		final File file = new File(queryPart);
		if (file.isFile()) {
			final byte[] buffer = new byte[(int) file.length()];
			new RandomAccessFile(queryPart, "r").readFully(buffer);
			return new String(buffer, "UTF-8");
		} else if (queryPart.trim().equals("")) {
			return getResources().getString(resourceKeyBase + ".1");
		} else if (queryPart.trim().matches("[0-9]*")) {
			return getResources().getString(
					resourceKeyBase + "." + queryPart.trim());
		}
		return queryPart;
	}

	public static StringBuffer assemblePrefixes(final Model model)
			throws FileNotFoundException, IOException {
		final Map<String, String> prefixMap = model.getNsPrefixMap();
		final StringBuffer query = new StringBuffer();
		for (final Object prefix : prefixMap.keySet().toArray())
			query.append(String.format("PREFIX %s: <%s> \n", prefix.toString(),
					prefixMap.get(prefix).toString()));
		return query;
	}
}
