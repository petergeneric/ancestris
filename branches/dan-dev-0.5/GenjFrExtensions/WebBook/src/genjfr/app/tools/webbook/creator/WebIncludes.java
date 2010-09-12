/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook.creator;

import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import java.io.File;
import java.io.PrintWriter;

/**
 *
 * @author frederic
 */
public class WebIncludes extends WebSection {

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

        // empty directory
        wh.emptyDir(dir, false);

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

        //
        filename = includeInit;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        if (!wp.param_PHP_Init.isEmpty()) {
            out.println("include(\"" + wp.param_PHP_Init + "\");");
        }
        out.println("include(\"" + wp.param_PHP_MyScript + "\");");
        out.println("?>");
        out.close();
        //
        filename = includeHeaderStart;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        out.println("include(\"" + wp.param_PHP_HeadStart + "\");");
        out.println("?>");
        out.close();
        //
        filename = includeHeaderEnd;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        out.println("include(\"" + wp.param_PHP_HeadEnd + "\");");
        out.println("?>");
        out.close();
        //
        filename = includeFooter;
        file = wh.getFileForName(dir, filename);
        out = wh.getWriter(file, UTF8);
        out.println("<?php");
        out.println("include(\"" + wp.param_PHP_Footer + "\");");
        out.println("?>");
        out.close();

    }
}
