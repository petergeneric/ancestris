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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.Constants;
import ancestris.welcome.content.LinkButton;
import ancestris.welcome.content.Utils;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht & Frederic Lapeyre
 */
class PreferencesPanel extends JPanel implements Constants {

    private final Image ICON = ImageUtilities.loadImage("ancestris/welcome/resources/ico_settings.png"); //NOI18N
    
    public PreferencesPanel() {
        super( new GridBagLayout() );
        setOpaque(false);
        addPreferences(BundleSupport.getLabel("Preferences"), BundleSupport.getLabel("PreferencesDescr"));
    }

    private void addPreferences( String label, String description ) {
        LinkButton b = new LinkButton(label, Utils.getColor(COLOR_HEADER), true, "GoPreferences") { //NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                logUsage();
                new ShowPreferencesAction("available").actionPerformed(e); //NOI18N
            }
        };
        b.setFont(GET_STARTED_FONT);
        b.setIcon(new ImageIcon(ICON));
        add(b);
        JLabel jDesc = new JLabel(description);
        jDesc.setBorder( BorderFactory.createEmptyBorder(5,9,2,0) );
        add(jDesc);
    }

    private static class ShowPreferencesAction extends AbstractAction {
        //TODO: voir pour ouvrir le bon tab dans les options
        private final String initialTab;
        public ShowPreferencesAction(String initialTab) {
            super( BundleSupport.getLabel( "GoPreferences" ) ); //NOI18N
            this.initialTab = initialTab;
        }
    public void actionPerformed(ActionEvent evt) {
        OptionsDisplayer.getDefault().open();
    }
}
}
