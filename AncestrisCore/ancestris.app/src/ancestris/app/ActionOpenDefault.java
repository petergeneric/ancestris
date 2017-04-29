/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import java.net.URL;
import javax.swing.JMenuItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

//XXX: I think there is a mismatch in Openable use
public final class ActionOpenDefault extends CookieAction {//implements Openable {

    private FileObject fileToOpen = null;
    
    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class<?>[]{
                    String.class
                };
    }

    @Override
    protected void performAction(Node[] nodes) {
        if (fileToOpen != null) {
            try {
                GedcomDirectory.getDefault().openGedcom(fileToOpen);
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
        setEnabled((name != null && !name.isEmpty()));
        String str = "";
        str = (name == null || name.isEmpty()) ? NbBundle.getMessage(ActionOpenDefault.class, "ActionOpenDefault.NoFile") : name;
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
        return "ancestris/view/images/OpenDefault.png";
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenuItem item = super.getMenuPresenter();
        //item.setOpaque(true);
        //item.setBackground(Color.YELLOW);
        //item.setForeground(Color.BLUE);
        return item;
    }

    /**
     * return the display name for defaukt file to open
     * @param nameOnly if true return only filename and extension
     * @return 
     */
    private String getDefaultFile(boolean nameOnly) {
        URL defaultFile = ancestris.core.CoreOptions.getInstance().getDefaultGedcom();
        if (defaultFile == null) {
            return null;
        }
        try {
            // check if it's a local file
            fileToOpen = URLMapper.findFileObject(defaultFile);
            if (!fileToOpen.isValid()) {
                return null;
            }
        } catch (Throwable t) {
            return null;
        }

        if (nameOnly) {
            return fileToOpen.getNameExt();
        }
        return FileUtil.getFileDisplayName(fileToOpen);
    }

    public boolean calcState() {
        return ancestris.core.CoreOptions.getInstance().getDefaultGedcom() != null;
    }

    //XXX: I think there is a mismatch in Openable use
//    public void open() {
//
//        String str = getDefaultFile(false);
//        setEnabled((str != null && !str.isEmpty()));
//
//        String statusText = StatusDisplayer.getDefault().getStatusText()
//                + (str != null ? "" : " - " + org.openide.util.NbBundle.getMessage(ActionOpenDefault.class, "OptionPanel.notexist.statustext"));
//        StatusDisplayer.getDefault().setStatusText(statusText);
//    }

}
