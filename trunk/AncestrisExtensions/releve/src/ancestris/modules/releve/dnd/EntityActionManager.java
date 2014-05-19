package ancestris.modules.releve.dnd;

import genj.gedcom.Entity;

/**
 *
 * @author Michel
 */
public interface EntityActionManager {
    void setRoot(Entity entity);
    void show(Entity entity);
    void selectSource();
}
