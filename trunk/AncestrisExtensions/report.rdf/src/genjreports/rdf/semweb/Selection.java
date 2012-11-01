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

public class Selection
{
    private final Model model;
    private final ResultSet resultSet;

    /**
     * 
     * @param args query-file, output-file, files with triples 
     * @throws IOException
     */
    public void main(final String... args) throws IOException
    {
        final String query = readQuery(args[0]);
        final OutputStream output = new FileOutputStream(args[1]);
        final String format = args[1].trim().replaceAll(".*[.]", "").toLowerCase();
        final String[] fileNames = Arrays.copyOfRange(args, 2, args.length - 2);

        if (format.equals("txt"))
            new PrintStream(output).println(new Selection(query, fileNames).toText());
        else if (format.equals("csv"))
            new Selection(query, fileNames).toCsv(output);
        else if (format.equals("xml"))
            new Selection(query, fileNames).toXml(output);
        else if (format.equals("tsv"))
            new Selection(query, fileNames).toTsv(output);
    }

    /**
     * @param query
     *        a file name, query should provide its own name space prefixes
     * @param fileNames
     *        other extensions than defined by {@link Extension} are skipped
     * @throws IOException
     *         in case of problems with the query file
     */
    public Selection(final String query, final String... fileNames) throws IOException
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
    public Selection(final String query, final File... files) throws IOException
    {

        model = ModelFactory.createDefaultModel();
        for (final File file : files)
            readIntoModel(file);
        resultSet = execute(query);
    }

    private ResultSet execute(final String query)
    {
        return QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap()).execSelect();
    }

    private void readIntoModel(final File file) throws FileNotFoundException
    {
        final String ext = file.getName().replaceAll(".*[.]", "").toLowerCase();
        try
        {
            final String language = Extension.valueOf(ext).language();
            model.read(new FileInputStream(file), (String) null, language);
        }
        catch (IllegalArgumentException e)
        {
            // just skip;
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
