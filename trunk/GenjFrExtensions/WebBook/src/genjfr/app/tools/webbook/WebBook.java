/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;

/**
 *
 * @author frederic
 */
public class WebBook {

    private Gedcom gedcom;
    private Log log;

    // Constructor
    public WebBook(Gedcom gedcom, Log log) throws InterruptedException {
        this.gedcom = gedcom;
        this.log = log;
        run();
    }

    private void run() throws InterruptedException {
        Entity[] ents = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        for (int i = 0; i < 20; i++) {
            log.write("" + i + "/20 " + ents[i]);
            Thread.sleep(1000);
        }
    }
}
