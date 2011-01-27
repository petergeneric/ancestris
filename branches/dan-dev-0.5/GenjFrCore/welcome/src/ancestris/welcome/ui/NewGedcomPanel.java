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

import ancestris.api.newgedcom.NewGedcom;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.Constants;
import ancestris.welcome.content.LinkButton;
import ancestris.welcome.content.Utils;
import genjfr.app.ActionNew;
import javax.swing.AbstractAction;
import org.openide.util.Lookup;

/**
 *
 * @author S. Aubrecht
 */
class NewGedcomPanel extends JPanel implements Constants {

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
                NewGedcom wiz = (NewGedcom)Lookup.getDefault().lookup(NewGedcom.class);
                if (wiz != null && wiz.create() != null){
                } else {
                    new ActionNew().actionPerformed(e); //NOI18N
                }
            }
        };
        b.setFont(GET_STARTED_FONT);
        add(b, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
        add(new JLabel(description), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(20, 9, 5, 5), 0, 0));
        add(new JLabel(), new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }
}
