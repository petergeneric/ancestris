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

import static com.hp.hpl.jena.rdf.model.ResourceFactory.*;
import static genjreports.rdf.semweb.Predicate.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Manages data from the sematic web related to place name literals.
 * 
 * @author Jo Pol
 */
public class Mashup
{
    private static final Logger logger = Logger.getLogger(Mashup.class.getName());

    private final URI idPrefix;
    private final File file;
    private final String language;
    private Model model;

    /**
     * Downloads and links external data for all places.
     * 
     * @param idPrefix
     *        used to construct new URIs.
     * @param file
     *        a file with one of the extension rdf/n3/ttl. Intermediate changes to the file by anything
     *        except methods of this instance, will be overwritten by methods of this instance.
     */
    public Mashup(final URI idPrefix, final File file)
    {
        this.file = file;
        this.idPrefix = idPrefix;
        final String extension = file.getName().trim().replaceAll(".*\\.", "").toLowerCase();
        language = Extension.valueOf(extension).language();
    }

    /**
     * Downloads resources from geonames and links them to place names with the statements
     * 
     * <pre>
     * &lt;id&gt; rdf:label KEY.
     * &lt;id&gt; rdfs:isDefinedBy &lt;http://sws.geonames.org/VALUE&gt;.
     * </pre>
     * 
     * where KEY and VALUE are from the placeNameIdMap and &lt;id&gt; is generated using the {@link URI}
     * passed to the constructor. Overwrites possible previous contents of the {@link File} passed to the
     * constructor.
     * 
     * @param placeNameIdMap
     *        KEY: place name literal. For gedcom files typically<br>
     *        the unique values of INDI:*:PLAC and FAM:*:PLAC<br>
     *        for example ", Washington, , , , DC, USA"<br>
     *        VALUE: GeoNameId's (the numeric component of the URIs)<br>
     *        uri example http://sws.geonames.org/4140963/about.rdf
     * @throws FileNotFoundException
     */
    public void create(final Map<String, String> placeNameIdMap) throws FileNotFoundException
    {
        logger.info(Arrays.deepToString(placeNameIdMap.keySet().toArray()));
        model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(Prefix.NAME_ID_MAP);
        addPlaces(placeNameIdMap);
        model.write(new FileOutputStream(file), language);
    }

    /**
     * Like {@link #create(Map)} but only adds places not yet in the file.
     * 
     * @param placeNameIdMap
     *        as {@link #create(Map)}
     * @throws FileNotFoundException
     */
    public void update(final Map<String, String> placeNameIdMap) throws FileNotFoundException
    {
        logger.info(Arrays.deepToString(placeNameIdMap.keySet().toArray()));
        readModel();
        addPlaces(placeNameIdMap);
        model.write(new FileOutputStream(file), language);
    }

    private void addPlaces(final Map<String, String> places) throws FileNotFoundException
    {
        for (final String place : places.keySet())
        {
            final String geoNameId = places.get(place);
            final Resource subject = createResource(idPrefix + geoNameId);
            final String uri = "http://sws.geonames.org/" + geoNameId + "/";
            if (!model.containsLiteral(subject, hasLabel.property(), place))
            {
                model.add(subject, hasLabel.property(), place);
                model.add(subject, isDefinedBy.property(), createResource(uri));
                download(uri);
            }
        }
    }

    private void download(final String geoNamesUri)
    {
        model.read(geoNamesUri + "about.rdf");
        for (final String uri : runQuery(geoNamesUri, seeAlso, "dbpedia.org"))
        {
            downloadDbPedia(uri);
            for (final String uri2 : runQuery(uri, seeAlso, "dbpedia.org"))
                downloadDbPedia(uri2);
            for (final String uri2 : runQuery(uri, sameAs, "dbpedia.org"))
                downloadDbPedia(uri2);
        }
    }

    private void downloadDbPedia(final String uri)
    {
        try
        {
            String url = URLDecoder.decode(uri, "UTF-8").replace("/resource/", "/data/") + ".rdf";
            logger.log(Level.INFO, url);
            model.read(url);
        }
        catch (UnsupportedEncodingException e)
        {
            // Is there any OS that does not support UTF-8?
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private List<String> runQuery(final String uri, final Predicate predicate, final String filterRegEx)
    {
        final ArrayList<String> result = new ArrayList<String>();
        final String format = "select distinct ?n {<%s> <%s> ?n. FILTER regex(str(?n),'%s')}";
        final String q = String.format(format, uri, predicate.property(), filterRegEx);
        logger.log(Level.FINE, "query: " + q);
        final QueryExecution queryExecution = QueryExecutionFactory.create(q, Syntax.syntaxARQ, model, new QuerySolutionMap());
        try
        {
            final ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext())
            {
                result.add(resultSet.next().get("n").asResource().getURI());
            }
        }
        finally
        {
            queryExecution.close();
        }
        return result;
    }

    private void readModel() throws FileNotFoundException
    {
        if (model == null)
        {
            model = ModelFactory.createDefaultModel();
            model.read(new FileInputStream(file), (String) null, language);
        }
    }

    public static void main(final String[] args) throws URISyntaxException, IOException
    {
        logger.log(Level.INFO, "started");
        final Map<String, String> places = new HashMap<String, String>();
        final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] fields = line.split("\t");
            places.put(line.substring(fields[0].length() + 1), fields[0]);
        }
        new Mashup(new URI(args[1]), new File(args[2])).create(places);
        br.close();
        logger.log(Level.INFO, "finished");
    }
}
