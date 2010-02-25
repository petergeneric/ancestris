/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.gedcom.Gedcom;

/**
 *
 * @author frederic
 */
public interface GenjInterface {

    public Gedcom getSelectedGedcom();
        public Gedcom getGedcom();

    public boolean close();
}