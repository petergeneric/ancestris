/*
 * Copyright (C) 2020 Zurga
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.exports.geneanet.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author Zurga
 */
public class GeneanetLogWorker implements Runnable {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private final GeneanetQueueManager gqm;
    private final JTextArea updateArea;
    private int nbWorker;

    public GeneanetLogWorker(GeneanetQueueManager gqm, JTextArea updateZone, int nbWorker) {
        this.gqm = gqm;
        this.updateArea = updateZone;
        this.nbWorker = nbWorker;

    }

    @Override
    public void run() {
        int i = 0;
        try {
            while (i < nbWorker) {
                String update = gqm.takeText();
                if ("STOP".equals(update)) {
                    i++;
                    continue;
                }
                updateArea.append(update);
                updateArea.append("\n");
            }
            LOG.log(Level.INFO, "Log terminated");
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Interrupted worker.", e);
        }
    }

}
