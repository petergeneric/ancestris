package genjreports.rdf.semweb;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Nice
{
    private static final Logger logger = Logger.getLogger(Mashup.class.getName());

    public static Date sleep(final long interval, final Date lastRequest)
    {

        if (lastRequest != null)
        {
            lastRequest.getTime();
            final long duration = new Date().getTime() - lastRequest.getTime();
            if (duration < interval)
            {
                final long l = interval - duration;
                logger.log(Level.INFO, "waiting " + l + " miliseconds to prevent a ban by the provider");
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
        return new Date();
    }
}
