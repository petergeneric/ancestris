/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import org.openide.util.NbBundle;

/**
 *
 * @author Zurga
 */
public class ColorChooserButton extends JButton {

    private Color current;
     private final List<ColorChangedListener> listeners = new ArrayList<>();
    
    /**
     * Default Constructor for Netbeans design editor.
     */
    public ColorChooserButton() {
        this(Color.GREEN);
    }

    public ColorChooserButton(Color c) {
        setSelectedColor(c);
        addActionListener((ActionEvent arg0) -> {
            Color newColor = JColorChooser.showDialog(null, NbBundle.getMessage(ColorChooserButton.class, "ColorChooser.title"), current);
            setSelectedColor(newColor);
        });
    }

    public final Color getSelectedColor() {
        return current;
    }

    public final void setSelectedColor(Color newColor) {
        setSelectedColor(newColor, true);
    }

    public void setSelectedColor(Color newColor, boolean notify) {

        if (newColor == null) {
            return;
        }

        current = newColor;
        setIcon(createIcon(current, 16, 16));
        repaint();

        if (notify) {
            // Notify everybody that may be interested.
            listeners.forEach((l) -> {
                l.colorChanged(newColor);
            });
        }
    }

    public void addColorChangedListener(ColorChangedListener toAdd) {
        listeners.add(toAdd);
    }

    public static ImageIcon createIcon(Color main, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width - 1, height - 1);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
}
