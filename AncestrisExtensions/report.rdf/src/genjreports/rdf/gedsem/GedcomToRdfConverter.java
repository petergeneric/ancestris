package genjreports.rdf.gedsem;

import genj.gedcom.Gedcom;
import genj.io.GedcomReaderFactory;
import genjreports.rdf.semweb.Extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Model;

// import org.junit.Test;

public class GedcomToRdfConverter
{

    private static final String LS = System.getProperty("line.separator");

    private static final String HELP = "=== OPTIONS === " + Arrays.deepToString(Option.values()) + //
            LS + "rules  : filename" + //
            LS + "format : default ttl; possible values: " + Arrays.deepToString(Extension.values()) + //
            LS + "uri    : default for the options FAM, INDI, OBJE, NOTE, REPO, SOUR, SUBM" + //
            LS + "         default for uri is " + UriFormats.DEFAULT_URI + //
            LS + "         make sure to add a terminator " + //
            LS + "gedcom : filename, only preceding options are applied to the conversion";

    private static enum Option
    {
        rules, format, uri, FAM, INDI, OBJE, NOTE, REPO, SOUR, SUBM, gedcom
    };

    public static void main(final String[] args) throws Exception
    {

        if (args == null || args.length == 0)
            throw createException("missing arguments");

        String qRules = null;
        final UriFormats uriFormats = new UriFormats();
        String language = Extension.ttl.language();
        Logger.getLogger("").setLevel(Level.OFF);

        for (int i = 0; i < args.length; i++)
        {
            final Option option = toOption(args[i]);
            final String value = args[++i];
            switch (option)
            {
            case format:
                language = toLanguage(value);
                break;
            case gedcom:
                final SemanticGedcomUtil util = new SemanticGedcomUtil();
                Model rdf = util.toRdf(readGedcom(value), uriFormats.getURIs());
                if (qRules != null)
                    rdf = util.getInfModel(read(qRules));
                rdf.write(System.out, language);
                break;
            case rules:
                qRules = value;
                break;
            case FAM:
                uriFormats.fam = value;
                break;
            case INDI:
                uriFormats.indi = value;
                break;
            case OBJE:
                uriFormats.obje = value;
                break;
            case NOTE:
                uriFormats.note = value;
                break;
            case REPO:
                uriFormats.repo = value;
                break;
            case SOUR:
                uriFormats.sour = value;
                break;
            case SUBM:
                uriFormats.subm = value;
                break;
            case uri:
                uriFormats.subm = value;
                uriFormats.fam = value;
                uriFormats.indi = value;
                uriFormats.obje = value;
                uriFormats.note = value;
                uriFormats.repo = value;
                uriFormats.sour = value;
                break;
            }
        }
    }

    private static String read(final String fileName) throws IOException
    {
        final File file = new File(fileName);
        final byte[] bytes = new byte[(int) file.length()];
        final FileInputStream inputStream = new FileInputStream(file);
        try
        {
            inputStream.read(bytes);
        }
        finally
        {
            inputStream.close();
        }
        return new String(bytes);
    }

    private static IllegalArgumentException createException(final String string)
    {

        System.err.println(HELP);
        return new IllegalArgumentException(string);
    }

    private static String toLanguage(final String value)
    {
        try
        {
            return Extension.valueOf(value).language();
        }
        catch (final IllegalArgumentException e)
        {
            throw createException("invalid value for " + Option.format + ": " + value);
        }
    }

    private static Option toOption(final String string)
    {

        if (!string.startsWith("-"))
        {
            throw createException("unkown option: " + string);
        }
        try
        {
            return Option.valueOf(string.substring(1));
        }
        catch (final IllegalArgumentException e)
        {
            throw createException("unkown option: " + string);
        }
    }

    private static Gedcom readGedcom(final String file) throws Exception
    {

        return GedcomReaderFactory.createReader(new FileInputStream(file), null).read();
    }

    /*
     * @Test(expected = IllegalArgumentException.class) public void noArgs() throws Exception {
     * main(null); }
     * @Test(expected = IllegalArgumentException.class) public void emptyArgs() throws Exception {
     * main(new String[] {}); }
     * @Test(expected = IllegalArgumentException.class) public void wrongOption() throws Exception {
     * main(new String[] { "-" }); }
     * @Test public void exampleGedcomToTurtle() throws Exception { // takes a few seconds to run final
     * String uriPattern = "http://my.domain.com/gedcom/{0}.html"; final String url = new
     * File("gedcom/example.ged").toURI().toURL().toString(); final String setOfRules = "2"; main(new
     * String[] { "-rules", setOfRules, "-uri", uriPattern, "-gedcom", url }); }
     */
}
