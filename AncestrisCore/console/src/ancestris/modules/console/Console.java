package ancestris.modules.console;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author dominique
 */
public class Console implements OutputListener {

    private InputOutput io = null;
    private OutputWriter out = null;
    private OutputWriter error = null;
    private boolean displayIDELog = false;

    public Console(String tabName) {
        this.displayIDELog = NbPreferences.forModule(Console.class).getBoolean("DisplayConsole", false);

        io = IOProvider.getDefault().getIO(tabName, true);
        out = io.getOut();
        error = io.getErr();
        if (displayIDELog == true) {
            io.select();
        }
    }
    
    public void show() {
        io.select();
    }

    public void reset() {
        close();
        try {
            out.reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void println(String s) {
        println(s, false);
    }

    public void println(String s, boolean time) {
        if (time) {
            s = getTime() + " " + s;
        }
        try {
            out.println(s, null);
        } catch (IOException ex) {
            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printError(String s) {
        printError(s, false);
    }

    public void printError(String s, boolean time) {
        if (time) {
            s = getTime() + " " + s;
        }
        error.println(s);
    }

    public void close() {
        out.close();
        io.getErr().close();
    }
    
    private String getTime() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());  
    }

    @Override
    public void outputLineSelected(OutputEvent oe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void outputLineAction(OutputEvent oe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void outputLineCleared(OutputEvent oe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
