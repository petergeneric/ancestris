/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * List of Element to display.
 * @author Zurga
 */
public class ResultsList extends AbstractListModel<DisplayPathElement> {

        /**
         * the results
         */
        private final List<DisplayPathElement> hits = new ArrayList<>();

        /**
         * clear the results (sync to EDT)
         */
        public void clear() {
            // nothing to do?
            if (hits.isEmpty()) {
                return;
            }
            // clear&notify
            int size = hits.size();
            hits.clear();
            fireIntervalRemoved(this, 0, size - 1);
            // done
        }

        /**
         * add a result (sync to EDT)
         */
        public void add(List<DisplayPathElement> list) {
            // nothing to do?
            if (list.isEmpty()) {
                return;
            }
            // remember 
            int size = hits.size();
            hits.addAll(list);
            fireIntervalAdded(this, size, hits.size() - 1);
            // done
        }

        /**
         * @see javax.swing.ListModel#getElementAt(int)
         */
        @Override
        public DisplayPathElement getElementAt(int index) {
            return hits.get(index);
        }

        /**
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize() {
            return hits.size();
        }

        /**
         * access to property
         */
        public DisplayPathElement getHit(int i) {
            return hits.get(i);
        }

    } //Results
