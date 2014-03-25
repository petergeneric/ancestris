/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.view;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.openide.util.actions.Presenter;

/**
 * Toolbar abstraction.
 * FIXME: we will have to see whether this class should be only for genj views
 * or for regular ancestrisTopComponents. 
 * Don't forget it is used in PreviewTopCompenent
 */
public class ToolBar {

    AtomicBoolean notEmpty = new AtomicBoolean(false);
    JToolBar bar = new JToolBar();

    public void add(Action action) {
        bar.add(action);
        bar.setVisible(true);
        notEmpty.set(true);
    }

    public void add(JComponent component) {
        bar.add(component);
        bar.setVisible(true);
        component.setFocusable(false);
        notEmpty.set(true);
    }

    public void add(Presenter.Toolbar component) {
        if (component != null)
        bar.add(component.getToolbarPresenter());
    }

    public void addSeparator() {
        bar.addSeparator();
        bar.setVisible(true);
        notEmpty.set(true);
    }

    public JToolBar getToolBar() {
        return (notEmpty.get()) ? bar : null;
    }

    public void setOrientation(int orientation) {
        bar.setOrientation(orientation);
    }

    public void beginUpdate() {
        notEmpty.set(false);
        bar.removeAll();
        bar.setVisible(false);
//      bar.validate();
    }

    public void endUpdate() {
    }

    public void addGlue() {
        bar.add(Box.createGlue());
    }
}
