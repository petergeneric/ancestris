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
import java.util.*;
import java.util.logging.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * Manager of downloads from the semantic web.
 * 
 * @author Jo Pol
 */
public class QueryUtil
{
    private static final Logger logger = Logger.getLogger(QueryUtil.class.getName());

    private final Model model;
    private final String dbpediaFilter;

    public QueryUtil(final Model model, final String languages) throws IOException
    {
        if (model == null)
            throw new IllegalArgumentException("model should not be null");
        this.model = model;

        if (languages == null || languages.length() == 0)
            dbpediaFilter = "/dbpedia.org";
        else
            dbpediaFilter = "/((" + languages + ")[.])?dbpedia.org";
    }

    /**
     * Get specific objects for a subject.
     * 
     * @param subject
     *        the resource URI for which properties are searched
     * @param predicate
     *        the type of property searched for
     * @param objectRegEx
     *        filter for the searched objects
     * @return
     */
    public Set<String> getProperties(final String subject, final Property predicate, final String objectRegEx)
    {
        final String format = "select distinct ?n {<%s> <%s> ?n. FILTER regex(str(?n),'%s')}";
        final String q = String.format(format, subject, predicate.getURI(), objectRegEx);
        return run(q);
    }

    /**
     * Gets DbPedia resources related with owl:sameAs. The desired language variants are specified at
     * construction time of this object instance.
     * 
     * @param uri typically a DbPedia resource
     * @return URI's of DbPedia resources
     */
    public Set<String> getSameDbpediaResources(final String uri)
    {
        // TODO ??? http://fr.dbpedia.org/ontology/wikiPageInterLanguageLink
        final String format = "select distinct ?n {<%s> <%s> ?n. FILTER regex(str(?n),'%s')}";
        final String q = String.format(format, uri, OWL.sameAs.getURI(), dbpediaFilter);
        return run(q);
    }

    /**
     * Runs a SPARQL with {@link Syntax.syntaxARQ}
     * 
     * @param query
     * @return the first column of the query result
     */
    public Set<String> runQuery(final String query)
    {
        return run(query);
    }

    private Set<String> run(final String query)
    {
        logger.log(Level.FINE, "query: " + query);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
        final Set<String> result = new HashSet<String>();
        try
        {
            final ResultSet resultSet = queryExecution.execSelect();
            final List<String> columnnNames = resultSet.getResultVars();
            while (resultSet.hasNext())
            {
                final QuerySolution row = resultSet.next();
                result.add(row.get(columnnNames.get(0)).toString());
            }
        }
        finally
        {
            queryExecution.close();
        }
        if (!result.isEmpty())
            logger.log(Level.INFO, Arrays.deepToString(result.toArray()));
        return result;
    }
}
