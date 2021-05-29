// @formatter:off
/*
 * Copyright 2012, J. Pol
 *
 * This file is part of free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 *
 * This package is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details. A copy of the GNU General Public License is
 * available at <http://www.gnu.org/licenses/>.
 */
// @formatter:on
package genjreports.rdf.gedsem;

import genj.gedcom.Gedcom;
import genj.io.GedcomReaderFactory;
import genj.util.Origin;
import genjreports.rdf.semweb.Extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// import org.junit.Test;

public class Convert
{

    private static final String LS = System.getProperty("line.separator");

    private static final String HELP = "=== OPTIONS === " + Arrays.deepToString(Option.values()) + //
            LS + "rules  : filename" + //
            LS + "format : default ttl; possible values: " + Arrays.deepToString(Extension.values()) + //
            LS + "uri    : default for the options FAM, INDI, OBJE, NOTE, REPO, SOUR, SUBM" + //
            LS + "         default for uri is " + UriFormats.DEFAULT_URI + //
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
                final Map<String, String> uriMap = uriFormats.getURIs();
                System.err.println("conversion done");
                if (qRules == null)
                     util.toRdf(readGedcom(value), uriMap).write(System.out, language);
                else
                {
                    System.err.println("applying rules");
                    util.getInfModel(qRules).write(System.out, language);
                    System.err.println("rules done");
                }
                break;
            case rules:
                qRules = read(value);
                break;
            case FAM:
                uriFormats.fam = chekURI(value);
                break;
            case INDI:
                uriFormats.indi = chekURI(value);
                break;
            case OBJE:
                uriFormats.obje = chekURI(value);
                break;
            case NOTE:
                uriFormats.note = chekURI(value);
                break;
            case REPO:
                uriFormats.repo = chekURI(value);
                break;
            case SOUR:
                uriFormats.sour = chekURI(value);
                break;
            case SUBM:
                uriFormats.subm = chekURI(value);
                break;
            case uri:
                uriFormats.subm = chekURI(value);
                uriFormats.fam = chekURI(value);
                uriFormats.indi = chekURI(value);
                uriFormats.obje = chekURI(value);
                uriFormats.note = chekURI(value);
                uriFormats.repo = chekURI(value);
                uriFormats.sour = chekURI(value);
                break;
            }
        }
    }

    private static String chekURI(final String value) throws URISyntaxException
    {
        try {
        return new URI(value).toString();
        } catch (URISyntaxException e) {
            throw createException(value+" "+e.getMessage());
        }
    }

    private static String read(final String fileName) throws IOException
    {
        File file = new File(fileName);
        if (!file.exists())
            throw createException(fileName+" does not exist");
        if (file.isDirectory())
            throw createException(fileName+" should be plain text file but is a directory");
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

    private static Gedcom readGedcom(final String fileName) throws Exception
    {
        File file = new File(fileName);
        if (!file.exists())
            throw createException(fileName+" does not exist");
        if (file.isDirectory())
            throw createException(fileName+" should be a gedcom file but is a directory");

        return GedcomReaderFactory.createReader(Origin.create(file.toURI().toURL()), null).read();
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
