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

/**
 *
 * @author frederic
 */
public class PropertyNumSosa extends PropertyNumericValue {
    
  public PropertyNumSosa(String tag) {
    super(tag);
  }

    @Override
    public PropertyComparator2 getComparator() {
        return NUMSOSAComparator.getInstance();
    }

    private static class NUMSOSAComparator extends PropertyComparator2.Default<PropertyNumSosa> {

        private static final NUMSOSAComparator INSTANCE = new NUMSOSAComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(PropertyNumSosa ns1, PropertyNumSosa ns2) {
            return ns1.compareTo(ns2);
        }

        /**
         * return size of sosa nbr
         */
        @Override
        public String getSortGroup(PropertyNumSosa p) {
            String sosaStr = p.extractNumberObject().toString();
            return String.valueOf(sosaStr.length());
        }
    }

  

}
