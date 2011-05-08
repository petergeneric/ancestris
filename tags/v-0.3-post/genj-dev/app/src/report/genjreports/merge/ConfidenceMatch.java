/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjreports.merge;

import genj.gedcom.Entity;
import java.util.Comparator;


/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class ConfidenceMatch implements Comparator {
   public Entity  ent1       = null;   // entity 1 assessed
   public Entity  ent2       = null;   // entity 2 assessed
   public String  id2        = null;   // Old id of the entity 2 (A does not change id)
   public int     confLevel  = 0;      // automatic scoring result of pair
   public boolean confirmed  = false;  // user/auto confirmed or not 
   public boolean toBeMerged = false;  // outcome result: should be merged or not
   public int     choice     = 0;      // In case of merge, ent1 or ent2 chosen

   public ConfidenceMatch() {
      this(null, null);
      }

   public ConfidenceMatch(Entity ent1, Entity ent2) {
      this.ent1 = ent1;
      this.ent2 = ent2;
      }

   // sort is descending on level (largest confidence first)
   public int compare(Object o1, Object o2) {
      return (((ConfidenceMatch)o2).confLevel - ((ConfidenceMatch)o1).confLevel);
      }

} // end of object

