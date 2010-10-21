/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.chart;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedList;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A x/y series is an implementation for x/y value pairs. It's
 * suitable for collecting statistical information for data that
 * doesn't have an inherent indexed character (like events
 * on an arbitrary timeline).
 */
public class XYSeries {

  /** values */
  private LinkedList points = new LinkedList();
  
  /** name */
  private String name;
  
  /**
   * Constructor
   */
  public XYSeries(String name) {
    this.name = name;
  }
    
  /** 
   * Accessor - get number of points in series
   */
  public int getSize() {
    return points.size();
  }
  
  /**
   * Accessor - get points by index
   */
  private Point2D.Float getPointByIndex(int i) {
    return (Point2D.Float)points.get(i);
  }
    
  /**
   * Accessor - get point by its x value
   * @return existing point or new point if didn't exist
   */
  private Point2D.Float getPointForX(float x) {
    
    // look at existing points
    for (int i=0;i<points.size();i++) {
      
      Point2D.Float p = (Point2D.Float)points.get(i);
      
      // found it?
      if (p.getX()==x) 
        return p;
      
      // past insertion point?
      if (p.getX()>x) {
        p = new Point2D.Float(x,0);
        points.add(i, p);
        return p;
      }
      
      // try next
    }
    
    // append new
    Point2D.Float p = new Point2D.Float(x,0);
    points.add(p);
    return p;
  }
  
  /**
   * Accessor - set current y-value at given x-position
   * @param x the x-position to increment (new point for x is created if necessary)
   * @param y the new y-value
   */
  public void set(float x, float y) {
    // find point
    Point2D.Float point = getPointForX(x);
    // set
    point.y = y;
    // done
  }
  
  /**
   * Accessor - increment current y-value at given x-position
   * @param x the x-position to increment (new point for x is created if necessary)
   */
  public void inc(float x) {
    // find point
    Point2D.Float point = getPointForX(x);
    // increase
    point.y++;
    // done
  }
  
  /**
   * Convenient converter to get a list of series from
   * a dynamic collection containing xy series
   */
  public static XYSeries[] toArray(Collection c) {
    return (XYSeries[])c.toArray(new XYSeries[c.size()]);
  }
  /**
   * Wrap into something JFreeChart can use
   */
  /*package*/ static XYDataset toXYDataset(XYSeries[] series) {
    return new XYDatasetImpl(series);
  }
  
  /**
   * A wrapper for Java Free Chart's XY DataSheet
   */
  private static class XYDatasetImpl extends AbstractXYDataset {
    
    /** wrapped */
    private XYSeries[] series;
    
    /**
     * Constructor
     */
    private XYDatasetImpl(XYSeries[] series) {
      this.series = series;
    }
    
    /**
     * # of series
     */
    public int getSeriesCount() {
      return series.length;
    }

    /**
     * series by index 
     */
    public String getSeriesName(int s) {
      return series[s].name;
    }

    /**
     * # of items in series
     */
    public int getItemCount(int s) {
      return series[s].getSize();
    }

    /**
     * item x for seriex
     */
    public Number getX(int i, int item) {
      Point2D.Float p = series[i].getPointByIndex(item);
      return new Float(p.x);
    }

    /**
     * item y for seriex
     */
    public Number getY(int i, int item) {
      Point2D.Float p = series[i].getPointByIndex(item);
      return new Float(p.y);
    }

	@Override
	public Comparable getSeriesKey(int series) {
		// if the name can be considered as the series key
		return this.series[series].name;
	}

  } //Wrapper
  
} //XYSeries
