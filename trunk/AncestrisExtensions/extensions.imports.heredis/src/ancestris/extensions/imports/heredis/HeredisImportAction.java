package ancestris.extensions.imports.heredis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.openide.util.Exceptions;

public final class HeredisImportAction implements ActionListener {

    JFileChooser fc = new JFileChooser() {
    };

    @Override
    public void actionPerformed(ActionEvent e) {
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File importFile = fc.getSelectedFile();
            File outFile = null;
            try {
                outFile = File.createTempFile("gedcom", ".ged");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            new HeredisImport (importFile, outFile).run();
        }
    }
}
