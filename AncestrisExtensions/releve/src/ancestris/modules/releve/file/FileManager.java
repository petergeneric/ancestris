package ancestris.modules.releve.file;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.ReleveTopComponent;
import ancestris.modules.releve.model.ModelAbstract;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class FileManager {

    private static final String FILE_DIRECTORY = "FileDirectory";
//    public static final int FILE_TYPE_UNKNOW = 0;
//    public static final int FILE_TYPE_ANCESTRISV1 = 1;
//    public static final int FILE_TYPE_EGMT = 2;
//    public static final int FILE_TYPE_NIMEGUE = 3;
    public static enum FileFormat { FILE_TYPE_UNKNOW,  FILE_TYPE_ANCESTRISV1, FILE_TYPE_EGMT, FILE_TYPE_NIMEGUE } ;

    /**
     * determine le type de fichier
     * puis charge le fichier
     * @param inputFile
     * @return
     * @throws Exception
     */
    public static FileBuffer loadFile(File inputFile) throws Exception {

        // je lis la premiere ligne du fichier
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String strLine = br.readLine();
        if (strLine==null || strLine.isEmpty() ) {
            throw new Exception(String.format("%s \n Fichier vide.", inputFile.getName()));
        }
        FileBuffer buffer = null;
        if (ReleveFileAncestrisV1.isValidFile(strLine)) {
            buffer = ReleveFileAncestrisV1.loadFile(inputFile);
        } else if (ReleveFileEgmt.isValidFile(strLine)) {
            buffer = ReleveFileEgmt.loadFile(inputFile);
        } else if (ReleveFileNimegue.isValidFile(strLine)) {
            buffer = ReleveFileNimegue.loadFile(inputFile);
        } else {
            throw new Exception(String.format("%s \n Fichier Format de fichier inconnu", inputFile.getName()));
        }
        return buffer;
    }
    
    /**
     * Enregistre dans un fichiers tous les releves
     * @param dataManager
     * @param resultFile
     * @param fileType
     */
    public static void saveFile(DataManager dataManager, File resultFile, FileFormat fileFormat) {
        saveFile(dataManager, resultFile, fileFormat, dataManager.getReleveBirthModel(), dataManager.getReleveMarriageModel(), dataManager.getReleveDeathModel(), dataManager.getReleveMiscModel());
    }

    /**
     * Enregistre dans un fichier certains types de releves
     * @param dataManager
     * @param resultFile
     * @param fileFormat format du fichier 
     * @param models  liste des modeles a enregistrer
     */
    public static void saveFile( DataManager dataManager, File resultFile, FileFormat fileFormat , ModelAbstract... models ) {
        if (resultFile != null) {
            // j'enregistre le répertoire du fichier
            NbPreferences.forModule(ReleveTopComponent.class).put(FILE_DIRECTORY, resultFile.getParent().toString());

            if (models.length == 0) {
                return;
            }

            switch (fileFormat) {
                case FILE_TYPE_ANCESTRISV1:
                    ReleveFileAncestrisV1.saveFile(dataManager, models[0], resultFile, false);
                    for(int i=1; i< models.length; i++) {
                        ReleveFileAncestrisV1.saveFile(dataManager, models[i], resultFile, true);
                    }
                    break;
                case FILE_TYPE_EGMT:
                    ReleveFileEgmt.saveFile(dataManager, models[0], resultFile, false);
                    for(int i=1; i< models.length; i++) {
                        ReleveFileEgmt.saveFile(dataManager, models[i], resultFile, true);
                    }
                    break;
                case FILE_TYPE_NIMEGUE:
                    ReleveFileNimegue.saveFile(dataManager, models[0], resultFile, false);
                    for(int i=1; i< models.length; i++) {
                        ReleveFileNimegue.saveFile(dataManager, models[i], resultFile, true);
                    }
                    break;

            }           
        }
    }

    /**
     * Ligne d'un fichier de releve
     *
     */
    public static class Line  {
        StringBuilder line = new StringBuilder();
        String fieldSeparator;


        /**
         *
         */
        public Line(String fieldSeparator) {
            this.fieldSeparator = fieldSeparator;
        }

        /**
         * ajoute un champ dans la ligne avec le seprateur de ligne
         * et en remplaçant toutes les occurrences du separateur par un point '.'
         * @param value
         * @return
         */
//        public StringBuilder appendSep( String  value ) {
//            return line.append(value.replace(fieldSeparator,".")).append(fieldSeparator);
//        }

        /**
         * ajout de valeurs muti
         */
        public StringBuilder appendSep(String value, String... otherValues) {
            int fieldSize = value.length();
            appendln(value);
            for (String otherValue : otherValues) {
                // j'ajoute les valeurs supplémentaires séparées par des virgules
                if (!otherValue.isEmpty()) {
                    // j'ajoute une virgule s'il y a une valeur déjà présente
                    if (fieldSize > 0) {
                        line.append(", ");
                    }
                    appendln(otherValue);
                    fieldSize += otherValue.length();
                }
            }
            return line.append(fieldSeparator);
        }


        public void appendln(String value) {
            line.append(value.replace(fieldSeparator,"."));
        }

        @Override
        public String toString() {
            return line.toString();
        }


    }
}
