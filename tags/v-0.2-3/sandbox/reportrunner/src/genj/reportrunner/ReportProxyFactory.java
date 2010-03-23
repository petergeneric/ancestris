package genj.reportrunner;

import genj.fo.Document;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.report.ReportLoader;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * Creates report proxy objects.
 * A report proxy overrides GUI methods to bypass user interaction.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version $Id: ReportProxyFactory.java,v 1.4 2009/04/30 17:03:24 pewu Exp $
 */
public class ReportProxyFactory
{
    private ClassPool pool;

    public ReportProxyFactory()
    {
        pool = ClassPool.getDefault();
        try
        {
            String reportDir = ReportLoader.getReportDirectory().toString();
            pool.appendClassPath(reportDir);
        }
        catch (NotFoundException e)
        {
            e.printStackTrace();
        }
    }

	/**
	 * Creates a proxy object for a given report.
	 * @param report  report object
	 * @return  new report proxy object
	 */
    public ReportProxy create(Report report) throws ReportProxyException
    {
        try
        {
            String className = report.getClass().getCanonicalName() + "Runner";
            CtClass myclass = pool.makeClass(className);
            myclass.setSuperclass(pool.get(report.getClass().getCanonicalName()));

            CtField f = new CtField(pool.get(ReportProxy.class.getCanonicalName()), "proxy", myclass);
            f.setModifiers(Modifier.PUBLIC);
            myclass.addField(f);

            CtMethod m = new CtMethod(CtClass.voidType, "showDocumentToUser",
                    new CtClass[] { pool.get(Document.class.getCanonicalName()) } , myclass);
            m.setBody("{ proxy.showDocumentToUser($1); }");
            myclass.addMethod(m);

            m = new CtMethod(CtClass.voidType, "showFileToUser",
                    new CtClass[] { pool.get(File.class.getCanonicalName()) } , myclass);
            m.setBody("{}");
            myclass.addMethod(m);

            m = new CtMethod(CtClass.voidType, "setOutput",
                    new CtClass[] { pool.get(PrintWriter.class.getCanonicalName()) } , myclass);
            m.setBody("{ this.out = $1; }");
            myclass.addMethod(m);

            m = new CtMethod(pool.get(String.class.getCanonicalName()), "translate",
                    new CtClass[] { pool.get(String.class.getCanonicalName()), pool.get(Object[].class.getCanonicalName()) } , myclass);
            m.setBody("{ return proxy == null ? $1 : proxy.translate($1, $2); }");
            myclass.addMethod(m);

            m = new CtMethod(pool.get(File.class.getCanonicalName()), "getFileFromUser",
                    new CtClass[] {
                        pool.get(String.class.getCanonicalName()),
                        pool.get(String.class.getCanonicalName()),
                        CtClass.booleanType,
                        pool.get(String.class.getCanonicalName())
                    }, myclass);
            m.setBody("{ return proxy.getFileFromUser(); }");
            myclass.addMethod(m);

            m = new CtMethod(CtClass.voidType, "start",
                    new CtClass[] { pool.get(Indi.class.getCanonicalName()) } , myclass);
            m.setBody("{ super.start($1); }");
            myclass.addMethod(m);

            @SuppressWarnings("unchecked")
            Class<Report> cl = myclass.toClass(report.getClass().getClassLoader(), null);
            Report newReport = cl.newInstance();

            // Set proxy object field
            ReportProxy proxy = new ReportProxy(report, newReport);
            Field[] fields = newReport.getClass().getDeclaredFields();
            for (Field field : fields)
                if (field.getName().equals("proxy"))
                    field.set(newReport, proxy);

            return proxy;
        }
        catch (NotFoundException e)
        {
            throw new ReportProxyException(e);
        }
        catch (CannotCompileException e)
        {
            throw new ReportProxyException(e);
        }
        catch (InstantiationException e)
        {
            throw new ReportProxyException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new ReportProxyException(e);
        }
    }
}
