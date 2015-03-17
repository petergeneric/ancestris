
package ancestris.modules.wizards.newgedcom;

import genj.gedcom.Context;
import genj.gedcom.Indi;

/**
 *
 * @author daniel.andre
 */
public interface INewGedcomProvider {
    public Context getContext();
    public Indi getFirst();
}
