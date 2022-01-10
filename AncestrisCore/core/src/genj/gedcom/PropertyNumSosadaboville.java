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

/**
 *
 * @author frederic
 */
public class PropertyNumSosadaboville extends PropertyNumDaboville {

    public PropertyNumSosadaboville(String tag) {
        super(tag);
    }

    @Override
    protected void calcNumbers(String value, ComparableList<Integer> array) {
        super.calcNumbers(value.split(" ")[0], array);
    }
    
    
    @Override
    public PropertyComparator2 getComparator() {
        return NUMSOSADABOVILLEComparator.getInstance();
    }

    private static class NUMSOSADABOVILLEComparator extends PropertyComparator2.Default<PropertyNumSosadaboville> {

        private static final NUMSOSADABOVILLEComparator INSTANCE = new NUMSOSADABOVILLEComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(PropertyNumSosadaboville nsd1, PropertyNumSosadaboville nsd2) {
            return nsd1.compareTo(nsd2);
        }

        /**
         * return size of sosa nbr
         */
        @Override
        public String getSortGroup(PropertyNumSosadaboville p) {
            Integer sosaInt = (Integer) p.getNumbersList().get(0);
            String sosaStr = String.valueOf(sosaInt);
            return String.valueOf(sosaStr.length());
        }
    }


}
