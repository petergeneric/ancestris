package ancestris.modules.releve.table;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Michel
 */
public class ErrorBuffer {
    private TreeSet<CheckError> errorSet = new TreeSet<CheckError>();
    private TreeMap<Integer,TreeMap<Integer,TreeSet<CheckError>>> errorTree = new TreeMap<Integer,TreeMap<Integer,TreeSet<CheckError>>>();

    StringBuilder sb = new StringBuilder();

    public StringBuilder append(String message) {
        return sb.append( message );
    }

    public String getError() {
        return sb.toString();
    }

    /**
     * retourn la ligne d'entete
     * @return
     */
    static protected String[] getHeader() {
        String[] header = {
            "Evenement",
            "Departement",
            "Ville",
            "Paroisse",
            "Notaire",
            "Cote",
            "Photo",
            "Jour",
            "Mois",
            "Annee",
            "Nom",
            "Prenom",
            "Sexe",
            "Age",
            "Lieu",
            "Infos",
            "Prenom pere",
            "Pere decede",
            "Info pere",
            "Nom mere",
            "Prenom mere",
            "Mere decede",
            "Info mere",
            "Nom conjoint",
            "Prenom conjoint",
            "Deces conjoint",
            "Age conjoint",
            "Lieu conjoint",
            "Info conjoint",
            "Prenom pere conjoint",
            "Deces pere conjoint",
            "Info pere conjoint",
            "Nom mere conjoint",
            "Prenom mere conjoint",
            "Deces mere conjoint",
            "Info mere conjoint",
            "Heritiers",
            "Nom parrain",
            "Prenom parrain",
            "Commentaire parrain",
            "Nom marrraine",
            "Prenom marrraine",
            "Infos marrraine",
            "Infos diverses"};

        return header;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Errors
    //////////////////////////////////////////////////////////////////////////
    void addError(int row, int col, String message) {
        CheckError error = new CheckError(row, col, message);
        errorSet.add(error);

        // j'ajoute la ligne dans la table des années
        TreeMap<Integer,TreeSet<CheckError>> rowErrors = errorTree.get(row);
        if (rowErrors == null) {
            rowErrors = new  TreeMap<Integer,TreeSet<CheckError>>();
            errorTree.put(row, rowErrors);
        }
        TreeSet<CheckError> colErrors = rowErrors.get(col);
        if (colErrors == null) {
            colErrors = new  TreeSet<CheckError>();
            rowErrors.put(col, colErrors);
        }
        colErrors.add(error);
    }

    public void removeAll() {
        errorSet.clear();
        errorTree.clear();

    }

    int getErrorCount() {
        return errorSet.size();
    }

    CheckError[] getErrors() {
        return errorSet.toArray(new CheckError[errorSet.size()]);
    }

    CheckError[] getError (int row, int col) {
        TreeMap<Integer,TreeSet<CheckError>> rowErrors = errorTree.get(row);
        if(rowErrors != null) {
            TreeSet<CheckError> colErrors = rowErrors.get(col);
            if(colErrors != null) {
                return colErrors.toArray(new CheckError[colErrors.size()]);
            } else {
                return new CheckError[0];
            }
        } else {
            return new CheckError[0];
        }
    }

    boolean hasError(int row) {
        return (errorTree.get(row) != null);
    }

    static public class CheckError implements Comparable<CheckError> {
        int row;
        int col;
        String message;

        public CheckError(int row, int col, String message) {
            this.row = row;
            this.col = col;
            this.message = message;
        }

        @Override
        public String toString() {
            // j'affiche le numero de ligne en commençant a 1 pour la premiere ligne
            return "ligne: "+ (row+1) + " colonne:" + getLetter(col) + " " + message;
        }

        /**
         * Compares this field to another field
         * @return  a negative integer, zero, or a positive integer as this object
         *      is less than, equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(CheckError otherCheckError) {
            if ( row == otherCheckError.row) {
                return  col - otherCheckError.col;
            } else {
                return row - otherCheckError.row ;
            }
        }
    }

    public static String getLetter( int col) {
        int dizain = col / 26;
        int unit = col - dizain * 26;
        String letter = "";
        if (dizain > 0) {
            letter += (char) (dizain + 64);
        }
        letter += (char) (unit + 65);
        return letter;
    }
}
