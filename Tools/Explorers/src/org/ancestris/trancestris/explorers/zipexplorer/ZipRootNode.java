/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author dominique
 */
public class ZipRootNode extends AbstractNode implements PropertyChangeListener, SaveCookie {

    private static final Logger logger = Logger.getLogger(ZipRootNode.class.getName());
    private boolean change = false;
    InstanceContent content;

    /** Creates a new instance of RootNode */
    public ZipRootNode(ZipArchive root, InstanceContent content) {
        super(new ZipDirectoryChildren(root.getRoot()));
        setDisplayName(root.getName());
        this.content = content;
    }

    @Override
    public Node.Cookie getCookie(Class type) {
        logger.entering(ZipRootNode.class.getName(), "getCookie", type);
        if (type == SaveCookie.class && change == true) {
            return this;
        } else {
            return super.getCookie(type);
        }
    }

    public void fire(boolean modified) {
        logger.entering(ZipRootNode.class.getName(), "fire", modified);
        content.add(this);
    }

    @Override
    public void save() throws IOException {
        logger.entering(ZipRootNode.class.getName(), "save");
        logger.log(Level.INFO, "Save file {0}");
        Confirmation msg = new NotifyDescriptor.Confirmation("Do you want to save ?", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);

        Object result = DialogDisplayer.getDefault().notify(msg);

        //When user clicks "Yes", indicating they really want to save,
        //we need to disable the Save button and Save menu item,
        //so that it will only be usable when the next change is made
        //to the text field:
        if (NotifyDescriptor.YES_OPTION.equals(result)) {
            change = false;
            //Implement your save functionality here.
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        change = true;
        fire(true);
    }
}
