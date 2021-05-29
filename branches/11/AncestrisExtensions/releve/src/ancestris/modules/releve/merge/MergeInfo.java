package ancestris.modules.releve.merge;

import java.util.ArrayList;


public class MergeInfo {
    private final InfoFormatter defaultFormatter;

    private final ArrayList<InfoItem> m_infos = new ArrayList<InfoItem>();

    MergeInfo() {
        this.defaultFormatter = new MergeInfo.InfoFormatter() {
            @Override
            public Object format(Object arg) {
                if (arg == null) {
                    return "";
                } else {
                    return arg;
                }
            }

        };
    }

    MergeInfo(String format, Object... args) {
        this.defaultFormatter = new MergeInfo.InfoFormatter() {
            @Override
            public Object format(Object arg) {
                if (arg == null) {
                    return "";
                } else {
                    return arg;
                }
            }

        };
        InfoItem infoItem = new InfoItem (format, args);
        m_infos.add(infoItem);
    }

    void add(String format, Object... args) {
        m_infos.add(new InfoItem (format, args) );
    }

    void addSeparator() {
        m_infos.add(null);
    }

    ArrayList<InfoItem> getArgs() {
        return m_infos;
    }


    /**
     * conversion en String par defaut
     * @return
     */
    @Override
    public String toString() {
        return toString( defaultFormatter );
    }

    /**
     * conversion en String avec Formatter personnalisé
     * @return
     */
    public String toString(InfoFormatter infoFormater) {
        StringBuilder format = new StringBuilder();
        ArrayList<Object> arrayArgs = new ArrayList<Object> ();
        for (InfoItem item : m_infos) {
            if ( item != null ) {
                format.append(item.format);
                for (Object arg : item.args) {
                    arrayArgs.add(infoFormater.format(arg));
                }
            } else {
                format.append(infoFormater.getSeparator());
            }
        }
        return String.format(format.toString(), arrayArgs.toArray());
    }


    private static class InfoItem {
        String format;
        Object[] args;
        InfoItem( String format, Object... args) {
            this.format = format;
            this.args = args;
        }
    }

    public static abstract class InfoFormatter {
        public abstract Object format( Object arg );
        public String getSeparator() {
            return " ";
        }
    }
}

