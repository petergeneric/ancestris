/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2019 Ancestris
 * 
 * Author: Ancestris
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.geo.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;

/**
 *
 * @author Zurga
 */
public class NoNameWaypointRenderer extends DefaultWaypointRenderer {

    private int markersSize;
    private Color markersColor;

    public NoNameWaypointRenderer(int size, Color couleur) {
        super();
        markersSize = size;
        markersColor = couleur;
    }

    public NoNameWaypointRenderer() {
        this(10, Color.BLUE);
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
        Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
        int x = Long.valueOf(Math.round(point.getX())).intValue();
        int y = Long.valueOf(Math.round(point.getY())).intValue();
        g.setStroke(new BasicStroke((int) (((double) markersSize) / 8 + 1)));
        g.setColor(markersColor);
        g.drawOval(x -markersSize, y -markersSize, 2 * markersSize, 2 * markersSize);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x -markersSize, y, x + markersSize, y);
        g.drawLine(x, y -markersSize, x, y + markersSize);
    }

}
