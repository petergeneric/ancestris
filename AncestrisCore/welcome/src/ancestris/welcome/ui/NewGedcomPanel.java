/**
 * Ancestris
 *
 * Copyright (C) 2010 - 2011 Ancestris Team <dev@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package ancestris.welcome.ui;

import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.Constants;
import ancestris.welcome.content.LinkButton;
import ancestris.welcome.content.Utils;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht & Frederic Lapeyre
 */
class NewGedcomPanel extends JPanel implements Constants {

    private final Image ICON = ImageUtilities.loadImage("ancestris/welcome/resources/ico_new.png"); //NOI18N
    
    public NewGedcomPanel() {
        super(new GridBagLayout());
        setOpaque(false);
        addNewGedcom(BundleSupport.getLabel("NewGedcom"), BundleSupport.getLabel("NewGedcomDescr"));
    }

    private void addNewGedcom(String label, String description) {
        LinkButton b = new LinkButton(label, Utils.getColor(COLOR_HEADER), true, "NewGedcom") { //NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                logUsage();
                new NewGedcomAction().actionPerformed(e);
            }
        };
        b.setFont(GET_STARTED_FONT);
        b.setIcon(new ImageIcon(ICON));
        add(b);
        JLabel jDesc = new JLabel(description);
        jDesc.setBorder( BorderFactory.createEmptyBorder(5,9,2,0) );
        add(jDesc);
    }
}
