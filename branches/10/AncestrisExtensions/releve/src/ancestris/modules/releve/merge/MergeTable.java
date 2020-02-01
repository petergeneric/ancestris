package ancestris.modules.releve.merge;

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.util.WordBuffer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbPreferences;

/**
 * La table affiche les données du relevé et les données de l'individu
 * dans deux colonnes afin de pouvoir les comparer facilement
 * et sélectionner les données du relevé à copier dans l'individu
 * et désélectionner celles que l'on ne veut pas copier
 * @author Michel
 */
public class MergeTable extends JTable {
    private static final Color yellowColor = new Color(240, 240, 10);
    private static final Color blueColor = new Color(200, 255, 255);
    private static final Color greyColor = new Color(240, 240, 240);



    public  MergeTable() {
        setPreferredSize(null);
        setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setBackground(Color.WHITE);
        setSelectionBackground(getBackground());
        setShowGrid(true);
        getTableHeader().setReorderingAllowed(false);
        //setIntercellSpacing(new Dimension(0, 0));
        //ToolTipManager.sharedInstance().registerComponent( this);
        //ToolTipManager.sharedInstance().setInitialDelay(0) ;

        MergeTableRenderer mergeTableRenderer = new MergeTableRenderer();
        setDefaultRenderer(Object.class, mergeTableRenderer);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                int column = target.columnAtPoint(e.getPoint());
                if (column == 4) {
                    int row = target.rowAtPoint(e.getPoint());
                    Object objectValue = getModel().getValueAt(row, column);
                    if( objectValue instanceof MergeTableAction && ((MergeTableAction)objectValue).isClickable() )  {
                        ((MergeTableAction)objectValue).applyAction( (java.awt.Frame) SwingUtilities.getWindowAncestor(target), e.getClickCount());
                    }
                }
            }
        });

        /**
         * change le curseur si la souris passe au dessus d'une cellule
         * contenant une entité cliquable
         */
        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                int column = target.columnAtPoint(e.getPoint());
                if (column == 4) {
                    int row = target.rowAtPoint(e.getPoint());
                    Object value = getModel().getValueAt(row, column);
                    if ( value instanceof MergeTableAction && ((MergeTableAction)value).isClickable() ) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        });

    }

    public void setModel(ProposalRuleTableModel model) {
        super.setModel(model);
//        model.addTableModelListener(this);
        loadColumnLayout();
    }

    /**
     * cet methode masque la checkbox inutile dans la colonne 2
     * @param renderer
     * @param row
     * @param column
     * @return
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        if (column == 2 && getModel().getValueAt(row, 2) == null  ) {
            // j'affiche un Jlabel à la place de la checkbox si la valeur de la colonne 2 est nule
            JLabel label = new JLabel("");
            if( ((ProposalRuleTableModel) getModel()).getCompareResult(row) == ProposalRule.CompareResult.EQUAL) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setText("=");
            }
            return label;
        }  else {
            return super.prepareRenderer(renderer, row, column);
        }
    }


    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        if (column == 2 && getModel().getValueAt(row, 2) == null  ) {
            // j'affiche un Jlabel à la place de la checkbox si la valeur de la colonne 2 est nule
            JLabel label = new JLabel("");
            return label;
        }  else {
            return super.prepareEditor(editor, row, column);
        }
    };

    /**
     * Enregistrer la largeur des colonnes
     * Cette méthode est appelée par la fenêtre principal avant la fermeture
     */
    protected void componentClosed() {
        saveColumnLayout();

    }

    /**
     * Set column layout from bundle configuration
     */
    private void loadColumnLayout() {
        String columnLayout = NbPreferences.forModule(MergeTable.class).get("MergeColumnLayout", "5,75,150,23,156,104");
        try {
            StringTokenizer tokens = new StringTokenizer(columnLayout, ",");
            int n = Integer.parseInt(tokens.nextToken());
            TableColumnModel columns = this.getColumnModel();
            for (int i = 0; i < n && i < columns.getColumnCount() ; i++) {
                TableColumn col = columns.getColumn(i);
                int w = Integer.parseInt(tokens.nextToken());
                col.setWidth(w);
                col.setPreferredWidth(w);
                col.setMinWidth(20);
            }

            String maxtext0="AAAAAAAAAA";
            String maxtext4="AAAAAAAAAAAAAAAAAAAA";
            for(int i=0 ; i< getRowCount(); i++) {
                if( getModel().getValueAt(i, 4) != null) {
                    String text = getModel().getValueAt(i, 0).toString();
                    if ( text.length() > maxtext0.length() ) {
                        maxtext0 = text;
                    }
                }if( getModel().getValueAt(i, 4) != null) {
                    String text = getModel().getValueAt(i, 4).toString();
                    if ( text.length() > maxtext4.length() ) {
                        maxtext4 = text;
                    }
                }
            }

            columns.getColumn(0).setMaxWidth(getGraphics().getFontMetrics().stringWidth(maxtext0) *2 +15);
            columns.getColumn(4).setMaxWidth(getGraphics().getFontMetrics().stringWidth(maxtext0) *2 +15);
            columns.getColumn(2).setMinWidth(25);
            columns.getColumn(2).setMaxWidth(25);
        } catch (NumberFormatException t) {
            // ignore, l'erreur sera corrigée par la prochaine sauvegarde
        }
    }

    /**
     * Return column layout - a string that can be used to return column widths and sorting
     */
    private void saveColumnLayout() {

        TableColumnModel columns = this.getColumnModel();

        WordBuffer columnLayout = new WordBuffer(",");
        columnLayout.append(columns.getColumnCount());

        for (int c = 0; c < columns.getColumnCount(); c++) {
            columnLayout.append(columns.getColumn(c).getWidth());
        }
        NbPreferences.forModule(MergeTable.class).put("MergeColumnLayout", columnLayout.toString());

    }

    /**
     * Cette classe gère l'affichage des cellules de la table
     */
    private class MergeTableRenderer extends JLabel implements TableCellRenderer {

        public MergeTableRenderer() {
            setOpaque(true);
        }

        /**
         *
         * @param table
         * @param value
         * @param isSelected
         * @param hasFocus
         * @param row
         * @param column
         * @return
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            ProposalRuleTableModel model = (ProposalRuleTableModel) table.getModel();
            setFont(table.getFont());
            setToolTipText(null);
            // je choisis le format d'affichage du texte
            int modelColumn = column;
            if ( value != null ) {
                if (value instanceof PropertyDate) {
                    PropertyDate date = (PropertyDate)value;
                    setText(date.getDisplayValue());
                    if ( column==1 && date.getPhrase() != null && !date.getPhrase().isEmpty()) {
                        //setToolTipText(((PropertyDate)value).getPhrase());
                        setToolTipText(wrapToolTip(date.getPhrase(), 80));
                    }
                } else if (value instanceof PropertyPlace) {
                    PropertyPlace place = (PropertyPlace)value;
                    setText(place.getDisplayValue());
                } else if (value instanceof MergeTableAction) {
                    MergeTableAction action = (MergeTableAction)value;
                    setText(action.getText() );
                    setToolTipText(action.getToolTipText() );

                } else {
                    String comment ;
                    if (value instanceof Property) {
                        comment = ((Property) value).getValue();
                    } else {
                        comment = value.toString();
                    }
                    setText(comment.replace('\n', ' '));

                    // j'affiche un tooltip pour les commentaires et les professions
                    if ( comment.length() > 8
                          && (column == 1 || column==3) && !value.toString().isEmpty())
                    {
                        setToolTipText(wrapToolTip(comment, 80));
                    }
                }
            } else {
                // la valeur est nulle
                setText("");
            }

            // je choisis la couleur de fond de la colonne 3
              switch (modelColumn) {
                case 3:
                    setForeground(table.getForeground());
                    switch (model.getCompareResult(row)) {
                        case EQUAL:
                        case MANDATORY:
                        case NOT_APPLICABLE:
                            setBackground(table.getBackground());
                            break;
                        case CONFLICT:
                            setBackground(Color.PINK);
                            break;
                        case COMPATIBLE:
                        default:
                            if (!model.isMergeChanged(row)) {
                                setBackground(blueColor);
                            } else {
                                setBackground(greyColor);
                            }
                    }
                    break;
                case 4:
                    if (value instanceof MergeTableAction &&  ((MergeTableAction)value).isClickable() ) {
                        // j'affiche en bleu pour signaler une action possible avec la souris
                        setForeground(Color.blue);
                    } else {
                        setForeground(table.getForeground());
                    }
                    setBackground(table.getBackground());
                    break;
                default:
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                    break;
            }
            return this;

        }
    }

    /**
     * convertit en HTML et "wrappe" le texte d'un tooltip
     * @param tip
     * @param maxLength longueur maximale des lignes
     * @return
     */
    public static String wrapToolTip(String tip, int maxLength) {
        // marge de detection d'un espace pour wrapper
        final int MARGING = 20;
        int position = 0;

        StringBuilder sb = new StringBuilder("<html>");
        while (position < tip.length()) {
            String buffer;
            if (position + maxLength + MARGING > tip.length()) {
                buffer = tip.substring(position);
            } else {
                buffer = tip.substring(position, position + maxLength + MARGING );
            }

            int firstReturn = buffer.indexOf('\n');
            if (firstReturn != -1) {
                // je decoupe au niveau du caractere '\n'
                sb.append(tip.substring(position, position + firstReturn)).append("<br>");
                position = position + firstReturn + 1;
            } else {
                if ( buffer.length() > maxLength ) {
                    // je cherche une découpe entre les mots
                    int lastSpace = buffer.lastIndexOf(' ');
                    if (lastSpace != -1) {
                        // je decoupe au dernier espace (je ne copie pas l'espace
                        sb.append(tip.substring(position, position + lastSpace)).append("<br>");
                        position = position + lastSpace + 1;
                    } else {
                        int separator = buffer.lastIndexOf(',');
                        if (separator != -1) {
                            // je decoupe au dernier separateur
                            sb.append(tip.substring(position, position + separator + 1)).append("<br>");
                            position = position + separator + 1;
                        } else {
                            // je decoupe arbitrairement à la longueur max
                            sb.append(tip.substring(position, position + maxLength +1)).append("<br>");
                            position = position + maxLength;
                        }
                    }
                } else {
                    // je prends toute la ligne
                    sb.append(tip.substring(position));
                    break;
                }
            }
        }

        sb.append(("</html>"));
        return sb.toString();
    }
}
