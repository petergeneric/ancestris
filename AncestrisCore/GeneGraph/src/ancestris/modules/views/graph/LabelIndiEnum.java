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
 * Enum for Label Indi choices
 *
 * @author Zurga
 */
public enum LabelIndiEnum {
    INDI_NAME("label.indi.name"),
    INDI_GIVE_NAME("label.indi.giveName"),
    INDI_NAME_ID("label.indi.nameId"),
    INDI_NAME_GENE("label.indi.nameGene"),
    INDI_ID_NAME_GENE("label.indi.idNameGene");

    private final String description;
    private final String code;

    private LabelIndiEnum(String desc) {
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
