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
package genj.util.swing;

import genj.util.ByteArray;
import genj.util.Dimension2d;
import genj.util.ImageSniffer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.GrayFilter;

/**
 * Improved ImageIcon
 * <il>
 *  <li>can be read conveniently as resource for object or class
 *  <li>knows about image resolution 
 * </il> */
public class ImageIcon extends javax.swing.ImageIcon {
  
  /** dpi */
  private Point dpi = null;
  
  /** cached overlayed icons */
  private Map overlays = new WeakHashMap();
  
  /**
   * Private special
   */
  public ImageIcon(Image copy) {
    super(copy);
  }
  
  /** 
   * Overriden default
   */
  public ImageIcon(String naMe, byte[] data) {
    super(data);

    if (getImageLoadStatus()!=MediaTracker.COMPLETE)
      throw new RuntimeException("load status incomplete");

    // 20040304 checking for valid width/height now - just discovered
    // that ImageIcon might otherwise accept no-image date without
    // a notice (and then ProxyFile not realizing it's NOT an image) 
    if (getIconHeight()<0||getIconWidth()<0)
      throw new RuntimeException("image with invalid width/height");

    // keep name & bytes
    setDescription(naMe);

    // sniff resolution
    String msg;
    
    dpi = new ImageSniffer(new ByteArrayInputStream(data)).getDPI();
    
    // done
  }
    /**
   * Alternative Constructor
   */
  public ImageIcon(Object from, String resource) {
    this(from.getClass(), resource);
  }
  
  private static String patchPNG(String resource) {
    return resource.indexOf('.')<0 ? resource+".png" : resource;
  }
  
  /**
   * Alternative Constructor
   */
  public ImageIcon(Class from, String resource) {
    this(from.getName()+'#'+resource, from.getResourceAsStream(patchPNG(resource)));
  }
  
  /**
   * Alternative Constructor
   */
  public ImageIcon(String name, InputStream in) {
    this(name, read(name, in));
  }
  
  /**
   * Returns resolution (dpi)
   * @return resolution in dpi or null if not known
   */
  public Point getResolution() {
    return dpi;
  }

  /**
   * Size in inches
   * @return size in inches or null if not known
   */
  public Dimension2D getSizeInInches() {
    // check whether we have a resolution
    if (dpi==null) 
      return null;
    return new Dimension2d(
      (double)getIconWidth ()/dpi.x, 
      (double)getIconHeight()/dpi.y
    );
  }
  
  /**
   * Size in points of give target space resolution
   */
  public Dimension getSizeInPoints(Point dpiTarget) {
    Dimension2D sizeInInches = getSizeInInches();
    if (sizeInInches==null) 
      return new Dimension(getIconWidth(), getIconHeight());
    return new Dimension(
      (int)(sizeInInches.getWidth()*dpiTarget.x), 
      (int)(sizeInInches.getHeight()*dpiTarget.y)
    );
  }
  
  /**
   * @see javax.swing.ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
   */
  public ImageIcon paintIcon(Graphics g, int x, int y) {
    super.paintIcon(null, g, x, y);
    return this;
  }
  
  
  
  /**
   * Reads image data from input stream
   */
  private static byte[] read(String name, InputStream in) {
    // check null (e.g. if resource wasn't found)
    if (in==null) 
      throw new IllegalArgumentException("no stream for "+name);
    // try to read it
    try {
      return new ByteArray(in).getBytes();
    } catch (IOException ex) {
      throw new IllegalArgumentException("can't read "+name+": "+ex.getMessage());
    } catch (InterruptedException e) {
      throw new IllegalStateException("interrupted while reading "+name);
    }
  }

  /**
   * Return a disabled/gray version
   */
  public ImageIcon getDisabled(int percentage) {

    GrayFilter filter = new GrayFilter(true, percentage);
    ImageProducer prod = new FilteredImageSource(getImage().getSource(), filter);
    Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
    
    ImageIcon result = new ImageIcon(grayImage);
    result.dpi = dpi;
    result.setDescription(getDescription());
    return result;
  }
  
  /**
   * Return a version with the given ImageIcon overlayed
   * @param overlay the image to overlay this with (javax.swing.* is enough)
   */ 
  public ImageIcon getOverLayed(ImageIcon overlay) {

    // already known?
    ImageIcon result = (ImageIcon)overlays.get(overlay);
    if (result!=null) {
      return result;
    }

    // create overlay
    int height = Math.max(getIconHeight(), overlay.getIconHeight());
    int width = Math.max(getIconWidth(), overlay.getIconWidth());

    Image image1 = getImage();
    Image image2 = overlay.getImage();
    BufferedImage composite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    Graphics g = composite.createGraphics();
    g.setClip(0,0,width,height);
    g.drawImage(image1, 0, 0, null);
    g.drawImage(image2, 0, 0, null);
    g.dispose();

    result = new ImageIcon(composite);
    result.dpi = dpi;
    result.setDescription(getDescription());

    // remember
    overlays.put(overlay, result);
        
    // done
    return result;
  }
  
} //ImageIcon 
