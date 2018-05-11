/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.calculatrice;

import java.util.logging.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.calculatrice//EN",
autostore = false)
public final class CalcTopComponent extends TopComponent {

    private static CalcTopComponent instance;
    /** path to the icon used by the component and its open action */
    private static final String ICON_PATH = "ancestris/modules/calculatrice/Calc.png";
    private static final String PREFERRED_ID = "CalcTopComponent";
    private Calculator calculator = null;

    public CalcTopComponent() {
        initCalcPanel();
        setName(NbBundle.getMessage(CalcTopComponent.class, "CTL_CalcTopComponent"));
        //setToolTipText(NbBundle.getMessage(CalcTopComponent.class, "HINT_CalcTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

    }

    private void initCalcPanel() {
        calculator = new Calculator();

        if (calculator == null) {
            return;
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(calculator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(calculator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 312, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized CalcTopComponent getDefault() {
        if (instance == null) {
            instance = new CalcTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the CalcTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized CalcTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(CalcTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof CalcTopComponent) {
            return (CalcTopComponent) win;
        }
        Logger.getLogger(CalcTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        //return TopComponent.PERSISTENCE_ALWAYS;
        return TopComponent.PERSISTENCE_NEVER;   
    }

    @Override
    public void open() {
         Mode m = WindowManager.getDefault().findMode ("ancestris-modules-calculatrice");
         if (m != null) {
            m.dockInto(this);
         }
         super.open();
    }

    @Override
    public void componentOpened() {
        if (calculator != null) {
            calculator.setFocus();
        }
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        //p.setProperty("version", "1.0");
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
//        String version = p.getProperty("version");
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
