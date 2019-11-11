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
package ancestris.modules.views.graph.graphstream;

import java.awt.event.MouseEvent;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicSprite;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.MouseManager;

/**
 *
 * @author Zurga
 */
public class AncestrisMouseManager implements MouseManager {
    // Attribute

    /**
     * The view this manager operates upon.
     */
    protected View view;

    /**
     * The graph to modify according to the view actions.
     */
    protected GraphicGraph graph;

    // Mouse Listener
    protected GraphicElement curElement;

    protected float x1, y1;

    // Construction
    @Override
    public void init(GraphicGraph graph, View view) {
        this.view = view;
        this.graph = graph;
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }

    @Override
    public void release() {
        view.removeMouseListener(this);
        view.removeMouseMotionListener(this);
    }

    // Command
    protected void mouseButtonPress(MouseEvent event) {
        view.requestFocus();

        // Unselect all.
        if (!event.isShiftDown()) {
            for (Node node : graph) {
                if (node.hasAttribute("ui.selected")) {
                    node.removeAttribute("ui.selected");
                }
            }

            for (GraphicSprite sprite : graph.spriteSet()) {
                if (sprite.hasAttribute("ui.selected")) {
                    sprite.removeAttribute("ui.selected");
                }
            }
        }
    }

    protected void mouseButtonRelease(MouseEvent event,
            Iterable<GraphicElement> elementsInArea) {
        for (GraphicElement element : elementsInArea) {
            if (!element.hasAttribute("ui.selected")) {
                element.addAttribute("ui.selected");
            }
        }
    }

    protected void mouseButtonPressOnElement(GraphicElement element,
            MouseEvent event) {
        view.freezeElement(element, true);
        if (event.getButton() == 3) {
            element.addAttribute("ui.selected");
        } else {
            element.addAttribute("ui.clicked");
        }
    }

    protected void elementMoving(GraphicElement element, MouseEvent event) {
        view.moveElementAtPx(element, event.getX(), event.getY());
    }

    protected void mouseButtonReleaseOffElement(GraphicElement element,
            MouseEvent event) {
        view.freezeElement(element, false);
        if (event.getButton() != 3) {
            element.removeAttribute("ui.clicked");
        } else {
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        // NOP
    }

    @Override
    public void mousePressed(MouseEvent event) {
        curElement = view.findNodeOrSpriteAt(event.getX(), event.getY());

        if (curElement != null) {
            mouseButtonPressOnElement(curElement, event);
        } else {
            x1 = event.getX();
            y1 = event.getY();
            mouseButtonPress(event);
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (curElement != null) {
            elementMoving(curElement, event);
        } else {
            double deltaX = event.getX() - x1;
            double deltaY = event.getY() -y1;
            Point3 p = view.getCamera().getViewCenter();
            Point3 p1 = view.getCamera().transformGuToPx(p.x, p.y, p.z);
            Point3 p2 =view.getCamera().transformPxToGu(p1.x - deltaX, p1.y - deltaY);
            
            view.getCamera().setViewCenter(p2.x, p2.y, p.z);
            x1 = event.getX();
            y1 = event.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (curElement != null) {
            mouseButtonReleaseOffElement(curElement, event);
            curElement = null;
        } else {
            float x2 = event.getX();
            float y2 = event.getY();
            float t;

            if (x1 > x2) {
                t = x1;
                x1 = x2;
                x2 = t;
            }
            if (y1 > y2) {
                t = y1;
                y1 = y2;
                y2 = t;
            }

            mouseButtonRelease(event, view.allNodesOrSpritesIn(x1, y1, x2, y2));
        }
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        // NOP
    }

    @Override
    public void mouseExited(MouseEvent event) {
        // NOP
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
