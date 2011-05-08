/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import org.openide.util.NbPreferences;

/**
 *
 * @author frederic
 */
public class WebBookParams {

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
    // panel 3
    public String param_media_GeneSources;
    public String param_media_DisplaySources;
    public String param_media_CopySources;
    public String param_media_GeneMedia;
    public String param_media_CopyMedia;
    public String param_media_GeneMap;
    public String param_media_DispUnknownLoc;
    public String param_media_GoogleKey;
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
    public WebBookParams(String gedName) {
        
        // panel 1
        param_title = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".title", "");
        param_author = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".author", "");
        param_address = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".address", "");
        param_phone = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".phone", "");
        param_email = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".email", "");
        param_dispMsg = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".dispMsg", "");
        param_dispStatAncestor = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".dispStatAncestor", "");
        param_dispStatLoc = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".dispStatLoc", "");
        param_message = NbPreferences.forModule(WebBookWizardPanel1.class).get(gedName + ".message", "");

        // panel 2
        param_decujus = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".decujus", "");
        param_unknown = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".unknown", "");
        param_dispSpouse = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".dispSpouse", "");
        param_dispKids = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".dispKids", "");
        param_dispSiblings = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".dispSiblings", "");
        param_dispRelations = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".dispRelations", "");
        param_dispNotes = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".dispNotes", "");
        param_dispId = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".dispId", "");
        param_dispEmailButton = NbPreferences.forModule(WebBookWizardPanel2.class).get(gedName + ".dispEmailButton", "");

        // panel 3
        param_media_GeneSources = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_GeneSources", "");
        param_media_DisplaySources = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_DisplaySources", "");
        param_media_CopySources = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_CopySources", "");
        param_media_GeneMedia = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_GeneMedia", "");
        param_media_CopyMedia = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_CopyMedia", "");
        param_media_GeneMap = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_GeneMap", "");
        param_media_DispUnknownLoc = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_DispUnknownLoc", "");
        param_media_GoogleKey = NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName + ".media_GoogleKey", "");

        // panel 4
        param_dispAncestors = NbPreferences.forModule(WebBookWizardPanel4.class).get(gedName + ".dispAncestors", "");
        param_ancestorMinGen = NbPreferences.forModule(WebBookWizardPanel4.class).get(gedName + ".ancestorMinGen", "");
        param_ancestorMaxGen = NbPreferences.forModule(WebBookWizardPanel4.class).get(gedName + ".ancestorMaxGen", "");
        param_ancestorSource = NbPreferences.forModule(WebBookWizardPanel4.class).get(gedName + ".ancestorSource", "");

        // panel 5
        param_localWebDir = NbPreferences.forModule(WebBookWizardPanel5.class).get(gedName + ".localWebDir", "");
        param_logFile = NbPreferences.forModule(WebBookWizardPanel5.class).get(gedName + ".logFile", "");

        // panel 6
        param_FTP_upload = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_upload", "");
        param_FTP_site = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_site", "");
        param_FTP_dir = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_dir", "");
        param_FTP_user = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_user", "");
        param_FTP_password = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_password", "");
        param_FTP_siteDesc = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_siteDesc", "");
        param_FTP_transfertType = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_transfertType", "");
        param_FTP_resetHistory = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_resetHistory", "");
        param_FTP_exec = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_exec", "");
        param_FTP_log = NbPreferences.forModule(WebBookWizardPanel6.class).get(gedName + ".FTP_log", "");

        // panel 7
        param_PHP_Support = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Support", "");
        param_PHP_Profil = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Profil", "");
        param_PHP_Code = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Code", "");
        param_PHP_Integrate = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Integrate", "");
        param_PHP_Test = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Test", "");
        param_PHP_Init = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Init", "");
        param_PHP_MyScript = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_MyScript", "");
        param_PHP_HeadStart = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_HeadStart", "");
        param_PHP_HeadCSS = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_HeadCSS", "");
        param_PHP_HeadEnd = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_HeadEnd", "");
        param_PHP_Footer = NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Footer", "");
    }

    void logParameters(Log log) {
        // panel 1
        log.write("param_title = " + param_title);
        log.write("param_author = " + param_author);
        log.write("param_address = " + param_address);
        log.write("param_phone = " + param_phone);
        log.write("param_email = " + param_email);
        log.write("param_dispMsg = " + param_dispMsg);
        log.write("param_dispStatAncestor = " + param_dispStatAncestor);
        log.write("param_dispStatLoc = " + param_dispStatLoc);
        log.write("param_message = " + param_message);
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
        // panel 3
        log.write("param_media_GeneSources = " + param_media_GeneSources);
        log.write("param_media_DisplaySources = " + param_media_DisplaySources);
        log.write("param_media_CopySources = " + param_media_CopySources);
        log.write("param_media_GeneMedia = " + param_media_GeneMedia);
        log.write("param_media_CopyMedia = " + param_media_CopyMedia);
        log.write("param_media_GeneMap = " + param_media_GeneMap);
        log.write("param_media_DispUnknownLoc = " + param_media_DispUnknownLoc);
        log.write("param_media_GoogleKey = " + param_media_GoogleKey);
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
        log.write("param_FTP_site = " + param_FTP_site);
        log.write("param_FTP_dir = " + param_FTP_dir);
        log.write("param_FTP_user = *******");
        log.write("param_FTP_password = *******");
        log.write("param_FTP_siteDesc = " + param_FTP_siteDesc);
        log.write("param_FTP_transfertType = " + param_FTP_transfertType);
        log.write("param_FTP_resetHistory = " + param_FTP_resetHistory);
        log.write("param_FTP_exec = " + param_FTP_exec);
        log.write("param_FTP_log = " + param_FTP_log);
        // panel 7
        log.write("param_PHP_Support = " + param_PHP_Support);
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
        log.write("----------- ");
    }
}
