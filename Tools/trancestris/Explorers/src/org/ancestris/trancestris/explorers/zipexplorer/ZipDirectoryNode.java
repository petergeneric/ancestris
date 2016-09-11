/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer;

import java.awt.Color;
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
        directory.addPropertyChangeListener(this);
        setDisplayName(directory.getName());
    }

    @Override
    public String getHtmlDisplayName() {
        Color color = directory.getColor();
        String hex = "#" + Integer.toHexString(color.getRGB()).substring(2);
        return "<font color='" + hex + "'>" + directory.getName() + " (" + (int) (((float) (directory.getTranslatedLineCount()) / (float) directory.getLineCount()) * 100) + " % / "+ directory.getLineCount() +") </font>";
    }

    @Override
    public String getName () {
        return directory.getName();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setDisplayName(getHtmlDisplayName());
    }
}
