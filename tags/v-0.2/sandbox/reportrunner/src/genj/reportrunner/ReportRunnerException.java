package genj.reportrunner;

/**
 * Exception in report runner.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version $Id: ReportRunnerException.java,v 1.1 2008/11/19 09:46:09 pewu Exp $
 */
public class ReportRunnerException extends Exception
{
    public ReportRunnerException(String arg0)
    {
        super(arg0);
    }

    public ReportRunnerException(Throwable arg0)
    {
        super(arg0);
    }
}
