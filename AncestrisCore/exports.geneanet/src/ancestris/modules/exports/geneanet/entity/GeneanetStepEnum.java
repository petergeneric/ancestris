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

/**
 * Enum to store step values.
 *
 * @author Zurga
 */
public enum GeneanetStepEnum {
    PREPARE("prepare"),
    UPDATE_TREE("update_tree"),
    RESYNC_TREE("resync_tree"),
    VERIFY_LINKS("verify_links"),
    INDEX_TREE("index_tree");

    private String step;

    private GeneanetStepEnum(String ste) {
        step = ste;
    }
    
    public int getStep() {
        switch(this) {
            case PREPARE : return 1;
            case UPDATE_TREE : return 2;
            case RESYNC_TREE : return 3;
            case VERIFY_LINKS : return 4;
            case INDEX_TREE : return 5;
        }
        return 1;
    }
    
    public String getStepName() {
        return step;
    }
}
