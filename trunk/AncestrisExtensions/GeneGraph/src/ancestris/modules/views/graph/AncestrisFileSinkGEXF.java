/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 * Original version :
 * Copyright 2006 - 2016
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkBase;
import org.graphstream.util.cumulative.CumulativeAttributes;
import org.graphstream.util.cumulative.CumulativeSpells;
import org.graphstream.util.cumulative.CumulativeSpells.Spell;
import org.graphstream.util.cumulative.GraphSpells;

public class AncestrisFileSinkGEXF extends FileSinkBase {
    
   private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    public static enum TimeFormat {
        INTEGER(new DecimalFormat("#", new DecimalFormatSymbols(Locale.ROOT))), DOUBLE(
                new DecimalFormat("#.0###################",
                        new DecimalFormatSymbols(Locale.ROOT))), DATE(
                new SimpleDateFormat("yyyy-MM-dd")), DATETIME(
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ"));
        Format format;

        TimeFormat(Format f) {
            this.format = f;
        }
    }

    private XMLStreamWriter stream;
    private boolean smart;
    private int depth;
    private int currentAttributeIndex = 0;
    private GraphSpells graphSpells;
    private TimeFormat timeFormat;

    public AncestrisFileSinkGEXF() {
        smart = true;
        depth = 0;
        graphSpells = null;
        timeFormat = TimeFormat.DOUBLE;
    }

    public void setTimeFormat(TimeFormat format) {
        this.timeFormat = format;
    }

    protected void putSpellAttributes(Spell s) throws XMLStreamException {
        if (s.isStarted()) {
            String start = s.isStartOpen() ? "startopen" : "start";
            String date = timeFormat.format.format(s.getStartDate());

            stream.writeAttribute(start, date);
        }

        if (s.isEnded()) {
            String end = s.isEndOpen() ? "endopen" : "end";
            String date = timeFormat.format.format(s.getEndDate());

            stream.writeAttribute(end, date);
        }
    }

    @Override
    protected void outputEndOfFile() throws IOException {
        try {
            if (graphSpells != null) {
                exportGraphSpells();
                graphSpells = null;
            }

            endElement(stream, false);
            stream.writeEndDocument();
            stream.flush();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void outputHeader() throws IOException {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT);

        try {
            stream = XMLOutputFactory.newFactory()
                    .createXMLStreamWriter(output);
            stream.writeStartDocument("UTF-8", "1.0");

            startElement(stream, "gexf");
            stream.writeAttribute("xmlns", "http://www.gexf.net/1.2draft");
            stream.writeAttribute("xmlns:xsi",
                    "http://www.w3.org/2001/XMLSchema-instance");
            stream.writeAttribute("xsi:schemaLocation",
                    "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd");
            stream.writeAttribute("version", "1.2");

            startElement(stream, "meta");
            stream.writeAttribute("lastmodifieddate", df.format(date));
            startElement(stream, "creator");
            stream.writeCharacters("GraphStream - " + getClass().getName());
            endElement(stream, true);
            endElement(stream, false);
        } catch (XMLStreamException | FactoryConfigurationError e) {
            throw new IOException(e);
        }
    }

    protected void startElement(XMLStreamWriter stream, String name)
            throws XMLStreamException {
        if (smart) {
            stream.writeCharacters("\n");

            for (int i = 0; i < depth; i++) {
                stream.writeCharacters(" ");
            }
        }

        stream.writeStartElement(name);
        depth++;
    }

    protected void endElement(XMLStreamWriter stream, boolean leaf)
            throws XMLStreamException {
        depth--;

        if (smart && !leaf) {
            stream.writeCharacters("\n");

            for (int i = 0; i < depth; i++) {
                stream.writeCharacters(" ");
            }
        }

        stream.writeEndElement();
    }

    @Override
    protected void exportGraph(Graph g) {
        GEXFAttributeMap nodeAttributes = new GEXFAttributeMap("node", g);
        GEXFAttributeMap edgeAttributes = new GEXFAttributeMap("edge", g);

        try {
            startElement(stream, "graph");
            stream.writeAttribute("defaultedgetype", "directed");

            nodeAttributes.export(stream);
            edgeAttributes.export(stream);

            startElement(stream, "nodes");
            for (Node n : g.getEachNode()) {
                startElement(stream, "node");
                stream.writeAttribute("id", n.getId());

                if (n.hasAttribute("label")) {
                    stream.writeAttribute("label", n.getAttribute("label")
                            .toString());
                }

                if (n.getAttributeCount() > 0) {
                    startElement(stream, "attvalues");
                    for (String key : n.getAttributeKeySet()) {
                        nodeAttributes.push(stream, n, key);
                    }
                    endElement(stream, false);
                }

                endElement(stream, n.getAttributeCount() == 0);
            }
            endElement(stream, false);

            startElement(stream, "edges");
            for (Edge e : g.getEachEdge()) {
                startElement(stream, "edge");

                stream.writeAttribute("id", e.getId());
                stream.writeAttribute("source", e.getSourceNode().getId());
                stream.writeAttribute("target", e.getTargetNode().getId());

                if (e.getAttributeCount() > 0) {
                    startElement(stream, "attvalues");
                    for (String key : e.getAttributeKeySet()) {
                        edgeAttributes.push(stream, e, key);
                    }
                    endElement(stream, false);
                }

                endElement(stream, e.getAttributeCount() == 0);
            }
            endElement(stream, false);

            endElement(stream, false);
        } catch (XMLStreamException e1) {
            LOG.log(Level.SEVERE, "Error during export", e1);
        }
    }

    protected void exportGraphSpells() {
        GEXFAttributeMap nodeAttributes = new GEXFAttributeMap("node",
                graphSpells);
        GEXFAttributeMap edgeAttributes = new GEXFAttributeMap("edge",
                graphSpells);

        try {
            startElement(stream, "graph");
            stream.writeAttribute("mode", "dynamic");
            stream.writeAttribute("defaultedgetype", "directed");
            stream.writeAttribute("timeformat", timeFormat.name().toLowerCase());

            nodeAttributes.export(stream);
            edgeAttributes.export(stream);

            startElement(stream, "nodes");
            for (String nodeId : graphSpells.getNodes()) {
                startElement(stream, "node");
                stream.writeAttribute("id", nodeId);

                CumulativeAttributes attr = graphSpells
                        .getNodeAttributes(nodeId);
                Object label = attr.getAny("label");

                if (label != null) {
                    stream.writeAttribute("label", label.toString());
                }

                CumulativeSpells spells = graphSpells.getNodeSpells(nodeId);

                if (!spells.isEternal()) {
                    startElement(stream, "spells");
                    for (int i = 0; i < spells.getSpellCount(); i++) {
                        Spell s = spells.getSpell(i);

                        startElement(stream, "spell");
                        putSpellAttributes(s);
                        endElement(stream, true);
                    }
                    endElement(stream, false);
                }

                if (attr.getAttributesCount() > 0) {
                    startElement(stream, "attvalues");
                    nodeAttributes.push(stream, nodeId, graphSpells);
                    endElement(stream, false);
                }

                endElement(stream,
                        spells.isEternal() && attr.getAttributesCount() == 0);
            }
            endElement(stream, false);

            startElement(stream, "edges");
            for (String edgeId : graphSpells.getEdges()) {
                startElement(stream, "edge");

                GraphSpells.EdgeData data = graphSpells.getEdgeData(edgeId);

                stream.writeAttribute("id", edgeId);
                stream.writeAttribute("source", data.getSource());
                stream.writeAttribute("target", data.getTarget());

                CumulativeAttributes attr = graphSpells
                        .getEdgeAttributes(edgeId);

                CumulativeSpells spells = graphSpells.getEdgeSpells(edgeId);

                if (!spells.isEternal()) {
                    startElement(stream, "spells");
                    for (int i = 0; i < spells.getSpellCount(); i++) {
                        Spell s = spells.getSpell(i);

                        startElement(stream, "spell");
                        putSpellAttributes(s);
                        endElement(stream, true);
                    }
                    endElement(stream, false);
                }

                if (attr.getAttributesCount() > 0) {
                    startElement(stream, "attvalues");
                    edgeAttributes.push(stream, edgeId, graphSpells);
                    endElement(stream, false);
                }

                endElement(stream,
                        spells.isEternal() && attr.getAttributesCount() == 0);
            }
            endElement(stream, false);

            endElement(stream, false);
        } catch (XMLStreamException e1) {
           LOG.log(Level.SEVERE, "Error during export", e1);
        }
    }

    protected void checkGraphSpells() {
        if (graphSpells == null) {
            graphSpells = new GraphSpells();
        }
    }

    @Override
    public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
            String attribute, Object value) {
        checkGraphSpells();
        graphSpells.edgeAttributeAdded(sourceId, timeId, edgeId, attribute,
                value);
    }

    @Override
    public void edgeAttributeChanged(String sourceId, long timeId,
            String edgeId, String attribute, Object oldValue, Object newValue) {
        checkGraphSpells();
        graphSpells.edgeAttributeChanged(sourceId, timeId, edgeId, attribute,
                oldValue, newValue);
    }

    @Override
    public void edgeAttributeRemoved(String sourceId, long timeId,
            String edgeId, String attribute) {
        checkGraphSpells();
        graphSpells.edgeAttributeRemoved(sourceId, timeId, edgeId, attribute);
    }

    @Override
    public void graphAttributeAdded(String sourceId, long timeId,
            String attribute, Object value) {
        checkGraphSpells();
        graphSpells.graphAttributeAdded(sourceId, timeId, attribute, value);
    }

    @Override
    public void graphAttributeChanged(String sourceId, long timeId,
            String attribute, Object oldValue, Object newValue) {
        checkGraphSpells();
        graphSpells.graphAttributeChanged(sourceId, timeId, attribute,
                oldValue, newValue);
    }

    @Override
    public void graphAttributeRemoved(String sourceId, long timeId,
            String attribute) {
        checkGraphSpells();
        graphSpells.graphAttributeRemoved(sourceId, timeId, attribute);
    }

    @Override
    public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
            String attribute, Object value) {
        checkGraphSpells();
        graphSpells.nodeAttributeAdded(sourceId, timeId, nodeId, attribute,
                value);
    }

    @Override
    public void nodeAttributeChanged(String sourceId, long timeId,
            String nodeId, String attribute, Object oldValue, Object newValue) {
        checkGraphSpells();
        graphSpells.nodeAttributeChanged(sourceId, timeId, nodeId, attribute,
                oldValue, newValue);
    }

    @Override
    public void nodeAttributeRemoved(String sourceId, long timeId,
            String nodeId, String attribute) {
        checkGraphSpells();
        graphSpells.nodeAttributeRemoved(sourceId, timeId, nodeId, attribute);
    }

    @Override
    public void edgeAdded(String sourceId, long timeId, String edgeId,
            String fromNodeId, String toNodeId, boolean directed) {
        checkGraphSpells();
        graphSpells.edgeAdded(sourceId, timeId, edgeId, fromNodeId, toNodeId,
                directed);
    }

    @Override
    public void edgeRemoved(String sourceId, long timeId, String edgeId) {
        checkGraphSpells();
        graphSpells.edgeRemoved(sourceId, timeId, edgeId);
    }

    @Override
    public void graphCleared(String sourceId, long timeId) {
        checkGraphSpells();
        graphSpells.graphCleared(sourceId, timeId);
    }

    @Override
    public void nodeAdded(String sourceId, long timeId, String nodeId) {
        checkGraphSpells();
        graphSpells.nodeAdded(sourceId, timeId, nodeId);
    }

    @Override
    public void nodeRemoved(String sourceId, long timeId, String nodeId) {
        checkGraphSpells();
        graphSpells.nodeRemoved(sourceId, timeId, nodeId);
    }

    @Override
    public void stepBegins(String sourceId, long timeId, double step) {
        checkGraphSpells();
        graphSpells.stepBegins(sourceId, timeId, step);
    }

    private class GEXFAttribute {

        int index;
        String key;
        String type;

        GEXFAttribute(String key, String type) {
            this.index = currentAttributeIndex++;
            this.key = key;
            this.type = type;
        }
    }

    private class GEXFAttributeMap extends HashMap<String, GEXFAttribute> {

        private static final long serialVersionUID = 6176508111522815024L;
        protected String type;

        GEXFAttributeMap(String type, Graph g) {
            this.type = type;

            Iterable<? extends Element> iterable;

            if (type.equals("node")) {
                iterable = (Iterable<? extends Element>) g.getNodeSet();
            } else {
                iterable = (Iterable<? extends Element>) g.getEdgeSet();
            }

            for (Element e : iterable) {
                for (String key : e.getAttributeKeySet()) {
                    Object value = e.getAttribute(key);
                    check(key, value);
                }
            }
        }

        GEXFAttributeMap(String type, GraphSpells spells) {
            this.type = type;

            if (type.equals("node")) {
                for (String nodeId : spells.getNodes()) {
                    CumulativeAttributes attr = spells
                            .getNodeAttributes(nodeId);

                    for (String key : attr.getAttributes()) {
                        for (Spell s : attr.getAttributeSpells(key)) {
                            Object value = s.getAttachedData();
                            check(key, value);
                        }
                    }
                }
            } else {
                for (String edgeId : spells.getEdges()) {
                    CumulativeAttributes attr = spells
                            .getEdgeAttributes(edgeId);

                    for (String key : attr.getAttributes()) {
                        for (Spell s : attr.getAttributeSpells(key)) {
                            Object value = s.getAttachedData();
                            check(key, value);
                        }
                    }
                }
            }
        }

        private void check(String key, Object value) {
            String id = getID(key, value);
            String attType = "string";

            if (containsKey(id)) {
                return;
            }

            if (value instanceof Integer || value instanceof Short) {
                attType = "integer";
            } else if (value instanceof Long) {
                attType = "long";
            } else if (value instanceof Float) {
                attType = "float";
            } else if (value instanceof Double) {
                attType = "double";
            } else if (value instanceof Boolean) {
                attType = "boolean";
            } else if (value instanceof URL || value instanceof URI) {
                attType = "anyURI";
            } else if (value.getClass().isArray() || value instanceof Collection) {
                attType = "liststring";
            }

            put(id, new GEXFAttribute(key, attType));
        }

        String getID(String key, Object value) {
            return String.format("%s@%s", key, value.getClass().getName());
        }

        void export(XMLStreamWriter stream) throws XMLStreamException {
            if (this.isEmpty()) {
                return;
            }

            startElement(stream, "attributes");
            stream.writeAttribute("class", type);

            for (GEXFAttribute a : values()) {
                startElement(stream, "attribute");
                stream.writeAttribute("id", Integer.toString(a.index));
                stream.writeAttribute("title", a.key);
                stream.writeAttribute("type", a.type);
                endElement(stream, true);
            }

            endElement(stream, this.isEmpty());
        }

        void push(XMLStreamWriter stream, Element e, String key)
                throws XMLStreamException {
            String id = getID(key, e.getAttribute(key));
            GEXFAttribute a = get(id);

            if (a == null) {
                // TODO
                return;
            }

            startElement(stream, "attvalue");
            stream.writeAttribute("for", Integer.toString(a.index));
            stream.writeAttribute("value", e.getAttribute(key).toString());
            endElement(stream, true);
        }

        void push(XMLStreamWriter stream, String elementId, GraphSpells spells)
                throws XMLStreamException {
            CumulativeAttributes attr;

            if (type.equals("node")) {
                attr = spells.getNodeAttributes(elementId);
            } else {
                attr = spells.getEdgeAttributes(elementId);
            }

            for (String key : attr.getAttributes()) {
                for (Spell s : attr.getAttributeSpells(key)) {
                    Object value = s.getAttachedData();
                    String id = getID(key, value);
                    GEXFAttribute a = get(id);

                    if (a == null) {
                        // TODO
                        return;
                    }

                    startElement(stream, "attvalue");
                    stream.writeAttribute("for", Integer.toString(a.index));
                    stream.writeAttribute("value", value.toString());
                    putSpellAttributes(s);
                    endElement(stream, true);
                }
            }
        }
    }
}
