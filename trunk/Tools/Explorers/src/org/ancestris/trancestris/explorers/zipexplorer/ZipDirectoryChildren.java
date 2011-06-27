/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import java.beans.PropertyChangeListener;
import org.ancestris.trancestris.resources.ResourceFile;
import org.ancestris.trancestris.resources.ZipDirectory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author dominique
 */
public class ZipDirectoryChildren extends Children.Keys<ZipDirectory> {

    private ZipDirectory zipDirectory = null;

    public ZipDirectoryChildren(ZipDirectory zipDirectory) {
        this.zipDirectory = zipDirectory;
    }

    @Override
    protected Node[] createNodes(ZipDirectory key) {
        ZipDirectoryNode newNode = new ZipDirectoryNode(key);
        newNode.addPropertyChangeListener((PropertyChangeListener) this.getNode());
        ResourceFile resourceFile = key.getResourceFile();
        if (resourceFile != null) {
            resourceFile.addPropertyChangeListener(WeakListeners.propertyChange(newNode, key));
        }
        return new Node[]{newNode};
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(zipDirectory.getDirs());
    }
}
