/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import ancestris.modules.webbook.WebBookWizardAction;
import java.io.File;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class WebIncludes extends WebSection {

    private String fileVariables = "init_variables.php";

    /**
     * Constructor
     */
    public WebIncludes(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        // create directory
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + includesDir, true);

        // copy icons and style sheet
        createFiles(dir);

        // done
        wh.log.write(includesDir + trs("EXEC_DONE"));
    }

    /**
     * Create icons and other files
     */
    public void createFiles(File dir) {
        // return if php not to be generated
        if (!wp.param_PHP_Support.equals("1")) {
            return;
        }
        String filename = "";
        File file = null;
        PrintWriter out = null;
        String PHPInit = NbBundle.getMessage(WebBookWizardAction.class, "PREF_PHPInit").substring(3);
        String PHPMyScript = NbBundle.getMessage(WebBookWizardAction.class, "PREF_PHPMyScript").substring(3);
        String PHPHeadStart = NbBundle.getMessage(WebBookWizardAction.class, "PREF_PHPHeadStart").substring(3);
        String PHPHeadEnd = NbBundle.getMessage(WebBookWizardAction.class, "PREF_PHPHeadEnd").substring(3);
        String PHPFooter = NbBundle.getMessage(WebBookWizardAction.class, "PREF_PHPFooter").substring(3);
        //
        if (wp.param_PHP_Integrate.equals("1")) {
            PHPInit = wp.param_PHP_Init;
            PHPMyScript = wp.param_PHP_MyScript;
            PHPHeadStart = wp.param_PHP_HeadStart;
            PHPHeadEnd = wp.param_PHP_HeadEnd;
            PHPFooter = wp.param_PHP_Footer;
        }

        //
        filename = includeInit;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        out.println("include(\"" + PHPInit + "\");");
        out.println("include(\"" + PHPMyScript + "\");");
        out.println("?>");
        out.close();
        //
        filename = includeHeaderStart;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        out.println("include(\"" + PHPHeadStart + "\");");
        out.println("?>");
        out.close();
        //
        filename = includeHeaderEnd;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        out.println("include(\"" + PHPHeadEnd + "\");");
        out.println("?>");
        out.close();
        //
        filename = includeFooter;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        out.println("include(\"" + PHPFooter + "\");");
        out.println("?>");
        out.close();

        if (!wp.param_PHP_Integrate.equals("1")) {
            //
            filename = fileVariables;
            file = wh.getFileForName(dir, filename);
            out = wh.getWriter(file, UTF8);
            out.println("<?php");
            out.println("// table of profiles and codes");
            out.println("$ident[\"" + wp.param_PHP_Profil + "\"] = \"" + wp.param_PHP_Code + "\";");
            out.println("$authgen[\"" + wp.param_PHP_Profil + "\"] = true;");
            out.println("?>");
            out.close();
            createStructureFiles(dir);
        }
    }

    private void createStructureFiles(File dir) {
        String imagesDir = "ancestris/modules/webbook/includes/";
        String toFile = dir.getAbsolutePath() + File.separator;
        String toDir = "includes/";
        wh.copyFiles(imagesDir, toFile, toDir);
    }
}
