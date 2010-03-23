/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Missing mathematical functions from the geom.* stuff
 */
public class Geometry {
  
  public final static double 
    ONE_RADIAN = 2 * Math.PI,
    QUARTER_RADIAN = ONE_RADIAN/4,
    HALF_RADIAN = ONE_RADIAN/2;

  /**
   * Calculate the radian for given degree
   */
  public static double getRadian(double degree) {
    return degree/360*Geometry.ONE_RADIAN;
  }
  
  /**
   * Calculate radian for given vector
   */
  public static double getRadian(Point2D vector) {
    double dx = vector.getX();
    double dy = vector.getY();
    double r = Math.sqrt(dx*dx + dy*dy);
    double result = dy < 0 ? Math.asin(dx/r) : HALF_RADIAN - Math.asin(dx/r) ;
    if (result<0)
      result += ONE_RADIAN;
    return result;
  }

  /**
   * Calculates the closest point from a list of points
   */
  public static Point2D getClosest(Point2D point, Collection<Point2D> points) {
    if (points.size()==0)
      throw new IllegalArgumentException();
    // assume first
    Point2D result = null;
    double distance = Double.MAX_VALUE;
    for (Point2D p : points) {
      double d = p.distance(point);
      if (d<=distance) {
        result = p;
        distance = d;
      }
    }
    // done
    return result;
  }
  
  /**
   * Calculates the farthest point from a list of points
   */
  public static Point2D getFarthest(Point2D point, Collection<Point2D> points) {
    if (points.size()==0)
      throw new IllegalArgumentException();
    // assume first
    Point2D result = null;
    double distance = 0;
    for (Point2D p : points) {
      double d = p.distance(point);
      if (d>=distance) {
        result = p;
        distance = d;
      }
    }
    // done
    return result;
  }
  
  /**
   * Calculates the "maximum" that the given shape extends into the given direction
   */
  public static Point2D getMax(Shape shape, double axis) {
    return new OpGetMax(shape, axis).getResult();
  }
  
  /**
   * Operation - calc maximum of a shape extending into a direction 
   */
  private static class OpGetMax implements FlattenedPathConsumer {

    private Point2D vector;
    private Point2D result;

    /**
     * Constructor
     */
    public OpGetMax(Shape shape, double axis) {

      axis = axis - QUARTER_RADIAN;

      vector = new Point2D.Double(
          Math.sin(axis) * 1, -Math.cos(axis) * 1
      );
      
      ShapeHelper.iterateShape(shape, this);
    }

    /**
     * Check points of a segment
     */
    public boolean consumeLine(Point2D start, Point2D end) {

      if (result==null) {
        result = new Point2D.Double(end.getX(), end.getY());
      } else {
        if (testPointVsLine(result, new Point2D.Double(result.getX() + vector.getX(), result.getY() + vector.getY()), end)>0)
          result.setLocation(end);
      }
      
      // continue
      return true;
    }
    
    /**
     * the result
     */
    Point2D getResult() {
      return result;
    }
  } //OpOriginOfTangent
  
  /**
   * Calculates the distance of two shapes along the given axis. 
   * For non-'parallel' shapes the result is Double.MAX_VALUE.
   * @param shape1 first shape
   * @param shape2 second shape
   * @param axis radian of axis (zero is north for vertical distance of two shapes)
   * @return distance
   */
  public static double getDistance(Shape shape1, Shape shape2, double axis) {
    return new OpShapeShapeDistance(shape1, shape2, axis).getResult();
  }
  
  /**
   * Operation - calculate distance of two shapes
   */
  private static class OpShapeShapeDistance implements FlattenedPathConsumer {
    
    private double result = Double.POSITIVE_INFINITY;
    private double axis;
    
    /** the axis vector we're measuring distance on */
    private Point2D vector;

    /** the current shape we're intersecting against */
    private Shape intersectWith;
    
    
    /**
     * Constructor
     */
    protected OpShapeShapeDistance(Shape shape1, Shape shape2, double axis) throws IllegalArgumentException {

      // keep an axis vector
      if (axis==QUARTER_RADIAN)
        vector = new Point2D.Double(1,0);
      else 
        vector = new Point2D.Double(Math.sin(axis),-Math.cos(axis));
      
      // iterate over shape1 intersecting lines along the axis with shape2
      this.axis = axis;
      intersectWith = shape2;
      ShapeHelper.iterateShape(shape1, this);
      
      // iterate over shape2 intersecting lines along the axis with shape1
      this.axis += HALF_RADIAN;
      intersectWith = shape1;
      ShapeHelper.iterateShape(shape2, this);
      
      // done
    }
    
    /**
     * The result
     */
    protected double getResult() {
      return result;
    }

    /**
     * only expecting lines to consume
     */
    public boolean consumeLine(Point2D start, Point2D end) {
      
      // create a line along axis going through 'end' 
      Point2D
        a = new Point2D.Double(end.getX()+vector.getX(), end.getY()+vector.getY()),
        b = new Point2D.Double(end.getX()-vector.getX(), end.getY()-vector.getY());

      // intersect line (a,b) with shape
      Collection<Point2D> is = getIntersections(a, b, true, intersectWith);

      // calculate smallest distance
      Point2D p = null;
      for (Point2D i : is) {
        result = Math.min(result, Math.sin(axis)*(i.getX()-end.getX()) - Math.cos(axis)*(i.getY()-end.getY()));
      }
      
      // continue
      return true;
    }
  } //OpShapeShapeDistance
  
  /**
   * Calculates the distance of a line (infinite) and a shape
   * @param lineStart line's start point
   * @param lineEnd line's end point
   * @param shape the shape
   */
  public static double getDistance(Point2D lineStart, Point2D lineEnd, PathIterator shape) {
    return new OpLineShapeDistance(lineStart, lineEnd, shape).getResult();
  }
  
  /**
   * Operation - calculate distance of line and shape
   */
  private static class OpLineShapeDistance implements FlattenedPathConsumer {
    
    /** resulting distance */
    private double delta = Double.MAX_VALUE;
    
    /** our criterias */
    private double lineStartX, lineStartY, lineEndX, lineEndY;
    
    /**
     * Constructor
     */
    protected OpLineShapeDistance(Point2D lineStart, Point2D lineEnd, PathIterator shape) {
      // remember
      lineStartX = lineStart.getX();
      lineStartY = lineStart.getY();
      lineEndX   = lineEnd.getX();
      lineEndY   = lineEnd.getY();
      // iterate over line segments in shape
      ShapeHelper.iterateShape(shape, this);
      // done
    }
    
    /**
     * Accessor - result
     */
    protected double getResult() {
      return delta;
    }
    
    /**
     * Callback - since we're using a flattening path iterator
     * only lines have to be consumed
     */
    public boolean consumeLine(Point2D start, Point2D end) {
      // calculate distance of line segment's start/end
      delta = Math.min(delta, Line2D.ptLineDist(lineStartX, lineStartY, lineEndX, lineEndY, start.getX(), start.getY()));
      delta = Math.min(delta, Line2D.ptLineDist(lineStartX, lineStartY, lineEndX, lineEndY, end  .getX(), end  .getY()));
      // continue
      return true;
    }
    
  } //OpLineShapeDistance
  
  /**
   * Calcualte the maximum distance of a point from given line segments in shape
   */
  public static double getMaximumDistance(Point2D point, Shape shape) {
    return new OpPointShapeMaxDistance(point, shape).getResult();
  }
  
  /**
   * Operation - calculate maximum distance of point from shape
   */
  private static class OpPointShapeMaxDistance implements FlattenedPathConsumer {
    
    private double result = Double.NEGATIVE_INFINITY;
    private Point2D point;
    
    /**
     * Constructor
     */
    protected OpPointShapeMaxDistance(Point2D point, Shape shape) {
      this.point = point;
      ShapeHelper.iterateShape(shape, this);
    }
    
    /**
     * The result
     */
    protected double getResult() {
      return result;
    }
    
    /**
     * @see gj.geom.FlattenedPathConsumer#consumeLine(java.awt.geom.Point2D, java.awt.geom.Point2D)
     */
    public boolean consumeLine(Point2D start, Point2D end) {
      result = Math.max(result, start.distance(point));
      result = Math.max(result, end.distance(point));
      return true;
    }
    
  } //OpPointShapeMaxDistance  
  
  /**
   * Calculates the minimum distance of a point and line segments in shape
   * @return distance or 0 for containment
   */
  public static double getMinimumDistance(Point2D point, PathIterator shape) {
    return new OpPointShapeMinDistance(point, shape).getResult();
  }
  
  /**
   * Operation - calculate distance of line and shape
   */
  private static class OpPointShapeMinDistance implements FlattenedPathConsumer {
    private double result = Double.MAX_VALUE;
    private Point2D point;
    
    /**
     * Constructor
     */
    protected OpPointShapeMinDistance(Point2D point, PathIterator shape) {
      this.point = point;
      ShapeHelper.iterateShape(shape, this);
    }
    
    /**
     * The result
     */
    protected double getResult() {
      return result;
    }
    
    /**
     * @see gj.geom.FlattenedPathConsumer#consumeLine(java.awt.geom.Point2D, java.awt.geom.Point2D)
     */
    public boolean consumeLine(Point2D start, Point2D end) {
      
      double distance = Line2D.ptSegDist(start.getX(), start.getY(), end.getX(), end.getY(), point.getX(), point.getY());
      result = Math.min(result, distance);
      return result!=0;
    }
    
  } //OpPointShapeMinDistance
  
  /**
   * Calculates the angle between two vectors
   * <pre>
   *  (a1,a2) -> (b1,b2) -> atan( dy / dx )
   * 
   *   where dx = b1-a1
   *         dy = b2-a2
   * 
   *  (* taking the quadrant into consideration)
   * </pre>
   */
  public static double getAngle(Point2D vectorA, Point2D vectorB) {
    return (  Math.atan2( (vectorB.getY()-vectorA.getY()), (vectorB.getX()-vectorA.getX()) ) - 2*Math.PI/4) % (2*Math.PI);
  }
  
  /**
   * Calculate the cross-product of two vectors. The cross-product
   * of two vectors points "upwards" when the rotation (using the
   * shorter way) which "twist" the first vector into the second 
   * one is a left rotation, otherwise it points "downwards". The 
   * length of the vector corresponds to the area of the 
   * parallelogram spanned by the vectors.
   * 
   * <pre>
   *   (a1,a2) -> (b1,b2) -> a1*b2 - b1*a2
   * </pre>
   */
  public static double getCrossProduct(Point2D vectorA, Point2D vectorB) {
    return getCrossProduct( vectorA.getX(), vectorA.getY(), vectorB.getX(), vectorB.getY());
  }
  
  /**
   * Calculate the cross-product of two vectors. The cross-product
   * of two vectors points "upwards" when the rotation (using the
   * shorter way) which "twist" the first vector into the second 
   * one is a left rotation, otherwise it points "downwards". The 
   * length of the vector corresponds to the area of the 
   * parallelogram spanned by the vectors.
   * 
   * <pre>
   *   (a1,a2) -> (b1,b2) -> a1*b2 - b1*a2
   * </pre>
   */
  public static double getCrossProduct(double vectorAx, double vectorAy, double vectorBx, double vectorBy) {
    return ( vectorAx*vectorBy ) - ( vectorBx*vectorAy );
  }

  /**
   * Calculate a point with given distance of origin
   * @param origin the original point
   * @param radian radian of direction
   * @param distance distance
   * @return point with distance in direction radian
   */
  public static Point2D getPoint(Point2D origin, double radian, double distance) {
    return new Point2D.Double( origin.getX() + Math.sin(radian)*distance, origin.getY() - Math.cos(radian) * distance);
  }

  public static Point2D getVector(Point2D origin, double radian) {
    return getPoint(origin, radian, 1);
  }
  
  /**
   * (x1,y1) -> (x2,y2) -> factor-> (x1 + (x2-x1)*factor, y1 + (y2-y1)*factor )
   */
  public static Point2D getPoint(Point2D p1, Point2D p2, double share) {
    if (share<0||share>1)
      throw new IllegalArgumentException("0 <= share <= 1");
    return new Point2D.Double( p1.getX() + (p2.getX()-p1.getX())*share, p1.getY() +  (p2.getY()-p1.getY())*share  );
  }
  
  public static Point2D getPoint(Point2D p, double factor) {
    return new Point2D.Double(p.getX()*factor, p.getY()*factor);
  }
  
  /**
   * substracts a from b
   * <pre>
   *   (a1,a2) -> (b1,b2) -> (b1-a1, b2-a2)
   * </pre>
   */
  public static Point2D getDelta(Point2D vectorA, Point2D vectorB) {
    return new Point2D.Double(vectorB.getX()-vectorA.getX(), vectorB.getY()-vectorA.getY());
  }
  
  public static Point2D getMid(Point2D a, Point2D b) {
    return new Point2D.Double( (a.getX()+b.getX())/2, (a.getY()+b.getY())/2); 
  }
  
  public static Point2D getNeg(Point2D p) {
    return new Point2D.Double(-p.getX(), -p.getY());
  }
  
  /**
   * adds a to b
   * <pre>
   *   (a1,a2) -> (b1,b2) -> (a1+b1, a2+b2)
   * </pre>
   */
  public static Point2D getSum(Point2D vectorA, Point2D vectorB) {
    return new Point2D.Double(vectorB.getX()+vectorA.getX(), vectorB.getY()+vectorA.getY());
  }
  
  /**
   * The length of a vector
   * <pre>
   *  c^2=x^2+y^2 => c=sqt(x^2+y^2)
   * </pre>
   */    
  public static double getLength(double x, double y) {
    return Math.sqrt(x*x + y*y);
  }
  
  public static double getLength(Point2D v) {
    return getLength(v.getX(), v.getY());
  }
  
  public static Shape getTranslated(Shape s, Point2D d) {
    GeneralPath result = new GeneralPath(s);
    result.transform(AffineTransform.getTranslateInstance(d.getX(), d.getY()));
    return result;
  }
  
  /**
   * Calculates the intersecting points of a line and a shape
   * @param lineStart start of line
   * @param lineEnd end of line
   * @param shape the shape
   */
  public static List<Point2D> getIntersections(Point2D lineStart, Point2D lineEnd, Shape shape) {
    return new OpLineShapeIntersections(lineStart, lineEnd, false, shape.getPathIterator(null)).result;
  }
  
  /**
   * Calculates the intersecting points of a line and a shape
   * @param lineStart start of line
   * @param lineEnd end of line
   * @param infinite whether line is infinite or not (segment)
   * @param shape the shape
   */
  public static List<Point2D> getIntersections(Point2D lineStart, Point2D lineEnd, boolean infinite, PathIterator shape) {
    return new OpLineShapeIntersections(lineStart, lineEnd, infinite, shape).result;
  }
  
  /**
   * Calculates the intersecting points of a line and a shape
   * @param lineStart start of line
   * @param lineEnd end of line
   * @param infinite whether line is infinite or not (segment)
   * @param shape the shape 
   */
  public static List<Point2D> getIntersections(Point2D lineStart, Point2D lineEnd, boolean infinite, Shape shape) {
    return getIntersections(lineStart, lineEnd, infinite, shape.getPathIterator(null));
  }
  
  /**
   * Operation - intersect line and shape
   */
  private static class OpLineShapeIntersections implements FlattenedPathConsumer {
    
    /** the intersections */
    private List<Point2D> result;
    
    /** it's distance */
    private double distance = Double.MAX_VALUE;
    
    /** our criterias */
    private Point2D lineStart, lineEnd;
    
    /** whether we're looking at line segment or infinite line */
    private boolean infinite;
    
    /**
     * Constructor
     */
    protected OpLineShapeIntersections(Point2D lineStart, Point2D lineEnd, boolean infinite, PathIterator shape) {
      // remember
      this.result = new ArrayList<Point2D>(10);
      this.lineStart = lineStart;
      this.lineEnd   = lineEnd;
      this.infinite = infinite;
      // iterate over line segments in shape
      ShapeHelper.iterateShape(shape, this);
      // done
    }
    
    /**
     * Callback - since we're using a flattening path iterator
     * only lines have to be consumed
     */
    public boolean consumeLine(Point2D start, Point2D end) {
      Point2D p = getIntersection(lineStart, lineEnd, infinite, start, end, false);
      if (p!=null) 
        result.add(p);
      return true;
    }
    
  } //Op

  /**
   * Tests for intersection of two lines (segments of finite length)
   * @param aStart line segment describing line A
   * @param aEnd line segment describing line A
   * @param bStart line segment describing line B
   * @param bEnd line segment describing line B
   */
  public static boolean testIntersection(Point2D aStart, Point2D aEnd, Point2D bStart, Point2D bEnd) {
    
    // To test for crossing we hold a line and check both endpoints' being left/right of it
    //
    //        |         b
    //       /|        /b\
    //      / |       / b \
    //    aaaaaaaaa ----b----
    //      \ |         b
    //       \|         b
    //        |         b 

    Point2D vectorA = getDelta(aStart, aEnd),   // a-a
            vector1 = getDelta(aStart, bStart), // a-b1
            vector2 = getDelta(aStart, bEnd);   // a-b2

    // .. cross-product is '-' for 'left' and '+' for right
    // so we hope for xp(aa,ab1) * x(aa,ab2) < 0 because
    //
    //   + * + = + 
    //   - * - = + 
    //   + * - = -
    //
    if (getCrossProduct(vectorA,vector1) * getCrossProduct(vectorA,vector2) >0) {
      return false;
    }
    
    // The same for the other line
    Point2D vectorB = getDelta(bStart, bEnd);   // b-b
            vector1 = getDelta(bStart, aStart); // b-a1
            vector2 = getDelta(bStart, aEnd);   // b-a2
        
    if (getCrossProduct(vectorB,vector1) * getCrossProduct(vectorB,vector2) >0) {
      return false;
    }
  
    // Yes, they do
    return true;  
  }
  
  /**
   * Calculates the intersecting point of finite line segments
   * @param aStart line segment describing line A
   * @param aEnd line segment describing line A
   * @param bStart line segment describing line B
   * @param bEnd line segment describing line B
   * @return either intersecting point or null if lines are parallel or line segments don't cross
   */
  public static Point2D getIntersection(Point2D aStart, Point2D aEnd, Point2D bStart, Point2D bEnd) {
    return getIntersection(aStart, aEnd, false, bStart, bEnd, false);
  }
  
  /**
   * Calculates the intersecting point of two lines
   * @param aStart line a
   * @param aEnd line a
   * @param aInfinite whether a is infinite or not
   * @param bStart line b
   * @param bEnd line b
   * @param bInfinite whether a is infinite or not
   * @return either intersecting point or null if lines are parallel or line segments don't cross
   */
  public static Point2D getIntersection(Point2D aStart, Point2D aEnd, boolean aInfinite, Point2D bStart, Point2D bEnd, boolean bInfinite) {
    
    // infinite case - do they intersect at all?
    Point2D i = getLineIntersection(aStart, aEnd, bStart, bEnd);
    if (i==null)
      return null;

    // b !infinite? check that bStart&bEnd are not on the same side of aStart->aEnd
    if (!bInfinite && testPointVsLine(aStart, aEnd, bStart)*testPointVsLine(aStart, aEnd, bEnd) > 0)
      return null;
    
    // a !infinite ? check that aStart&aEnd are not on the same side of bStart->bEnd 
    if (!aInfinite && testPointVsLine(bStart, bEnd, aStart)*testPointVsLine(bStart, bEnd, aEnd) > 0)
      return null;
    
    return i;
    
//    // infinite case - do they intersect at all?
//    if (!testIntersection(aStart,aEnd,bStart,bEnd)) 
//      return null;
//    
//    // We calculate the direction vectors a, b and c
//    //
//    //     AS b  BE
//    //      |\ \/
//    //    c-| \/
//    //      | /\  a
//    //      |/  \/
//    //     BS    \
//    //            AE
//    //
//    // Note equations for lines AS-AE, BS-BE, BS-AS
//    //
//    //  y = AS + s*v_a
//    //  y = BS + t*v_b
//    //  y = BS + u*v_c
//    //
//    double 
//      v_ax = aEnd.getX() - aStart.getX(),
//      v_ay = aEnd.getY() - aStart.getY(),
//      v_bx = bEnd.getX() - bStart.getX(),
//      v_by = bEnd.getY() - bStart.getY(),
//      v_cx = bStart.getX() - aStart.getX(),
//      v_cy = bStart.getY() - aStart.getY();
//
//    // Then we calculate the cross-product between
//    // vectors b/a and b/c
//    //
//    //  cp_ba = v_a x v_b
//    //  cp_bc = v_b x v_c
//    //
//    double cp_ba = getCrossProduct(v_bx,v_by,v_ax,v_ay);
//    double cp_bc = getCrossProduct(v_bx,v_by,v_cx,v_cy);
//    
//    // A zero x-prod means that the lines are either
//    // parallel or have coinciding endpoints
//    if (cp_ba==0) {
//      return null;
//    }
//    
//    // So our factor s for lines AS-AE is
//    //
//    // s = cp_bc/cp_ba
//    //
//    double s = cp_bc/cp_ba;
//    
//    // The result is defined by
//    //
//    //  AS + s * v_a
//    //
//    return new Point2D.Double(
//      aStart.getX()+s*v_ax,
//      aStart.getY()+s*v_ay
//    );
    
  }
  
  /**
   * Calculate the intersection point of two infinite lines defined by respective point and angle
   */
  public static Point2D getIntersection(Point2D pointA, double radianA, Point2D pointB, double radianB) {
    return getLineIntersection(
        pointA, 
        new Point2D.Double(pointA.getX() + Math.sin(radianA), pointA.getY() - Math.cos(radianA)),
        pointB,
        new Point2D.Double(pointB.getX() + Math.sin(radianB), pointB.getY() - Math.cos(radianB))
    );
  }
  
  /**
   * Calculate the intersection point of two infinite lines defined by two points each
   */
  public static Point2D getLineIntersection(Point2D aStart, Point2D aEnd, Point2D bStart, Point2D bEnd) {
    
    // a1*x + b1*y + c1 = 0 is line 1
    double a1 = aEnd.getY() - aStart.getY();
    double b1 = aStart.getX() - aEnd.getX();
    double c1 = aEnd.getX()*aStart.getY() - aStart.getX()*aEnd.getY();  
    
    // a2*x + b2*y + c2 = 0 is line 2 
    double a2 = bEnd.getY()-bStart.getY();
    double b2 = bStart.getX()-bEnd.getX();
    double c2 = bEnd.getX()*bStart.getY() - bStart.getX()*bEnd.getY();

    // check if lines are parallel
    // TODO review rounding error handling - for sufficiently small denominator we assume parallel
    double denom = a1*b2 - a2*b1;
    if (Math.abs(denom)< 0.000000001)
      return null;

    return new Point2D.Double( (b1*c2 - b2*c1)/denom , (a2*c1 - a1*c2)/denom);
  }
  
  /**
   * Calculate the 2D bounds of given iterator
   */
  public static Rectangle2D getBounds(Shape shape) {
  	return new OpShapeBounds(shape).getResult();
  }

  /**
   * Operation - calculate bounds of path iterator
   */
  private static class OpShapeBounds implements FlattenedPathConsumer {
    private Rectangle2D result;
    protected OpShapeBounds(Shape shape) {
      ShapeHelper.iterateShape(shape, this);
    }
    protected Rectangle2D getResult() {
      return result;
    }
    private void add(Point2D p) {
      if (result==null) {
        result = new Rectangle2D.Double(p.getX(),p.getY(),0,0);
      } else {
        result.add(p);
      }
    }
    public boolean consumeLine(Point2D start, Point2D end) {
      add(start);
      add(end);
      return true;
    }
  } //OpBounds
  
  
  /** 
   * Calculate the area of a rectangle
   */
  public static double getArea(Point2D a, Point2D b, Point2D c) {
    return Math.abs(_getArea(a,b,c));
  }
  private static double _getArea(Point2D a, Point2D b, Point2D c) {
    
    // we're using a geometric technique 
    // see http://www.richland.edu/james/lecture/m116/matrices/applications.html
    
    //       | x0 y0 1 |
    // 1/2 * | x1 y1 1 | = (x1*y2 - y1*x2 -x0*y2 + y0*x2 + x0*y1 - y0*x1)
    //       | x2 y2 1 |
    //
    double x0 = a.getX();
    double y0 = a.getY();
    double x1 = b.getX();
    double y1 = b.getY();
    double x2 = c.getX();
    double y2 = c.getY();
    double d =  (x1*y2 - y1*x2 -x0*y2 + y0*x2 + x0*y1 - y0*x1);
    return d/2;
    
  }
  
  /**
   * Test where a point lies in relation to a line
   * @return <0 if left, 0 if on, >0 if right of line
   */
  public static double testPointVsLine(Point2D start, Point2D end, Point2D point) {
    // for a triangle with corners a,b,c the determinant area calculation will return <0 
    // for counter-clockwise and >0 otherwise 
    return _getArea(start, end, point);
  }
  
  /**
   * Calculate the convex hull of a shape
   */
  public static ConvexHull getConvexHull(Shape shape) {
    // check understood objects
    if (shape instanceof Rectangle2D) {
      Rectangle2D r = (Rectangle2D)shape;
      ConvexHullImpl result = new ConvexHullImpl();
      result.moveTo(r.getMinX(), r.getMinY());
      result.lineTo(r.getMaxX(), r.getMinY());
      result.lineTo(r.getMaxX(), r.getMaxY());
      result.lineTo(r.getMinX(), r.getMaxY());
      result.closePath();
      return result;
    }
    // check marker
    if (shape instanceof ConvexHull)
      return (ConvexHull)shape;
    // create
    return getConvexHull(shape.getPathIterator(null));
  }
  public static ConvexHull getConvexHull(PathIterator shape) {
    
    // using the gift wrapping algorithm
    // see http://www.cse.unsw.edu.au/~lambert/java/3d/giftwrap.html
    
    // collect all points and find the point with lowest y coordinate (our start)
    // 20090209 added x comparison to find lowest/leftmost point
    final LinkedList<Point2D> points = new LinkedList<Point2D>();
    final Point2D start = new Point2D.Double(0, Double.MAX_VALUE);
    ShapeHelper.iterateShape(shape, new FlattenedPathConsumer() {
      public boolean consumeLine(Point2D from, Point2D to) {
        // replace current starting point or keep?
        if (start.getY()==Double.MAX_VALUE)
          start.setLocation(from);

          if (to.getY()<start.getY() || (to.getY()==start.getY() && to.getX()<start.getX()) ) {
            points.add(new Point2D.Double(start.getX(), start.getY()));
            start.setLocation(to.getX(), to.getY());
          } else {
            points.add(new Point2D.Double(to.getX(), to.getY()));
          }

        // continue with 'to'
        return true;
      }
    });
    
    // closing point
    points.add(new Point2D.Double(start.getX(), start.getY()));

    // iterate over sides of hull
    ConvexHullImpl result = new ConvexHullImpl();
    result.moveTo( (float)start.getX(), (float)start.getY());
    Point2D from = start;
    while (!points.isEmpty()) {
      
      // 'random' next point
      Point2D to = points.removeFirst();

      // compare to others
      for (ListIterator<Point2D> others = points.listIterator(); others.hasNext(); ) {
        Point2D other = others.next();
        if (to.equals(from) || (!other.equals(from)&&testPointVsLine(from, to, other)<0)) {
          Point2D xchange = to; to = other; others.set(xchange);
        }
      }
      
      // draw outside hull
      result.lineTo( (float)to.getX(), (float)to.getY());
      // back at starting point?
      from =to;
      if (from.equals(start))
        break;
    }
    
    // done
    return result;
  }
  
  /**
   * an impl for a convex hull marked shape
   */
  private static class ConvexHullImpl extends java.awt.geom.Path2D.Double implements ConvexHull {
    
  } //ConvexHullImpl

} //Geometry
