package ancestris.extensions.reports.view;

import genj.fo.CSVFormat;
import genj.fo.Format;
import genj.fo.HTMLFormat;
import genj.fo.PDFFormat;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class SaveReport {

    genj.fo.Document document = null;
    File saveFile = null;
    final FileNameExtensionFilter htmlFilter = new FileNameExtensionFilter(NbBundle.getMessage(this.getClass(), "SaveReport.fileType.html"), "html");
    final FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(NbBundle.getMessage(this.getClass(), "SaveReport.fileType.pdf"), "pdf");
    final FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(NbBundle.getMessage(this.getClass(), "SaveReport.fileType.csv"), "csv");
    private JFileChooser fileChooser = new JFileChooser() {

        @Override
        public void approveSelection() {
            File f = getSelectedFile();
            if (f.exists() && getDialogType() == SAVE_DIALOG) {
                int result = JOptionPane.showConfirmDialog(this, NbBundle.getMessage(this.getClass(), "SaveReport.Overwrite.Text"), NbBundle.getMessage(this.getClass(), "SaveReport.Overwrite.Title"), JOptionPane.YES_NO_CANCEL_OPTION);
                switch (result) {
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                    case JOptionPane.NO_OPTION:
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        super.cancelSelection();
                        return;
                }
            } else {
                FileNameExtensionFilter fileFilter = (FileNameExtensionFilter) getFileFilter();
                if (fileFilter.accept(f) == false) {
                    setSelectedFile(new File(f.getName() + "." + fileFilter.getExtensions()[0]));
                }
                super.approveSelection();
            }
        }
    };

    public SaveReport(genj.fo.Document doc, String fileName) {
        document = doc;
        fileChooser.addChoosableFileFilter(pdfFilter);
        fileChooser.addChoosableFileFilter(htmlFilter);
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileName.isEmpty() == false) {
            saveFile = new File(fileName);
            try {
                fileChooser.setCurrentDirectory(new File(saveFile.getCanonicalPath()));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            fileChooser.setSelectedFile(new File(saveFile.getName()));

            // set default file filter
            if (pdfFilter.accept(saveFile) == true) {
                fileChooser.setFileFilter(pdfFilter);
            } else if (htmlFilter.accept(saveFile) == true) {
                fileChooser.setFileFilter(htmlFilter);
            } else if (csvFilter.accept(saveFile) == true) {
                fileChooser.setFileFilter(csvFilter);
            } else {
                fileChooser.setFileFilter(pdfFilter);
            }
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileFilter(pdfFilter);
            saveFile = null;
        }
    }

    public String saveFile() {
        Format formatter = null;

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFileFilter().equals(htmlFilter)) {
                formatter = new HTMLFormat();
            } else if (fileChooser.getFileFilter().equals(pdfFilter)) {
                formatter = new PDFFormat();
            } else if (fileChooser.getFileFilter().equals(csvFilter)) {
                formatter = new CSVFormat();
            }
            if (formatter != null) {
                try {
                    saveFile = fileChooser.getSelectedFile();
                    formatter.format(document, saveFile);
                    return saveFile.getCanonicalPath();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return "";
                }
            }
        }
        return "";
    }
}
