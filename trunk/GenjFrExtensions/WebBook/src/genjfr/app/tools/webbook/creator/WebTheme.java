/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook.creator;

import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import java.io.File;
import java.io.IOException;

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

        // create directory
        wh.emptyDir(dir, false);

        // copy icons and style sheet
        createFiles(dir);

        // done
        wh.log.write(themeDir + trs("EXEC_DONE"));
    }



   /**
     * Helper - Create icons
     */
    public void createFiles(File dir) {

        // Get male and female icons in the dierctory
        String genjImagesDir = "img/";
        String toFile = dir.getAbsolutePath() + File.separator;

        try {
            wh.copy(genjImagesDir + styleFile, toFile + styleFile);
            wh.copy(genjImagesDir + "m.gif", toFile + "m.gif");
            wh.copy(genjImagesDir + "f.gif", toFile +"f.gif");
            wh.copy(genjImagesDir + "u.gif", toFile +"u.gif");
            wh.copy(genjImagesDir + "s.gif", toFile +"s.gif");
            wh.copy(genjImagesDir + "p.gif", toFile +"p.gif");
            wh.copy(genjImagesDir + "t.gif", toFile +"t.gif");
            wh.copy(genjImagesDir + "h.gif", toFile +"h.gif");
            wh.copy(genjImagesDir + "b.gif", toFile +"b.gif");
            wh.copy(genjImagesDir + "n.gif", toFile +"n.gif");
            wh.copy(genjImagesDir + "e.gif", toFile +"e.gif");
            wh.copy(genjImagesDir + "src.gif", toFile +"src.gif");
            wh.copy(genjImagesDir + "downbar.png", toFile +"downbar.png");
            wh.copy(genjImagesDir + "downleft.png", toFile +"downleft.png");
            wh.copy(genjImagesDir + "downright.png", toFile +"downright.png");
            wh.copy(genjImagesDir + "leftbar.png", toFile +"leftbar.png");
            wh.copy(genjImagesDir + "rightbar.png", toFile +"rightbar.png");
            wh.copy(genjImagesDir + "upbar.png", toFile +"upbar.png");
            wh.copy(genjImagesDir + "upleft.png", toFile +"upleft.png");
            wh.copy(genjImagesDir + "upright.png", toFile +"upright.png");
            wh.copy(genjImagesDir + "medno.png", toFile +"medno.png");
            wh.copy(genjImagesDir + "mednopic.png", toFile +"mednopic.png");
            wh.copy(genjImagesDir + "medpriv.png", toFile +"medpriv.png");
            wh.copy(genjImagesDir + "map.gif", toFile +"map.gif");
            wh.copy(genjImagesDir + "mail.gif", toFile +"mail.gif");
        } catch (IOException e) {
            //e.printStackTrace();
            wb.log.write(wb.log.ERROR, "createIcons - " + e.getMessage());
        }
    }

}
