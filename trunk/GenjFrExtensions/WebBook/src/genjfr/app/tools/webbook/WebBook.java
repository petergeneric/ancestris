/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import genjfr.app.tools.webbook.creator.WebHome;
import genj.gedcom.Gedcom;
import genjfr.app.tools.webbook.creator.WebCities;
import genjfr.app.tools.webbook.creator.WebCitiesDetails;
import genjfr.app.tools.webbook.creator.WebDays;
import genjfr.app.tools.webbook.creator.WebDaysDetails;
import genjfr.app.tools.webbook.creator.WebHelper;
import genjfr.app.tools.webbook.creator.WebIndividuals;
import genjfr.app.tools.webbook.creator.WebIndividualsDetails;
import genjfr.app.tools.webbook.creator.WebLastnames;
import genjfr.app.tools.webbook.creator.WebMap;
import genjfr.app.tools.webbook.creator.WebMedia;
import genjfr.app.tools.webbook.creator.WebRepSosa;
import genjfr.app.tools.webbook.creator.WebSearch;
import genjfr.app.tools.webbook.creator.WebSources;
import genjfr.app.tools.webbook.creator.WebStatsFrequent;
import genjfr.app.tools.webbook.creator.WebStatsImplex;
import genjfr.app.tools.webbook.creator.WebTheme;
import genjfr.app.tools.webbook.transfer.FTPRegister;
import genjfr.app.tools.webbook.transfer.FTPLoader;
import java.io.IOException;

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
    public Log log;
    public WebBookParams wp;
    private WebHelper wh;
    //
    // WebBook sections definition
    //
    public WebTheme sectionTheme;
    public WebHome sectionHome;
    public WebLastnames sectionLastnames;
    public WebIndividuals sectionIndividuals;
    public WebIndividualsDetails sectionIndividualsDetails;
    public WebSources sectionSources;
    public WebMedia sectionMedia;
    public WebCities sectionCities;
    public WebCitiesDetails sectionCitiesDetails;
    public WebDays sectionDays;
    public WebDaysDetails sectionDaysDetails;
    public WebStatsFrequent sectionStatsFrequent;
    public WebStatsImplex sectionStatsImplex;
    public WebRepSosa sectionRepSosa;
    public WebSearch sectionSearch;
    public WebMap sectionMap;
    //
    private FTPRegister uploadRegister;
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
        // Opens up the register that stores which files have been changed locally and uploaded
        uploadRegister = new FTPRegister(wp, wh);
        // Now initialises all sections
        sectionTheme = new WebTheme(true, this, wp, wh);
        sectionHome = new WebHome(true, this, wp, wh);
        sectionLastnames = new WebLastnames(true, this, wp, wh);
        sectionIndividuals = new WebIndividuals(true, this, wp, wh);
        sectionIndividualsDetails = new WebIndividualsDetails(true, this, wp, wh);
        sectionSources = new WebSources(true, this, wp, wh);
        sectionMedia = new WebMedia(true, this, wp, wh);
        sectionCities = new WebCities(true, this, wp, wh);
        sectionCitiesDetails = new WebCitiesDetails(true, this, wp, wh);
        sectionDays = new WebDays(true, this, wp, wh);
        sectionDaysDetails = new WebDaysDetails(true, this, wp, wh);
        sectionStatsImplex = new WebStatsImplex(true, this, wp, wh);
        sectionStatsFrequent = new WebStatsFrequent(true, this, wp, wh);
        sectionRepSosa = new WebRepSosa(true, this, wp, wh);
        sectionSearch = new WebSearch(true, this, wp, wh);
        sectionMap = new WebMap(true, this, wp, wh);
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
        log.write(" ");
        log.write("----------- " + log.trs("EXEC_init") + " -----------");
        sectionLastnames.init();
        sectionIndividuals.init();
        sectionIndividualsDetails.init();
        sectionSources.init();
        sectionMedia.init();
        sectionCities.init();
        sectionCitiesDetails.init();
        sectionDays.init();
        sectionDaysDetails.init();
        sectionStatsImplex.init();
        sectionStatsFrequent.init();
        sectionRepSosa.init();
        sectionSearch.init();
        sectionMap.init();
        //
        sectionHome.init();


        /**
         * Create site structure
         * - theme : style sheet and images
         * - includes : PHP code in the case of PHP support
         */
        log.write(" ");
        log.write("----------- " + log.trs("EXEC_create") + " -----------");
        sectionTheme.create();
        //wh.createIncludes();




        /**
         * Create sections in their directory structure
         */
        sectionLastnames.create();
        sectionIndividuals.create();
        sectionIndividualsDetails.create();
        sectionSources.create();
        sectionMedia.create();
        sectionCities.create();
        sectionCitiesDetails.create();
        sectionDays.create();
        sectionDaysDetails.create();
        sectionStatsFrequent.create();
        sectionStatsImplex.create();
        sectionRepSosa.create();
        sectionSearch.create();
        sectionMap.create();

        /**
         * Create home page
         */
        sectionHome.create();



        /**
         * Update web site pages
         */
        log.write(" ");
        log.write("----------- " + log.trs("EXEC_upload") + " -----------");
        uploadPages();



        /**
         * Run final user exec
         */
        log.write(" ");
        log.write("----------- " + log.trs("EXEC_runsShell") + " -----------");
        runUserShell();


        /**
         * Stop writing to log
         */
        log.write(" ");
        log.timeStamp();
        log.write("----------- " + log.trs("EXEC_end") + " -----------");

    }

    private void uploadPages() {
        if (wp.param_FTP_upload.equals("1")) {
            new FTPLoader(wp, wh, uploadRegister).run();
        }
    }

    private void runUserShell() {
        String shell = wp.param_FTP_exec;
        if (!shell.isEmpty()) {
            try {
                log.write(log.trs("shell_launch", shell));
                Runtime.getRuntime().exec(shell);
                log.write(log.trs("shell_cannotwait"));
            } catch (IOException e) {
                log.write(log.trs("error_shell", new String[]{shell, e.getMessage()}));
            }
        }

    }
}
