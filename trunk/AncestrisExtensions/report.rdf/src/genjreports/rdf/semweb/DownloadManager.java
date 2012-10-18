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

import static genjreports.rdf.semweb.Predicate.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Manager of downloads from the semantic web.
 * 
 * @author Jo Pol
 */
public class DownloadManager
{
    private static final Logger logger = Logger.getLogger(DownloadManager.class.getName());

    private final Model model;
    private final Set<String> tried = new HashSet<String>();
    private final Set<String> found;
    private final String dbpediaFilter;

    public DownloadManager(final Model model, final String languages) throws IOException
    {

        if (model == null)
            throw new IllegalArgumentException("model should not be null");
        this.model = model;

        if (languages == null || languages.length() == 0)
            dbpediaFilter = "/dbpedia.org";
        else
            dbpediaFilter = "/((" + languages + ")[.])?dbpedia.org";

        final String pfx = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
        Arrays.deepToString(runQuery(pfx + "SELECT ?dbp {?l rdfs:hasLabel ?p;rdfs:isDefinedBy ?gn.?gn rdfs:seeAlso ?dbp.","dbp").toArray());
        found = runQuery(pfx + " SELECT ?dbp {?l rdfs:hasLabel ?p;rdfs:isDefinedBy ?gn.?gn rdfs:seeAlso ?dbp.?dbp a ?t. }", "dbp");
        found.addAll(runQuery(pfx + " SELECT ?gn {?l rdfs:hasLabel ?p;rdfs:isDefinedBy ?gn.?gn a ?t. }", "gn"));
    }

    public Set<String> downloadGeoNames(final String uri) throws URISyntaxException, IOException
    {
        if (tried.contains(uri))
            return new HashSet<String>();
        download(uri, uri + "about.rdf");
        final Set<String> same = runQuery(uri, sameAs, "geonames.org");
        for (final String uri2 : same)
            if (!found.contains(uri2))
                downloadGeoNames(uri2);
        for (final String uri2 : runQuery(uri, seeAlso, "dbpedia.org"))
            if (!found.contains(uri2))
                same.addAll(downloadDbPedia(uri2));
        return same;
    }

    public Set<String> downloadDbPedia(final String uri) throws URISyntaxException, UnsupportedEncodingException
    {
        if (tried.contains(uri))
            return new HashSet<String>();
        download(uri, toDbpediaUrl(uri));
        final Set<String> same = runQuery(uri, sameAs, dbpediaFilter);
        for (final String uri2 : same)
            if (!found.contains(uri))
                downloadDbPedia(uri2);
        return same;
    }

    private void download(final String uri, final String url) throws URISyntaxException
    {
        if (tried.contains(uri) || found.contains(uri))
            return;
        tried.add(uri);
        Nice.sleep(new URI(url).getHost());
        logger.log(Level.INFO, "reading: " + url);
        try
        {
            model.read(url);
        }
        catch (final JenaException e)
        {
            logger.log(Level.WARNING, url + " " + e.getMessage());
        }
    }

    private String toDbpediaUrl(final String uri) throws UnsupportedEncodingException
    {
        final String decoded = URLDecoder.decode(uri, "UTF-8");
        return decoded.replace("/resource/", "/data/") + ".rdf";
    }

    private Set<String> runQuery(final String uri, final Predicate predicate, final String filterRegEx)
    {
        final String format = "select distinct ?n {<%s> <%s> ?n. FILTER regex(str(?n),'%s')}";
        final String q = String.format(format, uri, predicate.toUri(), filterRegEx);
        return runQuery(q, "n");
    }

    private Set<String> runQuery(final String q, final String columnName)
    {
        logger.log(Level.FINE, "query: " + q);
        final QueryExecution queryExecution = QueryExecutionFactory.create(q, Syntax.syntaxARQ, model, new QuerySolutionMap());
        final Set<String> result = new HashSet<String>();
        try
        {
            final ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext())
            {
                final QuerySolution row = resultSet.next();
                result.add(row.get(columnName).asResource().getURI());
            }
        }
        finally
        {
            queryExecution.close();
        }
        logger.log(Level.INFO, Arrays.deepToString(result.toArray()));
        return result;
    }
}
