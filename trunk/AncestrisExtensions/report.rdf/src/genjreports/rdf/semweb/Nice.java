// @formatter:off
/*
 * Copyright 2012, J. Pol
 *
 * This file is part of free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 *
 * This package is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details. A copy of the GNU General Public License is
 * available at <http://www.gnu.org/licenses/>.
 */
// @formatter:on
package genjreports.rdf.semweb;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Nice
{
    private static final Logger logger = Logger.getLogger(Mashup.class.getName());

    private static Map<String,Date> hostLastTimeMap = new HashMap<String,Date>();
    private static Map<String,Long> hostIntervalMap = new HashMap<String,Long>();
    private static final long defaultInterval = 5000;

    public static void setInterval(final String host, long milis)
    {
        hostIntervalMap.put(host,milis);
    }

    public static void sleep(final String host)
    {
	if (!hostLastTimeMap.containsKey(host))
            logger.log(Level.INFO, "ready to download " + host);
        else
        {
            if (!hostIntervalMap.containsKey(host))
                hostIntervalMap.put(host,defaultInterval);
            final long interval = hostIntervalMap.get(host);
            final long duration = new Date().getTime() - hostLastTimeMap.get(host).getTime();
            if (duration < interval)
            {
                final long l = interval - duration;
                logger.log(Level.INFO, "waiting " + l + " miliseconds to prevent a download ban from " + host);
                try
                {
                    Thread.sleep(l);
                }
                catch (InterruptedException e)
                {
                    // ignore
                }
            }
        }
        hostLastTimeMap.put(host,new Date());
    }
}
