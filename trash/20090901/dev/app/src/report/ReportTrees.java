/**
 * ReportTree
 *
 * Copyright (c) 2003 Tom Morris
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
 /*
  * TODO Daniel: add start(Indi) and start(Indi[]) entry points
  * TODO Daniel: print pedigree chart for trees less than ???
  * TODO Daniel: print additionnal info (ox+) for each indi
  */

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;


/**
 * GenJ - ReportTree
 *
 * $Header:
 * @author Tom Morris
 * @version 1.01
 */
public class ReportTrees extends Report {

    /**
     * interface when available
     */
    public int minGroupSize = 2;  // Don't print groups with size less than this
    public int maxGroupSize = 20;

    /**
     * This method actually starts this report
     */
    public void start(Gedcom gedcom) {

		String title = translate("fileheader",gedcom.getName());
        // Get a list of the individuals
        Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");

        println(title);

        // Step through all the Individuals we haven't seen yet
        println(translate("indicount",indis.length)+"\n");


        HashSet unvisited = new HashSet(Arrays.asList(indis));
        start(indis,unvisited);
	}

  /**
   * Main for argument individual
   */
  public void start(Indi indi) {

      println( translate("indiheader",indi.getName()));
      println();

    HashSet unvisited = new HashSet(Arrays.asList(indi.getGedcom().getEntities(Gedcom.INDI, "INDI:NAME")));

    start( new Indi[] { indi }, unvisited);
  }

  public void start(Indi[] indis) {
      println(translate("indisheader",indis.length));

      for (int i=0; i<indis.length; i++) {
          println ("- "+indis[i].getName());
      }
      println();

      // Get a list of the individuals
    HashSet unvisited = new HashSet(Arrays.asList(indis[0].getGedcom().getEntities(Gedcom.INDI, "INDI:NAME")));
      start(indis,unvisited);
    }

  public void start(Entity[] indis, HashSet allIndis) {
        HashSet unvisited = new HashSet(Arrays.asList(indis));
        List trees = new ArrayList();
        while (!unvisited.isEmpty()) {
          Indi indi = (Indi)unvisited.iterator().next();

          // start a new sub-tree
          Tree tree = new Tree();

          // indi has been visited now
          unvisited.remove(indi);

          // collect all relatives
          iterate(indi, tree, allIndis);

          // remember
          trees.add(tree);
        }

        // Report about groups
        if (!trees.isEmpty()) {

          // Sort in descending order by count
          Collections.sort(trees);

          // Print sorted list of groups
          println(align(translate("count"),7, Report.ALIGN_RIGHT)+"  "+translate("indi_name"));
          println("-------  ----------------------------------------------");

            int grandtotal=0;
            int loners=0;
            for (int i=0; i<trees.size(); i++) {

              Tree tree = (Tree)trees.get(i);

              // sort group entities by birth date
              grandtotal += tree.size();
              if (tree.size()<minGroupSize)
                loners +=tree.size();
              else if (tree.size()<maxGroupSize){
                  if (i != 0) println();
                  String prefix = ""+tree.size();
                  Iterator it = tree.iterator();
                  while (it.hasNext()){
                      Indi indi = (Indi)it.next();
                      println(align(prefix,7, Report.ALIGN_RIGHT)+"  "+indi.getId()+
                              " "+indi.getName()+
                              " "+"("+indi.getBirthAsString()+ " - "+
                              indi.getDeathAsString()+")" );
                      prefix = "";
                  }
              }
              else {
                println(align(""+tree.size(),7, Report.ALIGN_RIGHT)+"  "+tree );
              }
            }

            println("");
            println(translate("grandtotal",grandtotal));

            if (loners>0) {
                Object[] msgargs = {new Integer(loners), new Integer(minGroupSize)};
                println("\n"+translate("loners",msgargs));
            }

        }
        println("");
        println(translate("endreport"));

        // Done
        return;
    }

    /**
     * Iterate over an individual who's part of a sub-tree
     */
    private void iterate(Indi indi, Tree tree, Set unvisited) {

      // individuals we need to check
      Stack todos  = new Stack();
      if (unvisited.remove(indi))
          todos.add(indi);

      // loop
      while (!todos.isEmpty()) {

        Indi todo = (Indi)todos.pop();

        // belongs to group
        tree.add(todo);

        // check the ancestors
        Fam famc = todo.getFamilyWhereBiologicalChild();
        if (famc!=null)  {
          Indi mother = famc.getWife();
          if (mother!=null&&unvisited.remove(mother))
            todos.push(mother);

          Indi father = famc.getHusband();
          if (father!=null&&unvisited.remove(father))
            todos.push(father);
        }

        // check descendants
        Fam[] fams = todo.getFamiliesWhereSpouse();
        for (int f=0;f<fams.length;f++) {

            // Get the family & process the spouse
            Fam fam = fams[f];
            Indi spouse = fam.getOtherSpouse(todo);
            if (spouse!=null&&unvisited.remove(spouse))
              todos.push(spouse);

            // .. and all the kids
            Indi[] children = fam.getChildren();
            for (int c = 0; c < children.length; c++) {
              if (unvisited.remove(children[c]))
                todos.push(children[c]);
            }

            // next family
        }

        // continue with to-dos
      }

      // done
    }

    /**
     * A sub-tree of people related to each other
     */
    private class Tree extends HashSet implements Comparable {

      private Indi oldestIndividual;

      public int compareTo(Object that) {
        return ((Tree)that).size()-((Tree)this).size();
      }

      public String toString() {
        return oldestIndividual.getId()+
        " "+oldestIndividual.getName()+
        "("+oldestIndividual.getBirthAsString()+ "-"+
        oldestIndividual.getDeathAsString()+")";
      }

      public boolean add(Object o) {
        // Individuals expected
        Indi indi = (Indi)o;
        // check if oldest
        if (isOldest(indi))
          oldestIndividual = indi;
        // continue
        return super.add(o);
      }

      private boolean isOldest(Indi indi) {
        long jd;
        try {
          jd = oldestIndividual.getBirthDate().getStart().getJulianDay();
        } catch (Throwable t) {
          return true;
        }
        try {
          return indi.getBirthDate().getStart().getJulianDay() < jd;
        } catch (Throwable t) {
          return false;
        }

      }

    } //Tree

} //ReportTrees
