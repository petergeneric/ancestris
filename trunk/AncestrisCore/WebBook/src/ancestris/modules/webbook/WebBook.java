/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import ancestris.modules.webbook.creator.WebIncludes;
import ancestris.modules.webbook.creator.WebHome;
import genj.gedcom.Gedcom;
import ancestris.modules.webbook.creator.WebCities;
import ancestris.modules.webbook.creator.WebCitiesDetails;
import ancestris.modules.webbook.creator.WebDays;
import ancestris.modules.webbook.creator.WebDaysDetails;
import ancestris.modules.webbook.creator.WebHelper;
import ancestris.modules.webbook.creator.WebIndividuals;
import ancestris.modules.webbook.creator.WebIndividualsDetails;
import ancestris.modules.webbook.creator.WebLastnames;
import ancestris.modules.webbook.creator.WebMap;
import ancestris.modules.webbook.creator.WebMedia;
import ancestris.modules.webbook.creator.WebRepSosa;
import ancestris.modules.webbook.creator.WebSearch;
import ancestris.modules.webbook.creator.WebSources;
import ancestris.modules.webbook.creator.WebStatsFrequent;
import ancestris.modules.webbook.creator.WebStatsImplex;
import ancestris.modules.webbook.creator.WebTheme;
import ancestris.modules.webbook.transfer.FTPRegister;
import ancestris.modules.webbook.transfer.FTPLoader;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;

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
    private genj.util.Registry registry;
    public WebBookParams wp;
    private WebHelper wh;
    //
    // WebBook sections definition
    //
    public WebIncludes sectionIncludes;
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
        registry = gedcom.getRegistry();
        this.log = log;
        wp = new WebBookParams(gedcom);
        wh = new WebHelper(gedcom, log, wp);
        // Opens up the register that stores which files have been changed locally and uploaded
        uploadRegister = new FTPRegister(wp, wh);
        // Now initialises all sections
        sectionIncludes = new WebIncludes(true, this, wp, wh);
        sectionTheme = new WebTheme(true, this, wp, wh);
        sectionHome = new WebHome(true, this, wp, wh);
        sectionLastnames = new WebLastnames(true, this, wp, wh);
        sectionIndividuals = new WebIndividuals(true, this, wp, wh);
        sectionIndividualsDetails = new WebIndividualsDetails(true, this, wp, wh);
        sectionSources = new WebSources(wp.param_media_GeneSources.equals("1"), this, wp, wh);
        sectionMedia = new WebMedia(wp.param_media_GeneMedia.equals("1"), this, wp, wh);
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
        String version = pi.getPluginVersion();
        log.write("----------- " + log.trs("CTL_WebBookTitle") + " -----------");
        log.write(pi.getPluginDisplayName() + " - " + version);
        sectionHome.setVersion(version);
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
         * Empties local directory of all previous generations
         */
        log.write(" ");
        log.write("----------- " + log.trs("EXEC_clean") + " -----------");
        wh.cleanLocalDir();



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
        sectionIncludes.create();



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
        if (wp.param_FTP_upload.equals("1")) {
            log.write(log.trs("upload_yes"));
            new FTPLoader(wp, wh, uploadRegister).run();
        } else {
            log.write(log.trs("upload_none"));
        }


        /**
         * Launch browser if generated in html
         * - local address if not migrated to Internet
         * - http site if generated remotely
         */
        log.write(" ");
        log.write("----------- " + log.trs("EXEC_showPages") + " -----------");
        String fileStr = "";
        // if upload
        if (wp.param_FTP_upload.equals("1")) {
            int i = wp.param_FTP_site.indexOf("ftp");
            String siteStr = i == -1 ? wp.param_FTP_site : wp.param_FTP_site.substring(3);
            fileStr = "https://www" + siteStr;
        } else if (!wp.param_PHP_Support.equals("1")) {
            fileStr = "file://" + wp.param_localWebDir + File.separator + "index.html";
        }
        try {
            URI uri = new URI(fileStr);
            if (Desktop.isDesktopSupported()) {
                log.write(uri.toString());
                registry.put("localwebsite", uri.toString());
                Desktop.getDesktop().browse(uri);
            } else {
            }
        } catch (Exception ex) {
        }

        
        /**
         * Stop writing to log
         */
        log.write(" ");
        log.timeStamp();
        log.write("----------- " + log.trs("EXEC_end") + " -----------");

        
    }
}
