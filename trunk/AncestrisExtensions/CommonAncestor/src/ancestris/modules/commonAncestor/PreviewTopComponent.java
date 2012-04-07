package ancestris.modules.commonAncestor;

import genj.gedcom.Context;
import genj.gedcom.Indi;
import genj.view.ToolBar;
import ancestris.gedcom.GedcomDirectory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

import ancestris.modules.commonAncestor.graphics.IGraphicsOutput;
import ancestris.modules.commonAncestor.graphics.IGraphicsRenderer;
import ancestris.modules.commonAncestor.graphics.ScreenOutput;

/**
 * Top component which displays ancestor tree preview
 */
@ServiceProvider(service = PreviewTopComponent.class)
public class PreviewTopComponent extends TopComponent implements AncestorListener  {

    private static final String PREFERRED_ID        = "PreviewTopComponent";
    private static final String DOCK_MODE           = "dockMode";
    private static final String SEPARATED_WINDOW    = "separatedWindow";
    //private static final String TITLE_ICON_PATH     = "ancestris/modules/commonAncestor/CommonAncestor.png";
    private PreviewView view;
    AToolBar bar = null;
    private Context context;
    IGraphicsOutput output = new ScreenOutput();
    protected genj.util.Registry registry;
    private String dockMode;
    private boolean separatedWindowFlag = false;
    private boolean isDocking = false;
    SamePanel samePanel;

    /**
     *   factory
     */
    public static synchronized PreviewTopComponent createInstance(SamePanel samePanel) {
        PreviewTopComponent previewTopComponent = null;
        // get current current gedcom
        //Context currentContext = App.center.getSelectedContext(true);
        Context currentContext = GedcomDirectory.getInstance().getLastContext();
        previewTopComponent = new PreviewTopComponent();
        previewTopComponent.init(currentContext, samePanel);
        previewTopComponent.addAncestorListener(previewTopComponent);            
        return previewTopComponent;
    }

    public PreviewTopComponent() {
        super();

    }

    public void init(Context context, SamePanel samePanel) {
        this.context = context;
        this.samePanel = samePanel;

        setName(context.getGedcom().getName());
        setToolTipText(NbBundle.getMessage(CommonAncestorTopComponent.class, "HINT_PreviewTopComponent") + ": " + context.getGedcom().getName());

        // create  layout
        setLayout(new BorderLayout());

        // add view at CENTER
        view = new PreviewView();
        add(view, BorderLayout.CENTER);

        // add tool bar at WEST
        bar = new AToolBar();
        bar.beginUpdate();
        view.populate(bar);
        bar.endUpdate();
        if ((bar != null) && (bar.getToolBar() != null)) {
            bar.setOrientation(SwingConstants.VERTICAL);
            add(bar.getToolBar(), BorderLayout.WEST);
        }
        repaint();

        // set previous docking mode
        registry = new genj.util.Registry(genj.util.Registry.get(PreviewTopComponent.class), getClass().getName());
        // get previous separatedWindowFlag ( default value = true)
        separatedWindowFlag = registry.get(SEPARATED_WINDOW, true);
        // get previous dock mose ( default value = "ancestris-output")
        dockMode= registry.get(DOCK_MODE, "ancestris-output");
        if (separatedWindowFlag == false) {
            dock();
        } else {
            open();
            undock();
        }
    }

    /**
     * dock previewTopComponent in the main window
     */
    public void dock() {
        Mode mode = WindowManager.getDefault().findMode(dockMode);
        if (mode != null) {
            isDocking = true;
            mode.dockInto(this);
            isDocking = false;
        }
        open();
        requestActive();
    }

    /**
     * undock previewTopComponent and show it in a separated window
     * TopComponent API does not provide "undock" method. So I use "ALT-D"
     * event, as human user.
     */
    public void undock() {
        //  sending ALT-D event is in a runnable task because window will be completly opened before it
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // previewTopComponent must be active to receive ALT-D key event
                requestActive();  
                // create  ALT-D key event
                KeyEvent evt = new KeyEvent(PreviewTopComponent.this, KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis() + 100,
                        KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                        KeyEvent.VK_D, 'D');
                // send  ALT-D key event
                PreviewTopComponent.this.dispatchEvent(evt);
                // resize the window if the window is too small
                Dimension dimension = PreviewTopComponent.this.getTopLevelAncestor().getSize();
                if (dimension.width < 600) {
                    dimension.width = 600;
                }
                if (dimension.height < 400) {
                    dimension.height = 400;
                }
                PreviewTopComponent.this.getTopLevelAncestor().setSize(dimension);
            }
        });
    }

    @Override
    public boolean canClose() {
        if ( isDocking == false) {
            // record dockmode and separatedWindowFlag flag        
            registry.put(DOCK_MODE, dockMode);
            registry.put(SEPARATED_WINDOW, separatedWindowFlag);
             // reset reference in parent
            samePanel.onClosePreview();
        }
        return super.canClose();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

//    @Override
//    public Image getIcon() {
//       return ImageUtilities.loadImage(TITLE_ICON_PATH, true);
//    }
//
    public Context getContext() {
        return context;
    }

    /**
     * fill previewTopComponent view with the renderer result
     * 
     * @param indi1
     * @param indi2
     * @param firstIndiDirectLinks
     * @param secondIndiDirectLinks
     * @param displayedId
     * @param displayRecentYears
     * @param husband_or_wife_first 
     */
    public void updatePreView(Indi indi1, Indi indi2, List<Step> firstIndiDirectLinks, List<Step> secondIndiDirectLinks, boolean displayedId, boolean displayRecentYears, int husband_or_wife_first) {
        try {
            IGraphicsRenderer renderer = new Renderer(indi1, indi2, firstIndiDirectLinks, secondIndiDirectLinks, displayedId, displayRecentYears, husband_or_wife_first);
            view.setRenderer(renderer);
            output.output(renderer);
            view.showResult(output.result());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }        
    }

    /**
     * @return  separatedWindowFlag
     */
    public boolean getSeparatedWindowFlag() {
        return separatedWindowFlag;
    }

    /**
     * change window container
     * 
     * @param dockMode the dockMode to set
     */
    public void setSeparatedWindowFlag(boolean separatedWindowFlag) {
        this.separatedWindowFlag = separatedWindowFlag;
        if (separatedWindowFlag == true) {
            undock();
        }else {
            dock();
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    // AncestorListener implementation
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * update dockMode attribute when previewTopComponent dock mode change 
     * @param ae not used
     */
    public void ancestorAdded(AncestorEvent ae) {
        Mode dockModeTemp = WindowManager.getDefault().findMode(this);

        if (dockModeTemp == null || (dockModeTemp != null && dockModeTemp.getName().startsWith("anonymous"))) {
            separatedWindowFlag = true;
        } else {
            separatedWindowFlag = false;
            dockMode = dockModeTemp.getName();
        }
    }

    public void ancestorRemoved(AncestorEvent ae) {
        //System.out.println("ancestorRemoved" + ae.paramString());
    }

    public void ancestorMoved(AncestorEvent ae) {
        //System.out.println("ancestorMoved" + ae.paramString());
    }


    /////////////////////////////////////////////////////////////////////////////
    // AToolBar
    /////////////////////////////////////////////////////////////////////////////
    static private class AToolBar implements ToolBar {

        AtomicBoolean notEmpty = new AtomicBoolean(false);
        JToolBar bar = new JToolBar();

        public JToolBar getToolBar() {
            return (notEmpty.get()) ? bar : null;
        }

        @Override
        public void add(Action action) {
            bar.add(action);
            bar.setVisible(true);
            notEmpty.set(true);
        }

        @Override
        public void add(JComponent component) {
            bar.add(component);
            bar.setVisible(true);
            component.setFocusable(false);
            notEmpty.set(true);
        }

        @Override
        public void addSeparator() {
            bar.addSeparator();
            bar.setVisible(true);
            notEmpty.set(true);
        }

        private void setOrientation(int orientation) {
            bar.setOrientation(orientation);
        }

        @Override
        public void beginUpdate() {
            notEmpty.set(false);
            bar.removeAll();
            bar.setVisible(false);
        }

        @Override
        public void endUpdate() {
        }

        @Override
        public void addGlue() {
            bar.add(Box.createGlue());
        }
    }

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
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
