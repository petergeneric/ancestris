/**
 * Ancestris
 *
 * Copyright (C) 2010 - 2011 Ancestris Team <dev@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genjfr.util;

import genj.util.swing.MenuHelper;
import java.awt.Component;
import java.util.Collection;


import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class which proxies MenuHelper class to NB Platform pop menu system
 */
@ServiceProvider(service = ActionPresenterProvider.class, position = 1)
public class MyActionPresenter extends ActionPresenterProvider {
    
    private ActionPresenterProvider getOther(){
       Collection<? extends ActionPresenterProvider> a = Lookup.getDefault().lookup(new Lookup.Template(ActionPresenterProvider.class)).allInstances();
       for (ActionPresenterProvider app: a) {
           if (app != this) {
               return app;
           }
       }
       return new Default();
    }
    /**
     * Creates a PopupMenu
     */
    public JPopupMenu createEmptyPopup() {
        return getOther().createEmptyPopup();
    }

    public final JMenuItem createPopupPresenter(Action action) {
        MenuHelper mh = new MenuHelper();

        mh.createPopup();
        return mh.createItem(action);
    }

    public JMenuItem createMenuPresenter(Action action) {
        return getOther().createMenuPresenter(action);
    }

    public Component createToolbarPresenter(Action action) {
        return getOther().createToolbarPresenter(action);
    }

    public Component[] convertComponents(Component comp) {
        return getOther().convertComponents(comp);
    }

    //
    // Default implementation of the the presenter
    //

    private static final class Default extends ActionPresenterProvider {

        public JMenuItem createMenuPresenter(Action action) {
            return new JMenuItem(action);
        }

        public JMenuItem createPopupPresenter(Action action) {
            return new JMenuItem(action);
        }

        public Component createToolbarPresenter(Action action) {
            return new JButton(action);
        }

        public JPopupMenu createEmptyPopup() {
            return new JPopupMenu();
        }

        public Component[] convertComponents(Component comp) {
            return new Component[] {comp};
        }
    }

} 

