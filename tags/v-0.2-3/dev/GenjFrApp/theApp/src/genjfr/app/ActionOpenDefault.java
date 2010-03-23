/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import java.io.File;
import javax.swing.JMenuItem;
import org.netbeans.api.actions.Openable;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CookieAction;

public final class ActionOpenDefault extends CookieAction implements Openable {

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
                ActionOpen open = new ActionOpen(fileToOpen.toURI().toURL().toString(),true);
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
        String name = getDefaultFile(true);
        String str = "";
        str = (name == null || name.isEmpty()) ? "<...>" : name;
        return NbBundle.getMessage(ActionOpenDefault.class, "CTL_ActionOpenDefault") + " " + str;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(Node[] arg0) {
        super.enable(arg0);
        return calcState();
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
    private String getDefaultFile(boolean nameOnly) {
        String defaultFile = NbPreferences.forModule(App.class).get("gedcomFile", "");
        if (defaultFile.isEmpty()) {
            return "";
        }
        File local = null;
        try {
            // check if it's a local file
            local = new File(defaultFile);
            fileToOpen = local;
            if (!local.exists()) {
                return null;
            }
        } catch (Throwable t) {
            return null;
        }

        if (nameOnly) {
            defaultFile = local.getName();
        }
        return defaultFile;
    }

    public boolean calcState() {
        String str = getDefaultFile(false);
        return (str != null && !str.isEmpty());
    }

    public void open() {

        String str = getDefaultFile(false);
        setEnabled((str != null && !str.isEmpty()));

        String statusText = StatusDisplayer.getDefault().getStatusText()
                + (str != null ? "" : " - " + org.openide.util.NbBundle.getMessage(ActionOpenDefault.class, "OptionPanel.notexist.statustext"));
        StatusDisplayer.getDefault().setStatusText(statusText);
    }

}
