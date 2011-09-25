/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.api.editor;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.util.ChangeSupport;
import genj.view.ViewContext;
import java.awt.Image;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 * The base class for all the editors
 */
public abstract class Editor extends JPanel {

    protected ChangeSupport changes = new ChangeSupport(this);
    protected List<Action> actions = new ArrayList<Action>();
    private String title = null;

    /**
     * Accessor - current
     */
    public abstract ViewContext getContext();

    /**
     * Accessor - current
     */
    public abstract void setContext(Context context);

    /**
     * commit changes
     */
    public abstract void commit() throws GedcomException;

    /**
     * Editor's actions
     */
    public List<Action> getActions() {
        return actions;
    }

    public void addChangeListener(ChangeListener listener) {
        changes.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changes.removeChangeListener(listener);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        if (title != null) {
            return title;
        }
        return getTitleImpl();
    }

    protected String getTitleImpl() {
        return (getContext() == null ? "" : getContext().getText());
    }

    public Image getImageIcone() {
        return null;
    }

    /**
     * Show editor in a dialog window
     * @return true if commit succesfull, false if not or cancel button has been clicked
     */
    public boolean showPanel() {
        NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(this), getTitle(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }
        try {
            getContext().getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    commit();
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;

    }
} //Editor

