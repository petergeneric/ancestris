package ancestris.modules.releve;

import java.awt.Component;

/**
 *
 * @author Michel
 */


public interface MenuCommandProvider {
    public void showPopupMenu(Component invoker, int x, int y);
    public void showStandalone(boolean show);
    public void showConfigPanel();
    public void showOptionPanel();
    public void showToFront();
    public void standaloneEditorClosed();
}
