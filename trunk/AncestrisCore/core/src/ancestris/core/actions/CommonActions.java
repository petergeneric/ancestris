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

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

/**
 *
 * @author daniel
 */
public class CommonActions {

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

    public static Action createSeparatorAction(String title) {
        SeparatorAction result = new SeparatorAction(title);
        return result;
    }

    private static class SeparatorAction extends AbstractAction implements Presenter.Popup {

        private String title;

        public SeparatorAction(String title) {
            this.title = title;
            setEnabled(true);
            putValue(NAME, title);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //nothing to do
        }

        public JMenuItem getPopupPresenter() {
//            JMenuItem item = new JMenuItem("<html><b><font size=+1>"+title+"</font></b></html>");
            JMenuItem item = new TitleMenuItem(new StringBuilder("<html><b>").append(title).append("</b></html>").toString());
            String tt = (String) getValue(SHORT_DESCRIPTION);
            if (tt!=null){
                //setToolTipText("<html><p width=\"500\">" +value+"</p></html>");
                item.setToolTipText(new StringBuilder("<html><body width=\"500\">")
                        .append(tt.replaceAll("\\n", "<br/>"))
                        .append("</body></html>")
                        .toString());
            }
            item.setEnabled(false);
            return item;
        }
    }

    static private class TitleMenuItem extends JMenuItem implements DynamicMenuContent {

        private JLabel title;

        public TitleMenuItem(String title) {
            this.title = new JLabel(title);
            this.title.setBackground(UIManager.getColor("MenuItem.selectionBackground"));
            this.title.setForeground(UIManager.getColor("MenuItem.selectionForeground"));
            this.title.setOpaque(true);
        }

        @Override
        public void setToolTipText(String text) {
            title.setToolTipText(text);
        }

        
        public JComponent[] getMenuPresenters() {
            return new JComponent[]{title};
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }
    }
}
