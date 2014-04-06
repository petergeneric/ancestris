package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.MergeModel.CompareResult;
import ancestris.modules.releve.dnd.MergeModel.RowType;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.Source;
import genj.util.WordBuffer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JTable;
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
    private EntityActionManager entityActionManager = null;
    static private Color yellowColor = new Color(240, 240, 10);
    static private Color blueColor = new Color(200, 255, 255);
    static private Color greyColor = new Color(240, 240, 240);
    static private String entityCursorToolTip = "<html>Simple clic: centrer dans l'arbre.<br>Double clic: racine de l'arbre</html>";

    public  MergeTable() {
        setPreferredSize(null);
        setAutoResizeMode(AUTO_RESIZE_OFF );
        //setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN );
        setSelectionBackground(getBackground());
        MergeTableRenderer mergeTableRenderer = new MergeTableRenderer();
        setDefaultRenderer(Object.class, mergeTableRenderer);
        loadColumnLayout();
    }

    public void setModel(MergeModel model) {
        super.setModel(model);
        loadColumnLayout();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                int row = target.rowAtPoint(e.getPoint());
                int column = target.columnAtPoint(e.getPoint());

                if (column == 4) {
                    Entity entity = (Entity) ((MergeModel) getModel()).getValueAt(row, column);
                    if (entityActionManager != null && entity != null) {
                        if (e.getClickCount() == 2) {
                            entityActionManager.setRoot(entity);
                        } else {
                            entityActionManager.show(entity);
                        }
                    }
                }
            }        
        });

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                //int cModel = target.columnAtPoint(e.getPoint());
                //int column = target.convertColumnIndexToView(cModel);
                int row = target.rowAtPoint(e.getPoint());
                int column = target.columnAtPoint(e.getPoint());
                Object value = ((MergeModel) getModel()).getValueAt(row, column);
                if (entityActionManager != null && value != null) {
                    if (value instanceof Indi || value instanceof Fam) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    /**
     * cet methode masque les check box inutiles dans la colonne 2
     * @param renderer
     * @param row
     * @param column
     * @return
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        CompareResult cr = ((MergeModel)getModel()).getCompareResult(row) ;
        if (column == 2 && ( cr == MergeModel.CompareResult.NOT_APPLICABLE
                  || cr == MergeModel.CompareResult.EQUAL)  ) {
            JLabel label = new JLabel("");// Box.createRigidArea(c.getPreferredSize());
            //label.setBackground(Color.LIGHT_GRAY);
            label.setOpaque(true);
            return label;
        }  else {
            return c;
        }
    }


    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        CompareResult cr = ((MergeModel)getModel()).getCompareResult(row) ;
        if (column == 2 &&( cr == MergeModel.CompareResult.NOT_APPLICABLE
                  || cr == MergeModel.CompareResult.EQUAL) ) {
            return new JLabel("");//Box.createRigidArea(c.getPreferredSize());
        }  else {
            return c;
        }
    };
//
//
//    @Override
//    public TableCellEditor getCellEditor(final int row, int column) {
//        //int modelColumn = convertColumnIndexToModel(column);
//        final int modelColumn = column;
//        if ((modelColumn == 1 || modelColumn == 3) && ((MergeModel)getModel()).getChoice(row, modelColumn) != null) {
//            JComboBox comboBox = new JComboBox(((MergeModel)getModel()).getChoice(row, modelColumn));
//            comboBox.setEditable(false);
//            comboBox.setRenderer(new MergeCellComboRenderer());
//            return new DefaultCellEditor(comboBox);
//        } else {
//            return super.getCellEditor(row, column);
//        }
//    }

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
            }
        } catch (Throwable t) {
            // ignore
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

    void setEntityActionManager(EntityActionManager entityActionManager) {
        this.entityActionManager = entityActionManager;
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
            MergeModel model = (MergeModel) table.getModel();
            MergeModel.MergeRow mergeRow = model.getRow(row);
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
                } else if (value instanceof Source) {
                    if ( column == 4 ) {
                        setText(((Source)value).getId());
                    } else {
                        setText(((Source)value).getTitle());
                    }
                } else if (value instanceof Entity) {
                    if ( column == 4 ) {
                        setText(((Entity)value).getId());
                        // j'affiche un tooltip pour
                        setToolTipText(entityCursorToolTip);
                    } else {
                        setText(((Entity)value).getDisplayValue());
                    }
                } else if (value instanceof PropertyEvent) {
                    setText(((PropertyEvent)value).getPropertyValue("TYPE"));                    
                } else {
                    // j'affiche un tooltip pour les commentaires et les professions
                    if ( (mergeRow.rowType == RowType.EventComment
                          ||  mergeRow.rowType == RowType.IndiOccupation
                          ||  mergeRow.rowType == RowType.IndiMarriedOccupation
                          ||  mergeRow.rowType == RowType.IndiFatherOccupation
                          ||  mergeRow.rowType == RowType.IndiMotherOccupation
                          ||  mergeRow.rowType == RowType.WifeOccupation
                          ||  mergeRow.rowType == RowType.WifeMarriedOccupation
                          ||  mergeRow.rowType == RowType.WifeFatherOccupation
                          ||  mergeRow.rowType == RowType.WifeMotherOccupation )
                          && (column == 1 || column==3) && !value.toString().isEmpty()) {
                        //String tooltipText = "<html>";
                        //tooltipText += value.toString().replace("\n", "<br>");
                        //tooltipText += "</html>";

                        setToolTipText(wrapToolTip(value.toString(), 80));
                    }
                    setText(value.toString().replace('\n', ' '));
                }
            } else {
                // la valeur est nulle
                if ( column == 4 ) {
                    if (mergeRow.compareResult != CompareResult.NOT_APPLICABLE) {
                        switch ( mergeRow.rowType) {
                            case EventSource :
                                setText("Nouvelle source");
                                break;
                            case MarriageFamily :
                            case IndiParentFamily:
                            case IndiMarriedFamily :
                            case WifeParentFamily:
                            case WifeMarriedFamily:
                                setText("Nouvelle famille");
                                break;
                            case IndiLastName :
                            case IndiFatherLastName :
                            case IndiMotherLastName :
                            case WifeLastName :
                            case WifeFatherLastName :
                            case WifeMotherLastName :
                                setText("Nouvel individu");
                                break;
                            default:
                                setText("");
                        }
                    } else {
                        // la comparaison est applicable , je n'affiche rien dans la colonne 4
                        setText("");
                    }
                } else {
                    setText("");
                }
            }

            // je choisis la couleur de fond
            if (mergeRow.rowType == RowType.Separator) {
                setBackground(greyColor);
                setForeground(table.getForeground());
            } else {
                switch (modelColumn) {
                    case 0:
                        setBackground(greyColor);
                        setForeground(table.getForeground());
                        break;
                    case 1:
                        setBackground(table.getBackground());
                        setForeground(table.getForeground());
                        break;
                    case 3:
                        setForeground(table.getForeground());
                        switch (model.getCompareResult(row)) {
                            case NOT_APPLICABLE:
                                setBackground(table.getBackground());
                                break;
                            case CONFLIT:
                                setBackground(Color.PINK);
                                break;
                            case EQUAL:
                                setBackground(table.getBackground());
                                break;
                            default:
                                if (mergeRow.merge == mergeRow.merge_initial) {
                                    setBackground(blueColor);
                                } else {
                                    setBackground(yellowColor);
                                }
                        }
                        break;
                    case 4:
                        if (entityActionManager != null && value != null && (value instanceof Fam || value instanceof Indi)) {
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
