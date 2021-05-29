package ancestris.modules.releve;

import java.awt.Component;

/**
 *
 * @author Michel
 */


public interface MenuCommandProvider {
    public void showPopupMenu(Component invoker, int x, int y);
    public void showStandalone();
    public void showStandalone(int panelIndex, int recordNo);
    public void showConfigPanel();
    public void showOptionPanel();
    public void showToFront();
    public void setBrowserVisible(boolean visible);
    public void toggleBrowserVisible();
    public void setGedcomLinkSelected(boolean selected);
    public void showImage();
}
