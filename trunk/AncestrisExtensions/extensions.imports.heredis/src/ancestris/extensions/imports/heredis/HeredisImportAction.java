package ancestris.extensions.imports.heredis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class HeredisImportAction implements ActionListener {

    JFileChooser fc = new JFileChooser();

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, NbBundle.getMessage(HeredisImportAction.class, "warning"));
        fc.setDialogTitle(NbBundle.getMessage(HeredisImportAction.class, "openDialog.Title"));
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File importFile = fc.getSelectedFile();
            File outFile = null;
            try {
                outFile = File.createTempFile("gedcom", ".ged");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            new HeredisImport(importFile, outFile).run();
        }
    }
}
