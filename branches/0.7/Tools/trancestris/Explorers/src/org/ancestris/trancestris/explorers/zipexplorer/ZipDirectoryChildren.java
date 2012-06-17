/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import org.ancestris.trancestris.resources.ZipDirectory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

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
        return new Node[]{newNode};
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(zipDirectory.getDirs());
    }
}
