package ancestris.modules.editors.genealogyeditor.beans;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class ImageBean extends javax.swing.JPanel {

    private String[] genders = new String[] {"unknown", "male", "female"};
    private boolean isDefault = true;
    private Image loadImage = null;
    private Image scaledImage = null;

    /**
     * Creates new form ImageBean
     */
    public ImageBean() {
        super();
        try {
            loadImage = ImageIO.read(ImageBean.class.getResourceAsStream("/ancestris/modules/editors/genealogyeditor/resources/profile_" + genders[0] + ".png"));
            isDefault = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(null);
        setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/beans/Bundle").getString("ImageBean.toolTipText"), new Object[] {})); // NOI18N
        setMinimumSize(new java.awt.Dimension(150, 200));
        setName(org.openide.util.NbBundle.getMessage(ImageBean.class, "ImageBean.name")); // NOI18N
        setPreferredSize(new java.awt.Dimension(150, 200));
        setRequestFocusEnabled(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        if (loadImage != null) {
            if (getWidth() < getHeight()) {
                scaledImage = loadImage.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
            } else {
                scaledImage = loadImage.getScaledInstance(-1, getHeight(), Image.SCALE_DEFAULT);
            }

            repaint();
        }
    }//GEN-LAST:event_formComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    public void setImage(File file, int defaultGender) {
        InputStream imageInputStream;

        if (file != null && file.exists()) {
            try {
                imageInputStream = new FileInputStream(file);
                loadImage = ImageIO.read(imageInputStream);
                // FIXME: We should display some icon if file cannot be read as image
                if (loadImage != null){
                    if (getWidth() > 0 && getWidth() < getHeight()) {
                        scaledImage = loadImage.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
                    } else if (getHeight() > 0) {
                        scaledImage = loadImage.getScaledInstance(-1, getHeight(), Image.SCALE_DEFAULT);
                    }
                }
                isDefault = false;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            imageInputStream = ImageBean.class.getResourceAsStream("/ancestris/modules/editors/genealogyeditor/resources/profile_" + genders[defaultGender] + ".png");
            try {
                loadImage = ImageIO.read(imageInputStream);
                if (getWidth() > 0 && getWidth() < getHeight()) {
                    scaledImage = loadImage.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
                } else if (getHeight() > 0) {
                    scaledImage = loadImage.getScaledInstance(-1, getHeight(), Image.SCALE_DEFAULT);
                }
                isDefault = true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        repaint();
    }

    public void setImage(byte[] imageData, int defaultGender) {
        if (imageData != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);

            try {
                loadImage = ImageIO.read(bais);
                if (getWidth() > 0 && getWidth() < getHeight()) {
                    scaledImage = loadImage.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
                } else if (getHeight() > 0) {
                    scaledImage = loadImage.getScaledInstance(-1, getHeight(), Image.SCALE_DEFAULT);
                }
                isDefault = false;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            try {
                loadImage = ImageIO.read(ImageBean.class.getResourceAsStream("/ancestris/modules/editors/genealogyeditor/resources/profile_" + genders[defaultGender] + ".png"));
                if (getWidth() > 0 && getWidth() < getHeight()) {
                    scaledImage = loadImage.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
                } else if (getHeight() > 0) {
                    scaledImage = loadImage.getScaledInstance(-1, getHeight(), Image.SCALE_DEFAULT);
                }
                isDefault = true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scaledImage != null) {
            ((Graphics2D) g).drawImage(scaledImage, 0 + ((getWidth() - scaledImage.getWidth(this)) / 2), ((getHeight() - scaledImage.getHeight(this)) / 2), null);
        }
    }
    
    public boolean isDefault() {
        return isDefault;
    }
}
