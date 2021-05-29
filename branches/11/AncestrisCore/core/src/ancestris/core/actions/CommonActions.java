/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.core.actions;

import genj.gedcom.Property;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

/**
 *
 * @author daniel & frederic
 */
public class CommonActions {

    public static int TYPE_CONTEXT_MENU = 0;
    public static int TYPE_DND_MENU = 1;

    /**
     * Special action that does nothing and is hidden in submenu.
     */
    public static final Action NOOP = new NoOpAction();

    private static class NoOpAction extends AbstractAction {

        public NoOpAction() {
            setEnabled(false);
            putValue(NAME, "noop");
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //noop
        }
    }

    public static Action createTitleAction(int menuType, Property property, Object... o) {
        return new TitleAction(menuType, property, o);
    }

    private static class TitleAction extends AbstractAction implements Presenter.Popup {

        private Property property;
        private int menuType;
        private Object[] o;
        
        public TitleAction(int menuType, Property property, Object... o) {
            this.property = property;
            this.menuType = menuType;
            this.o = o;
            setEnabled(true);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //nothing to do
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return new TitleMenuItem(menuType, property, o); 
        }
    }

    static private class TitleMenuItem extends JMenuItem implements DynamicMenuContent {

        private JPanel panel;

        public TitleMenuItem(int menuType, Property property, Object... o) {
            switch (menuType) {
                case 0: panel = new TitleContextMenuPanel(property);
                    break;
                case 1: panel = new TitleDNDMenuPanel(property, o);
                    break;
                default: 
                    break;
            }
            
        }

        @Override
        public JComponent[] getMenuPresenters() {
            return new JComponent[]{panel};
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }
    }

    
}
