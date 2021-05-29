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

import ancestris.modules.exports.geneanet.entity.GeneanetMedia;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Queue Manager for the workers.
 *
 * @author Zurga
 */
public class GeneanetQueueManager {

    private final BlockingQueue<GeneanetMedia> mediaToSend = new ArrayBlockingQueue<>(50);
    private final BlockingQueue<String> updateText = new ArrayBlockingQueue<>(100);
    private long nbMedia = 1;
    
    public void putMedia(GeneanetMedia media) throws InterruptedException {
        mediaToSend.put(media);
    }
    
    public GeneanetMedia takeMedia() throws InterruptedException {
        return mediaToSend.take();
    }
    
    public void putUpdate(String update) throws InterruptedException {
        updateText.put(update);
    }
    
    public String takeText() throws InterruptedException {
        return updateText.take();
    }
    
    public int countMedia() {
        return mediaToSend.size();
    }
    
    public int countUpdate() {
        return updateText.size();
    }
    
    public long getCurentNb() {
        return nbMedia++;
    }
}
