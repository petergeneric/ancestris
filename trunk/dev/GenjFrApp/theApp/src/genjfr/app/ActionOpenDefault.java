/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import java.io.File;
import javax.swing.JMenuItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CookieAction;

public final class ActionOpenDefault extends CookieAction {

    private File fileToOpen = null;

    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{
                    String.class
                };
    }

    @Override
    protected void performAction(Node[] nodes) {
        if (fileToOpen != null) {
            try {
                ActionOpen open = new ActionOpen(fileToOpen.toURI().toURL().toString());
                open.trigger();
            } catch (Exception e) {
                System.out.println("Error opening default gedcom:" + e);
            }
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        String name = getGedName().isEmpty() ? "<...>" : getGedName();
        return NbBundle.getMessage(ActionOpenDefault.class, "CTL_ActionOpenDefault") + " " + name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(Node[] arg0) {
        super.enable(arg0);
        if (getDefaultFile().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    protected String iconResource() {
        return "genjfr/app/OpenDefault.png";
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenuItem item = super.getMenuPresenter();
        //item.setOpaque(true);
        //item.setBackground(Color.YELLOW);
        //item.setForeground(Color.BLUE);
        return item;
    }

    /*
     * GetDefaultFile
     * Take advantage to initialise fileToOpen
     *
     */
    private String getDefaultFile() {
        String gedcomDir = NbPreferences.forModule(genj.app.App.class).get("gedcomDir", "");
        if (gedcomDir.isEmpty()) {
            return "";
        }
        String gedcomDefaultFile = getGedName();
        String defaultFile = gedcomDir + File.separator + gedcomDefaultFile;
        try {
            // check if it's a local file
            File local = new File(defaultFile);
            fileToOpen = local;
            if (!local.exists()) {
                return "";
            }
        } catch (Throwable t) {
            return "";
        }

        return gedcomDefaultFile;
    }

    private String getGedName() {
        String gedcomDefaultFile = NbPreferences.forModule(genj.app.App.class).get("gedcomFile", "");
        if (gedcomDefaultFile.isEmpty()) {
            return "";
        }
        return gedcomDefaultFile;
    }
}
