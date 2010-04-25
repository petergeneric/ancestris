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
package genj.print;

import genj.util.Dimension2d;
import genj.util.EnvironmentChecker;
import genj.util.Resources;
import genj.util.Trackable;
import genj.util.WordBuffer;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.util.swing.ProgressWidget;
import genj.util.swing.UnitGraphics;
import genj.window.WindowManager;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.Arrays;
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
import javax.swing.Action;
import javax.swing.JComponent;

/**
 * Our own task for printing
 */
public class PrintTask extends Action2 implements Printable, Trackable {

  /** our flavor */
  /*package*/ final static DocFlavor FLAVOR = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
  
  /*package*/ final static Resources RESOURCES = Resources.get(PrintTask.class);
  /*package*/ final static Logger LOG = Logger.getLogger("genj.print");
  
  /** the owning component */
  private JComponent owner;

  /** our print service */
  private PrintService service;

  /** the current renderer */
  private Printer renderer;

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
  private Dimension cachedPages;

  /**
   * Constructor
   */
  public PrintTask(Printer setRenderer, String setTitle, JComponent setOwner, PrintRegistry setRegistry) throws PrintException {
    
    // looks
    setImage(new ImageIcon(this,"images/Print"));

    // remember 
    renderer = setRenderer;
    owner = setOwner;
    title = RESOURCES.getString("title", setTitle);
    registry = setRegistry;

    // setup async
    setAsync(Action2.ASYNC_SAME_INSTANCE);
    
    // restore last service
    PrintService service = registry.get(getDefaultService());
    if (!service.isDocFlavorSupported(FLAVOR))
      service = getDefaultService();
    setService(service);

    // setup a default job name
    attributes.add(new JobName(title, null));
    
    // restore print attributes
    registry.get(attributes);

    // done
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
   * Owner access
   */
  /*package*/ JComponent getOwner() {
    return owner;
  }

  /**
   * Invalidate current state (in case parameters/options/service has changed)
   */
  /*package*/ void invalidate() {
    // forget cached information
    cachedPages = null;
  }
  
  /**
   * Set current service
   */
  /*package*/ void setService(PrintService set) {
    // known?
    if (service==set)
      return;
    // keep
    service = set;
    // remember
    registry.put(service);
    // reset state
    invalidate();
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
  /*package*/ Point getResolution() {
    // In java printing the resolution is always 72dpi
    return new Point(72,72);
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
   * Calculate page in inches
   */
  /*package*/ Rectangle2D getPage(int x, int y, float pad) {
    
    Dimension2D size = getPageSize();

    return new Rectangle2D.Double(
       pad + x*(size.getWidth ()+pad), 
       pad + y*(size.getHeight()+pad), 
       size.getWidth(), 
       size.getHeight()
    );
  }
  
  /**
   * Resolve page size (in inches)
   */
  /*package*/ Dimension2D getPageSize() {
    
    OrientationRequested orientation = (OrientationRequested)getAttribute(OrientationRequested.class);
    MediaSize media = MediaSize.getMediaSizeForName((MediaSizeName)getAttribute(Media.class));
    
    Dimension2D result = new Dimension2d();
    
    if (orientation==OrientationRequested.LANDSCAPE||orientation==OrientationRequested.REVERSE_LANDSCAPE)
      result.setSize(media.getY(MediaSize.INCH), media.getX(MediaSize.INCH));
    else
      result.setSize(media.getX(MediaSize.INCH), media.getY(MediaSize.INCH));
    
    return result;
  }
  
  /**
   * Compute pages
   */
  /*package*/ Dimension getPages() {
    if (cachedPages==null)
      cachedPages = renderer.calcSize(new Dimension2d(getPrintable()), getResolution());
    return cachedPages;
  }
  
  /**
   * Renderer
   */
  /*package*/ Printer getRenderer() {
    return renderer;
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
  private PrintRequestAttribute getAttribute(Class category) {
    // check
    if (!PrintRequestAttribute.class.isAssignableFrom(category))
      throw new IllegalArgumentException("only PrintRequestAttributes allowed");
    // check our attributes first
    Object result = (PrintRequestAttribute)attributes.get(category);
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
      else if (result.getClass().isArray()&&result.getClass().getComponentType()==category) {
	      LOG.finer( "Got PrintRequestAttribute values "+Arrays.toString((Object[])result)+" for category "+category);
	      
	      // apparently some systems can return null values, e.g. 
	      //    [null, (0.0,0.0)->(279.4,355.6)mm, (0.0,0.0)->(279.4,431.8)mm, (0.0,0.0)->(330.2,482.6)mm, (0.0,0.0)->(406.4,508.0)mm, (0.0,0.0)->(406.4,609.6)mm, (0.0,0.0)->(1188.861,1682.044)mm, (0.0,0.0)->(1682.044,2380.897)mm, (0.0,0.0)->(203.2,254.0)mm, (0.0,0.0)->(203.2,304.8)mm, (0.0,0.0)->(841.022,1188.861)mm, (0.0,0.0)->(594.078,841.022)mm, (0.0,0.0)->(420.158,594.078)mm, (0.0,0.0)->(297.039,420.158)mm, (0.0,0.0)->(209.903,297.039)mm, (0.0,0.0)->(148.519,209.903)mm, (0.0,0.0)->(215.9,279.4)mm, (0.0,0.0)->(279.4,431.8)mm, (0.0,0.0)->(431.8,558.8)mm, (0.0,0.0)->(558.8,863.6)mm, (0.0,0.0)->(863.6,1117.6)mm, (0.0,0.0)->(228.6,304.8)mm, (0.0,0.0)->(304.8,457.2)mm, (0.0,0.0)->(457.2,609.6)mm, (0.0,0.0)->(609.6,914.4)mm, (0.0,0.0)->(914.4,1219.2)mm, (0.0,0.0)->(916.869,1296.811)mm, (0.0,0.0)->(647.7,916.869)mm, (0.0,0.0)->(457.906,647.7)mm, (0.0,0.0)->(323.85,457.906)mm, (0.0,0.0)->(228.953,323.85)mm, (0.0,0.0)->(161.925,228.953)mm, (0.0,0.0)->(104.775,241.3)mm, (0.0,0.0)->(161.925,228.953)mm, (0.0,0.0)->(110.067,220.133)mm, (0.0,0.0)->(98.425,190.5)mm, (0.0,0.0)->(184.15,266.7)mm, (0.0,0.0)->(999.772,1413.933)mm, (0.0,0.0)->(706.967,999.772)mm, (0.0,0.0)->(499.886,706.967)mm, (0.0,0.0)->(352.778,499.886)mm, (0.0,0.0)->(249.767,352.778)mm, (0.0,0.0)->(175.683,249.767)mm, (0.0,0.0)->(1029.758,1455.914)mm, (0.0,0.0)->(727.781,1029.758)mm, (0.0,0.0)->(514.703,727.781)mm, (0.0,0.0)->(363.008,514.703)mm, (0.0,0.0)->(256.469,363.008)mm, (0.0,0.0)->(182.739,256.469)mm, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null] 
	      // as seen in Francois' log files

	      Object[] os = (Object[])result;
	      result = null;
	      for (int i=0;result==null && i<os.length;i++) 
	        result = os[i];
	    } else {
	      // according to http://java.sun.com/j2se/1.4.2/docs/guide/jps/spec/attributes.fm5.html the result can be an array or the single value
        LOG.finer( "Got PrintRequestAttribute value "+result+" for category "+category);
	    }
    }
    // remember
    if (result!=null)
      attributes.add((PrintRequestAttribute)result);
    // done
    return (PrintRequestAttribute)result;
  }
  
  /**
   * @see genj.util.swing.Action2#preExecute()
   */
  protected boolean preExecute() {

    // show dialog
    PrintWidget widget = new PrintWidget(this);

    // prepare actions
    Action[] actions = { 
        new Action2(RESOURCES, "print"),
        Action2.cancel() 
    };

    // show it in dialog
    int choice = WindowManager.getInstance(owner).openDialog("print", title, WindowManager.QUESTION_MESSAGE, widget, actions, owner);

    // keep settings
    registry.put(attributes);

    // check choice
    if (choice != 0 || getPages().width == 0 || getPages().height == 0)
      return false;

    // file output?
    String file = EnvironmentChecker.getProperty(this, "genj.print.file", null, "Print file output");
    if (file!=null)
      attributes.add(new Destination(new File(file).toURI()));
    
    // setup progress dlg
    progress = WindowManager.getInstance(owner).openNonModalDialog(null, title, WindowManager.INFORMATION_MESSAGE, new ProgressWidget(this, getThread()), Action2.cancelOnly(), owner);

    // continue
    return true;
  }

  /**
   * @see genj.util.swing.Action2#execute()
   */
  protected void execute() {
    try {
      service.createPrintJob().print(new SimpleDoc(this, FLAVOR, null), attributes);
    } catch (PrintException e) {
      throwable = e;
    }
  }

  /**
   * @see genj.util.swing.Action2#postExecute(boolean)
   */
  protected void postExecute(boolean preExecuteResult) {
    // close progress
    WindowManager.getInstance(owner).close(progress);
    // something we should know about?
    if (throwable != null) 
      LOG.log(Level.WARNING, "print() threw error", throwable);
    // finished
  }

  /**
   * @see genj.util.Trackable#cancelTrackable()
   */
  public void cancelTrackable() {
    cancel(true);
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
    return RESOURCES.getString("progress", new String[] { "" + (page + 1), "" + (getPages().width * getPages().height) });
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

    // prepare current page/clip
    Point dpi = getResolution();
    
    Rectangle2D printable = getPrintable();
    UnitGraphics ug = new UnitGraphics(graphics, dpi.x, dpi.y);
    ug.pushClip(0,0, printable);

    // translate for to top left on page
    ug.translate(printable.getX(), printable.getY()); 

    // draw content
    renderer.renderPage((Graphics2D)graphics, new Point(col, row), new Dimension2d(printable), dpi, false);
    
    // next
    return PAGE_EXISTS;
  }
  
} //PrintTask