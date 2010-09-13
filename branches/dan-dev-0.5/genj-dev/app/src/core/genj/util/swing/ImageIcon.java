/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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

import genj.renderer.DPI;
import genj.util.ByteArray;
import genj.util.Dimension2d;
import genj.util.ImageSniffer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Icon;

/**
 * Improved ImageIcon <il> <li>can be read conveniently as resource for object
 * or class <li>knows about image resolution </il>
 */
public class ImageIcon extends javax.swing.ImageIcon {

  private final static ImageFilter GRAYSCALE_FILTER = new BufferedImageFilter(new GrayscaleFilter());

  /** dpi */
  private DPI dpi = null;

  /** cached overlayed icons */
  private Map<Object, ImageIcon> overlays = new WeakHashMap<Object, ImageIcon>();

  /**
   * Private special
   */
  public ImageIcon(Image copy) {
    super(copy);
  }
  
  /**
   * from icon
   */
  public ImageIcon(Icon icon) {
    if (icon instanceof javax.swing.ImageIcon)
      setImage( ((javax.swing.ImageIcon)icon).getImage() );
    else {
      BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      icon.paintIcon(null, image.getGraphics(), 0, 0);
      setImage(image);
    }
  }

  /**
   * Overriden default
   */
  public ImageIcon(String naMe, byte[] data) {
    super(data);

    if (getImageLoadStatus() != MediaTracker.COMPLETE)
      throw new RuntimeException("load status incomplete");

    // 20040304 checking for valid width/height now - just discovered
    // that ImageIcon might otherwise accept no-image date without
    // a notice (and then ProxyFile not realizing it's NOT an image)
    if (getIconHeight() < 0 || getIconWidth() < 0)
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
    return resource.indexOf('.') < 0 ? resource + ".png" : resource;
  }

  /**
   * Alternative Constructor
   */
  public ImageIcon(Class<?> from, String resource) {
    this(from.getName() + '#' + resource, from.getResourceAsStream(patchPNG(resource)), true);
  }

  /**
   * Alternative Constructor
   */
  public ImageIcon(String name, InputStream in) {
    this(name, in, false);
  }
  
  private ImageIcon(String name, InputStream in, boolean close) {
    this(name, read(name, in, close));
  }

  /**
   * Returns resolution (dpi)
   * 
   * @return resolution in dpi or null if not known
   */
  public DPI getResolution() {
    return dpi;
  }

  /**
   * Size in inches
   * 
   * @return size in inches or null if not known
   */
  public Dimension2D getSizeInInches() {
    // check whether we have a resolution
    if (dpi == null)
      return null;
    return new Dimension2d((double) getIconWidth() / dpi.horizontal(), (double) getIconHeight() / dpi.vertical());
  }

  /**
   * Size in points of give target space resolution
   */
  public Dimension getSizeInPoints(DPI dpiTarget) {
    Dimension2D sizeInInches = getSizeInInches();
    if (sizeInInches == null)
      return new Dimension(getIconWidth(), getIconHeight());
    return new Dimension((int) (sizeInInches.getWidth() * dpiTarget.horizontal()), (int) (sizeInInches.getHeight() * dpiTarget.vertical()));
  }

  /**
   * @see javax.swing.ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics,
   *      int, int)
   */
  public ImageIcon paintIcon(Graphics g, int x, int y) {
    super.paintIcon(null, g, x, y);
    return this;
  }

  /**
   * Reads image data from input stream
   */
  private static byte[] read(String name, InputStream in, boolean close) {
    // check null (e.g. if resource wasn't found)
    if (in == null)
      throw new IllegalArgumentException("no stream for " + name);
    // try to read it
    try {
      return new ByteArray(in).getBytes();
    } catch (IOException ex) {
      throw new IllegalArgumentException("can't read " + name + ": " + ex.getMessage());
    } catch (InterruptedException e) {
      throw new IllegalStateException("interrupted while reading " + name);
    } finally {
      if (close) try { in.close(); } catch (IOException e) {}
    }
  }

  /**
   * Return a disabled/gray version
   */
  public ImageIcon getGrayedOut() {

    // already known?
    ImageIcon result = overlays.get("grayedout");
    if (result != null) 
      return result;
    
    ImageProducer prod = new FilteredImageSource(getImage().getSource(), GRAYSCALE_FILTER);
    Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
    result = new ImageIcon(grayImage);
    result.dpi = dpi;
    result.setDescription(getDescription());
    
    overlays.put("grayedout", result);
    
    return result;
  }
  
  public ImageIcon getTransparent(final int threshold) {
    
    // already known?
    ImageIcon result = overlays.get(Integer.toString(threshold));
    if (result != null) 
      return result;
 
    if (threshold<0||threshold>255)
      throw new IllegalArgumentException("!(0<=threshold<=255)");
    
    ImageProducer prod = new FilteredImageSource(getImage().getSource(), new RGBImageFilter() {
      @Override
      public int filterRGB(int x, int y, int rgb) {
        int alpha = (rgb >> 24) & 0xff;
        alpha = alpha * threshold / 255;
        return (rgb&0x00FFFFFF) | (alpha<<24);
      }
    });
    result = new ImageIcon(Toolkit.getDefaultToolkit().createImage(prod));
    result.dpi = dpi;
    result.setDescription(getDescription());
    overlays.put(Integer.toString(threshold), result);
    
    return result;
    
  }

  /**
   * Return a version with the given ImageIcon overlayed
   * 
   * @param overlay
   *          the image to overlay this with (javax.swing.* is enough)
   */
  public ImageIcon getOverLayed(ImageIcon overlay) {

    // already known?
    ImageIcon result = overlays.get(overlay);
    if (result != null) 
      return result;

    // create overlay
    int height = Math.max(getIconHeight(), overlay.getIconHeight());
    int width = Math.max(getIconWidth(), overlay.getIconWidth());

    Image image1 = getImage();
    Image image2 = overlay.getImage();
    BufferedImage composite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    Graphics g = composite.createGraphics();
    g.setClip(0, 0, width, height);
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

  /**
   * A grayscale filter from http://www.jhlabs.com/ip/index.html
   * 
   *   Copyright 2006 Jerry Huxtable
   *   
   *   Licensed under the Apache License, Version 2.0 (the "License");
   *   you may not use this file except in compliance with the License.
   *   You may obtain a copy of the License at
   * 
   *      http://www.apache.org/licenses/LICENSE-2.0
   * 
   *   Unless required by applicable law or agreed to in writing, software
   *   distributed under the License is distributed on an "AS IS" BASIS,
   *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   *   See the License for the specific language governing permissions and
   *   limitations under the License.
   */
  private static class GrayscaleFilter implements BufferedImageOp {

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
      if (dstCM == null)
        dstCM = src.getColorModel();
      return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
      return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
      if (dstPt == null)
        dstPt = new Point2D.Double();
      dstPt.setLocation(srcPt.getX(), srcPt.getY());
      return dstPt;
    }

    public RenderingHints getRenderingHints() {
      return null;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
      int width = src.getWidth();
      int height = src.getHeight();
      int type = src.getType();
      WritableRaster srcRaster = src.getRaster();

      if (dst == null)
        dst = createCompatibleDestImage(src, null);
      WritableRaster dstRaster = dst.getRaster();

      int[] inPixels = new int[width];
      for (int y = 0; y < height; y++) {
        // We try to avoid calling getRGB on images as it causes them to become
        // unmanaged, causing horrible performance problems.
        if (type == BufferedImage.TYPE_INT_ARGB) {
          srcRaster.getDataElements(0, y, width, 1, inPixels);
          for (int x = 0; x < width; x++)
            inPixels[x] = filterRGB(x, y, inPixels[x]);
          dstRaster.setDataElements(0, y, width, 1, inPixels);
        } else {
          src.getRGB(0, y, width, 1, inPixels, 0, width);
          for (int x = 0; x < width; x++)
            inPixels[x] = filterRGB(x, y, inPixels[x]);
          dst.setRGB(0, y, width, 1, inPixels, 0, width);
        }
      }

      return dst;
    }

    private int filterRGB(int x, int y, int rgb) {
      int a = rgb & 0xff000000;
      int r = (rgb >> 16) & 0xff;
      int g = (rgb >> 8) & 0xff;
      int b = rgb & 0xff;
      rgb = (r * 77 + g * 151 + b * 28) >> 8; // NTSC luma
      return a | (rgb << 16) | (rgb << 8) | rgb;
    }

  }

} // ImageIcon
