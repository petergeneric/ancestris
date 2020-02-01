package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldPicture extends FieldSimpleValue {

    /**
     * Si la valeur se termine par un nombre, la comparaison commence par faire 
     * une comparaison alphabetique de la chaine de caracteres avant le nombre final
     * Si les chaines sont egales, la compairaison fait une comparaison numérique 
     * du nombre finale.
     * 
     * exemple :  comparaison de "page2" et "page10" , 
     *   la permière partie "page et "page" est identique. 
     *   la deuxème partie  2 est inférieure à 10 
     *   donc "page2" est inférieur à "page10"
     *
     *
     * @return  a negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Field that) {
        String value1= this.toString();
        int i1;
        // je recherche le début du nombre final dans la premiere valeur
        for (i1 = value1.length() -1 ; i1 >= 0 &&  value1.charAt(i1) >= '0' && value1.charAt(i1) <= '9' ; i1--) {
        }
        i1++;

        String value2= that.toString();
        int i2;
        // je recherche le début du nombre final dans la deuxième valeur
        for (i2 = value2.length() -1 ; i2 >= 0 &&  value2.charAt(i2) >= '0' && value2.charAt(i2) <= '9' ; i2--) {
        }
        i2++;

        int result = value1.substring(0,i1).compareTo(value2.substring(0,i2));
        if (result == 0) {
            // les deux chaines sont égales
            // j'extrait la valeur numérique du nombre final de la premiere valeur
            int num1 = 0;
            if ( i1 < value1.length() ) {
                num1 = new Integer(value1.substring(i1,value1.length())) +1;
            }
            // j'extrait la valeur numérique du nombre final de la deuxième valeur
            int num2 = 0;
            if ( i2 < value2.length() ) {
                num2 = new Integer(value2.substring(i2,value2.length())) +1;
            }
            // je compare les valeurs numériques
            result = num1 - num2;
        }
        return result;
    }
}
