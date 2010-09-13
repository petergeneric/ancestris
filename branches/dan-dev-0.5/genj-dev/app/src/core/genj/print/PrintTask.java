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
package genj.print;

import genj.option.Option;
import genj.option.PropertyOption;
import genj.renderer.DPI;
import genj.renderer.EmptyHintKey;
import genj.util.Dimension2d;
import genj.util.EnvironmentChecker;
import genj.util.Resources;
import genj.util.Trackable;
import genj.util.WordBuffer;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

/**
 * Our own task for printing
 */
/*package*/ class PrintTask implements Printable, Trackable {

  /** our flavor */
  /*package*/ final static DocFlavor FLAVOR = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
  /*package*/ final static Resources RESOURCES = Resources.get(PrintTask.class);
  /*package*/ final static Logger LOG = Logger.getLogger("genj.print");
  
  /** our print service */
  private PrintService service;

  /** the current renderer */
  private PrintRenderer renderer;

  /** current page */
  private int page = 0;

  /** any problem that might occur async */
  private Throwable throwable;

  /** the title */
  private String title;
  
  /** registry */
  private PrintRegistry registry;

  /** progress key */
  private String progress;
  
  /** settings */
  private PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
  
  /** pages */
  private Dimension pages;
  private double zoomx = 1.0D, zoomy = 1.0D;
  private boolean printEmpties = false;

  /**
   * Constructor
   */
  public PrintTask(String title, PrintRenderer renderer) throws PrintException {
  
    // remember 
    this.renderer = renderer;
    this.title = title;
    this.registry = PrintRegistry.get(renderer);
    
    // restore last service
    PrintService service = registry.get(getDefaultService());
    if (!service.isDocFlavorSupported(FLAVOR))
      service = getDefaultService();
    setService(service);

    // setup a default job name
    attributes.add(new JobName("GenJ", null));
    
    // restore print attributes
    registry.get(attributes);
    
    // file output preset?
    String file = EnvironmentChecker.getProperty("genj.print.file", null, "Print file output");
    if (file!=null)
      attributes.add(new Destination(new File(file).toURI()));

    // done
  }
  
  /*package*/ String getTitle() {
    return title;
  }

  /**
   * default print service
   */
  protected PrintService getDefaultService() throws PrintException {
    // check system default
    PrintService service = PrintServiceLookup.lookupDefaultPrintService();
    if (service!=null) {
      // check suitability
      if (service.isDocFlavorSupported(FLAVOR))
        return service;
      LOG.info("Default print service not supported");
    } else {
      LOG.info("No default print service available (are you running CUPS?)");
    }
    
    // try to find a better one
    PrintService[] suitables = PrintServiceLookup.lookupPrintServices(FLAVOR, null);
    if (suitables.length==0)
      throw new PrintException("Couldn't find any suitable printer");
    
    return suitables[0];
  }
  
  /**
   * suitable services
   */
  protected PrintService[] getServices() {
    return PrintServiceLookup.lookupPrintServices(FLAVOR, null);    
  }

  /**
   * Attributes access
   */
  /*package*/ PrintRequestAttributeSet getAttributes() {
    return attributes;
  }

  /**
   * Set current service
   */
  /*package*/ void setService(PrintService set) {
    // keep
    service = set;
    // remember
    registry.put(service);
  }

  /**
   * Get current service
   */
  /*package*/ PrintService getService() {
    return service;
  }

  /**
   * Resolve resolution (in inches)
   */
  /*package*/ DPI getResolution() {
    // In java printing the resolution is always 72dpi
    return new DPI (72,72);
//    PrinterResolution resolution = (PrinterResolution)getAttribute(PrinterResolution.class);
//    return new Point(
//      resolution.getCrossFeedResolution(PrinterResolution.DPI),
//      resolution.getFeedResolution(PrinterResolution.DPI)
//    );
  }
  
  /**
   * Resolve printable area (in inches)
   */
  /*package*/ Rectangle2D getPrintable() {
    
    OrientationRequested orientation = (OrientationRequested)getAttribute(OrientationRequested.class);
    MediaPrintableArea printable = (MediaPrintableArea)getAttribute(MediaPrintableArea.class);
    Rectangle2D result = new Rectangle2D.Float();
    
    if (orientation==OrientationRequested.LANDSCAPE||orientation==OrientationRequested.REVERSE_LANDSCAPE) {
      // Landscape
      Dimension2D size = getPageSize();
      result.setRect(
          size.getWidth()-printable.getHeight(MediaSize.INCH)-printable.getY(MediaSize.INCH), 
          printable.getX(MediaSize.INCH),
          printable.getHeight(MediaSize.INCH),
          printable.getWidth(MediaSize.INCH)
        );
    } else {
      // Portrait
      result.setRect(
        printable.getX(MediaSize.INCH),
        printable.getY(MediaSize.INCH), 
        printable.getWidth(MediaSize.INCH),
        printable.getHeight(MediaSize.INCH)
      );
    }    
    return result;
  }

  /**
   * Calculate printable in inches
   */
  /*package*/ Rectangle2D getPrintable(Rectangle2D page) {
    Rectangle2D printable = getPrintable();
    return new Rectangle2D.Double(
      page.getMinX() + printable.getX(), 
      page.getMinY() + printable.getY(), 
      printable.getWidth(), 
      printable.getHeight()
    );
  }
  
  /**
   * Resolve page size (in inches)
   */
  /*package*/ Dimension2D getPageSize() {
    
    OrientationRequested orientation = (OrientationRequested)getAttribute(OrientationRequested.class);
    Media media = (Media)getAttribute(Media.class);
    
    // try to find out media size for MediaSizeName
    MediaSize size = null;
    if (media instanceof MediaSizeName) 
      size = MediaSize.getMediaSizeForName((MediaSizeName)media);

    // hmm, might be sun.print.CustomMediaSizeName - try fallback: public MediaSizeName getStandardMedia()
    if (size==null) {
      try {
        size = MediaSize.getMediaSizeForName((MediaSizeName)media.getClass().getMethod("getStandardMedia").invoke(media));
        LOG.fine("Got MediaSize "+size+" from "+media+".getStandardMedia()");
      } catch (Throwable t) {
        // ignored
      }
    }

    // fallback to A4
    if (size==null) {
      LOG.warning("Need MediaSize, got unknown MediaSizeName, MediaTray or MediaName '"+media+"' - using A4");
      attributes.add(MediaSizeName.ISO_A4);
      size = MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4);
    }
    
    Dimension2D result = new Dimension2d();

    double w,h;
    if (orientation==OrientationRequested.LANDSCAPE||orientation==OrientationRequested.REVERSE_LANDSCAPE) {
      result.setSize(size.getY(MediaSize.INCH), size.getX(MediaSize.INCH));
    } else {
      result.setSize(size.getX(MediaSize.INCH), size.getY(MediaSize.INCH));
    }    
    return result;
  }
  
  /**
   * pages 
   */
  /*package*/ void setPages(Dimension pages, boolean fit) {
    
    if (pages.width==0 || pages.height==0)
      throw new IllegalArgumentException("0 not allowed");
    
    this.pages = pages;

    Rectangle2D printable = getPrintable();
    Dimension2D size = getSize();
    
    this.zoomx = pages.width*printable.getWidth()   / size.getWidth();
    this.zoomy = pages.height*printable.getHeight() / size.getHeight();
    
    if (!fit) {
      if (zoomx>zoomy) zoomx=zoomy;
      if (zoomy>zoomx) zoomy=zoomx;
    }
  }
  
  /*package*/ boolean isPrintEmpties() {
    return printEmpties;
  }
  
  
  /*package*/ void setPrintEmpties(boolean set) {
    this.printEmpties  = set;
  }
  
  /*package*/ void setZoom(double zoom) {
    this.zoomx = zoom;
    this.zoomy = zoom;
    this.pages = null;
  }
  
  /*package*/ Dimension2D getSize() {
    return renderer.getSize();
  }
  
  /*package*/ Dimension getPages() {
    if (pages!=null) 
      return pages;
    // ask renderer
    Rectangle2D printable = getPrintable();
    Dimension2D dim = renderer.getSize();
    return new Dimension(
      (int)Math.ceil(dim.getWidth ()*zoomx/printable.getWidth ()),
      (int)Math.ceil(dim.getHeight()*zoomy/printable.getHeight())
    );
  }
  
  /**
   * transform print attributes to string
   */
  private String toString(PrintRequestAttributeSet atts) {
    WordBuffer buf = new WordBuffer(",");
    Attribute[] array = attributes.toArray();
    for (int i = 0; i < array.length; i++) 
      buf.append(array[i].getClass().getName()+"="+array[i].toString());
    return buf.toString();
  }
  
  /**
   * Resolve a print attribute
   */
  private PrintRequestAttribute getAttribute(Class<? extends PrintRequestAttribute> category) {
    // check our attributes first
    Object result = attributes.get(category);
    if (result instanceof PrintRequestAttribute)
      return (PrintRequestAttribute)result;
    // make sure we know the media if this is not Media category
    if (!Media.class.isAssignableFrom(category))
      getAttribute(Media.class);
    // now grab configured default for category
    result = service.getDefaultAttributeValue(category);
    // fallback to first supported
    if (result==null) {
      LOG.finer( "Couldn't find default PrintRequestAttribute for category "+category);
      // note - at this point we have made sure that Media is defined in attributes so
      // MediaPrintableArea for example should result in something non-null
      result = service.getSupportedAttributeValues(category, null, attributes);
      if (result==null)
        LOG.warning( "Couldn't find supported PrintRequestAttribute for category "+category+" with "+toString(attributes));
      else if (result.getClass().isArray()) {
	      LOG.fine( "Got PrintRequestAttribute values "+Arrays.toString((Object[])result)+" for category "+category);
	      
	      // apparently some systems can return null values, e.g. 
	      //    [null, (0.0,0.0)->(279.4,355.6)mm, (0.0,0.0)->(279.4,431.8)mm, (0.0,0.0)->(330.2,482.6)mm, (0.0,0.0)->(406.4,508.0)mm, (0.0,0.0)->(406.4,609.6)mm, (0.0,0.0)->(1188.861,1682.044)mm, (0.0,0.0)->(1682.044,2380.897)mm, (0.0,0.0)->(203.2,254.0)mm, (0.0,0.0)->(203.2,304.8)mm, (0.0,0.0)->(841.022,1188.861)mm, (0.0,0.0)->(594.078,841.022)mm, (0.0,0.0)->(420.158,594.078)mm, (0.0,0.0)->(297.039,420.158)mm, (0.0,0.0)->(209.903,297.039)mm, (0.0,0.0)->(148.519,209.903)mm, (0.0,0.0)->(215.9,279.4)mm, (0.0,0.0)->(279.4,431.8)mm, (0.0,0.0)->(431.8,558.8)mm, (0.0,0.0)->(558.8,863.6)mm, (0.0,0.0)->(863.6,1117.6)mm, (0.0,0.0)->(228.6,304.8)mm, (0.0,0.0)->(304.8,457.2)mm, (0.0,0.0)->(457.2,609.6)mm, (0.0,0.0)->(609.6,914.4)mm, (0.0,0.0)->(914.4,1219.2)mm, (0.0,0.0)->(916.869,1296.811)mm, (0.0,0.0)->(647.7,916.869)mm, (0.0,0.0)->(457.906,647.7)mm, (0.0,0.0)->(323.85,457.906)mm, (0.0,0.0)->(228.953,323.85)mm, (0.0,0.0)->(161.925,228.953)mm, (0.0,0.0)->(104.775,241.3)mm, (0.0,0.0)->(161.925,228.953)mm, (0.0,0.0)->(110.067,220.133)mm, (0.0,0.0)->(98.425,190.5)mm, (0.0,0.0)->(184.15,266.7)mm, (0.0,0.0)->(999.772,1413.933)mm, (0.0,0.0)->(706.967,999.772)mm, (0.0,0.0)->(499.886,706.967)mm, (0.0,0.0)->(352.778,499.886)mm, (0.0,0.0)->(249.767,352.778)mm, (0.0,0.0)->(175.683,249.767)mm, (0.0,0.0)->(1029.758,1455.914)mm, (0.0,0.0)->(727.781,1029.758)mm, (0.0,0.0)->(514.703,727.781)mm, (0.0,0.0)->(363.008,514.703)mm, (0.0,0.0)->(256.469,363.008)mm, (0.0,0.0)->(182.739,256.469)mm, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null] 
	      // as seen in Francois' log files

	      Object[] os = (Object[])result;
	      result = null;
	      for (int i=0;result==null && i<os.length;i++) {
	        if (os[i]!=null && category.isAssignableFrom(os[i].getClass())) {
	          result = os[i];
	          break;
	        }
	      }
      }
    }
    // revert to media default if not available
    if (result==null&&category==Media.class) {
      result = MediaSizeName.ISO_A4;
      LOG.warning("fallback media is "+result);
      attributes.add((Media)result);
    }    
    // try to find yet another fallback for media printable area
    // according to this bug in Java pre7
    //   http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6508532
    // there can be null or [null] returned (as seen in Daniel's setup)
    if (result==null&&category==MediaPrintableArea.class) {
      Dimension2D page = getPageSize();
      result = new MediaPrintableArea(1,1,(float)page.getWidth()-2,(float)page.getHeight()-2, MediaPrintableArea.INCH);
      LOG.warning( "Using fallback MediaPrintableArea "+result);
    }
    // remember
    if (result!=null) {
      attributes.add((PrintRequestAttribute)result);
      LOG.fine( "PrintRequestAttribute for category "+category+" is "+result+" with "+toString(attributes));
    } else {
      LOG.warning( "Couldn't find any PrintRequestAttribute for category "+category+" with "+toString(attributes));
    }
    // done
    return (PrintRequestAttribute)result;
  }

  /*package*/ List<? extends Option> getOptions() {
    return PropertyOption.introspect(renderer);
  }

  /**
   * @see genj.util.Trackable#cancelTrackable()
   */
  public void cancelTrackable() {
  }

  /**
   * @see genj.util.Trackable#getProgress()
   */
  public int getProgress() {
    return (int) (page / (float) (getPages().width * getPages().height) * 100);
  }

  /**
   * @see genj.util.Trackable#getState()
   */
  public String getState() {
    return RESOURCES.getString("progress", (page + 1), (getPages().width * getPages().height) );
  }
  
  /*package*/ void print() {
    
    // store current settings
    registry.put(attributes);

    // init print
    try {
      service.createPrintJob().print(new SimpleDoc(this, FLAVOR, null), attributes);
    } catch (PrintException e) {
      LOG.log(Level.WARNING, "print failed", e);
    }
    
    // debug target?
    String file = EnvironmentChecker.getProperty("genj.print.file", null, "Print file output");
    if (file!=null)
      try {
        Desktop.getDesktop().open(new File(file));
      } catch (Throwable t) {
        LOG.log(Level.FINE, "can't open "+file, t);
      }
    
  }
  
  /**
   * callback - printable
   */
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    // what's the current page
    Dimension pages = getPages();
    int
      row = pageIndex/pages.width,
      col = pageIndex%pages.width;
    if (col>=pages.width||row>=pages.height) 
      return NO_SUCH_PAGE;

    page = pageIndex;
    
    // bring forward resolution
    Graphics2D g2d = (Graphics2D)graphics;
    g2d.setRenderingHint(DPI.KEY, getResolution());
    
    // draw content
    print((Graphics2D)graphics, row, col);
    
    // next
    return PAGE_EXISTS;
  }
  
  
  /*package*/ void print(Graphics2D graphics, int row, int col) {

    // prepare current page/clip
    DPI dpi = DPI.get(graphics);
    
    Rectangle2D pixels = dpi.toPixel(getPrintable());
    
    graphics.translate(pixels.getX()-col*pixels.getWidth(), pixels.getY()-row*pixels.getHeight()); 
    
    Rectangle2D box = new Rectangle2D.Double(
        col*pixels.getWidth(),
        row*pixels.getHeight(),
        pixels.getWidth(),
        pixels.getHeight());
    graphics.clip(box);

    graphics.scale(zoomx, zoomy);

    graphics.setRenderingHint(EmptyHintKey.KEY, true);
    
    renderer.render(graphics);

    // done
  }

} //PrintTask