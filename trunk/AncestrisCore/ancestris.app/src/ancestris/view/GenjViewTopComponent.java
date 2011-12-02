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
package ancestris.view;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.app.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import genj.view.ActionProvider.Purpose;
import genj.view.ToolBar;
import genj.view.View;
import genj.view.ViewFactory;
import java.awt.Image;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Top component which displays Genj Views.
 */
public abstract class GenjViewTopComponent extends AncestrisTopComponent {

    private final static Logger LOG = Logger.getLogger("ancestris.view");

    /*
     * The following methods are used in conjunction with old Genj View classes.
     * Il getViewFactory returns non null, then this TopComponent proxies (thru
     * ViewProxy class) that View Class.
     * If it is null, then this will be a more conventionnal TopComponent.
     */
    /**
     * Must be overiden for a Genj View TopComponent
     * @return
     */
    public abstract ViewFactory getViewFactory();
    private GenjViewProxy viewProxy = null;

    /**
     * return Genj View proxyinstance for this view factory if applicable
     * @return
     */
    private GenjViewProxy getViewProxy() {
        if (getViewFactory() == null) {
            viewProxy = null;
            return null;
        }
        if (viewProxy == null) {
            viewProxy = new GenjViewProxy(getViewFactory());
        }
        return viewProxy;
    }

    @Override
    public Image getImageIcon() {
        if (getViewFactory() != null) {
            return getViewFactory().getImage().getImage();
        }
        return super.getImageIcon();
    }

    @Override
    public void setName() {
        if (getViewFactory() != null) {
            setName(getViewProxy().getName());
        }
        super.setName();
    }

    @Override
    public void setToolTipText() {
        if (getViewFactory() != null) {
            setToolTipText(getViewProxy().getToolTipText());
        } else 
        super.setToolTipText();
    }

    //XXX: do it in editorTC? in encestrisTC?
    @Override
    public void runWhenSizeIsCorrect() {
        if (getViewProxy() != null) {
            getViewProxy().setContext(getContext(), true);
        }
    }

    //XXX: do it in editorTC? in encestrisTC?
    @Override
    public boolean createPanel() {
        JPanel panel = getViewProxy().createPanel();
        if (panel == null) {
            return false;
        }
        setPanel(panel);
        setContext(getContext(), true);
        setToolBar(getViewProxy().createToolBar());
        return true;
    }

    //XXX: do it in editorTC
    @Override
    public boolean canClose() {
        if (getViewProxy().view != null) {
            getViewProxy().view.closing();
        }
        return super.canClose();
    }

    @Override
    protected void setContextImpl(Context context,boolean isActionPerformed){
        getViewProxy().setContext(context, isActionPerformed);
    }

    public View getView(){
        return getViewProxy().view;
    }

    /**
     * A class that proxies Genj View class to be used by an AncestrisTopComponent.
     * @author daniel
     */
    public class GenjViewProxy implements ActionProvider, GedcomFileListener {
        //XXX:: must be private (temporarily set to public to be accessed from GenjViewTC

        private View view;
        private ViewFactory factory;
//        private Context context;

        public GenjViewProxy() {
        }

        public GenjViewProxy(ViewFactory factory) {
            this.factory = factory;
        }

        public JPanel createPanel() {
            if (factory == null) {
                return null;
            }
            // create the view
            view = factory.createView();
            AncestrisPlugin.register(view);
            return view;
        }

        public void createActions(Context context, Purpose purpose, Group into) {
            // Delegate
            if (!(view instanceof ActionProvider)) {
                return;
            }
            if (view == null || context == null) {
                return;
            }

            if (context.sameGedcom(getContext())) {
                ((ActionProvider) view).createActions(context, purpose, into);
            }
        }

        public String getName() {
            if (factory == null) {
                return "";
            }
            return factory.getTitle();
        }

        public String getToolTipText() {
            if (factory == null) {
                return "";
            }
            return (factory.getTitle());
        }

        // ToolBar support
        public JToolBar createToolBar() {
            if (view == null) {
                return null;
            }

            AToolBar bar = new AToolBar();

            bar.beginUpdate();
            view.populate(bar);
            bar.endUpdate();

            return bar.getToolBar();
        }

        protected void setContext(Context context, boolean isActionPerformed) {
            if (view == null) {
                return;
            }
            view.setContext(context, isActionPerformed);
        }


        public void commitRequested(Context context) {
            if (context.sameGedcom(getContext())){
                LOG.log(Level.FINER, "context selection on unknown gedcom", new Throwable());
                return;
            }
            if (view != null) {
                view.commit();
            }
        }

        public void gedcomClosed(Gedcom gedcom) {
        }

        public void gedcomOpened(Gedcom gedcom) {
        }
    }

    static private class AToolBar implements ToolBar {

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

        public void addSeparator() {
            bar.addSeparator();
            bar.setVisible(true);
            notEmpty.set(true);
        }

        public JToolBar getToolBar() {
            return (notEmpty.get()) ? bar : null;
        }

        private void setOrientation(int orientation) {
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
}
