/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.util;

/**
 * A helper to access values in a string separated through separator by index
 */
public class DirectAccessTokenizer {

    private String string, separator;
    private String[] valeur;
  
    private boolean skipEmpty;

    /**
     * Constructor
     */
    public DirectAccessTokenizer(String string, String separator) {
        this(string, separator, false);
    }

    public DirectAccessTokenizer(String string, String separator, boolean skipEmpty) {
        this.skipEmpty = skipEmpty;
        this.string = string;
        this.separator = separator;
        this.valeur = this.string.split(separator, -1);
      
    }

    /**
     * Tokens
     */
    public String[] getTokens() {
        return getTokens(false);
    }

    /**
     * Tokens
     */
    public String[] getTokens(boolean trim) {
        String[] valTrim = valeur.clone();
        if (trim) {
            for (int i = 0; i < valTrim.length; i++) {
                valTrim[i] = valTrim[i].trim();
            }
        }
        return valTrim;
    }

    /**
     * Count tokens
     */
    public int count() {
        return valeur.length;
    }

    /**
     * Check if a pattern exists in the original String
     * @param search text to search
     * @return  position index if found, -1 if not.
     */
    public int contains(String search){
        if (search == null || !string.contains(search)) {
            return -1;
        }
        for (int i = 0; i < valeur.length ; i++) {
            if (search.equals(valeur[i].trim())) {
                return i;
            }
        }
        return -1;
    }

  

    /**
     * Access rest of string after position.
     * @param pos Position to begin with
     * @return The string from the position index
     */
    public String getSubstringFrom(int pos) {
        return getSubstring(pos, valeur.length);
    }
    
    /**
     * Get the substring between from and until.
     * @param from Position to begin
     * @param until Position to end before.
     * @return The substring between the two positions.
     */
    public String getSubstring(int from, int until) {
        final StringBuilder sb = new StringBuilder(string.length());
        
        for (int i = from; i < until ; i++) {
            sb.append(valeur[i]);
            if (i < until -1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * Access token by position
     *
     * @return token at position or null if no such exists
     */
    public String get(int pos) {
        return get(pos, false);
    }

    /**
     * Access token by position.
     * @param pos Position of token
     * @param trim True if the value returned is trimmed
     * @return token at position or null if no such exists
     */
    public String get(int pos, boolean trim) {

        // legal argument?
        if (pos < 0 || pos >= valeur.length) {
            return null;
        }

        String result = valeur[pos];
        int indice = pos;
        if (skipEmpty) {
            indice++;
            while ("".equals(result.trim()) && indice < valeur.length) {
                result = valeur[indice];
                indice++;

            }
        }

        return trim ? result.trim() : result;
    }

    /**
     * String representation
     */
    public String toString() {
        return string.replaceAll(separator, ", ");
    }

}
