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
import ancestris.modules.exports.geneanet.entity.GenenaetIndiId;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyForeignXRef;
import genj.io.InputSource;
import genj.io.input.ByteInput;
import genj.io.input.FileInput;
import genj.io.input.URLInput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.openide.util.NbBundle;
/**
 * Create Media needed to send.
* @author Zurga
 */
public class GeneanetMediaProducer implements Runnable {
     private final static Logger LOG = Logger.getLogger("ancestris.app", null);
    
    private final GeneanetQueueManager queueManager;
    private final Set<String> mediaAlreadySentList;
    private final List<GeneanetMedia> okMediaList;
    private final Gedcom currentGedcom;
    private int nbWorker;

    public GeneanetMediaProducer(GeneanetQueueManager queueManager, Set<String> mediaAlreadySentList, List<GeneanetMedia> okMediaList, Gedcom currentGedcom, int nbWorker) {
        this.queueManager = queueManager;
        this.mediaAlreadySentList = mediaAlreadySentList;
        this.okMediaList = okMediaList;
        this.currentGedcom = currentGedcom;
        this.nbWorker = nbWorker;
    }

    @Override
    public void run() {
        final List<PropertyFile> props = (List<PropertyFile>) currentGedcom.getPropertiesByClass(PropertyFile.class);
        // Create Map to get easily the current media.
       Map<String, Property> mapMedia = new HashMap<>(props.size());
        for (Property prop : props) {
            mapMedia.put(prop.getValue(), prop);
        }
        for (GeneanetMedia okMedia : okMediaList) {
            updateIds(okMedia);
            try {
               if (mediaAlreadySentList.contains(okMedia.getPathName())) {
                    // File Already sent previously
                    continue;
                }
                Property media = mapMedia.get(okMedia.getPathName());
                if (!(media instanceof PropertyFile)) {
                    queueManager.putUpdate(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.deposit.error") + " " + okMedia.getPathName());
                    LOG.log(Level.INFO, "Unable to send this media :" + media.toString());
                    continue;
                }
                Property titles = media.getProperty("TITL");
                if (titles == null) {
                    final Property parent = media.getParent();
                    if (parent != null) {
                        titles = parent.getProperty("TITL");
                    }
               }
                if (titles != null) {
                    final String titre = titles.getValue();
                    if (titre.length() > 50) {
                        okMedia.setTitle(titre.substring(0, 50));
                    } else {
                        okMedia.setTitle(titre);
                    }
                }

                final Property types = media.getProperty("_GENEANET_TYPE");
                if (types != null) {
                    okMedia.setType(GeneanetMediaTypeEnum.getValue(types.getValue()));
                } else {
                    okMedia.setType(getType(media));
                }

                final Property form = media.getProperty("FORM");
                if (form != null) {
                    okMedia.setForm(form.getValue());
                }

                //Get File
                PropertyFile pFile = (PropertyFile) media;
                Optional<InputSource> input = pFile.getInput();
                if (!input.isPresent()) {
                    queueManager.putUpdate(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.deposit.error") + " " + okMedia.getPathName());
                    LOG.log(Level.INFO, "No input detected for media :" + okMedia.getPathName());
                    continue;
                }

                InputSource source = input.get();
                if (source instanceof FileInput) {
                    okMedia.setFichier(((FileInput) source).getFile());
                } else if (source instanceof URLInput || source instanceof ByteInput) {
                    try {
                        File localTempFile = File.createTempFile("AncTemp", null);
                        FileUtils.copyInputStreamToFile(source.open(), localTempFile);
                        localTempFile.deleteOnExit();
                        okMedia.setFichier(localTempFile);
                    } catch (IOException e) {
                        queueManager.putUpdate(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.deposit.error") + " " + okMedia.getPathName());
                        LOG.log(Level.INFO, "Unable to download remote file : " + okMedia.getPathName(), e);
                        continue;
                    }
                } else {
                    queueManager.putUpdate(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.deposit.error") + " " + okMedia.getPathName());
                    LOG.log(Level.INFO, "Unable to get file : " + okMedia.getPathName());
                    continue;
                }

                try {
                    queueManager.putMedia(okMedia);
                } catch (InterruptedException e) {
                    LOG.log(Level.INFO, "Error with media : " + okMedia.getPathName(), e);
                    queueManager.putUpdate(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.deposit.error") + " " + okMedia.getPathName());
                }
            } catch (InterruptedException | MissingResourceException t) {
                LOG.log(Level.INFO, "throwable with media : " + okMedia.getPathName(), t);
                
            }
        }
        try {
            GeneanetMedia poison = new GeneanetMedia("", new ArrayList<>());
            poison.setType(GeneanetMediaTypeEnum.STOP);
            for (int i = 0; i < nbWorker; i++) {
                queueManager.putMedia(poison);
            }
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "throwable with poison ", e);
        }
        LOG.log(Level.INFO, "Media list completed");
    }
    
      private void updateIds(GeneanetMedia okMedia) {
        for (GenenaetIndiId id : okMedia.getIds()) {
            Entity e = currentGedcom.getEntity("INDI", id.getId().replace("@", ""));
            if (e != null && e instanceof Indi) {
                Indi indi = (Indi) e;
                id.setFirstName(indi.getFirstName());
                id.setLastName(indi.getLastName());
            }
        }
    }

    private GeneanetMediaTypeEnum getType(Property media) {
        if (media.getEntity() instanceof Media) {
            Media obje = (Media) media.getEntity();
            if (obje.isConnected()) {
                for (PropertyForeignXRef p : obje.getProperties(PropertyForeignXRef.class)) {
                    if (p.getTarget() != null && p.getTarget().getParent() != null && "INDI".equals(p.getTarget().getParent().getTag())) {
                        return GeneanetMediaTypeEnum.PORTRAITS;
                    }
                }
            }
        } else {
            Property parent = media.getParent();
            if (parent != null && "OBJE".equals(parent.getTag())) {
                Property gParent = parent.getParent();
                if ("INDI".equals(gParent.getTag())) {
                    return GeneanetMediaTypeEnum.PORTRAITS;
                }
            }
        }

        return GeneanetMediaTypeEnum.ETAT_CIVIL;
   }
    
}
