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

import ancestris.modules.exports.geneanet.GeneanetSynchronizePanel;
import ancestris.modules.exports.geneanet.entity.GeneanetMedia;
import ancestris.modules.exports.geneanet.entity.GeneanetMediaTypeEnum;
import ancestris.modules.exports.geneanet.entity.GeneanetToken;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import org.openide.util.NbBundle;

/**
 * Worker to send media.
 * @author Zurga
 */
public class GeneanetMediaWorker implements Runnable {
    
    private final static Logger LOG = Logger.getLogger("ancestris.app", null);
    
    private final GeneanetQueueManager queueManager;
    private final GeneanetToken theToken;
    private final Set<String> mediaAlreadySentList;
    private final JLabel nbEncoursFile;
    private JLabel encoursFile;
    
    public GeneanetMediaWorker(GeneanetQueueManager gqm, GeneanetToken token, Set<String> mediaAlreadySentList, JLabel nbEncoursFile, JLabel encoursFile) {
        queueManager = gqm;
        theToken = token;
        this.mediaAlreadySentList = mediaAlreadySentList;
        this.nbEncoursFile = nbEncoursFile;
        this.encoursFile = encoursFile;
        
    }

    @Override
    public void run() {
        try {
            while(true) {
                GeneanetMedia media = queueManager.takeMedia();
                if (media.getType() == GeneanetMediaTypeEnum.STOP) {
                    queueManager.putUpdate("STOP");
                    return;
                }
                nbEncoursFile.setText(String.valueOf(queueManager.getCurentNb()));
                encoursFile.setText(media.getPathName());
                try {
                    GeneanetUtil.sendMedia(theToken, media);
                    GeneanetUtil.referenceMedia(theToken, media);
                    mediaAlreadySentList.add(media.getPathName());
                } catch (GeneanetException e) {
                    LOG.log(Level.INFO, "Error with media : " + media.getPathName(), e);
                    queueManager.putUpdate(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.deposit.error") + " " + media.getPathName());
                }
            }
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Interrupted worker.", e);
        }
        
    }
    
    
}
