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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test Geometry type
 */
public class GeometryTest extends TestCase {
  
  private double TwoPi = Math.PI*2;
  
  public void testInterpolation() {
    
    
    tst( Geometry.getInterpolation(0, r(0,0,1,1), r(10,10,1,1)), r(0,0,1,1) );
    tst( Geometry.getInterpolation(1, r(0,0,1,1), r(10,10,1,1)), r(10,10,1,1) );
    
    tst( Geometry.getInterpolation(0.5, r(0,0,1,1), r(10,10,1,1)), r(5,5,1,1) );
    
    Shape s1 = ShapeHelper.createShape(p(1,1), p(1,0), p(0,0), p(0,1));
    Shape s2 = ShapeHelper.createShape(p(0,0), p(0,1), p(1,1), p(1,0));
    tst( Geometry.getInterpolation(0.5, s1, s2), r(0.5,0.5,0,0) );
    
  }
  
  public void testAlignment() {
    
    // +-------->
    //    rrr
    // +--r-r--->
    //    rrr
    // +-------->
    
    tst( r(10,-5,10,10), ShapeHelper.createShape( r(10,10,10,10), p(0,0), p(1,0), -1  ) );
    tst( r(10, 0,10,10), ShapeHelper.createShape( r(10,10,10,10), p(0,0), p(1,0), 0) );
    tst( r(10, 5,10,10), ShapeHelper.createShape( r(10,10,10,10), p(0,0), p(1,0), +1 ) );

    //  + + +
    //  | | |
    //  |rrr|
    //  |r|r|
    //  |rrr|
    //  | | |
    //  V V V
    
    tst( r( 5,10,10,10), ShapeHelper.createShape( r(10,10,10,10), p(0,0), p(0,1), -1  ) );
    tst( r( 0,10,10,10), ShapeHelper.createShape( r(10,10,10,10), p(0,0), p(0,1), 0) );
    tst( r(-5,10,10,10), ShapeHelper.createShape( r(10,10,10,10), p(0,0), p(0,1), +1 ) );

    //      A  A  A
    //     /  /  /
    //    /rrr  /
    //   / r/r /
    //  /  rrr/
    // /  /  /
    //+  +  +
    tst( r(-5,-5,10,10), ShapeHelper.createShape( r(10,10,10,10), p(-1,1), p(1,-1), -1  ) );
    tst( r( 0, 0,10,10), ShapeHelper.createShape( r(10,10,10,10), p(-1,1), p(1,-1), 0) );
    tst( r( 5, 5,10,10), ShapeHelper.createShape( r(10,10,10,10), p(-1,1), p(1,-1), +1 ) );
  }
  
  public void testConvexHull() {

    GeneralPath gp = new GeneralPath();
    gp.moveTo(1,0);
    gp.lineTo(0,0);
    gp.lineTo(2, 0);
    gp.lineTo(2,1);
    gp.lineTo(0, 1);
    gp.lineTo(0, 0);
    tst(Geometry.getConvexHull(gp), r(1,0.5,2,1));

    // 
    //  +--+--+
    //  |  |  |
    //  +--+--+
    //
    
    gp = new GeneralPath();
    gp.append(r(0.5,0.5), false);
    gp.append(r(1.5,0.5), false);
    
    tst(Geometry.getConvexHull(gp), r(1,0.5,2,1));
    
    // 
    //  +--+--+
    //   \ | /  
    //     + 
    //
    
    gp = new GeneralPath();
    gp.moveTo(0, 0);
    gp.lineTo(1, 0);
    gp.lineTo(0, 1);
    gp.lineTo(-1, 0);
    gp.lineTo(0, 0);
    gp.lineTo(0, 1);

    tst(Geometry.getConvexHull(gp),
      new Polygon(
        new int[]{-1,0,1},
        new int[]{0 ,1,0},
        3
      ));

  }
  
  private void tst(Shape s1, Shape s2) {
    Area a = new Area(s1);
    a.subtract(new Area(s2));
    assertTrue(a.isEmpty());
    a = new Area(s2);
    a.subtract(new Area(s1));
    assertTrue(a.isEmpty());
  }
  
  public void testPointLine() {
    
    //  x
    // --->
    //
    assertTrue( Geometry.testPointVsLine( p(0, 0), p(2, 0), p(1, 1)) > 0);
    
    //   x
    // <---
    //   
    assertTrue( Geometry.testPointVsLine(p(2, 0), p(1, 0), p(1, 1)) < 0);
    
    assertTrue( Geometry.testPointVsLine(p(0,0), p(1,1), p(0,1)) > 0);
    assertTrue( Geometry.testPointVsLine(p(0,0), p(1,1), p(1,1)) == 0);

    //
    // <---
    //   x
    assertTrue( Geometry.testPointVsLine(p(0, 0), p(-1, 0), p(-0.5, -0.5)) > 0);
    
    //  |
    //  | x
    //  V
    assertTrue( Geometry.testPointVsLine(p(0, 0), p(0, -1), p(0.5, 0.5)) > 0);
    
    assertTrue( Geometry.testPointVsLine(p(0, -1), p(0, -2), p(1, 0)) > 0);

    //  ^
    //  | x
    //  |
    assertTrue( Geometry.testPointVsLine( p(-1, 0), p(-1, 1), p(1, 0.5)) < 0);
    
    assertTrue( Geometry.testPointVsLine(p(0, -1), p(0, 1), p(1, 0)) < 0);
    
    assertTrue( Geometry.testPointVsLine(p(0, -1), p(0, 0), p(1, 1)) < 0);
      
      
//    //   ^
//    //   | x
//    //   |
//    assertFalse( Geometry.testPointLeftOfLine(
//        p(0, 0), p(0, 2), p( 1, 1)
//    ));
//    //   |
//    //   | x
//    //   V
//    assertTrue( Geometry.testPointLeftOfLine(
//        p(0, 2), p(0, 0), p(1, 1)
//    ));
//    
//    assertTrue( Geometry.testPointLeftOfLine(
//        p(0, 1), p(0, -1), p(1, 0)
//    ));
//    
//    assertFalse( Geometry.testPointLeftOfLine(
//        p(0, -2), p(0, -1), p(1, 0)
//    ));
  }
  
  public void testRectangleArea() {

    tst(  0.5, Geometry.getArea(p(0,0), p(1,0), p(0,1)) );
    tst(  0.5, Geometry.getArea(p(0,0), p(1,0), p(0,-1)) );
    
  }
  
  public void testRadian() {
    
    tst(radian(  0), Geometry.getRadian(p(0,-1000)));
    tst(radian( 45), Geometry.getRadian(p( 7, -7 )));
    tst(radian( 90), Geometry.getRadian(p(100,0  )));
    tst(radian(135), Geometry.getRadian(p( 99,99 )));
    tst(radian(180), Geometry.getRadian(p(0,10   )));
    tst(radian(225), Geometry.getRadian(p(-2,2   )));
    tst(radian(270), Geometry.getRadian(p(-1,0   )));
    tst(radian(315), Geometry.getRadian(p(-1,-1   )));
    
  }
  
  public void testShapeIntersections() {

    // some extreme close line intersection tests
    tst(
        Geometry.getIntersections(p(2.5, -0.5000000000000001), p(0.5, -0.49999999999999994), true, r(0,0)),
        p(-0.5, -0.49999999999999994), p(0.5, -0.49999999999999994)
    );
    
    tst(
        Geometry.getIntersections(p(1.5, -0.5000000000000001), p(-0.5, -0.49999999999999994), r(1,0)),
        p(0.5, -0.5)
    );
    tst(
        Geometry.getIntersections(p(-0.5, -0.5000000000000001), p(1.5, -0.49999999999999994), r(1,0)),
        p(1.5, -0.5)
    );
    
    
  }

  public void testIntersections() {
    
    // --
    tst( null, Geometry.getLineIntersection(p(0,0), p(1,0), p(1,0), p(2,0)));
    tst( null, Geometry.getIntersection(p(0,0), p(1,0), p(1,0), p(2,0)));
    
    // - -
    tst( null, Geometry.getLineIntersection(p(0,0), p(1,0), p(2,0), p(3,0)));
    tst( null, Geometry.getIntersection(p(0,0), p(1,0), p(2,0), p(3,0)));
    
    // --==--
    tst( null, Geometry.getLineIntersection(p(0,0), p(3,0), p(1,0), p(2,0)));
    tst( null, Geometry.getIntersection(p(0,0), p(3,0), p(1,0), p(2,0)));
    
    // | |
    tst( null, Geometry.getLineIntersection(p(0,0), p(0,1), p(1,0), p(1,1)));
    tst( null, Geometry.getIntersection(p(0,0), p(0,1), p(1,0), p(1,1)));

    // X
    tst( p(0.5,0.5), Geometry.getLineIntersection(p(0,0), p(1,1), p(0,1), p(1,0)));
    tst( p(0.5,0.5), Geometry.getIntersection(p(0,0), radian(135), p(0,1), radian(225)));

    // +
    tst( p(0,0), Geometry.getLineIntersection(p(-1,0), p(1,0), p(0,-1), p(0,1)));
    tst( p(0,0), Geometry.getIntersection(p(-1,0), p(1,0), p(0,-1), p(0,1)));
    
    // /
    //  \
    tst( p(0.5,0.5), Geometry.getLineIntersection(p(1,0), p(0,1), p(1,1), p(2,2)));
    tst( null, Geometry.getIntersection(p(1,0), p(0,1), p(1,1), p(2,2)));
    tst( p(0.5,0.5), Geometry.getIntersection(p(1,0), p(0,1), false, p(1,1), p(2,2), true));
    tst( p(0.5,0.5), Geometry.getIntersection(p(1,0), p(0,1), true, p(1,1), p(2,2), true));
    tst( null, Geometry.getIntersection(p(1,0), p(0,1), true, p(1,1), p(2,2), false));

    // | -
    tst( p(0,0.5), Geometry.getLineIntersection(p(0,0), p(0,1), p(2,0.5), p(3,0.5)));
    tst( null, Geometry.getIntersection(p(0,0), p(0,1), p(2,0.5), p(3,0.5)));
    
    // \/
    tst( p(1,1), Geometry.getIntersection(p(0,0), p(1,1), p(1,1), p(2,0)));
    tst( p(1,1), Geometry.getIntersection(p(0,0), p(1,1), p(2,0), p(1,1)));
        
    
    
  }
  
  /**
   * Test maximum of a shape calculations
   */
  public void testShapeMaximum() {
    
    // -----
    //  xxx
    //  x x
    //  xxx
    tst( 5, Geometry.getMax( r(10,10,10,10), radian(0)).getY() );
    
    //  xxx
    //  x x
    //  xxx
    // -----
    tst( 1, Geometry.getMax(  r(0.5,0.5), radian(180)).getY() );
    
    //  xxx|
    //  x x|
    //  xxx|
    tst( 0.5 , Geometry.getMax( r(0,0), radian(90)).getX() );
    
    //  xxx
    //  x x
    //  xxx/
    //    /
    tst(p(.5,.5), Geometry.getMax( r(0,0), radian(90+45)));
    
    //  /
    // /xxx
    //  x x
    //  xxx
    tst(p(.5,.5), Geometry.getMax( r(1,1), radian(270+45)));
    
    
    //    x  |
    //   x x |
    //  x   x|
    //   x x |
    //    x  |
  }
  
  /**
   * Test shape distance calculations
   */
  public void testShapeShapeDistance() {
    
    // xxx   
    // xoxo
    // xxxo
    //  ooo
    tst( -0.6, dist(r(0,0), r(0.4,0), radian(90)));
    tst( -1.4, dist(r(0.4,0), r(0,0), radian(90)));

    // ******
    //-*-**-*-
    // ****** 
    tst( 0, dist(r(0,0), r(1,0), radian(90))); 
    
    // *** ***
    //-*-*-*-*-
    // *** *** 
    tst( 1, dist(r(0,0), r(2,0), radian(90)));
    
    //  |
    // ***
    // *|* 
    // ***    
    // ***
    // *|*    
    // ***
    //  |
    tst( 0, dist(r(0,0), r(0,1), radian(180))); 

    //  |
    // ***
    // *|* 
    // ***    
    //  |
    // ***
    // *|*    
    // ***    
    //  |
    tst( 1, dist(r(0,0), r(0,2), radian(180)));
    
    // ***
    //-*-*-
    // ***    
    //
    // ***
    //-*-*-   
    // ***    
    tst( Double.POSITIVE_INFINITY, dist(r(0,0), r(0,2), radian(90)) );
    
    // ***
    //-*-*-***-
    // *** * *
    //     ***
    tst( 4, dist(r(0,0), r(5,0.5), radian(90)));
    
    // ***
    // * *    
    // ***    
    //    ***
    //    * *
    //    ***
    tst( 0, dist(r(0,0), r(1,1), radian(135)));
    
    // ***
    // *\*    
    // *** 
    //    \
    //     ***
    //     *\*
    //     ***
    tst( hypotenuse(4,4), dist(r(0,0), r(5,5), radian(135)));

    // ooo
    //-oxxx-
    // oxox
    //  xxx
    tst( -0.5, dist(r(0,0), r(0.5,0.5), radian(90)));
    
    //
    //  *   *
    //-*-*-*-*-
    //  *   *
    GeneralPath s1 = new GeneralPath(r(0,0));
    s1.transform(AffineTransform.getRotateInstance(radian(45)));
    GeneralPath s2 = new GeneralPath(s1);
    s2.transform(AffineTransform.getTranslateInstance(3,0));
    tst(3-hypotenuse(1,1), dist(s1,s2,radian(90)));
    
    //     ***
    // *** * *
    // * * ***
    // * **
    //  * *
    //  ***
    s1 = new GeneralPath(r(0,0)); 
    s1.append(r(0.5,0.5), true);
    s2 = new GeneralPath(r(3,-0.5));
    tst(1.5, dist(s1,s2,radian(90)));
    s2.transform(AffineTransform.getTranslateInstance(0,-0.1));
    tst(2, dist(s1,s2,radian(90)));
    
    // done
  }
  
  /**
   * equals test
   */
  private void tst(List<Point2D> ps1, Point2D... ps2) {
    
    assertEquals(ps2.length, ps1.size());
    
    i: for (int i=0;i<ps2.length;i++) {
      
      for (int j=0;j<ps1.size();j++) {
        if (equals(ps1.get(j), ps2[i]))
          continue i;
      }
      
      fail("can't fine "+ps2[i]+" in ps1");
    }
    
    
  }
  
  /**
   * equals test
   */
  private void tst(Point2D a, Point2D b) {
    if (a==b)
      return;
    if (a==null) {
      assertNull("expected null but got "+b,b);
      return;
    }
    if (b==null) {
      assertNull("expected null but got "+a,a);
      return;
    }
      
    if (!equals(a,b))
      fail("expected "+a+" but got "+b);
  }
  
  private boolean equals(Point2D a, Point2D b) {
    // TODO review testing points arithmetics - for sufficiently small delta we assume equals
    return Math.abs(a.getX()-b.getX()) < 0.0000001 && Math.abs(a.getY()-b.getY())<0.0000001;
  }
  
  /**
   * equals test
   */
  private void tst(double a, double b) {
    // TODO review testing double arithmetics - for sufficiently small delta we assume equals
    assertEquals(a,b,0.0000001);
  }
  
  /**
   * calculate radian from degree
   */
  private double radian(double degree) {
    return TwoPi/360*degree;
  }
  
  /**
   * calculate the distance of two shapes
   */
  private double dist(Shape s1, Shape s2, double angle) {
    return Geometry.getDistance(s1,s2,angle);
  }
  
  /** 
   * calculate the hypotenuse for given a and b
   * @return sqrt(a*a+b*b)
   */
  private double hypotenuse(double a, double b) {
    return Math.sqrt(a*a + b*b);
  }

  /**
   * create a rectangle centered around (x,y) with size (1,1)
   */
  private Rectangle2D r(double x, double y) {
    return new Rectangle2D.Double(x-0.5,y-0.5,1,1);
  }
  
  /**
   * create a rectangle centered around (x,y) with size (w,h)
   */
  private Rectangle2D r(double x, double y, double w, double h) {
    return new Rectangle2D.Double(x-w/2,y-h/2,w,h);
  }
  
  private Point2D p(double x, double y) {
    return new Point2D.Double(x,y);
  }
  
}
