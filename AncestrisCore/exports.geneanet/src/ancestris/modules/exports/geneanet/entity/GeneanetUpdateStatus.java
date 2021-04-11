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
 * Objet to store status of action.
 * @author Zurga
 */
public class GeneanetUpdateStatus {
    
    private GeneanetStatusEnum status;
    private String action;
    private GeneanetStepEnum step;

    public GeneanetUpdateStatus(String status, String action, String step) {
        this.status = GeneanetStatusEnum.valueOf(status.toUpperCase());
        this.action = action;
        this.step = GeneanetStepEnum.valueOf(step.toUpperCase());
        if (this.step == null){
            this.step = GeneanetStepEnum.PREPARE;
        }
    }

    public GeneanetStatusEnum getStatus() {
        return status;
    }

    public void setStatus(GeneanetStatusEnum status) {
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public GeneanetStepEnum getStep() {
        return step;
    }

    public void setStep(GeneanetStepEnum step) {
        this.step = step;
   }
    
    
    
}
