package ancestris.app;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomFileListener;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.util.swing.GraphicsHelper;
import genj.util.swing.PopupWidget;
import genj.view.SelectionListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

public final class ActionHistory implements Presenter.Toolbar {

    private static HistoryCombo historyCombo;

    @Override
    public java.awt.Component getToolbarPresenter() {
        if (historyCombo == null) {
            historyCombo = new HistoryCombo();
        }
        return historyCombo;
    }

    private static class HistoryCombo extends JPanel {

        private final Icon POPUP = GraphicsHelper.getIcon(Color.BLACK, 0, 0, 8, 0, 4, 4);
        private List<Entity> history = new ArrayList<Entity>();
        private int index = -1;
        private EventHandler events = new EventHandler();
        private Back back = new Back();
        private Forward forward = new Forward();
        private Popup pick = new Popup();

        /**
         * Constructor
         */
        public HistoryCombo() {
            setLayout(new java.awt.GridBagLayout());
            add(back);
            add(forward);
            add(pick);
        }

        public Component add(Action a) {
            JButton b = new JButton(a);
            b.setFocusable(false);
            return add(b);
        }

        private void fireSelection(Entity e) {
            SelectionDispatcher.fireSelection(new Context(e));
        }

        private void update() {
            boolean enable = index < history.size() - 1;
            forward.setEnabled(enable);
            forward.setTip(enable ? NbBundle.getMessage(ActionNew.class,"CTL_ActionHistoryForward", getEntityText(index + 1)) : "");

            enable = index > 0;
            back.setEnabled(enable);
            back.setTip(enable ? NbBundle.getMessage(ActionNew.class,"CTL_ActionHistoryBack", getEntityText(index - 1)) : "");

            pick.setEnabled(history.size() > 1);
        }

        private String getEntityText(int i) {
            try {
                Entity e = history.get(i);
                return e.toString();
            } catch (Exception e) {
            }
            return null;
        }
        
        public void gotoPrevious() {
            if (index < 1) {
                return;
            }
            index--;
            update();
            fireSelection(history.get(index));
        }

        public void gotoNext() {
            if (index == history.size() - 1) {
                return;
            }
            index++;
            update();
            fireSelection(history.get(index));
        }

        /** back */
        private class Popup extends PopupWidget {

            Popup() {
                super(POPUP);
            }

            @Override
            public void showPopup() {
                removeItems();
                for (int i = 0; i < history.size(); i++) {
                    JMenuItem item = new JMenuItem(new Jump(i));
                    if (index == i) {
                        item.setFont(item.getFont().deriveFont(Font.BOLD));
                    }
                    addItem(item);
                }
                super.showPopup();
            }
        }

        private class Jump extends AbstractAncestrisAction {

            private int i;

            public Jump(int i) {
                this.i = i;
                Entity entity = history.get(i);
                setImage(entity.getImage());
                setText(entity.toString());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                index = i;
                update();
                fireSelection(history.get(i));
            }
        }

        /** back */
        @ActionID(category = "Edit", id = "ancestris.app.ActionHistory.Back")
        @ActionRegistration(displayName = "#CTL_ActionHistoryBack", lazy = false)
        @ActionReferences(value = {
        @ActionReference(path = "Actions/Edit", position = 9),
        @ActionReference(path = "Shortcuts", name= "A-LEFT")
        })
        public static class Back extends AbstractAncestrisAction {

            public Back() {
                setImage(new ImageIcon(ActionHistory.class.getResource("Back.png")));
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                historyCombo.gotoPrevious();
            }
        }

        /** forward */
        @ActionID(category = "Edit", id = "ancestris.app.ActionHistory.Forward")
        @ActionRegistration(displayName = "#CTL_ActionHistoryForward", lazy = false)
        @ActionReferences(value = { 
        @ActionReference(path = "Actions/Edit", position = 10),
        @ActionReference(path = "Shortcuts", name= "A-RIGHT")
        })
        public static class Forward extends AbstractAncestrisAction {

            public Forward() {
                setImage(new ImageIcon(ActionHistory.class.getResource("Forward.png")));
            }

            public void actionPerformed(ActionEvent evt) {
                historyCombo.gotoNext();
            }
        }

        private class EventHandler implements SelectionListener, GedcomFileListener, GedcomListener {

            EventHandler() {
                AncestrisPlugin.register(this);
            }

            @Override
            public void gedcomClosed(Gedcom gedcom) {
                history.clear();
                index = -1;
                update();
                gedcom.removeGedcomListener(this);
            }

            @Override
            public void gedcomOpened(Gedcom gedcom) {
                gedcom.addGedcomListener(this);
            }

            public void commitRequested(Context context) {
            }

            @Override
            public void setContext(Context context) {

                Entity e = context.getEntity();
                if (e == null) {
                    return;
                }

                // don't add twice to tail
                if (!history.isEmpty() && history.get(index) == e) {
                    return;
                }

                // add
                while (history.size() > index + 1) {
                    history.remove(history.size() - 1);
                }
                history.add(++index, e);

                // trim
                while (history.size() > 16) {
                    index--;
                    history.remove(0);
                }

                update();

                // show
            }

            @Override
            public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
            }

            @Override
            public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {

                // affects history?
                int i = history.indexOf(entity);
                if (i < 0) {
                    return;
                }

                // remove it
                history.remove(i);
                if (index >= i) {
                    index--;
                }

                // go to previous (or next if only thing available)
                i--;
                if (i < 0 && history.size() > 0) {
                    i++;
                }
                if (i >= 0) {
                    fireSelection(history.get(i));
                }

                // fallback to first available entity of same type that is not the entity that is being deleted
                if (i < 0) {
                    Entity first = null;
                    for (Entity e : gedcom.getEntities()) {
                        if (e.getTag().equals(entity.getTag()) && !e.getId().equals(entity.getId())) {
                            first = e;
                        }
                    }
                    if (first != null) {
                        fireSelection(first);
                    }
                }

                update();
            }

            @Override
            public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
            }

            @Override
            public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
            }

            @Override
            public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
            }
        }
    }
}
