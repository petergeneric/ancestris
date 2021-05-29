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
package ancestris.awt;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.openide.awt.MouseUtils;

/**
 * "Filtered" MouseAdapter.
 * <ul><li/> If event is single click, the behaviour is unchanged from
 * {@link MouseAdapter}
 * <li/> If double click is detected, then single click event is never
 * processed
 * </ul>
 * <p/>
 * <b>Note:</b>this behaviour is available only by overriding {@link #mouseClickedFiltered(java.awt.event.MouseEvent) }
 * instead of {@link MouseAdapter#mouseClicked(java.awt.event.MouseEvent) }
 *
 * @author daniel
 */
public class FilteredMouseAdapter extends MouseAdapter implements ActionListener {
    private static final int CLICK_INTERVAL = click_interval();
    private Timer timer;
    private MouseEvent lastEvent;

    private static int click_interval(){
        Integer result =(Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
        return result == null?400:result;
    }
    public FilteredMouseAdapter() {
        super();
        Logger.getLogger("ancestris.util").log(Level.FINER, "doubleclic interval: {0}", CLICK_INTERVAL);
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
            mouseClickedFiltered(me);
        } else {
            timer.restart();
        }
    }

    public void actionPerformed(ActionEvent e) {
        mouseClickedFiltered(lastEvent);
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a component.
     * it is not called for single click event if double clic occurred
     *
     * @param me Mouse Event
     */
    public void mouseClickedFiltered(MouseEvent me) {
        super.mouseClicked(me);
    }
}
