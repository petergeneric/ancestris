/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.ancestris.trancestris.resources.ResourceFile;
import org.ancestris.trancestris.resources.ZipDirectory;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dominique
 */
public class ZipDirectoryNode extends AbstractNode implements PropertyChangeListener, SaveCookie {

    boolean change = false;
    ZipDirectory directory;

    public ZipDirectoryNode(ZipDirectory directory) {
        super(!directory.getDirs().isEmpty() ? new ZipDirectoryChildren(directory) : Children.LEAF, Lookups.singleton(directory));
        this.directory = directory;
        setDisplayName(directory.getName());
        ResourceFile resourceFile = directory.getResourceFile();
        if (resourceFile != null) {
            resourceFile.addPropertyChangeListener(WeakListeners.propertyChange(this, directory));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        change = true;
    }

    @Override
    public Node.Cookie getCookie(Class type) {
        if (type == SaveCookie.class && change == true) {
            return this;
        } else {
            return super.getCookie(type);
        }
    }

    @Override
    public void save() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getHtmlDisplayName() {
        if (directory.isTranslated() == false) {
            return "<font color='#FF0000'>" + directory.getName() + "</font>";
        } else {
            return "<font color='#0000FF'>" + directory.getName() + "</font>";
        }
    }
}
