package ancestris.modules.commonAncestor;

import ancestris.modules.commonAncestor.graphics.IGraphicsOutput;
import ancestris.modules.commonAncestor.graphics.IGraphicsRenderer;
import ancestris.modules.commonAncestor.graphics.ScreenOutput;
import ancestris.swing.ToolBar;
import genj.gedcom.Context;
import genj.gedcom.Indi;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays ancestor tree preview
 */
@ServiceProvider(service = PreviewTopComponent.class)
public class PreviewTopComponent extends TopComponent implements AncestorListener {

    private static final String PREFERRED_ID = "PreviewTopComponent";
    private static final String DOCK_MODE = "dockMode";
    private static final String SEPARATED_WINDOW = "separatedWindow";
    private static final String PREVIEW_ZOOM = "previewZoom";
    private PreviewView view;
    ToolBar bar = null;
    private Context context;
    IGraphicsOutput output = new ScreenOutput();
    protected genj.util.Registry registry;
    private String dockMode;
    private boolean separatedWindowFlag = false;
    private boolean isDocking = false;
    SamePanel samePanel;

    /**
     * factory
     */
    public static synchronized PreviewTopComponent createInstance(SamePanel samePanel) {
        // get current current gedcom
        Context currentContext = null;
        if ( samePanel.getContext() != null ) {
            currentContext = samePanel.getContext();
        } else {
            Context lookupContext = Utilities.actionsGlobalContext().lookup(Context.class);
            if (lookupContext != null) {
                if (lookupContext.getGedcom() != null) {
                    currentContext = lookupContext;
                }
            }
        }

        if( currentContext != null ) {
            if( currentContext.getGedcom() != null ) {
                PreviewTopComponent previewTopComponent = new PreviewTopComponent();
                previewTopComponent.init(currentContext, samePanel);
                previewTopComponent.addAncestorListener(previewTopComponent);
                return previewTopComponent;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public PreviewTopComponent() {
        super();

    }
    
    public void init(Context context, SamePanel samePanel) {
        this.context = context;
        this.samePanel = samePanel;

        setName(context.getGedcom().getDisplayName());
        setToolTipText(NbBundle.getMessage(CommonAncestorTopComponent.class, "HINT_PreviewTopComponent", context.getGedcom().getDisplayName()));
        registry = new genj.util.Registry(genj.util.Registry.get(PreviewTopComponent.class), getClass().getName());

        // create  layout
        setLayout(new BorderLayout());

        // add view at CENTER
        view = new PreviewView();
        add(view, BorderLayout.CENTER);

        // get previous zoom ( default value = 1.0s)
        view.setZoom(Double.valueOf(registry.get(PREVIEW_ZOOM, "1.0")));

        // add tool bar at WEST
        bar = new ToolBar();
        bar.beginUpdate();
        view.populate(bar);
        bar.endUpdate();
        if ((bar != null) && (bar.getToolBar() != null)) {
            bar.setOrientation(SwingConstants.HORIZONTAL);
            add(bar.getToolBar(), BorderLayout.NORTH);
        }
        repaint();

        // set previous docking mode
        // get previous separatedWindowFlag ( default value = true)
        separatedWindowFlag = registry.get(SEPARATED_WINDOW, true);
        // get previous dock mose ( default value = "ancestris-output")
        dockMode = registry.get(DOCK_MODE, "ancestris-output"); // dockmode of preview
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
                // previewTopComponent must be active to receive ALT-U key event (set as undocking in layer.xml of main application)
                requestActive();
                // create  ALT-D key event
                KeyEvent evt = new KeyEvent(PreviewTopComponent.this, KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis() + 100,
                        KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                        KeyEvent.VK_U, 'U');
                // send  ALT-Shift-U key event (for Undock)
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
        if (isDocking == false) {
            // record dockmode and separatedWindowFlag flag        
            registry.put(DOCK_MODE, dockMode);
            registry.put(SEPARATED_WINDOW, separatedWindowFlag);
            // record preview zoom
            registry.put(PREVIEW_ZOOM, Double.toString(view.getZoom()));
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
    public Image getIcon() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/ancestris/modules/commonAncestor/CommonAncestor.png"));
        return icon.getImage();
    }

    public Context getContext() {
        return context;
    }

    /**
     * fill previewTopComponent view with the renderer result
     *
     * @param indi1
     * @        param indi2
     * @        param firstIndiDirectLinks
     * @        param secondIndiDirectLinks
     * @        param displayedId
     * @        param displayRecentYears
     * @        param husband_or_wife_first
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
     * @return separatedWindowFlag
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
        } else {
            dock();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // AncestorListener implementation
    ///////////////////////////////////////////////////////////////////////////
    /**
     * update dockMode attribute when previewTopComponent dock mode change
     *
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
