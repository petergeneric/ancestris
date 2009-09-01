/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.util.DirectAccessTokenizer;
import genj.util.Resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This type knows how to convert geo data file into cvs for genj's geo service 
 * Sources supported are USGS (Gazetteer)
 *  http://geonames.usgs.gov/geonames/stategaz/index.html
 * and GNS (GEOnet Name Server)
 *  http://earth-info.nga.mil/gns/html/cntry_files.html
 */
public class GeoImport {

  private static Charset UTF8 = Charset.forName("UTF8");
  private static Parser USGS = new USGSParser(), GNS = new GNSParser(); 
  
  private Writer sqlOut;
  private int nLocations,nJurisdictions;
  
  /**
   * Constructor
   */
  private GeoImport(File out) throws IOException {
    this.sqlOut = new OutputStreamWriter(new FileOutputStream(out), UTF8);
    sqlOut.write("SET NAMES utf8; SET CHARACTER SET utf8;");
  }
  
  /**
   * Log some stuff
   */
  private static void log(String msg) {
    System.out.println(msg);
  }

  /**
   * Main method
   */
  public static void main(String[] args) {
    
    // check argument
    if (args.length<2) {
      log("Use : GeoImport [-j create jurisdictions] [path to folder with geodata files from USGS or GNS] [output filename]");
      return;
    }
    
    // run it
    try {
      
      int files = 0;
      
      // jurisdiction option?
      boolean jurisdictions = false;
      if (args[0].equals("-j")) {
        jurisdictions = true;
        files++;
      }
      
      // do the import
      GeoImport gi = new GeoImport(new File(args[files+1]));
      if (jurisdictions) gi.parseJurisdictions();
      gi.parseFolder(new File(args[files]));
      gi.close();
      
      // done
      log("Done: "+gi.nLocations+" places and "+gi.nJurisdictions+" jurisdictions generated");
      
    } catch (IOException e) {
      log("Err : "+e.getMessage());
    }

  }
  
  /**
   * Parse jurisdictions
   */
  private void parseJurisdictions() throws IOException {
    
    // open properties file
    Resources jurisdictions = new Resources(getClass().getResourceAsStream("jurisdictions.properties"));
    
      // look for all 'xx.yy = aaa,bbb,ccc'
      // where 
      //  xx = country
      //  yy = state code
      //  aaa,bbb,ccc = state names or abbreviations
      for (Iterator keys = jurisdictions.getKeys().iterator(); keys.hasNext(); ) {
        // grab key
        String key = keys.next().toString();
        if (key.length()!=5) continue;
        // analyze country and jurisdiction code
        String country = key.substring(0,2);
        String adm1 = key.substring(3,5);
        // parse all of its names
        StringTokenizer names = new StringTokenizer(jurisdictions.getString(key), ",");
        for (int n=0; names.hasMoreTokens(); n++) {
          nJurisdictions++;
          
          sqlOut.write("INSERT INTO jurisdictions VALUES (\"");
          sqlOut.write(country.toLowerCase());
          sqlOut.write("\",\"");
          sqlOut.write(adm1.toLowerCase()); //paranoid as we are
          sqlOut.write("\",\"");
          sqlOut.write(names.nextToken().trim());
          sqlOut.write("\",");
          sqlOut.write(n==0 ? '1' : '0');
          sqlOut.write(");");
          
        }
      }
      
      // done

  }
  
  /**
   * Parse folder for GNS or USGS files
   */
  private void parseFolder(File folder) throws IOException {
    
    // loop over files
    File[] files = folder.listFiles(); 
    for (int f=0;f<files.length;f++) { 
      
      // parse each file
      File file = files[f];
      if (file.isFile())
        parseFile(files[f]);

      // don't do more than 100.000 records into one file
    }
    
     // done
  }
  
  /**
   * Read an import file
   */
  private void  parseFile(File file) throws IOException {
    
    // grab parser
    Parser parser = getParser(file);
    if (parser==null) {
      log("Info: Skipping "+file);
      return;
    }
    log("Info: Parsing "+file+" as "+parser.getName());
    
    // calculate filename without suffix
    String filename = file.getName();
    if (filename.indexOf('.')>0) filename = filename.substring(0, filename.indexOf('.'));
    
    // open and read 
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));

    in.readLine(); // skip header
    
    while (true) {
      // read line
      String line = in.readLine();
      if (line==null) break;
      // parse it
      if (parser.parse(line, filename))  {
        nLocations++;
        parser.write(sqlOut);
      }
      // next
    }
    
    in.close();
    
    // done
  }
  
  /**
   * close any open files
   */
  private void close() throws IOException {
    if (sqlOut!=null) {
      sqlOut.close();
      sqlOut = null;
    }      
  }

  /**
   * find parser for file
   */
  private Parser getParser(File file) {
    // check name
    String name = file.getName();
    // USGS?
    if (name.matches("[A-Z][A-Z]_DECI.TXT"))
      return USGS;
    // GNS?
    if (name.matches("[a-z][a-z].txt"))
      return GNS;
    // hmm
    return null;
  }
  
  /**
   * A parser for either USGS or GNS files
   */
  private static abstract class Parser {
    
    String country;
    String state;
    String city;
    float lat, lon;
    
    abstract String getName();
    abstract boolean parse(String line, String filename);
    
    /**
     * write parsed value
     */
    void write(Writer out) throws IOException {

      // safety check
      if (city.indexOf('\"')>0) {
        log("Warn: removing quote from city "+city);
        city = city.replace('\"', ' ');
      }
      if (country.indexOf('\"')>0) 
        throw new IOException("Found quote in text"+country);
      if (country.length()!=2) 
        throw new IOException("Found bad country "+country);
      if (state.indexOf('\"')>0) 
        throw new IOException("Found quote in text"+state);
      if (state.length()>2) 
        throw new IOException("Found bad state "+state);

      // write place
      out.write("INSERT INTO locations VALUES (\"");
      out.write(city);
      out.write("\",\"");
      out.write(state.toLowerCase()); //paranoid as we are
      out.write("\",\"");
      out.write(country.toLowerCase());
      out.write("\",");
      out.write(Float.toString(lat));
      out.write(",");
      out.write(Float.toString(lon));
      out.write(");");
      
      // done
    }
    
  }
  
  /**
   * A USGS parser - us state files
   */
  private static class USGSParser extends Parser {
    
    /** logical name */
    String getName() { return "USGS"; }
    
    /** parsing logic  */
    boolean parse(String line, String filename) {
      
      // FID (state) (name) (type) county state# county# (lat) (lon) lat lon dmslat dmslon dmslat dmslon elev poption fedstat cell
      DirectAccessTokenizer values = new DirectAccessTokenizer(line, "|");

      // country
      country = "us";
      
      // state
      state = values.get(1);
      
      // grab name
      city = values.get(2);
      
      // look for 'populated areas' only
      if (!"ppl".equals(values.get(3))) 
        return false;
      
      // grab lat lon
      try {
        String 
          sLat = values.get(9),
          sLon = values.get(10); 
        if (sLat.length()==0||"UNKNOWN".equals(sLat)||sLon.length()==0||"UNKNOWN".equals(sLon)) 
          return false;
        lat = Float.parseFloat(sLat); // LAT
        lon = Float.parseFloat(sLon); // LON

      } catch (NumberFormatException e) {
        log("Info: format problem in: "+line);
        return false;
      }
      
      // done
      return true;

    }
  }
  
  /**
   * A GNS parser - country files
   */
  private static class GNSParser extends Parser  {

    private static Properties fips2iso;

    /** constructor */
    GNSParser() {
      fips2iso = new Properties();
      try {
        fips2iso.load(GeoImport.class.getResourceAsStream("fips2iso.properties"));
      } catch (IOException e) {
      }
    }
    
    /** logical name */
    String getName() { return "GNS"; }

    /** parsing logic  */
    boolean parse(String line, String filename) {
      
      // RC UFI UNI (LAT) (LON) DMSLAT DMSLON UTM JOG (FC) DSG PC (CC1) (ADM1) ADM2 DIM CC2 NT LC SHORT_FORM GENERIC SORT_NAME (NAME) FULL MOD
      DirectAccessTokenizer values = new DirectAccessTokenizer(line, "\t");
      
      try {
        lat = Float.parseFloat(values.get(3)); // LAT
        lon = Float.parseFloat(values.get(4)); // LON
      } catch (NumberFormatException e) {
        log("Info: format problem in: "+line);
        return false;
      }

      // look for 'populated areas' only
      String cat = values.get(9);
      if (cat.length()==0||'P'!=cat.charAt(0)) 
        return false;
      
      // use filename as iso country code - the CC1 I used before can be something else
      country = filename.toLowerCase();
      String iso = (String)fips2iso.get(country);
      if (iso!=null) country = iso;
      
      // grab state
      state = values.get(13);
      if (state.length()>2) 
        return false;
      
      // grab name
      city = values.get(22); // FULL_NAME

      // done
      return true;
      
    }
    
  }
  
}
