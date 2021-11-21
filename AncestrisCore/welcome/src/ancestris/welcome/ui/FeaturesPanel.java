/**
 * Ancestris
 *
 * Copyright (C) 2010 - 2018 Ancestris Team <frederic@ancestris.org>
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

import ancestris.app.FlyerAction;
import ancestris.modules.donation.DonationAction;
import ancestris.tour.TourAction;
import ancestris.util.swing.DialogManager;
import ancestris.welcome.WelcomeOptions;
import ancestris.welcome.content.BundleSupport;
import static ancestris.welcome.content.BundleSupport.BUNDLE_NAME;
import ancestris.welcome.content.Constants;
import static ancestris.welcome.content.Constants.COLOR_HEADER;
import static ancestris.welcome.content.Constants.GET_STARTED_FONT;
import ancestris.welcome.content.LinkButton;
import ancestris.welcome.content.Utils;
import genj.io.FileAssociation;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Frederic Lapeyre
 */
class FeaturesPanel extends JPanel implements Constants {

    private final Image ICON1 = ImageUtilities.loadImage("ancestris/welcome/resources/ico_flyer.png"); //NOI18N
    private final Image ICON2 = ImageUtilities.loadImage("ancestris/welcome/resources/ico_tour.png"); //NOI18N
    private final Image ICON3 = ImageUtilities.loadImage("ancestris/welcome/resources/ico_asso.png"); //NOI18N
    private final Image ICON4 = ImageUtilities.loadImage("ancestris/welcome/resources/ico_donation.png"); //NOI18N
    
    public FeaturesPanel() {
        super(new GridBagLayout());
        setOpaque(false);
        addShowFlyer(BundleSupport.getLabel("Flyer"), ICON1, new FlyerAction());
        addShowFlyer(BundleSupport.getLabel("Tour"), ICON2, new ShowTour());
        addShowFlyer(BundleSupport.getLabel("Asso"), ICON3, new ShowAsso());
        addShowFlyer(BundleSupport.getLabel("Donation"), ICON4, new DonationAction());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (WelcomeOptions.getDefault().isFirstStarts()) {
                    String title = NbBundle.getMessage(WelcomeOptions.class, "LBL_Tab_Title");
                    String question = BundleSupport.getLabel("TourQuestion");
                    Object o = DialogManager.create(title, question)
                            .setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).show();
                    if (o.equals(DialogManager.OK_OPTION)) {
                        new ShowTour().actionPerformed(null);
                    }
                }
            }
        });
    }

    private void addShowFlyer(String label, Image icon, final ActionListener action) {
        LinkButton b = new LinkButton(label, Utils.getColor(COLOR_HEADER), true, label) { //NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                logUsage();
                action.actionPerformed(e);
            }
        };
        b.setFont(GET_STARTED_FONT);
        b.setIcon(new ImageIcon(icon));
        add(b);
        add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    private class ShowAsso implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                FileAssociation.getDefault().execute(new URL(NbBundle.getMessage(DonationAction.class, "donation_link")));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public class ShowTour implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            
            boolean supported = true;
            Logger LOG = Logger.getLogger("ancestris.welcome");

            // Determine if the GraphicsDevice supports translucency.
            GraphicsEnvironment graphenv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice graphdev = graphenv.getDefaultScreenDevice();

            //If translucent windows aren't supported, exit.
            if (!graphdev.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                LOG.info("Guided Tour : translucency not supported. Will use opaque windows instead.");  
                //supported = false; // "Showing internet tour... " // instead ?
            }

            if (supported) {
                new TourAction().actionPerformed(event);
            } else {
                try {
                    FileAssociation.getDefault().execute(new URL(NbBundle.getBundle(BUNDLE_NAME).getString("WelcomePage/GettingStartedLinks/tour.url.target")));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
}
