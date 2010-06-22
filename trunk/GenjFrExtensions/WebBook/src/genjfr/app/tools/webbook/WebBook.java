/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import genjfr.app.tools.webbook.creator.WebHome;
import genj.gedcom.Gedcom;
import genjfr.app.tools.webbook.creator.WebHelper;
import genjfr.app.tools.webbook.creator.WebIndividuals;
import genjfr.app.tools.webbook.creator.WebLastnames;
import genjfr.app.tools.webbook.creator.WebTheme;

/**
 * Ancestris WebBook
 * @author frederic
 * Initial WebBook : 2006
 * NetBeans version : June 2010
 *
 * Logic structure:
 * ================
 * - Start writing to log
 *
 * - Load all parameters coming from the webbook wizard
 *   (in order to play with a consistent set of parameters because all
 *   controls are performed in wizard, not here anymore)
 *   Log them along the way.
 *
 * - Initiate webbook helper (contains common tools)
 *
 * - Create webbook structure (index, html/php, header with keywords, style, footer, etc.)
 *      - Write sections names in index
 *
 * - Create each section in its directory structure
 *
 * - Upload to server
 *
 * - Run final local user shell
 *
 * - Stop writing to log
 *
 *
 *
 * Logic if need to add a section in the book
 * ==========================================
 * - add definition option and websection
 * - create new section in main logic
 * - export link in index page
 * - add creation of section in initSections
 * - add retrieval of name in getSection
 *
 */
public class WebBook {

    private Gedcom gedcom;
    public  Log log;
    public WebBookParams wp;
    private WebHelper wh;
    //
    // WebBook sections definition
    //
    public WebTheme sectionTheme;
    public WebHome sectionHome;
    public WebLastnames sectionLastnames;
    public WebIndividuals sectionIndividuals;
    //

    /**
     * Constructor
     * @param gedcom
     * @param log
     * @throws InterruptedException
     */
    public WebBook(Gedcom gedcom, Log log) throws InterruptedException {
        this.gedcom = gedcom;
        this.log = log;
        wp = new WebBookParams(gedcom.getName());
        wh = new WebHelper(gedcom, log, wp);
        sectionTheme = new WebTheme(true, this, wp, wh);
        sectionHome = new WebHome(true, this, wp, wh);
        sectionLastnames = new WebLastnames(true, this, wp, wh);
        sectionIndividuals = new WebIndividuals(true, this, wp, wh);
        run();
    }

    private void run() throws InterruptedException {
        /**
         * Start writing to log
         */
        WebBookPlugin pi = new WebBookPlugin();
        log.write("----------- " + log.trs("CTL_WebBookTitle") + " -----------");
        log.write(pi.getPluginDisplayName() + " - " + pi.getPluginVersion());
        log.write("----------- " + log.trs("EXEC_start") + " -----------");
        log.timeStamp();
        log.write(" ");

        
        /**
         * Write parameters values in the log.
         */
        log.write(" ");
        log.write("----------- " + log.trs("EXEC_params") + " -----------");
        wp.logParameters(log);




        /**
         * Initiate all sections together as they use eachother
         */
        sectionLastnames.init();
        sectionIndividuals.init();
        //...etc...



        /**
         * Create site structure
         * - theme : style sheet and images
         * - includes : PHP code in the case of PHP support
         */
        sectionTheme.create();
        //wh.createIncludes();




        /**
         * Create sections in their directory structure
         */
        sectionLastnames.create();
        sectionIndividuals.create();
        //...etc...

        /**
         * Create home page
         */
        sectionHome.create();



        /**
         * Create home page
         */
        //uploadPages();



        /**
         * Run final user exec
         */
        //runUserShell();



        /**
         * Stop writing to log
         */
        log.write(" ");
        log.timeStamp();
        log.write("----------- " + log.trs("EXEC_end") + " -----------");

    }

}
