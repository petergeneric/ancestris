package genjreports.rdf;

import ancestris.util.swing.DialogManager;
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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.report.Report;
import genjreports.rdf.gedsem.SemanticGedcomUtil;
import genjreports.rdf.gedsem.UriFormats;
import genjreports.rdf.semweb.Extension;
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
import java.util.Map;

public class ReportRdf extends Report /* implements BatchCompatible */
{

    static final String DEFAULT_STYLE_SHEET = "http://www.w3.org/TR/rdf-sparql-XMLres/result-to-html.xsl";

    public static class Queries
    {
        public String qGedcom = "";
        public String qRules = "";
    }

    public static class DisplayFormats
    {
        public String styleSheet = DEFAULT_STYLE_SHEET;
        public String convertedFileName = "gedcom.ttl";
        public boolean reuseConversion = false;
        public String reportFileName = "report.txt";
        public boolean askForOverwrite = true;
    };

    public UriFormats uriFormats = new UriFormats();
    public DisplayFormats output = new DisplayFormats();
    public Queries queries = new Queries();

    public void start(final Gedcom gedcom) throws IOException
    {

        final String query = getQuery(queries.qGedcom);
        if (optionsOk(query))
            run("", getModel(gedcom), query);
    }

    public void start(final Entity entity) throws IOException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {

        final String query = getQuery(queries.qGedcom);
        if (optionsOk(query))
            run(entity.getId(), getModel(entity.getGedcom()), String.format(query, entity.getId()));
    }

    public void run(final String id, final Model model, final String query) throws FileNotFoundException, IOException
    {

        progress("executing query");
        final ResultSet resultSet = execSelect(assembleQuery(query, model), model);
        if (resultSet == null || !resultSet.hasNext())
        {
            progress("no results");
            return;
        }
        progress("query completed");

        final String extension = extractExtension(output.reportFileName);
        if (extension.equals("xml"))
        {
            if (output.styleSheet.trim().length() > 0)
                write(output.reportFileName, ResultSetFormatter.asXMLString(resultSet, output.styleSheet));
            else
                write(output.reportFileName, ResultSetFormatter.asXMLString(resultSet));
        }
        else if (extension.equals("txt"))
        {
            write(output.reportFileName, ResultSetFormatter.asText(resultSet));
        }
    }

    private boolean optionsOk(final String query)
    {

        boolean ok = true;
        if (output.convertedFileName.trim().length() > 0)
        {
            final String ext = extractExtension(output.convertedFileName);
            try
            {
                Extension.valueOf(ext);
            }
            catch (final IllegalArgumentException e)
            {
                println(output.convertedFileName + " should have one of the extensions " + Arrays.deepToString(Extension.values()));
                ok = false;
            }
        }
        // TODO if not reuse there should be a convertedFileName
        // [extension of] report output
        if (query.trim().length() == 0)
        {
            println("no query");
            ok = false;
        }
        // TODO read messages from properties
        return ok;
    }

    private String extractExtension(final String reportFileName)
    {
        return reportFileName.trim().replaceAll(".*\\.", "").toLowerCase();
    }

    private Model getModel(final Gedcom gedcom) throws FileNotFoundException, IOException
    {

        final Model model;
        final String fileName = output.convertedFileName.trim();
        final String extension = extractExtension(output.convertedFileName);
        final String language = Extension.valueOf(extension).language();
        if (output.reuseConversion)
        {
            progress("reading " + fileName);
            model = ModelFactory.createDefaultModel();
            model.read(new FileInputStream(fileName), (String) null, language);
            progress("reading completed");
        }
        else
        {
            model = convert(gedcom);
            writeConvertedGedcom(model, fileName);
        }
        // TODO read the other RDF files from the folder and its sub-folders
        return model;
    }

    public InfModel convert(final Gedcom gedcom) throws FileNotFoundException, IOException
    {

        final SemanticGedcomUtil util = new SemanticGedcomUtil();
        progress("converting");
        util.toRdf(gedcom, uriFormats.getURIs());
        progress("applying rules");
        final InfModel model = util.getInfModel(getQuery(queries.qRules));
        progress("rules completed");
        return model;
    }

    private void progress(final String string)
    {

        final DateFormat dateFormat = new SimpleDateFormat(" HH:mm:ss.SSS ");
        final Date date = new Date();
        getOut().println(dateFormat.format(date) + string);
        getOut().flush();
    }

    private void writeConvertedGedcom(final Model model, final String fileName) throws FileNotFoundException
    {
        if (fileName.length() == 0)
            return;
        final String language;
        try
        {
            language = Extension.valueOf(extractExtension(fileName)).language();
        }
        catch (final IllegalArgumentException exception)
        {
            return;
        }
        if (fileName.startsWith("#"))
        {
            model.write(getOut(), language);
            return;
        }
        final File file = new File(fileName);
        if (!doNotOverwrite(file))
        {
            writeProgress(file);
            model.write(new FileOutputStream(file), language);
        }
    }

    private void write(final String fileName, final String content) throws IOException
    {
        if (fileName.startsWith("#"))
        {
            getOut().println("############################################");
            getOut().println(content);
            getOut().flush();
            return;
        }
        final File file = new File(fileName);
        if (doNotOverwrite(file))
            return;
        writeProgress(file);
        final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        try
        {
            fileOutputStream.write(content.getBytes());
        }
        finally
        {
            fileOutputStream.close();
        }
    }

    private boolean doNotOverwrite(final File file)
    {
        if (file.exists() && output.askForOverwrite)
        {
            final String format = getResources().getString("overwrite.question");
            final String prompt = MessageFormat.format(format, file.getAbsoluteFile());
            return (DialogManager.YES_OPTION != DialogManager.createYesNo(getName(), prompt)
                    .setMessageType(DialogManager.WARNING_MESSAGE)
                    .show());
        }
        return false;
    }

    private void writeProgress(final File file)
    {
        final String format = getResources().getString("progress.writing");
        final String prompt = MessageFormat.format(format, file.getAbsoluteFile());
        getOut().println(prompt);
        getOut().flush();
    }

    private ResultSet execSelect(final String query, final Model model)
    {
        try
        {
            final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
            final ResultSet resultSet = queryExecution.execSelect();
            progress("query executed");
            flush();
            return resultSet;
        }
        catch (final QueryParseException exception)
        {
            println(exception.getMessage());
            println(query);
            flush();
        }
        return null;
    }

    String assembleQuery(final String query, final Model model) throws IOException, FileNotFoundException, UnsupportedEncodingException
    {
        final StringBuffer fullQuery = assemblePrefixes(model);
        fullQuery.append(getResources().getString("query.function.prefixes"));
        fullQuery.append(query);
        return fullQuery.toString();
    }

    private String getQuery(final String queryPart) throws FileNotFoundException, IOException
    {
        final File file = new File(queryPart);
        final byte[] buffer = new byte[(int) file.length()];
        RandomAccessFile f = new RandomAccessFile(queryPart, "r");
        f.readFully(buffer);
        f.close();
        return new String(buffer, "UTF-8");
    }

    public static StringBuffer assemblePrefixes(final Model model) throws FileNotFoundException, IOException
    {
        final Map<String, String> prefixMap = model.getNsPrefixMap();
        final StringBuffer query = new StringBuffer();
        for (final Object prefix : prefixMap.keySet().toArray())
            query.append(String.format("PREFIX %s: <%s> \n", prefix.toString(), prefixMap.get(prefix).toString()));
        return query;
    }
}
