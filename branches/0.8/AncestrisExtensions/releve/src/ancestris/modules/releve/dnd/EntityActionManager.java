package ancestris.modules.releve.dnd;

import genj.gedcom.Property;

/**
 *
 * @author Michel
 */
public interface EntityActionManager {
    void setRoot(Property entity);
    boolean show(Property entity);
    void selectSource();
}
