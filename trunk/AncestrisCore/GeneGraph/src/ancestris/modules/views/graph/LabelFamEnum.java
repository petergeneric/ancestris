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

import org.openide.util.NbBundle;

/**
 * Enum for Fam labels.
 *
 * @author Zurga
 */
public enum LabelFamEnum {
    FAM_DATE("label.fam.date"),
    FAM_PLACE("label.fam.place"),
    FAM_SIGNE("label.fam.signeDate"),
    FAM_ID_DATE("label.fam.idDate");
    
    private final String description;
    private final String code;

    private LabelFamEnum(String desc) {
        code = desc;
        description = NbBundle.getMessage(GraphTopComponent.class, desc);
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return description;
    }

}
