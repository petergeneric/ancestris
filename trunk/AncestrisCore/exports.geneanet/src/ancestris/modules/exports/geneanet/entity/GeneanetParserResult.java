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
package ancestris.modules.exports.geneanet.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity to store Geneanet media parsering
 * @author Zurga
 */
public class GeneanetParserResult {
    
    private final List<GeneanetMedia> okMedia = new ArrayList<>();
    private final List<String> koMedia = new ArrayList();
    private int nbMedia;
    private int nbIndi;

    public GeneanetParserResult(int nbMedia, int nbIndi) {
        this.nbMedia = nbMedia;
        this.nbIndi = nbIndi;
    }

    public List<GeneanetMedia> getOkMedia() {
        return okMedia;
    }

    public List<String> getKoMedia() {
        return koMedia;
    }

    public int getNbMedia() {
        return nbMedia;
    }

    public int getNbIndi() {
        return nbIndi;
    }

    public void setOkMedia(List<GeneanetMedia> okMedia) {
        this.okMedia.clear();
        this.okMedia.addAll(okMedia);
    }
    
    public void addOkMedia(GeneanetMedia media) {
        okMedia.add(media);
    }

    public void setKoMedia(List<String> koMedia) {
        this.koMedia.clear();
        this.koMedia.addAll(koMedia);
    }
    
    public void addKoMedia(String media) {
        koMedia.add(media);
    }

    public void setNbMedia(int nbMedia) {
        this.nbMedia = nbMedia;
    }

    public void setNbIndi(int nbIndi) {
        this.nbIndi = nbIndi;
    }
    
    
    
}
