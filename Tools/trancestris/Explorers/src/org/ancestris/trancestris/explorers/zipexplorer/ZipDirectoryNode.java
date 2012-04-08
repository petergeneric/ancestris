/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import org.ancestris.trancestris.resources.ZipDirectory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author dominique
 */
public class ZipDirectoryNode extends AbstractNode {

    boolean change = false;
    ZipDirectory directory;

    public ZipDirectoryNode(ZipDirectory directory) {
        super(!directory.getDirs().isEmpty() ? new ZipDirectoryChildren(directory) : Children.LEAF, Lookups.singleton(directory));
        this.directory = directory;
        setDisplayName(directory.getName());
    }

    @Override
    public String getHtmlDisplayName() {
        if (directory.isTranslated() == false) {
            if (directory.getResourceFile() != null) {
                return "<font color='#FF0000'>" + directory.getName() + " (" + directory.getResourceFile().getTranslatedPercent() + " %) </font>";
            } else {
                return "<font color='#FF0000'>" + directory.getName() + "</font>";

            }
        } else {
            if (directory.getResourceFile() != null) {
                return "<font color='#0000FF'>" + directory.getName() + " (" + directory.getResourceFile().getTranslatedPercent() + " %) </font>";
            } else {
                return "<font color='#0000FF'>" + directory.getName() + "</font>";
            }
        }
    }
}
