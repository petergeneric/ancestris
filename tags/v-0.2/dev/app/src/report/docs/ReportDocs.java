/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package docs;

import genj.gedcom.*;
import genj.report.*;

import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.window.GenjFrWindowManager;
import genj.window.WindowManager;


/**
 * GenJ - Report
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 *
 * Ajouter de lancer le rapport à partir d'un individu ou d'une famille
 *
 *
 */
public class ReportDocs extends Report {

  private String REG = "window.docs";
  private String TITLE = translate("name");
  public final static ImageIcon IMG_SOURCE = Grammar.V55.getMeta(new TagPath("SOUR")).getImage("Source");


  /**
   * Entry points
   */
  public void start(Gedcom gedcom) {
     start(gedcom, null);
     }

  public void start(Indi indi) {
     start(indi.getGedcom(), indi);
     }

  public void start(Fam fam) {
     start(fam.getGedcom(), fam);
     }

  public void start(Source source) {
     start(source.getGedcom(), source);
     }

  public void start(Repository repo) {
     start(repo.getGedcom(), repo);
     }

  /**
   * Our main logic
   */
  public void start(Gedcom gedcom, Entity entity) {

    println(translate("Launching"));

    // create window
    Registry genJregistry = new Registry("genj");
    final WindowManager winMgr = new GenjFrWindowManager(new Registry(genJregistry, "window"), null);
    final Registry registry = new Registry(genJregistry, REG);

    // setup panel
    final EditDocsPanel view = new EditDocsPanel(this, gedcom, entity, registry, winMgr);
    final Gedcom gedcomFinal = gedcom;

    // prepare close action
    Action2 close = new Action2() {
      protected void execute() {
        HelperDocs.processClose(gedcomFinal, view, registry, winMgr);
      }
    };

    // show it
    winMgr.openWindow(REG, TITLE, IMG_SOURCE, view, null, close);

    println(translate("Finishing"));


  } // end_of_start


} // End_of_Report
