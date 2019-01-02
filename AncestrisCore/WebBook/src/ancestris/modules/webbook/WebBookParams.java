/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import genj.gedcom.Gedcom;
import genj.util.Registry;

/**
 *
 * @author frederic
 */
public class WebBookParams {

    public static final String WB_PREFIX = "webbook";

    //
    // WebBook wizard panels parameters
    // panel 1
    public String param_title;
    public String param_author;
    public String param_address;
    public String param_phone;
    public String param_email;
    public String param_dispMsg;
    public String param_dispStatAncestor;
    public String param_dispStatLoc;
    public String param_message;
    // panel 2
    public String param_decujus;
    public String param_unknown;
    public String param_dispSpouse;
    public String param_dispKids;
    public String param_dispSiblings;
    public String param_dispRelations;
    public String param_dispNotes;
    public String param_dispId;
    public String param_dispEmailButton;
    public String param_hidePrivateData;
    // panel 3
    public String param_media_GeneSources;
    public String param_media_DisplaySources;
    public String param_media_CopySources;
    public String param_media_GeneMedia;
    public String param_media_CopyMedia;
    public String param_media_GeneMap;
    public String param_media_DispUnknownLoc;
    // panel 4
    public String param_dispAncestors;
    public String param_ancestorMinGen;
    public String param_ancestorMaxGen;
    public String param_ancestorSource;
    // panel 5
    public String param_localWebDir;
    public String param_logFile;
    // panel 6
    public String param_FTP_upload;
    public String param_FTP_site;
    public String param_FTP_dir;
    public String param_FTP_user;
    public String param_FTP_password;
    public String param_FTP_siteDesc;
    public String param_FTP_transfertType;
    public String param_FTP_resetHistory;
    public String param_FTP_exec;
    public String param_FTP_log;
    // panel 7
    public String param_PHP_Support;
    public String param_PHP_Profil;
    public String param_PHP_Code;
    public String param_PHP_Integrate;
    public String param_PHP_Test;
    public String param_PHP_Init;
    public String param_PHP_MyScript;
    public String param_PHP_HeadStart;
    public String param_PHP_HeadCSS;
    public String param_PHP_HeadEnd;
    public String param_PHP_Footer;

    /**
     * Constructor
     * @param gedName
     */
    public WebBookParams(Gedcom gedcom) {
        Registry gedcomSettings = gedcom.getRegistry();
        // panel 1
        param_title = gedcomSettings.get(WB_PREFIX + ".title", "");
        param_author = gedcomSettings.get(WB_PREFIX + ".author", "");
        param_address = gedcomSettings.get(WB_PREFIX + ".address", "");
        param_phone = gedcomSettings.get(WB_PREFIX + ".phone", "");
        param_email = gedcomSettings.get(WB_PREFIX + ".email", "");
        param_dispMsg = gedcomSettings.get(WB_PREFIX + ".dispMsg", "");
        param_dispStatAncestor = gedcomSettings.get(WB_PREFIX + ".dispStatAncestor", "");
        param_dispStatLoc = gedcomSettings.get(WB_PREFIX + ".dispStatLoc", "");
        param_message = gedcomSettings.get(WB_PREFIX + ".message", "");

        // panel 2
        param_decujus = gedcomSettings.get(WB_PREFIX + ".decujus", "");
        param_unknown = gedcomSettings.get(WB_PREFIX + ".unknown", "");
        param_dispSpouse = gedcomSettings.get(WB_PREFIX + ".dispSpouse", "");
        param_dispKids = gedcomSettings.get(WB_PREFIX + ".dispKids", "");
        param_dispSiblings = gedcomSettings.get(WB_PREFIX + ".dispSiblings", "");
        param_dispRelations = gedcomSettings.get(WB_PREFIX + ".dispRelations", "");
        param_dispNotes = gedcomSettings.get(WB_PREFIX + ".dispNotes", "");
        param_dispId = gedcomSettings.get(WB_PREFIX + ".dispId", "");
        param_dispEmailButton = gedcomSettings.get(WB_PREFIX + ".dispEmailButton", "");
        param_hidePrivateData = gedcomSettings.get(WB_PREFIX + ".hidePrivateData", "");

        // panel 3
        param_media_GeneSources = gedcomSettings.get(WB_PREFIX + ".media_GeneSources", "");
        param_media_DisplaySources = gedcomSettings.get(WB_PREFIX + ".media_DisplaySources", "");
        param_media_CopySources = gedcomSettings.get(WB_PREFIX + ".media_CopySources", "");
        param_media_GeneMedia = gedcomSettings.get(WB_PREFIX + ".media_GeneMedia", "");
        param_media_CopyMedia = gedcomSettings.get(WB_PREFIX + ".media_CopyMedia", "");
        param_media_GeneMap = gedcomSettings.get(WB_PREFIX + ".media_GeneMap", "");
        param_media_DispUnknownLoc = gedcomSettings.get(WB_PREFIX + ".media_DispUnknownLoc", "");

        // panel 4
        param_dispAncestors = gedcomSettings.get(WB_PREFIX + ".dispAncestors", "");
        param_ancestorMinGen = gedcomSettings.get(WB_PREFIX + ".ancestorMinGen", "");
        param_ancestorMaxGen = gedcomSettings.get(WB_PREFIX + ".ancestorMaxGen", "");
        param_ancestorSource = gedcomSettings.get(WB_PREFIX + ".ancestorSource", "");

        // panel 5
        param_localWebDir = gedcomSettings.get(WB_PREFIX + ".localWebDir", "");
        param_logFile = gedcomSettings.get(WB_PREFIX + ".logFile", "");

        // panel 6
        param_FTP_upload = gedcomSettings.get(WB_PREFIX + ".FTP_upload", "");
        param_FTP_site = gedcomSettings.get(WB_PREFIX + ".FTP_site", "");
        param_FTP_dir = gedcomSettings.get(WB_PREFIX + ".FTP_dir", "");
        param_FTP_user = gedcomSettings.get(WB_PREFIX + ".FTP_user", "");
        param_FTP_password = gedcomSettings.get(WB_PREFIX + ".FTP_password", "");
        param_FTP_siteDesc = gedcomSettings.get(WB_PREFIX + ".FTP_siteDesc", "");
        param_FTP_transfertType = gedcomSettings.get(WB_PREFIX + ".FTP_transfertType", "");
        param_FTP_resetHistory = gedcomSettings.get(WB_PREFIX + ".FTP_resetHistory", "");
        param_FTP_exec = gedcomSettings.get(WB_PREFIX + ".FTP_exec", "");
        param_FTP_log = gedcomSettings.get(WB_PREFIX + ".FTP_log", "");

        // panel 7
        param_PHP_Support = gedcomSettings.get(WB_PREFIX + ".PHP_Support", "");
        param_PHP_Profil = gedcomSettings.get(WB_PREFIX + ".PHP_Profil", "");
        param_PHP_Code = gedcomSettings.get(WB_PREFIX + ".PHP_Code", "");
        param_PHP_Integrate = gedcomSettings.get(WB_PREFIX + ".PHP_Integrate", "");
        param_PHP_Test = gedcomSettings.get(WB_PREFIX + ".PHP_Test", "");
        param_PHP_Init = gedcomSettings.get(WB_PREFIX + ".PHP_Init", "");
        param_PHP_MyScript = gedcomSettings.get(WB_PREFIX + ".PHP_MyScript", "");
        param_PHP_HeadStart = gedcomSettings.get(WB_PREFIX + ".PHP_HeadStart", "");
        param_PHP_HeadCSS = gedcomSettings.get(WB_PREFIX + ".PHP_HeadCSS", "");
        param_PHP_HeadEnd = gedcomSettings.get(WB_PREFIX + ".PHP_HeadEnd", "");
        param_PHP_Footer = gedcomSettings.get(WB_PREFIX + ".PHP_Footer", "");
    }

    void logParameters(Log log) {
        // panel 1
        log.write("param_title = " + param_title);
        log.write("param_author = " + param_author);
        log.write("param_address = " + param_address);
        log.write("param_phone = " + param_phone);
        log.write("param_email = " + param_email);
        log.write("param_dispMsg = " + param_dispMsg);
        if (!param_dispMsg.equals("0")) {
            log.write("param_dispStatAncestor = " + param_dispStatAncestor);
            log.write("param_dispStatLoc = " + param_dispStatLoc);
            log.write("param_message = " + param_message);
        }
        // panel 2
        log.write("param_decujus = " + param_decujus);
        log.write("param_unknown = " + param_unknown);
        log.write("param_dispSpouse = " + param_dispSpouse);
        log.write("param_dispKids = " + param_dispKids);
        log.write("param_dispSiblings = " + param_dispSiblings);
        log.write("param_dispRelations = " + param_dispRelations);
        log.write("param_dispNotes = " + param_dispNotes);
        log.write("param_dispId = " + param_dispId);
        log.write("param_dispEmailButton = " + param_dispEmailButton);
        log.write("param_hidePrivateData = " + param_hidePrivateData);
        // panel 3
        log.write("param_media_GeneSources = " + param_media_GeneSources);
        log.write("param_media_DisplaySources = " + param_media_DisplaySources);
        log.write("param_media_CopySources = " + param_media_CopySources);
        log.write("param_media_GeneMedia = " + param_media_GeneMedia);
        log.write("param_media_CopyMedia = " + param_media_CopyMedia);
        log.write("param_media_GeneMap = " + param_media_GeneMap);
        log.write("param_media_DispUnknownLoc = " + param_media_DispUnknownLoc);
        // panel 4
        log.write("param_dispAncestors = " + param_dispAncestors);
        log.write("param_ancestorMinGen = " + param_ancestorMinGen);
        log.write("param_ancestorMaxGen = " + param_ancestorMaxGen);
        log.write("param_ancestorSource = " + param_ancestorSource);
        // panel 5
        log.write("param_localWebDir = " + param_localWebDir);
        log.write("param_logFile = " + param_logFile);
        // panel 6
        log.write("param_FTP_upload = " + param_FTP_upload);
        if (!param_FTP_upload.equals("0")) {
            log.write("param_FTP_site = " + param_FTP_site);
            log.write("param_FTP_dir = " + param_FTP_dir);
            log.write("param_FTP_user = *******");
            log.write("param_FTP_password = *******");
            log.write("param_FTP_siteDesc = " + param_FTP_siteDesc);
            log.write("param_FTP_transfertType = " + param_FTP_transfertType);
            log.write("param_FTP_resetHistory = " + param_FTP_resetHistory);
            log.write("param_FTP_exec = " + param_FTP_exec);
            log.write("param_FTP_log = " + param_FTP_log);
        }
        // panel 7
        log.write("param_PHP_Support = " + param_PHP_Support);
        if (!param_PHP_Support.equals("0")) {
            log.write("param_PHP_Profil = " + param_PHP_Profil);
            log.write("param_PHP_Code = *******");
            log.write("param_PHP_Integrate = " + param_PHP_Integrate);
            log.write("param_PHP_Test = " + param_PHP_Test);
            log.write("param_PHP_Init = " + param_PHP_Init);
            log.write("param_PHP_MyScript = " + param_PHP_MyScript);
            log.write("param_PHP_HeadStart = " + param_PHP_HeadStart);
            log.write("param_PHP_HeadCSS = " + param_PHP_HeadCSS);
            log.write("param_PHP_HeadEnd = " + param_PHP_HeadEnd);
            log.write("param_PHP_Footer = " + param_PHP_Footer);
        }
        log.write("----------- ");
    }
}
