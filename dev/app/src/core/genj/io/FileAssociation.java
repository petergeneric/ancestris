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
package genj.io;

import genj.util.Resources;

import java.awt.Component;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

/**
 * A file association
 */
public class FileAssociation {
  
  private static Logger LOG = Logger.getLogger("genj.io");
  
  /** instances */
  private static List associations = new LinkedList();
  
  /** suffix */
  private Set suffixes = new HashSet();
  
  /** action name e.g. OPEN */
  private String name = "";
  
  /** external app */
  private String executable = "";
  
  /**
   * Constructor
   */
  public FileAssociation() {
  }
  
  /**
   * Constructor
   */
  public FileAssociation(String s) throws IllegalArgumentException {
    // break down into '*'
    StringTokenizer tokens = new StringTokenizer(s,"*");
    if (tokens.countTokens()!=3)
      throw new IllegalArgumentException("need three *-separators");
    // Set comma separated suffixes
    setSuffixes(tokens.nextToken());
    // get name and executable
    name = tokens.nextToken();
    executable = tokens.nextToken();
    // done
  }
  
  /**
   * Constructor
   */
  public FileAssociation(String suffixes, String name, String executable) throws IllegalArgumentException {
    setSuffixes(suffixes);
    this.name = name;
    this.executable = executable;
    // done
  }
  
  /**
   * String representation - usable for constructor
   */
  public String toString() {
    return getSuffixes()+"*"+name+"*"+executable;
  }
  
  /**
   * setter
   */
  public void setName(String set) {
    name = set;
  }
  
  /**
   * Accessor - name
   */
  public String getName() {
    return name;
  }
  
  /**
   * setter
   */
  public void setExecutable(String set) {
    executable = set;
  }
  
  /**
   * Accessor - exec
   */
  public String getExecutable() {
    return executable;
  }
  
  /**
   * Accessor - suffixes as comma separated list
   */
  public void setSuffixes(String set) {
    
    StringTokenizer ss = new StringTokenizer(set,",");
    if (ss.countTokens()==0)
      throw new IllegalArgumentException("need at least one suffix");
    suffixes.clear();
    while (ss.hasMoreTokens())
      suffixes.add(ss.nextToken().trim());
  }
  
  /**
   * Accessor - suffixes as comma separated list
   */
  public String getSuffixes() {
    StringBuffer result = new StringBuffer();
    Iterator it = suffixes.iterator();
    while (it.hasNext()) {
      result.append(it.next());
      if (it.hasNext()) result.append(',');
    }
    return result.toString();
  }
  
  /**
   * Execute
   */
  public void execute(URL url) {
    new Thread(new Sequence(url.toString())).start();
  }
  
  /**
   * Execute
   */
  public void execute(File file) {
    // go
    new Thread(new Sequence(file.getAbsolutePath())).start();
  }
  
  private class Sequence implements Runnable {
    private String file;
    Sequence(String file) {
      this.file = file;
    }
    public void run() {
      runCommands();
    }
    
    private void runCommands() {
      // loop over commands
      StringTokenizer cmds =  new StringTokenizer(getExecutable(), "&");
      while (cmds.hasMoreTokens()) 
        runCommand(cmds.nextToken().trim());
    }      
    
    private void runCommand(String cmd) {
      
      // make sure there's at least one file argument somewhere - quote if necessary
      if (cmd.indexOf('%')<0) {
        cmd = cmd + " " + (file.indexOf(' ')<0 ?  "%" : "\"%\"");
      }

      // look for % replacements
      // example - the forward slash is meant to be a backward slash here
      // file = c:/documents and settings/user/foo.ps
      // path = c://documents and settings//user//foo.ps
      // suffix = ps
      // nosuffix = c://documents and settings//user//foo
      String suffix = getSuffix(file);
      String pathRegEx = file.replaceAll("\\\\","\\\\\\\\");
      String pathNoSuffixRegEx = pathRegEx.substring(0, pathRegEx.length()-suffix.length()-1);
      
      // replace file placeholders %.suffix first
      cmd = Pattern.compile("%(\\.[a-zA-Z]*)").matcher(cmd).replaceAll(pathNoSuffixRegEx+"$1");
      // replace file placholders % next
      cmd = Pattern.compile("%").matcher(cmd).replaceAll(pathRegEx);
      
      // parse it
      String[] cmdarray = parse(cmd);
      
      // run it
      LOG.info("Running command: "+Arrays.asList(cmdarray));
      
      try {
        int rc = Runtime.getRuntime().exec(cmdarray).waitFor(); 
        if (rc!=0) 
          LOG.log(Level.INFO, "External returned "+rc);
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "External threw "+t.getMessage(), t);
      }
      
    }
    
  } // Sequence of Commands run sequentially

  /**
   * Our own parse cmd into tokens - the Java implementation breaks down the cmd into
   * strings not minding quotes. The string is re-assembled fine in the windows implementation
   * but fails to assemble nicely on Linux. This leads to no-quotes and therefore no-spaces
   * on Linux otherwise.
   */
  public static String[] parse(String cmd) {
    
    List tokens = new ArrayList();
    StringBuffer token = new StringBuffer(32);
    boolean quoted=false;
    for (int i=0;i<cmd.length();i++) {
      char c = cmd.charAt(i);
      switch (c) {
        case ' ': 
        case '\t':
          if (quoted) {
            token.append(c);
          } else {
            if (token.length()>0) tokens.add(token.toString());
            token.setLength(0);
          }
          break;
        case '\"':
          if (quoted) {
            tokens.add(token.toString());
            token.setLength(0);
            quoted = false;
          } else {
            if (token.length()>0) tokens.add(token.toString());
            token.setLength(0);
            quoted = true;
          }
          break;
        default:
          token.append(c);
      }
    }
    if (quoted) {
      LOG.warning("Umatched quotes in "+cmd);
    }
    if (token.length()>0) tokens.add(token.toString());
    
    // done 
    return (String[])tokens.toArray(new String[tokens.size()]);
    
  }
  
  /**
   * Gets all
   */
  public static List getAll() {
    return new ArrayList(associations);
  }

  /**
   * Gets associations   */
  public static List getAll(String suffix) {
    List result = new ArrayList();
    Iterator it = associations.iterator();
    while (it.hasNext()) {
      FileAssociation fa = (FileAssociation)it.next();
      if (fa.suffixes.contains(suffix))
        result.add(fa);
    }
    return result;
  }
  
  /**
   * Get the file suffix for given file
   */
  public static String getSuffix(File file) {
    return getSuffix(file.getName());
  }
  
  public static String getSuffix(String file) {
    
    // grab extension
    Matcher m = Pattern.compile(".*\\.(.*)$").matcher(file);
    
    // done
    return m.matches() ? m.group(1) : "";
  }
  
  /**
   * Gets first available association or asks the user for appropriate one
   */
  public static FileAssociation get(File file, String name, Component owner) {
    if (file.isDirectory())
      return get("[dir]", "[dir]", "Directory", owner);
      
    String suffix = getSuffix(file);
    if (suffix.length()==0)
      return null;
    return get(suffix, suffix, name, owner);
  }
  
  /**
   * Gets first available association or asks the user for appropriate one for a browser url and executes it
   */
  public static void open(URL url, Component owner) {
    // is a file?
    if ("file".equals(url.getProtocol())) {
      try {
        // without decoding the notorious space in "my documents" would turn via %20 into %2520 
        String decodedFileName;
        decodedFileName = URLDecoder.decode(url.getFile(),"UTF-8");
        File file = new File(decodedFileName); 
        FileAssociation fa = FileAssociation.get(file, "Open", owner);
        if (fa!=null)
          fa.execute(file);
      } catch (UnsupportedEncodingException e) { }
    } else {
      // find browser capable assoc
      FileAssociation fa = FileAssociation.get("html", "html, htm, xml", "Browse", owner);
      if (fa!=null)  
        fa.execute(url);
    }
  }
  
  /**
   * Gets first available association or asks the user for appropriate one
   */
  public static FileAssociation get(String suffix, String suffixes, String name, Component owner) {
    // look for it
    Iterator it = associations.iterator();
    while (it.hasNext()) {
      FileAssociation fa = (FileAssociation)it.next();
      if (fa.suffixes.contains(suffix))
        return fa;
    }
    // not found - ask for it
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(Resources.get(FileAssociation.class).getString("assocation.choose", suffixes));
    int rc = chooser.showOpenDialog(owner);
    File file = chooser.getSelectedFile(); 
    if (rc!=JFileChooser.APPROVE_OPTION||file==null||!file.exists())
      return null;
    // find out path
    String executable =  file.getAbsolutePath();
    if (executable.indexOf(' ')>=0) executable = "\"" +executable + "\"";
    // keep it
    FileAssociation association = new FileAssociation(suffixes, name, executable);
    add(association);
    // done
    return association;
  }
  
  /**
   * Deletes an association
   */
  public static boolean del(FileAssociation fa) {
    return associations.remove(fa);
  }

  /**
   * Add an association   */
  public static FileAssociation add(FileAssociation fa) {
    if (!associations.contains(fa))
      associations.add(fa);
    return fa;
  }
  
} //FileAssociation
