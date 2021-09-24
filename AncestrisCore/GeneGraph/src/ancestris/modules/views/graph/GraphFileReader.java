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

import genj.gedcom.Gedcom;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Load a graphe.
 *
 * @author Zurga
 */
public class GraphFileReader {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private static final String COLON = ":";

    private final File in;
    private final Graph leGraphe;
    private final Gedcom leGedcom;

    public GraphFileReader(File fichierToRead, Graph graph, Gedcom gedcom) {
        super();
        in = fichierToRead;
        leGraphe = graph;
        leGedcom = gedcom;
    }

    public void start() {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(in), StandardCharsets.UTF_8))) {
            String st = br.readLine();
            if (leGedcom.getName().equals(st)) {
                leGraphe.clear();
            } else {
                return;
            }
            while ((st = br.readLine()) != null) {
                manageLine(st);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to read Graph File.", e);
        }
        // reinit display attributes for all deriving graphs 
        Object style = leGraphe.getAttribute("ui.stylesheet");
        leGraphe.setAttribute("ui.stylesheet", style);
        leGraphe.setAttribute("ui.antialias");
    }

    private void manageLine(String line) {
        String[] split = line.split(COLON);
        if ("N".equals(split[0])) {
            createNode(split);
        }
        if ("E".equals(split[0])) {
            createEdge(split);
        }
    }

    private void createNode(String[] split) {
        if (leGedcom.getEntity(split[1]) == null) {
            return;
        }
        Node noeud = leGraphe.addNode(split[1]);
        if (!"0".equals(split[3]) || !"0".equals(split[4]) || !"0".equals(split[5])) {
            noeud.setAttribute("xyz", Double.valueOf(split[3]), Double.valueOf(split[4]), Double.valueOf(split[5]));
        }
    }

    private void createEdge(String[] split) {
        if (leGraphe.getEdge(split[2]) == null || leGraphe.getEdge(split[3]) == null) {
            return;
        }
        leGraphe.addEdge(split[1], split[2], split[3]);
    }

}
