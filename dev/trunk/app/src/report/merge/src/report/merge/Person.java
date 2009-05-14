/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

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
   
   public Indi indi            = null;
   
   public boolean merged       = false;
   public int sex              = 0; 
   public boolean birthInfo    = false;
   public int yearMin          = -4000;    // birth date year min
   public int yearMax          = +4000;    // birth date year max
   
   public String id            = "";
   public String lastName      = "";
   public String firstName     = "";
   public int bS               = 0;   // birth start day in julian day
   public int bE               = 0;   // end
   public String birthCity     = "";
   public String birthCountry  = "";
   public String birthPlace    = "";
   public int dS               = 0;   // death start day in julian day
   public int dE               = 0;   // end
   public String deathCity     = "";
   public String deathPlace    = "";
   public int lastNameLength   = 0;
   public int firstNameLength  = 0;
   public int birthCityLength  = 0;
   public int birthPlaceLength = 0;
   public int deathCityLength  = 0;
   public int deathPlaceLength = 0;
   public int[] lastNameCode   = new int[SIZE];
   public int[] firstNameCode  = new int[SIZE];
   public int[] birthCityCode  = new int[SIZE];
   public int[] birthPlaceCode = new int[SIZE];
   public int[] deathCityCode  = new int[SIZE];
   public int[] deathPlaceCode = new int[SIZE];
   public Person father        = null;
   public Person mother        = null;
   public HashSet partners     = null;    // list of persons
   public HashSet kids         = null;    // list of persons
   public HashSet siblings     = null;    // list of persons 
  
} // end of object
  
