/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

import genj.gedcom.Entity;

import java.util.HashSet;


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
   
   public String title         = "";               
   public String text          = "";               
   public String auth          = "";               
   public String abbr          = "";               
   public int    titleLength   = 0;                
   public int[]  titleCode     = new int[SIZE];    
   public int    authLength    = 0;                
   public int[]  authCode      = new int[SIZE];    
   public int    abbrLength    = 0;                
   public int[]  abbrCode      = new int[SIZE];    
   public int    textLength    = 0;                
   public HashSet textCode     = new HashSet();    // of strings
  
} // end of object
  
