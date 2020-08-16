/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.donation;

import ancestris.util.swing.DialogManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class DonationAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        String title = NbBundle.getMessage(DonationAction.class, "CTL_DonationAction").replace("&", "");
        DonationPanel panel = new DonationPanel();
        
        DialogManager.create(title, panel).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).setResizable(false).show();
    }

    
}
