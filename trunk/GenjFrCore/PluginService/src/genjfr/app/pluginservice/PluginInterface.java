/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.pluginservice;

/**
 *
 * @author frederic
 */
public interface PluginInterface {

    public String getPluginName();
    public String getPluginVersion();

    public boolean launchModule(Object o);
}
