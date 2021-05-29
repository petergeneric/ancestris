/*
 * WaypointRenderer.java
 *
 * Created on March 30, 2006, 5:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jxmapviewer.viewer;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jxmapviewer.JXMapViewer;

/**
 * This is a standard waypoint renderer.
 * @author joshy
 */
public class DefaultWaypointRenderer implements WaypointRenderer<Waypoint>
{
    private static final Logger LOG = Logger.getLogger("ancestris.app");
    
    private BufferedImage img = null;

    /**
     * Uses a default waypoint image
     */
    public DefaultWaypointRenderer()
    {
        try
        {
            img = ImageIO.read(DefaultWaypointRenderer.class.getResource("/org/jxmapviewer/images/standard_waypoint.png"));
        }
        catch (IOException ex)
        {
            LOG.log(Level.WARNING, "couldn't read standard_waypoint.png", ex);
        }
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint w)
    {
        if (img == null)
            return;

        Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());
        
        int x = (int)point.getX() -img.getWidth() / 2;
        int y = (int)point.getY() -img.getHeight();
        
        g.drawImage(img, x, y, null);
    }
}
