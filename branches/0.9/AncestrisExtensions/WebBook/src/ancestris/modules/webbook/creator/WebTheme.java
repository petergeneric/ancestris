/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import java.io.File;

/**
 *
 * @author frederic
 */
public class WebTheme extends WebSection {

    /**
     * Constructor
     */
    public WebTheme(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        // create directory
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + themeDir, true);

        // copy icons and style sheet
        createFiles(dir);

        // done
        wh.log.write(themeDir + trs("EXEC_DONE"));
    }

    /**
     * Create icons and other files
     */
    public void createFiles(File dir) {

        String imagesDir = "ancestris/modules/webbook/img/";
        String toFile = dir.getAbsolutePath() + File.separator;
        String fromDir = "img/";
        wh.copyFiles(imagesDir, toFile, fromDir);
    }
}
