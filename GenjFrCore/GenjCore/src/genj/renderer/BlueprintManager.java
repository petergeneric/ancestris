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
package genj.renderer;

import genj.gedcom.Gedcom;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A manager for our blueprints */
public class BlueprintManager {
  
  private final static String SUFFIX = ".html";
  private final static Registry REGISTRY = Registry.get(BlueprintManager.class);
  private final static Resources RESOURCES = Resources.get(BlueprintManager.class);
  
  public final static String TXT_BLUEPRINT = RESOURCES.getString("blueprint");
  
  private final static String[][] DEFAULTS = {
   { "INDI", "default", "complete", "verbose", "colorful", "professional", "simple", "pastel", "light", "small" },
   { "FAM", "default", "complete", "simple", "pastel", "light", "small" },
   { "OBJE", "default", "complete", "55" },
   { "NOTE", "default", "complete" },
   { "SOUR", "default", "complete" },
   { "SUBM", "default", "complete" },
   { "REPO", "default", "complete" }
  };

  /*package*/ final static Logger LOG = Logger.getLogger("genj.renderer");

  /** blueprints per entity */
  private Map<String, List<Blueprint>> tag2blueprints = new HashMap<String, List<Blueprint>>();

  /** singleton */
  private static BlueprintManager instance;
  
  /**
   * Singleton access
   */
  public static BlueprintManager getInstance() {
    if (instance==null)
      instance = new BlueprintManager();
    return instance;
  }
  
  /**
   * Constructor   */
  private BlueprintManager() {
    
    // load readonly/predefined blueprints (from resources)
    for (int t=0;t<DEFAULTS.length;t++) {
      
      String[] defaults = DEFAULTS[t];
      
      String tag = defaults[0];

      for (int i=1;i<defaults.length;i++) {
        String key = defaults[i];
        String name = RESOURCES.getString("blueprints."+key, false);
        if (name==null)
          name = key;
        try {
          addBlueprint(loadBlueprint(
              getClass().getResourceAsStream("blueprints/"+tag+"/"+key+SUFFIX),
              tag,
              name,
              true
          ));
        } catch (Throwable e) {
          LOG.warning("can't read pre-defined blueprint "+tag+"/"+key);
        }
      }
      
    }
    
    // load user defined blueprints from disk
    loadBlueprints();
    
    // done
  }
  
  /**
   * Resolve blueprint directory
   */
  private File getBlueprintDirectory() {
    return new File(EnvironmentChecker.getProperty("user.home.genj/blueprints", "?", "Looking for blueprints"));
  }
  
  /**
   * Resolve blueprint filename
   */
  private File getBlueprintFile(Blueprint blueprint) throws IOException {
    // check for quotes in there
    if (blueprint.getName().indexOf('\"')>=0)
      throw new IOException("Quotes are not allowed in blueprint names");
    return new File(getBlueprintDirectory(), "/"+blueprint.getTag()+"/"+blueprint.getName()+SUFFIX).getCanonicalFile(); 
  }
  
  /**
   * Save a blueprint to disk
   */
  /*package*/ void saveBlueprint(Blueprint blueprint) throws IOException {
    
    // necessary?
    if (!blueprint.isDirty())
      return;
    
    // put it back to where it belongs
    File file = getBlueprintFile(blueprint); 
    File parent = file.getParentFile();
    if ( (!parent.exists()&&!parent.mkdirs()) ||!parent.isDirectory() )
      throw new IOException("Cannot create folder for blueprint "+blueprint.getName());
    
    readwrite(new StringReader(blueprint.getHTML()), new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
    
    blueprint.clearDirty();
    
    LOG.log(Level.INFO, "saved blueprint "+file);
    
    // done
  }
  
  /**
   * Load all available blueprints from disk
   */
  private void loadBlueprints() {
    
    File dir = getBlueprintDirectory();
    try {
      
      // exists?
      if (!dir.isDirectory()||!dir.exists())
        return;
  
      // do it for each entity we know about
      for (int i=0; i<Gedcom.ENTITIES.length; i++) 
        loadBlueprints(dir, Gedcom.ENTITIES[i]);
    
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "unexpected throwable loading blueprints from "+dir, t);
    }
  }
  
  /**
   * Load blueprints for one entity from disk
   */
  private void loadBlueprints(File dir, String tag) throws IOException {
    
    // exists?
    dir = new File(dir, tag);
    if (!dir.isDirectory()||!dir.exists())
      return;
    
    // loop over blueprints
    File[] files = dir.listFiles();
    for (int b=0;b<files.length;b++) {
      
      // check name of blueprint
      File file = files[b];
      String name = file.getName();
      if (!name.endsWith(SUFFIX)||file.isDirectory())
        continue;
      name = name.substring(0, name.length()-SUFFIX.length());
      
      Blueprint blueprint = loadBlueprint(new FileInputStream(file), tag, name, false);
      blueprint.clearDirty();
      addBlueprint(blueprint);
      
    }
    
    // done
  }
  
  /**
   * Load one blueprint from inputstream
   */
  private Blueprint loadBlueprint(InputStream in, String tag, String name, boolean readOnly) throws IOException {
    
    StringWriter html = new StringWriter(512);
    readwrite(new InputStreamReader(in, "UTF8"), html);
    in.close();
    
    return new Blueprint(tag, name, html.toString(), readOnly);
  }
  
  /**
   * transfer lines
   */
  private void readwrite(Reader in, Writer out) throws IOException {
    // transfer in to out
    BufferedReader bin = new BufferedReader(in);
    BufferedWriter bout = new BufferedWriter(out);
    while (true) {
      String line = bin.readLine();
      if (line==null) break;
      bout.write(line);
      bout.newLine();
    }
    bin.close();
    bout.close();
    // done
  }
  
  /**
   * Blueprint for given type with given name
   * @param origin an optional context that blueprints are loaded from if necessary
   * @param tag the entity tag the blueprint is supposed to be for
   * @param the name of the blueprint   */
  public Blueprint getBlueprint(String tag, String name) {
    // patch name if default
    if (name.length()==0)
      name = "Default";
    // look through global blueprints for that type
    List<Blueprint> bps = getBlueprints(tag);
    for (int i=0; i<bps.size(); i++) {
      Blueprint bp = (Blueprint)bps.get(i);
      // .. found! return
      if (bp.getName().equals(name)) 
        return bp;   	
    }
    // fallback try first
    return (Blueprint)bps.get(0);
  }
  
  /**
   * Blueprints for a given type   */
  public List<Blueprint> getBlueprints(String tag) {
    return Collections.unmodifiableList(getBlueprintsInternal(tag));
  }
  
  private List<Blueprint> getBlueprintsInternal(String tag) {
    List<Blueprint> result = tag2blueprints.get(tag);
    if (result==null) {
      result = new ArrayList<Blueprint>();
      tag2blueprints.put(tag, result);
    }
    return result;
  }
  
  /**
   * Adds a blueprint   */
  public Blueprint addBlueprint(Blueprint blueprint) throws IOException {
    
    // try calculating its filename - just for test in case it's !readOnly
    if (!blueprint.isReadOnly())
      getBlueprintFile(blueprint);
    
    // keep it overriding same name unless read-only
    List<Blueprint> blueprints = getBlueprintsInternal(blueprint.getTag());
    for (ListIterator<Blueprint> it=blueprints.listIterator(); it.hasNext(); ) {
      Blueprint other = (Blueprint)it.next();
      // found one with same name?
      if (other.getName().equalsIgnoreCase(blueprint.getName())) {
        // don't allow if read only
        if (other.isReadOnly())
          throw new IOException("Can't overwrite read-only blueprint");
        // remove
        it.remove();
        break;
      }
    }
    
    // save it
    if (!blueprint.isReadOnly())
      saveBlueprint(blueprint);
    
    // keep it
    blueprints.add(blueprint);
    
    // done 
    return blueprint;
  }
  
  /**
   * Deletes a blueprint   */
  public void delBlueprint(Blueprint blueprint) throws IOException {
    // allowed?
    if (blueprint.isReadOnly()) 
      throw new IOException("Can't delete read-only Blueprint");
    // remove it from disk
    if (!blueprint.isReadOnly()) {
      if (!getBlueprintFile(blueprint).delete()) 
        throw new IOException("Couldn't delete blueprint "+blueprint);
    }
    // remove it
    getBlueprintsInternal(blueprint.getTag()).remove(blueprint);
    // done
  }
  
} //BlueprintManager
