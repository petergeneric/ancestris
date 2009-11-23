/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package webbook;

import java.io.File;



/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebSection {

  private final static String SEP = "/";

  public  String sectionName   = "";   // e.g. "Individuals of my genealogy"
  public  String sectionDir    = "";   // e.g. individuals
  public  String sectionPrefix = "";   // e.g. "persons_"
  public  String formatNbrs    = "";   // e.g. "%03d"
  public  String sectionSuffix = "";   // e.g. ".html"
  public  int    nbPerPage     = 0;    // e.g. 50
  public  String sectionLink   = "";   // e.g. individuals/persons_001.html

  /**
   * Constructor
   */
  public WebSection(String sectionName, String sectionDir, String sectionPrefix, String formatNbrs, String sectionSuffix, int firstPage, int nbPerPage) {

     this.sectionName = sectionName;
     this.sectionDir = sectionDir;
     this.sectionPrefix = sectionPrefix;
     this.formatNbrs = formatNbrs;
     this.sectionSuffix = sectionSuffix;
     this.nbPerPage = nbPerPage;
     this.sectionLink = sectionDir + SEP + sectionPrefix + 
                        ( (formatNbrs.length() == 0) ? "" : String.format(formatNbrs, firstPage) ) + sectionSuffix;
     }


} // End_of_Class



