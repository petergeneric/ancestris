/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

import genj.gedcom.Entity;


/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class Info {

   // For performance reasons, do not store methods in a Info object
   // but rather in an extension
   
   static private int SIZE = 36;
   
   public Entity entity        = null;
   public String id            = ""; 
   
   public boolean merged       = false;
   
   public String title         = "";               // for all non indi/fam entities
   public String text          = "";               // for sources only
   public String auth          = "";               // for sources only
   public String abbr          = "";               // for sources only 
   public int    titleLength   = 0;                // for sources only
   public int[]  titleCode     = new int[SIZE];    // for sources only
   public int    textLength    = 0;                // for sources only
   public int[]  textCode      = new int[SIZE];    // for sources only
   public int    authLength    = 0;                // for sources only 
   public int[]  authCode      = new int[SIZE];    // for sources only
   public int    abbrLength    = 0;                // for sources only
   public int[]  abbrCode      = new int[SIZE];    // for sources only
   
  
} // end of object
  
