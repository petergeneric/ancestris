/**
 * Copyright (C) 2010 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author frederic
 */
public class Log {

    private InputOutput io;
    private PrintWriter outFile;
    public boolean NORMAL = true;
    public boolean ERROR = false;
    public boolean endSuccessful = true;

    /** Constructor */
    public Log(String logname, String title) {
        // initialises output window
        io = IOProvider.getDefault().getIO(title, true);
        io.select();

        // warn if log file name not supplied
        if (logname == null || logname.trim().isEmpty()) {
            io.getOut().println("Warning!!! No log file name provided, therefore writing to output window only.");
            return;
        }

        // initialises log file
        File logFile = new File(logname);
        if (logFile == null) {
            io.getErr().println(NbBundle.getMessage(WebBookStarter.class, "LOG_ErrorLogFile"));
        } else {
            try {
                outFile = new PrintWriter(new FileWriter(logFile));
            } catch (IOException ioe) {
                io.getErr().println("IO Exception!");
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Closing log file
     */
    public void close() {
        if (outFile != null) {
            outFile.flush();
            outFile.close();
        }
    }

    /**
     * Writing text to screen and log file
     */
    public void write(String text) {
        write(NORMAL, text);
    }

    public void write(boolean mode, String text) {
        if (mode) {
            io.getOut().println(text);
        } else {
            io.getErr().println(text);
            endSuccessful = false;
        }
        if (outFile != null) {
            outFile.println(text);
        }
    }

    /**
     * Writing timestamp
     */
    public void timeStamp() {
        write(NORMAL, Calendar.getInstance().getTime().toString());
    }

    /**
     * Error handling
     */
    public void printStackTrace(Exception ex) {
        ex.printStackTrace(outFile);
        ex.printStackTrace(io.getErr());
        endSuccessful = false;
    }

    /**
     * Extra methods, not sure if will be usefull
     * @param level
     * @param indent
     * @param str
     * @param length
     * @param text
     */
    public void write(int level, int indent, String str, int length, String text) {
        String line = "";
        for (int i = 1; i <= length; i++) {
            line += str;
        }

        String indentSpaces = "";
        for (int i = 1; i <= indent; i++) {
            indentSpaces += " ";
        }

        if (level == 0) {
            write(indentSpaces + text);
        }
        if (level == 1) {
            write(" ");
            write(line);
            write(indentSpaces + text);
            write(line);
        }
        if (level == 2) {
            write(line);
            write(indentSpaces + text);
            write(" ");
        }
        if (level == 9) {
            write(" ");
            write(line);
            write(indentSpaces + "##### " + text + " #####");
            write(line);
            write(" ");
        }
    }

    public void timeStamp(int indent, String str) {
        write(0, indent, "", 0, str + Calendar.getInstance().getTime().toString());
    }

    /**
     * Translators methods to make it quicker to code
     */
    public String trs(String string) {
        return NbBundle.getMessage(WebBook.class, string);
    }

    public String trs(String string, Object param1) {
        return NbBundle.getMessage(WebBook.class, string, param1);
    }

    public String trs(String string, Object param1, Object param2) {
        return NbBundle.getMessage(WebBook.class, string, param1, param2);
    }

    public String trs(String string, Object param1, Object param2, Object param3) {
        return NbBundle.getMessage(WebBook.class, string, param1, param2, param3);
    }

    public String trs(String string, Object param1, Object param2, Object param3, Object param4) {
        return NbBundle.getMessage(WebBook.class, string, param1, param2, param3, param4);
    }

    public String trs(String string, Object[] arr) {
        return NbBundle.getMessage(WebBook.class, string, arr);
    }
} 
  
