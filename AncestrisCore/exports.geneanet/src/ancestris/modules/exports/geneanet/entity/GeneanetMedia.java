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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity to store Media structure from Geneanet.
 *
 * @author Zurga
 */
public class GeneanetMedia {

    private String pathName;
    private final List<GenenaetIndiId> ids = new ArrayList<>();
    private String depositId;
    private String viewsId;
    private File fichier;
    private String title;
    private GeneanetMediaTypeEnum type;
    private int prive = 0;

    public GeneanetMedia(String pathName, List<GenenaetIndiId> ids) {
        this.pathName = pathName;
        this.ids.addAll(ids);
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public List<GenenaetIndiId> getIds() {
        return ids;
    }

    public void setIds(List<GenenaetIndiId> ids) {
        this.ids.clear();
        this.ids.addAll(ids);
    }

    public String getDepositId() {
        return depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }

    public String getViewsId() {
        return viewsId;
    }

    public void setViewsId(String viewsId) {
        this.viewsId = viewsId;
    }

    public File getFichier() {
        return fichier;
    }

    public void setFichier(File fichier) {
        this.fichier = fichier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GeneanetMediaTypeEnum getType() {
        return type;
    }

    public void setType(GeneanetMediaTypeEnum type) {
        this.type = type;
    }

    public int getPrive() {
        return prive;
    }

    public void setPrive(int prive) {
        this.prive = prive;
    }
}
