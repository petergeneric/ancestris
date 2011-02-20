/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard;

import genj.gedcom.Context;
import genjfr.app.AncestrisTopComponent;
import genjfr.app.App;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class OpenAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Context contextToOpen = App.center.getSelectedContext(true);
        if (contextToOpen != null) {
            AncestrisTopComponent win = new EditTopComponent().create();
            win.init(contextToOpen);
            win.open();
            win.requestActive();
        }
    }
}
