package genj.reportrunner;

/**
 * Exception while working with a report proxy object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version $Id: ReportProxyException.java,v 1.2 2008/11/19 09:46:09 pewu Exp $
 */
public class ReportProxyException extends ReportRunnerException
{
    public ReportProxyException(String arg0)
    {
        super(arg0);
    }

    public ReportProxyException(Throwable arg0)
    {
        super(arg0);
    }
}
