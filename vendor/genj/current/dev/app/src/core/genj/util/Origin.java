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
package genj.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An origin describes where a resource came from. This is normally
 * a URL that points to that resource. Other resources can be 
 * loaded relative to the initial origin. Supported origins are
 * file-system (file://) and remote http resource (http://).
 * A resource can be comprised of an archive (jar or zip) - the
 * URL then has to include an anchor that identifies the file
 * in the archive serving as the origin. 
 * Other resources can be opened relative to an origin - that's 
 * either relative to the same 'directory' of the original or
 * pulled from the same archive.
 */
public abstract class Origin {
  
  private static Logger LOG = Logger.getLogger( "genj.util");
  
  /** chars we need */
  private final static char
    BSLASH = '\\',
    FSLASH = '/',
    COLON  = ':';

  /** the url that the origin is based on */
  protected URL url;
  
  /**
   * Constructor
   */
  protected Origin(URL url) {
    this.url = url;
  }

  /**
   * Factory method to create an instance for given location string
   * @param s url as string
   */
  public static Origin create(String s) throws MalformedURLException {
    // delegate
    return create(new URL(s));
  }
  
  /**
   * Factory method to create an instance for given url
   * @param url either http://host/dir/file or ftp://dir/file or
   *   protocol://[host/]dir/file.zip#file 
   */
  public static Origin create(URL url) {

    // What will it be File/ZIP?
    if (url.getFile().endsWith(".zip")) {
      return new ZipOrigin(url);
    } else {
      return new DefaultOrigin(url);
    }

  }

  /**
   * Open this origin for input
   * @return stream to read from
   */
  public abstract InputStream open() throws IOException;

  /**
   * Open resource relative to this origin
   * @param name the name of resource to open - either relative "./somethingelse"
   *  or absolute "file://dir/somethingelse"
   */
  public final InputStream open(String name) throws IOException {

    // Make sure name is correctly encoded
    name = back2forwardslash(name);

    // Absolute file specification?
    if (ABSOLUTE.matcher(name).matches()) {
      
      LOG.fine("Trying to open "+name+" as absolute path (origin is "+this+")");

      URLConnection uc;
      try {
        uc = new URL(name).openConnection();
      } catch (MalformedURLException e1) {
        // ... hmmm, with "file:"?
        try {
          // 20021210 using file:// here seems to slow things down if no
          // file exists
          uc = new URL("file:"+name).openConnection();
        } catch (MalformedURLException e2) {
          return null;
        }
      }
      return new InputStreamImpl(uc.getInputStream(),uc.getContentLength());

    }

    // relative file
    LOG.fine("Trying to open "+name+" as relative path (origin is "+this+")");
    
    return openImpl(name);
  }
  
  /**
   * Open file relative to origin
   */
  protected abstract InputStream openImpl(String name) throws IOException;

  /**
   * String representation
   */
  public String toString() {
    return url.toString();
  }

  /**
   * Tries to calculate a relative path for given file
   * @param file the file that might be relative to this origin
   * @return relative path or null if not applicable
   */
  private final static Pattern ABSOLUTE = Pattern.compile("([a-z]:).*|([A-Z]:).*|\\/.*|\\\\.*");
  
  public String calcRelativeLocation(String file) {

    // 20060614 by looking at Daniel's log-file with FINE enabled I was able to see that
    // files are opened as file:/foo/bar/...
    // Some code change from march took out the file:/ making the filename effectively
    // relative to user.dir ... not a good idea
    String here = url.toString();
    // .. so lets first check for file:// and strip file:/ away if we can
    if (here.startsWith("file://"))
      here = here.substring("file:/".length());
    // .. a single file:/foo/bar we'll turn into /foo/bar or file:foo/bar into foo/bar
    else if (here.startsWith("file:"))
      here = here.substring("file:".length());
    
    // a relative path can't be made relative
    if (!ABSOLUTE.matcher(file).matches())
      return null;
    
    // try to compare canonical forms
    try {
      here = back2forwardslash(new File(here.substring(0,here.lastIndexOf(FSLASH))).getCanonicalPath()) + "/";
      file = back2forwardslash(new File(file).getCanonicalPath()); 
      
      boolean startsWith = file.startsWith(here);
      LOG.fine("File "+file+" is "+(startsWith?"":"not ")+"relative to "+here);
      if (startsWith)
        return file.substring(here.length());
    } catch (Throwable t) {
    }
    

    // no good
    return null;
  }
  
  /**
   * Lists the files available at this origin if that information is available
   */
  public abstract String[] list() throws IOException ;
  
  /**
   * Returns the Origin as a File or null if no local gedcom file can be named
   */
  public abstract File getFile();
  
  /**
   * Returns an absolute file representation of a resource relative 
   * to this origin
   * @exception IllegalArgumentException if not applicable (e.g. for origin http://host/dir/file)
   */
  public abstract File getFile(String name);

  /**
   * Returns the Origin's filename. For example
   * <pre>
   *  file://d:/gedcom/[example.ged]
   *  http://host/dir/[example.ged]
   *  http://host/dir/archive.zip#[example.ged]
   * </pre>
   */
  public String getFileName() {
    return getName();
  }

  /**
   * Returns the origin's distinctive name. For example
   * <pre>
   *  file://d:/gedcom/[example.ged]
   *  http://host/dir/[example.ged]
   *  http://host/dir/[archive.zip#example.ged]
   * </pre>
   */
  public String getName() {
    String path = back2forwardslash(url.toString());
    if (path.endsWith(""+FSLASH))
      path = path.substring(0, path.length()-1);
    return path.substring(path.lastIndexOf(FSLASH)+1);
  }
  
  /**
   * Object Comparison
   */
  public boolean equals(Object other) {
    return other instanceof Origin && ((Origin)other).url.toString().equals(url.toString());
  }
  
  /**
   * Object hash
   */
  public int hashCode() {
    return url.toString().hashCode();
  }
  
  /**
   * Returns a cleaned up string with forward instead
   * of backwards slash(e)s
   */
  protected String back2forwardslash(String s) {
    return s.toString().replace(BSLASH, FSLASH);
  }
  
  /**
   * A default origin 
   */
  private static class DefaultOrigin extends Origin {

    /**
     * Constructor
     */
    protected DefaultOrigin(URL url) {
      super(url);
    }
    
    /**
     * @see genj.util.Origin#open()
     */
    public InputStream open() throws IOException {
      URLConnection uc = url.openConnection();
      return new InputStreamImpl(uc.getInputStream(),uc.getContentLength());
    }
    
    /**
     * @see genj.util.Origin#openImpl(java.lang.String)
     */
    protected InputStream openImpl(String name) throws IOException {

      // Calc the file's name
      String path = back2forwardslash(url.toString());
      path = path.substring(0, path.lastIndexOf(FSLASH) +1) + name;

      // Connect
      try {

        URLConnection uc = new URL(path).openConnection();
        return new InputStreamImpl(uc.getInputStream(),uc.getContentLength());

      } catch (MalformedURLException e) {
        throw new IOException(e.getMessage());
      }

    }

    /**
     * list directory of origin if file
     */
    public String[] list() {
      File dir = getFile();
      if (dir==null) 
        throw new IllegalArgumentException("list() not supported by url protocol");
      if (!dir.isDirectory())
        dir = dir.getParentFile();
      return dir.list();
    }
    
    /**
     * @see genj.util.Origin#getFile()
     */
    public File getFile() {
      return "file".equals(url.getProtocol()) ? new File(url.getFile()) : null;
    }

    /**
     * @see genj.util.Origin#getFile(java.lang.String)
     */
    public File getFile(String file) {
      
      // good argument?
      if (file.length()<1) return null;
      
      // Absolute file specification?
      if (ABSOLUTE.matcher(file).matches()) 
        return new File(file);
      
      // should be in parent directory
      return new File(getFile().getParent(), file);
    }


  } //DefaultOrigin
 

  /**
   * Class which stands for an origin of a resource - this Origin
   * is pointing to a ZIP file so all relative files are read
   * from the same archive
   */
  private static class ZipOrigin extends Origin {

    /** cached bytes */
    private byte[] cachedBits;

    /**
     * Constructor
     */
    protected ZipOrigin(URL url) {
      super(url);
    }

    /**
     * list directory of origin if file
     */
    public String[] list() throws IOException {
      ArrayList result = new ArrayList();
      ZipInputStream in  = openImpl();
      while (true) {
        ZipEntry entry = in.getNextEntry();
        if (entry==null) break;
        result.add(entry.getName());
      }
      in.close();
      return (String[]) result.toArray(new String[result.size()]);
    }
    
    /**
     * @see genj.util.Origin#open()
     */
    public InputStream open() throws IOException {

      // There has to be an anchor into the zip
      String anchor = url.getRef();
      if ((anchor==null)||(anchor.length()==0)) {
        throw new IOException("ZipOrigin needs anchor for open()");
      }

      // get it (now relative)
      return openImpl(anchor);
    }
    
    /**
     * open the zip input stream
     */
    private ZipInputStream openImpl() throws IOException {
      
      // We either load from cached bits or try to open the connection
      if (cachedBits==null) try {
        cachedBits = new ByteArray(url.openConnection().getInputStream(), true).getBytes();
      } catch (InterruptedException e) {
        throw new IOException("interrupted while opening "+getName());
      }

      // Then we can read the zip from the cached bits
      return new ZipInputStream(new ByteArrayInputStream(cachedBits));

    }
    
    /**
     * @see genj.util.Origin#openImpl(java.lang.String)
     */
    protected InputStream openImpl(String file) throws IOException {

       ZipInputStream zin = openImpl();

      // .. loop through files
      for (ZipEntry zentry = zin.getNextEntry();zentry!=null;zentry=zin.getNextEntry()) {
        if (zentry.getName().equals(file)) 
          return new InputStreamImpl(zin, (int)zentry.getSize());
      }

      // not found
      throw new IOException("Couldn't find resource "+file+" in ZIP-file");
    }

    /**
     * @see genj.util.Origin#getFile()
     */
    public File getFile() {
      return null;
    }

    /**
     * Returns the Origin's Filename file://d:/gedcom/example.zip#[example.ged]
     * @see genj.util.Origin#getFileName()
     */
    public String getFileName() {
      return url.getRef();
    }

    /**
     * File not available
     * @see genj.util.Origin#getFile(java.lang.String)
     */
    public File getFile(String name) {
      return null;
    }

  } //ZipOrigin
  
  /**
   * An InputStream returned from Origin
   */
  private static class InputStreamImpl extends InputStream {

    /** wrapped input stream */
    private InputStream in;
    
    /** length of data */
    private int len;

    /**
     * Constructor
     */
    protected InputStreamImpl(InputStream in, int len) {
      this.in=in;
      this.len=len;
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
      return in.read();
    }
    
    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
      return in.read(b, off, len);
    }

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
      return len;
    }
    
    /**
     * 20040220 have to delegate close() to 'in' to make
     * sure the input is closed right (file open problems)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
      in.close();
    }

  } //InputStreamImpl

} //Origin
