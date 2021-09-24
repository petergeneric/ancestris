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
 *     Hicham Brahimi   <hicham.brahimi@graphstream-project.org>
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
package ancestris.modules.views.graph.graphstream;

import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Algorithme AStar with no direction.
 *
 * @author Zurga
 */
public class AncestrisAStar extends AStar {

    public AncestrisAStar(Graph graphe) {
        super(graphe);
    }

    /**
     * The A* algorithm proper.
     *
     * @param sourceNode The source node.
     * @param targetNode The target node.
     */
    @Override
    protected void aStar(Node sourceNode, Node targetNode) {
        clearAll();
        open.put(
                sourceNode,
                new AStarNode(sourceNode, null, null, 0, costs.heuristic(
                        sourceNode, targetNode)));

        pathFound = false;

        while (!open.isEmpty()) {
            AStarNode current = getNextBetterNode();

            assert (current != null);

            if (current.node == targetNode) {
                // We found it !
                assert current.edge != null;
                pathFound = true;
                result = buildPath(current);
                return;
            } else {
                open.remove(current.node);
                closed.put(current.node, current);

                // For each successor of the current node :
                current.node.edges().forEach(edge -> {
                    Node next = edge.getOpposite(current.node);
                    double h = costs.heuristic(next, targetNode);
                    double g = current.g + costs.cost(current.node, edge, next);
                    double f = g + h;

                    // If the node is already in open with a better rank, we
                    // skip it.
                    AStarNode alreadyInOpen = open.get(next);

                    if (!(alreadyInOpen != null && alreadyInOpen.rank <= f)) {

                        // If the node is already in closed with a better rank; we
                        // skip it.
                        AStarNode alreadyInClosed = closed.get(next);

                        if (!(alreadyInClosed != null && alreadyInClosed.rank <= f)) {

                            closed.remove(next);
                            open.put(next, new AStarNode(next, edge, current, g, h));
                        }
                    }
                });
            }
        }
    }
}
