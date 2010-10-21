/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genj.app;

import genj.gedcom.Context;
import genj.view.View;
import genj.view.ViewFactory;
import java.io.File;
import java.net.URL;
import javax.swing.JComponent;

/**
 *
 * @author daniel
 */
public interface IWorkbenchHelper extends WorkbenchListener{
    public void register (Object o);

    public void unregister (Object o);

    public View openViewImpl(ViewFactory factory, Context context);

    public Context getContext();

    public void fireSelection(Context context, boolean isActionPerformed);

    public File chooseFile(String title, String action, JComponent accessory);

    public Context openGedcom(URL uRL);

}
