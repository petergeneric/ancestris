/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import ancestris.view.Images;
import ancestris.core.actions.AbstractAncestrisAction;
import java.awt.event.ActionEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class ActionOpen extends AbstractAncestrisAction {

    private FileObject file = null; 

    /** constructor - good for button or menu item */
    public ActionOpen() {
        setTip(NbBundle.getMessage(ActionOpen.class,"HINT_ActionOpen"));
        setText(NbBundle.getMessage(ActionOpen.class,"CTL_ActionOpen"));
        setImage(Images.imgOpen);
    }

    /** constructor - good for button or menu item */
    public ActionOpen(FileObject file) {
        this.file = file;
        setTip(NbBundle.getMessage(ActionOpen.class,"HINT_ActionOpen_file",FileUtil.getFileDisplayName(file)));
        setText(NbBundle.getMessage(ActionOpen.class,"CTL_ActionOpen_file",FileUtil.getFileDisplayName(file)));
        setImage(Images.imgOpen);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (file != null) {
            GedcomDirectory.getDefault().openGedcom(file);
        } else {
            GedcomDirectory.getDefault().openGedcom();
        }
    }

} // ActionOpen

