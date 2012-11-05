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
package genjreports.rdf.semweb;

import java.io.*;
import java.util.Arrays;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;

public class Select
{
    private final Model model;
    private final ResultSet resultSet;

    /**
     * @param args
     *        query-file, output-file, files with triples
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException
    {
        final String query = readQuery(args[0]);
        final OutputStream output = new FileOutputStream(args[1]);
        final String format = args[1].trim().replaceAll(".*[.]", "").toLowerCase();
        final String[] fileNames = Arrays.copyOfRange(args, 2, args.length - 2);

        if (format.equals("txt"))
            new PrintStream(output).println(new Select(query, fileNames).toText());
        else if (format.equals("csv"))
            new Select(query, fileNames).toCsv(output);
        else if (format.equals("xml"))
            new Select(query, fileNames).toXml(output);
        else if (format.equals("tsv"))
            new Select(query, fileNames).toTsv(output);
    }

    /**
     * @param query
     *        a file name, query should provide its own name space prefixes
     * @param fileNames
     *        other extensions than defined by {@link Extension} are skipped
     * @throws IOException
     *         in case of problems with the query file
     */
    public Select(final String query, final String... fileNames) throws IOException
    {

        model = ModelFactory.createDefaultModel();
        for (final String fileName : fileNames)
            readIntoModel(new File(fileName));
        resultSet = execute(query);
    }

    /**
     * @param query
     *        a file name, query should provide its own name space prefixes
     * @param files
     *        other extensions than defined by {@link Extension} are skipped
     * @throws IOException
     *         in case of problems with the query file
     */
    public Select(final String query, final File... files) throws IOException
    {

        model = ModelFactory.createDefaultModel();
        for (final File file : files)
            readIntoModel(file);
        resultSet = execute(query);
    }

    private ResultSet execute(final String queryFileName) throws IOException
    {
        final byte[] bytes = new byte[(int) new File(queryFileName).length()];
        final InputStream inputStream = new FileInputStream(queryFileName);
        try
        {
            inputStream.read(bytes);
        }
        finally
        {
            inputStream.close();
        }
        final String q = new String(bytes);
        final QuerySolutionMap qsm = new QuerySolutionMap();
        return QueryExecutionFactory.create(q, Syntax.syntaxARQ, model, qsm).execSelect();
    }

    private void readIntoModel(final File file) throws IOException
    {
        final FileInputStream inputStream = new FileInputStream(file);
        try
        {
            final String language = Extension.valueOf(file).language();
            model.read(inputStream, (String) null, language);
        }
        catch (final IllegalArgumentException e)
        {
            // just skip;
        }
        finally
        {
            inputStream.close();
        }
    }

    public String toText()
    {
        checkState();
        final Prologue prologue = new Prologue(PrefixMapping.Factory.create().setNsPrefixes(model.getNsPrefixMap()));
        return new String(ResultSetFormatter.asText(resultSet, prologue).getBytes());
    }

    public void toXml(final OutputStream outputStream)
    {
        checkState();
        ResultSetFormatter.outputAsXML(outputStream, resultSet);
    }

    public void toXml(final OutputStream outputStream, final String styleSheet)
    {
        checkState();
        ResultSetFormatter.outputAsXML(outputStream, resultSet, styleSheet);
    }

    public void toTsv(final OutputStream outputStream)
    {
        checkState();
        ResultSetFormatter.outputAsTSV(outputStream, resultSet);
    }

    public void toCsv(final OutputStream outputStream)
    {
        checkState();
        ResultSetFormatter.outputAsCSV(outputStream, resultSet);
    }

    private void checkState()
    {
        if (!resultSet.hasNext())
            throw new IllegalStateException("result can be processed just once");
    }

    private static String readQuery(final String fileName) throws IOException
    {
        final File file = new File(fileName);
        final byte[] buffer = new byte[(int) file.length()];
        final InputStream inputStream = new FileInputStream(file);
        try
        {
            inputStream.read(buffer, 0, System.in.available());
            return new String(buffer);
        }
        finally
        {
            inputStream.close();
        }
    }
}
