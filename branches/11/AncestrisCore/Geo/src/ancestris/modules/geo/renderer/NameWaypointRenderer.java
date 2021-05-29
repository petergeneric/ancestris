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

import ancestris.modules.geo.GeoPoint;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;

/**
 * WaypointRenderer with names.
 *
 * @author Zurga
 */
public class NameWaypointRenderer extends DefaultWaypointRenderer {

    private int markersSize;
    private Color markersColor;

    public NameWaypointRenderer(int size, Color couleur) {
        super();
        markersSize = size;
        markersColor = couleur;
    }

    public NameWaypointRenderer() {
        this(10, Color.BLUE);
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

        Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
        int x = Long.valueOf(Math.round(point.getX())).intValue();
        int y = Long.valueOf(Math.round(point.getY())).intValue();
        // get name
        double coex = ((double) markersSize) / 10;
        String name = ((GeoPoint) wp).getGeoNodeObject().getCity();
        g.setFont(new Font("Dialog", Font.PLAIN, (int) (12 * coex)));
        double width = (int) g.getFontMetrics().getStringBounds(name, g).getWidth();
        //draw tab

        g.setPaint(markersColor);
        Polygon triangle = new Polygon();
        triangle.addPoint(x, y);
        triangle.addPoint(x + (int) (7 * coex), y + (int) (-11 * coex));
        triangle.addPoint(x + (int) (-7 * coex), y + (int) (-11 * coex));
        g.fill(triangle);

        GradientPaint colortowhite = new GradientPaint(x + (int) ((-width / 2 - 5)), y - 4 * markersSize, markersColor, x + (int) ((-width / 2 - 5)) , y - 2 * markersSize, Color.WHITE, true);
        g.setPaint(colortowhite);
        g.fillRoundRect(x + (int) ((-width / 2 - 5)), y - 3 * markersSize, (int) ((width + 10)), 2 * markersSize, 10, 10);
        g.setPaint(markersColor);
        g.drawString(name, x + (int) ((-width / 2)), y + (int) ((-16) * coex)); //text
    }
}
