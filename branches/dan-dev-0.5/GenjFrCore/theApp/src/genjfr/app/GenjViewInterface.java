/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import org.openide.windows.Mode;

/**
 *
 * @author frederic
 */
public interface GenjViewInterface {

    public Gedcom getGedcom();
    public Mode getMode();
    public void setDefaultMode(String mode);
    public void setDefaultMode(Mode mode);
    public void init(Context context);
    public AncestrisTopComponent create(Context context);

    public boolean close();
}