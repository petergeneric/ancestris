/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import java.util.logging.Logger;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.nodes.AbstractNode;

/**
 *
 * @author dominique
 */
public class ZipRootNode extends AbstractNode {

    private static final Logger logger = Logger.getLogger(ZipRootNode.class.getName());

    /** Creates a new instance of RootNode */
    public ZipRootNode(ZipArchive root) {
        super(new ZipDirectoryChildren(root.getRoot()));
        setDisplayName(root.getName());
    }
}
