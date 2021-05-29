package ancestris.modules.releve.file;

import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.ReleveTopComponent;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.RecordModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class FileManager {

    private static final String FILE_DIRECTORY = "FileDirectory";
    public static enum FileFormat { 
        FILE_TYPE_UNKNOW,
        FILE_TYPE_ANCESTRISV1,
        FILE_TYPE_ANCESTRISV2, 
        FILE_TYPE_ANCESTRISV3,
        FILE_TYPE_ANCESTRISV4,
        FILE_TYPE_ANCESTRISV5,
        FILE_TYPE_EGMT,
        FILE_TYPE_NIMEGUE,
        FILE_TYPE_PDF
    } ;

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
            throw new Exception(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.EmptyFile"), inputFile.getName()));
        }
        StringBuilder sb  = new StringBuilder();
        FileBuffer buffer = null;
        if (ReleveFileAncestrisV5.isValidFile(inputFile, sb.append('\n'))) {
            buffer = ReleveFileAncestrisV5.loadFile(inputFile);
        } else if (ReleveFileAncestrisV4.isValidFile(inputFile, sb.append('\n'))) {
            buffer = ReleveFileAncestrisV4.loadFile(inputFile);
        } else if (ReleveFileEgmt.isValidFile(inputFile, sb.append('\n'))) {
            buffer = ReleveFileEgmt.loadFile(inputFile);
        } else if (ReleveFileNimegue.isValidFile(inputFile, sb.append('\n'))) {
            buffer = ReleveFileNimegue.loadFile(inputFile);
        } else if (ReleveFileAncestrisV3.isValidFile(inputFile, sb.append('\n'))) {
            buffer = ReleveFileAncestrisV3.loadFile(inputFile);
        } else if (ReleveFileAncestrisV2.isValidFile(inputFile, sb.append('\n'))) {
            buffer = ReleveFileAncestrisV2.loadFile(inputFile);
        } else if (ReleveFileAncestrisV1.isValidFile(inputFile, sb.append('\n'))) {
            buffer = ReleveFileAncestrisV1.loadFile(inputFile);
        } else {
            throw new Exception(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.UnknownFormat"), inputFile.getName())+ "\n" + sb.toString());
        }
        return buffer;
    }
    
    /**
     * Enregistre dans un fichiers tous les releves
     * @param dataManager
     * @param resultFile
     * @param fileType
     */
    public static StringBuilder saveFile(DataManager dataManager, PlaceManager placeManager, File resultFile, FileFormat fileFormat) {
        return saveFile(placeManager, resultFile, fileFormat, dataManager.getDataModel(), null);
    }

    /**
     * Enregistre dans un fichier certains types de releves
     * @param placeManager
     * @param saveFile
     * @param fileFormat format du fichier 
     * @param models  liste des modeles a enregistrer
     */
    public static StringBuilder saveFile( PlaceManager placeManager, File saveFile, FileFormat fileFormat , RecordModel recordModel, RecordType recordType ) {
        StringBuilder sb = new StringBuilder();
        if (saveFile != null) {
            // j'enregistre le répertoire du fichier
            NbPreferences.forModule(ReleveTopComponent.class).put(FILE_DIRECTORY, saveFile.getParent());

            switch (fileFormat) {
                case FILE_TYPE_ANCESTRISV5:
                default:
                    sb.append(ReleveFileAncestrisV5.saveFile(placeManager, recordModel, recordType, saveFile, false));
                    break;
                case FILE_TYPE_EGMT:
                    sb.append(ReleveFileEgmt.saveFile(placeManager, recordModel, recordType, saveFile, false));
                    break;
                case FILE_TYPE_NIMEGUE:
                    sb.append(ReleveFileNimegue.saveFile(placeManager, recordModel, recordType, saveFile, false));
                    break;
                case FILE_TYPE_PDF:
                    sb.append(ReleveFilePdf.saveFile(placeManager, recordModel, recordType, saveFile, false));
                    break;
            }           
        }
        return sb;
    }

    /**
     * Ligne d'un fichier de releve
     *
     */
    public static class Line  {
        StringBuilder line = new StringBuilder();
        char fieldSeparator;
        static char quote = '\"';



        /**
         *
         */
        public Line(char fieldSeparator) {
            this.fieldSeparator = fieldSeparator;
        }

        /**
         * ajoute un champ dans la ligne avec le sepreateur de ligne
         * et en remplaçant toutes les occurrences du separateur par un point '.'
         * @param value
         * @return
         */

        public StringBuilder appendCsvFn(String value, String... otherValues) {
            // j'ajoute le separateur de champ
            return appendCsv(value,otherValues).append(fieldSeparator);
        }

        /**
         * ajout de valeurs multi
         */
        public StringBuilder appendCsv(String value, String... otherValues) {
            int fieldSize = value.length();
            StringBuilder sb = new StringBuilder();
            boolean separatorFound = false;
            sb.append(value);
            separatorFound |= value.indexOf(fieldSeparator)!=-1 ;
            for (String otherValue : otherValues) {
                // j'ajoute les valeurs supplémentaires séparées par des virgules
                if (!otherValue.isEmpty()) {
                    separatorFound |= value.indexOf(fieldSeparator)!=-1 ;
                    // je concantene les valeurs en inserant une virgule dans
                    // si la valeur précedente n'est pas vide
                    if (fieldSize > 0) {
                        sb.append(", ");
                    }
                    sb.append(otherValue);
                    fieldSize += otherValue.length();
                }
            }

            // j'ajoute des quotes au debut et a la fin du champ si le separateur
            // est present dans les données
            if (separatorFound) {
                line.append(quote).append(sb).append(quote);
            } else {
                line.append(sb);
            }            
            return line;
        }

        /**
         * lit les données en entrée jusqu'au separateur de fin de ligne \n
         * applique les cas particulier prévus dans la norme du format CSV (RFC 4180).
         * Cas ou les données d'un champ sont entre guillement :
         *     ;"aaa; bbb"; => ;aaa bbb;
         * traite le cas du double guillemet protegeant un guillemet
         *     ;aaa""bbb; => ;aaa"bbb;
         * @param reader  données en entrée
         * @param fieldSeparator  caractere seprateur de champ
         * @return String[] champs lu ou null si la fin de fichier est rencontrée
         * @throws IOException
         */
        public static String[] splitCSV(BufferedReader reader, char fieldSeparator) throws IOException {
            final List<String> fields = new ArrayList<String>();
            final StringBuilder sb = new StringBuilder(100);
            boolean fieldFound = false;
            
            for (boolean quoted = false;; sb.append('\n')) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                fieldFound = true;
                for (int i = 0, len = line.length(); i < len; i++) {
                    final char c = line.charAt(i);
                    if (c == quote) {
                        if (quoted && i < len - 1 && line.charAt(i + 1) == quote) {
                            //deux guillemets se suivent
                            sb.append(c);
                            // je saute le guillemet suivant
                            i++;
                        } else {
                            if (quoted) {
                                if (i == len - 1 || line.charAt(i + 1) == fieldSeparator) {
                                    //guillemet en fin de champ
                                    quoted = false;
                                    continue;
                                }
                            } else {
                                // guillement en début de champ
                                if (sb.length() == 0) {
                                    quoted = true;
                                    continue;
                                }
                            }                            
                            sb.append(c);
                        }
                    } else if (c == fieldSeparator && !quoted) {
                        fields.add(sb.toString());
                        sb.setLength(0);
                    } else {
                        sb.append(c);
                    }
                }
                if (!quoted) {
                    break;
                }
            }
            if( fieldFound) {
                // j'ajoute le dernier champ
                fields.add(sb.toString());
                return fields.toArray(new String[fields.size()]);
            } else {
                return null;
            }
        }


        /**
         * ajout de valeurs mutiples et le separateur de fin de champ
         */
        public StringBuilder appendNimegueFn(String value, String... otherValues) {            
            return appendNimegue(value, otherValues).append(fieldSeparator);
        }


         /**
         * ajoute un champ dans la ligne avec le seprateur de ligne
         * et en remplaçant toutes les occurrences du separateur par un point '.'
         * @param value
         * @return
         */
        public StringBuilder appendNimegue(String value, String... otherValues) {
            int fieldSize = value.length();
            line.append(value.replace(fieldSeparator,'.'));
            for (String otherValue : otherValues) {
                // j'ajoute les valeurs supplémentaires séparées par des virgules
                if (!otherValue.isEmpty()) {
                    // j'ajoute une virgule s'il y a une valeur déjà présente
                    if (fieldSize > 0) {
                        line.append(", ");
                    }
                    line.append(otherValue.replace(fieldSeparator,'.'));
                    fieldSize += otherValue.length();
                }
            }
            return line;
        }

        @Override
        public String toString() {
            return line.toString();
        }


    }
}
