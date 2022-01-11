/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2022 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.gedcom;

import ancestris.util.ComparableList;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author frederic
 */
public class PropertyNumDaboville extends PropertyNumericValue {

    private ComparableList<BigInteger> numbers = new ComparableList<>();   // Conversion of Daboville number into sortable numbers array (numbers like numbers, a, b, c like numbers too.

    public PropertyNumDaboville(String tag) {
        super(tag);
    }

    public void setValue(String set) {
        super.setValue(set);
        calcNumbers(getValue(), numbers);
    }
    
    public ComparableList<BigInteger> getNumbersList() {
        return numbers;
    }
    
    protected void calcNumbers(String value, ComparableList<BigInteger> array) {
        Pattern numberPattern = Pattern.compile("([0-9]+[a-z]*)*");
        Pattern digitsPattern = Pattern.compile("^[0-9]+");
        Pattern marrPattern = Pattern.compile("[a-z]$");

        array.clear();
        Matcher m = numberPattern.matcher(value);
        while (m.find()) {
            String bit = m.group();
            if (bit.isEmpty()) {
                continue;
            }
            char marr = 'a';  // to sort properly "1a" before "1.1"
            Matcher md = digitsPattern.matcher(bit);
            if (md.find()) {
                BigInteger i1 = new BigInteger(md.group());
                array.add(i1);
            }
            Matcher mm = marrPattern.matcher(bit);
            if (mm.find()) {
                marr = mm.group().charAt(0);
            }
            BigInteger i2 = new BigInteger(String.valueOf(Character.getNumericValue(marr)));
            array.add(i2);
        }
    }

    /**
     * Compare two sosa extracting numeric part of the value
     */
    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Property other) {
        PropertyNumericValue that = (PropertyNumericValue) other;
        // boxes don't match?
        if (that.value.getClass() != this.value.getClass()) {
            return super.compareTo(other);
        }
        // let boxes compare
        int c = ((Comparable<Object>) (this.getNumbersList())).compareTo((Comparable<Object>) (((PropertyNumDaboville)that).getNumbersList())); 
        return c == 0 ? this.getValue().compareTo(that.getValue()) : c;  // // to sort properly "1" before "1a"
    }


    
    @Override
    public PropertyComparator2 getComparator() {
        return NUMDABOVILLEComparator.getInstance();
    }

    private static class NUMDABOVILLEComparator extends PropertyComparator2.Default<PropertyNumDaboville> {

        private static final NUMDABOVILLEComparator INSTANCE = new NUMDABOVILLEComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(PropertyNumDaboville nd1, PropertyNumDaboville nd2) {
            return nd1.compareTo(nd2);
        }

        /**
         * return size of list
         */
        @Override
        public String getSortGroup(PropertyNumDaboville p) {
            return String.valueOf(p.getNumbersList().size()/2);
        }
    }

}
