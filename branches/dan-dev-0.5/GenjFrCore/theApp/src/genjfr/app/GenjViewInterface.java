/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.gedcom.Gedcom;
import org.openide.windows.Mode;

/**
 *
 * @author frederic
 */
public interface GenjViewInterface {

    public Gedcom getSelectedGedcom();
    public Gedcom getGedcom();
    public Mode getMode();
    public void setDefaultMode(String mode);
    public void setDefaultMode(Mode mode);

    public boolean close();
}