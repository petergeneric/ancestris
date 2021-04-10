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

import ancestris.gedcom.GedcomFileListener;
import ancestris.swing.ToolBar;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.view.View;
import genj.view.ViewFactory;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * Top component which displays Genj Views.
 */
public abstract class GenjViewTopComponent extends AncestrisTopComponent implements GedcomFileListener {

    private final static Logger LOG = Logger.getLogger("ancestris.view");

    /*
     * The following methods are used in conjunction with old Genj View classes.
     * If getViewFactory returns non null, then this TopComponent proxies
     * that View Class.
     * If it is null, then this will be a more conventionnal TopComponent.
     */
    /**
     * Must be overiden for a Genj View TopComponent
     *
     * @return
     */
    public abstract ViewFactory getViewFactory();
//    private GenjViewProxy viewProxy = null;
    private View view;

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
            setName(getViewFactory().getTitle());
        }
        super.setName();
    }

    @Override
    public void setToolTipText() {
        if (getViewFactory() != null) {
            setToolTipText(getViewFactory().getTooltip());
        } else {
            super.setToolTipText();
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        if (getViewFactory() != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (view != null) {
                        view.setContext(getContext());
                    }
                }
            });
        }
    }

    // ToolBar support
    @Override
    public JToolBar getToolBar() {
        if (view == null) {
            return null;
        }

        final ToolBar bar = new ToolBar();

        bar.beginUpdate();
        view.populate(bar);
        bar.endUpdate();

        return bar.getToolBar();
    }

    //XXX: do it in editorTC? in encestrisTC?
    @Override
    public boolean createPanel() {
        if (view == null && getViewFactory() != null) {
            view = getViewFactory().createView();
            //       AncestrisPlugin.register(view);
        }
        if (view == null) {
            return false;
        }

        setPanel(view);
        setContext(getContext());
        addToolBar();
        return true;
    }

    @Override
    public void componentClosed() {
        if (view != null) {
            view.closing();
        }
        view = null;
        super.componentClosed();
    }

    @Override
    public boolean canClose() {
        return super.canClose();
    }

    @Override
    protected void setContextImpl(Context context) {
        if (view != null) {
            view.setContext(context);
        } else {
            super.setContextImpl(context);
        }
    }

    public View getView() {
        return view;
    }

    @Override
    public void commitRequested(Context context) {
        if (!context.sameGedcom(getContext())) {
            LOG.log(Level.FINER, "context selection on unknown gedcom", new Throwable());
            return;
        }
        if (view != null) {
            view.commit();
        }
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
    }
}
