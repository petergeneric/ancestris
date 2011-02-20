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
package ancestris.util;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;
import org.openide.awt.MouseUtils;

/**
 * MouseAdapter "filtrant" cad:
 * <ul><li> si simple click => fonctionnement normal</li>
 * <li> si double clic: le simple clic n'est pas envoyé</li>
 * </ul><p/>Il faut surcharger filteredMouseClicked à la place de mouseClicked
 * @author daniel
 */
public class FilteredMouseAdapter extends MouseAdapter implements ActionListener {

    private static final int CLICK_INTERVAL = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
    private Timer timer;
    private MouseEvent lastEvent;

    public FilteredMouseAdapter() {
        super();
        timer = new Timer(CLICK_INTERVAL, this);
        timer.setRepeats(false);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        this.lastEvent = me;
        if (MouseUtils.isDoubleClick(me)) {
            if (timer != null) {
                timer.stop();
            }
            filteredMouseClicked(me);
        } else {
            timer.restart();
        }
    }

    public void actionPerformed(ActionEvent e) {
        filteredMouseClicked(lastEvent);
    }

    public void filteredMouseClicked(MouseEvent me) {
        super.mouseClicked(me);
    }
}
