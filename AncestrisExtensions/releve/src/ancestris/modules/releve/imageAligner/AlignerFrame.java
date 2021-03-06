package ancestris.modules.releve.imageAligner;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class AlignerFrame extends javax.swing.JFrame {

    static private AlignerFrame currentAlignerFrame = null;            
    
    /**
     * affiche la fenetre pour aligner les images
     */
    static public void showAlignImage() {
        if (currentAlignerFrame == null) {
            currentAlignerFrame = new AlignerFrame();            
        }

        currentAlignerFrame.toFront();
        currentAlignerFrame.setVisible(true);
    }
    
    /**
     * affiche la fenetre pour aligner les images
     */
    static public void closeAlignImage() {
        if (currentAlignerFrame != null) {
            currentAlignerFrame.closeComponent(); 
            currentAlignerFrame = null;
        }
    }
    
    /**
     * Cree une nouvelle fenetre
     * Recupere la taille et la position de la session précédente
     *
     * Remarque : A sa fermeture (windowClosing) la fenetre enregistre sa taille
     * et sa position et appelle ReleveTopComponent.setStandaloneEditor(false)
     * pour signaler sa fermeture
     */
    private AlignerFrame() {
        setTitle(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerOptionsPanel.border.title"));
        
        initComponents();

        ImageIcon icon = new ImageIcon(AlignerFrame.class.getResource("/ancestris/modules/releve/images/Releve.png"));
        setIconImage(icon.getImage());
        
        // je configure la taille de la fenetre
        String size = NbPreferences.forModule(AlignerFrame.class).get("AlignerFrameSize", "300,450,0,0");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        String[] dimensions = size.split(",");
        Rectangle browserFrameBounds = new Rectangle();
    
        if ( dimensions.length >= 4 ) {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            int x = Integer.parseInt(dimensions[2]);
            int y = Integer.parseInt(dimensions[3]);
            if ( width < 100 ) {
                width = 100;
            }
            if ( height < 100 ) {
                height = 100;
            }
            if ( x < 10 || x > screen.width -10) {
                x = (screen.width / 2) - (width / 2);
            }
            if ( y < 10 || y > screen.height -10) {
                y = (screen.height / 2) - (height / 2);
            }
            browserFrameBounds.setBounds(x, y, width, height);
        } else {
            browserFrameBounds.setBounds(screen.width / 2 -100, screen.height / 2- 100, 300, 450);
        }

        // j'applique la taille de la fenetre avant de dimensionner jSplitPane1
        setBounds(browserFrameBounds);

        // listener pour intercepter l'evenement de fermeture de la fenetre.

        addWindowListener( new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                componentClosed();
            }
        });

    }

     /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void closeComponent() {
       componentClosed();
       dispose();
    }

    /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void componentClosed() {
        // j'affiche la fenetre dans le mode normal pour récuperer la
        // position et la taille
        if (getExtendedState() != JFrame.NORMAL) {
            setExtendedState(JFrame.NORMAL);
        }
        // j'enregistre la taille dans les preferences
        String size;
        size = String.valueOf(this.getWidth()) + ","
                + String.valueOf(this.getHeight()) + ","
                + String.valueOf(this.getLocation().x) + ","
                + String.valueOf(this.getLocation().y);
        
        NbPreferences.forModule(AlignerFrame.class).put("AlignerFrameSize", size);
        
        alignerPanel1.componentClosed();
        this.setVisible(false);
        this.dispose();
    }

   
    

    

    /**
     * initialise le titre de la fenêtre
     * @param fileName
     */
    @Override
    public void setTitle(String fileName) {
        super.setTitle(fileName);
    }




    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        alignerPanel1 = new ancestris.modules.releve.imageAligner.AlignerPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        getContentPane().add(alignerPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        alignerPanel1.frameUpdated();
    }//GEN-LAST:event_formWindowStateChanged

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        alignerPanel1.frameUpdated();
    }//GEN-LAST:event_formComponentResized

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.releve.imageAligner.AlignerPanel alignerPanel1;
    // End of variables declaration//GEN-END:variables

}
