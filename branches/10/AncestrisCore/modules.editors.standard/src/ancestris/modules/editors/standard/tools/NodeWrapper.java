/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard.tools;

import ancestris.core.TextOptions;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;

/**
 *
 * @author frederic
 */
public class NodeWrapper {

    private static String NO_NAME = "?";
    private static String NO_DATE = "-";

    public static int PARENTS = 0;
    public static int SIBLING = 1;
    public static int BROTHER = 2;
    public static int SISTER = 3;
    public static int MEUNKNOWN = 4;
    public static int MEMALE = 5;
    public static int MEFEMALE = 6;
    public static int SPOUSE = 7;
    public static int CHILD = 8;
    public static int BOY = 9;
    public static int GIRL = 10;

    private static String b = "";
    private static String m = "";
    private static String d = "";
    private static String c = "";

    private Entity entity = null;
    private int type = 0;
    private Object object = null;
    private Indi myIndi = null;

    public NodeWrapper(int type, Object o) {
        this.type = type;
        this.object = o;

        b = TextOptions.getInstance().getBirthSymbol();
        m = TextOptions.getInstance().getMarriageSymbol();
        d = TextOptions.getInstance().getDeathSymbol();
        c = TextOptions.getInstance().getBaptismSymbol();

        if (type == MEUNKNOWN) {
            Indi indi = (Indi) o;
            this.entity = indi;
            if (indi.getSex() == PropertySex.MALE) {
                this.type = MEMALE;
            } else if (indi.getSex() == PropertySex.FEMALE) {
                this.type = MEFEMALE;
            }
        } else if (type == SIBLING) {
            Indi indi = (Indi) o;
            this.entity = indi;
            if (indi.getSex() == PropertySex.MALE) {
                this.type = BROTHER;
            } else if (indi.getSex() == PropertySex.FEMALE) {
                this.type = SISTER;
            }
        } else if (type == CHILD) {
            Indi indi = (Indi) o;
            this.entity = indi;
            if (indi.getSex() == PropertySex.MALE) {
                this.type = BOY;
            } else if (indi.getSex() == PropertySex.FEMALE) {
                this.type = GIRL;
            }
        } else if (type == PARENTS) {
            this.entity = (Fam) o;
        } else if (type == SPOUSE) {
            this.entity = (Fam) o;
        }
    }

    public NodeWrapper(int type, Object o, Indi indi) {
        this.type = type;
        this.object = o;
        this.myIndi = indi;
        this.entity = (Fam) o;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getType() {
        return type;
    }

    public String getDisplayValue() {

        if (object == null) {
            return "";
        }

        StringBuilder ret = new StringBuilder("<html>");

        if (type == PARENTS) {
            Fam fam = (Fam) object;
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();
            ret.append(getName(husband));
            ret.append(m).append(fam.getMarriageDate() != null ? fam.getMarriageDate().getDisplayValue() : "").append(" ");
            ret.append(getName(wife));
            this.entity = husband;

        } else if (type == SIBLING || type == BROTHER || type == SISTER) {
            Indi indi = (Indi) object;
            ret.append(getName(indi));

        } else if (type == MEUNKNOWN || type == MEMALE || type == MEFEMALE) {
            Indi indi = (Indi) object;
            //ret.append("<b>");
            ret.append(getName(indi));
            //ret.append("</b>");

        } else if (type == SPOUSE) {
            Fam fam = (Fam) object;
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();
            if (husband != null && husband.equals(myIndi)) {
                ret.append(wife != null ? getName(wife) : "");
                this.entity = wife;
            } else if (wife != null && wife.equals(myIndi)) {
                ret.append(husband != null ? getName(husband) : "");
                this.entity = husband;
            } else {
                ret.append("");
                this.entity = fam;
            }
            String dateStr = fam.getMarriageDate() != null ? fam.getMarriageDate().getDisplayValue() : NO_DATE;
            if (dateStr.trim().isEmpty()) {
                dateStr = NO_DATE;
            }
            ret.append(m).append(dateStr).append(" ");

        } else if (type == CHILD || type == BOY || type == GIRL) {
            Indi indi = (Indi) object;
            ret.append(getName(indi));

        } else {
            ret.append(object.toString());
        }

        ret.append("</html>");
        return ret.toString();
    }

    private String getName(Indi indi) {
        boolean useChr = false;
        if (indi == null) {
            return "";
        }
        String ln = (indi.getLastName() != null) ? indi.getLastName() : NO_NAME;
        if (ln.trim().isEmpty()) {
            ln = NO_NAME;
        }
        String fn = (indi.getFirstName() != null) ? indi.getFirstName() : NO_NAME;
        if (fn.trim().isEmpty()) {
            fn = NO_NAME;
        }
        if (fn.endsWith(",")) {
            fn = fn.replaceAll("\\s*,\\s*$", "");
        }
        String bd = (indi.getBirthAsString() != null) ? indi.getBirthAsString() : NO_NAME;
        if (bd.trim().isEmpty()) {
            if (TextOptions.getInstance().isUseChr() && !"".equals(indi.getCHRAsString())) {
                bd = indi.getCHRAsString();
                useChr = true;
            } else {
                bd = NO_DATE;
            }

        }
        String dd = (indi.getDeathAsString() != null) ? indi.getDeathAsString() : NO_NAME;
        if (dd.trim().isEmpty()) {
            dd = NO_DATE;
        }

        StringBuilder ret = new StringBuilder("");
        ret.append(ln).append(", ").append(fn);
        ret.append(" (").append(useChr ? c : b).append(bd).append(" ");
        ret.append(d).append(dd).append(") ");
        return ret.toString();
    }

    public boolean isMe() {
        return (type == MEUNKNOWN || type == MEMALE || type == MEFEMALE);
    }

    public Fam getCurrentFamily(Indi indi) {
        if (type < SPOUSE) {
            return null;
        }
        if (type == SPOUSE) {
            return (Fam) object;
        }
        if (type > SPOUSE) {
            Indi child = (Indi) object;
            Fam fam = child.getFamilyWhereBiologicalChild();
            if (fam == null) {
                return null;
            }
            return fam;
        }
        return null;
    }

}
