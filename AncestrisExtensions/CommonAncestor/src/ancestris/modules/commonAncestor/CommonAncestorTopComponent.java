package ancestris.modules.commonAncestor;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.view.SelectionListener;
import java.awt.BorderLayout;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = CommonAncestorTopComponent.class)
public final class CommonAncestorTopComponent extends TopComponent implements SelectionListener, GedcomFileListener {

    //private static final String PREFERRED_ID = "CommonAncestorTopComponent";
    private SamePanel samePanel;
    private Context context = null;

    /**
     * CommonAncestorTopComponent factory
     */
    public static CommonAncestorTopComponent createInstance(Context context) {
        CommonAncestorTopComponent commonAncestorTopComponent = null;

        // search existing CommonAncestorTopComponent with the same Gedcom
        Context currentContext = context;
        //Context currentContext = GedcomDirectory.getInstance().getLastContext();
        if (currentContext != null) {
            Gedcom currentGedcom = currentContext.getGedcom();
            if (currentGedcom != null) {
                // find existing commonAncestorTopComponent with same gedcom
                for (CommonAncestorTopComponent tc : AncestrisPlugin.lookupAll(CommonAncestorTopComponent.class)) {
                    if (tc.getContext() != null) {
                        Gedcom gedcom = tc.getContext().getGedcom();
                        if (gedcom != null) {
                            if (currentGedcom.equals(gedcom)) {
                                commonAncestorTopComponent = tc;
                                break;
                            }
                        }
                    }
                }
            }

            if (commonAncestorTopComponent == null) {
                // create a new componenet
                commonAncestorTopComponent = new CommonAncestorTopComponent();
                commonAncestorTopComponent.initContext(currentContext);
                // set default dock mode
                Mode mode = WindowManager.getDefault().findMode("ancestris-nav");
                if (mode != null) {
                    mode.dockInto(commonAncestorTopComponent);
                }
                commonAncestorTopComponent.open();
                commonAncestorTopComponent.requestActive();
            } else {
                // Topcomponent already exist, bring it to front
                commonAncestorTopComponent.open();
                commonAncestorTopComponent.requestActive();
            }
        }
        return commonAncestorTopComponent;
    }

    /**
     * default constructor
     */
    public CommonAncestorTopComponent() {
        super();
    }

    /**
     * setContext widgets inside panel
     * @param context 
     */
    public void initContext(Context context) {
        this.context = context;
    }

    /**
     * Create Panel and register listeners
     * But if context is not valid , close CommonAncestorTopComponent
     */
    @Override
    public void componentOpened() {
        super.componentOpened();
        if (context != null && context.getGedcom() != null) {
            // je mets a jour le titre de la fenetre
            setName(context.getGedcom().getName());
            setToolTipText(NbBundle.getMessage(CommonAncestorTopComponent.class, "HINT_CommonAncestorTopComponent") + ": " + context.getGedcom().getName());

            // create my panel
            samePanel = new SamePanel();
            samePanel.init(context);
            setLayout(new BorderLayout());
            add(samePanel, BorderLayout.NORTH);

            // register for selectionListener
            AncestrisPlugin.register(this);
            // register for close gedcom
            //Workbench.getInstance().addWorkbenchListener(this);
            //context.getGedcom().addGedcomListener(this);
        } else {
            // contexte non valide
            // je ferme le composant
            close();
        }
    }

    /**
     * unregister listener before closing
     */
    @Override
    public void componentClosed() {
        if (samePanel != null) {
            samePanel.closePreview();
        }
        AncestrisPlugin.unregister(this);
        //Workbench.getInstance().removeWorkbenchListener(this);        
        super.componentClosed();
    }

    /**
     * 
     * @param context
     */
    @Override
    public void setContext(Context context) {
        if (context != null && context.getGedcom() != null) {
            if (context.getGedcom().equals(this.context.getGedcom()) && samePanel != null) {
                samePanel.updateCurrentIndividu(context.getEntity());
            }
        }
    }

//    @Override
//    protected String preferredID() {
//        return PREFERRED_ID;
//    }
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public Context getContext() {
        return context;
    }

//    /////////////////////////////////////////////////////////////////////////////
//    // implements WorkbenchListener
//    ///////////////////////////////////////////////////////////////////////////
//
//    public void selectionChanged(Context context, boolean isActionPerformed) {
//    }
//
//    public void processStarted(Trackable process) {
//    }
//
//    public void processStopped(Trackable process) {
//    }
//
//    public void commitRequested(Context context) {
//    }
//
//    public void workbenchClosing() {
//    }
    /**
     * Close CommonAncestorTopComponenent when its gedcom is going to close
     * @param workbench
     * @param gedcom 
     */
    public void gedcomClosed(Gedcom gedcom) {
        if (gedcom.equals(getContext().getGedcom())) {
            close();
        }
    }

    public void gedcomOpened(Gedcom gedcom) {
    }

//    public void viewOpened(View view) {
//    }
//
//    public void viewClosed(View view) {
//    }
//
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 300, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents

    public void commitRequested(Context context) {
        // nothing to do
    }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
