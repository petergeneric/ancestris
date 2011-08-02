/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.ancestris.trancestris.resources.ZipDirectory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dominique
 */
public class ZipDirectoryNode extends AbstractNode implements PropertyChangeListener {

    boolean change = false;
    ZipDirectory directory;

    public ZipDirectoryNode(ZipDirectory directory) {
        super(!directory.getDirs().isEmpty() ? new ZipDirectoryChildren(directory) : Children.LEAF, Lookups.singleton(directory));
        this.directory = directory;
        this.directory.addPropertyChangeListener(this);
        setDisplayName(directory.getName());
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        fireDisplayNameChange(directory.getName(), directory.getName());
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
