package ancestris.modules.commonAncestor;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component for Common Ancestor panel
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class CommonAncestorTopComponent extends AncestrisTopComponent {

    private static final String PREFERRED_ID = "CommonAncestorTopComponent";
    private SamePanel samePanel = null;
    
    public static void createInstance(Context context) {
        CommonAncestorTopComponent ca = new CommonAncestorTopComponent();
        ca = (CommonAncestorTopComponent) ca.create(context);
        ca.init(context);
        if (!ca.isOpen) {
            ca.open();
        }
        ca.requestActive();
    }
    
    @Override
    public String getAncestrisDockMode() {
        return getDefaultMode();
    }

    public String getDefaultMode() {
        return AncestrisDockModes.PROPERTIES;
    }

    public boolean isSingleView() {
        return true;
    }

    @Override
    public boolean createPanel() {
        Context context = getContext();
        if (context != null && context.getGedcom() != null) {
            initComponents();
            if (samePanel == null) {
               samePanel = new SamePanel();
            }
            samePanel.init(context);
            samePanel.updateCurrentIndividu(context.getEntity());
            jScrollPane.setViewportView(samePanel);
            setPanel(jScrollPane);
        }
        return true;
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage("ancestris/modules/commonAncestor/CommonAncestor.png", true);
    }
    
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }    

    @Override
    public void componentClosed() {
        if (samePanel != null) {
            samePanel.closePreview();
            samePanel.onClosePreview();
        }
        super.componentClosed();
    }

    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables
}
