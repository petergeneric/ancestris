/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.view;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
public interface AncestrisViewInterface {

    public Gedcom getGedcom();
    public Mode getMode();
    public void setDefaultMode(String mode);
    public void setDefaultMode(Mode mode);
    public void init(Context context);
    public TopComponent create(Context context);

    public boolean close();
}