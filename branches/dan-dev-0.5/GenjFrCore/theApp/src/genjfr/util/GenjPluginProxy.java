/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.util;

import genj.app.PluginFactory;
import genj.app.Workbench;
import genj.app.WorkbenchAdapter;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Trackable;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import genj.view.View;
import genjfr.app.App;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.util.HashMap;
import java.util.Map;

/**
 * a special class to proxy a Genj Plugin into an Ancestris plugin.
 * @author daniel
 */
public class GenjPluginProxy extends GenjFrPlugin {
    private static Map<PluginFactory,GenjPluginProxy> instance = new HashMap<PluginFactory, GenjPluginProxy>();

    private GenjPlugin genjPluginInstance;

    private GenjPluginProxy(PluginFactory plugFactory){
        genjPluginInstance = (GenjPlugin) plugFactory.createPlugin(App.workbenchHelper.getWorkbench());

    }

    public GenjPluginProxy getInstance(PluginFactory plugFactory){
        if (instance.get(plugFactory) == null){
            instance.put(plugFactory, new GenjPluginProxy(plugFactory));
        }
        return instance.get(plugFactory);
    }

    public Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException();
    }

    class GenjPlugin extends WorkbenchAdapter implements ActionProvider {


	public void commitRequested(Workbench workbench) {
	}

	public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
	}

	public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
	}

	public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
	}

	public void viewClosed(Workbench workbench, View view) {
	}

	public void viewOpened(Workbench workbench, View view) {
	}


  public void processStarted(Workbench workbench, Trackable process) {
  }

  public void processStopped(Workbench workbench, Trackable process) {
  }

    public void workbenchClosing(Workbench workbench) {
    }

    public void viewRestored(Workbench workbench, View view) {
    }



        public void createActions(Context context, Purpose purpose, Group into) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
