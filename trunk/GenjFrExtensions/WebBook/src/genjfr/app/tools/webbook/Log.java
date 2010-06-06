/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.webbook;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
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

    /** Constructor */
    public Log(String logname, String title) {
        // initialises output window
        io = IOProvider.getDefault().getIO(title, true);
        io.select();

        // initialises log file
        File logFile = new File(logname);
        if (logFile == null) {
            io.getErr().println(NbBundle.getMessage(WebBookStarter.class, "LOG_ErrorLogFile"));
            return;
        } else {
            try {
                outFile = new PrintWriter(new FileWriter(logFile));
            } catch (IOException ioe) {
                io.getErr().println("IO Exception!");
                //ioe.printStackTrace();
            }
        }
    }

    /**
     * Closing log file
     */
    public void close() {
        outFile.flush();
        outFile.close();
        return;
    }

    /**
     * Writing text to screen and log file
     */
    public void write(String text) {
        write(NORMAL, text);
        return;
    }

    public void write(boolean mode, String text) {
        if (mode) {
            io.getOut().println(text);
        } else {
            io.getErr().println(text);
        }
        outFile.println(text);
    }

    /**
     * Writing timestamp
     */
    public void timeStamp() {
        write(NORMAL, Calendar.getInstance().getTime().toString());
        return;
    }


    /**
     * Error hadnling
     */
    void printStackTrace(Exception ex) {
        ex.printStackTrace(outFile);
        ex.printStackTrace(io.getErr());
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
        return;
    }

    public void timeStamp(int indent, String str) {
        write(0, indent, "", 0, str + Calendar.getInstance().getTime().toString());
        return;
    }

} 
  
