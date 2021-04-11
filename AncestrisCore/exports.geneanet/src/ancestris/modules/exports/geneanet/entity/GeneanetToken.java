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

import java.util.Calendar;
import java.util.Date;

/**
 * Class for managing Geneanet Token and keep track of expiring time.
 *
 * @author Zurga
 */
public class GeneanetToken {

    private String token;
    private String refreshToken;
    private final String clientId;
    private final String secretId;

    private Date beginning;

    public GeneanetToken(String tok, Date begin, String client, String secret) {
        token = tok;
        beginning = begin;
        clientId = client;
        secretId = secret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getBeginning() {
        return beginning;
    }

    public void setBeginning(Date beginning) {
        this.beginning = beginning;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSecretId() {
        return secretId;
    }
    
    /**
     * Token is expired when one hour is passed from the beginning time.
     * @return true if token expired, false in other cases.
     */
    public boolean isExpired() {
        final Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(beginning);
        currentCal.add(Calendar.SECOND, 3600);
        return currentCal.getTimeInMillis() < System.currentTimeMillis();
    }
}
