/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.document.view;

import static ancestris.modules.document.view.Bundle.*;
import genj.gedcom.Context;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Panel for showing a document. This class must be extended to
 * provide specific functionnalities and contain all the base logic
 * to be displayed in a {@link DocumentViewTopComponent}.
 *
 * The view will be a JComponent (generally a JPanel) inside a JScrolPan.
 *
 * @author Daniel
 */
public class AbstractDocumentView extends JPanel {

    private transient JScrollPane scrollPane = null;
    private Action[] actions = null;
    private Context context;

    private AbstractDocumentView() {
        super();
        setFocusCycleRoot(true);
        scrollPane = new JScrollPane();
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Create and register the AbstractDocumentView. The view is initially
     * filled with a "No Content" label
     *
     * @param context Gedcom Context
     * @param title   Tab title
     * @param tooltip Tooltip for tab
     */
    public AbstractDocumentView(Context context, String title, String tooltip) {
        this();
        this.context = context;
        setName(title);
        setToolTipText(tooltip);
        setView(null);
        setToolbarActions(null);
        init();
    }

    final void init() {
        DocumentViewTopComponent.findInstance().addPanel(this);
    }

    /*
     * Returns gedcom context for this view
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Set the component this view must show. If null, show "no content" text.
     *
     * @param component
     */
    @NbBundle.Messages("nocontent=No Content")
    public final void setView(JComponent component) {
        if (component == null) {
            component = new JLabel(nocontent());
        }
        scrollPane.setViewportView(component);
        
        // scroll up if text document
        if (component instanceof HyperLinkTextPane) {
            final HyperLinkTextPane hltp = (HyperLinkTextPane) component;
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    hltp.setCaretPosition(0);
                    validate();
                    repaint();
                }
            });
        }
        
        validate();
    }

    /**
     * Set this view's toolbar. In this default implementation, toolbar is
     * oriented vertically in the west. For usability it is recommended to set
     * no more than five buttons.
     */
    private void setToolbar() {
        Component oldTb = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.WEST);
        if (oldTb != null) {
            remove(oldTb);
        }
        if (actions != null) {
            JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
            toolBar.setFloatable(false);
            for (Action a : actions) {
                toolBar.add(a);
            }
            add(toolBar, BorderLayout.WEST);
        }
        validate();
    }

    /**
     * Set this view toolbars actions.
     *
     * @param actions
     */
    public final void setToolbarActions(Action[] actions) {
        this.actions = actions == null?null:actions.clone();
        setToolbar();
        validate();
    }

    /**
     * gets toolbar actions. Usefull to add action to actions already set
     * by the derived class
     */
    public Action[] getToolbarActions() {
        return actions == null?null:actions.clone();
    }

    public void close() {
        DocumentViewTopComponent.findInstance().removePanel(this);
        closeNotify();
    }

    protected void closeNotify() {
        if (scrollPane != null) {
            scrollPane.setViewport(null);
        }
        //super.closeNotify();
    }
}
