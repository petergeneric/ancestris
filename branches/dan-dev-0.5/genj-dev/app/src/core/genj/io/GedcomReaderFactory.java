/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2006 Nils Meier <nils@meiers.net>
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
package genj.io;

import genj.crypto.Enigma;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Submitter;
import genj.util.EnvironmentChecker;
import genj.util.MeteredInputStream;
import genj.util.Origin;
import genj.util.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GedcomReader is a custom reader for Gedcom compatible information. Normally
 * it's used by GenJ's application or applet when trying to open a file or
 * simply reading from a stream. This type can be used by 3rd parties that
 * are interested in reading Gedcom into the GenJ object representation as well.
 */
public class GedcomReaderFactory {
  
  private final static Resources RESOURCES = Resources.get("genj.io");
  private static Logger LOG = Logger.getLogger("genj.io");
  
  /** estimated average byte size of one entity */
  private final static int ENTITY_AVG_SIZE = 150;
  
  /**
   * factory method
   */
  public static GedcomReader createReader(Origin origin, GedcomReaderContext context) throws IOException {
    LOG.info("Initializing reader for "+origin);
    return new Impl(new Gedcom(origin), origin.open(), context!=null?context:new DefaultContext());
  }

  public static GedcomReader createReader(InputStream in, GedcomReaderContext context) throws IOException {
    return new Impl(new Gedcom(), in, context!=null?context:new DefaultContext());
  }
  
  /**
   * reader implementation
   */
  private static class Impl implements GedcomReader {

    /** status the reader goes through */
    private final static int READHEADER = 0, READENTITIES = 1, LINKING = 2;
  
    /** lots of state we keep during reading */
    private Gedcom gedcom;
    private int progress;
    private int entity = 0;
    private int state;
    private int length;
    private String gedcomLine;
    private ArrayList<LazyLink> lazyLinks = new ArrayList<LazyLink>();
    private String tempSubmitter;
    private boolean cancel=false;
    private Object lock = new Object();
    private EntityReader reader;
    private MeteredInputStream meter;
    private Enigma enigma;
    private GedcomReaderContext context;
  
    /**
     * Constructor
     */
    private Impl(Gedcom ged, InputStream in, GedcomReaderContext context) throws IOException {
      
      GedcomEncodingSniffer sniffer = new GedcomEncodingSniffer(in);
      Charset charset = sniffer.getCharset();
      String encoding = sniffer.getEncoding();
  
      if (!sniffer.isDeterministic())
        context.handleWarning(0, RESOURCES.getString("read.warn.nochar"), new Context(ged));
  
      String charsetName = EnvironmentChecker.getProperty("genj.gedcom.charset", null, "checking for forced charset for read of "+ged.getName());
      if (charsetName!=null) {
        try {
          charset = Charset.forName(charsetName);
          encoding = Gedcom.UTF8;
        } catch (Throwable t) {
          LOG.log(Level.WARNING, "Can't force charset "+charset, t);
        }
      }
  
      // init some data
      this.length = sniffer.available();
      this.gedcom = ged;
      this.gedcom.setEncoding(encoding);
      this.context = context;
      this.meter = new MeteredInputStream(sniffer);
      this.reader = new EntityReader(new InputStreamReader(meter, charset));
  
      // Done
    }
  
    /**
     * Thread-safe cancel of read()
     */
    public void cancelTrackable() {
      cancel=true;
    }
  
    /**
     * Returns progress of save in %
     * @return percent as 0 to 100
     */
    public int getProgress() {
      
      // reading right now?
      if (state==READENTITIES&&length>0)
          progress = (int)Math.min(100, meter.getCount()*100/length);
  
      // done
      return progress;
    }
  
    /**
     * Returns current read state as explanatory string
     */
    public String getState() {
      switch (state) {
        case READHEADER :
          return RESOURCES.getString("progress.read.header");
        case READENTITIES :default:
          return RESOURCES.getString("progress.read.entities", ""+reader.getLines(), ""+entity );
        case LINKING      :
          return RESOURCES.getString("progress.read.linking");
      }
    }
  
    /**
     * number of lines read
     */
    public int getLines() {
      return reader.getLines();
    }
  
    /**
     * Actually writes the gedcom-information
     * @exception GedcomIOException reading failed
     * @exception GedcomFormatException reading Gedcom-data brought up wrong format
     * @exception GedcomEncryptionException encountered encrypted property and password didn't match
     */
    public Gedcom read() throws GedcomEncryptionException, GedcomIOException, GedcomFormatException {
  
      // check state - we pass gedcom only once!
      if (gedcom==null)
        throw new IllegalStateException("can't call read() twice");
  
      // try it
      try {
        readGedcom();
        return gedcom;
      } catch (GedcomIOException gex) {
        throw gex;
      } catch (Throwable t) {
        // catch anything bubbling up here
        LOG.log(Level.SEVERE, "unexpected throwable", t);
        throw new GedcomIOException(t.toString(), reader.getLines());
      } finally  {
        // close in
        try { reader.in.close(); } catch (Throwable t) {};
        // allow gc to collect gedcom
        gedcom  = null;
        lazyLinks.clear();
      }
  
      // nothing happening here
    }
  
    /**
     * Read Gedcom as a whole
     *
     */
    private void readGedcom() throws IOException {
  
      long start = System.currentTimeMillis();
  
      // Read the Header
      readHeader();
      state++;
      long header =System.currentTimeMillis();
  
      // Read records after the other
      while (reader.readEntity()!=null) {
        if (cancel)
          throw new GedcomIOException("Cancelled", getLines());
      }
        
      long records = System.currentTimeMillis();
  
      // Next state
      state++;
  
      // Prepare submitter
      if (tempSubmitter.length()>0) {
        try {
          Submitter sub = (Submitter)gedcom.getEntity(Gedcom.SUBM, tempSubmitter.replace('@',' ').trim());
          gedcom.setSubmitter(sub);
        } catch (IllegalArgumentException t) {
          context.handleWarning(0, RESOURCES.getString("read.warn.setsubmitter", tempSubmitter), new Context(gedcom));
        }
      }
  
      // Link references
      linkReferences();
      long linking = System.currentTimeMillis();
  
      long total = System.currentTimeMillis();
      LOG.log(Level.FINE, gedcom.getName()+" loaded in "+(total-start)/1000+"s (header "+(header-start)/1000+"s, records "+(records-header)/1000+"s, linking "+(linking-records)/1000+"s)");
  
      // Done
    }
  
    /**
     * linkage
     */
    private void linkReferences() throws GedcomIOException {
  
      // loop over kept references
      for (int i=0,n=lazyLinks.size(); i<n; i++) {
        LazyLink lazyLink = (LazyLink)lazyLinks.get(i);
        try {
          if (lazyLink.xref.getParent()!=null && lazyLink.xref.getTarget()==null)
            lazyLink.xref.link();
          progress = Math.min(100,(int)(i*(100*2)/n));  // 100*2 because Links are probably backref'd
        } catch (GedcomException ex) {
          context.handleWarning(lazyLink.line, ex.getMessage(), new Context(lazyLink.xref));
        } catch (Throwable t) {
          throw new GedcomIOException(RESOURCES.getString("read.error.xref", new Object[]{ lazyLink.xref.getTag(), lazyLink.xref.getValue() }), lazyLink.line);
        }
      }
  
      // done
    }
  
    /**
     * Read Header
     * @exception GedcomIOException reading from <code>BufferedReader</code> failed
     * @exception GedcomFormatException reading Gedcom-data brought up wrong format
     */
    private boolean readHeader() throws IOException {
  
      Entity header = reader.readEntity();
      if (header==null||!header.getTag().equals("HEAD"))
        throw new GedcomFormatException(RESOURCES.getString("read.error.noheader"),0);
  
      //  0 HEAD
      //  1 SOUR GENJ
      //  2 VERS Version.getInstance().toString()
      //  2 NAME GenealogyJ
      //  2 CORP Nils Meier
      //  3 ADDR http://genj.sourceforge.net
      //  1 DEST ANY
      //  1 DATE date
      //  2 TIME time
      //  1 SUBM '@'+gedcom.getSubmitter().getId()+'@'
      //  1 SUBN '@'+gedcom.getSubmission().getId()+'@'
      //  1 GEDC
      //  2 VERS 5.5
      //  2 FORM Lineage-Linked
      //  1 CHAR encoding
      //  1 LANG language
      //  1 PLAC
      //  2 FORM place format
      //  1 FILE file
  
      // check 1 SUBM
      tempSubmitter = header.getPropertyValue("SUBM");
  // NM 20080329 - really GenJ doesn't care whether this is set or not - we should really warn before saving but not
  // when reading - the user is not going to add a submitter to everything he opens so we might as well ignore a
  // missing SUBM
  //    if (tempSubmitter.length()==0)
  //      warnings.add(new Warning(0, RESOURCES.getString("read.warn.nosubmitter"), gedcom));
  
      // check 1 SOUR
      String source = header.getPropertyValue("SOUR");
  // NM 20080329 - same here - GenJ doesn't care and is not going to write this on save anyways
  //    if (source.length()==0)
  //      warnings.add(new Warning(0, RESOURCES.getString("read.warn.nosourceid"), gedcom));
  
      // check for
      // 1 GEDC
      // 2 VERSion and
      // 2 FORMat
      Property vers = header.getPropertyByPath("HEAD:GEDC:VERS");
      if (vers==null||header.getPropertyByPath("HEAD:GEDC:FORM")==null)
        context.handleWarning(0, RESOURCES.getString("read.warn.badgedc"), new Context(gedcom));
      else {
        String v = vers.getValue();
        if ("5.5".equals(v)) {
          gedcom.setGrammar(Grammar.V55);
          LOG.info("Found VERS "+v+" - Gedcom version is 5.5");
        } else if ("5.5.1".equals(v)) {
          gedcom.setGrammar(Grammar.V551);
          LOG.info("Found VERS "+v+" - Gedcom version is 5.5.1");
        } else {
          String s = RESOURCES.getString("read.warn.badversion", v, gedcom.getGrammar().getVersion() );
          context.handleWarning(0, RESOURCES.getString("read.warn.badversion", v, gedcom.getGrammar().getVersion() ), new Context(gedcom));
          LOG.warning(s);
        }
      }
  
  
      // check 1 LANG
      String lang = header.getPropertyValue("LANG");
      if (lang.length()>0) {
        gedcom.setLanguage(lang);
        LOG.info("Found LANG "+lang+" - Locale is "+gedcom.getLocale());
      }
  
      // check 1 CHAR
      String encoding = header.getPropertyValue("CHAR");
      if (encoding.length()>0) {
        gedcom.setEncoding(encoding);
        if (encoding.equals("ASCII"))
          context.handleWarning(0, RESOURCES.getString("read.warn.ascii"), new Context(gedcom));
      }
  
      // check
      // 1 PLAC
      // 2 FORM
      Property plac = header.getProperty("PLAC");
      if (plac!=null) {
        String form = plac.getPropertyValue("FORM");
        gedcom.setPlaceFormat(form);
        LOG.info("Found Place.Format "+form);
      }
  
      // get rid of it for now
      gedcom.deleteEntity(header);
  
      // Done
      return true;
    }
  
    /**
     * our entity reader
     */
    private class EntityReader extends PropertyReader {
  
      /** constructor */
      EntityReader(Reader in) {
        super(in, null, false);
      }
  
      /** read one entity */
      Entity readEntity() throws IOException {
  
        if (!readLine(true))
          throw new GedcomFormatException(RESOURCES.getString("read.error.norecord"),lines);
  
        if (level!=0)
          throw new GedcomFormatException(RESOURCES.getString("read.error.nonumber"), lines);
  
        // Trailer? we're done
        if (tag.equals("TRLR")) {
          // consume any trailing blanks
          if (readLine(true))
            throw new GedcomFormatException(RESOURCES.getString("read.error.aftertrlr"), lines);
          return null;
        }
  
        // Create entity and read its properties
        Entity result;
        try {
  
          result = gedcom.createEntity(tag, xref);
  
          // warn about missing xref if it's a well known type
          if (result.getClass()!=Entity.class&&xref.length()==0)
            context.handleWarning(getLines(), RESOURCES.getString("read.warn.recordnoid", Gedcom.getName(tag)), new Context(result));
  
          // preserve value for those who care
          result.setValue(value);
  
          // continue into properties
          readProperties(result, 0, 0);
  
        } catch (GedcomException ex) {
          throw new GedcomIOException(ex.getMessage(), lines);
        }
  
        // the trailer?
        if (!tag.equals("TRLR"))
          entity++;
  
        // Done
        return result;
      }
  
      /** override read to get a chance to decrypt values */
      protected void readProperties(Property prop, int currentLevel, int pos) throws IOException {
        // let super do its thing
        super.readProperties(prop, currentLevel, pos);
        // decrypt lazy
        decryptLazy(prop);
      }
  
      /**
       * Decrypt a value if necessary
       */
      private void decryptLazy(Property prop) throws GedcomIOException {
  
        // 20060128 an xref is never crypted and getValue() is expensive so we try to avoid this
        if (prop instanceof PropertyXRef)
          return;
        // 20060128 a valid date can't need decryption and getValue() is expensive so we try to avoid this
        if ((prop instanceof PropertyDate)&&prop.isValid())
          return;
  
        // no need to do anything if not encrypted value
        String value = prop.getValue();
        if (!Enigma.isEncrypted(value))
          return;
  
        // set property private
        prop.setPrivate(true, false);
  
        // no need to do anything for unknown password
        if (gedcom.getPassword()==Gedcom.PASSWORD_UNKNOWN) 
          return;
  
        // try to decrypt until we have a good password or bailed
        while (enigma==null) {

          // ask for it
          String pwd = context.getPassword();
          
          // bail if not provided
          if (pwd==null) {
            context.handleWarning(getLines(), RESOURCES.getString("crypt.password.unknown"), new Context(prop));
            gedcom.setPassword(Gedcom.PASSWORD_UNKNOWN);
            return;
          }
          
          // try it
          try {
            enigma = Enigma.getInstance(pwd);
            enigma.decrypt(value);
            gedcom.setPassword(pwd);
          } catch (IOException e) {
            enigma = null;
          }

          // try again if needed
        }
  
        // have enigma - has to work now
        try {
          prop.setValue(enigma.decrypt(value));
        } catch (IOException e) {
          throw new GedcomIOException(RESOURCES.getString("crypt.password.invalid"), lines);
        }
  
        // done
      }
  
      /** keep track of xrefs - we're going to link them lazily afterwards */
      protected void link(PropertyXRef xref, int line) {
        // keep as warning
        lazyLinks.add(new LazyLink(xref, line));
      }
  
      /** keep track of empty lines */
      protected void trackEmptyLine() {
        // care about empty lines before TRLR
        if (!"TRLR".equals(tag))
          context.handleWarning(getLines(), RESOURCES.getString("read.error.emptyline"), new Context(gedcom));
      }
  
      /** keep track of bad levels */
      protected void trackBadLevel(int level, Property parent) {
        context.handleWarning(getLines(), RESOURCES.getString("read.warn.badlevel", ""+level), new Context(parent));
      }
  
      /** keep track of bad properties */
      protected void trackBadProperty(Property property, String message) {
        context.handleWarning(getLines(), message, new Context(property));
      }
  
    } //EntityReader
  
    /**
     * A lazy link
     */
    private static class LazyLink {
  
      private PropertyXRef xref;
      private int line;
  
      LazyLink(PropertyXRef xref, int line) {
        this.xref = xref;
        this.line = line;
      }
    }

  } //GedcomReaderImpl
  
  private static class DefaultContext implements GedcomReaderContext {
    public String getPassword() {
      return null;
    }
    public void handleWarning(int line, String warning, Context context) {
    }
  }

}