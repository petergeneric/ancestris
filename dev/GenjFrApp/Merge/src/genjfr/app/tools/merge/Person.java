/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.merge;

import genj.gedcom.Indi;
import java.util.HashSet;

/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class Person {

   // For performance reasons, do not store methods in a Person object
   // but rather in an extension
   
   static private int SIZE = 36;
   
   public boolean merged       = false;

   public Indi indi            = null;
   public int sex              = 0; 
   public boolean birthInfo    = false;
   public int yearMin          = -4000;    // birth date year min
   public int yearMax          = +4000;    // birth date year max
   public String id            = "";

   public String lastName      = "";
   public int lastNameLength   = 0;
   public int[] lastNameCode   = new int[SIZE];
   public String lastPhonex    = "";

   public String firstName     = "";
   public int firstNameLength  = 0;
   public int[] firstNameCode  = new int[SIZE];
   public String firstPhonex   = "";

   public int bS               = 0;   // birth start day in julian day
   public int bE               = 0;   // end

   public String birth         = "";
   public int birthLength      = 0;
   public int[] birthCode      = new int[SIZE];

   public int dS               = 0;   // death start day in julian day
   public int dE               = 0;   // end

   public String death         = "";
   public int deathLength      = 0;
   public int[] deathCode      = new int[SIZE];

   public int mS               = 0;   // marr start day in julian day
   public int mE               = 0;   // end

   public String marr          = "";
   public int marrLength       = 0;
   public int[] marrCode       = new int[SIZE];

   public int buS              = 0;   // burial start day in julian day
   public int buE              = 0;   // end

   public String burial        = "";
   public int burialLength     = 0;
   public int[] burialCode     = new int[SIZE];

   public Person father        = null;
   public Person mother        = null;
   public HashSet partners     = null;    // list of persons
   public HashSet kids         = null;    // list of persons
   public HashSet siblings     = null;    // list of persons 

   public String defLastName   = "";  // defaulted to lastname of Person > father > natural kids > siblings of same father > or empty
   public int deflnLength      = 0;
   public int[] deflnCode      = new int[SIZE];

} // end of object
  
