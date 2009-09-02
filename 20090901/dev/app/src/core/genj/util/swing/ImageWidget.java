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

import genj.util.Origin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A widget that shows a dynamically loaded, weak reference'd image
 */
public class ImageWidget extends JPanel {
  
  private final static Logger LOG = Logger.getLogger("genj.util");
  
  /** a singleton static worker shared between all image widgets */
  private final static Worker WORKER = new Worker();
  
  /** a cache mapping sources to soft-references of images */
  private final static Map source2imgsoftref = new HashMap();
  
  /** content */
  private JScrollPane scroll = new JScrollPane(); 
  private Content content = new Content();
  
  /** zoom */
  private double zoom;
  
  /**
   * Constructor
   */
  public ImageWidget() {
    super(new BorderLayout());
    scroll = new JScrollPane(new ViewPortAdapter(content));
    add(scroll, BorderLayout.CENTER);
  }
  
  /**
   * Setter - source
   */
  public void setSource(Source source) {
    content.setSource(source);
  }
  
  /**
   * Getter - source
   */
  public Source getSource() {
    return content.source;
  }
  
  /**
   * Setter - zoom
   */
  public void setZoom(double zoom) {
    if (zoom<0)
      return;
    this.zoom = zoom;
    content.revalidate();
    content.setToolTipText(zoom==0 ? null : (int)(zoom*100D)+"%");

  }
  
  /**
   * Getter - zoom
   */
  public double getZoom() {
    return zoom;
  }
  
  /**
   * Lookup  to cached image
   */
  private static Image lookupCachedImage(Source source) {
    Image result = null;
    SoftReference ref = (SoftReference)source2imgsoftref.get(source);
    if (ref!=null) 
      result = (Image)ref.get();
    return result;
  }
  
  /**
   * Keep a cached image
   */
  private static void keepCachedImage(Source source, Image img) {
    source2imgsoftref.put(source, new SoftReference(img));
  }
  
   /**
   * Content
   */
  private class Content extends JComponent implements Runnable, IIOReadUpdateListener {
    
    /** source */
    private Source source = null;

    /** preferred size */
    private Dimension cachedDimension;
    
    /** our async slice of process time */
    public void run() {
      
      // maybe we're already good?
      if (lookupCachedImage(source)!=null)
        return;
      
      // load it
      InputStream in = null;
      ImageReader reader = null;
      try {
        
        in = source.open();
        
        ImageInputStream iin = ImageIO.createImageInputStream(in);
          
        Iterator iter = ImageIO.getImageReaders(iin);
        if (!iter.hasNext()) 
          throw new IOException("no suitable image reader for "+source);
    
        reader = (ImageReader)iter.next();
        reader.setInput(iin, false, false);
        reader.addIIOReadUpdateListener(this);
          
        reader.read(0, reader.getDefaultReadParam());
        
      } catch (Throwable t) {
        LOG.fine("Loading "+source+" failed with "+t.getMessage());
        
        // blank image
        keepCachedImage(source, null);
        
      } finally {
        try { in.close(); } catch (Throwable t) {} 
        try { reader.dispose(); } catch (Throwable t) {} 
      }
        
        // done
    }
    
    /**
     * setter for source
     */
    private void setSource(Source source) {
      this.source = source;
      cachedDimension = null;
      revalidate();
    }
    
    /**
     * cached image
     */
    private Image getCachedImage() {
      return lookupCachedImage(source);
    }
    
    /**
     * cached image size
     */
    private Dimension getCachedImageSize() {
      
      // cached ?
      if (cachedDimension==null) {
      
        // check image
        int w = 0, h = 0;
        Image img = getCachedImage();
        if (img==null) {
          WORKER.add(this);
          return new Dimension(0,0);
        }

        // calculate now and remember
        cachedDimension = new Dimension(img.getWidth(null), h = img.getHeight(null));
        
      }
      
      // done - return private copy
      return new Dimension(cachedDimension);
    }

    /**
     * paint callback
     */
    protected void paintComponent(Graphics g) {
      
      // anything in here?
      if (source==null)
        return;
      
      // try'n image
      Image img = getCachedImage();
      if (img==null) {        
        WORKER.add(this);
        return;
      }

      // show it
      Graphics2D g2d = (Graphics2D)g;
      
      // TODO ImageWidget - do the dpi math
      // zoom==0 means "to fit"
      double scale;
      if (zoom==0) {
        Dimension avail = getSize();
        scale = avail.width/(double)getCachedImageSize().width;
      } else {
        scale = zoom;
      }
      g2d.scale(scale, scale);
      
      // here ya go
      g2d.drawImage(img, 0, 0, null);
    }

    /**
     * layout callback
     */
    public Dimension getPreferredSize() {
      
      // anything in here?
      if (source==null)
        return new Dimension(0,0);
      Dimension dim = getCachedImageSize();
      double scale;
      
      // zoom of 0 means "to fit"
      // TODO ImageWidget - do the dpi math
      if (zoom==0)  {
        Dimension avail = scroll.getSize();
        double zx = avail.width/(double)dim.width;
        double zy = avail.height/(double)dim.height;
        scale = Math.min(1, Math.min(zx,zy));
      } else {
        scale = zoom;
      }
      
      // scale now
      dim.width *= scale;
      dim.height *= scale;
      return dim;
      
////  // check physical size
////  Dimension dim = img.getSizeInPoints(viewManager.getDPI());
////  float factor = (float)zoom/100;
////  dim.width *= factor;
////  dim.height *= factor;
////  return dim;
    }

    /** IIOReadUpdateListener callback */
    public void passComplete(ImageReader reader, BufferedImage img) {
    }

    /** IIOReadUpdateListener callback */
    public void thumbnailPassComplete(ImageReader reader, BufferedImage thumb) {
    }

    /** IIOReadUpdateListener callback */
    public void passStarted(ImageReader reader, BufferedImage img, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
    }

    /** IIOReadUpdateListener callback */
    public void thumbnailPassStarted(ImageReader reader, BufferedImage thumb, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
    }

    /** IIOReadUpdateListener callback */
    public void imageUpdate(ImageReader reader, BufferedImage img, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
      
      // keep image around
      keepCachedImage(source, img);
      
      // check for image w/h
      int w = img.getWidth();
      int h = img.getHeight();
      if (cachedDimension==null||cachedDimension.width!=w||cachedDimension.height!=h) {
        cachedDimension = new Dimension(w,h);
        revalidate();
        return;
      }
      
      // check if visible?
      if (getWidth()==0||getHeight()==0||!isVisible())
        return;
      
      // calculate true width/height of updated area
      width = width*periodX;
      height = height*periodY;
      
      // show progress
      Graphics2D g2d = (Graphics2D)getGraphics();
      
      // zoom (==0 means "to fit")
      // TODO ImageWidget - do the dpi math
      // zoom==0 means "to fit"
      double scale;
      if (zoom==0) {
        Dimension avail = getSize();
        scale = avail.width/(double)getCachedImageSize().width;
      } else {
        scale = zoom;
      }
      g2d.scale(scale, scale);
      
      try {
        g2d.drawImage(img, 
            minX, minY,
            minX+width, minY+height,
            minX, minY,
            minX+width, minY+height,
            null);
      } catch (Throwable t) {
        // This can fail intermittently (case: PNG/interlaced) - don't want to kill the load process though
      }
      
      
      // done
    }

    /** IIOReadUpdateListener callback */
    public void thumbnailUpdate(ImageReader source, BufferedImage thumb, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
    }
  } //Content
  
  /**
   * The source of an image to show
   */
  public abstract static class Source {
    
    /** name */
    protected String name;
    
    /** constructor */
    private Source() {
    }
    
    /** constructor */
    public Source(String name) {
      this.name = name;
    }
    
    /**
     * open implementation
     */
    protected abstract InputStream open() throws IOException;
    
    /** string rep */
    public String toString() {
      return name!=null ? name : super.toString();
    }
    
  } //Source
  
  /**
   * A source relative to an origin
   */
  public static class RelativeSource extends Source {
    
    private Origin origin;
    
    /** constructor */
    public RelativeSource(Origin origin, String name) {
      super(name);
      this.origin = origin;
    }
    
    /** impl */
    protected InputStream open() throws IOException {
      return origin.open(name);
    }
    
    /** comparison */
    public boolean equals(Object other) {
      if (!(other instanceof RelativeSource))
        return false;
      RelativeSource that = (RelativeSource)other;
      return this.origin.equals(that.origin) && this.name.equals(that.name);
    }
    
    /** hash */
    public int hashCode() {
      return origin.hashCode() + name.hashCode();
    }
    
  } //RelativeSource
  
  /**
   * A file source
   */
  public static class FileSource extends Source {
    
    private File file;
    
    /** constructor */
    public FileSource(File file) {
      super(file.getAbsolutePath());
      this.file = file;
    }
    
    /** impl */
    protected InputStream open() throws IOException {
      return new FileInputStream(file);
    }
    
    /** comparison */
    public boolean equals(Object other) {
      if (!(other instanceof FileSource))
        return false;
      FileSource that = (FileSource)other;
      return this.file.equals(that.file);
      
    }
    
    /** hash */
    public int hashCode() {
      return file.hashCode();
    }
    
  } //FileSource
  
  /**
   * A byte array source
   */
  public static class ByteArraySource extends Source {
    
    private byte[] data;
    
    /** constructor */
    public ByteArraySource(byte[] data) {
      super(data.length+" bytes");
      this.data = data;
    }
    
    protected InputStream open() throws IOException {
      return new ByteArrayInputStream(data);
    }
    
    public int hashCode() {
      return data.hashCode();
    }
    
    public boolean equals(Object obj) {
      return data==obj;
    }
    
  } //ByteArraySource
  
  /**
   * The singleton worker for all
   */
  private static class Worker implements Runnable {
    
    /** lifo stack of needy runnables */
    private Stack stack = new Stack();
    
    /** the currently served runnable */
    private Runnable current = null;
    
    /** constructor */
    Worker() {
      Thread t = new Thread(this);
      // 20070225 use normal priority so we don't inherit a UI thread priority
      t.setPriority(Thread.NORM_PRIORITY);
      t.setDaemon(true);
      t.start();
    }
    
    /** add a needy widget */
    synchronized void add(Runnable r) {
      if (current!=r && !stack.contains(r))
        stack.push(r);
      notify();
    }
    
    /** async load */
    public void run() {
      // we do this forever
      while (true) try {
        // pleasure to serve
        next();
        current.run();
        current = null; 
        // load
      } catch (Throwable t) {}
      // never
    }
    
    /** grab next needy */
    private synchronized void next() throws InterruptedException {
      while (true) {
        // something to do?
        if (!stack.isEmpty()) {
          current = (Runnable)stack.pop();
          return;
        }
        wait();
        // try again
      }
    }
  
  } //Loader
  
} //ImageWidget
