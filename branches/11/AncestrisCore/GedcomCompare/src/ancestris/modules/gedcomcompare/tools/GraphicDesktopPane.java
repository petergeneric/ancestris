/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.gedcomcompare.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 *
 * @author frederic
 */
public class GraphicDesktopPane extends JDesktopPane {

    private static final Stroke s = new BasicStroke(2.0f);
    private CubicCurve2D cubcurve;
    private List<Link> listOfLinks;
    
    public GraphicDesktopPane() {
        listOfLinks = new LinkedList<Link>();
    }

    public void addLink(JInternalFrame f1, JInternalFrame f2) {

        // locate if link does not already exist
        boolean found = false;
        for (Link link : listOfLinks) {
            if ((link.f1 == f1 && link.f2 == f2) || (link.f1 == f2 && link.f2 == f1)){
                found = true;
            }
        }
        
        // if not found, create it
        if (!found) {
            listOfLinks.add(new Link(f1, f2));
        }
    }

    
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(s);
        
        // Use array to avoid concurrent modification of listOfLinks
        Link[] array = listOfLinks.toArray(new Link[listOfLinks.size()]);
        for (int i = 0; i < array.length; i++) {
            Link link = array[i];
            if (!link.f1.isIcon() && !link.f2.isIcon()) {
                int x1 = link.f1.getX() + link.f1.getWidth() * 4 / 5;
                int y1 = link.f1.getY() + link.f1.getHeight() / 2;
                int x2 = link.f2.getX() + link.f2.getWidth() * 1 / 5;
                int y2 = link.f2.getY() + link.f2.getHeight() / 2;

                cubcurve = new CubicCurve2D.Float(x1, y1, x1 + link.f1.getWidth() * 1 / 5, y1 + 10, x2 - link.f2.getWidth() * 1 / 5, y2 + 10, x2, y2);
                g2d.draw(cubcurve);
            }
        }
    }

    public void removeLink(JInternalFrame f) {
        List<Link> linksToRemove = new LinkedList<Link>();
        
        for (Link link : listOfLinks) {
            if (link.f1 == f || link.f2 == f) {
                linksToRemove.add(link);
            }
        }
        listOfLinks.removeAll(linksToRemove);
    }

    private class Link {
        public JInternalFrame f1;
        public JInternalFrame f2;

        private Link(JInternalFrame f1, JInternalFrame f2) {
            this.f1 = f1;
            this.f2 = f2;
        }
        
    }
    
}

