/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;

/**
 * File writer for graphe.
 *
 * @author Zurga
 */
public class GraphFileWriter {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private static final String UI_LABEL = "ui.label";
    private static final char COLON = ':';

    private final File out;
    private final GraphicGraph leGraphic;

    public GraphFileWriter(File fichierToWrite, GraphicGraph graphic) {
        super();
        out = fichierToWrite;
        leGraphic = graphic;
    }

    public void start(String gedcomName) {
        try (OutputStreamWriter bw = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            bw.write(gedcomName + "\n");
            
            for (Node n : leGraphic.nodes().collect(Collectors.toList())) {
                final String line = createNodeLine(n);
                bw.write(line);
            }
            for (Edge e : leGraphic.edges().collect(Collectors.toList())) {
                final String line = createEdgeLine(e);
                bw.write(line);
            }
            bw.flush();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to write Graph File.", e);
        }
    }

    private String createNodeLine(Node node) {
        StringBuilder retour = new StringBuilder("N:");
        retour.append(node.getId()).append(COLON);
        final String label = (String) node.getAttribute(UI_LABEL);
        retour.append(label).append(COLON);
        GraphicNode gn = (GraphicNode) leGraphic.getNode(node.getId());
        retour.append(gn.x).append(COLON);
        retour.append(gn.y).append(COLON);
        retour.append(gn.z).append(COLON);

        retour.append('\n');

        return retour.toString();
    }

    private String createEdgeLine(Edge edge) {
        StringBuilder retour = new StringBuilder("E:");
        retour.append(edge.getId()).append(COLON);
        retour.append(edge.getNode0().getId()).append(COLON);
        retour.append(edge.getNode1().getId()).append(COLON);

        retour.append('\n');

        return retour.toString();
    }

}
